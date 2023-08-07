package com.bytechainx.psi.common;

import java.io.File;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;

/**
 * 配置中心
 */
public  class CommonConfig extends JFinalConfig {
	
	public static String dbBackupPath; // 备份数据路径
	public static String pcWebDomain;
	public static String resourceDomain;
	public static String resourceUploadPath; // 上传静态文件的根目录
	public static String resourceUploadDomain; // 上传文件访问网址
	public static String traderApiSecret; // api接口调用私钥
	public static String traderApiDomain; // 交易中心域名
	
	public static String redisIp; // redis ip
	public static int redisPort; // redis port
	public static String redisPassword; // redis pwd
	
	public static String mysqlJdbcUrl; // mysql jdbcUrl
	public static String mysqlUser; // mysql user
	public static String mysqlPassword; // mysql pwd
	
	public static String adapayDeviceId; // 汇付设备ID
	
	public static String adapayMerchantApiMockKey; // 汇付代理商测试环境APIKEY
	public static String adapayMerchantApiKey; // 汇付代理商正式环境APIKEY
	public static String adapayMerchantRsaPrivateKey; // 汇付代理商API私钥
	
	public static String adapayDefaultRsaPrivateKey; // 汇付商户默认统一API私钥
	public static String adapayDefaultRsaPublicKey; // 汇付商户默认统一API公钥
	
	public static String adapaySysApiMockKey; // 管店云系统汇付测试环境APIKEY
	public static String adapaySysApiKey; // 管店云系统汇付正式环境APIKEY
	public static String adapaySysRsaPrivateKey; // 管店云系统汇付API私钥
	public static String adapaySysAppId; // 管店云系统汇付应用ID
	
	public static String smsJunmeiUserId; //  骏媒短信接口ID
	public static String smsJunmeiUserUser; //  骏媒短信接口用户名
	public static String smsJunmeiUserPwd; //  骏媒短信接口密码
	public static String smsSubmailAppid; //  赛邮短信接口app_id
	public static String smsSubmailAppkey; //  赛邮短信接口app_key
	
	public static String lodopCloudScriptUrl1 = "http://127.0.0.1:8000/CLodopfuncs.js";// 云打印机URL
	public static String lodopCloudScriptUrl2 = "http://127.0.0.1:18000/CLodopfuncs.js";// 云打印机URL
	public static String lodopLocalScriptUrl1 = "http://127.0.0.1:8000/CLodopfuncs.js";// 本地打印机URL
	public static String lodopLocalScriptUrl2 = "http://127.0.0.1:18000/CLodopfuncs.js";// 本地打印机URL
	
	public static String commonConfigPath; // 公用配置目录路径

	@Override
	public void configConstant(Constants me) {
		
		loadPropertyFile(new File(commonConfigPath+File.separator+"app-config-dev.txt"));
		
		pcWebDomain = getProperty("pc.web.domain");
		resourceDomain = getProperty("resource.domain");
		resourceUploadPath = getProperty("resource.upload.path");
		resourceUploadDomain = getProperty("resource.upload.domain");
		traderApiDomain = getProperty("trader.api.domain");
		
		dbBackupPath = getProperty("db.backup.path");
		
		mysqlJdbcUrl = getProperty("mysql.jdbcUrl");
		mysqlUser = getProperty("mysql.user");
		mysqlPassword = getProperty("mysql.password");
		
		redisIp = getProperty("redis.ip");
		redisPort = getPropertyToInt("redis.port");
		redisPassword = getProperty("redis.password");
		
		traderApiDomain = getProperty("trader.api.domain");
		traderApiSecret = getProperty("trader.api.secret");
		
		adapayDeviceId = getProperty("adapay.device.id"); // 汇付设备ID
		adapayMerchantApiMockKey = getProperty("adapay.merchant.api.mock.key"); // 汇付代理商测试环境APIKEY
		adapayMerchantApiKey = getProperty("adapay.merchant.api.key"); // 汇付代理商正式环境APIKEY
		adapayMerchantRsaPrivateKey = getProperty("adapay.merchant.rsa.private.Key"); // 汇付代理商API私钥
		
		adapayDefaultRsaPrivateKey = getProperty("adapay.default.rsa.private.Key"); // 汇付商户默认统一API私钥
		adapayDefaultRsaPublicKey = getProperty("adapay.default.rsa.public.Key"); // 汇付商户默认统一API公钥
		
		adapaySysApiMockKey = getProperty("adapay.sys.api.mock.key"); // 管店云系统汇付测试环境APIKEY
		adapaySysApiKey = getProperty("adapay.sys.api.key"); // 管店云系统汇付正式环境APIKEY
		adapaySysRsaPrivateKey = getProperty("adapay.sys.rsa.private.Key"); // 管店云系统汇付API私钥
		adapaySysAppId = getProperty("adapay.sys.app.id"); // 管店云系统汇付应用ID
		
		lodopCloudScriptUrl1 = getProperty("lodop.cloud.script.url1");
		lodopCloudScriptUrl2 = getProperty("lodop.cloud.script.url2");
		lodopLocalScriptUrl1 = getProperty("lodop.local.script.url1");
		lodopLocalScriptUrl2 = getProperty("lodop.local.script.url2");
		
		smsJunmeiUserId = getProperty("sms.junmei.user.id"); //  骏媒短信接口用户ID
		smsJunmeiUserUser = getProperty("sms.junmei.user.user"); //  骏媒短信接口用户名
		smsJunmeiUserPwd = getProperty("sms.junmei.user.pwd"); //  骏媒短信接口密码
		
		smsSubmailAppid = getProperty("sms.submail.app.id"); //  赛邮短信接口app_id
		smsSubmailAppkey = getProperty("sms.submail.app.key"); //  赛邮短信接口app_key
		
	}

	@Override
	public void configRoute(Routes me) {
		
	}

	@Override
	public void configEngine(Engine me) {
		// 以下配置支持静态方法调用表达式：com.jfinal.kit.StrKit::isBlank('abc')
		me.setStaticMethodExpression(true);
		// 以下配置支持静态属性访问表达式：com.jfinal.core.Constant::JFINAL_VERSION
		me.setStaticFieldExpression(true);
	}

	@Override
	public void configPlugin(Plugins me) {
		
	}

	@Override
	public void configInterceptor(Interceptors me) {
		
	}

	@Override
	public void configHandler(Handlers me) {
		
	}
	
	/**
	 * 抽取成独立的方法，便于 _Generator 中重用该方法，减少代码冗余
	 */
	public DruidPlugin getDruidPlugin() {
		return new DruidPlugin(getProperty("mysql.jdbcUrl"), getProperty("mysql.user"), getProperty("mysql.password").trim());
	}
	
	
}





