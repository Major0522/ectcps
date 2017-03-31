package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.webSocket.WebSocketConnection;
import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.ProtocolSelect;
import com.seagen.ecc.ectcps.util.MessageUtils;

/**
 * 客户端解码(WebSocket)
 * 
 * @author kuangjianbo
 * 
 */
public class WebSocketDecoder extends ClientHandler {
	private static Logger log = LoggerFactory.getLogger(WebSocketDecoder.class);

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	private ClientConfig clientConfig;

	public WebSocketDecoder(WebSocketConnection connection) {
		this.connection = connection;
		this.clientConfig = connection.getClientConfig();
		URI uri = null;
		try {
			uri = new URI(clientConfig.getUri());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri,
				WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		handshakeFuture = ctx.newPromise();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		handshaker.handshake(ctx.channel());
	}

	public void onLogin(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof BinaryWebSocketFrame) {
			ByteBuf reply = ((BinaryWebSocketFrame) msg).content();
			int loginResult = reply.readByte();
			if (loginResult == 1) {// successed
				needLogin = false;
				connection.setLogined(true);
			} else {//
				log.warn("登录失败(" + connection.getClientConfig().getUserId()
						+ "),reply=" + loginResult + ",channel="
						+ ctx.channel());
				ctx.close();
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		connection.setLastReceived(System.currentTimeMillis());
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {// 握手完毕可以通信
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			log.info("WebSocket Client connected!");
			handshakeFuture.setSuccess();
			log.info("start login...");
			ByteBuf loginBuf = ProtocolSelect.createLoginBuffer(connection
					.getClientConfig());
			ctx.writeAndFlush(new BinaryWebSocketFrame(loginBuf));
			return;
		}
		if (msg instanceof FullHttpResponse) {// 普通的HTTP消息,当异常处理
			FullHttpResponse response = (FullHttpResponse) msg;
			connection.close();
			throw new Exception("Unexpected FullHttpResponse (getStatus="
					+ response.getStatus() + ", content="
					+ response.content().toString(CharsetUtil.UTF_8) + ')');
		}
		if (msg instanceof WebSocketFrame) {
			WebSocketFrame frame = (WebSocketFrame) msg;
			try {
				if (needLogin) {// 需要登录
					onLogin(ctx, msg);
					return;
				}
				if (frame instanceof BinaryWebSocketFrame) {// 二进制消息
					BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
					CommandMessage cm = MessageUtils
							.byteBuf2CommandMessage(binaryFrame.content());
					HandlerProxy.messageReceived(connection.getClientConfig()
							.getClientName(), cm, ctx.channel());
				} else if (frame instanceof TextWebSocketFrame) {// 文本消息
					TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
					log.info("文本消息:" + textFrame.text());
					// CommandMessage cm =
					// JsonUtil.jsonStrToObj(textFrame.text(),
					// CommandMessage.class);
					// HandlerProxy.messageReceived(connection.getClientConfig()
					// .getClientName(), cm, ctx.channel());
				} else if (frame instanceof PongWebSocketFrame) {
				} else if (frame instanceof CloseWebSocketFrame) {// 关闭
					connection.close();
				}
			} finally {
				frame.release();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (!handshakeFuture.isDone()) {
			handshakeFuture.setFailure(cause);
		}
		super.exceptionCaught(ctx, cause);
	}
}
