package com.seagen.ecc.ectcps.simple;

import io.netty.util.ResourceLeakDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.ICommandHandler;
import com.seagen.ecc.ectcps.device.mq.MqDevice;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Param;
import com.seagen.ecc.ectcps.protocol.Protocol;

public class MqTest {
	protected static Logger log = LoggerFactory.getLogger(MqTest.class);

	public static void main(String[] args) {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setReceiveQueue("ecbos");
		clientConfig.setProtocolType(Protocol.ProtocolType.MQ);
		clientConfig.setUri("amqp://guest:guest@127.0.0.1:5672");
		MqDevice device = new MqDevice(clientConfig);
		device.start();
		HandlerProxy.setHandler(clientConfig.getClientName(),
				new ICommandHandler() {
					@Override
					public void messageSent(String descAddress, Object msg) {
						if (msg instanceof CommandMessage)
							log.info("发送信息:" + msg);
					}

					@Override
					public void messageSendFail(String descAddress, Object msg,
							String error) {
					}

					@Override
					public void messageReceived(String remoteAddress, Object msg) {
						log.info("收到信息:" + msg);

					}
				});
		while (true) {
			CommandMessage cm = new CommandMessage();
			cm.setCabinetNo(100002);
			StringBuilder b = new StringBuilder();
			for (int i = 1; i < 1000; i++) {
				b.append(i);
			}
			cm.setParamList(new Param[] { new Param("a", b.toString()) });
			device.addOutMessage(cm);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

			}
		}
	}
}
