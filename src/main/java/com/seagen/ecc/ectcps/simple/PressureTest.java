package com.seagen.ecc.ectcps.simple;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.device.ICommandHandler;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Param;
import com.seagen.ecc.ectcps.protocol.Protocol.ProtocolType;

/**
 * 压力测试
 * 
 */
public class PressureTest {
	protected static Logger log = LoggerFactory.getLogger(PressureTest.class);

	public static void main(String[] args) {

		final Random random = new Random();
		final int start = 5001;
		final int end = start + 1;// 1000个连接,每个连接每秒发送一条数据给其它连接

		final DefaultConnection[] conns = new DefaultConnection[end - start];
		for (int i = start; i < end; i++) {
			ClientConfig clientConfig = new ClientConfig();
			clientConfig.setServerIp("192.168.0.195");
			clientConfig.setTimeout(0);
			clientConfig.setUserId(i);
			clientConfig.setProtocolType(ProtocolType.COMMAND_SSL_SIMPLE);
			clientConfig.setIdentity("test");
			conns[i - start] = new DefaultConnection(clientConfig);
			HandlerProxy.setHandler(clientConfig.getClientName(),
					new ICommandHandler() {
						@Override
						public void messageSent(String descAddress, Object msg) {
							if (msg instanceof CommandMessage)
								log.info("发送信息:" + msg);

						}

						@Override
						public void messageSendFail(String descAddress,
								Object msg, String error) {

						}

						@Override
						public void messageReceived(String remoteAddress,
								Object msg) {
							if (msg instanceof CommandMessage)
								log.info("收到信息:" + msg);

						}
					});
		}

		final ExecutorService ec = Executors.newFixedThreadPool(20);// 20个线程来完成
		for (int n = 0; n < 20; n++) {// 建立连接
			ec.execute(new Runnable() {
				public void run() {
					for (int i = start; i < end; i++) {
						if (!conns[i - start].isConnected()) {
							conns[i - start].connect();
						}
					}
				};
			});
		}
		TimerTask task = new TimerTask() {// 发送数据
			public void run() {
				ec.execute(new Runnable() {
					public void run() {
						for (int i = start; i < end; i++) {
							int cabinetNo;
							if (i == start) {
								cabinetNo = end - 1;
							} else if (i == end - 1) {
								cabinetNo = start;
							} else {
								cabinetNo = i + 1;
							}
							CommandMessage cm = new CommandMessage(cabinetNo,
									1, random.nextInt());
							cm.setParamList(new Param[] { new Param("a", "b"),
									new Param("aasdfasd", "asdf"),
									new Param("qwerqwerqwerq", "qwerqwerqwer"),
									new Param("a", "b"),
									new Param("asdfasdf", "asdf"),
									new Param("asdfasdf", "asdf"),
									new Param("asdfasdf", "asdf"),
									new Param("阿斯蒂芬阿斯蒂芬阿萨德发", "请问人情阿萨德发送法定化味去") });
							conns[i - start].write(cm);
						}
					};
				});
			};
		};
		new Timer().schedule(task, 1000, 5000);

	}
}
