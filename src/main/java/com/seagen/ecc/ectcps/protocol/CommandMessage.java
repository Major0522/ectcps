package com.seagen.ecc.ectcps.protocol;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import com.seagen.ecc.ectcps.util.MessageUtils;
import com.seagen.ecc.utils.DateUtils;
import com.seagen.ecc.utils.JsonUtil;

/**
 * 命令信息结构
 */
public class CommandMessage implements Serializable {

	private static final long serialVersionUID = 839356762460850016L;
	private long serialNumber;// 命令流水号，执行结果需要返回流水号，用于命令跟结果值的对应
	private long cabinetNo; // 通信对方的编号
	private int commandType;// 命令类型, 1 请求，2请求答复，3上报，4上报答复，5心跳，6心跳答复
	private int functionCode;// 操作码
	private Param[] paramList;// 参数列表

	/** 添加map简单方法, 兼容前面的版本,内部使用,不要添加get,set */
	private transient Map<String, String> map;

	public void put(String key, Object value) {
		if (map == null) {
			map = MessageUtils.paramList2Map(paramList);
		}
		map.put(key, value == null ? null : value.toString());
	}

	public void putAll(Map<String, String> map) {
		for (Map.Entry<String, String> e : map.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}

	public String get(String key) {
		if (map == null) {
			map = MessageUtils.paramList2Map(paramList);
		}
		return map.get(key);
	}

	public Param[] getParamList() {
		if (paramList == null && map != null) {
			paramList = MessageUtils.map2ParamList(map);
		}
		return paramList;
	}

	/***********************/

	public CommandMessage() {
		this.serialNumber = DateUtils.getSerialNo();
		this.functionCode = 0;
	}

	public CommandMessage(long cabinetNo, int commandType) {
		this();
		this.cabinetNo = cabinetNo;
		this.commandType = commandType;
	}

	public CommandMessage(long cabinetNo, int commandType, int functionCode) {
		this(cabinetNo, commandType);
		this.functionCode = functionCode;
	}

	public void setParamList(Param[] paramList) {
		this.paramList = paramList;
	}

	public int getFunctionCode() {
		return functionCode;
	}

	public void setFunctionCode(int functionCode) {
		this.functionCode = functionCode;
	}

	public long getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(long cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public long getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(long serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * 命令类型, 1 请求，2请求答复，3上报，4上报答复，5心跳，6心跳答复
	 */
	public int getCommandType() {
		return commandType;
	}

	public void setCommandType(int commandType) {
		this.commandType = commandType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cabinetNo ^ (cabinetNo >>> 32));
		result = prime * result + commandType;
		result = prime * result + functionCode;
		result = prime * result + (int) (serialNumber ^ (serialNumber >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandMessage other = (CommandMessage) obj;
		if (cabinetNo != other.cabinetNo)
			return false;
		if (commandType != other.commandType)
			return false;
		if (functionCode != other.functionCode)
			return false;
		if (serialNumber != other.serialNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommandMessage [serialNumber=");
		builder.append(serialNumber);
		builder.append(", cabinetNo=");
		builder.append(cabinetNo);
		builder.append(", commandType=");
		builder.append(commandType);
		builder.append(", functionCode=");
		builder.append(functionCode);
		builder.append(", paramList=");
		builder.append(Arrays.toString(getParamList()));
		builder.append("]");
		return builder.toString();
	}

	public static void main(String[] args) {
		CommandMessage cm = new CommandMessage();
		// cm.setParamList(new Param[] { new Param("a", "b"), new Param("d",
		// "e") });
		cm.put("c", "d");
		cm.put("e", "f");
		System.out.println(cm);
		System.out.println(JsonUtil.ojbToJsonPrettyStr(cm));
	}
}