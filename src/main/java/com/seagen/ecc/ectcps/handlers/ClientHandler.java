package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ConnectTimeoutException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.protocol.ProtocolSelect;

@Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
	private static Logger log = LoggerFactory.getLogger(ClientHandler.class);
	protected DefaultConnection connection;
	protected boolean needLogin = true;
	protected static AtomicInteger count = new AtomicInteger();

	public ClientHandler() {
	}

	public ClientHandler(DefaultConnection connection) {
		this.connection = connection;
		this.needLogin = connection.getClientConfig().isNeedLogin();
	}

	/**
	 * 更新客户端的登录状态,并且定制协议
	 * 
	 * @param channel
	 */
	void updateLoginState(Channel channel) {
		needLogin = false;
		connection.setChannel(channel);
		ClientConfig config = connection.getClientConfig();
		ProtocolSelect.applyProtocol(channel, config, true);
		channel.pipeline().addLast("clientHandler", this);
		connection.setLogined(true);
		log.info("login success:" + connection.getClientConfig().getUserId());
	}

	/**
	 * 当客户端获取连接时发送认证信息
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.info("channelConnected login check...");
		if (needLogin) {
			log.info("start login...");
			ByteBuf loginBuf = ProtocolSelect.createLoginBuffer(connection
					.getClientConfig());
			ctx.writeAndFlush(loginBuf);
		} else {
			updateLoginState(ctx.channel());
		}

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		connection.setLastReceived(System.currentTimeMillis());
		log.debug(connection.getClientConfig().getClientName() + ":read count:"
				+ count.addAndGet(1));
		if (needLogin) {// 没有登录的话先需要获取登录认证才允许通信
			ByteBuf reply = null;
			try {
				reply = ((ByteBuf) msg);
				int loginResult = reply.readByte();
				if (loginResult == 1) {// successed
					updateLoginState(ctx.channel());
				} else {// failed
					log.warn("登录失败(" + connection.getClientConfig().getUserId()
							+ "),reply=" + loginResult + ",channel="
							+ ctx.channel());
					ctx.close();
				}
			} finally {
				if (reply != null) {
					reply.release();
				}
			}
		} else {// 已经认证了的话,处理接收的的消息
			HandlerProxy.messageReceived(connection.getClientConfig()
					.getClientName(), msg, ctx.channel());
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause instanceof ConnectTimeoutException) {
			log.error("连接超时!!!");
		} else if (cause instanceof IOException && cause.getMessage() != null
				&& cause.getMessage().startsWith("远程主机强迫关闭了一个现有的连接")) {
			log.warn(cause.getMessage());
		} else {
			log.error("exceptionCaught", cause);
		}
		connection.close();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("连接关闭,id=" + connection.getClientConfig().getUserId()
				+ ",chanel=:" + ctx.channel());
		ctx.close();
		connection.close();
	}
}
