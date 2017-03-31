package com.seagen.ecc.ectcps.simple;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.device.mq.MqConnection;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Param;
import com.seagen.ecc.ectcps.protocol.Protocol;

public class MqConnectionTest {
	public static void main(String[] args) {
		final Random random = new Random();
		final int start = Math.abs(random.nextInt());
		final int end = start + 1;// 1000个连接,每个连接每秒发送一条数据给其它连接

		final DefaultConnection[] conns = new DefaultConnection[end - start];
		for (int i = start; i < end; i++) {
			ClientConfig clientConfig = new ClientConfig();
			clientConfig.setReceiveQueue(clientConfig.getSendQueue());
			clientConfig.setProtocolType(Protocol.ProtocolType.MQ);
			clientConfig.setUri("amqp://admin:admin@192.168.0.218:5672");
			conns[i - start] = new MqConnection(clientConfig);
		}
		final ExecutorService ec = Executors.newFixedThreadPool(20);// 20个线程来完成
		for (int n = 0; n < 10; n++) {// 建立连接
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
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
									new Param("阿斯蒂芬阿斯蒂芬阿萨德发", "请问人情阿萨德发送法定化味去") });
							conns[i - start].write(cm);
						}
					};
				});
			};
		};
		Timer timer = new Timer();
		timer.schedule(task, 1000, 100);
		try {
			Thread.sleep(50 * 1000);
		} catch (InterruptedException e) {
		}
		timer.cancel();

	}
}
