package com.seagen.ecc.ectcps.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seagen.ecc.ectcps.protocol.CommandMessage;
import com.seagen.ecc.ectcps.protocol.Param;
import com.seagen.ecc.ectcps.protocol.Protocol.CommandType;
import com.seagen.ecc.utils.JsonUtil;

/**
 * {@link CommandMessage}消息处理工具类,
 * 
 * @author kuangjianbo
 * 
 */
public class MessageUtils {

	public static Map<String, String> paramList2Map(Param[] paramList) {
		Map<String, String> map = new HashMap<String, String>();
		if (paramList != null) {
			for (int i = 0; i < paramList.length; i++) {
				map.put(paramList[i].getParamName(),
						paramList[i].getParamValue());
			}
		}
		return map;
	}

	public static Param[] map2ParamList(Map<String, String> map) {
		Param[] ret = new Param[map.size()];
		int i = 0;
		for (Map.Entry<String, String> e : map.entrySet()) {
			ret[i++] = new Param(e.getKey(), e.getValue());
		}
		return ret;
	}

	public static ByteBuf commandMessage2ByteBuf(CommandMessage cm) {
		return commandMessage2ByteBuf(cm, null, null);
	}

	public static ByteBuf commandMessage2ByteBuf(CommandMessage cm,
			Charset charset) {
		return commandMessage2ByteBuf(cm, null, charset);
	}

	/**
	 * 将CommandMessage消息对象转换成字节码
	 * 
	 * @param cm
	 * @param buf
	 * @param charset
	 * @return
	 */
	public static ByteBuf commandMessage2ByteBuf(CommandMessage cm,
			ByteBuf buf, Charset charset) {
		if (charset == null) {
			charset = NettyUtils.DEFAULT_CHARSET;
		}
		if (buf == null) {
			// buf = Unpooled.directBuffer();
			buf = allocator.buffer();
		}
		buf.writeLong(cm.getSerialNumber());// 8
		buf.writeInt((int) cm.getCabinetNo());// 4
		buf.writeByte(cm.getCommandType());// 1
		buf.writeInt(cm.getFunctionCode());// 4
		if (cm.getParamList() != null) {
			Param[] params = cm.getParamList();

			int validCount = 0;
			for (int i = 0; i < params.length; i++) {
				if (params[i] != null) {
					validCount++;
				}
			}
			buf.writeShort(validCount);// 2

			for (int i = 0; i < params.length; i++) {
				if (params[i] == null) {
					continue;
				}

				NettyUtils.writeByteString(buf, charset,
						params[i].getParamName());
				NettyUtils.writeString(buf, charset, params[i].getParamValue());
			}
		} else {
			buf.writeShort(0);
		}
		return buf;
	}

	/**
	 * 将特定的字节码转换成CommandMessage消息对象
	 * 
	 * @param buf
	 * @param charset
	 * @return
	 */
	public static CommandMessage byteBuf2CommandMessage(ByteBuf buf,
			Charset charset) {
		if (charset == null) {
			charset = NettyUtils.DEFAULT_CHARSET;
		}
		CommandMessage cm = new CommandMessage();
		cm.setSerialNumber(buf.readLong());// 8
		cm.setCabinetNo(buf.readInt());// 4
		cm.setCommandType(buf.readByte());// 1
		cm.setFunctionCode(buf.readInt());// 4
		int len = buf.readShort();// 2
		if (len > 0) {
			Param[] params = new Param[len];
			for (int i = 0; i < len; i++) {
				String name = NettyUtils.readByteString(buf, charset);
				String value = NettyUtils.readString(buf, charset);
				params[i] = new Param(name, value);
			}
			cm.setParamList(params);
		}
		return cm;
	}

	public static CommandMessage byteBuf2CommandMessage(ByteBuf buf) {
		return byteBuf2CommandMessage(buf, null);
	}

	public static <T> T paramList2Bean(Param[] paramList, Class<T> bean) {
		Map<String, String> map = paramList2Map(paramList);
		T ret = JsonUtil.map2Bean(map, bean);
		return ret;
	}

	public static Param[] bean2ParamList(Object bean) {
		Class<?> cla = bean.getClass();
		BeanInfo bi;
		try {
			bi = Introspector.getBeanInfo(cla);
		} catch (IntrospectionException e) {
			e.printStackTrace();
			return null;
		}
		PropertyDescriptor[] pd = bi.getPropertyDescriptors();
		List<Param> ret = new ArrayList<Param>();
		for (int i = 0; i < pd.length; i++) {
			if (pd[i].getWriteMethod() == null || pd[i].getReadMethod() == null) {
				continue;
			}
			try {
				String name = pd[i].getName();
				Object value = pd[i].getReadMethod().invoke(bean);
				ret.add(new Param(name, value == null ? null : value.toString()));
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return ret.toArray(new Param[ret.size()]);
	}

	public static String getParamValue(Param[] params, String name) {
		String value = null;
		for (Param p : params) {
			if (name.equals(p.getParamName())) {
				value = p.getParamValue();
			}
		}
		return value;
	}

	private static ByteBufAllocator allocator = new UnpooledByteBufAllocator(
			false);

	public static CommandMessage createReplyMessage(CommandMessage message) {
		CommandMessage replyMessage = new CommandMessage();
		replyMessage.setCabinetNo(message.getCabinetNo());
		replyMessage.setCommandType(CommandType.REQUEST_RESP);
		replyMessage.setFunctionCode(message.getFunctionCode());
		replyMessage.setSerialNumber(message.getSerialNumber());
		replyMessage.setParamList(null);

		return replyMessage;
	}
}
