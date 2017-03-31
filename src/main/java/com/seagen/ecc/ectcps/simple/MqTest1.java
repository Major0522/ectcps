package com.seagen.ecc.ectcps.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MqTest1 {
	protected static Logger log = LoggerFactory.getLogger(MqTest1.class);

	public static void main(String[] args) throws Exception {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setUri("amqp://guest:guest@127.0.0.1:5672");
		Connection conn = connectionFactory.newConnection();
		for (int i = 0; i < 10; i++) {
			Channel channel = conn.createChannel();
			System.out.println(channel.getChannelNumber());
			channel.queueDeclare(i+"", false,
					false, true, null);
		}
		
	}

}
