package com.seagen.ecc.ectcps.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.protocol.McsMessage;
import com.seagen.ecc.utils.JsonUtil;

public class MscEncoder extends MessageToByteEncoder<McsMessage> {
	private static Logger log = LoggerFactory.getLogger(MscEncoder.class);
	private Charset charset;

	public MscEncoder(Charset charset) {
		super();
		this.charset = charset;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, McsMessage msg, ByteBuf out) {
		String json = JsonUtil.ojbToJsonStr(msg);
		if (json == null) {
			log.error(msg + "转换成json失败");
			return;
		}
		byte[] pack = json.getBytes(charset);
		out.writeByte(McsMessage.HEADER_FIRST);
		out.writeInt(pack.length);
		out.writeBytes(pack);
	}
}
