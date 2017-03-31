package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.util.MessageUtils;

/**
 * 编码器,将普通消息(CommandMessage)编码为WebSocket消息
 * 
 * @author kuangjianbo
 * 
 */
public class WebSocketEncoder extends MessageToMessageEncoder<CommandMessage> {
	private static Logger log = LoggerFactory.getLogger(WebSocketEncoder.class);

	protected void encode(ChannelHandlerContext ctx, CommandMessage msg,
			List<Object> out) throws Exception {
		if (msg == null) {
			log.warn("msg==null");
			return;
		}
		ByteBuf buf = null;
		try {
			// 二进制发送
			buf = ctx.alloc().buffer();
			MessageUtils.commandMessage2ByteBuf(msg, buf, null);
			WebSocketFrame wmsg = new BinaryWebSocketFrame(buf);
			// 文本发送
			// WebSocketFrame wmsg = new TextWebSocketFrame(
			// JsonUtil.ojbToJsonStr(msg));
			out.add(wmsg);
		} catch (Exception e) {
			log.error("encode failed", e);
			if (buf != null) {
				buf.release();
			}
		} finally {
			// ReferenceCountUtil.releaseLater(buf);
		}
	}
}
