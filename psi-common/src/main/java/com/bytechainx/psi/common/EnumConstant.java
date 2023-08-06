package com.bytechainx.psi.common;

import java.math.BigDecimal;

/**
 * 枚举常量表
 * 
 * @author defier
 *
 */
public final class EnumConstant {

	/** 交易类型 **/
	public enum TraderTypeEnum {

		recharge(1, "帐户充值"), withdraw(2, "用户提现"), open(3, "购买产品"), renew(4, "产品续费"), module(5, "购买模块"), sms(6, "短信购买"), account(7, "购买帐户");

		private int value;

		private String name;

		private TraderTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static TraderTypeEnum getEnum(int value) {
			for (TraderTypeEnum c : TraderTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 购买类型 **/
	public enum BuyTypeEnum {

		NEWBUY(1, "新购"), RECHARGE(2, "续费");

		private int value;

		private String name;

		private BuyTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static BuyTypeEnum getEnum(int value) {
			for (BuyTypeEnum c : BuyTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 支付订单类型 **/
	public enum PayOrderTypeEnum {

		saleOrder(1, "销售单");

		private int value;

		private String name;

		private PayOrderTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static PayOrderTypeEnum getEnum(int value) {
			for (PayOrderTypeEnum c : PayOrderTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 资金流向 **/
	public enum FundFlowEnum {

		income(1, "收入"), expenses(2, "支出"), adjust(3, "调整");

		private int value;

		private String name;

		private FundFlowEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static FundFlowEnum getEnum(int value) {
			for (FundFlowEnum c : FundFlowEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 审核类型 **/
	public enum AuditStatusEnum {

		waiting(1, "待审核"), pass(2, "审核通过"), reject(3, "审核拒绝");

		private int value;

		private String name;

		private AuditStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static AuditStatusEnum getEnum(int value) {
			for (AuditStatusEnum c : AuditStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 在线支付付款状态
	 */
	public enum OnlinePayStatusEnum {

		nopay(1, "未发起付款"), wait(2, "已发起付款"), success(3, "付款成功"), fail(4, "付款失败"), reject(5, "付款拒绝");

		private int value;

		private String name;

		private OnlinePayStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OnlinePayStatusEnum getEnum(int value) {
			for (OnlinePayStatusEnum c : OnlinePayStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 租户订单付款状态
	 */
	public enum TenantOrderPayStatusEnum {

		wait(1, "待付款"), success(2, "已付款"), close(3, "交易关闭");

		private int value;

		private String name;

		private TenantOrderPayStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static TenantOrderPayStatusEnum getEnum(int value) {
			for (TenantOrderPayStatusEnum c : TenantOrderPayStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 支付渠道
	 */
	public enum PaySourceEnum {

		balance(1, "帐户余额"), offline(2, "线下支付"), online(3, "第三方支付");

		private int value;

		private String name;

		private PaySourceEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static PaySourceEnum getEnum(int value) {
			for (PaySourceEnum c : PaySourceEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 是、否 常量 **/
	public enum FlagEnum {

		YES(true, "是"), NO(false, "否");

		private boolean value;

		private String name;

		private FlagEnum(boolean value, String name) {
			this.value = value;
			this.name = name;
		}

		public boolean getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static FlagEnum getEnum(boolean value) {
			for (FlagEnum c : FlagEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 地区级别 **/
	public enum RegionLevelEnum {

		COUNTRY(1, "国家"), PROVINCE(2, "省"), CITY(3, "市"), COUNTY(4, "区/县");

		private int value;

		private String name;

		private RegionLevelEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static RegionLevelEnum getEnum(int value) {
			for (RegionLevelEnum c : RegionLevelEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	
	/** 发送状态 **/
	public enum SendStatusEnum {
		
		WAITING(-1, "待发送"), 
		SUCCESS(0, "成功"), 
		FAIL(1, "失败");

		private int value;

		private String name;

		private SendStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}
		
		public static SendStatusEnum getEnum(int value) {
			for (SendStatusEnum c : SendStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 用户状态 **/
	public enum UserActiveStatusEnum {

		waiting(0, "未激活"), enable(1, "启用"), disable(2, "停用");

		private int value;

		private String name;

		private UserActiveStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static UserActiveStatusEnum getEnum(int value) {
			for (UserActiveStatusEnum c : UserActiveStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 操作权限类型 **/
	public enum OperTypeEnum {

		feature(0, "功能权限"), data(1, "敏感数据权限");

		private int value;

		private String name;

		private OperTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OperTypeEnum getEnum(int value) {
			for (OperTypeEnum c : OperTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 门店类型 **/
	public enum StoreTypeEnum {

		directSale(1, "直营店"), joinSale(2, "加盟店");

		private int value;

		private String name;

		private StoreTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static StoreTypeEnum getEnum(int value) {
			for (StoreTypeEnum c : StoreTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 付款类型 **/
	public enum SalePayTypeEnum {

		nowCash(1, "现结"), monthCash(2, "月结"), deliveryCash(3, "货到付款"), depositBefore(4, "订金+发货前收尾款"), depositAfter(5, "订金+发货后尾款");

		private int value;

		private String name;

		private SalePayTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static SalePayTypeEnum getEnum(int value) {
			for (SalePayTypeEnum c : SalePayTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 经营模式 **/
	public enum SaleModeEnum {

		wholesale(1, "批发"), retail(2, "零售"), all(3, "批零兼营");

		private int value;

		private String name;

		private SaleModeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static SaleModeEnum getEnum(int value) {
			for (SaleModeEnum c : SaleModeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 通知消息类别 **/
	public enum MsgTypeEnum {
		systemNotice(0, "系统通知"), bizWarn(1, "业务预警"), announce(2, "企业公告");

		private int value;

		private String name;

		private MsgTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static MsgTypeEnum getEnum(int value) {
			for (MsgTypeEnum c : MsgTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	/** 通知消息等级 **/
	public enum MsgLevelEnum {
		general(0, "普通"), important(1, "重要"), urgent(2, "紧急");

		private int value;

		private String name;

		private MsgLevelEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static MsgLevelEnum getEnum(int value) {
			for (MsgLevelEnum c : MsgLevelEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 商品类型 **/
	public enum GoodsTypeEnum {
		general(0, "普通商品"), assembly(1, "组合商品");

		private int value;

		private String name;

		private GoodsTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static GoodsTypeEnum getEnum(int value) {
			for (GoodsTypeEnum c : GoodsTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 日志类型 **/
	public enum DbLogTypeEnum {
		reset(1, "系统重置"), restore(2, "数据恢复");

		private int value;

		private String name;

		private DbLogTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static DbLogTypeEnum getEnum(int value) {
			for (DbLogTypeEnum c : DbLogTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 单据状态 **/
	public enum OrderStatusEnum {

		draft(0, "草稿"), normal(1, "正常"), disable(2, "作废"), close(3, "关闭");

		private int value;

		private String name;

		private OrderStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OrderStatusEnum getEnum(int value) {
			for (OrderStatusEnum c : OrderStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 商品属性类型 **/
	public enum GoodsAttrTypeEnum {
		select(1, "选择型"), input(2, "输入型");

		private int value;

		private String name;

		private GoodsAttrTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static GoodsAttrTypeEnum getEnum(int value) {
			for (GoodsAttrTypeEnum c : GoodsAttrTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 数据状态 **/
	public enum DataStatusEnum {
		enable(1, "启用"), disable(2, "停用"), delete(3, "删除");

		private int value;

		private String name;

		private DataStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static DataStatusEnum getEnum(int value) {
			for (DataStatusEnum c : DataStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 提成方式 **/
	public enum CommissionTypeEnum {
		receipts(1, "按销售实收"), receiptsProfit(2, "按实收毛利");

		private int value;

		private String name;

		private CommissionTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static CommissionTypeEnum getEnum(int value) {
			for (CommissionTypeEnum c : CommissionTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 提成计算规则 **/
	public enum CommissionRuleTypeEnum {
		all(1, "统一提成"), step(2, "阶梯提成");

		private int value;

		private String name;

		private CommissionRuleTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static CommissionRuleTypeEnum getEnum(int value) {
			for (CommissionRuleTypeEnum c : CommissionRuleTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 提成适用员工 **/
	public enum CommissionApplyTypeEnum {
		all(0, "所有员工"), assign(1, "指定员工");

		private int value;

		private String name;

		private CommissionApplyTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static CommissionApplyTypeEnum getEnum(int value) {
			for (CommissionApplyTypeEnum c : CommissionApplyTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 销售单和进货单的收付款类型 **/
	public enum FundTypeEnum {

		cash(1, "本单现付"), balance(2, "余额扣款"), checking(3, "核销清账");

		private int value;

		private String name;

		private FundTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static FundTypeEnum getEnum(int value) {
			for (FundTypeEnum c : FundTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 关联单类型 **/
	public enum RefOrderTypeEnum {

		sale_order(1, "销售单"), sale_reject_order(2, "销售退货单"), purchase_order(3, "进货单"), purchase_reject_order(4, "进货退货单"), trader_receipt_order(5, "收款单"),
		trader_pay_order(6, "付款单"), trader_income_expenses(7, "日常收支单"), trader_transfer_order(8, "账户互转单"), balance_account(9, "账户调整");

		private int value;

		private String name;

		private RefOrderTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static RefOrderTypeEnum getEnum(int value) {
			for (RefOrderTypeEnum c : RefOrderTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 对账关联单类型 **/
	public enum CheckingRefOrderTypeEnum {

		sale_order(1, "销售单"), sale_reject_order(2, "销售退货单"), purchase_order(3, "进货单"), purchase_reject_order(4, "进货退货单"), trader_receipt_order(5, "收款单"), trader_pay_order(6, "付款单");

		private int value;

		private String name;

		private CheckingRefOrderTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static CheckingRefOrderTypeEnum getEnum(int value) {
			for (CheckingRefOrderTypeEnum c : CheckingRefOrderTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 商品出入库单据类型 **/
	public enum StockInOutOrderTypeEnum {

		sale_order(1, "销售单"), sale_reject_order(2, "销售退货单"), purchase_order(3, "进货单"), purchase_reject_order(4, "进货退货单"), inventory_checking(5, "库存盘点单"), inventory_swap(6, "库存调拨单");

		private int value;

		private String name;

		private StockInOutOrderTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static StockInOutOrderTypeEnum getEnum(int value) {
			for (StockInOutOrderTypeEnum c : StockInOutOrderTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 租户配置 **/
	public enum TenantConfigEnum {

		// 审核设置
		audit_purchase_order("false", "进货单"), 
		audit_purchase_reject_order("false", "进货退货单"), 
		audit_inventory_checking("false", "盘点单"), 
		audit_inventory_swap("false", "调拨单"),
		audit_sale_order("false", "销售单"), 
		audit_sale_reject_order("false", "销售退货单"), 
		audit_trader_pay_order("false", "付款单"), 
		audit_trader_receipt_order("false", "收款单"),
		audit_trader_income_expenses("false", "日常收支单"), 
		audit_trader_transfer_order("false", "结算账户互转单"),
		
		// 单据默认显示的行数，每个租户可自定义
		rows_purchase_order("8", "进货单默认行数"),
		rows_purchase_reject_order("8", "进货退货单默认行数"),
		rows_inventory_checking("13", "盘点单默认行数"),
		rows_inventory_swap("8", "调拨单默认行数"),
		rows_sale_order("8", "销售单默认行数"),
		rows_sale_reject_order("8", "销售退货单默认行数"),
		
		// 单据其他费用，多个使用逗号隔开
		fee_purchase_order("", "进货单其他费用"),
		fee_purchase_reject_order("", "进货退货单其他费用"),
		fee_sale_order("", "销售单其他费用"),
		fee_sale_reject_order("", "销售退货单其他费用"),
		
		// 单据支出成本，多个使用逗号隔开
		cost_purchase_order("", "进货单支出成本"),
		cost_purchase_reject_order("", "进货退货单支出成本"),
		cost_sale_order("", "销售单支出成本"),
		cost_sale_reject_order("", "销售退货单支出成本"),
		
		// 自动化通知
		customer_fee_notice_config("{config_open:false,customer_fee_ai_notice:false,customer_fee_sms_notice:false,customer_fee_weixin_notice:false}", "客户消费通知"),
		customer_delivery_notice_config("{config_open:false,customer_delivery_ai_notice:true, customer_delivery_sms_notice:false, customer_delivery_weixin_notice:false}", "发货通知客户"),
		order_audit_notice_config("{config_open:false,purchaseOrder_auditor_id:0,purchaseOrder_audit_sms_notice:false,purchaseOrder_audit_system_notice:false,saleOrder_auditor_id:0,saleOrder_audit_sms_notice:false,saleOrder_audit_system_notice:false,fundOrder_auditor_id:0,fundOrder_audit_sms_notice:false,fundOrder_audit_system_notice:false,audit_handler_sms_notice:false,audit_handler_system_notice:false,audit_makeMan_sms_notice:false,audit_makeMan_system_notice:false}", "单据审核通知"),
		

		create_order_print_confirm("{sale_book_order_flag:false,sale_book_order_confirm:false,sale_order_flag:true,sale_order_confirm:true,sale_reject_order_flag:false,sale_reject_order_confirm:false,purchase_book_order_flag:false,purchase_book_order_confirm:false,purchase_order_flag:true,purchase_order_confirm:true,purchase_reject_order_flag:false,purchase_reject_order_confirm:false,inventory_checking_flag:false,inventory_checking_confirm:false,inventory_swap_flag:true,inventory_swap_confirm:true,trader_pay_flag:false,trader_pay_confirm:false,trader_receipt_flag:false,trader_receipt_confirm:false}", "单据打印设置"),
		
		// 通用设置
		common_order_code_rule("{type:2,seq:4}", "单据编号规则"),
		order_goods_discount("false", "单据启用单行折扣"),
		order_fund_paytime("false", "单据启用收付款日期"),
		sale_negative_stock("false", "销售时允许负库存"),
		sale_create_price_lowest("true", "销售开单，价格低于进货价提醒"),
		sale_receipt_fund_all("false", "销售单本次收款默认全部收款"),
		sale_get_last_price("true", "非系统预置客户，自动获取商品的上次结算价和上次折后价"),
		sale_show_goods_profit("false", "销售单显示商品毛利润"),
		
		purchase_receipt_fund_all("false", "进货单本次收款默认全部收款"),
		purchase_edit_sale_price("true", "在录入进货单时，商品进货价发生变化，提示修改销售价"),
		
		swap_goods_negative_stock("false", "调拨时允许负库存"),
		
		sale_order_odd_type(OrderOddTypeEnum.subJiao.getValue()+"", "销售单抹零"),
		
		cashier_show_goods_pic("true", "显示商品图片"),
		cashier_show_goods_stock("true", "显示商品库存"),
		cashier_hide_zerostock_goods("false", "隐藏售罄商品"),
		cashier_sale_order_create_print("false", "开单并打印"),
		cashier_receipt_fund_voice_broadcast("true", "收款语音播报"),
		cashier_default_out_warehouse_id("", "默认出库仓库"),
		
		;
		
		private String value; // 默认值

		private String name;

		private TenantConfigEnum(String value, String name) {
			this.value = value;
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static TenantConfigEnum getEnum(String name) {
			for (TenantConfigEnum c : TenantConfigEnum.values()) {
				if (name.equalsIgnoreCase(c.name())) {
					return c;
				}
			}
			return null;
		}
	}
	
	/**
	 * 抹零设置
	 */
	public enum OrderOddTypeEnum {

		subFen(1, "抹分"), subJiao(2, "抹角"), halfUpJiao(3, "四舍五入到\"角\""), halfUpYuan(4, "四舍五入到\"元\"");

		private int value;

		private String name;

		private OrderOddTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OrderOddTypeEnum getEnum(int value) {
			for (OrderOddTypeEnum c : OrderOddTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 订单付款状态
	 */
	public enum OrderPayStatusEnum {

		no(1, "未付"), part(2, "部分付"), finish(3, "已付清");

		private int value;

		private String name;

		private OrderPayStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OrderPayStatusEnum getEnum(int value) {
			for (OrderPayStatusEnum c : OrderPayStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	

	/**
	 * 进货单物流状态
	 */
	public enum LogisticsStatusInEnum {

		waiting(1, "待入库"), stockin(2, "已入库");

		private int value;

		private String name;

		private LogisticsStatusInEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static LogisticsStatusInEnum getEnum(int value) {
			for (LogisticsStatusInEnum c : LogisticsStatusInEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 出入库类型
	 */
	public enum StockIoTypeEnum {

		in(1, "入库"), out(2, "出库"), adjust(3, "调整");
		;

		private int value;

		private String name;

		private StockIoTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static StockIoTypeEnum getEnum(int value) {
			for (StockIoTypeEnum c : StockIoTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 出库物流状态
	 */
	public enum LogisticsStatusEnum {

		waiting(1, "待出库"), stockout(2, "已出库"), arrivelogisticscompany(3, "货到物流"), loading(4, "装车中"), ontheway(5, "在途中"), arrived(6, "已到站"), deliverying(7, "配送中"), signed(8, "客户签收");

		private int value;

		private String name;

		private LogisticsStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static LogisticsStatusEnum getEnum(int value) {
			for (LogisticsStatusEnum c : LogisticsStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 单据编号规则
	 */
	public enum OrderCodeRuleTypeEnum {

		yy(1, "两位年份"), yymm(2, "两位年份+两位月份"), yymmdd(3, "两位年份+四位日期");

		private int value;

		private String name;

		private OrderCodeRuleTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OrderCodeRuleTypeEnum getEnum(int value) {
			for (OrderCodeRuleTypeEnum c : OrderCodeRuleTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 退货处理类型 **/
	public enum RejectDealTypeEnum {

		stockin(1, "入库"), scrap(2, "报废");

		private int value;

		private String name;

		private RejectDealTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static RejectDealTypeEnum getEnum(int value) {
			for (RejectDealTypeEnum c : RejectDealTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 库存告警类型 **/
	public enum StockWarnTypeEnum {
		ok(0, "正常"), lowest(1, "低于最低库存"), lowsafe(2, "低于安全库存"), highest(3, "高于最高库存");

		private int value;

		private String name;

		private StockWarnTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static StockWarnTypeEnum getEnum(int value) {
			for (StockWarnTypeEnum c : StockWarnTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 优惠类型 **/
	public enum DiscountTypeEnum {
		percent(1, "折扣百分比"), amount(2, "优惠金额");

		private int value;

		private String name;

		private DiscountTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static DiscountTypeEnum getEnum(int value) {
			for (DiscountTypeEnum c : DiscountTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 物流配送服务 **/
	public enum LogisticsServiceEnum {
		no(0, "无"), delivery_install(1, "配送安装");

		private int value;

		private String name;

		private LogisticsServiceEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static LogisticsServiceEnum getEnum(int value) {
			for (LogisticsServiceEnum c : LogisticsServiceEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 退货原因 **/
	public enum RejectReasonTypeEnum {
		goods_damage(1, "商品破损"), goods_discrepancy(2, "商品不符"), delivery_timeout(3, "未按时间发货"), goods_quality(4, "商品质量问题"), other(0, "其他");

		private int value;

		private String name;

		private RejectReasonTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static RejectReasonTypeEnum getEnum(int value) {
			for (RejectReasonTypeEnum c : RejectReasonTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	public enum IndustryEnum {

		food(1, "食品酒饮"), fashion(2, "服装鞋帽"), material(3, "五金建材"), electrical(4, "家电数码"), book(5, "文体图书"), daylife(7, "日化用品"), mombaby(8, "母婴用品"), automobile(9, "汽修汽配"), supermarket(10, "商超便利"), medical(11, "医疗器械"), other(0, "其他行业");

		private int value;
		private String name;

		private IndustryEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static IndustryEnum getEnum(Integer value) {
			for (IndustryEnum c : IndustryEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 操作日志类型 **/
	public enum OperLogTypeEnum {

		login(1, "登录/登出日志"), create(2, "创建日志"), update(3, "修改日志"), delete(4, "删除日志"), setting(5, "其他设置"), operDelete(0, "操作日志删除");

		private int value;

		private String name;

		private OperLogTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OperLogTypeEnum getEnum(int value) {
			for (OperLogTypeEnum c : OperLogTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 操作日志来源平台 **/
	public enum PlatformTypeEnum {

		web(1, "web前端"), app(2, "APP前端"), miniApp(3, "小程序端"), wechat(3, "公众号端");

		private int value;

		private String name;

		private PlatformTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static PlatformTypeEnum getEnum(int value) {
			for (PlatformTypeEnum c : PlatformTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/**
	 * 购买帐户折扣
	 */
	public enum AccountFeeDiscountEnum {

		account_1(1, BigDecimal.ONE, "1个帐户"), account_3(3, new BigDecimal(0.95), "3个帐户"), account_10(10, new BigDecimal(0.9), "10个帐户"), account_20(20, new BigDecimal(0.85), "20个帐户"), account_50(50, new BigDecimal(0.7), "50个帐户");

		private BigDecimal discount;
		private int count;
		private String name;

		private AccountFeeDiscountEnum(int count, BigDecimal discount, String name) {
			this.count = count;
			this.discount = discount;
			this.name = name;
		}

		public BigDecimal getDiscount() {
			return discount;
		}

		public int getCount() {
			return count;
		}

		public String getName() {
			return name;
		}

		public static AccountFeeDiscountEnum getEnum(int count) {
			for (AccountFeeDiscountEnum c : AccountFeeDiscountEnum.values()) {
				if (count == c.getCount()) {
					return c;
				}
			}
			return null;
		}

	}

	/**
	 * 手续费支付方
	 */
	public enum FeePayAccountEnum {

		out(1, "转出账户支付"), in(2, "转入账户支付");

		private int value;
		private String name;

		private FeePayAccountEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static FeePayAccountEnum getEnum(int value) {
			for (FeePayAccountEnum c : FeePayAccountEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}

	}
	
	/** 库存预警值设置类型 **/
	public enum StockConfigWarnTypeEnum {
		unify(1, "统一设置（不按规格设置）"), alone(2, "独立设置（按规格区分来设置）");

		private int value;

		private String name;

		private StockConfigWarnTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static StockConfigWarnTypeEnum getEnum(int value) {
			for (StockConfigWarnTypeEnum c : StockConfigWarnTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	/** 单据默认单位类型 **/
	public enum OrderUnitTypeEnum {
		purchase(1, "进货"), sale(2, "销售"), stock(3, "库存");

		private int value;

		private String name;

		private OrderUnitTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OrderUnitTypeEnum getEnum(int value) {
			for (OrderUnitTypeEnum c : OrderUnitTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	/** 盘点类型 **/
	public enum InventoryCheckTypeEnum {
		hand(1, "手动盘点"), scan(2, "扫码盘点");

		private int value;

		private String name;

		private InventoryCheckTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static InventoryCheckTypeEnum getEnum(int value) {
			for (InventoryCheckTypeEnum c : InventoryCheckTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}

	/** 转进货单、销售单状态 **/
	public enum TransferStatusEnum {
		no(0, "未转进货"), part(1, "部分转进货"), finish(2, "已转进货");

		private int value;

		private String name;

		private TransferStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static TransferStatusEnum getEnum(int value) {
			for (TransferStatusEnum c : TransferStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	/** 退货类型 **/
	public enum RejectTypeEnum {
		no(0, "无退货"), part(1, "部分退货"), all(2, "全部退货");

		private int value;

		private String name;

		private RejectTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static RejectTypeEnum getEnum(int value) {
			for (RejectTypeEnum c : RejectTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	/** 消息数据类型 **/
	public enum MsgDataTypeEnum {
		sale_book_order_audit(1, "待审核销售订单"), sale_book_order(2, "销售订单"), sale_order_audit(3, "待审核销售单"), sale_order_warehouse(4, "待出库销售单"),
		purchase_book_order_audit(5, "待审核进货订单"), purchase_book_order(6, "进货订单"), purchase_order_audit(7, "待审核进货单"), purchase_order(8, "进货单"),
		recepit_order_audit(9, "待审核收款单"), pay_order_audit(10, "待审核付款单"), income_order_audit(11, "待审核日常收入单"), expenses_order_audit(12, "待审核日常支出单"),
		purchase_reject_order_audit(13, "进货退货单"), sale_reject_order_audit(14, "销售退货单"), sale_book_order_warning(15, "销售订单交货提醒");

		private int value;

		private String name;

		private MsgDataTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static MsgDataTypeEnum getEnum(int value) {
			for (MsgDataTypeEnum c : MsgDataTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	
	/** 单据来源 **/
	public enum OrderSourceEnum {
		pc(0, "PC管理端"), cash(1, "收银台"), app(2, "APP端"), xcx(3, "小程序商城");

		private int value;

		private String name;

		private OrderSourceEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static OrderSourceEnum getEnum(int value) {
			for (OrderSourceEnum c : OrderSourceEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	
	/** 打印模板单据类型 **/
	public enum PrintTemplateOrderTypeEnum {

		goods_tag(0, "商品标签"),sale_book_order(1, "销售订单"), sale_order(2, "销售单"), sale_reject_order(3, "销售退货单"), purchase_book_order(4, "进货订单"), purchase_order(5, "进货单"), purchase_reject_order(6, "进货退货单"), 
		inventory_swap(7, "调拨单"), inventory_checking(8, "盘点单"), trader_receipt_order(9, "收款单"), trader_pay_order(10, "付款单");

		private int value;

		private String name;

		private PrintTemplateOrderTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static PrintTemplateOrderTypeEnum getEnum(int value) {
			for (PrintTemplateOrderTypeEnum c : PrintTemplateOrderTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	
	/** 打印类型 **/
	public enum PrintModeTypeEnum {

		all(1, "平打"), part(2, "套打");

		private int value;

		private String name;

		private PrintModeTypeEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static PrintModeTypeEnum getEnum(int value) {
			for (PrintModeTypeEnum c : PrintModeTypeEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
	
	/** 导出状态 **/
	public enum ExportStatusEnum {

		ing(1, "导出中"), finish(2, "导出完成"), fail(3, "导出失败");

		private int value;

		private String name;

		private ExportStatusEnum(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static ExportStatusEnum getEnum(int value) {
			for (ExportStatusEnum c : ExportStatusEnum.values()) {
				if (value == c.getValue()) {
					return c;
				}
			}
			return null;
		}
	}
	
}
