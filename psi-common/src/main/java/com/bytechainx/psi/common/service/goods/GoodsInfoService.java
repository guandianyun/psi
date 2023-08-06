package com.bytechainx.psi.common.service.goods;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.kit.PinYinUtil;
import com.bytechainx.psi.common.model.GoodsAttributeRef;
import com.bytechainx.psi.common.model.GoodsImageRef;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsSpecRef;
import com.bytechainx.psi.common.model.GoodsStockConfig;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrderGoods;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


/**
* 商品管理
*/
public class GoodsInfoService extends CommonService {

	/**
	* 分页列表
	*/
	public Page<GoodsInfo> paginate(Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");

		conditionFilter(conditionColumns, where, params);
		
		where.append(" and data_status != ?");
		params.add(DataStatusEnum.delete.getValue());
		
		return GoodsInfo.dao.paginate(pageNumber, pageSize, "select * ", "from goods_info "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	
	public Ret create(GoodsInfo goodsInfo, List<GoodsSpecRef> goodsSpecRefList, List<GoodsAttributeRef> goodsAttributeRefList) {
		if(StringUtils.isEmpty(goodsInfo.getName())) {
			return Ret.fail("商品名称不能为空");
		}
		if(goodsInfo.getMainUnitId() == null || goodsInfo.getMainUnitId() <= 0) {
			return Ret.fail("主单位不能为空");
		}
		GoodsInfo _goodsInfo = GoodsInfo.dao.findFirst("select * from goods_info where name = ? and data_status != ? limit 1", goodsInfo.getName(), DataStatusEnum.delete.getValue());
		if(_goodsInfo != null) {
			return Ret.fail("商品名称已存在");
		}
		_goodsInfo = GoodsInfo.dao.findFirst("select * from goods_info where bar_code = ? and data_status != ? limit 1", goodsInfo.getBarCode(), DataStatusEnum.delete.getValue());
		if(_goodsInfo != null) {
			return Ret.fail("商品条码已存在");
		}
		
		if(goodsInfo.getGoodsCategoryId() == null) {
			goodsInfo.setGoodsCategoryId(0);
		}
		goodsInfo.setCode(PinYinUtil.getFirstSpell(goodsInfo.getName()));
		goodsInfo.setCreatedAt(new Date());
		goodsInfo.setUpdatedAt(new Date());
		goodsInfo.save();
		
		for(GoodsImageRef image : goodsInfo.getImageList()) {
			image.setGoodsInfoId(goodsInfo.getId());
			image.setCreatedAt(new Date());
			image.save();
		}
		
		// 商品规格
		if(!goodsInfo.getSpecFlag()) { // 关闭了多规格，规格数据清除
			goodsSpecRefList.clear();
		}
		if(goodsSpecRefList != null && !goodsSpecRefList.isEmpty()) {
			for (GoodsSpecRef e : goodsSpecRefList) {
				if(e.getGoodsSpecId() == null || e.getGoodsSpecId() <= 0 || StringUtils.isEmpty(e.getSpecValue())) {
					continue;
				}
				e.setGoodsInfoId(goodsInfo.getId());
				e.save();
			}
		}
		// 商品属性
		if(goodsAttributeRefList != null && !goodsAttributeRefList.isEmpty()) {
			for (GoodsAttributeRef e : goodsAttributeRefList) {
				if(e.getGoodsAttributeId() == null || e.getGoodsAttributeId() <= 0) {
					continue;
				}
				e.setGoodsInfoId(goodsInfo.getId());
				e.save();
			}
		}
		
		return Ret.ok("新增商品成功").set("targetId", goodsInfo.getId());
	}


	/**
	* 修改
	*/
	
	public Ret update(GoodsInfo goodsInfo, List<GoodsSpecRef> goodsSpecRefList, List<GoodsAttributeRef> goodsAttributeRefList) {
		if(StringUtils.isEmpty(goodsInfo.getName())) {
			return Ret.fail("商品名称不能为空");
		}
		if(goodsInfo.getMainUnitId() == null || goodsInfo.getMainUnitId() <= 0) {
			return Ret.fail("主单位不能为空");
		}
		GoodsInfo _goodsInfo = GoodsInfo.dao.findFirst("select * from goods_info where name = ? and data_status != ? limit 1", goodsInfo.getName(), DataStatusEnum.delete.getValue());
		if(_goodsInfo != null && _goodsInfo.getId().intValue() != goodsInfo.getId().intValue()) {
			return Ret.fail("商品名称已存在");
		}
		_goodsInfo = GoodsInfo.dao.findFirst("select * from goods_info where bar_code = ? and data_status != ? limit 1", goodsInfo.getBarCode(), DataStatusEnum.delete.getValue());
		if(_goodsInfo != null && _goodsInfo.getId().intValue() != goodsInfo.getId().intValue()) {
			return Ret.fail("商品条码已存在");
		}
		_goodsInfo = GoodsInfo.dao.findById(goodsInfo.getId());
		if(_goodsInfo == null) { // 不是同一个租户
			return Ret.fail("商品不存在,无法修改");
		}
		goodsInfo.setCode(PinYinUtil.getFirstSpell(goodsInfo.getName()));
		goodsInfo.setUpdatedAt(new Date());
		goodsInfo.update();
		
		// 更新商品图片
		updateGoodsImage(goodsInfo);
		// 商品规格更新
		updateGoodsSpecRef(goodsInfo, goodsSpecRefList);
		
		// 商品属性更新
		updateGoodsAttributeRef(goodsInfo, goodsAttributeRefList);
		
		return Ret.ok("修改商品成功");
	}

	/**
	 * 商品库存更新
	 * @param tenantOrgId
	 * @param goodsInfo
	 * @param inventoryStockList
	 */
	public void updateInventoryStock(GoodsInfo goodsInfo, List<InventoryStock> inventoryStockList) {
		List<InventoryStock> _inventoryStockList =  goodsInfo.getInventoryStockList();
		List<InventoryStock> deleteList = new ArrayList<>();
		if(inventoryStockList == null || inventoryStockList.isEmpty()) {
			deleteList = _inventoryStockList;
		} else {
			for (InventoryStock old : _inventoryStockList) {
				boolean isExist = false;
				for (InventoryStock e : inventoryStockList) {
					if(e.getId() != null && e.getId().intValue() == old.getId().intValue()) {
						isExist = true;
						break;
					}
				}
				if(!isExist) { // 不存在需要删除
					deleteList.add(old);
				}
			}
		}
		for (InventoryStock e : deleteList) {
			e.delete();
		}
		
		if (inventoryStockList == null || inventoryStockList.isEmpty()) {
			return;
		}
		for (InventoryStock e : inventoryStockList) {
			if(e.getId() == null || e.getId() <= 0) {
				e.setGoodsInfoId(goodsInfo.getId());
				e.setCreatedAt(new Date());
				e.setUpdatedAt(new Date());
				e.save();
			} else {
				e.setGoodsInfoId(goodsInfo.getId());
				e.setUpdatedAt(new Date());
				e.update();
			}
		}
	}

	/**
	 * 商品属性更新
	 * @param goodsInfo
	 * @param goodsAttributeRefList
	 */
	private void updateGoodsAttributeRef(GoodsInfo goodsInfo, List<GoodsAttributeRef> goodsAttributeRefList) {
		List<GoodsAttributeRef> _goodsAttributeRefList =  goodsInfo.getGoodsAttributeRefList();
		List<GoodsAttributeRef> deleteList = new ArrayList<>();
		if(goodsAttributeRefList == null || goodsAttributeRefList.isEmpty()) {
			deleteList = _goodsAttributeRefList;
		} else {
			for (GoodsAttributeRef old : _goodsAttributeRefList) {
				boolean isExist = false;
				for (GoodsAttributeRef e : goodsAttributeRefList) {
					if(e.getId() != null && e.getId().intValue() == old.getId().intValue()) {
						isExist = true;
						break;
					}
				}
				if(!isExist) { // 不存在需要删除
					deleteList.add(old);
				}
			}
		}
		for (GoodsAttributeRef e : deleteList) {
			e.delete();
		}
		if (goodsAttributeRefList == null || goodsAttributeRefList.isEmpty()) {
			return;
		}
		for (GoodsAttributeRef e : goodsAttributeRefList) {
			if (e.getGoodsAttributeId() == null || e.getGoodsAttributeId() <= 0) {
				continue;
			}
			if(e.getId() == null || e.getId() <= 0) {
				GoodsAttributeRef _e = GoodsAttributeRef.dao.findFirst("select * from goods_attribute_ref where goods_info_id = ? and goods_attribute_id = ?", goodsInfo.getId(), e.getGoodsAttributeId());
				if(_e != null) {
					e.setId(_e.getId());
					e.update();
				} else {
					e.setGoodsInfoId(goodsInfo.getId());
					e.save();
				}
				
			} else {
				e.update();
			}
		}
	}

	/**
	 * 商品规格关联更新
	 * @param goodsInfo
	 * @param goodsSpecRefList
	 */
	private void updateGoodsSpecRef(GoodsInfo goodsInfo, List<GoodsSpecRef> goodsSpecRefList) {
		if(!goodsInfo.getSpecFlag()) { // 关闭了多规格，规格数据清除
			goodsSpecRefList.clear();
		}
		
		List<GoodsSpecRef> _goodsSpecRefList =  goodsInfo.getGoodsSpecRefList();
		List<GoodsSpecRef> deleteList = new ArrayList<>();
		if(goodsSpecRefList == null || goodsSpecRefList.isEmpty()) {
			deleteList = _goodsSpecRefList;
		} else {
			for (GoodsSpecRef old : _goodsSpecRefList) {
				boolean isExist = false;
				for (GoodsSpecRef e : goodsSpecRefList) {
					if(e.getId() != null && e.getId().intValue() == old.getId().intValue()) {
						isExist = true;
						break;
					}
				}
				if(!isExist) { // 不存在需要删除
					deleteList.add(old);
				}
			}
		}
		for (GoodsSpecRef e : deleteList) {
			e.delete();
		}
		
		if (goodsSpecRefList == null || goodsSpecRefList.isEmpty()) {
			return;
		}
		for (GoodsSpecRef e : goodsSpecRefList) {
			if (e.getGoodsSpecId() == null || e.getGoodsSpecId() <= 0 || StringUtils.isEmpty(e.getSpecValue())) {
				continue;
			}
			if(e.getId() == null || e.getId() <= 0) {
				GoodsSpecRef _e = GoodsSpecRef.dao.findFirst("select * from goods_spec_ref where goods_info_id = ? and goods_spec_id = ?", goodsInfo.getId(), e.getGoodsSpecId());
				if(_e != null) {
					e.setId(_e.getId());
					e.update();
				} else {
					e.setGoodsInfoId(goodsInfo.getId());
					e.save();
				}
			} else {
				e.update();
			}
		}
		
	}


	/**
	* 删除
	*/
	
	public Ret delete(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(id);
			if(goodsInfo == null) {
				continue;
			}
			SaleOrderGoods saleOrderGoods = SaleOrderGoods.dao.findFirst("select count(*) as counts from sale_order_goods where goods_info_id = ?", id);
			if(saleOrderGoods.getInt("counts") > 0) {
				return Ret.fail("商品已开进货单，无法删除");
			}
			SaleRejectOrderGoods saleRejectOrderGoods = SaleRejectOrderGoods.dao.findFirst("select count(*) as counts from sale_reject_order_goods where goods_info_id = ?", id);
			if(saleRejectOrderGoods.getInt("counts") > 0) {
				return Ret.fail("商品已开进货退货单，无法删除");
			}
			PurchaseOrderGoods purchaseOrderGoods = PurchaseOrderGoods.dao.findFirst("select count(*) as counts from purchase_order_goods where goods_info_id = ?", id);
			if(purchaseOrderGoods.getInt("counts") > 0) {
				return Ret.fail("商品已开进货单，无法删除");
			}
			PurchaseRejectOrderGoods purchaseRejectOrderGoods = PurchaseRejectOrderGoods.dao.findFirst("select count(*) as counts from purchase_reject_order_goods where goods_info_id = ?", id);
			if(purchaseRejectOrderGoods.getInt("counts") > 0) {
				return Ret.fail("商品已开进货退货单，无法删除");
			}
			
			goodsInfo.setDataStatus(DataStatusEnum.delete.getValue());
			goodsInfo.setUpdatedAt(new Date());
			goodsInfo.update();
			
			Db.update("update goods_price set data_status = ? where goods_info_id = ?", DataStatusEnum.delete.getValue(), id);
			Db.update("update inventory_stock set data_status = ? where goods_info_id = ?", DataStatusEnum.delete.getValue(), id);
			Db.update("update inventory_batch_quality_stock set data_status = ? where goods_info_id = ?", DataStatusEnum.delete.getValue(), id);
		}
		return Ret.ok();
	}
	
	/**
	* 停用
	*/
	
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(id);
			if(goodsInfo == null) {
				continue;
			}
			goodsInfo.setDataStatus(DataStatusEnum.disable.getValue());
			goodsInfo.setUpdatedAt(new Date());
			goodsInfo.update();
			
			Db.update("update goods_price set data_status = ? where goods_info_id = ?", DataStatusEnum.disable.getValue(), id);
			Db.update("update inventory_stock set data_status = ? where goods_info_id = ?", DataStatusEnum.disable.getValue(), id);
			Db.update("update inventory_batch_quality_stock set data_status = ? where goods_info_id = ?", DataStatusEnum.disable.getValue(), id);

		}
		return Ret.ok("停用商品成功");
	}

	
	/**
	* 启用
	*/
	
	public Ret enable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(id);
			if(goodsInfo == null) {
				continue;
			}
			goodsInfo.setDataStatus(DataStatusEnum.enable.getValue());
			goodsInfo.setUpdatedAt(new Date());
			goodsInfo.update();
			
			Db.update("update goods_price set data_status = ? where goods_info_id = ?", DataStatusEnum.enable.getValue(), id);
			Db.update("update inventory_stock set data_status = ? where goods_info_id = ?", DataStatusEnum.enable.getValue(), id);
			Db.update("update inventory_batch_quality_stock set data_status = ? where goods_info_id = ?", DataStatusEnum.enable.getValue(), id);
		}
		return Ret.ok("启用商品成功");
	}

	
	/**
	 * 商品更新价格
	 * @param goodsId
	 * @param goodsUnionIds
	 * @param barCodes
	 * @param costPrices
	 * @param salePriceList
	 */
	public Ret updatePrice(Integer goodsId, String[] goodsUnionIds, String[] barCodes, String[] costPrices, Kv salePriceList) {
		GoodsInfo info = GoodsInfo.dao.findById(goodsId);
		if(info == null) {
			return Ret.fail("商品不存在");
		}
		for (int index = 0; index < goodsUnionIds.length; index++) {
			String unionIds = goodsUnionIds[index];
			unionIds = StringUtils.replace(unionIds, "|", ",");
			JSONObject jsonObject = JSONObject.parseObject("{"+ unionIds +"}");
			Integer spec1Id = jsonObject.getInteger("spec_1_id");
			Integer specOption1Id = jsonObject.getInteger("spec_option_1_id");
			Integer spec2Id = jsonObject.getInteger("spec_2_id");
			Integer specOption2Id = jsonObject.getInteger("spec_option_2_id");
			Integer spec3Id = jsonObject.getInteger("spec_3_id");
			Integer specOption3Id = jsonObject.getInteger("spec_option_3_id");
			Integer unitId = jsonObject.getInteger("unit_id");
			GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(goodsId, spec1Id, specOption1Id, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
			if(goodsPrice == null) {
				goodsPrice = new GoodsPrice();
				goodsPrice.setGoodsInfoId(goodsId);
				goodsPrice.setBarCode(barCodes[index]);
				goodsPrice.setSpec1Id(spec1Id);
				goodsPrice.setSpecOption1Id(specOption1Id);
				goodsPrice.setSpec2Id(spec2Id);
				goodsPrice.setSpecOption2Id(specOption2Id);
				goodsPrice.setSpec3Id(spec3Id);
				goodsPrice.setSpecOption3Id(specOption3Id);
				goodsPrice.setUnitId(unitId);
				goodsPrice.setCreatedAt(new Date());
				goodsPrice.save();
			}
			String costPirce = costPrices[index];
			if(StringUtils.isNotEmpty(costPirce)) {
				goodsPrice.setCostPrice(new BigDecimal(costPirce));
				if( goodsPrice.getAvgCostPrice() == null || goodsPrice.getAvgCostPrice().compareTo(BigDecimal.ZERO) <= 0) {
					goodsPrice.setAvgCostPrice(new BigDecimal(costPirce));
				}
				if(goodsPrice.getInitPrice() == null || goodsPrice.getInitPrice().compareTo(BigDecimal.ZERO) <= 0) {
					goodsPrice.setInitPrice(new BigDecimal(costPirce));
				}
			}
			JSONObject salePriceJson = new JSONObject();
			for (Object key : salePriceList.keySet()) {
				String[] values = (String[]) salePriceList.get(key);
				salePriceJson.put((String)key, values[index]);
			}
			if(StringUtils.isEmpty(goodsPrice.getBarCode()) && StringUtils.isNotEmpty(barCodes[index])) {
				goodsPrice.setBarCode(barCodes[index]);
			}
			goodsPrice.setSalePrice(salePriceJson.toJSONString());
			goodsPrice.setDataStatus(DataStatusEnum.enable.getValue());
			goodsPrice.setUpdatedAt(new Date());
			goodsPrice.update();
		}
		return Ret.ok("商品价格更新成功");
	}
	
	/**
	* 导入
	*/
	public Ret createImport() {

		return Ret.ok();
	}

	/**
	 * 更新库存预警配置
	 * @param tenantOrgId
	 * @param goodsId
	 * @param goodsUnionIds
	 * @param lowStocks
	 * @param safeStocks
	 * @param highStocks
	 * @return
	 */
	public Ret updateStockWarnConfig(Integer goodsId, String[] goodsUnionIds, String[] lowStocks, String[] safeStocks, String[] highStocks) {
		for (int index = 0; index < goodsUnionIds.length; index++) {
			String unionIds = goodsUnionIds[index];
			unionIds = StringUtils.replace(unionIds, "|", ",");
			JSONObject jsonObject = JSONObject.parseObject("{"+ unionIds +"}");
			Integer spec1Id = jsonObject.getInteger("spec_1_id");
			Integer specOption1Id = jsonObject.getInteger("spec_option_1_id");
			Integer spec2Id = jsonObject.getInteger("spec_2_id");
			Integer specOption2Id = jsonObject.getInteger("spec_option_2_id");
			Integer spec3Id = jsonObject.getInteger("spec_3_id");
			Integer specOption3Id = jsonObject.getInteger("spec_option_3_id");
			Integer unitId = jsonObject.getInteger("unit_id");
			GoodsStockConfig stockConfig = GoodsStockConfig.dao.findBySpec(goodsId, spec1Id, specOption1Id, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
			if(stockConfig == null) {
				stockConfig = new GoodsStockConfig();
				stockConfig.setGoodsInfoId(goodsId);
				stockConfig.setSpec1Id(spec1Id);
				stockConfig.setSpecOption1Id(specOption1Id);
				stockConfig.setSpec2Id(spec2Id);
				stockConfig.setSpecOption2Id(specOption2Id);
				stockConfig.setSpec3Id(spec3Id);
				stockConfig.setSpecOption3Id(specOption3Id);
				stockConfig.setUnitId(unitId);
				stockConfig.setCreatedAt(new Date());
				stockConfig.save();
			}
			String lowStock = lowStocks[index];
			if(StringUtils.isNotEmpty(lowStock)) {
				stockConfig.setLowStock(new BigDecimal(lowStock));
			}
			String highStock = highStocks[index];
			if(StringUtils.isNotEmpty(highStock)) {
				stockConfig.setHighStock(new BigDecimal(highStock));
			}
			String safeStock = safeStocks[index];
			if(StringUtils.isNotEmpty(safeStock)) {
				stockConfig.setSafeStock(new BigDecimal(safeStock));
			}
			stockConfig.setUpdatedAt(new Date());
			stockConfig.update();
		}
		return Ret.ok("商品库存预警配置更新成功");
	}

	/**
	 * 更新商品图片
	 * @param tenantOrgId
	 * @param goodsInfo
	 */
	private void updateGoodsImage(GoodsInfo goodsInfo) {
		List<GoodsImageRef> oldGoodsImageList = GoodsImageRef.dao.find("select * from goods_image_ref where goods_info_id = ?", goodsInfo.getId());
		List<GoodsImageRef> deleteList = new ArrayList<>();
		if(goodsInfo.getImageList() == null || goodsInfo.getImageList().isEmpty()) {
			deleteList = oldGoodsImageList;
		} else {
			for (GoodsImageRef old : oldGoodsImageList) {
				boolean isExist = false;
				for (GoodsImageRef e : goodsInfo.getImageList()) {
					if(StringUtils.equals(e.getOriginal(), old.getOriginal())) {
						isExist = true;
						break;
					}
				}
				if(!isExist) { // 不存在需要删除
					deleteList.add(old);
				}
			}
		}
		for (GoodsImageRef e : deleteList) {
			e.delete();
		}
		for(GoodsImageRef image : goodsInfo.getImageList()) {
			GoodsImageRef _image = GoodsImageRef.dao.findFirst("select * from goods_image_ref where original = ?", image.getOriginal());
			if(_image != null) {
				continue;
			}
			image.setGoodsInfoId(goodsInfo.getId());
			image.setCreatedAt(new Date());
			image.save();
		}
	}

	/**
	 * 下架
	 * @param tenantOrgId
	 * @param ids
	 * @return
	 */
	public Ret pull(String[] ids) {
		if(ids == null || ids.length <= 0) {
			return Ret.fail("参数错误");
		}
		for (String id : ids) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(Integer.parseInt(id));
			if(goodsInfo == null) {
				continue;
			}
			goodsInfo.setUpdatedAt(new Date());
			goodsInfo.update();
		}
		return Ret.ok("商品下架成功");
	}

	/**
	 * 商品上架
	 * @param tenantOrgId
	 * @param ids
	 * @return
	 */
	public Ret push(String[] ids) {
		if(ids == null || ids.length <= 0) {
			return Ret.fail("参数错误");
		}
		for (String id : ids) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(Integer.parseInt(id));
			if(goodsInfo == null) {
				continue;
			}
			goodsInfo.setUpdatedAt(new Date());
			goodsInfo.update();
		}
		return Ret.ok("商品上架成功");
	}

	
	
}