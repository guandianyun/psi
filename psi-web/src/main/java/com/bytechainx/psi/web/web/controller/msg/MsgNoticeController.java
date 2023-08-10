/**
 * 
 */
package com.bytechainx.psi.web.web.controller.msg;

import java.util.Date;

import com.bytechainx.psi.common.EnumConstant.FlagEnum;
import com.bytechainx.psi.common.model.MsgNoticeSend;
import com.bytechainx.psi.common.service.msg.MsgNoticeService;
import com.bytechainx.psi.web.web.controller.base.BaseController;
import com.jfinal.aop.Inject;
import com.jfinal.core.Path;
import com.jfinal.plugin.activerecord.Page;

/**
 * 消息通知
 * @author defier
 */
@Path("/msg/notice")
public class MsgNoticeController extends BaseController {

	@Inject
	private MsgNoticeService msgNoticeService;
	
	public void index() {

	}
	
	public void list() {
		pageSize = 10;
		int pageNumber = getInt("pageNumber", 1);
		Boolean readFlag = getBoolean("read_flag");
		Page<MsgNoticeSend> page = msgNoticeService.paginate(getAdminId(), readFlag, pageNumber, pageSize);
		setAttr("page", page);
	}
	

	public void show() {
		Integer id = getInt("id");
		MsgNoticeSend msg = MsgNoticeSend.dao.findBy(id);
		if(msg == null) {
			renderError(404);
			return;
		}
		msg.setReadFlag(FlagEnum.YES.getValue());
		msg.setReadTime(new Date());
		msg.update();
		
		setAttr("msg", msg);
	}
}
