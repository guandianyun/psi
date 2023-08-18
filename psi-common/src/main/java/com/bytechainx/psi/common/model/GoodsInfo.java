package com.bytechainx.psi.common.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderUnitTypeEnum;
import com.bytechainx.psi.common.model.base.BaseGoodsInfo;

/**
 * 商品
 */
@SuppressWarnings("serial")
public class GoodsInfo extends BaseGoodsInfo<GoodsInfo> {
	
	public static final GoodsInfo dao = new GoodsInfo().dao();
	
	private List<GoodsImageRef> imageList = new ArrayList<>(); 
	
	public GoodsInfo findById(Integer id) {
		return GoodsInfo.dao.findFirst("select * from goods_info where id = ? and data_status != ? limit 1", id, DataStatusEnum.delete.getValue());
	}
	
	/**
	 * 库存预警配置
	 * @return
	 */
	public List<GoodsStockConfig> getGoodsStockConfigList() {
		return GoodsStockConfig.dao.find("select * from goods_stock_config where goods_info_id = ?", getId());
	}
	/**
	 * 商品关联的规格
	 * @return
	 */
	public List<GoodsSpecRef> getGoodsSpecRefList() {
		return GoodsSpecRef.dao.find("select * from goods_spec_ref where goods_info_id = ?", getId());
	}
	/**
	 * 第一个规格
	 * @return
	 */
	public GoodsSpecRef getFirstGoodsSpec() {
		return GoodsSpecRef.dao.findFirst("select * from goods_spec_ref where goods_info_id = ? order by position limit 0,1", getId());
	}
	/**
	 * 第二个规格
	 * @return
	 */
	public GoodsSpecRef getSecondGoodsSpec() {
		return GoodsSpecRef.dao.findFirst("select * from goods_spec_ref where goods_info_id = ? order by position limit 1,1", getId());
	}
	/**
	 * 第三个规格
	 * @return
	 */
	public GoodsSpecRef getThirdGoodsSpec() {
		return GoodsSpecRef.dao.findFirst("select * from goods_spec_ref where goods_info_id = ? order by position limit 2,1", getId());
	}
	/**
	 * 查询价格
	 * @param spec1Id
	 * @param specOption1I
	 * @param spec2Id
	 * @param specOption2Id
	 * @param spec3Id
	 * @param specOption3Id
	 * @param unitId
	 * @return
	 */
	public GoodsPrice getGoodsPrice(Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		GoodsPrice goodsPrice = GoodsPrice.dao.findFirst("select * from goods_price where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", getId(), spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
		if(goodsPrice != null) {
			goodsPrice.put("salePriceMap", JSONObject.parse(goodsPrice.getSalePrice()));
		}
		return goodsPrice;
	}
	/**
	 * 查询客户对应的商品规格价格
	 * @return
	 */
	public GoodsPrice getCustomerPrice(Integer customerInfoId, Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		GoodsPrice goodsPrice = getGoodsPrice(spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
		if(goodsPrice == null) {
			return null;
		}
		return getCustomerPrice(customerInfoId, goodsPrice);
	}
	
	/**
	 * 只返回一条价格，多规格也只返回一条用于展示
	 * @return
	 */
	public GoodsPrice getCustomerPrice(Integer customerInfoId) {
		GoodsPrice goodsPrice = getSalePrice();
		return getCustomerPrice(customerInfoId, goodsPrice);
	}
	
	/**
	 * 只返回一条价格，多规格也只返回一条用于展示
	 * @return
	 */
	public GoodsPrice getSalePrice() {
		Integer unitId = getMainUnitId();
		return GoodsPrice.dao.findFirst("select * from goods_price where goods_info_id = ? and unit_id = ? limit 1", getId(), unitId);
	}

	/**
	 * 获取客户的价格
	 * @param customerInfoId
	 * @param goodsPrice
	 */
	private GoodsPrice getCustomerPrice(Integer customerInfoId, GoodsPrice goodsPrice) {
		if(goodsPrice == null) {
			return null;
		}
		JSONObject salePrice = JSONObject.parseObject(goodsPrice.getSalePrice());
		if(salePrice == null) {
			return null;
		}
		CustomerPriceLevel priceLevel = null;
		CustomerInfo customerInfo = CustomerInfo.dao.findById(customerInfoId);
		if(customerInfo == null) { // 返回零售价
			priceLevel = CustomerPriceLevel.dao.findFirst("select * from customer_price_level where default_flag = ? and name like '%零售%'", FlagEnum.YES.getValue());
		} else if(customerInfo.getCustomerPriceLevelId() == null || customerInfo.getCustomerPriceLevelId() <= 0) { // 返回默认批发价
			priceLevel = CustomerPriceLevel.dao.findFirst("select * from customer_price_level where default_flag = ? and name like '%批发%'", FlagEnum.YES.getValue());
		} else {
			priceLevel = customerInfo.getCustomerPriceLevel(); // 客户的价格等级
		}
		if(priceLevel == null) {
			return null;
		}
		String priceString = salePrice.getString(priceLevel.getId()+"");
		if(StringUtils.isNotEmpty(priceString)) {
			goodsPrice.put("cust_price", new BigDecimal(priceString));
		}
		return goodsPrice;
	}
	
	/**
	 * 只返回一条采购价格，多规格也只返回一条用于展示
	 * @return
	 */
	public GoodsPrice getPurchasePrice() {
		Integer unitId = getMainUnitId();
		return GoodsPrice.dao.findFirst("select * from goods_price where goods_info_id = ? and unit_id = ? limit 1", getId(), unitId);
	}
	
	public InventoryStock getGoodsStock(Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return InventoryStock.dao.findFirst("select * from inventory_stock where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", getId(), spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	
	public BigDecimal getGoodsSumStock() {
		InventoryStock stock = InventoryStock.dao.findFirst("select ifnull(sum(stock),0) as stock from inventory_stock where goods_info_id = ? limit 1", getId());
		stock.setGoodsInfoId(getId());
		BigDecimal stockNumber = stock.getStock();
		if(stockNumber == null) {
			return BigDecimal.ZERO;
		}
		return stockNumber;
	}
	
	/**
	 * 商品库存预警设置
	 * @param spec1Id
	 * @param specOption1I
	 * @param spec2Id
	 * @param specOption2Id
	 * @param spec3Id
	 * @param specOption3Id
	 * @param unitId
	 * @return
	 */
	public GoodsStockConfig getGoodsStockConfig(Integer spec1Id, Integer specOption1I, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		return GoodsStockConfig.dao.findFirst("select * from goods_stock_config where goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ? limit 1", getId(), spec1Id, specOption1I, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
	}
	/**
	 * 商品关联的属性
	 * @return
	 */
	public List<GoodsAttributeRef> getGoodsAttributeRefList() {
		return GoodsAttributeRef.dao.find("select * from goods_attribute_ref where goods_info_id = ?", getId());
	}
	/**
	 * 货品所有单位
	 * @return
	 */
	public List<GoodsUnit> getGoodsUnitList() {
		return Arrays.asList(getGoodsUnit());
	}
	/**
	 * 商品单位列表，单据默认单位显示在前面
	 * @param unitType
	 * @return
	 */
	public List<GoodsUnit> getGoodsUnitList(OrderUnitTypeEnum unitType) {
		return getGoodsUnitList();
	}
	
	public List<GoodsUnit> getGoodsUnitListSort(Integer defaultUnitId) {
		List<GoodsUnit> unitList = getGoodsUnitList();
		return unitList;
	}
	
	/**
	 * 所属分类
	 * @return
	 */
	public GoodsCategory getGoodsCategory() {
		return GoodsCategory.dao.findFirst("select * from goods_category where id = ?", getGoodsCategoryId());
	}

	public List<GoodsPrice> getGoodsPriceList() {
		return GoodsPrice.dao.find("select * from goods_price where goods_info_id = ?", getId());
	}

	public List<InventoryStock> getInventoryStockList() {
		return InventoryStock.dao.find("select * from inventory_stock where goods_info_id = ?", getId());
	}
	
	public GoodsUnit getGoodsUnit() {
		return GoodsUnit.dao.findById(getMainUnitId());
	}
	public SupplierInfo getSupplierInfo() {
		return SupplierInfo.dao.findById(getSupplierInfoId());
	}
	public GoodsUnit getStockUnit() {
		return GoodsUnit.dao.findById(getMainUnitId());
	}
	public GoodsUnit getPurchaseUnit() {
		return GoodsUnit.dao.findById(getMainUnitId());
	}
	public GoodsUnit getSaleUnit() {
		return GoodsUnit.dao.findById(getMainUnitId());
	}
	/**
	 * 获取库存的主单位
	 * @return
	 */
	public GoodsUnit getStockMainUnit() {
		return getGoodsUnit();
	}
	/**
	 * 获取销售的主单位
	 * @return
	 */
	public GoodsUnit getSaleMainUnit() {
		return getGoodsUnit();
	}
	
	/**
	 * 获取进货的主单位
	 * @return
	 */
	public GoodsUnit getPurchaseMainUnit() {
		return getGoodsUnit();
	}
	/**
	 * 主图
	 * @return
	 */
	public GoodsImageRef getMainThumb() {
		GoodsImageRef ref = GoodsImageRef.dao.findFirst("select * from goods_image_ref where goods_info_id = ? and main_flag = ? limit 1", getId(), FlagEnum.YES.getValue());
		if(ref == null) {
			ref = GoodsImageRef.dao.findFirst("select * from goods_image_ref where goods_info_id = ? order by position limit 1", getId());
		}
		return ref;
	}
	
	public List<GoodsImageRef> getThumbList() {
		return GoodsImageRef.dao.find("select * from goods_image_ref where goods_info_id = ?", getId());
	}
	
	/**
	 * 根据属性ID获取商品属性值
	 * @param attributeId
	 * @return
	 */
	public GoodsAttributeRef getAttribute(Integer attributeId) {
		return GoodsAttributeRef.dao.findFirst("select * from goods_attribute_ref where goods_info_id = ? and goods_attribute_id = ?", getId(), attributeId);
	}

	/**
	 * 通过关键词搜索规格
	 * @param tenantOrgId
	 * @param id
	 * @param keyword
	 * @return
	 */
	public List<Map<String, String>> findSpecByKeyword(Integer goodsId, String keyword) {
		List<GoodsSpecRef> specRefList = GoodsSpecRef.dao.find("select * from goods_spec_ref where goods_info_id = ? order by position", goodsId);
		if(specRefList == null || specRefList.isEmpty()) {
			return null;
		}
		GoodsSpecRef firstGoodsSpecRef = specRefList.get(0);
		GoodsSpec firstGoodsSpec = firstGoodsSpecRef.getGoodsSpec();
		List<GoodsSpecOptions> firstSpecOptionList = firstGoodsSpecRef.getSpecValueList();
		
		List<GoodsSpecOptions> secondSpecOptionList = new ArrayList<>();
		GoodsSpecRef secondGoodsSpecRef = null;
		if(specRefList.size() > 1) {
			secondGoodsSpecRef = specRefList.get(1);
			secondSpecOptionList = secondGoodsSpecRef.getSpecValueList();
		}
		List<GoodsSpecOptions> thirdSpecOptionList = new ArrayList<>();
		GoodsSpecRef thirdGoodsSpecRef = null;
		if(specRefList.size() > 2) {
			thirdGoodsSpecRef = specRefList.get(2);
			thirdSpecOptionList = thirdGoodsSpecRef.getSpecValueList();
		}
		List<Map<String, String>> resultList = new ArrayList<>(); // 返回结果，三三组合，Key为ID组合，Value为规格选项名称
		for (GoodsSpecOptions first : firstSpecOptionList) {
			if(secondSpecOptionList.isEmpty()) { // 只有一个规格
				Map<String, String> map = new HashMap<>();
				map.put("id", firstGoodsSpecRef.getGoodsSpecId()+":"+first.getId());
				map.put("name", first.getOptionValue());
				map.put("full_name", firstGoodsSpec.getName() +":"+ first.getOptionValue());
				resultList.add(map);
				continue;
			}
			GoodsSpec secondGoodsSpec = secondGoodsSpecRef.getGoodsSpec();
			for (GoodsSpecOptions second : secondSpecOptionList) {
				if(thirdSpecOptionList.isEmpty()) {// 只有两个规格
					Map<String, String> map = new HashMap<>();
					map.put("id", firstGoodsSpecRef.getGoodsSpecId()+":"+first.getId()+"|"+secondGoodsSpecRef.getGoodsSpecId()+":"+second.getId());
					map.put("name", first.getOptionValue()+"/"+second.getOptionValue());
					map.put("full_name", firstGoodsSpec.getName() +":"+ first.getOptionValue()+" / "+secondGoodsSpec.getName() +":"+ second.getOptionValue());
					resultList.add(map);
					continue;
				}
				GoodsSpec thirdGoodsSpec = thirdGoodsSpecRef.getGoodsSpec();
				for (GoodsSpecOptions third : thirdSpecOptionList) {
					Map<String, String> map = new HashMap<>();
					map.put("id", firstGoodsSpecRef.getGoodsSpecId()+":"+first.getId()+"|"+secondGoodsSpecRef.getGoodsSpecId()+":"+second.getId()+"|"+thirdGoodsSpecRef.getGoodsSpecId()+":"+third.getId());
					map.put("name", first.getOptionValue()+"/"+second.getOptionValue()+"/"+third.getOptionValue());
					map.put("full_name", firstGoodsSpec.getName() +":"+ first.getOptionValue()+" / "+secondGoodsSpec.getName() +":"+ second.getOptionValue()+" / "+thirdGoodsSpec.getName() +":"+ third.getOptionValue());
					resultList.add(map);
				}
			}
		}
		if(StringUtils.isEmpty(keyword)) {
			return resultList;
		}
		// 按关键词过滤不符合条件的规格
		List<Map<String, String>> _resultList = new ArrayList<>();
		for (Map<String, String> map : resultList) {
			boolean flag = true; // 是否全部匹配
			String[] keywordList = StringUtils.split(keyword, " "); // 多个关键词使用空格隔开
			for (String kw : keywordList) {
				if(!StringUtils.contains(map.get("name"), kw)) { // 不匹配
					flag = false;
					break;
				}
			}
			if(flag) { // 全匹配
				_resultList.add(map);
			}
		}
		return _resultList;
	}
	

	public void setGoodsImages(String[] thumbs, String[] originals) {
		if(thumbs == null || thumbs.length <= 0) {
			return;
		}
		for (int index = 0; index < thumbs.length; index++) {
			GoodsImageRef image = new GoodsImageRef();
			image.setCreatedAt(new Date());
			if(index == 0) {
				image.setMainFlag(FlagEnum.YES.getValue());
			}
			image.setOriginal(originals[index]);
			image.setPosition(index);
			image.setThumb(thumbs[index]);
			imageList.add(image);
		}
	}
	public List<GoodsImageRef> getImageList() {
		return imageList;
	}
	
	public String getGoodsTypeName() {
		return GoodsTypeEnum.getEnum(getGoodsType()).getName();
	}
	
}

