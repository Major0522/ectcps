package com.seagen.ecc.ectcps.device.bcs;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.AttachableDevice;
import com.seagen.ecc.ectcps.protocol.CommandMessage;

public class BcsDevice extends AttachableDevice {
	public BcsDevice(ClientConfig clientConfig) {
		super(clientConfig);
	}

	@Override
	public Object getHeartBeatPack() {
		CommandMessage cm = new CommandMessage();
		cm.setCabinetNo(this.getConnection().getClientConfig().getUserId());
		cm.setCommandType(5);
		return cm;
	}

	@Override
	protected void startHeartBeatThread() {
		if (heartBeatThread != null) {
			heartBeatThread.stopHeartBeat();
		}
		heartBeatThread = new BcsHeartBeatThread(this);
		heartBeatThread.start();
	}
}
