/**
 * 
 */
package com.seagen.ecc.ectcps.util;

import com.seagen.ecc.ectcps.protocol.Param;

/**
 * @author Bridge
 *
 */
public class ParamUtils {
	public static String getParamValue(Param[] params, String name) {
		String value = null;
		for (Param p : params) {
			if (name.equals(p.getParamName())) {
				value = p.getParamValue();
			}
		}
		return value;
	}
	
	public static int getParamValueAsInteger(Param[] params, String name) {
		String stringValue = getParamValue(params, name);
		return Integer.parseInt(stringValue);
	}
}
