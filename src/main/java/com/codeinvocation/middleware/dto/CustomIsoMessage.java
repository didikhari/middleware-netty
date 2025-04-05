package com.codeinvocation.middleware.dto;

import java.util.Date;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoValue;

public class CustomIsoMessage extends IsoMessage {
	
	public CustomIsoMessage(String header) {
		super(header);
	}
	
	public CustomIsoMessage() {
		super();
	}
	
	public String getString(int bitNo) {
		Object result = this.getObjectValue(bitNo);
		if (result == null)
			return null;
		
		if (result instanceof String)
			return (String) result;
		else
			return result.toString();
	}
	
	public Integer getInteger(int bitNo) {
		Object result = this.getObjectValue(bitNo);
		if (result == null)
			return null;
		
		if (result instanceof Integer)
			return (Integer) result;
		else
			return Integer.valueOf(result.toString());
	}
	
	public String dumpField(boolean incoming, String connectionId, String remoteAddress,
			Long timestamp) {
		String direction = incoming ? "INCOMING":"OUTGOING";
        StringBuilder sb = new StringBuilder("\n");
        sb.append("====="+direction+" ISOMSG=====\n");
        sb.append("CONNECTION ID\t"+connectionId);
        sb.append("\n");
        sb.append("REMOTE ADDRESS\t"+remoteAddress);
        sb.append("\n");
        sb.append("TIMESTAMP\t"+new Date(timestamp));
        sb.append("\n");
        sb.append(String.format("MTI\t[%04x]", getType()));
        sb.append("\n");

        //Fields
        for (int i = 2; i < 129; i++) {
            IsoValue<?> v = getField(i);
            if (v != null) {
            	sb.append("DE-"+i);
                String desc = v.toString();
                sb.append("\t[");
                sb.append(desc);
                sb.append("]\n");
            }
        }

        sb.append("====="+direction+" ISOMSG=====\n");
        return sb.toString();
    
	}
}
