
package com.bytechainx.psi.web.config;

import com.bytechainx.psi.common.CommonConfig;
import com.bytechainx.psi.common.EnumConstant.AuditStatusEnum;
import com.bytechainx.psi.common.EnumConstant.CommissionApplyTypeEnum;
import com.bytechainx.psi.common.EnumConstant.CommissionRuleTypeEnum;
import com.bytechainx.psi.common.EnumConstant.CommissionTypeEnum;
import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.EnumConstant.DiscountTypeEnum;
import com.bytechainx.psi.common.EnumConstant.ExportStatusEnum;
import com.bytechainx.psi.common.EnumConstant.FeePayAccountEnum;
import com.bytechainx.psi.common.EnumConstant.FundFlowEnum;
import com.bytechainx.psi.common.EnumConstant.FundTypeEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsAttrTypeEnum;
import com.bytechainx.psi.common.EnumConstant.GoodsTypeEnum;
import com.bytechainx.psi.common.EnumConstant.IndustryEnum;
import com.bytechainx.psi.common.EnumConstant.InventoryCheckTypeEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusEnum;
import com.bytechainx.psi.common.EnumConstant.LogisticsStatusInEnum;
import com.bytechainx.psi.common.EnumConstant.MsgDataTypeEnum;
import com.bytechainx.psi.common.EnumConstant.MsgLevelEnum;
import com.bytechainx.psi.common.EnumConstant.OnlinePayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OperLogTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderCodeRuleTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderOddTypeEnum;
import com.bytechainx.psi.common.EnumConstant.OrderPayStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderSourceEnum;
import com.bytechainx.psi.common.EnumConstant.OrderStatusEnum;
import com.bytechainx.psi.common.EnumConstant.OrderUnitTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PlatformTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PrintModeTypeEnum;
import com.bytechainx.psi.common.EnumConstant.PrintTemplateOrderTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectDealTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectReasonTypeEnum;
import com.bytechainx.psi.common.EnumConstant.RejectTypeEnum;
import com.bytechainx.psi.common.EnumConstant.SaleModeEnum;
import com.bytechainx.psi.common.EnumConstant.SalePayTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockConfigWarnTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockIoTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StockWarnTypeEnum;
import com.bytechainx.psi.common.EnumConstant.StoreTypeEnum;
import com.bytechainx.psi.common.EnumConstant.TenantConfigEnum;
import com.bytechainx.psi.common.EnumConstant.TransferStatusEnum;
import com.bytechainx.psi.common.EnumConstant.UserActiveStatusEnum;
import com.bytechainx.psi.common.Modules;
import com.bytechainx.psi.common.Permissions;
import com.bytechainx.psi.common.kit.DateUtil;
import com.bytechainx.psi.common.kit.StrUtil;
import com.bytechainx.psi.common.model._MappingKit;
import com.bytechainx.psi.common.plugin.QuartzPlugin;
import com.bytechainx.psi.web.web.interceptor.OperLogInterceptor;
import com.bytechainx.psi.web.web.interceptor.PermissionInterceptor;
import com.bytechainx.psi.web.web.interceptor.ViewContextInterceptor;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.template.Engine;

/**
 * 配置中心
 */
public class AppConfig extends CommonConfig {
	
	public static Boolean devMode = false;  // 是否开发模式
	public static Boolean engineDevMode = false;  // 支持模板文件热加载
	public static boolean staratup = false; // 是否启动完成，所有的task要在启动完成后才能执行
	public static String version; // 版本号
	
	public static QuartzPlugin quartzJobs;
	public static Boolean mockMode = false; // 模拟开发模式，一些权限无需验证，模拟数据
	
	public static String baseViewPath = "/WEB-INF/views";
	public static String noPermissionViews =  baseViewPath+"/common/_nopermission.html"; // 没有权限展示页面
	public static String noLoginViews =  baseViewPath+"/common/_noLogin.html"; // 登录超时展示页面
	
	
	public void configConstant(Constants me) {
		useFirstFound("app-config-pro.txt", "app-config-dev.txt");
		devMode = getPropertyToBoolean("devMode", false);
		engineDevMode = getPropertyToBoolean("engineDevMode", false);
		me.setDevMode(devMode);
		mockMode = getPropertyToBoolean("mockMode", false);
		version = getProperty("version");
		commonConfigPath = getProperty("common.config.path");
		
		super.configConstant(me);
		
		// 支持依赖注入
		me.setInjectDependency(true);
		// 不对父类进行注入，提升注入性能
		me.setInjectSuperClass(false);
		
		me.setError404View(baseViewPath+"/404.html");
		me.setError500View(baseViewPath+"/500.html");
	}
	
	public void configRoute(Routes me) {
		me.setBaseViewPath(baseViewPath);
		me.scan("com.bytechainx.psi.");
	}
	
	public void configEngine(Engine me) {
		// devMode 为 true 时支持模板文件热加载
		me.setDevMode(engineDevMode);
		me.addSharedFunction(baseViewPath+"/common/_paginate.html");
		me.addSharedFunction(baseViewPath+"/common/_commonMethod.html");
		me.addSharedObject("StrUtil", new StrUtil());
		me.addSharedObject("DateUtil", new DateUtil());
		me.addSharedObject("resourceUploadDomain", resourceUploadDomain);
		me.addSharedObject("resourceDomain", resourceDomain);
		me.addSharedObject("pcWebDomain", pcWebDomain);
		me.addSharedObject("lodopCloudScriptUrl1", lodopCloudScriptUrl1);
		me.addSharedObject("lodopCloudScriptUrl2", lodopCloudScriptUrl2);
		me.addSharedObject("lodopLocalScriptUrl1", lodopLocalScriptUrl1);
		me.addSharedObject("lodopLocalScriptUrl2", lodopLocalScriptUrl2);
		me.addEnum(UserActiveStatusEnum.class);
		me.addEnum(Modules.class);
		me.addEnum(Permissions.class);
		me.addEnum(StoreTypeEnum.class);
		me.addEnum(DataStatusEnum.class);
		me.addEnum(SaleModeEnum.class);
		me.addEnum(IndustryEnum.class);
		me.addEnum(OperLogTypeEnum.class);
		me.addEnum(PlatformTypeEnum.class);
		me.addEnum(FundFlowEnum.class);
		me.addEnum(OrderStatusEnum.class);
		me.addEnum(FeePayAccountEnum.class);
		me.addEnum(GoodsAttrTypeEnum.class);
		me.addEnum(StockConfigWarnTypeEnum.class);
		me.addEnum(InventoryCheckTypeEnum.class);
		me.addEnum(AuditStatusEnum.class);
		me.addEnum(StockWarnTypeEnum.class);
		me.addEnum(OrderPayStatusEnum.class);
		me.addEnum(FundTypeEnum.class);
		me.addEnum(TransferStatusEnum.class);
		me.addEnum(RejectTypeEnum.class);
		me.addEnum(RejectReasonTypeEnum.class);
		me.addEnum(RejectDealTypeEnum.class);
		me.addEnum(LogisticsStatusInEnum.class);
		me.addEnum(LogisticsStatusEnum.class);
		me.addEnum(StockIoTypeEnum.class);
		me.addEnum(CommissionTypeEnum.class);
		me.addEnum(CommissionRuleTypeEnum.class);
		me.addEnum(CommissionApplyTypeEnum.class);
		me.addEnum(GoodsTypeEnum.class);
		me.addEnum(OrderUnitTypeEnum.class);
		me.addEnum(SalePayTypeEnum.class);
		me.addEnum(OrderCodeRuleTypeEnum.class);
		me.addEnum(MsgDataTypeEnum.class);
		me.addEnum(MsgLevelEnum.class);
		me.addEnum(DiscountTypeEnum.class);
		me.addEnum(OrderOddTypeEnum.class);
		me.addEnum(TenantConfigEnum.class);
		me.addEnum(OrderSourceEnum.class);
		me.addEnum(PrintTemplateOrderTypeEnum.class);
		me.addEnum(PrintModeTypeEnum.class);
		me.addEnum(ExportStatusEnum.class);
		me.addEnum(OnlinePayStatusEnum.class);
	}
	
	/**
	 * 抽取成独立的方法，便于 _Generator 中重用该方法，减少代码冗余
	 */
	public DruidPlugin getDruidPlugin() {
		return new DruidPlugin(mysqlJdbcUrl, mysqlUser, mysqlPassword);
	}
	
	public void configPlugin(Plugins me) {
		
		me.add(new EhCachePlugin());
		
		RedisPlugin redis = new RedisPlugin("psi", redisIp, redisPort, redisPassword);
	    me.add(redis);
		
		// 配置 JDBC 连接池插件
		DruidPlugin druidPlugin = getDruidPlugin();
		me.add(druidPlugin);
		
		// 配置 ActiveRecordPlugin
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.setShowSql(devMode);	// 是否输出 sql 到控制台
		_MappingKit.mapping(arp);	// 自动添加 model 到 table 的映射
		me.add(arp);
		
		//定时任务
		quartzJobs = new QuartzPlugin();
//		quartzJobs.add("0 0/10 * * * ?", new WithdrawJob()); // 每隔10分钟执行一次
//		quartzJobs.add("*/3 * * * * ?", new UrlschemeGeneratorJob()); // 每隔3秒钟执行一次
//		quartzJobs.add("0 0 10 * * ?", new ServiceExpireJob());  // 每天上午10点执行一次
//		
		me.add(quartzJobs);
		
	}
	
	public void configInterceptor(Interceptors me) {
		 me.addGlobalActionInterceptor(new ViewContextInterceptor());
		 me.addGlobalActionInterceptor(new PermissionInterceptor());
		 me.addGlobalActionInterceptor(new OperLogInterceptor());
	}
	
	public void configHandler(Handlers me) {
		me.add(new UrlSkipHandler("^/resources/(.*?)", false));
	}
	
	// 服务启动时回调 onStart()
	public void onStart() {
		staratup = true;
	}

	// 服务关闭时回调 onStop()
	public void onStop() {
		
	}
}





