package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.util.MessageUtils;

public class CommandMessageEncoder extends MessageToByteEncoder<CommandMessage> {
	private static Logger log = LoggerFactory
			.getLogger(CommandMessageEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, CommandMessage msg,
			ByteBuf out) throws Exception {
		if (msg == null) {
			log.warn("msg==null");
			return;
		}
		try {
			out.markWriterIndex();
			MessageUtils.commandMessage2ByteBuf(msg, out, null);
		} catch (Exception e) {
			log.error("encode failed", e);
			out.resetWriterIndex();
		}
	}
}
