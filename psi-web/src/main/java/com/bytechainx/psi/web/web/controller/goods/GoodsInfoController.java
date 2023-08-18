package com.bytechainx.psi.web.web.controller.goods;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderUnitTypeEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.annotation.Permission;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.CustomerPriceLevel;
import com.bytechainx.psi.common.model.GoodsAttribute;
import com.bytechainx.psi.common.model.GoodsAttributeRef;
import com.bytechainx.psi.common.model.GoodsCategory;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsSpec;
import com.bytechainx.psi.common.model.GoodsSpec.GoodsSpecDto;
import com.bytechainx.psi.common.model.GoodsSpecRef;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.service.goods.GoodsCategoryService;
import com.bytechainx.psi.common.service.goods.GoodsInfoService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
* 商品管理
*/
@Path("/goods/goods/info")
public class GoodsInfoController extends BaseController {

	@Inject
	private GoodsInfoService goodsInfoService;
	@Inject
	private GoodsCategoryService goodsCategoryService;

	/**
	* 首页
	*/
	@Permission(Permissions.goods_goods_info)
	public void index() {
		List<GoodsCategory> topCategoryList = GoodsCategory.dao.findTop();
		setAttr("topCategoryList", topCategoryList);
	}

	/**
	* 列表
	*/
	@Permission(Permissions.goods_goods_info)
	public void list() {
		int pageNumber = getInt("pageNumber", 1);
		Integer goodsCategoryId = getInt("goods_category_id");
		Integer supplierInfoId = getInt("supplier_info_id");
		Boolean hideStopFlag = getBoolean("hide_stop_flag"); // 隐藏停用商品
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("goods_category_id", goodsCategoryId);
		condKv.set("supplier_info_id", supplierInfoId);
		
		condKv.set("name,remark", keyword); // 多字段模糊查询
		
		if(hideStopFlag) {
			condKv.set("data_status", DataStatusEnum.enable.getValue());
		}
		
		Page<GoodsInfo> page = goodsInfoService.paginate(condKv, pageNumber, pageSize);
		setAttr("page", page);
		setAttr("hideStopFlag", hideStopFlag);
	}

	/**
	* 查看
	*/
	@Permission(Permissions.goods_goods_info_show)
	public void show() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		List<GoodsAttribute> goodsAttributeList = GoodsAttribute.dao.findAll();
		GoodsUnit stockUnit = info.getStockMainUnit();
		List<CustomerPriceLevel> priceLevelList = CustomerPriceLevel.dao.findAll();
		setAttr("priceLevelList", priceLevelList);
		setAttr("stockUnit", stockUnit);
		setAttr("goodsAttributeList", goodsAttributeList);
		setAttr("goodsInfo", info);
	}


	/**
	* 添加
	*/
	@Permission(Permissions.goods_goods_info_create)
	public void add() {
		setAttrCommon();
		setAttr("barCode", DateUtil.getSecondNumber(new Date()));
	}

	/**
	* 新增
	*/
	@Permission(Permissions.goods_goods_info_create)
	@Before(Tx.class)
	public void create() {
		GoodsInfo info = getModel(GoodsInfo.class, "", true);
		
		info.setGoodsImages(getParaValues("thumbs"), getParaValues("originals"));
		
		String specFlag = get("spec_flag");
		if(StringUtils.equals(specFlag, "on")) {
			info.setSpecFlag(true);
		} else {
			info.setSpecFlag(false);
		}
		String saleFlag = get("sale_flag");
		if(StringUtils.equals(saleFlag, "on")) {
			info.setSaleFlag(true);;
		} else {
			info.setSaleFlag(false);;
		}
		String purchaseFlag = get("purchase_flag");
		if(StringUtils.equals(purchaseFlag, "on")) {
			info.setPurchaseFlag(true);;
		} else {
			info.setPurchaseFlag(false);;
		}
		
		List<GoodsSpecRef> goodsSpecRefList = new ArrayList<>();
		Integer[] goodsSpecIds = getParaValuesToInt("goods_spec_ids");
		if(info.getSpecFlag() && goodsSpecIds != null && goodsSpecIds.length > 0) {
			int position = 1; // 规格要排序，从小到大
			for (Integer specId : goodsSpecIds) {
				for (GoodsSpecRef ref : goodsSpecRefList) {
					if(ref.getGoodsSpecId().intValue() == specId.intValue()) {
						renderJson(Ret.fail("所选规格存在重复"));
						return;
					}
				}
				String specValueIds = get("spec_value_ids_"+specId);
				if(StringUtils.isEmpty(specValueIds)) {
					continue;
				}
				GoodsSpecRef ref = new GoodsSpecRef();
				ref.setGoodsSpecId(specId);
				ref.setSpecValue(specValueIds);
				ref.setPosition(position++);
				goodsSpecRefList.add(ref);
			}
		}
		
		List<GoodsAttributeRef> goodsAttributeRefList = new ArrayList<>();
		String[] attributeValues = getParaValues("goods_attribute_values");
		Integer[] attributeIds = getParaValuesToInt("goods_attribute_ids");
		if(attributeIds != null) {
			for (int index = 0; index < attributeIds.length; index++) {
				GoodsAttributeRef ref = new GoodsAttributeRef();
				ref.setGoodsAttributeId(attributeIds[index]);
				ref.setAttrValue(attributeValues[index]);
				goodsAttributeRefList.add(ref);
			}
		}
		
		Ret ret = goodsInfoService.create(info,goodsSpecRefList,goodsAttributeRefList);
		renderJson(ret);
	}



	/**
	* 编辑
	*/
	@Permission(Permissions.goods_goods_info_update)
	public void edit() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		setAttr("goodsInfo", info);
		if(StringUtils.isEmpty(info.getBarCode())) {
			setAttr("barCode", DateUtil.getSecondNumber(new Date()));
		}
		setAttrCommon();
		
	}
	
	/**
	* 编辑商品子件
	*/
	@Permission(Permissions.goods_goods_info_update)
	public void editAssembly() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null || info.getGoodsType() != GoodsTypeEnum.assembly.getValue()) { // 非组合商品
			renderError(404);
			return;
		}
		setAttr("goodsInfo", info);
	}
	
	/**
	 * 编辑多规格商品子件
	 */
	@Permission(Permissions.goods_goods_info_update)
	public void editChildGoods() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		String specIds = get("spec_ids"); // 规格ID数值，格式: id1:idvalue1|id2:idvalue2
		String specNames = get("spec_names");
		
		GoodsInfo info = GoodsInfo.dao.findById(id);
		
		setAttr("goodsInfo", info);
		setAttr("specIds", specIds);
		setAttr("specNames", specNames);
	}
	
	@Permission(Permissions.goods_goods_info_update)
	public void multiSpecList() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null || info.getGoodsType() != GoodsTypeEnum.assembly.getValue()) { // 非组合商品
			renderError(404);
			return;
		}
		setAttr("goodsInfo", info);
	}
	
	/**
	* 编辑价格
	*/
	@Permission(Permissions.goods_goods_info_updatePrice)
	public void editPrice() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		List<CustomerPriceLevel> priceLevelList = CustomerPriceLevel.dao.findAll();
		setAttr("goodsInfo", info);
		setAttr("priceLevelList", priceLevelList);
		keepPara("sourcePage");
	}
	
	/**
	* 价格设置
	*/
	@Permission(Permissions.goods_goods_info_updatePrice)
	@Before(Tx.class)
	public void updatePrice() {
		Integer goodsId = getInt("id");
		if(goodsId == null || goodsId <= 0) {
			renderError(404);
			return;
		}
		String[] goodsUnionIds = getParaValues("goods_union_ids");
		String[] barCodes = getParaValues("bar_codes");
		String[] costPrices = getParaValues("cost_prices");
		List<CustomerPriceLevel> priceLevelList = CustomerPriceLevel.dao.findAll();
		Kv salePriceList = Kv.create();
		for (CustomerPriceLevel priceLevel : priceLevelList) {
			String[] salePrices = getParaValues("sale_prices_"+priceLevel.getId());
			salePriceList.set(""+priceLevel.getId(), salePrices);
		}
		Ret ret = goodsInfoService.updatePrice(goodsId, goodsUnionIds, barCodes, costPrices, salePriceList);
		
		renderJson(ret);
	}
	
	/**
	* 批量价格
	*/
	@Permission(Permissions.goods_goods_info_updatePrice)
	public void batchPrice() {
		Integer goodsId = getInt("goodsId");
		if(goodsId == null || goodsId <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(goodsId);
		setAttr("goodsInfo", info);
		keepPara("priceName");
	}
	
	/**
	* 编辑库存预警设置
	*/
	@Permission(Permissions.goods_goods_info_updateStockConfig)
	public void editStockConfig() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(id);
		if(info == null) {
			renderError(404);
			return;
		}
		GoodsUnit stockUnit = info.getStockMainUnit();
		setAttr("goodsInfo", info);
		setAttr("stockUnit", stockUnit);
	}
	/**
	* 库存预警设置
	*/
	@Permission(Permissions.goods_goods_info_updateStockConfig)
	@Before(Tx.class)
	public void updateStockConfig() {
		Integer goodsId = getInt("id");
		if(goodsId == null || goodsId <= 0) {
			renderError(404);
			return;
		}
		GoodsInfo info = GoodsInfo.dao.findById(goodsId);
		if(info == null) {
			renderError(404);
			return;
		}
		String stockWarnFlag = get("stock_warn_flag");
		if(StringUtils.equals(stockWarnFlag, "on")) {
			info.setStockWarnFlag(true);
		} else {
			info.setStockWarnFlag(false);;
		}
		Integer stockWarnType = getInt("stock_warn_type");
		info.setStockWarnType(stockWarnType);
		info.setUpdatedAt(new Date());
		info.update();
		
		String[] goodsUnionIds = getParaValues("goods_union_ids");
		String[] lowStocks = getParaValues("low_stocks");
		String[] safeStocks = getParaValues("safe_stocks");
		String[] highStocks = getParaValues("high_stocks");
		
		Ret ret = goodsInfoService.updateStockWarnConfig(goodsId, goodsUnionIds, lowStocks, safeStocks, highStocks);
		
		renderJson(ret);
	}
	
	/**
	* 批量设置库存预警
	*/
	@Permission(Permissions.goods_goods_info_updateStockConfig)
	public void batchStock() {
		keepPara();
	}
	
	/**
	* 修改
	*/
	@Permission(Permissions.goods_goods_info_update)
	@Before(Tx.class)
	public void update() {
		GoodsInfo info = getModel(GoodsInfo.class, "", true);
		info.setGoodsImages(getParaValues("thumbs"), getParaValues("originals"));
		
		String specFlag = get("spec_flag");
		if(StringUtils.equals(specFlag, "on")) {
			info.setSpecFlag(true);
		} else {
			info.setSpecFlag(false);
		}
		String saleFlag = get("sale_flag");
		if(StringUtils.equals(saleFlag, "on")) {
			info.setSaleFlag(true);;
		} else {
			info.setSaleFlag(false);;
		}
		String purchaseFlag = get("purchase_flag");
		if(StringUtils.equals(purchaseFlag, "on")) {
			info.setPurchaseFlag(true);;
		} else {
			info.setPurchaseFlag(false);;
		}
		
		List<GoodsSpecRef> goodsSpecRefList = new ArrayList<>();
		Integer[] goodsSpecIds = getParaValuesToInt("goods_spec_ids");
		if(info.getSpecFlag() && goodsSpecIds != null && goodsSpecIds.length > 0) {
			int position = 1; // 规格要排序，从小到大
			for (Integer specId : goodsSpecIds) {
				for (GoodsSpecRef ref : goodsSpecRefList) {
					if(ref.getGoodsSpecId().intValue() == specId.intValue()) {
						renderJson(Ret.fail("所选规格存在重复"));
						return;
					}
				}
				String specValueIds = get("spec_value_ids_"+specId);
				if(StringUtils.isEmpty(specValueIds)) {
					continue;
				}
				GoodsSpecRef ref = new GoodsSpecRef();
				ref.setGoodsSpecId(specId);
				ref.setSpecValue(specValueIds);
				ref.setPosition(position++);
				goodsSpecRefList.add(ref);
			}
		}
		
		List<GoodsAttributeRef> goodsAttributeRefList = new ArrayList<>();
		String[] attributeValues = getParaValues("goods_attribute_values");
		Integer[] attributeIds = getParaValuesToInt("goods_attribute_ids");
		for (int index = 0; index < attributeIds.length; index++) {
			GoodsAttributeRef ref = new GoodsAttributeRef();
			ref.setGoodsAttributeId(attributeIds[index]);
			ref.setAttrValue(attributeValues[index]);
			goodsAttributeRefList.add(ref);
		}
		
		Ret ret = goodsInfoService.update(info,goodsSpecRefList,goodsAttributeRefList);
		
		renderJson(ret);
	}



	/**
	* 删除
	*/
	@Permission(Permissions.goods_goods_info_delete)
	@Before(Tx.class)
	public void delete() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsInfoService.delete(Arrays.asList(id));
		renderJson(ret);
	}



	/**
	* 停用
	*/
	@Permission(Permissions.goods_goods_info_disable)
	@Before(Tx.class)
	public void disable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsInfoService.disable(Arrays.asList(id));

		renderJson(ret);
	}
	
	/**
	* 启用
	*/
	@Permission(Permissions.goods_goods_info_disable)
	@Before(Tx.class)
	public void enable() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("ID不能为空"));
			return;
		}
		Ret ret = goodsInfoService.enable(Arrays.asList(id));

		renderJson(ret);
	}



	/**
	 * 设置公共数据
	 */
	private void setAttrCommon() {
		List<GoodsCategory> topCategoryList = GoodsCategory.dao.findTop();
		List<GoodsUnit> goodsUnitList = GoodsUnit.dao.findAll();
		List<GoodsSpec> goodsSpecList = GoodsSpec.dao.findAll();
		List<GoodsAttribute> goodsAttributeList = GoodsAttribute.dao.findAll();
		setAttr("topCategoryList", topCategoryList);
		setAttr("goodsUnitList", goodsUnitList);
		setAttr("goodsSpecList", goodsSpecList);
		setAttr("goodsAttributeList", goodsAttributeList);
		
	}

	/**
	* 导入
	*/
	@Permission(Permissions.goods_goods_info_createImport)
	public void createImport() {

		renderJson(Ret.ok());
	}
	
	
	/**
	 * 加载货品信息
	 */
	public void listByJson() {
		int pageNumber = getInt("pageNumber", 1);
		String keyword = get("keyword");
		Kv condKv = Kv.create();
		condKv.set("name,code,bar_code", keyword); // 多字段模糊查询
		condKv.set("data_status", DataStatusEnum.enable.getValue());
		Boolean purchaseFlag = getBoolean("purchase_flag");
		if(purchaseFlag != null && purchaseFlag) {
			condKv.set("purchase_flag", FlagEnum.YES.getValue());
		}
		Boolean saleFlag = getBoolean("sale_flag");
		if(saleFlag != null && saleFlag) {
			condKv.set("sale_flag", FlagEnum.YES.getValue());
		}
		
		Page<GoodsInfo> page = goodsInfoService.paginate(condKv, pageNumber, pageSize);
		renderJson(Ret.ok().set("data", page.getList()));
	}
	
	
	/**
	 * 查询货品规格
	 */
	public void listSpecByJson() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("商品不存在"));
			return;
		}
		String keyword = get("keyword");
		List<Map<String, String>> goodsSpecList = GoodsInfo.dao.findSpecByKeyword(id, keyword);
		
		renderJson(Ret.ok().set("data", goodsSpecList));
	}
	
	/**
	 * 查询货品单位
	 */
	public void listUnitByJson() {
		Integer id = getInt("id");
		if(id == null || id <= 0) {
			renderJson(Ret.fail("商品不存在"));
			return;
		}
		String keyword = get("keyword");
		int unitType = getInt("unit_type", OrderUnitTypeEnum.stock.getValue());
		
		GoodsInfo goodsInfo = GoodsInfo.dao.findById(id);
		List<GoodsUnit> unitList = goodsInfo.getGoodsUnitList(OrderUnitTypeEnum.getEnum(unitType));
		if(StringUtils.isEmpty(keyword)) {
			renderJson(Ret.ok().set("data", unitList));
			return;
		}
		List<GoodsUnit> _unitList = new ArrayList<>();
		for (GoodsUnit goodsUnit : unitList) {
			if(StringUtils.contains(goodsUnit.getName(), keyword)) {
				_unitList.add(goodsUnit);
			}
		}
		renderJson(Ret.ok().set("data", _unitList));
	}
	
	
	/**
	 * 查询商品价格
	 */
	public void priceByJson() {
		Integer customerInfoId = getInt("customer_info_id");
		Integer goodsInfoId = getInt("goods_info_id");
		Integer unitId = getInt("goods_unit_id");
		String specstring  = get("goods_spec_ids");
		
		GoodsSpecDto specDto = GoodsSpec.dao.getGoodsSpecDto(specstring);
		GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(goodsInfoId, specDto.getSpec1Id(), specDto.getSpecOption1Id(), specDto.getSpec2Id(), specDto.getSpecOption2Id(), specDto.getSpec3Id(), specDto.getSpecOption3Id(), unitId);
		if(goodsPrice == null) {
			goodsPrice = new GoodsPrice();
			goodsPrice.setCostPrice(BigDecimal.ZERO);
		}
		if(customerInfoId != null && customerInfoId > 0) { // 查找客户的销售价格
			BigDecimal salePrice = goodsPrice.getCustomerSalePrice(customerInfoId);
			if(salePrice != null) {
				goodsPrice.put("cust_price", salePrice);
			}
		}
		if(goodsPrice.get("cust_price") == null) {
			goodsPrice.put("cust_price", BigDecimal.ZERO);
		}
		
		renderJson(Ret.ok().set("data", goodsPrice));
	}

}