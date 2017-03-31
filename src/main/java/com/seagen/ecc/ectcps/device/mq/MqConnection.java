package com.seagen.ecc.ectcps.device.mq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.handlers.MqClientHandler;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.util.MessageUtils;

/**
 * 当前连接/会话
 * 
 * @author kuangjianbo
 * 
 */
public class MqConnection extends DefaultConnection {
	private ConnectionFactory connectionFactory;
	private Connection conn;
	private Channel mqChannel;

	public MqConnection(ClientConfig clientConfig) {
		super(clientConfig);
		clientConfig.setNeedLogin(false);
		clientConfig.setTimeout(0);
		connectionFactory = new ConnectionFactory();
		try {
			connectionFactory.setUri(clientConfig.getUri());
		} catch (KeyManagementException | NoSuchAlgorithmException
				| URISyntaxException e) {
			log.error("错误的URI地址,uri=" + clientConfig.getUri(), e);
		}
	}

	@Override
	public boolean isConnected() {
		return mqChannel != null && mqChannel.isOpen();
	}

	@Override
	public synchronized void connect() {
		if (!isConnected()) {
			try {
				int wait = connectCount.getAndAdd(1);
				if (wait > 0) {
					log.info(clientConfig.getClientName() + ":连接频繁,等待" + wait
							+ "秒");
					Thread.sleep(wait % waitSeconds * 1000);
				}
			} catch (InterruptedException e) {
			}
			close();
			this.mqChannel = createChanel();
			// 声明/创建队列
			try {
				Map<String, Object> arguments = new HashMap<String, Object>();
				arguments.put("expiration", "60000");
				mqChannel.queueDeclare(clientConfig.getReceiveQueue(), false,
						false, true, arguments);
				mqChannel.basicConsume(clientConfig.getReceiveQueue(),
						new MqClientHandler(this));
				mqChannel.queueDeclare(clientConfig.getSendQueue(), false,
						false, true, arguments);
			} catch (IOException e) {
				log.error("mqChannel连接异常!!" + e);
				if (mqChannel != null) {
					try {
						mqChannel.close();
					} catch (IOException e1) {
					}
				}
			}
		} else {
			connectCount.set(1);
			log.info(clientConfig.getClientName() + ":isConnected!!!");
		}

	}

	@Override
	public void write(Object obj) {
		CommandMessage cm = null;
		if (obj instanceof CommandMessage) {
			cm = (CommandMessage) obj;
		} else {
			log.error("错误的消息类型:" + obj);
			return;
		}
		if (!isConnected()) {// 未连接,先连接
			log.info(clientConfig.getClientName()
					+ ":not Connected,start connect...");
			connect();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		try {
			if (isConnected()) {// 可以发送消息
				byte[] buf = MessageUtils.commandMessage2ByteBuf(cm).array();
				mqChannel.basicPublish("", clientConfig.getSendQueue(), null,
						buf);
				HandlerProxy
						.messageSent(clientConfig.getClientName(), cm, null);
				int loc = count.addAndGet(1);
				log.debug(clientConfig.getClientName() + ":write count:" + loc);
			}
			return;
		} catch (Exception e) {
			log.error("发送mq消息异常", e);
		}
		HandlerProxy.messageSendFail(clientConfig.getClientName(), cm, null,
				"connect failed,not send.");

	}

	@Override
	public void close() {
		if (mqChannel != null) {
			try {
				mqChannel.close();
			} catch (IOException e) {
				log.error("关闭Channel异常," + e.getClass().getSimpleName()
						+ ",message=" + e.getMessage());
			}
		}
	}

	public Channel createChanel() {
		Channel ch = null;
		try {
			if (conn == null || !conn.isOpen()) {
				if (conn != null) {
					conn.close();
				}
				conn = connectionFactory.newConnection();
			}
			ch = conn.createChannel();
		} catch (IOException e) {
			log.error("连接失败," + e.getClass().getSimpleName() + ",message="
					+ e.getMessage());
		}
		return ch;
	}

	public Channel getMqChannel() {
		return mqChannel;
	}

}
