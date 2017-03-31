package com.seagen.ecc.ectcps.protocol;

import java.io.Serializable;

public class Param implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4897079703482449445L;

	/**
	 * 参数名称
	 */
	private String paramName;

	/**
	 * 参数值
	 */
	private String paramValue;

	public Param() {
		super();
	}

	public Param(String paramName, String paramValue) {
		super();
		this.paramName = paramName;
		this.paramValue = paramValue;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Param [paramName=");
		builder.append(paramName);
		builder.append(", paramValue=");
		builder.append(paramValue);
		builder.append("]");
		return builder.toString();
	}

}