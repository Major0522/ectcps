package com.seagen.ecc.ectcps.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息发送线程,从队列中获取消息并且完成发送
 * 
 */
public class MessageOutThread extends Thread {
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected AttachableDevice attachableDevice;
	protected volatile boolean isRuning = true;

	public MessageOutThread(AttachableDevice attachableDevice) {
		super();
		this.attachableDevice = attachableDevice;
		setName("MessageOutThread in device:"
				+ this.attachableDevice.getConnection().getClientConfig()
						.getClientName());
	}

	public AttachableDevice getAttachableDevice() {
		return attachableDevice;
	}

	public void setAttachableDevice(AttachableDevice attachableDevice) {
		this.attachableDevice = attachableDevice;
	}

	public void stopOutThread() {
		this.isRuning = false;
		try {
			interrupt();
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		log.info("MessageOut Thread starting...");
		while (isRuning) {
			try {
				Object obj = attachableDevice.takeOutMessage();
				if (obj != null) {
					attachableDevice.getConnection().write(obj);
				}
			} catch (Exception e) {
				log.error("write message error", e);
			}
		}
	}
}