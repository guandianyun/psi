package com.bytechainx.psi.common.service.setting;


import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.BizException;
import com.bytechainx.psi.common.CommonConfig;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsAttrTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PrintModeTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.SalePayTypeEnum;
import com.bytechainx.psi.common.kit.PackageUtil;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.model.CustomerCategory;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.CustomerPriceLevel;
import com.bytechainx.psi.common.model.GoodsAttribute;
import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.model.GoodsSpec;
import com.bytechainx.psi.common.model.GoodsSpecOptions;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.SupplierCategory;
import com.bytechainx.psi.common.model.SupplierInfo;
import com.bytechainx.psi.common.model.TenantPrintTemplate;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.model.TraderBookAccount;
import com.bytechainx.psi.common.model.TraderFundType;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Ret;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;

import cn.hutool.core.io.FileUtil;


/**
* 系统重置
*/
public class SystemResetService extends CommonService {

	private static final Log LOG = Log.getLog(SystemResetService.class);
	// 无需备份和恢复的数据表
	public static String[] exclued_tables = { "system_region", "system_module", "system_oper", "system_holiday", "tenant_org", "tenant_account", "tenant_product_order", "tenant_sms_order",
			"tenant_pay_order","tenant_pay_order_log", "tenant_merchant", "tenant_trader_order", "tenant_module","tenant_db_log", "tenant_db_backup", "tenant_oper_log", "tenant_admin", "tenant_role", "tenant_role_oper_ref", "customer_weixin_mobile", "customer_weixin_user",
			"system_bank", "system_business_category", "tenant_wxmp_config", "tenant_merchant_info", "tenant_merchant_account", "tenant_merchant_credential", "tenant_merchant_certification", "system_weixin_open", "tenant_weixin_xcx", "tenant_weixin_xcx_audit"};
	// 清除基础数据的表
	public static String[] clear_data_tables = {"inventory_checking","inventory_swap","purchase_book_order","purchase_order",
			"purchase_reject_order","sale_book_order","sale_order","sale_reject_order",
			"inventory_stock","inventory_batch_quality_stock","inventory_batch_quality_stock", "inventory_stock_log", 
			"trader_pay_order", "trader_receipt_order", "trader_income_expenses", "trader_transfer_order", "trader_fund_order", 
			"trader_book_account_logs", "trader_customer_receivable", "trader_supplier_payable","goods_print_tag","mall_notice"};
	
	/**
	 * 系统重置
	 * @param tenantOrgId
	 * @param adminId
	 * @param dataType 0:系统初始化，1：清除单据
	 * @return
	 */
	public Ret resetData(int adminId, Integer dataType, String ip) {
		// 开始清空数据
		if(dataType == 0) { // 系统初始化
			systemInitData();
		} else if(dataType == 1) { // 清除单据
			cleanOrderData();
		}
		
		return Ret.ok("系统重置成功");
	}

	/**
	 * 清除单据
	 * @param tenantOrgId
	 */
	private void cleanOrderData() {
		for (int i = 0; i < clear_data_tables.length; i++) {
			try {
				String tableName = clear_data_tables[i];
				
				Db.delete("delete from " + tableName + " ");
				
			} catch (Exception e) {
				LOG.error("删除租户数据异常", e);
				throw new BizException("删除租户数据异常"+e.getMessage());
			}
		}
		
		Db.update("update trader_balance_account set balance = 0 where tenant_org_id = "  );
		Db.update("update trader_balance_store_ref set balance = 0 where tenant_org_id = "  );
		Db.update("update trader_book_account set in_amount = 0, out_amount= 0, pay_amount = 0  where tenant_org_id = "  );
	}

	/**
	 * 系统初始化
	 * @param tenantOrgId
	 */
	private void systemInitData() {
		cleanAll();
		initTenantData();
	}
	
	/**
	 * 删除租户数据
	 * @param tenantOrgId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Ret cleanAll() {
		// 不需要清除的数据
		Set<String> excluedOrgIdTable = new HashSet<String>();
		for (int i = 0; i < exclued_tables.length; i++) {
			excluedOrgIdTable.add(exclued_tables[i]);
		}

		String packageName = "com.bytechainx.psi.common.model";
		List<String> classNames = PackageUtil.getClassName(packageName, true);
		for (int i = 0; i < classNames.size(); i++) {
			try {
				Table table = TableMapping.me().getTable((Class<? extends Model>) Class.forName(classNames.get(i)));
				if(table == null) {
					continue;
				}
				String tableName = table.getName();
				if (excluedOrgIdTable.contains(tableName)) {
					continue;
				}
				Db.delete("delete from " + tableName + " ");
			} catch (Exception e) {
				LOG.error("删除租户数据异常", e);
				throw new BizException("删除租户数据异常"+e.getMessage());
			}
		}
		return Ret.ok();
	}
	
	/**
	 * 初始化租户的基本数据
	 * @param tenantOrgId
	 * @return
	 */
	public Ret initTenantData() {
		String[] accountNams = new String[] { "现金", "微信", "支付宝", "在线支付" };

		for (String name : accountNams) {
			// 创建结算帐户
			TraderBalanceAccount balanceAccount = new TraderBalanceAccount();
			balanceAccount.setName(name);
			balanceAccount.setCode(PinYinUtil.getFirstSpell(name));
			balanceAccount.setAccountName("");
			balanceAccount.setAccountNo("");
			balanceAccount.setCreatedAt(new Date());
			balanceAccount.setDefaultFlag(FlagEnum.YES.getValue());
			balanceAccount.setPosition(100);
			balanceAccount.setUpdatedAt(new Date());
			if(StringUtils.equals(name, "在线支付")) { // 默认禁用，等开通后再开启
				balanceAccount.setDataStatus(DataStatusEnum.disable.getValue());
			}
			balanceAccount.save();

		}

		Integer customerPriceLevelId1 = 0; // 零售价ID
		Integer customerPriceLevelId2 = 0; // 批发价ID

		// 创建客户价格等级
		String[] priceLevels = new String[] { "零售价", "批发价" };
		for (String level : priceLevels) {
			CustomerPriceLevel customerPriceLevel = new CustomerPriceLevel();
			customerPriceLevel.setCreatedAt(new Date());
			customerPriceLevel.setDefaultFlag(FlagEnum.YES.getValue());
			customerPriceLevel.setDataStatus(DataStatusEnum.enable.getValue());
			customerPriceLevel.setName(level);
			customerPriceLevel.setUpdatedAt(new Date());
			customerPriceLevel.save();

			if (level.equals("零售价")) {
				customerPriceLevelId1 = customerPriceLevel.getId();
			}
			if (level.equals("批发价")) {
				customerPriceLevelId2 = customerPriceLevel.getId();
			}
		}
		Integer customerCategoryId = null;
		String[] customerCategoryNames = new String[] { "默认分类" };
		for (String customerCategoryName : customerCategoryNames) {
			CustomerCategory customerCategory = new CustomerCategory();
			customerCategory.setCreatedAt(new Date());
			customerCategory.setName(customerCategoryName);
			customerCategory.setUpdatedAt(new Date());
			customerCategory.save();
			
			customerCategoryId = customerCategory.getId();
		}

		// 创建往来账帐户
		String[] custNames = new String[] { "零售客户", "批发客户" };
		for (String custName : custNames) {
			TraderBookAccount traderBookAccount = new TraderBookAccount();
			traderBookAccount.setCreatedAt(new Date());
			traderBookAccount.setInAmount(BigDecimal.ZERO);
			traderBookAccount.setOpenBalance(BigDecimal.ZERO);
			traderBookAccount.setOutAmount(BigDecimal.ZERO);
			traderBookAccount.setPayAmount(BigDecimal.ZERO);
			traderBookAccount.setUpdatedAt(new Date());
			traderBookAccount.save();

			CustomerInfo customer = new CustomerInfo();
			customer.setCreatedAt(new Date());
			customer.setDefaultFlag(FlagEnum.YES.getValue());
			customer.setName(custName);
			customer.setCode(PinYinUtil.getFirstSpell(custName));
			customer.setUpdatedAt(new Date());
			if (custName.equals("零售客户")) {
				customer.setCustomerPriceLevelId(customerPriceLevelId1);
			} else if (custName.equals("批发客户")) {
				customer.setCustomerPriceLevelId(customerPriceLevelId2);
			}
			customer.setDataStatus(DataStatusEnum.enable.getValue());
			customer.setDiscount(new BigDecimal("1"));
			customer.setMobile("");
			customer.setPayType(SalePayTypeEnum.nowCash.getValue());
			customer.setTraderBookAccountId(traderBookAccount.getId());
			customer.setCustomerCategoryId(customerCategoryId);
			customer.save();
		}
		// 创建默认零散供应商
		TraderBookAccount traderBookAccount = new TraderBookAccount();
		traderBookAccount.setCreatedAt(new Date());
		traderBookAccount.setInAmount(BigDecimal.ZERO);
		traderBookAccount.setOpenBalance(BigDecimal.ZERO);
		traderBookAccount.setOutAmount(BigDecimal.ZERO);
		traderBookAccount.setUpdatedAt(new Date());
		traderBookAccount.save();

		Integer supplierCategoryId = null;
		String[] supplierCategoryNames = new String[] { "默认分类" };
		for (String supplierCategoryName : supplierCategoryNames) {
			SupplierCategory supplierCategory = new SupplierCategory();
			supplierCategory.setCreatedAt(new Date());
			supplierCategory.setName(supplierCategoryName);
			supplierCategory.save();
			
			supplierCategoryId = supplierCategory.getId();
		}
		
		SupplierInfo supplier = new SupplierInfo();
		supplier.setCreatedAt(new Date());
		supplier.setDefaultFlag(FlagEnum.YES.getValue());
		supplier.setName("零散供应商");
		supplier.setCode(supplier.getName());
		supplier.setUpdatedAt(new Date());
		supplier.setMobile("");
		supplier.setDataStatus(DataStatusEnum.enable.getValue());
		supplier.setTraderBookAccountId(traderBookAccount.getId());
		supplier.setSupplierCategoryId(supplierCategoryId);
		supplier.save();
		
		// FIXME 商品的规格，单位，属性，可以根据不同行业创建默认的数据
		String[] unitNames = new String[] { "支", "只", "桶", "箱", "套", "斤", "件" };
		for (String unitName : unitNames) {
			GoodsUnit goodsUnit = new GoodsUnit();
			goodsUnit.setCreatedAt(new Date());
			goodsUnit.setDataStatus(DataStatusEnum.enable.getValue());
			goodsUnit.setName(unitName);
			goodsUnit.setUpdatedAt(new Date());
			goodsUnit.save();
		}
		String[] specNames = new String[] { "颜色" };
		for (String specName : specNames) {
			GoodsSpec goodsSpec = new GoodsSpec();
			goodsSpec.setCreatedAt(new Date());
			goodsSpec.setDataStatus(DataStatusEnum.enable.getValue());
			goodsSpec.setName(specName);
			goodsSpec.setUpdatedAt(new Date());
			goodsSpec.save();
			
			String[] specOptionsNames = new String[] { "红色", "白色", "绿色", "蓝色", "黑色", "黄色" };
			for (String specOptionsName : specOptionsNames) {
				GoodsSpecOptions goodsSpecOptions = new GoodsSpecOptions();
				goodsSpecOptions.setDataStatus(DataStatusEnum.enable.getValue());
				goodsSpecOptions.setGoodsSpecId(goodsSpec.getId());
				goodsSpecOptions.setOptionValue(specOptionsName);
				goodsSpecOptions.save();
			}
		}
		
		String[] goodsCategoryNames = new String[] { "默认分类"};
		for (String goodsCategoryName : goodsCategoryNames) {
			GoodsCategory goodsCategory = new GoodsCategory();
			goodsCategory.setCreatedAt(new Date());
			goodsCategory.setParentId(0);
			goodsCategory.setName(goodsCategoryName);
			goodsCategory.setUpdatedAt(new Date());
			goodsCategory.save();
		}
		
		String[] goodsAttributeNames = new String[] { "品牌" };
		for (String goodsAttributeName : goodsAttributeNames) {
			GoodsAttribute goodsAttribute = new GoodsAttribute();
			goodsAttribute.setCreatedAt(new Date());
			goodsAttribute.setAttrType(GoodsAttrTypeEnum.select.getValue());
			goodsAttribute.setAttrValues("其他");
			goodsAttribute.setDefaultFlag(FlagEnum.NO.getValue());
			goodsAttribute.setName(goodsAttributeName);
			goodsAttribute.setUpdatedAt(new Date());
			goodsAttribute.save();
		}
		
		String[] traderFundTypeNames = new String[] { "房租", "电费", "水费", "通讯费", "员工工资" };
		for (String traderFundTypeName : traderFundTypeNames) {
			TraderFundType traderFundType = new TraderFundType();
			traderFundType.setCreatedAt(new Date());
			traderFundType.setName(traderFundTypeName);
			traderFundType.setFundFlow(FundFlowEnum.expenses.getValue());
			traderFundType.setPostion(0);
			traderFundType.setUpdatedAt(new Date());
			traderFundType.save();
		}
		
		traderFundTypeNames = new String[] { "分红" };
		for (String traderFundTypeName : traderFundTypeNames) {
			TraderFundType traderFundType = new TraderFundType();
			traderFundType.setCreatedAt(new Date());
			traderFundType.setName(traderFundTypeName);
			traderFundType.setFundFlow(FundFlowEnum.income.getValue());
			traderFundType.setPostion(0);
			traderFundType.setUpdatedAt(new Date());
			traderFundType.save();
		}
		
		for(PrintTemplateOrderTypeEnum orderTypeEnum : PrintTemplateOrderTypeEnum.values()) {
			String content = FileUtil.readString(new File(CommonConfig.commonConfigPath+"/printTpl/"+orderTypeEnum.name()+".tpl"), "utf-8");
			String tplName = "二等分";
			String paperType = "241*140";
			if(orderTypeEnum.getValue() == PrintTemplateOrderTypeEnum.goods_tag.getValue()) {
				tplName = "小票标签";
				paperType = "80mm*116mm";
			}
			TenantPrintTemplate printTemplate = new TenantPrintTemplate();
			printTemplate.setContent(content);
			printTemplate.setCreatedAt(new Date());
			printTemplate.setDefaultFlag(FlagEnum.YES.getValue());
			printTemplate.setName(tplName);
			printTemplate.setOrderType(orderTypeEnum.getValue());
			printTemplate.setPaperType(paperType);
			printTemplate.setPrintMode(PrintModeTypeEnum.all.getValue());
			printTemplate.setUpdatedAt(new Date());
			printTemplate.save();
		}
		
		return Ret.ok();
	}


}