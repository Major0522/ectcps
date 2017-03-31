package com.seagen.ecc.ectcps.device.mcs;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.AttachableDevice;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.protocol.McsMessage;
import com.seagen.ecc.ectcps.protocol.Protocol;

/**
 * 与模块控制系统的通信设备
 */
public class McsDevice extends AttachableDevice {

	public McsDevice(ClientConfig clientConfig) {
		super(clientConfig);
	}

	@Override
	public Object getHeartBeatPack() {
		McsMessage cm = new McsMessage();
		cm.setCabinetNo(this.getConnection().getClientConfig().getUserId());
		cm.setCommandType(5);
		cm.setFunctionCode(2);
		cm.setModuleName("Program");
		return cm;
	}

	/**
	 * 等待一定时间才允许发送下一个命令,防止命令发送过快导致硬件程序处理不过来
	 */
	@Override
	public synchronized void addOutMessage(Object message) {
		super.addOutMessage(message);
		try {
			Thread.sleep(80);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void start() {
		log.info(clientConfig.getClientName() + ":device start...");
		this.connection = new McsConnection(clientConfig, this);
		this.setRunning(true);
		this.startMessageOutThread();
		this.sendHeartBeatPack();
	}

	/**
	 * 通知客户端,重启设备
	 */
	@Override
	public void restart() {
		McsMessage cm = new McsMessage();
		cm.setCabinetNo(this.getConnection().getClientConfig().getUserId());
		cm.setCommandType(Protocol.CommandType.REPORT);
		cm.setFunctionCode(4);// 重启
		cm.setModuleName("Program");
		HandlerProxy.messageReceived(connection.getClientConfig()
				.getClientName(), cm, null);
		super.restart();
	}

	@Override
	public void close() {
		clearMessage();
		super.close();
	}

	public void clearMessage() {
		outQueue.clear();
	}
}
