package com.bytechainx.psi.common.service.base;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.MsgLevelEnum;
import com.bytechainx.psi.common.EnumConstant.MsgTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.kit.SmsKit;
import com.bytechainx.psi.common.model.CustomerInfo;
import com.bytechainx.psi.common.model.GoodsStockConfig;
import com.bytechainx.psi.common.model.InventoryCheckingGoods;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.MsgNotice;
import com.bytechainx.psi.common.model.MsgNoticeSend;
import com.bytechainx.psi.common.model.PurchaseOrderGoods;
import com.bytechainx.psi.common.model.PurchaseRejectOrderGoods;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.SaleRejectOrderGoods;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TenantConfig;
import com.jfinal.kit.Kv;

public class CommonService {

	
	/**
	 * 更新库存告警状态
	 */
	protected void updateStockWarn(List<?> orderGoodsList) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				for (Object model : orderGoodsList) {
					if(model instanceof InventoryCheckingGoods) {
						InventoryCheckingGoods _model = (InventoryCheckingGoods)model;
						updateStockWarn(_model.getGoodsInfoId(), _model.getSpec1Id(), _model.getSpecOption1Id(),_model.getSpec2Id(), _model.getSpecOption2Id(),_model.getSpec3Id(), _model.getSpecOption3Id(), _model.getUnitId());
					} else if(model instanceof PurchaseOrderGoods) {
						PurchaseOrderGoods _model = (PurchaseOrderGoods)model;
						updateStockWarn(_model.getGoodsInfoId(), _model.getSpec1Id(), _model.getSpecOption1Id(),_model.getSpec2Id(), _model.getSpecOption2Id(),_model.getSpec3Id(), _model.getSpecOption3Id(), _model.getUnitId());

					} else if(model instanceof PurchaseRejectOrderGoods) {
						PurchaseRejectOrderGoods _model = (PurchaseRejectOrderGoods)model;
						updateStockWarn(_model.getGoodsInfoId(), _model.getSpec1Id(), _model.getSpecOption1Id(),_model.getSpec2Id(), _model.getSpecOption2Id(),_model.getSpec3Id(), _model.getSpecOption3Id(), _model.getUnitId());

					} else if(model instanceof SaleOrderGoods) {
						SaleOrderGoods _model = (SaleOrderGoods)model;
						updateStockWarn(_model.getGoodsInfoId(), _model.getSpec1Id(), _model.getSpecOption1Id(),_model.getSpec2Id(), _model.getSpecOption2Id(),_model.getSpec3Id(), _model.getSpecOption3Id(), _model.getUnitId());

					} else if(model instanceof SaleRejectOrderGoods) {
						SaleRejectOrderGoods _model = (SaleRejectOrderGoods)model;
						updateStockWarn(_model.getGoodsInfoId(), _model.getSpec1Id(), _model.getSpecOption1Id(),_model.getSpec2Id(), _model.getSpecOption2Id(),_model.getSpec3Id(), _model.getSpecOption3Id(), _model.getUnitId());

					}
				}
			}
		};
		thread.start();
	}

	private void updateStockWarn(Integer goodsId, Integer spec1Id, Integer specOption1Id, Integer spec2Id, Integer specOption2Id, Integer spec3Id, Integer specOption3Id, Integer unitId) {
		GoodsStockConfig stockConfig = GoodsStockConfig.dao.findBySpec(goodsId, spec1Id, specOption1Id, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
		if(stockConfig == null) {
			return;
		}
		InventoryStock inventoryStock = InventoryStock.dao.findByWarehouse(goodsId, spec1Id, specOption1Id, spec2Id, specOption2Id, spec3Id, specOption3Id, unitId);
		if(inventoryStock == null || inventoryStock.getStock() == null) {
			return;
		}
		if(inventoryStock.getStock().compareTo(stockConfig.getLowStock()) < 0) { // 小于最低库存
			inventoryStock.setWarnType(StockWarnTypeEnum.lowest.getValue());
			
		} else if(inventoryStock.getStock().compareTo(stockConfig.getHighStock()) > 0) {
			inventoryStock.setWarnType(StockWarnTypeEnum.highest.getValue());
			
		} else if(inventoryStock.getStock().compareTo(stockConfig.getSafeStock()) < 0) {
			inventoryStock.setWarnType(StockWarnTypeEnum.lowsafe.getValue());
			
		} else {
			inventoryStock.setWarnType(StockWarnTypeEnum.ok.getValue());
		}
		
		inventoryStock.setUpdatedAt(new Date());
		inventoryStock.update();
	}
	
	/**
	 * 给客户发送短信通知，异步执行
	 * @param saleOrder
	 */
	protected void sendSaleOrderNotice(SaleOrder saleOrder) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				if (saleOrder.getAuditStatus() != AuditStatusEnum.pass.getValue()) {
					return;
				}
				//{config_open:'true',customer_delivery_ai_notice:'true', customer_delivery_sms_notice:'false', customer_delivery_weixin_notice:'false'}
				JSONObject noticeConfig = TenantConfig.dao.findJsonByKeyCahce(TenantConfigEnum.customer_delivery_notice_config);
				if(!noticeConfig.getBooleanValue("config_open")) { // 是否开启
					return;
				}
				CustomerInfo customerInfo = saleOrder.getCustomerInfo();
				if(noticeConfig.getBooleanValue("customer_delivery_weixin_notice")) { // 微信通知
				}
				if(noticeConfig.getBooleanValue("customer_delivery_sms_notice")) { // 短信通知
					String content = "尊敬的"+customerInfo.getName()+"，您订购的商品("+saleOrder.getOrderGoodsStr()+")已发货，请注意查收";
					SmsKit.sendNoticeSms(customerInfo.getMobile(), content);
				}
			}
		};
		
		thread.start();
	}
	
	/**
	 * 发送消息通知
	 * @param tenantOrgId 
	 * @param payOrderAudit
	 * @param title
	 * @param content
	 * @param auditorId
	 * @param smsFlag
	 * @param sysFlag
	 */
	protected void sendNoticeMsg(MsgDataTypeEnum dataType, String title, String content, Integer auditorId, Boolean smsFlag, Boolean sysFlag) {
		if(smsFlag != null && smsFlag) {
			TenantAdmin admin = TenantAdmin.dao.findById(auditorId);
			SmsKit.sendNoticeSms(admin.getMobile(), title);
		}
		if(sysFlag != null && sysFlag) {
			MsgNotice msgNotice = new MsgNotice();
			msgNotice.setContent(content);
			msgNotice.setCreatedAt(new Date());
			msgNotice.setDataType(dataType.getValue());
			msgNotice.setMsgLevel(MsgLevelEnum.general.getValue());
			msgNotice.setMsgType(MsgTypeEnum.systemNotice.getValue());
			msgNotice.setSenderId(0);
			msgNotice.setSenderName("系统");
			msgNotice.setTitle(title);
			msgNotice.save();
			
			MsgNoticeSend msgNoticeSend = new MsgNoticeSend();
			msgNoticeSend.setCreatedAt(new Date());
			msgNoticeSend.setMsgNoticeId(msgNotice.getId());
			msgNoticeSend.setReadFlag(false);
			msgNoticeSend.setReceiverId(auditorId);
			msgNoticeSend.setSenderId(0);
			msgNoticeSend.setSenderName("系统");
			msgNoticeSend.save();
		}
	}
	
	/**
	 * 组装条件查询
	 * @param conditionColumns
	 * @param where
	 * @param params
	 */
	protected void conditionFilter(Kv conditionColumns, StringBuffer where, List<Object> params) {
		if(conditionColumns == null || conditionColumns.isEmpty()) {
			return;
		}
		for (Object key : conditionColumns.keySet()) {
			Object value = conditionColumns.get(key);
			if(value == null) {
				continue;
			}
			if(value instanceof Integer) {
				where.append(" and "+ key +"  = ?");
				params.add(value);
			} else if(value instanceof String) {
				String _key = (String)key;
				String[] columns = _key.split(","); // 多字段模糊查询，字段用逗号隔开
				where.append(" and (");
				for(int index = 0; index < columns.length; index++) {
					where.append(columns[index] +"  like ?");
					params.add("%"+value+"%");
					if(index < columns.length -1) {
						where.append(" or ");
					}
				}
				where.append(" )");
			} else if(value instanceof Boolean) {
				where.append(" and "+ key +"  = ?");
				params.add(value);
			}  else if(value instanceof ConditionFilter) {
				ConditionFilter filter = (ConditionFilter) value;
				if(filter.getOperator().equals(Operator.eq)) {
					where.append(" and "+ key +"  = ?");
					params.add(filter.getValue());
				} else if(filter.getOperator().equals(Operator.neq)) {
					where.append(" and "+ key +"  != ?");
					params.add(filter.getValue());
				} else if(filter.getOperator().equals(Operator.in)) {
					where.append(" and "+ key +"  in ("+ filter.getValue() +") ");
				} else if(filter.getOperator().equals(Operator.notIn)) {
					where.append(" and "+ key +" not in ("+ filter.getValue() +") ");
				} else if(filter.getOperator().equals(Operator.like)) {
					where.append(" and "+ key +"  like ? ");
					params.add("%"+filter.getValue()+"%");
				}  else if(filter.getOperator().equals(Operator.lt)) {
					where.append(" and "+ key +"  < ?");
					params.add(filter.getValue());
				} else if(filter.getOperator().equals(Operator.more)) {
					int paramLength = ((String)key).split("\\?").length-1;
					where.append(" and "+ key);
					for (int i = 0; i < paramLength; i++) {
						params.add(filter.getValue());
					}
				}
			}
		}
	}
	
	
}
