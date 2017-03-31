package com.seagen.ecc.ectcps.device.mq;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.AttachableDevice;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Protocol.ProtocolType;

public class MqDevice extends AttachableDevice {
	public MqDevice(ClientConfig clientConfig) {
		super(clientConfig);
	}

	@Override
	public Object getHeartBeatPack() {
		CommandMessage cm = new CommandMessage();
		cm.setCabinetNo(this.getConnection().getClientConfig().getUserId());
		cm.setCommandType(5);
		return cm;
	}

	public void start() {
		log.info(clientConfig.getClientName() + ":device start...");
		if (clientConfig.getProtocolType() == ProtocolType.MQ) {
			this.connection = new MqConnection(clientConfig);
		} else {
			this.connection = new DefaultConnection(clientConfig);
		}
		this.setRunning(true);
		this.startMessageOutThread();
		this.sendHeartBeatPack();
	}
}
