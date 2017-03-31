package com.seagen.ecc.ectcps.device;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.NettyClient;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;

/**
 * 当前连接/会话
 * 
 * @author kuangjianbo
 * 
 */
public class DefaultConnection {
	protected static Logger log = LoggerFactory
			.getLogger(DefaultConnection.class);
	protected volatile Channel channel;
	protected ClientConfig clientConfig;
	protected volatile boolean isLogined = false;// 是否登录
	protected volatile boolean isBreak = false;// 中断信号,如果收到中断信号,将停止当前发送
	public static AtomicInteger count = new AtomicInteger();// 发送消息数
	protected AtomicInteger connectCount = new AtomicInteger();// 连接次数
	protected int waitSeconds = 60;// 最久连接等待时间,每连接一次,连接等待时长加一秒,但不超过该值
	private long lastReceived = System.currentTimeMillis();// 最后收到消息

	public DefaultConnection(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public boolean isConnected() {
		return channel != null && channel.isActive();
	}

	public synchronized void connect() {
		if (!isConnected()) {
			try {
				int wait = connectCount.getAndAdd(1) % waitSeconds;
				if (wait > 0) {
					log.info(clientConfig.getClientName() + ":连接频繁,等待" + wait
							+ "秒");
					Thread.sleep(wait * 1000);
				}
			} catch (InterruptedException e) {
			}
			NettyClient.getInstance().createChannel(this);
		} else {
			connectCount.set(1);
			log.info(clientConfig.getClientName() + ":isConnected!!!");
		}
	}

	/**
	 * 向通道中写数据,当没有连接/登录时,将会先进行连接/登录,如果连接或者登录失败,数据将会发送失败,不重复连接
	 * 
	 * @param obj
	 */
	public void write(Object obj) {
		if (isBreak) {
			setBreak(false);
			HandlerProxy.messageSendFail(clientConfig.getClientName(), obj,
					channel, "send break!");
			return;
		}
		if (!isBreak && !isConnected()) {// 未连接,先连接
			log.info(clientConfig.getClientName()
					+ ":not Connected,start connect...");
			connect();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		if (!isBreak && !isLogined) {// 未登录,等待登录完成
			log.info(clientConfig.getClientName() + ":not logined, wait...");
			connect();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		if (isConnected() && isLogined) {// 可以发送消息
			channel.writeAndFlush(obj);
			HandlerProxy
					.messageSent(clientConfig.getClientName(), obj, channel);
			int loc = count.addAndGet(1);
			log.debug(clientConfig.getClientName() + ":write count:" + loc);
			connectCount.set(1);
		} else {// 连接失败,未发送
			HandlerProxy.messageSendFail(clientConfig.getClientName(), obj,
					channel, "connect failed,not send.");
		}

	}

	public synchronized void setChannel(Channel channel) {
		this.channel = channel;

	}

	public boolean isLogined() {
		return isLogined;
	}

	public void setLogined(boolean isLogined) {
		this.isLogined = isLogined;
	}

	public void setClientConfig(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	public void close() {
		setLogined(false);
		if (channel != null) {
			channel.close();
			channel = null;
		}
	}

	public boolean isBreak() {
		return isBreak;
	}

	public void setBreak(boolean isBreak) {
		this.isBreak = isBreak;
	}

	public long getLastReceived() {
		return lastReceived;
	}

	public void setLastReceived(long lastReceived) {
		this.lastReceived = lastReceived;
	}

}
