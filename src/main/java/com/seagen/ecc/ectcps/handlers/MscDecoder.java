package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.protocol.McsMessage;
import com.seagen.ecc.utils.JsonUtil;

/**
 * 与模块控制系统之间的协议解码
 * 
 */
public class MscDecoder extends ByteToMessageDecoder {
	private static Logger log = LoggerFactory.getLogger(MscDecoder.class);
	private Charset charset;

	public MscDecoder(Charset charset) {
		super();
		this.charset = charset;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf,
			List<Object> out) {
		if (buf.readableBytes() < 5) {
			return;
		}
		buf.markReaderIndex();
		byte first = buf.readByte();
		while (first != McsMessage.HEADER_FIRST) {// 找到头字节
			log.debug("look for HEADER_FIRST");
			if (buf.isReadable()) {
				first = buf.readByte();
			} else {
				break;
			}
		}
		if (first != McsMessage.HEADER_FIRST) {
			return;
		}
		int length = buf.readInt();// 数据长度
		if (buf.readableBytes() < length) {
			buf.resetReaderIndex();
			log.debug("data hasn't enough length");
			return;
		}
		byte[] msg = new byte[length];
		buf.readBytes(msg);
		String json = new String(msg, charset);
		McsMessage ret = JsonUtil.jsonStrToObj(json, McsMessage.class);
		out.add(ret);
	}

}
