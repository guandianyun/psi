package com.bytechainx.psi.purchase.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.StockInOutOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockIoTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.model.GoodsInfo;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.GoodsUnit;
import com.bytechainx.psi.common.model.InventoryChecking;
import com.bytechainx.psi.common.model.InventoryCheckingGoods;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.InventoryStockLog;
import com.bytechainx.psi.common.model.TenantConfig;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;


/**
* 盘点
*/
public class StockCheckingService extends CommonService {


	/**
	* 分页列表
	*/
	public Page<InventoryChecking> paginate(String startDay, String endDay, Kv conditionColumns, Integer pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(StringUtils.isNotEmpty(startDay)) {
			where.append(" and order_time  >= ?");
			params.add(startDay);
		}
		if(StringUtils.isNotEmpty(endDay)) {
			where.append(" and order_time  <= ?");
			params.add(endDay);
		}

		conditionFilter(conditionColumns, where, params);

		return InventoryChecking.dao.paginate(pageNumber, pageSize, "select * ", "from inventory_checking "+where.toString()+" order by id desc", params.toArray());
	}

	/**
	* 新增
	*/
	public Ret create(InventoryChecking order, List<InventoryCheckingGoods> orderGoodList) {
		if( order == null) {
			return Ret.fail("参数错误");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("盘点员不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderGoodList == null || orderGoodList.isEmpty()) {
			return Ret.fail("商品不能为空"); 
		}
		for (int first = 0; first < orderGoodList.size(); first++) {
			InventoryCheckingGoods firstGoods = orderGoodList.get(first);
			for (int second = first + 1; second < orderGoodList.size(); second++) {
				InventoryCheckingGoods secondGoods = orderGoodList.get(second);
				if (firstGoods.getGoodsInfoId().intValue() == secondGoods.getGoodsInfoId().intValue()
						&& firstGoods.getSpec1Id().intValue() == secondGoods.getSpec1Id().intValue() && firstGoods.getSpecOption1Id().intValue() == secondGoods.getSpecOption1Id().intValue()
						&& firstGoods.getSpec2Id().intValue() == secondGoods.getSpec2Id().intValue() && firstGoods.getSpecOption2Id().intValue() == secondGoods.getSpecOption2Id().intValue()
						&& firstGoods.getSpec3Id().intValue() == secondGoods.getSpec3Id().intValue() && firstGoods.getSpecOption3Id().intValue() == secondGoods.getSpecOption3Id().intValue() 
						&& firstGoods.getUnitId() == secondGoods.getUnitId().intValue()
						&& StringUtils.equals(firstGoods.getBatch(), secondGoods.getBatch())
						&& StringUtils.equals(firstGoods.getQuality(), secondGoods.getQuality())) {
					
					return Ret.fail("存在重复商品，请仔细检查，商品名称："+secondGoods.getGoodsInfo().getName());
				}
			}
		}
		
		// 设置审核状态
		AuditStatusEnum auditStatus = getAuditStatus(order);
		order.setAuditStatus(auditStatus.getValue());

		String orderCode = order.generateOrderCode(); //  获取订单号，单号存在redis上。
		if(StringUtils.isEmpty(orderCode)) {
			orderCode = order.generateOrderCode();
			if(StringUtils.isEmpty(orderCode)) {
				return Ret.fail("创建订单异常，请稍候重试");
			}
		}
		order.setOrderCode(orderCode);
		order.setCreatedAt(new Date());
		order.setUpdatedAt(new Date());
		order.save();
		
		// 保存商品
		updateOrderGoods(order, orderGoodList);
		// 更新商品库存
		updateInventoryStock(order, orderGoodList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());
		
		return Ret.ok("新增盘点单成功").set("targetId", order.getId());
	}


	/**
	* 修改
	*/
	public Ret update(InventoryChecking order, List<InventoryCheckingGoods> orderGoodList) {
		if(order == null || order.getId() == null || order.getId() <= 0) {
			return Ret.fail("参数错误");
		}
		if(order.getHandlerId() == null || order.getHandlerId() <= 0) {
			return Ret.fail("盘点员不能为空");
		}
		if(order.getOrderTime() == null) {
			return Ret.fail("单据时间不能为空");
		}
		if(orderGoodList == null || orderGoodList.isEmpty()) {
			return Ret.fail("商品不能为空"); 
		}
		
		InventoryChecking _order = InventoryChecking.dao.findById(order.getId());
		if(_order == null) {
			return Ret.fail("盘点单不存在，无法修改");
		}
		if(_order.getOrderStatus() == OrderStatusEnum.normal.getValue() && order.getOrderStatus() != OrderStatusEnum.normal.getValue()) {
			return Ret.fail("单据状态异常，无法修改"); 
		}
		
		for (int first = 0; first < orderGoodList.size(); first++) {
			InventoryCheckingGoods firstGoods = orderGoodList.get(first);
			for (int second = first + 1; second < orderGoodList.size(); second++) {
				InventoryCheckingGoods secondGoods = orderGoodList.get(second);
				if (firstGoods.getGoodsInfoId().intValue() == secondGoods.getGoodsInfoId().intValue()
						&& firstGoods.getSpec1Id().intValue() == secondGoods.getSpec1Id().intValue() && firstGoods.getSpecOption1Id().intValue() == secondGoods.getSpecOption1Id().intValue()
						&& firstGoods.getSpec2Id().intValue() == secondGoods.getSpec2Id().intValue() && firstGoods.getSpecOption2Id().intValue() == secondGoods.getSpecOption2Id().intValue()
						&& firstGoods.getSpec3Id().intValue() == secondGoods.getSpec3Id().intValue() && firstGoods.getSpecOption3Id().intValue() == secondGoods.getSpecOption3Id().intValue() 
						&& firstGoods.getUnitId() == secondGoods.getUnitId().intValue()
						&& StringUtils.equals(firstGoods.getBatch(), secondGoods.getBatch())
						&& StringUtils.equals(firstGoods.getQuality(), secondGoods.getQuality())) {
					
					return Ret.fail("存在重复商品，请仔细检查，商品名称："+secondGoods.getGoodsInfo().getName());
				}
			}
		}
		
		// 审核状态要根据是否开启审核开关来设置，默认无需审核
		if (_order.getOrderStatus() == OrderStatusEnum.draft.getValue()) { // 如果之前是草稿单，则处理审核状态
			AuditStatusEnum auditStatus = getAuditStatus(order);
			order.setAuditStatus(auditStatus.getValue());
		} else {
			order.setAuditStatus(_order.getAuditStatus());
		}
		
		order.setUpdatedAt(new Date());
		order.update();
		
		// 更新商品数据
		updateOrderGoods(order, orderGoodList);
		// 更新商品库存
		updateInventoryStock(order, orderGoodList);
		// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
		updateStockWarn(order.getOrderGoodsList());
				
		StringBuffer operDesc = new StringBuffer();
		operDesc.append("修改盘点单");
		
		return Ret.ok("修改盘点单成功").set("targetId", order.getId());
	}

	


	/**
	* 作废
	*/
	public Ret disable(List<Integer> ids) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		for (Integer id : ids) {
			InventoryChecking order = InventoryChecking.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getOrderStatus() == OrderStatusEnum.disable.getValue()) { // 已作废的单据不能重复作废
				continue;
			}
			// 商品库存回退
			undoInventoryStock(order, order.getOrderGoodsList());
						
			order.setOrderStatus(OrderStatusEnum.disable.getValue());
			order.setUpdatedAt(new Date());
			order.update();
			
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
			
		}
		return Ret.ok("单据作废成功");
	}


	/**
	* 审核
	*/
	public Ret audit(List<Integer> ids, AuditStatusEnum auditStatus, String auditDesc, Integer auditorId) {
		if(ids == null || ids.isEmpty()) {
			return Ret.fail("参数错误");
		}
		if(auditStatus == null) {
			return Ret.fail("审核状态错误");
		}
		if(auditorId == null || auditorId <= 0) {
			return Ret.fail("审核人不能为空");
		}
		
		for (Integer id : ids) {
			InventoryChecking order = InventoryChecking.dao.findById(id);
			if(order == null) {
				continue;
			}
			if(order.getAuditStatus() != AuditStatusEnum.waiting.getValue()) { // 已审核的订单不能重复审核
				continue;
			}
			order.setAuditorId(auditorId);
			order.setAuditDesc(auditDesc);
			order.setAuditStatus(auditStatus.getValue());
			order.setAuditTime(new Date());
			order.setUpdatedAt(new Date());
			order.update();
			
			// 更新商品库存
			updateInventoryStock(order, order.getOrderGoodsList());
			// 更新货品的告警状态，FIXME 要不要判断状态，减少无效操作
			updateStockWarn(order.getOrderGoodsList());
						
		}
		return Ret.ok("单据审核成功");
	}
	
	/**
	 * 删除盘点单
	 * @param tenantOrgId
	 * @param ids
	 * @return
	 */
	public Ret delete(Integer id) {
		if(id == null) {
			return Ret.fail("参数错误");
		}
		InventoryChecking order = InventoryChecking.dao.findById(id);
		if(order == null) {
			return Ret.fail("盘点单不存在");
		}
		if(order.getOrderStatus() != OrderStatusEnum.draft.getValue()) { // 非草稿单不能删除
			return Ret.fail("单据不是草稿单，不能删除");
		}
		order.delete();
		
		return Ret.ok("删除盘点单成功");
	}

	
	/**
	 * 修改盘点单时，更新商品数据
	 * @param tenantOrgId
	 * @param order
	 * @param orderGoodList
	 */
	private void updateOrderGoods(InventoryChecking order, List<InventoryCheckingGoods> orderGoodList) {
		List<InventoryCheckingGoods> oldOrderGoodList = InventoryCheckingGoods.dao.find("select * from inventory_checking_goods where inventory_checking_id = ?", order.getId());
		// 先过滤出已删除的记录
		List<InventoryCheckingGoods> deleteGoodsList = new ArrayList<>();
		for(InventoryCheckingGoods oldGoods : oldOrderGoodList) {
			boolean isExist = false;
			for (InventoryCheckingGoods goods : orderGoodList) {
				if(goods.getId() != null && goods.getId().intValue() == oldGoods.getId().intValue()) {
					isExist = true;
					break;
				}
			}
			if(!isExist) { // 需要删除的记录
				deleteGoodsList.add(oldGoods);
			}
		}
		// 库存释放
		undoInventoryStock(order, deleteGoodsList);
				
		// 删除商品数据
		for (InventoryCheckingGoods e : deleteGoodsList) {
			e.delete();
		}
		
		BigDecimal sumProfitLoss = BigDecimal.ZERO; // 总盈亏
		BigDecimal sumCheckNumber = BigDecimal.ZERO; // 盘点总数
		BigDecimal sumDifferNumber = BigDecimal.ZERO; // 盈亏总数
		
		// 保存商品
		for (InventoryCheckingGoods e : orderGoodList) {
			if (e.getGoodsInfoId() == null || e.getGoodsInfoId() <= 0 || e.getCheckNumber().doubleValue() <= 0) {
				continue;
			}
			
			InventoryCheckingGoods _e = InventoryCheckingGoods.dao.findFirst("select * from inventory_checking_goods where inventory_checking_id = ? and goods_info_id = ? and spec_1_id = ? and spec_option_1_id = ? and spec_2_id = ? and spec_option_2_id = ? and spec_3_id = ? and spec_option_3_id = ? and unit_id = ?", 
																				order.getId(), e.getGoodsInfoId(), e.getSpec1Id(), e.getSpecOption1Id(), e.getSpec2Id(), e.getSpecOption2Id(), e.getSpec3Id(), e.getSpecOption3Id(), e.getUnitId());
			BigDecimal profitLoss = BigDecimal.ZERO;
			GoodsPrice goodsPrice = GoodsPrice.dao.findBySpec(e.getGoodsInfoId(), e.getSpec1Id(), e.getSpecOption1Id(), e.getSpec2Id(), e.getSpecOption2Id(), e.getSpec3Id(), e.getSpecOption3Id(), e.getUnitId());
			if(goodsPrice != null) {
				profitLoss = e.getCheckNumber().multiply(goodsPrice.getAvgCostPrice()).subtract(e.getCurrentNumber().multiply(goodsPrice.getAvgCostPrice()));
			}
			if(_e != null) {
				_e.setBatch(e.getBatch());
				_e.setQuality(e.getQuality());
				_e.setGoodsBatchQualityId(e.getGoodsBatchQualityId());
				_e.setCheckNumber(e.getCheckNumber());
				_e.setCurrentNumber(e.getCurrentNumber());
				_e.setProfitLoss(profitLoss);
				_e.setConvertNumber(e.getConvertNumber());
				_e.setRemark(e.getRemark());
				_e.setUpdatedAt(new Date());
				
				_e.update();
				
			} else {
				e.setInventoryCheckingId(order.getId());
				e.setProfitLoss(profitLoss);
				e.setUpdatedAt(new Date());
				e.setCreatedAt(new Date());
				e.save();
			}
			
			sumProfitLoss = sumProfitLoss.add(profitLoss);
			sumCheckNumber = sumCheckNumber.add(e.getCheckNumber());
			sumDifferNumber = sumDifferNumber.add(e.getCheckNumber().subtract(e.getCurrentNumber()));
		}
		order.setProfitLoss(sumProfitLoss);
		order.setCheckNumber(sumCheckNumber);
		order.setDifferNumber(sumDifferNumber);
		order.update();
		
	}
	
	
	/**
	 * 商品入库，修改库存
	 * @param tenantOrgId
	 * @param order
	 * @param orderGoods
	 * @param changeNumber
	 */
	private void updateInventoryStock(InventoryChecking order, List<InventoryCheckingGoods> orderGoodsList) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return;
		}
		for (InventoryCheckingGoods orderGoods : orderGoodsList) {
			BigDecimal changeNumber = orderGoods.getCheckNumber(); // 发生变更的数量
			if(changeNumber == null || changeNumber.compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			BigDecimal changeStock = changeNumber; // 换算后的库存单位的数量
			BigDecimal goodsStock = orderGoods.getCheckNumber(); // 换算后的库存单位的数量
			
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(inventoryStock == null) { // 商品库存不存在，则新增
				inventoryStock = createInventoryStock(order, orderGoods, goodsStock, stockUnit);
			} else {
				inventoryStock.setStock(changeStock); // 调整库存
				inventoryStock.setUpdatedAt(new Date());
				inventoryStock.update();
			}
			// 库存流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.inventory_checking.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.setChangeNumber(orderGoods.getCheckNumber());
				stockLog.setRemainNumber(inventoryStock.getStock());
				stockLog.update();
			} else {
				stockLog = new InventoryStockLog();
				stockLog.setChangeNumber(orderGoods.getCheckNumber());
				stockLog.setCreatedAt(new Date());
				stockLog.setGoodsInfoId(orderGoods.getGoodsInfoId());
				stockLog.setIoType(StockIoTypeEnum.adjust.getValue());
				stockLog.setOrderCode(order.getOrderCode());
				stockLog.setOrderId(order.getId());
				stockLog.setOrderType(StockInOutOrderTypeEnum.inventory_checking.getValue());
				stockLog.setRemainNumber(inventoryStock.getStock());
				stockLog.setSpec1Id(orderGoods.getSpec1Id());
				stockLog.setSpecOption1Id(orderGoods.getSpecOption1Id());
				stockLog.setSpec2Id(orderGoods.getSpec2Id());
				stockLog.setSpecOption2Id(orderGoods.getSpecOption2Id());
				stockLog.setSpec3Id(orderGoods.getSpec3Id());
				stockLog.setSpecOption3Id(orderGoods.getSpecOption3Id());
				stockLog.setUnitId(orderGoods.getUnitId());
				stockLog.save();
			}
		}
		// 解锁仓库
	}


	/**
	 * 商品库存重置回退
	 * @param tenantOrgId
	 * @param order
	 * @param orderGoods
	 */
	private void undoInventoryStock(InventoryChecking order, List<InventoryCheckingGoods> orderGoodsList) {
		if(order.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
			return;
		}
		for (InventoryCheckingGoods orderGoods : orderGoodsList) {
			GoodsInfo goodsInfo = GoodsInfo.dao.findById(orderGoods.getGoodsInfoId());
			GoodsUnit stockUnit = goodsInfo.getStockMainUnit(); // 通过库存单位查询库存
			
			InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(orderGoods.getGoodsInfoId(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), stockUnit.getId());
			if(inventoryStock == null) {
				continue;
			}
			BigDecimal goodsCheckStock = orderGoods.getCheckNumber(); // 换算后的盘点库存
			BigDecimal currentStock = orderGoods.getCurrentNumber(); // 换算后的当前库存
			
			if(inventoryStock.getStock().compareTo(goodsCheckStock) != 0) { // 实际库存与盘点的库存不一致，可能有新的入库或者出库
				BigDecimal changeStock = inventoryStock.getStock().subtract(goodsCheckStock); // 计算当前库存与盘点库存的差额，就是新的出入库数量
				currentStock = currentStock.add(changeStock); // 盘点之前的库存加上差额，就是最新的库存
			}
			inventoryStock.setStock(currentStock); // 重置库存
			inventoryStock.setUpdatedAt(new Date());
			inventoryStock.update();
			
			// 删除流水
			InventoryStockLog stockLog = InventoryStockLog.dao.findByOrderId(order.getId(), StockInOutOrderTypeEnum.inventory_checking.getValue(), orderGoods.getSpec1Id(), orderGoods.getSpecOption1Id(), orderGoods.getSpec2Id(), orderGoods.getSpecOption2Id(), orderGoods.getSpec3Id(), orderGoods.getSpecOption3Id(), orderGoods.getUnitId());
			if(stockLog != null) {
				stockLog.delete();
			}
		}
	}
	
	/**
	 * 创建商品库存
	 * @param tenantOrgId
	 * @param order
	 * @param orderGoods
	 * @param stockUnit 
	 * @param goodsStock 
	 * @return
	 */
	private InventoryStock createInventoryStock(InventoryChecking order, InventoryCheckingGoods orderGoods, BigDecimal goodsStock, GoodsUnit stockUnit) {
		InventoryStock outInventoryStock = new InventoryStock();
		outInventoryStock.setCreatedAt(new Date());
		outInventoryStock.setDataStatus(DataStatusEnum.enable.getValue());
		outInventoryStock.setGoodsInfoId(orderGoods.getGoodsInfoId());
		outInventoryStock.setInitStock(BigDecimal.ZERO);
		outInventoryStock.setLockStock(BigDecimal.ZERO);
		outInventoryStock.setReserveStock(BigDecimal.ZERO);
		outInventoryStock.setSpec1Id(orderGoods.getSpec1Id());
		outInventoryStock.setSpecOption1Id(orderGoods.getSpecOption1Id());
		outInventoryStock.setSpec2Id(orderGoods.getSpec2Id());
		outInventoryStock.setSpecOption2Id(orderGoods.getSpecOption2Id());
		outInventoryStock.setSpec3Id(orderGoods.getSpec3Id());
		outInventoryStock.setSpecOption3Id(orderGoods.getSpecOption3Id());
		outInventoryStock.setUnitId(stockUnit.getId());
		outInventoryStock.setStock(goodsStock);
		outInventoryStock.setUpdatedAt(new Date());
		outInventoryStock.setWarnType(StockWarnTypeEnum.ok.getValue());
		outInventoryStock.save();
		return outInventoryStock;
	}

	/**
	 * 设置单据审核状态
	 * @param tenantOrgId
	 * @param order
	 */
	private AuditStatusEnum getAuditStatus(InventoryChecking order) {
		TenantConfig auditConfig = TenantConfig.dao.findByKeyCache(TenantConfigEnum.audit_inventory_checking);
		if (Boolean.parseBoolean(auditConfig.getAttrValue())) { // 需要审核
			 return AuditStatusEnum.waiting;
		} else {
			if (order.getOrderStatus() == null || order.getOrderStatus() == OrderStatusEnum.normal.getValue()) { // 草稿不能自动审核
				return AuditStatusEnum.pass;
			} else {
				return AuditStatusEnum.waiting;
			}
		}
	}

	
}