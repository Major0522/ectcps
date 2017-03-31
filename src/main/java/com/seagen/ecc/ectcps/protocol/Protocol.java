package com.seagen.ecc.ectcps.protocol;

/**
 * 协议定义
 */
public interface Protocol {

	public interface ProtocolType {
		public static final byte STRING = 1;
		public static final byte STRING_COMPRESS = 2;
		public static final byte STRING_SSL = 4;
		public static final byte MCS = 16;
		public static final byte COMMAND = 32;
		public static final byte COMMAND_COMPRESS = 64;
		public static final byte COMMAND_SSL = 8;
		public static final byte COMMAND_SSL_SIMPLE = 9;
		public static final byte WEBSOCKET = 66;
		public static final byte MQ = 68;
	}

	public interface CharSetType {
		public static final byte UTF8 = 1;
		public static final byte GBK = 2;
	}

	public interface RoleType {
		public static final byte CABINET = 1;
		public static final byte REMOTECONTROL = 2;
	}

	public interface CommandType {

		/** 请求， 1 */
		public final int REQUEST = 1;

		/** 请求答复，2 */
		public final int REQUEST_RESP = 2;

		/** 上报，3 */
		public final int REPORT = 3;

		/** 上报答复，4 */
		public final int REPORT_RESP = 4;

		/** 心跳，5 */
		public final int HEARTBEAT = 5;

		/** 心跳答复，6 */
		public final int HEARTBEAT_RESP = 6;

	}

}
