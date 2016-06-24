/*
 * Copyright 2002 Hotwire. All Rights Reserved.
 *
 * This software is the proprietary information of Hotwire.
 * Use is subject to license terms.
 */

package com.hotwire.sid;

/**
 * This interface lists all the error codes available
 * <p>
 * <b>Persistence type:</b> none
 *
 * @author Cyril Bouteille
 * @version 2.0
 */
public interface HotwireErrors {

	// //////////////////////////////////////////////////////////////
	// ATTRIBUTES

	int IPGEO_INVALID_SERVER_ADAPTER_TYPE = 21700;
	int IPGEO_GEO_SERVER_NOT_AVAILABLE = 21701;
	int IPGEO_GEO_SERVER_INVALID_RESPONSE = 21702;
	int IPGEO_GEO_SERVER_INVALID_ARGUMENT = 21703;
	int IPGEO_GEO_SERVER_RUNTIME_ERROR = 21704;

}
