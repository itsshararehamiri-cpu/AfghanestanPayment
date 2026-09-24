package com.danesh.iso;

import org.jpos.iso.ISOException;

public class ISOExceptionUtil {

	public static boolean isIOException(ISOException e)

	{
		String errorMsg = e.getMessage();
		if ("unexpected exception".equals(errorMsg))
			return true;
		else if ("unconnected ISOChannel".equals(errorMsg))
			return true;
		return false;
	}

}
