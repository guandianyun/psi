/**
 * 
 */
package com.bytechainx.psi.web.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.CommonConstant;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.MsgTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.dto.ConditionFilter;
import com.bytechainx.psi.common.dto.ConditionFilter.Operator;
import com.bytechainx.psi.common.dto.UserSession;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.model.GoodsPrice;
import com.bytechainx.psi.common.model.InventoryStock;
import com.bytechainx.psi.common.model.MsgNotice;
import com.bytechainx.psi.common.model.MsgNoticeSend;
import com.bytechainx.psi.common.model.PurchaseOrder;
import com.bytechainx.psi.common.model.SaleOrder;
import com.bytechainx.psi.common.model.SaleOrderGoods;
import com.bytechainx.psi.common.model.TenantAdmin;
import com.bytechainx.psi.common.model.TraderBalanceAccount;
import com.bytechainx.psi.common.service.msg.MsgNoticeService;
import com.bytechainx.psi.purchase.service.PurchaseOrderService;
import com.bytechainx.psi.purchase.service.StatPurchaseService;
import com.bytechainx.psi.purchase.service.StockInfoService;
import com.bytechainx.psi.sale.service.SaleOrderService;
import com.bytechainx.psi.sale.service.StatHotSaleService;
import com.bytechainx.psi.sale.service.StatSaleService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.bytechainx.psi.web.web.interceptor.PermissionInterceptor;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.kit.Kv;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * 首页
 * @author defier
 */
@Path("/dashboard")
public class DashboardController extends BaseController {

	@Inject
	private MsgNoticeService msgNoticeService;
	@Inject
	private StatSaleService statSaleService;
	@Inject
	private SaleOrderService saleOrderService;
	@Inject
	private PurchaseOrderService purchaseOrderService;
	@Inject
	private StockInfoService stockInfoService;
	@Inject
	private StatPurchaseService statPurchaseService;
	@Inject
	private StatHotSaleService statHotSaleService;
	
	public void index() {
		
	}
	
	public void home() {
		UserSession session = getUserSession();
		SaleOrder todaySaleStat = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.todaySaleStat");
		if(todaySaleStat == null) {
			todaySaleStat = statSaleService.sumByCustomer(null, null, DateUtil.getDayStr(new Date())+" 00:00:00", DateUtil.getSecondStr(new Date()));
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.todaySaleStat", todaySaleStat);
		}
		SaleOrder yesterdaySaleStat = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.yesterdaySaleStat");
		if(yesterdaySaleStat == null) {
			yesterdaySaleStat = statSaleService.sumByCustomer(null, null, DateUtil.getDayStr(DateUtil.daysAddOrSub(new Date(),-1))+" 00:00:00", DateUtil.getDayStr(DateUtil.daysAddOrSub(new Date(),-1))+" 23:59:59");
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.yesterdaySaleStat", yesterdaySaleStat);
		}
		SaleOrder monthSaleStat = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.monthSaleStat");
		if(monthSaleStat == null) {
			monthSaleStat = statSaleService.sumByCustomer(null, null, DateUtil.getMonthFirstDay()+" 00:00:00", DateUtil.getMonthLastDay()+" 23:59:59");
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.monthSaleStat", monthSaleStat);
		}
		SaleOrder preMonthSaleStat = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.preMonthSaleStat");
		if(preMonthSaleStat == null) {
			preMonthSaleStat = statSaleService.sumByCustomer(null, null, DateUtil.getPreMonthFirstDay()+" 00:00:00", DateUtil.getPreMonthLastDay()+" 23:59:59");
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.preMonthSaleStat", preMonthSaleStat);
		}
		
		setAttr("todaySaleStat", todaySaleStat);
		setAttr("yesterdaySaleStat", yesterdaySaleStat);
		setAttr("monthSaleStat", monthSaleStat);
		setAttr("preMonthSaleStat", preMonthSaleStat);
		
		// 待收款销售单
		Page<SaleOrder> saleOrderPage = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.saleOrderPage");
		if(saleOrderPage == null) {
			Kv condKv = Kv.create();
			conditionFilterStore(condKv, Permissions.sale_sale); // 添加门店过滤条件
			condKv.set("order_status", OrderStatusEnum.normal.getValue());
			condKv.set("audit_status", AuditStatusEnum.pass.getValue());
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(OrderPayStatusEnum.finish.getValue());
			condKv.set("pay_status", filter);
			saleOrderPage = saleOrderService.paginate(null, null, condKv, 1, pageSize);
			
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.saleOrderPage", saleOrderPage);
		}
		
		// 待付款进货单
		Page<PurchaseOrder> purchaseOrderPage = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.purchaseOrderPage");
		if(purchaseOrderPage == null) {
			Kv condKv = Kv.create();
			conditionFilterStore(condKv, Permissions.sale_sale); // 添加门店过滤条件
			condKv.set("order_status", OrderStatusEnum.normal.getValue());
			condKv.set("audit_status", AuditStatusEnum.pass.getValue());
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(OrderPayStatusEnum.finish.getValue());
			condKv.set("pay_status", filter);
			purchaseOrderPage = purchaseOrderService.paginate(null, null, condKv, 1, pageSize);
			
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.purchaseOrderPage", purchaseOrderPage);
		}
		
		// 库存预警
		Page<InventoryStock> stockInfoPage = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.stockInfoPage");
		if(stockInfoPage == null) {
			Kv condKv = Kv.create();
			condKv.set("data_status", DataStatusEnum.enable.getValue());
			ConditionFilter filter = new ConditionFilter();
			filter.setOperator(Operator.neq);
			filter.setValue(StockWarnTypeEnum.ok.getValue());
			condKv.set("warn_type", filter);
			stockInfoPage = stockInfoService.paginate(condKv, 1, pageSize);
			
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.stockInfoPage", stockInfoPage);
		}
		
		if(session.hasOper(Permissions.sensitiveData_account_stat)) {
			//账户余额
			TraderBalanceAccount sumTraderBalanceAccount = TraderBalanceAccount.dao.findFirst("select sum(balance) as balance from trader_balance_account where data_status = ?", DataStatusEnum.enable.getValue());
			// 库存总成本
			BigDecimal sumStockAmount = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.sumStockAmount");
			if(sumStockAmount == null) {
				List<InventoryStock> sumInventoryStock = InventoryStock.dao.find("select goods_info_id,spec_1_id,spec_option_1_id,spec_2_id,spec_option_2_id,spec_3_id,spec_option_3_id,unit_id,sum(stock) as stock from inventory_stock where data_status = ? group by goods_info_id,spec_1_id,spec_option_1_id,spec_2_id,spec_option_2_id,spec_3_id,spec_option_3_id,unit_id", DataStatusEnum.enable.getValue());
				sumStockAmount = BigDecimal.ZERO; // 库存成本
				for (InventoryStock inventoryStock : sumInventoryStock) {
					GoodsPrice price = inventoryStock.getGoodsPrice();
					if(price == null) {
						continue;
					}
					sumStockAmount = sumStockAmount.add(price.getAvgCostPrice().multiply(inventoryStock.getStock()));
				}
				CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.sumStockAmount", sumStockAmount);
			}
			
			// 应收欠款
			SaleOrder sumSaleReceipt = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.sumSaleReceipt");
			if(sumSaleReceipt == null) {
				sumSaleReceipt = statSaleService.sumByCustomer(null, null, null, null);
				CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.sumSaleReceipt", sumSaleReceipt);
			}
			// 应付欠款
			PurchaseOrder sumPurchasePay = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.sumPurchasePay");
			if(sumPurchasePay == null) {
				sumPurchasePay = statPurchaseService.sumBySupplier(null, null, null, null);
				CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.sumPurchasePay", sumPurchasePay);
			}
			
			setAttr("sumBalance", sumTraderBalanceAccount.getBalance());
			setAttr("sumBalance", sumTraderBalanceAccount.getBalance());
			setAttr("sumStockAmount", sumStockAmount);
			setAttr("sumSaleReceipt", sumSaleReceipt);
			setAttr("sumPurchasePay", sumPurchasePay);
		}
		 
		setAttr("waitingPaySaleOrderCount", saleOrderPage.getTotalRow());
		setAttr("waitingPayPurchaseOrderCount", purchaseOrderPage.getTotalRow());
		setAttr("stockInfoWarnCount", stockInfoPage.getTotalRow());
	}
	
	/**
	 * 统计销售量，按天统计，年度按月统计
	 */
	public void loadSaleStatByDay() {
		Integer days = getInt("days"); // 30:近30天，90：近一季度, 365:近1年
		if(days == null) {
			renderJson(Ret.fail("参数非法"));
		}
		List<SaleOrder> saleStatList = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.loadSaleStatByDay.saleStatList."+days);
		List<Date> saleStatDayList = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.loadSaleStatByDay.saleStatDayList."+days);
		if(saleStatList == null) {
			saleStatList = new ArrayList<>();
			saleStatDayList = new ArrayList<>();
			int time = days;
			if(days == 365) {
				time = 12;// 12个月，按月统计
			}
			for (int beforeDay = time-1; beforeDay >= 0; beforeDay--) {
				Date dayDate = DateUtil.daysAddOrSubNow(-beforeDay);
				String startTime = DateUtil.getDayStr(dayDate)+" 00:00:00";
				String endTime = DateUtil.getDayStr(dayDate)+" 23:59:59";
				if(days == 365) {
					dayDate = DateUtil.getYearMonthDate(DateUtil.monthsAddOrSub(new Date(), -beforeDay));
					startTime = DateUtil.getMonthFirstDay(dayDate)+" 00:00:00";
					endTime = DateUtil.getMonthLastDay(dayDate)+" 23:59:59";
					
				}
				
				SaleOrder saleStat = statSaleService.sumByCustomer(null, null, startTime, endTime);

				saleStatList.add(saleStat);
				saleStatDayList.add(dayDate);
			}
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.loadSaleStatByDay.saleStatList."+days, saleStatList);
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.loadSaleStatByDay.saleStatDayList."+days, saleStatDayList);
		}
		setAttr("saleStatList", saleStatList);
		setAttr("saleStatDayList", saleStatDayList);
		setAttr("days", days);
	}
	
	/**
	 * 统计销售量，按天统计，年度按月统计
	 */
	public void loadGoodsStatByDay() {
		Integer days = getInt("days"); // 30:近30天，90：近一季度, 365:近1年
		if(days == null || days > 365) {
			renderJson(Ret.fail("参数非法"));
		}
		Page<SaleOrderGoods> goodsStatPage = CacheKit.get(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.loadGoodsStatByDay.goodsStatList."+days);
		if(goodsStatPage == null) {
			Date dayDate = DateUtil.daysAddOrSubNow(-days);
			String startTime = DateUtil.getDayStr(dayDate)+" 00:00:00";
			String endTime = DateUtil.getDayStr(new Date())+" 23:59:59";
			
			goodsStatPage = statHotSaleService.paginate(null, startTime, endTime, 1, 16);
			// 升序排序
			Collections.sort(goodsStatPage.getList(), Comparator.comparing(SaleOrderGoods::getBuyNumber));
			
			CacheKit.put(CommonConstant.CACHE_NAME_ONE_MINUTE_STORE, "home.stat.loadGoodsStatByDay.goodsStatList."+days, goodsStatPage);
		}
		setAttr("goodsStatList", goodsStatPage.getList());
	}
	
	/**
	 * 检测请求执行结果
	 */
	public void checkRequestResult() {
		String requestId = get("request_id");
		if(StringUtils.isEmpty(requestId)) {
			renderJson(Ret.fail("非法请求..."));
			return;
		}
		Cache redis = Redis.use();
		String response = redis.lpop(requestId);
		if(response == null) {
			renderJson(Ret.fail("waiting"));
			return;
		}
		renderJson(response);
	}
	
	/**
	 * 心跳
	 */
	public void heartBeat() {
		TenantAdmin tenantAdmin = getCurrentAdmin();
		if(tenantAdmin.getActiveStatus() != UserActiveStatusEnum.enable.getValue()) {
			tenantAdmin = TenantAdmin.dao.findById(tenantAdmin.getId()); // 避免缓存没有更新
			if(tenantAdmin.getActiveStatus() != UserActiveStatusEnum.enable.getValue()) {
				removeSessionAttr(CommonConstant.SESSION_ID_KEY);
				renderJson(Ret.fail("用户状态异常"));
				return;
			}
		}
		UserSession session = getUserSession();
		session.setHeartTime(new Date());
		
		// 重要的消息需要再右下角弹窗，紧急的直接居中弹窗。
		Page<MsgNoticeSend> noticePage = msgNoticeService.paginate(getAdminId(), false, 1, 10);
		Ret ret = Ret.ok();
		if(noticePage.getTotalRow() > 0) {
			for (MsgNoticeSend msg : noticePage.getList()) {
				MsgNotice msgNotice = msg.getMsgNotice();
				msg.put("msg_level", msgNotice.getMsgLevel());
				msg.put("msg_type_name", MsgTypeEnum.getEnum(msgNotice.getMsgType()).getName());
				msg.put("title", msgNotice.getTitle());
			}
			ret.set("noticePage", noticePage);// FIXME 单独一个待办事项？？？待审核销售单、进货单，销售订单预计发货到期、这些都要且只要生成一条消息到消息通知表
		}
		
		tenantAdmin.setOnlineTimes(tenantAdmin.getOnlineTimes()+2); // 心跳2分钟一次
		tenantAdmin.setUpdatedAt(new Date());
		tenantAdmin.update();
		
		renderJson(ret);
	}
	
	/**
	 * 客服
	 */
	@Clear(PermissionInterceptor.class)
	public void service() {
		
	}
	
}
