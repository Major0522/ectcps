package com.seagen.ecc.ectcps.simple;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.common.CmdConst;
import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.device.ICommandHandler;
import com.seagen.ecc.ectcps.handlers.HandlerProxy;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Param;
import com.seagen.ecc.ectcps.protocol.Protocol.ProtocolType;
import com.seagen.ecc.utils.DateUtils;

/**
 * 
 * @author kuangjianbo
 * 
 */
public class PutExpressTest {
	public static void main(String[] args) {
		// Options.loadProperties("appConfig.properties");
		final int start = 2000;// 柜子号开始
		final int count = 1000;// 柜子数
		final DefaultConnection[] conns = createConns(start, count);
		for (int i = 0; i < count; i++) {
			connMap.put(start + i, conns[i]);
		}
		TimerTask task = new TimerTask() {// 发送数据
			public void run() {
				if (putSet.size() > 500) {
					log.info("上一轮放件未完成," + putSet.size());
					return;
				}
				for (int i = 0; i < count; i++) {
					CommandMessage cm = createPutMess("1001" + (i + start),
							"1440000" + (i + start));
					try {
						conns[i].write(cm);
					} catch (Exception e) {
						log.error("放件发送异常", e);
					}
				}
			};
		};
		new Timer().schedule(task, 1000, 10000);
	}

	final static Logger log = LoggerFactory.getLogger(PutExpressTest.class);
	final static ExecutorService ec = Executors.newFixedThreadPool(8);
	final static Set<Integer> co = Collections
			.synchronizedSet(new HashSet<Integer>());
	final static Set<Long> putSet = Collections
			.synchronizedSet(new HashSet<Long>());
	final static Set<Long> getSet = Collections
			.synchronizedSet(new HashSet<Long>());
	final static AtomicInteger putCount = new AtomicInteger();
	final static AtomicInteger getCount = new AtomicInteger();
	final static Long serverNo = 99998L;
	final static ConcurrentHashMap<Integer, DefaultConnection> connMap = new ConcurrentHashMap<>();

	public static DefaultConnection[] createConns(final int start,
			final int count) {
		log.info("开始创建连接,start=" + start + ",count=" + count);
		long startTime = System.currentTimeMillis();
		final DefaultConnection[] conns = new DefaultConnection[count];
		final int end = start + count;
		for (int i = start; i < end; i++) {
			ClientConfig clientConfig = new ClientConfig();
			clientConfig.setServerIp("120.26.65.150");
			// clientConfig.setServerIp("120.26.65.0");
			// clientConfig.setServerIp("127.0.0.1");
			// clientConfig.setServerIp("192.168.0.195");
			clientConfig.setTimeout(0);
			clientConfig.setUserId(i);
			clientConfig.setProtocolType(ProtocolType.COMMAND_SSL_SIMPLE);
			clientConfig.setIdentity("test");
			clientConfig.setClientName(i + "");
			conns[i - start] = new DefaultConnection(clientConfig);
			HandlerProxy.setHandler(clientConfig.getClientName(),
					new ExpressHandler(i));

		}
		while (co.size() < count) {
			ec.execute(new Runnable() {
				public void run() {
					for (int i = 0; i < count; i++) {
						if (!conns[i].isConnected()) {
							conns[i].connect();
						}
						if (conns[i].isLogined()) {
							co.add(i);
						}
					}
				};
			});
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			log.info("启动数" + co.size());
		}
		log.info("启动时间:" + (System.currentTimeMillis() - startTime));
		TimerTask task = new TimerTask() {
			public void run() {
				for (int i = 0; i < count; i++) {
					final int loc = i;
					ec.execute(new Runnable() {
						public void run() {
							CommandMessage cm = new CommandMessage();
							cm.setCommandType(5);
							conns[loc].write(cm);
						};
					});

				}

			};
		};
		new Timer().schedule(task, 1000, 40000);
		return conns;
	}

	public static CommandMessage createPutMess(String courierAccNo,
			String recipientPhone) {
		CommandMessage message = new CommandMessage();
		message.setCabinetNo(serverNo);
		message.setCommandType(1);
		message.setFunctionCode(CmdConst.FUN_CODE_EXP_PUT);
		String expressId = DateUtils.getSerialNo() + "";
		message.setParamList(new Param[] {
				new Param("expressId", expressId),
				new Param("trackingNo", expressId),
				new Param("courierAccNo", courierAccNo + ""),
				// new Param("recipieAccNo", recAccNo != null ? recAccNo
				// .toString() : null),
				new Param("recipientPhone", recipientPhone + ""),
				new Param("inBoundTime", DateUtils.getDateTimeStr()),
				new Param("cellId", "1"), new Param("slaveId", "1"),
				new Param("cellType", "1"), new Param("dynamicPwd", expressId) });
		return message;
	}

	public static CommandMessage createGetMess(String expressId) {
		CommandMessage message = new CommandMessage();
		message.setCabinetNo(serverNo);
		message.setFunctionCode(CmdConst.FUN_CODE_EXP_GET);
		message.setParamList(new Param[] { new Param("expressId", expressId),
				new Param("isLocal", "1"),
				new Param("receiptTime", DateUtils.getDateTimeStr()) });
		return message;
	}

	static class ExpressHandler implements ICommandHandler {

		public ExpressHandler(int cabinetId) {
			this.cabinetId = cabinetId;
		}

		private int cabinetId;

		@Override
		public void messageSent(String descAddress, Object msg) {
			log.info("发送成功:" + msg);
			CommandMessage cm = (CommandMessage) msg;
			if (cm.getFunctionCode() == CmdConst.FUN_CODE_EXP_GET) {// 取件
				getSet.add(cm.getSerialNumber());
			} else if (cm.getFunctionCode() == CmdConst.FUN_CODE_EXP_PUT) {// 放件
				putSet.add(cm.getSerialNumber());
			}
		}

		@Override
		public void messageSendFail(String descAddress, Object msg, String error) {
			log.info("发送失败:" + msg);
		}

		@Override
		public void messageReceived(String remoteAddress, Object msg) {
			log.info("收到:" + msg);
			CommandMessage cm = (CommandMessage) msg;
			if (cm.getFunctionCode() == CmdConst.FUN_CODE_EXP_GET) {// 取件
				if (CmdConst.RETURN_CODE_SUCCESS.equals(cm
						.get(CmdConst.RETURN_CODE))) {
					getSet.remove(cm.getSerialNumber());
					int count = getCount.addAndGet(1);
					log.info("取件成功=" + count + ",未成功=" + getSet.size());
				} else {
					log.error("取件失败:" + cm);
				}
			} else if (cm.getFunctionCode() == CmdConst.FUN_CODE_EXP_PUT) {// 放件
				if (CmdConst.RETURN_CODE_SUCCESS.equals(cm
						.get(CmdConst.RETURN_CODE))) {
					putSet.remove(cm.getSerialNumber());
					int count = putCount.addAndGet(1);
					log.info("放件成功=" + count + ",未成功=" + putSet.size());
					// 取
					CommandMessage reply = createGetMess(cm.get("expressId"));
					try {
						connMap.get(cabinetId).write(reply);
					} catch (Exception e) {
						log.error("取件发送异常", e);
					}
				} else {
					log.error("放件失败:" + cm);
				}
			}
		}
	}
}
