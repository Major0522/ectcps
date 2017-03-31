package com.seagen.ecc.ectcps.protocol;

import java.io.Serializable;
import java.util.Arrays;

import com.seagen.ecc.utils.DateUtils;

public class McsMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1380625114271927104L;
	public static final byte HEADER_FIRST = 0x10;

	/**
	 * 命令流水号，执行结果需要返回流水号，用于命令跟结果值的对应
	 */
	private long serialNumber;

	/**
	 * 柜体编号
	 */
	private long cabinetNo;

	/** 命令类型, 1 请求，2请求答复，3上报，4上报答复，5心跳，6心跳答复 */
	private int commandType;

	/**
	 * 模块名称
	 */
	private String moduleName;

	/**
	 * 操作码
	 */
	private int functionCode;

	/**
	 * 参数列表
	 */
	private Param[] paramList;

	public McsMessage() {
		this.serialNumber = DateUtils.getSerialNo();
		this.moduleName = "";
	}

	public McsMessage(int commandType) {
		this();
		this.commandType = commandType;
	}

	public Param[] getParamList() {
		return paramList;
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
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
		result = prime * result
				+ ((moduleName == null) ? 0 : moduleName.hashCode());
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
		McsMessage other = (McsMessage) obj;
		if (cabinetNo != other.cabinetNo)
			return false;
		if (commandType != other.commandType)
			return false;
		if (functionCode != other.functionCode)
			return false;
		if (moduleName == null) {
			if (other.moduleName != null)
				return false;
		} else if (!moduleName.equals(other.moduleName))
			return false;
		if (serialNumber != other.serialNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("McsMessage [serialNumber=");
		builder.append(serialNumber);
		builder.append(", cabinetNo=");
		builder.append(cabinetNo);
		builder.append(", commandType=");
		builder.append(commandType);
		builder.append(", moduleName=");
		builder.append(moduleName);
		builder.append(", functionCode=");
		builder.append(functionCode);
		builder.append(", paramList=");
		builder.append(Arrays.toString(paramList));
		builder.append("]");
		return builder.toString();
	}

}
