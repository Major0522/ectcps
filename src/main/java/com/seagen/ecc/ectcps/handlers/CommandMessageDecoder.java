package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.util.MessageUtils;

public class CommandMessageDecoder extends ByteToMessageDecoder {
	private static Logger log = LoggerFactory
			.getLogger(CommandMessageDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf,
			List<Object> out) {
		if (!buf.isReadable()) {
			return;
		}
		try {
			CommandMessage cm = MessageUtils.byteBuf2CommandMessage(buf, null);
			out.add(cm);
		} catch (Exception e) {
			log.error("decode failed", e);
		} finally {
			if (buf.readerIndex() != buf.writerIndex()) {
				log.warn("buf.readerIndex() != buf.writerIndex()");
				buf.readerIndex(buf.writerIndex());
			}
		}
	}
}
