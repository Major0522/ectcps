package com.seagen.ecc.ectcps.handlers;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.seagen.ecc.ectcps.device.ICommandHandler;

/**
 * 业务代理
 * 
 * @author kuangjianbo
 * 
 */
public class HandlerProxy {
	/**
	 * 默认的业务处理线程池
	 */
	private static ExecutorService es = Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors() * 10);

	public static ExecutorService getEs() {
		return es;
	}

	/**
	 * 用户可以根据需要自定义线程池
	 * 
	 * @param es
	 */
	public static void setEs(ExecutorService es) {
		if (HandlerProxy.es != null) {
			HandlerProxy.es.shutdown();
		}
		HandlerProxy.es = es;
	}

	private static Map<String, ICommandHandler> map = new ConcurrentHashMap<String, ICommandHandler>();

	public static void setHandler(String deviceNo, ICommandHandler handler) {
		map.put(deviceNo, handler);
	}

	public static ICommandHandler getHandler(String deviceNo) {
		return map.get(deviceNo);
	}

	public static void messageReceived(String deviceNo, final Object msg,
			Channel channel) {
		final ICommandHandler handler = map.get(deviceNo);
		if (handler != null) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					handler.messageReceived(null, msg);
				}
			});
		}
	}

	public static void messageSent(String deviceNo, final Object msg,
			Channel channel) {
		final ICommandHandler handler = map.get(deviceNo);
		if (handler != null) {
			handler.messageSent(null, msg);
		}
	}

	public static void messageSendFail(String deviceNo, final Object msg,
			Channel channel, final String error) {
		final ICommandHandler handler = map.get(deviceNo);
		if (handler != null) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					handler.messageSendFail(null, msg, error);
				}
			});
		}
	}
}
