package com.seagen.ecc.ectcps.device;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;

public abstract class AttachableDevice {
	protected Logger log = LoggerFactory.getLogger(getClass());
	protected BlockingQueue<Object> outQueue = new LinkedBlockingQueue<Object>();
	protected MessageOutThread messageOutThread;
	protected HeartBeatThread heartBeatThread;
	protected volatile DefaultConnection connection;
	protected volatile boolean isRunning;
	protected ClientConfig clientConfig;

	public AttachableDevice() {
	}

	public AttachableDevice(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	protected void startHeartBeatThread() {
		if (heartBeatThread != null) {
			heartBeatThread.stopHeartBeat();
		}
		heartBeatThread = new HeartBeatThread(this);
		heartBeatThread.start();
	}

	protected void startMessageOutThread() {
		if (messageOutThread != null) {
			messageOutThread.stopOutThread();
		}
		messageOutThread = new MessageOutThread(this);
		messageOutThread.start();
	}

	public void start() {
		log.info(clientConfig.getClientName() + ":device start...");
		this.connection = new DefaultConnection(clientConfig);
		this.setRunning(true);
		this.startMessageOutThread();
		this.startHeartBeatThread();
		this.sendHeartBeatPack();
		log.info(clientConfig.getClientName() + ":device start finish...");
	}

	public void stop() {
		log.info(clientConfig.getClientName() + ":device stop...");
		this.setRunning(false);
		this.connection.setBreak(true);
		this.connection.close();
		this.messageOutThread.stopOutThread();
		this.heartBeatThread.stopHeartBeat();
		log.info(clientConfig.getClientName() + ":device stop finish...");
	}

	public void restart() {
		log.info(clientConfig.getClientName() + ":device restart...");
		this.stop();
		this.start();
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public DefaultConnection getConnection() {
		return connection;
	}

	public void setConnection(DefaultConnection connection) {
		this.connection = connection;
	}

	public boolean hashOutMessage() {
		return outQueue.size() > 0;
	}

	public void addOutMessage(Object message) {
		if (outQueue.contains(message)) {
			log.info(connection.getClientConfig().getClientName()
					+ ":same message has in outQueue");
			return;
		}
		outQueue.add(message);
	}

	public Object takeOutMessage() {
		try {
			return outQueue.take();
		} catch (InterruptedException e) {
			log.warn("InterruptedException");
		}
		return null;
	}

	public void close() {
		connection.close();
	}

	public void sendHeartBeatPack() {
		if (clientConfig.getTimeout() > 0) {
			Object heartBeat = getHeartBeatPack();
			if (heartBeat != null && !hashOutMessage()) {
				log.info(getConnection().getClientConfig().getClientName()
						+ ":send HeartBeatPack...");
				addOutMessage(heartBeat);
			}
		}
	}

	/*
	 * public TimeoutHandler getTimeoutHandler() { int timeout =
	 * connection.getClientConfig().getTimeout(); if (timeout > 0) { return new
	 * TimeoutHandler(timeout, this); } return null; }
	 */

	public boolean isConnected() {
		boolean ret = connection.isConnected();
		if (!ret) {// 如果没有连接,尝试发送心跳
			sendHeartBeatPack();
		}
		return ret;
	}

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	public abstract Object getHeartBeatPack();
}
