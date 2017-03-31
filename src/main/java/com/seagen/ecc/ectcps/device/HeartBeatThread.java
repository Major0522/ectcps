package com.seagen.ecc.ectcps.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatThread extends Thread {
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected AttachableDevice attachableDevice;
	protected volatile boolean isRuning = true;

	public HeartBeatThread(AttachableDevice attachableDevice) {
		super();
		this.attachableDevice = attachableDevice;
		setName("HeartBeatThread in device:"
				+ this.attachableDevice.getConnection().getClientConfig()
						.getClientName());
	}

	public AttachableDevice getAttachableDevice() {
		return attachableDevice;
	}

	public void setAttachableDevice(AttachableDevice attachableDevice) {
		this.attachableDevice = attachableDevice;
	}

	public void stopHeartBeat() {
		this.isRuning = false;
		try {
			interrupt();
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		log.info("HeartBeat Thread starting...");
		while (isRuning) {
			try {
				int timeout = attachableDevice.getClientConfig().getTimeout() * 1000;
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {
				}
				if ((System.currentTimeMillis() - attachableDevice
						.getConnection().getLastReceived()) > (timeout * 3)) {
					log.info("3 times not read message,device restart...");
					netAlarm();
					attachableDevice.restart();
				} else {
					attachableDevice.sendHeartBeatPack();
				}
			} catch (Exception e) {
				log.error("HeartBeat error", e);
			}
		}
	}

	/**
	 * 网络故障通知(3次心跳周期后没有收到消息)
	 */
	public void netAlarm() {
		// do nothing
	}
}