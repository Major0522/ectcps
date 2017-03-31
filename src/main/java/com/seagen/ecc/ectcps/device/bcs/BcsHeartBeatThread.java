package com.seagen.ecc.ectcps.device.bcs;

import com.seagen.ecc.common.CmdConst;
import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.AttachableDevice;
import com.seagen.ecc.ectcps.device.HeartBeatThread;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Protocol;

public class BcsHeartBeatThread extends HeartBeatThread {
	public BcsHeartBeatThread(AttachableDevice attachableDevice) {
		super(attachableDevice);
	}

	/**
	 * 网络故障告警,交由业务层处理.业务层将会收到消息,无需回复.
	 */
	@Override
	public void netAlarm() {
		ClientConfig config = attachableDevice.getConnection()
				.getClientConfig();
		CommandMessage cm = new CommandMessage();
		cm.setCabinetNo(config.getUserId());
		cm.setCommandType(Protocol.CommandType.REPORT);
		cm.setFunctionCode(CmdConst.FUN_CODE_NET_ALARM);
		HandlerProxy.messageReceived(config.getClientName(), cm, null);
	}
}