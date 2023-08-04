package com.bytechainx.psi.common.model;

import com.bytechainx.psi.common.EnumConstant.MsgTypeEnum;
import com.bytechainx.psi.common.model.base.BaseMsgNotice;

/**
 * 通知消息
 */
@SuppressWarnings("serial")
public class MsgNotice extends BaseMsgNotice<MsgNotice> {
	
	public static final MsgNotice dao = new MsgNotice().dao();

	public String getMsgTypeName() {
		return MsgTypeEnum.getEnum(getMsgType()).getName();
	}
}

