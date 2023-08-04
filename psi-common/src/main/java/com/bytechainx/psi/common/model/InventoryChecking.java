package com.bytechainx.psi.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.OrderCodeBuilder;
import com.bytechainx.psi.common.model.base.BaseInventoryChecking;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.redis.Redis;

/**
 * 盘点单
 * 
 */
@SuppressWarnings("serial")
public class InventoryChecking extends BaseInventoryChecking<InventoryChecking> {
	
	public static final InventoryChecking dao = new InventoryChecking().dao();
	
	public InventoryChecking findById(Integer id) {
		return InventoryChecking.dao.findFirst("select * from inventory_checking where id = ? limit 1", id);
	}
	
	/**
	 * 获取单据关联的商品
	 * @return
	 */
	public List<InventoryCheckingGoods> getOrderGoodsList() {
		return InventoryCheckingGoods.dao.find("select * from inventory_checking_goods where inventory_checking_id = ?", getId());
	}
	
	// 单据编号缓存redis key
	private static final String REDIS_ORDER_CODE_LIST_KEY = "order.code.inventory.checking.tenant.id.";

	/**
	 * 生成单据编号，从redis队列中获取
	 * @return
	 */
	public String generateOrderCode() {
		String code = Redis.use().lpop(REDIS_ORDER_CODE_LIST_KEY);
		if(code == null) {
			return null;
		}
		return "PDD"+code;
	}

	/**
	 * 预生成单据编号，存入redis队列
	 * @param tenantOrgId
	 */
	public void buildOrderCodes() {
		OrderCodeBuilder.build(REDIS_ORDER_CODE_LIST_KEY, "inventory_checking");
	}
	
	/**
	 * 盘点员
	 * @return
	 */
	public TenantAdmin getHandler() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getHandlerId(), "select * from tenant_admin where id = ?", getHandlerId());
	}
	/**
	 * 制单人
	 * @return
	 */
	public TenantAdmin getMakeMan() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getMakeManId(), "select * from tenant_admin where id = ?", getMakeManId());
	}
	/**
	 * 审核人
	 * @return
	 */
	public TenantAdmin getAuditor() {
		return TenantAdmin.dao.findFirstByCache(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "tenant.admin.id."+getAuditorId(), "select * from tenant_admin where id = ?", getAuditorId());
	}
	
	
	public List<String> getOrderImgList() {
		return Arrays.asList(StringUtils.split(getOrderImg() == null ? "" : getOrderImg(), ","));
	}
	
	/**
	 * 审核设置，单据是否需要审核
	 * @return
	 */
	public boolean getAuditConfigFlag() {
		return findAuditConfig();
	}
	
	public boolean findAuditConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_inventory_checking);
		return Boolean.parseBoolean(config.getAttrValue());
	}

	public Integer findRowsConfig() {
		TenantConfig config = TenantConfig.dao.findByKeyCache(TenantConfigEnum.rows_inventory_checking);
		return Integer.parseInt(config.getAttrValue());
	}
	
	
	/**
	 * 打印数据
	 * @return
	 */
	public Kv getPrintData() {
		Kv kv = Kv.create();
		kv.set("order_code", getOrderCode());
        kv.set("order_bar_code", "");//订单号(条码)
        
        kv.set("order_time", DateUtil.getDayStr(getOrderTime()));
        TenantOrg tenantOrg = TenantOrg.dao.findCacheById();
        kv.set("company_full_name", tenantOrg.getFullName());
        kv.set("company_name", tenantOrg.getName());
        kv.set("company_mobile", tenantOrg.getMobile());
        TenantAdmin admin = TenantAdmin.dao.findById(getHandlerId());
        kv.set("handler_name", admin.getRealName());
        kv.set("handler_mobile", admin.getMobile());
        kv.set("remark", getRemark());
        
        
        List<Kv> goodsKvs = new ArrayList<>();
        List<InventoryCheckingGoods> goodsList = getOrderGoodsList();
        for (int index = 0; index < goodsList.size(); index++) {
        	InventoryCheckingGoods orderGoods = goodsList.get(index);
        	Kv goodsKv = Kv.create();
        	goodsKv.set("index", index+1);
        	GoodsInfo goodsInfo = orderGoods.getGoodsInfo();
        	goodsKv.set("goods_name", goodsInfo.getName());
        	goodsKv.set("goods_spec", orderGoods.getGoodsSpecNames());
            goodsKv.set("goods_bar_code", goodsInfo.getBarCode());//条码
            goodsKv.set("goods_unit_name", orderGoods.getGoodsUnit().getName());
            goodsKv.set("goods_check_number", orderGoods.getCheckNumber().stripTrailingZeros().toPlainString());
            goodsKv.set("goods_profit_loss_number", orderGoods.getCheckNumber().subtract(orderGoods.getCurrentNumber()));
            goodsKv.set("goods_profit_loss", orderGoods.getProfitLoss());
            goodsKv.set("goods_remark", orderGoods.getRemark());
            
            goodsKvs.add(goodsKv);
            
		}
        
        TenantAdmin makeMan = getMakeMan();
        kv.set("make_man", makeMan.getRealName());
        kv.set("make_man_mobile", makeMan.getMobile());
        
        kv.set("list", goodsKvs);
        return kv;
	}
	
	/**
	 * 获取默认打印模板
	 * @return
	 */
	public TenantPrintTemplate getPrintDefaultTemplate() {
		TenantPrintTemplate printTemplate = TenantPrintTemplate.dao.findDefault(PrintTemplateOrderTypeEnum.inventory_checking.getValue());
        if (printTemplate == null) {
        	printTemplate = TenantPrintTemplate.dao.findDefault(PrintTemplateOrderTypeEnum.inventory_checking.getValue());
        }
        return printTemplate;
	}

	/**
	 * 打印模板列表
	 * @return
	 */
	public List<TenantPrintTemplate> getPrintTemplateList() {
		return TenantPrintTemplate.dao.find("select * from tenant_print_template where order_type = ?", PrintTemplateOrderTypeEnum.inventory_checking.getValue());
	}
	
	
	
}

