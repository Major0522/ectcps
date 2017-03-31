package com.seagen.ecc.ectcps.simple;

import io.netty.util.ResourceLeakDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.ICommandHandler;
import com.seagen.ecc.ectcps.device.webSocket.WebSocketDevice;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Param;
import com.seagen.ecc.ectcps.protocol.Protocol;
import com.seagen.ecc.utils.NetUtils;

public class WebsockeTest {
	protected static Logger log = LoggerFactory.getLogger(WebsockeTest.class);

	public static void main(String[] args) {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setClientName("test");
		clientConfig.setServerIp("127.0.0.1");
		clientConfig.setServerPort(18090);
		clientConfig.setUserId(100002);
		clientConfig.setTimeout(1);
		clientConfig.setNeedLogin(true);
		clientConfig.setIdentity(NetUtils.getMachineCode());
		clientConfig.setPasswd("password");
		clientConfig.setProtocolType(Protocol.ProtocolType.WEBSOCKET);
		clientConfig.setUri("ws://192.168.0.216:8080/ecc_cs/ws?name=abcd");
		// clientConfig
		//
		// .setWebSocketUri("ws://192.168.0.216:8080/websocketTomcat8/ws?name=abcd");
		WebSocketDevice device = new WebSocketDevice(clientConfig);
		device.start();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// device.addOutMessage(new TextWebSocketFrame("Message #"));
		// device.addOutMessage(new TextWebSocketFrame("Message 呵呵"));
		// device.addOutMessage(new BinaryWebSocketFrame(ProtocolSelect
		// .createLoginBuffer(clientConfig)));
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
						if (msg instanceof CommandMessage)
							log.info("收到信息:" + msg);

					}
				});
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {

			}
			CommandMessage cm = new CommandMessage();
			cm.setCabinetNo(99998);
			StringBuilder b = new StringBuilder();
			for (int i = 1; i < 1000; i++) {
				b.append(i);
			}
			cm.setParamList(new Param[] { new Param("a", b.toString()) });
			device.addOutMessage(cm);
		}
	}
}
