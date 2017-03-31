package com.seagen.ecc.ectcps.device;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 超时处理
 * 
 * @author kuangjianbo
 * 
 */
@Sharable
public class TimeoutHandler extends IdleStateHandler {
	private static Logger log = LoggerFactory.getLogger(TimeoutHandler.class);
	private AttachableDevice device;

	public TimeoutHandler(int seconds, AttachableDevice device) {
		super(seconds * 3, seconds, 0);
		this.device = device;
	}

	@Override
	protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt)
			throws Exception {
		super.channelIdle(ctx, evt);
		if (evt.state() == IdleState.READER_IDLE) {
//			if (device.getIdleCount().addAndGet(1) % 3 == 0) {
//				log.info("READER_IDLE 3 times,device restart...");
//				device.restart();
//				return;
//			}
			log.info("READER_IDLE,reconnect...");
			device.close();
			device.sendHeartBeatPack();
		} else if (evt.state() == IdleState.WRITER_IDLE) {
			device.sendHeartBeatPack();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		super.exceptionCaught(ctx, cause);
		device.sendHeartBeatPack();
	}
}