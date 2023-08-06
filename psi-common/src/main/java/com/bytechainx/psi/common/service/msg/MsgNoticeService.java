/**
 * 
 */
package com.bytechainx.psi.common.service.msg;

import java.util.ArrayList;
import java.util.List;

import com.bytechainx.psi.common.model.MsgNoticeSend;
import com.bytechainx.psi.common.service.base.CommonService;
import com.jfinal.plugin.activerecord.Page;

/**
 * 消息通知
 * @author defier
 *
 */
public class MsgNoticeService extends CommonService {
	
	public Page<MsgNoticeSend> paginate(Integer receiverId, Boolean readFlag, int pageNumber, int pageSize) {
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.append("where 1 = 1 ");
		if(receiverId != null && receiverId > 0) {
			where.append(" and receiver_id = ?");
			params.add(receiverId);
		}
		if(readFlag != null) {
			where.append(" and read_flag = ?");
			params.add(readFlag);
		}
		return MsgNoticeSend.dao.paginate(pageNumber, pageSize, "select * ", "from msg_notice_send "+where.toString()+" order by id desc", params.toArray());
	}
	
	
}
