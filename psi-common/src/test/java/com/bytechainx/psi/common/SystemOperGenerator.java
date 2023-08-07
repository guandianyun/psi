/**
 * 
 */
package com.bytechainx.psi.common;

import java.io.File;
import java.io.IOException;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;

import com.bytechainx.psi.common.EnumConstant.OperTypeEnum;
import com.bytechainx.psi.common.model.SystemOper;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * 权限生成器,新增的权限会生成SQL到文件
 * 
 * @author defier
 *
 */
public class SystemOperGenerator {

	public static DataSource getDataSource() {
		CommonConfig config = new CommonConfig();
		config.useFirstFound("app-config-pro.txt", "app-config-dev.txt");

		DruidPlugin druidPlugin = config.getDruidPlugin();
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.addMapping("system_oper", SystemOper.class);
		
		druidPlugin.start();
		arp.start();
		return druidPlugin.getDataSource();
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// base  所使用的包名
		String basePackageName = "com.bytechainx.psi.common";
		// base  文件保存路径
		String baseOutputDir = System.getProperty("user.dir") + "/src/test/java/" + basePackageName.replace('.', '/');
		System.out.println("输出路径：" + baseOutputDir);
		//  文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String OutputDir = baseOutputDir + "/system-oper.sql";
		
		getDataSource();
		
		StringBuffer sb = new StringBuffer();
		Permissions[] permissions = Permissions.values();
		for (Permissions p : permissions) {
			System.out.println("正在处理:"+p.name()+","+p.getName());
			SystemOper oper = SystemOper.dao.findFirst("select * from system_oper where oper_code = ?", p.name());
			if(oper != null) {
				continue;
			}
			oper = new SystemOper();
			oper.setModuleCode(p.getModule().name());
			oper.setOperCode(p.name());
			oper.setOperName(p.getName());
			if(p.name().startsWith(Permissions.sensitiveData.name())) { // 敏感数据
				oper.setOperType(OperTypeEnum.data.getValue());
			} else {
				oper.setOperType(OperTypeEnum.feature.getValue());
			}
			if(p.name().indexOf("_") < 0) {
				oper.setParentId(0);
				
			} else {
				String parentCode = p.name().substring(0, p.name().lastIndexOf("_"));
				Permissions parent = Permissions.getEnum(parentCode);
				SystemOper parentOper = SystemOper.dao.findFirst("select * from system_oper where oper_code = ?", parent.name());
				oper.setParentId(parentOper.getId());
			}
			oper.save();
			
			sb.append(String.format("insert into system_oper (id, oper_type, oper_name, oper_code, module_code, parent_id) values (%s,%s,'%s','%s','%s',%s);", oper.getId(),oper.getOperType(), oper.getOperName(), oper.getOperCode(), oper.getModuleCode(), oper.getParentId()));
			sb.append("\r\n");
		}
		FileUtils.write(new File(OutputDir), sb);
	}

}
