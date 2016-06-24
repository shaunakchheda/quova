/*
 * Copyright 2007 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */

package com.hotwire.sid;


/**
 * Wrap any IP_Geo related exceptions with this class
 * <p/>
 * Possible errors:
 * <ul>
 * <li>IP_GeoServer is unavailable</li>
 * <li>Connection time out</li>
 * <li>Unknown IP_GeoServerAdapterType requested
 * </ul>
 *
 * @author Mike McClay
 * @since 9.04
 */
@SuppressWarnings("serial")
public class HwIP_GeoException extends Exception{

	// //////////////////////////////////////////////////////////////
	// CONSTRUCTORS

	/**
	 * Constructor to create a new HwIP_GeoException with an error code
	 *
	 * @param errorCode
	 * @param e
	 */
	public HwIP_GeoException(int errorCode, Exception e) {
		System.out.println("Error code : "  + errorCode);
		System.out.println(e.toString());
	}

	/**
	 * Constructor to create a new HwIP_GeoException with an error code
	 *
	 * @param errorCode
	 */
	public HwIP_GeoException(int errorCode) {
		System.out.println("Error code : " + errorCode);
	}
}
