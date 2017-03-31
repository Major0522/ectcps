package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.seagen.ecc.ectcps.device.mq.MqConnection;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.util.MessageUtils;
import com.seagen.ecc.utils.DateUtils;

/**
 * mq消息接收消息处理器
 * 
 * @author kuangjianbo
 * 
 */
public class MqClientHandler extends DefaultConsumer {
	private static Logger log = LoggerFactory.getLogger(MqClientHandler.class);
	protected static AtomicInteger count = new AtomicInteger();
	private MqConnection connection;

	public MqClientHandler(MqConnection connection) {
		super(connection.getMqChannel());
		this.connection = connection;
	}

	@SuppressWarnings("static-access")
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope,
			BasicProperties properties, byte[] body) throws IOException {
		connection.setLastReceived(System.currentTimeMillis());
		log.debug(connection.getClientConfig().getClientName() + ":read count:"
				+ count.addAndGet(1));
		CommandMessage cm = MessageUtils.byteBuf2CommandMessage(Unpooled
				.wrappedBuffer(body));
		HandlerProxy.messageReceived(connection.getClientConfig()
				.getClientName(), cm, null);
		getChannel().basicAck(envelope.getDeliveryTag(), false);// 接收到直接删除
		// 计算往返时间,统计
		long end = System.currentTimeMillis();
		long start = DateUtils.serialNo2Date(cm.getSerialNumber());
		String info = String.format("read,write,time:%s,%s,%s,%s", count.get(),
				connection.count.get(), end - start, cm.getSerialNumber());
		log.debug(info);
	}

}
