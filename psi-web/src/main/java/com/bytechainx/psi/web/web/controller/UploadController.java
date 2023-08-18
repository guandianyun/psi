/**
 * 
 */
package com.bytechainx.psi.web.web.controller;

import com.bytechainx.psi.common.kit.FileKit;
import com.bytechainx.psi.web.config.AppConfig;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.core.Path;
import com.jfinal.kit.Ret;
import com.jfinal.upload.UploadFile;

/**
 * 上传文件
 * @author defier
 */
@Path("/upload")
public class UploadController extends BaseController {

	public void index() {
		UploadFile uploadFile = getFile();
		if(uploadFile == null) {
			renderJson(Ret.fail("上传文件不存在"));
			return;
		}	
		Ret ret = FileKit.uploadImage(uploadFile, AppConfig.resourceUploadPath);
		//return Ret.ok().set("filePath", filePath).set("thumbPath", thumbPath);
		renderJson(ret);
	}
	
}
