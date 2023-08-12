/**
 * 
 */
package com.bytechainx.psi.common;

/**
 * 操作权限常量
 * @author defier
 *
 */
public enum Permissions {
	
	sale("销售", Modules.core), 
	inventory("库存", Modules.core), 
	fund("资金", Modules.core), 
	goods("商品", Modules.core), 
	setting("设置", Modules.core), 
	appstore("应用", Modules.core),
	cash("收银台", Modules.core),
	sensitiveData("敏感数据", Modules.core),
	
	
	// -----------------------销售-------------------------------------//
	sale_sale("销售", Modules.core),
	sale_sale_order("销售单", Modules.core),
	sale_sale_rejectOrder("销售退货单", Modules.core),
	
	// 销售单权限点
	sale_sale_order_show("查看", Modules.core), // 销售单-查看
	sale_sale_order_create("新增", Modules.core), // 销售单-新增
	sale_sale_order_update("修改", Modules.core), // 销售单-修改
	sale_sale_order_audit("审核", Modules.core), // 销售单-审核
	sale_sale_order_disable("作废", Modules.core), // 销售单-作废
	sale_sale_order_export("导出", Modules.core), // 销售单-导出
	
	// 销售退货单权限点
	sale_sale_rejectOrder_show("查看", Modules.core), // 销售退货单-查看
	sale_sale_rejectOrder_create("新增", Modules.core), // 销售退货单-新增
	sale_sale_rejectOrder_update("修改", Modules.core), // 销售退货单-修改
	sale_sale_rejectOrder_audit("审核", Modules.core), // 销售退货单-审核
	sale_sale_rejectOrder_disable("作废", Modules.core), // 销售退货单-作废
	sale_sale_rejectOrder_export("导出", Modules.core), // 销售退货单-导出
	
	sale_customer("客户", Modules.core),
	sale_customer_category("客户分类", Modules.core),
	sale_customer_info("客户管理", Modules.core),
	sale_customer_priceLevel("价格等级", Modules.core),
	
	// 客户分类权限点
	sale_customer_category_show("查看", Modules.core), // 客户分类-查看
	sale_customer_category_create("新增", Modules.core), // 客户分类-新增
	sale_customer_category_update("修改", Modules.core), // 客户分类-修改
	sale_customer_category_delete("删除", Modules.core), // 客户分类-删除
	
	// 客户管理权限点
	sale_customer_info_show("查看", Modules.core), // 客户管理-查看
	sale_customer_info_create("新增", Modules.core), // 客户管理-新增
	sale_customer_info_update("修改", Modules.core), // 客户管理-修改
	sale_customer_info_delete("删除", Modules.core), // 客户管理-删除
	sale_customer_info_disable("停用", Modules.core), // 客户管理-停用
	sale_customer_info_updateSaleman("修改所属业务员", Modules.core), // 客户管理-修改所属业务员
	sale_customer_info_updateStore("修改所属门店", Modules.core), // 客户管理-修改所属门店
	sale_customer_info_createImport("导入", Modules.core), // 客户管理-导入
	
	// 价格等级权限点
	sale_customer_priceLevel_show("查看", Modules.core), // 价格等级-查看
	sale_customer_priceLevel_create("新增", Modules.core), // 价格等级-新增
	sale_customer_priceLevel_update("修改", Modules.core), // 价格等级-修改
	sale_customer_priceLevel_delete("删除", Modules.core), // 价格等级-删除
	sale_customer_priceLevel_disable("停用", Modules.core), // 价格等级-停用
	
	// 营销
	sale_market("营销", Modules.core),
	
	// 销售统计
	sale_stat("统计", Modules.core), 
	sale_stat_sale("销售统计", Modules.core),
	sale_stat_hotSale("热销分析", Modules.core),
	sale_stat_storeSale("门店统计", Modules.core),
	
	sale_stat_sale_show("查看", Modules.core), // 销售统计
	sale_stat_hotSale_show("查看", Modules.core), // 热销分析
	sale_stat_storeSale_show("查看", Modules.core), // 门店统计
	
	
	// -------------------------库存-----------------------------------//
	inventory_purchase("进货", Modules.core),
	inventory_purchase_order("进货单", Modules.core),
	inventory_purchase_rejectOrder("进货退货单", Modules.core),
	
	// 进货单权限点
	inventory_purchase_order_show("查看", Modules.core), // 进货单-查看
	inventory_purchase_order_create("新增", Modules.core), // 进货单-新增
	inventory_purchase_order_update("修改", Modules.core), // 进货单-修改
	inventory_purchase_order_audit("审核", Modules.core), // 进货单-审核
	inventory_purchase_order_disable("作废", Modules.core), // 进货单-作废
	inventory_purchase_order_export("导出", Modules.core), // 进货单-导出

	// 进货退货单权限点
	inventory_purchase_rejectOrder_show("查看", Modules.core), // 进货退货单-查看
	inventory_purchase_rejectOrder_create("新增", Modules.core), // 进货退货单-新增
	inventory_purchase_rejectOrder_update("修改", Modules.core), // 进货退货单-修改
	inventory_purchase_rejectOrder_audit("审核", Modules.core), // 进货退货单-审核
	inventory_purchase_rejectOrder_disable("作废", Modules.core), // 进货退货单-作废
	inventory_purchase_rejectOrder_export("导出", Modules.core), // 进货退货单-导出
	
	
	inventory_supplier("供应商", Modules.core),
	inventory_supplier_category("供应商分类", Modules.core),
	inventory_supplier_info("供应商管理", Modules.core),
	
	// 供应商分类权限点
	inventory_supplier_category_show("查看", Modules.core), // 供应商分类-查看
	inventory_supplier_category_create("新增", Modules.core), // 供应商分类-新增
	inventory_supplier_category_update("修改", Modules.core), // 供应商分类-修改
	inventory_supplier_category_delete("删除", Modules.core), // 供应商分类-删除

	// 供应商管理权限点
	inventory_supplier_info_show("查看", Modules.core), // 供应商管理-查看
	inventory_supplier_info_create("新增", Modules.core), // 供应商管理-新增
	inventory_supplier_info_update("修改", Modules.core), // 供应商管理-修改
	inventory_supplier_info_delete("删除", Modules.core), // 供应商管理-删除
	inventory_supplier_info_disable("停用", Modules.core), // 供应商管理-停用
	inventory_supplier_info_createImport("导入", Modules.core), // 供应商管理-导入
	
	inventory_stock("库存", Modules.core),
	inventory_stock_info("库存查询", Modules.core),
	inventory_stock_checking("盘点", Modules.core),
	inventory_stock_swap("调拨", Modules.core),
	inventory_stock_warehouse("仓库管理", Modules.core),
	
	// 库存查询权限点
	inventory_stock_info_show("查看", Modules.core), // 库存查询-查看
	// 库存预警权限点
	inventory_stock_info_warning("库存预警", Modules.core), // 库存查询-预警查看、补货操作
	
	// 库存盘点权限点
	inventory_stock_checking_show("查看", Modules.core), // 库存盘点-查看
	inventory_stock_checking_create("新增", Modules.core), // 库存盘点-新增
	inventory_stock_checking_update("修改", Modules.core), // 库存盘点-修改
	inventory_stock_checking_audit("审核", Modules.core), // 库存盘点-审核
	inventory_stock_checking_disable("作废", Modules.core), // 库存盘点-作废
	
	// 库存调拨权限点
	inventory_stock_swap_show("查看", Modules.core), // 库存调拨-查看
	inventory_stock_swap_create("新增", Modules.core), // 库存调拨-新增
	inventory_stock_swap_update("修改", Modules.core), // 库存调拨-修改
	inventory_stock_swap_audit("审核", Modules.core), // 库存调拨-审核
	inventory_stock_swap_disable("作废", Modules.core), // 库存调拨-作废
	
	// 仓库管理权限点
	inventory_stock_warehouse_show("查看", Modules.core), // 仓库管理-查看
	inventory_stock_warehouse_create("新增", Modules.core), // 仓库管理-新增
	inventory_stock_warehouse_update("修改", Modules.core), // 仓库管理-修改
	inventory_stock_warehouse_delete("删除", Modules.core), // 仓库管理-删除
	inventory_stock_warehouse_disable("删除", Modules.core), // 仓库管理-停用
	inventory_stock_warehouse_unlock("仓库解锁", Modules.core), // 仓库管理-仓库解锁
	
	// 库存统计
	inventory_stat("统计", Modules.core), 
	inventory_stat_purchase("进货统计", Modules.core),
	inventory_stat_purchaseGoods("商品分析", Modules.core),
//	inventory_stat_stock("库存统计", Modules.core),
	
	inventory_stat_purchase_show("查看", Modules.core), // 进货统计
	inventory_stat_purchaseGoods_show("查看", Modules.core), // 商品分析
//	inventory_stat_stock_show("查看", Modules.core), // 库存统计
	
	// -------------------------资金-----------------------------------//
	fund_book("往来账", Modules.core),
	fund_book_receiptOrder("收款单", Modules.core),
	fund_book_payOrder("付款单", Modules.core),
	fund_book_customerBill("客户对账", Modules.core),
	fund_book_supplierBill("供应商对账", Modules.core),
	
	// 收款单权限点
	fund_book_receiptOrder_show("查看", Modules.core), // 收款单-查看
	fund_book_receiptOrder_create("新增", Modules.core), // 收款单-新增
	fund_book_receiptOrder_update("修改", Modules.core), // 收款单-修改
	fund_book_receiptOrder_audit("审核", Modules.core), // 收款单-审核
	fund_book_receiptOrder_disable("作废", Modules.core), // 收款单-作废
	fund_book_receiptOrder_export("导出", Modules.core), // 收款单-导出
	
	// 付款单权限点
	fund_book_payOrder_show("查看", Modules.core), // 付款单-查看
	fund_book_payOrder_create("新增", Modules.core), // 付款单-新增
	fund_book_payOrder_update("修改", Modules.core), // 付款单-修改
	fund_book_payOrder_audit("审核", Modules.core), // 付款单-审核
	fund_book_payOrder_disable("作废", Modules.core), // 付款单-作废
	fund_book_payOrder_export("导出", Modules.core), // 付款单-导出
	
	fund_book_customerBill_show("查看", Modules.core), // 客户对账-查看
	fund_book_customerBill_openBalance("查看", Modules.core), // 客户对账-期初调整
	fund_book_customerBill_export("导出", Modules.core), // 客户对账-导出
	
	fund_book_supplierBill_show("查看", Modules.core), // 供应商对账-查看
	fund_book_supplierBill_openBalance("查看", Modules.core), // 供应商对账-期初调整
	fund_book_supplierBill_export("导出", Modules.core), // 供应商对账-导出
	
	fund_flow("日常收支", Modules.core),
	fund_flow_income("收入单据", Modules.core),
	fund_flow_expenses("支出单据", Modules.core),
	
	// 收入单据权限点
	fund_flow_income_show("查看", Modules.core), // 收入单据-查看
	fund_flow_income_create("新增", Modules.core), // 收入单据-新增
	fund_flow_income_update("修改", Modules.core), // 收入单据-修改
	fund_flow_income_audit("审核", Modules.core), // 收入单据-审核
	fund_flow_income_disable("作废", Modules.core), // 收入单据-作废
	fund_flow_income_export("导出", Modules.core), // 收入单据-导出
	
	// 支出单据权限点
	fund_flow_expenses_show("查看", Modules.core), // 支出单据-查看
	fund_flow_expenses_create("新增", Modules.core), // 支出单据-新增
	fund_flow_expenses_update("修改", Modules.core), // 支出单据-修改
	fund_flow_expenses_audit("审核", Modules.core), // 支出单据-审核
	fund_flow_expenses_disable("作废", Modules.core), // 支出单据-作废
	fund_flow_expenses_export("导出", Modules.core), // 支出单据-导出
	
	//结算账户管理
	fund_account("账户", Modules.core),
	fund_account_info("结算帐户", Modules.core),
	fund_account_fundType("收支项目", Modules.core),
	fund_account_transfer("账户互转", Modules.core),

	// 结算帐户权限点
	fund_account_info_show("查看", Modules.core), // 结算帐户-查看
	fund_account_info_create("新增", Modules.core), // 结算帐户-新增
	fund_account_info_update("修改", Modules.core), // 结算帐户-修改
	fund_account_info_delete("删除", Modules.core), // 结算帐户-删除
	fund_account_info_disable("停用", Modules.core), // 结算帐户-停用
	
	// 收支项目权限点
	fund_account_fundType_show("查看", Modules.core), // 收支项目-查看
	fund_account_fundType_create("新增", Modules.core), // 收支项目-新增
	fund_account_fundType_update("修改", Modules.core), // 收支项目-修改
	fund_account_fundType_delete("删除", Modules.core), // 收支项目-删除
	fund_account_fundType_disable("停用", Modules.core), // 收支项目-停用
	

	// 账户互转权限点
	fund_account_transfer_show("查看", Modules.core), // 账户互转-查看
	fund_account_transfer_create("新增", Modules.core), // 账户互转-新增
	fund_account_transfer_update("修改", Modules.core), // 账户互转-修改
	fund_account_transfer_disable("作废", Modules.core), // 账户互转-作废
	
	// 资金统计
	fund_stat("统计", Modules.core), 
	fund_stat_fundOrder("资金流水", Modules.core), 
	fund_stat_profit("经营利润", Modules.core),

	fund_stat_fundOrder_show("查看", Modules.core), // 资金流水-查看
	fund_stat_profit_show("查看", Modules.core), // 经营利润-查看
	

	// -------------------------商品-----------------------------------//
	goods_goods("商品", Modules.core), 
	goods_goods_info("商品管理", Modules.core),
	goods_goods_category("商品分类", Modules.core),
	goods_goods_spec("规格管理", Modules.core),
	goods_goods_unit("单位管理", Modules.core),
	goods_goods_attribute("商品属性", Modules.core),
	goods_goods_printTag("打印条码", Modules.core),
	
	// 商品管理权限点
	goods_goods_info_show("查看", Modules.core), // 商品管理-查看
	goods_goods_info_create("新增", Modules.core), // 商品管理-新增
	goods_goods_info_update("修改", Modules.core), // 商品管理-修改基本信息
	goods_goods_info_updatePrice("修改价格", Modules.core), // 商品管理-修改价格
	goods_goods_info_updateStockConfig("修改库存预警", Modules.core), // 商品管理-修改库存预警配置
	goods_goods_info_delete("删除", Modules.core), // 商品管理-删除
	goods_goods_info_disable("停用", Modules.core), // 商品管理-停用
	goods_goods_info_printBarcode("打印条码", Modules.core), // 商品管理-打印条码
	goods_goods_info_createImport("导入", Modules.core), // 商品管理-导入
	
	// 商品分类权限点
	goods_goods_category_show("查看", Modules.core), // 商品分类-查看
	goods_goods_category_create("新增", Modules.core), // 商品分类-新增
	goods_goods_category_update("修改", Modules.core), // 商品分类-修改
	goods_goods_category_delete("删除", Modules.core), // 商品分类-删除
	
	// 规格管理权限点
	goods_goods_spec_show("查看", Modules.core), // 规格管理-查看
	goods_goods_spec_create("新增", Modules.core), // 规格管理-新增
	goods_goods_spec_update("修改", Modules.core), // 规格管理-修改
	goods_goods_spec_delete("删除", Modules.core), // 规格管理-删除
	goods_goods_spec_disable("停用", Modules.core), // 规格管理-停用
	
	// 单位管理权限点
	goods_goods_unit_show("查看", Modules.core), // 单位管理-查看
	goods_goods_unit_create("新增", Modules.core), // 单位管理-新增
	goods_goods_unit_update("修改", Modules.core), // 单位管理-修改
	goods_goods_unit_delete("删除", Modules.core), // 单位管理-删除
	goods_goods_unit_disable("停用", Modules.core), // 单位管理-停用
	
	// 商品属性权限点
	goods_goods_attribute_show("查看", Modules.core), // 商品属性-查看
	goods_goods_attribute_create("新增", Modules.core), // 商品属性-新增
	goods_goods_attribute_update("修改", Modules.core), // 商品属性-修改
	goods_goods_attribute_delete("删除", Modules.core), // 商品属性-删除
	goods_goods_attribute_disable("停用", Modules.core), // 商品属性-停用
	
	// 打印条码
	goods_goods_printTag_show("查看", Modules.core), // 打印条码-查看
	goods_goods_printTag_create("新增", Modules.core), // 打印条码-新增
	goods_goods_printTag_delete("删除", Modules.core), // 打印条码-删除
	
	
	goods_mall("商城管理", Modules.core), 
	goods_mall_goods("商品上架", Modules.core), 
	goods_mall_setting("商城设置", Modules.core), 
	goods_mall_notice("商城公告", Modules.core), 
	goods_mall_user("商城用户", Modules.core),
	goods_mall_xcx("小程序管理", Modules.core),
	
	// 商品上下架管理
	goods_mall_goods_show("查看", Modules.core), // 商品上下架管理-商品查看
	goods_mall_goods_update("描述修改", Modules.core), // 商品上下架管理-商品描述修改
	goods_mall_goods_push("商品上架", Modules.core), // 商品上下架管理-商品上架
	
	// 商城设置
	goods_mall_setting_show("查看", Modules.core), // 商户信息-查看
	goods_mall_setting_update("修改", Modules.core), // 商户信息-修改
	
	// 商城公告
	goods_mall_notice_show("查看", Modules.core), // 商城公告-查看
	goods_mall_notice_update("修改", Modules.core), // 商城公告-修改
	goods_mall_notice_create("新增", Modules.core), // 商城公告-新增
	goods_mall_notice_delete("删除", Modules.core), // 商城公告-新增
	
	// 商城公告
	goods_mall_user_show("查看", Modules.core), // 商城用户-查看

	// 小程序管理
	goods_mall_xcx_show("查看", Modules.core), // 小程序管理-查看
		
	
	// -------------------------设置-----------------------------------//
	setting_tenant("商户管理", Modules.core), 
	setting_tenant_info("商户信息", Modules.core),
	setting_tenant_admin("员工管理", Modules.core),
	setting_tenant_role("角色管理", Modules.core),
	
	// 商户信息权限点
	setting_tenant_info_show("查看", Modules.core), // 商户信息-查看
	setting_tenant_info_update("修改", Modules.core), // 商户信息-修改
	
	// 员工管理权限点
	setting_tenant_admin_show("查看", Modules.core), // 员工管理-查看
	setting_tenant_admin_create("新增", Modules.core), // 员工管理-新增
	setting_tenant_admin_update("修改", Modules.core), // 员工管理-修改
	setting_tenant_admin_disable("停用", Modules.core), // 员工管理-停用

	// 角色管理权限点
	setting_tenant_role_show("查看", Modules.core), // 角色管理-查看
	setting_tenant_role_create("新增", Modules.core), // 角色管理-新增
	setting_tenant_role_update("修改", Modules.core), // 角色管理-修改
	setting_tenant_role_delete("删除", Modules.core), // 角色管理-删除	
	
	setting_config("基础设置", Modules.core), 
	setting_config_system("系统设置", Modules.core), 
	setting_config_print("打印设置", Modules.core), 
	
	// 系统设置权限点
	setting_config_system_show("查看", Modules.core), // 系统设置-查看
	setting_config_system_update("修改", Modules.core), // 系统设置-修改

	// 打印设置权限点
	setting_config_print_tplSetting("打印模板设置", Modules.core), // 打印设置-打印模板设置
	setting_config_print_orderSetting("单据打印设置", Modules.core), // 打印设置-单据打印设置
	setting_config_print_cloudPrint("绑定云打印", Modules.core), // 打印设置-绑定云打印
	setting_config_print_printerSetting("本地打印机设置", Modules.core), // 打印设置-本地打印机设置
	
	setting_system("系统操作", Modules.core), 
	setting_system_reset("系统重置", Modules.core), 
	setting_system_operLog("操作日志", Modules.core),
	
	// 操作日志权限点
	setting_system_operLog_show("查看", Modules.core), // 操作日志-查看
		
		
	// -------------------------应用-----------------------------------//
	appstore_subscribe("订阅应用", Modules.core),
	appstore_subscribe_appstore("应用市场", Modules.core),
	appstore_subscribe_info("我的订阅", Modules.core),
	appstore_subscribe_log("购买记录", Modules.core),
	
	// 应用市场
	appstore_subscribe_appstore_show("查看", Modules.core), // 应用市场-查看
	appstore_subscribe_appstore_create("订购应用", Modules.core), // 应用市场-订购应用
	
	// 我的订阅
	appstore_subscribe_info_show("查看", Modules.core), // 我的订阅-查看
	appstore_subscribe_info_renew("系统续费", Modules.core), // 我的订阅-系统续费
	appstore_subscribe_info_buySms("购买短信", Modules.core), // 我的订阅-购买短信
	appstore_subscribe_info_buyAccount("订购帐户", Modules.core), // 我的订阅-订购帐户
	appstore_subscribe_info_setting("应用设置", Modules.core), // 我的订阅-应用设置
	
	// 购买记录
	appstore_subscribe_log_show("查看", Modules.core), // 购买记录-查看
	
	//----------------------收银台----------------------------------//
	cash_cash("收银台", Modules.core), // 收银台
	
	cash_cash_cashier("收银台", Modules.core), // 收银台-收银
	
	cash_cash_cashier_create("开单收款", Modules.core), // 收银台-收款开单
	cash_cash_cashier_setting("收银设置", Modules.core), // 收银台-设置
	
	// -------------------------敏感数据-----------------------------------//
	sensitiveData_price("价格权限", Modules.core),
	sensitiveData_customer("客户权限", Modules.core),
	sensitiveData_supplier("供应商权限", Modules.core),
	sensitiveData_account("结算账户权限", Modules.core),
	sensitiveData_order("单据权限", Modules.core),
	
	// 价格权限权限点
	sensitiveData_price_purchase("进货价", Modules.core), // 价格权限-进货价
	sensitiveData_price_wholesale("批发价", Modules.core), // 价格权限-批发价
	sensitiveData_price_costProfit("成本价和利润", Modules.core), // 价格权限-成本价和利润
	
	// 客户权限权限点
	sensitiveData_customer_due("查看欠款", Modules.core), // 客户权限-查看欠款
	sensitiveData_customer_mobile("查看手机号", Modules.core), // 客户权限-查看手机号
	sensitiveData_customer_showSaleman("查看其他业务员的客户", Modules.core), // 客户权限-查看其他业务员的客户
	
	// 供应商权限权限点
	sensitiveData_supplier_due("查看欠款", Modules.core), // 供应商权限-查看欠款
	sensitiveData_supplier_mobile("查看手机号", Modules.core), // 供应商权限-查看手机号
	
	sensitiveData_account_stat("资产统计", Modules.core), // 结算账户权限-首页资产统计
	
	// 单据权限点
	sensitiveData_order_showSaleOrder("查看他人销售单据", Modules.core), // 单据权限-查看他人销售单据
	sensitiveData_order_updateSaleOrder("编辑他人销售单据", Modules.core), // 单据权限-编辑他人销售单据
	
	sensitiveData_order_showPurchaseOrder("查看他人进货单据", Modules.core), // 单据权限-查看他人进货单据
	sensitiveData_order_updatePurchaseOrder("编辑他人进货单据", Modules.core), // 单据权限-编辑他人进货单据
	
	sensitiveData_order_showFundOrder("查看他人往来账单据", Modules.core), // 单据权限-查看他人往来账单据
	sensitiveData_order_updateFundOrder("编辑他人往来账单据", Modules.core), // 单据权限-编辑他人往来账单据
	
	sensitiveData_order_showFlowOrder("查看他人日常收支单据", Modules.core), // 单据权限-查看他人日常收支单据
	sensitiveData_order_updateFlowOrder("编辑他人日常收支单据", Modules.core), // 单据权限-编辑他人日常收支单据
	
	sensitiveData_order_showStockOrder("查看他人调拨/盘点单据", Modules.core), // 单据权限-查看他人调拨/盘点单据
	sensitiveData_order_updateStockOrder("编辑他人调拨/盘点单据", Modules.core), // 单据权限-编辑他人调拨/盘点单据
	
	
	
	;
	
	private String name;
	private Modules module;

	private Permissions(String name, Modules module) {
		this.name = name;
		this.module = module;
	}

	public String getName() {
		return name;
	}
	public Modules getModule() {
		return module;
	}
	
	public static Permissions getEnum(String value) {
		for (Permissions c : Permissions.values()) {
			if (value.equalsIgnoreCase(c.name())) {
				return c;
			}
		}
		return null;
	}
	
}
