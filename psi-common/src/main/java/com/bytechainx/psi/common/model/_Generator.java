package com.bytechainx.psi.common.model;

import javax.sql.DataSource;

import com.bytechainx.psi.common.CommonConfig;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * Model、BaseModel、_MappingKit 生成器
 * 
 * 添加新表，或者修改表结构时重新运行该生成器即可
 */
public class _Generator {

	/**
	 * 重用 JFinalClubConfig 中的数据源配置，避免冗余配置
	 */
	public static DataSource getDataSource() {
		CommonConfig config = new CommonConfig();
		config.useFirstFound("app-config-pro.txt", "app-config-dev.txt");

		DruidPlugin druidPlugin = config.getDruidPlugin();
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}

	public static void main(String[] args) throws Exception {
		// model 所使用的包名 (MappingKit 默认使用的包名)
		String modelPackageName = "com.bytechainx.psi.common.model";

		// base model 所使用的包名
		String baseModelPackageName = modelPackageName + ".base";

		// base model 文件保存路径
		String baseModelOutputDir = System.getProperty("user.dir") + "/src/main/java/" + baseModelPackageName.replace('.', '/');

		System.out.println("输出路径：" + baseModelOutputDir);

		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";

		// 创建生成器
		Generator generator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
		// 配置是否生成备注
		generator.setGenerateRemarks(true);

		// 设置数据库方言
		generator.setDialect(new MysqlDialect());

		// 设置是否生成链式 setter 方法，强烈建议配置成 false，否则 fastjson 反序列化会跳过有返回值的 setter 方法
		generator.setGenerateChainSetter(false);

		// 添加不需要生成的表名
		generator.addExcludedTable("adv");

		// 设置是否在 Model 中生成 dao 对象
		generator.setGenerateDaoInModel(true);

		// 设置是否生成字典文件
		generator.setGenerateDataDictionary(false);

		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非
		// OscUser
		// gernerator.setRemovedTableNamePrefixes("t_");

		// 生成
		generator.generate();
	}
}
