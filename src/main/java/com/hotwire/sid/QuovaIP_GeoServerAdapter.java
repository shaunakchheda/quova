package com.hotwire.sid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.slf4j.*;

import org.apache.commons.lang.StringUtils;

import com.quova.common.IPInfoField;
import com.quova.common.QuovaReturnCode;
import com.quova.geodirectoryapi.GeoDirectoryAPI;
import com.quova.geodirectoryapi.IPInfo;
import com.quova.geodirectoryapi.QuovaResponse;
import com.quova.util.QuovaException;

/**
 * Adapter used to connect to the Quova GeoServer
 *
 * @author Mike McClay <mmcclay@hotwire.com />
 * @since 9.04
 */
public class QuovaIP_GeoServerAdapter{

	// ///////////////// ATTRIBUTES /////////////////////////

	/**
	 * Logger
	 */
	private static final Logger log = Logger
			.getLogger(QuovaIP_GeoServerAdapter.class.getName());

	/**
	 * Holds the value of the confidence factor threshold that must be exceeded
	 * to trust Quova IP Geo data.
	 */
	private int cityConfidenceFactorThreshold = 0;

	/**
	 * IPAddress to use when querying Quova, if it is specified in the property
	 * hotwire.eis.geo.quova.ipAddressOverride If this value is not empty, then
	 * the adapter will use this ip for all queries to quova ignoring the
	 * ipAddress passed to the adapter's lookup methods
	 * <p/>
	 * THIS IS FOR NON-PROD ENVIRONMENTS ONLY - IT SHOULD NOT BE USED IN
	 * PRODUCTION
	 */
	private String ipAddressOverride;

	/**
	 * Holds the connection to a Quova GeoServer
	 */
	private GeoDirectoryAPI geoDirectoryConnection;

	// protected transient HwProperties hwProps;
	protected transient Properties hwProps;

	// //////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Method takes an ipAddress as input and retrieves the following
	 * geographical information: countryCode, zipCode (if city confidence
	 * threshold exceeded), NDMA Code, and Gmt Offset
	 *
	 * @param ipAddress
	 * @return an IP_GeoResponse object
	 * @throws HwIP_GeoException 
	 */
	public IP_GeoResponse lookup(String ipAddress) throws HwIP_GeoException{
		IPInfo ipInfoRecord = getIP_InfoRecord(ipAddress);
		return getIP_GeoResponseFromIPInfoRecord(ipInfoRecord);
	}

	/**
	 * Queries Quova with ipAddress and returns the IPInfo record that Quova
	 * returns, null if the call raises an exception
	 *
	 * @param ipAddress
	 * @return IPInfo record returned by Quova, null if the IP was not found or
	 *         an exception was thrown
	 */
	private IPInfo getIP_InfoRecord(String ipAddress) throws HwIP_GeoException {
		IPInfo ipInfoRecord = null;

		// if the ipAddressOverride property is not null, then use it to query
		// for geo information
		if (!StringUtils.isEmpty(ipAddressOverride)) {
			ipAddress = ipAddressOverride;
		}

		// throw exception if ipAddress is empty (Quova throws does not allow an
		// empty IP as an arg)
		if (StringUtils.isEmpty(ipAddress)) {
			log.warning("IP Address passed to Quova GeoServer was empty");
			throw new HwIP_GeoException(
					HotwireErrors.IPGEO_GEO_SERVER_INVALID_ARGUMENT);
		}

		if (geoDirectoryConnection != null) {
			QuovaResponse quovaResponse = null;
			long startTimeMillis = System.currentTimeMillis();
			try {
				// Query Quova adapter for geo information
				quovaResponse = geoDirectoryConnection.getIPInfo(ipAddress);

				// log elapsed query time
				if (log.isLoggable(Level.FINE)) {
					log.fine("Successful Quova query for ip " + ipAddress
							+ " took "
							+ (System.currentTimeMillis() - startTimeMillis)
							+ " milliseconds");
				}
			} catch (QuovaException qe) {
				long errorQueryTime = System.currentTimeMillis()
						- startTimeMillis;
				boolean isRecoverableError = false;
				QuovaReturnCode quovaExceptionReturnCode = qe.getErrorCode();
				if (QuovaReturnCode.INVALID_INPUT
						.equals(quovaExceptionReturnCode)
						|| QuovaReturnCode.TIMEOUT
								.equals(quovaExceptionReturnCode)) {
					// recoverable, log but don't null connection
					isRecoverableError = true;
				} else {
					if (QuovaReturnCode.POOL_FAILURE
							.equals(quovaExceptionReturnCode)) {
						// recoverable, but pool tuning is needed to handle
						// load, syslog but don't null connection
						// if this happens it should be only for a short time,
						// so it won't spam syslogs
						isRecoverableError = true;
					} else {
						// unrecoverable error such as invalid license, server
						// down etc
						// null the connection so that the app does not keep
						// querying the broken geo server
						// syslog so that the server can be brought back up
						geoDirectoryConnection = null;
					}
					// HwLogger.syslog(SyslogFunctionalAreas.AREA_QUOVA,
					// "Exception querying Quova - exception code: " +
					// quovaExceptionReturnCode);
				}
				String errorTypeMessage = isRecoverableError ? "Recoverable error"
						: "Unrecoverable error";
				log.log(Level.SEVERE, errorTypeMessage
						+ " encountered querying Quova - exception code: "
						+ quovaExceptionReturnCode + ". Query took "
						+ errorQueryTime + " milliseconds.", qe);
				throw new HwIP_GeoException(
						HotwireErrors.IPGEO_GEO_SERVER_NOT_AVAILABLE, qe);
			} catch (Throwable t) {
				// THIS SHOULD NEVER HAPPEN, however we catch it here so it does
				// not crash the server
				// null the connection so that the app does not query the broken
				// geo server
				geoDirectoryConnection = null;
				// HwLogger.syslog(SyslogFunctionalAreas.AREA_QUOVA,
				// "Runtime error encountered querying Geo Server");
				log.severe("Runtime error encountered querying Geo Server: "
						+ t);
				throw new HwIP_GeoException(
						HotwireErrors.IPGEO_GEO_SERVER_RUNTIME_ERROR,
						new Exception(t));
			}

			// Quova supports batch ipLookups, since we only sent 1 ipAddress,
			// we want the record at index 0
			ipInfoRecord = quovaResponse.getIPInfo(0);

			// sanity check the return code
			if (!ipInfoRecord.getRetCode().equals(QuovaReturnCode.NOT_FOUND)
					&& !ipInfoRecord.getRetCode().equals(
							QuovaReturnCode.SUCCESS)) {
				// we received an info record with an invalid return code, log
				// it
				log.severe("Quova returned a response with an error return code: "
						+ ipInfoRecord.getRetCode());
				throw new HwIP_GeoException(
						HotwireErrors.IPGEO_GEO_SERVER_INVALID_RESPONSE);
			}
		} else {
			log.warning("Quova Geo Server connection is null, geo server query will be skipped");
			throw new HwIP_GeoException(
					HotwireErrors.IPGEO_GEO_SERVER_NOT_AVAILABLE);
		}

		return ipInfoRecord;
	}

	/**
	 * Helper method that takes an ipInfoRecord as input and populates an
	 * IP_GeoResponse with the record It currently sets the countryCode,
	 * gmtOffset, ndmaCode and zipCode if cityConfidenceFactor is >=
	 * cityConfidenceFactorThreshold
	 *
	 * @param ipInfoRecord
	 * @return IP_GeoResponse
	 */
	private IP_GeoResponse getIP_GeoResponseFromIPInfoRecord(IPInfo ipInfoRecord) {
		IP_GeoResponse ipGeoResponse = new IP_GeoResponse(
				ipInfoRecord.getString(IPInfoField.IP_ADDRESS),
				'Q');
		// set country code
		ipGeoResponse.setCountryCode(ipInfoRecord
				.getString(IPInfoField.COUNTRY));

		// parse GMT
		try {
			Float gmtOffset = ipInfoRecord.getFloat(IPInfoField.TIMEZONE);
			if (gmtOffset != null) {
				ipGeoResponse.setGmtOffset(gmtOffset.floatValue());
			}
		} catch (QuovaException qe) {
			// this is expected since gmtOffset sometimes comes back as
			// 'multizone' so ignore this
			if (log.isLoggable(Level.FINE)) {
				log.fine("Could not parse IPInfoField.TIMEZONE for ip: "
						+ ipGeoResponse.getIP_Address() + ". "
						+ "String value of field is: "
						+ ipInfoRecord.getString(IPInfoField.TIMEZONE));
			}
		}

		// parse NDMA code
		try {
			Integer ndmaCode = ipInfoRecord.getInt(IPInfoField.DMA);
			if (ndmaCode != null) {
				ipGeoResponse.setNDMA_Code(ndmaCode.intValue());
			}
		} catch (QuovaException qe) {
			log.log(Level.SEVERE,
					"Could not parse IPInfoField.DMA for ip: "
							+ ipGeoResponse.getIP_Address() + ". "
							+ "String value of field is: "
							+ ipInfoRecord.getString(IPInfoField.DMA), qe);
		}

		// evaluate the city confidence factor and if it is >=
		// cityConfidenceFactorThreshold, set the zip code
		Integer cityConfidenceFactor = ipInfoRecord.getCF(IPInfoField.CITY);
		if (cityConfidenceFactor != null
				&& cityConfidenceFactor.intValue() >= cityConfidenceFactorThreshold) {
			ipGeoResponse.setZipCode(ipInfoRecord.getString(IPInfoField.ZIP));
			ipGeoResponse.setCityName(ipInfoRecord.getString(IPInfoField.CITY));
			ipGeoResponse.setStateName(ipInfoRecord
					.getString(IPInfoField.STATE));
		}

		// parse other numeric fields
		try {
			Float longitude = ipInfoRecord.getFloat(IPInfoField.LONGITUDE);
			if (longitude != null) {
				ipGeoResponse.setLongitude(longitude.floatValue());
			}

			Float latitude = ipInfoRecord.getFloat(IPInfoField.LATITUDE);
			if (latitude != null) {
				ipGeoResponse.setLatitude(latitude.floatValue());
			}
		} catch (QuovaException qe) {
			log.log(Level.SEVERE,
					"Could not parse latitude / longitude for ip: "
							+ ipGeoResponse.getIP_Address() + ". "
							+ "String value of latitude is: "
							+ ipInfoRecord.getString(IPInfoField.LATITUDE)
							+ ". " + "String value of longitude is: "
							+ ipInfoRecord.getString(IPInfoField.LONGITUDE)
							+ ".", qe);
		}
		return ipGeoResponse;
	}

	/**
	 * Called to refresh properties and Geo Directory Server connection
	 */
	public void configurationChanged() {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Initializing QuovaIP_GeoServerAdapter");
		}

		String geoDirectoryServerConnectionPoolSize = null;
		String geoDirectoryServerClientTimeout = null;
		String geoDirectoryServerPrimaryServerIp = null;
		String geoDirectoryServerPrimaryServerPort = null;
		String geoDirectoryServerSecondaryServerIp = null;
		String geoDirectoryServerSecondaryServerPort = null;

		try {
			String hegq = "hotwire.eis.geo.quova.";

			// read attribute properties

			// read general Quova settings
			geoDirectoryServerConnectionPoolSize = hwProps.getProperty(hegq
					+ "geoDirectoryServerConnectionPoolSize");
			geoDirectoryServerClientTimeout = hwProps.getProperty(hegq
					+ "geoDirectoryServerClientTimeoutInMillis");

			// read primary Quova server settings
			geoDirectoryServerPrimaryServerIp = hwProps.getProperty(hegq
					+ "geoDirectoryServerPrimaryServerIp");
			geoDirectoryServerPrimaryServerPort = hwProps.getProperty(hegq
					+ "geoDirectoryServerPrimaryServerPort");

			// read secondary Quova server settings, if any
			geoDirectoryServerSecondaryServerIp = hwProps.getProperty(hegq
					+ "geoDirectoryServerSecondaryServerIp");
			geoDirectoryServerSecondaryServerPort = hwProps.getProperty(hegq
					+ "geoDirectoryServerSecondaryServerPort");

		} catch (Exception e) {
			String errorMessage = "Encountered runtime exception reading properties: "
					+ e;
			// HwLogger.syslog(SyslogFunctionalAreas.AREA_QUOVA, errorMessage);
			log.log(Level.SEVERE, errorMessage, e);
			return;
		}

		// check that we have all of the required Quova settings
		if (StringUtils.isEmpty(geoDirectoryServerConnectionPoolSize)
				|| StringUtils.isEmpty(geoDirectoryServerClientTimeout)
				|| StringUtils.isEmpty(geoDirectoryServerPrimaryServerIp)
				|| StringUtils.isEmpty(geoDirectoryServerPrimaryServerPort)) {

			// we dont have all the settings and therefore can't create the
			// Quova client, log this
			String errorMessage = "One or more Quova settings are not available, connection will not be made:"
					+ "\ngeoDirectoryServerConnectionPoolSize: "
					+ geoDirectoryServerConnectionPoolSize
					+ "\ngeoDirectoryServerClientTimeoutInMillis: "
					+ geoDirectoryServerClientTimeout
					+ "\ngeoDirectoryServerPrimaryServerIp: "
					+ geoDirectoryServerPrimaryServerIp
					+ "\ngeoDirectoryServerPrimaryServerPort: "
					+ geoDirectoryServerPrimaryServerPort;

			// Logger.log(Level.SEVERE,errorMessage);
			log.severe(errorMessage);
			return;
		}

		// create the geoDirectoryServerProperties object with the read
		// properties
		Properties geoDirectoryServerProperties = new Properties();
		geoDirectoryServerProperties.setProperty("CONNECTION_POOL_SIZE",
				geoDirectoryServerConnectionPoolSize);
		geoDirectoryServerProperties.setProperty("USER_TIMEOUT",
				geoDirectoryServerClientTimeout);
		geoDirectoryServerProperties.setProperty("PRIMARY_SERVER_IP",
				geoDirectoryServerPrimaryServerIp);
		geoDirectoryServerProperties.setProperty("PRIMARY_SERVER_PORT",
				geoDirectoryServerPrimaryServerPort);

		// configure a secondary server if both the secondary server ip and port
		// have been specified in the props
		if (!StringUtils.isEmpty(geoDirectoryServerSecondaryServerIp)
				&& !StringUtils.isEmpty(geoDirectoryServerSecondaryServerPort)) {
			geoDirectoryServerProperties.setProperty("SEC_SERVER_IP",
					geoDirectoryServerSecondaryServerIp);
			geoDirectoryServerProperties.put("SEC_SERVER_PORT",
					geoDirectoryServerSecondaryServerPort);
		}

		// configure the hard-coded properties
		geoDirectoryServerProperties.setProperty("NETWORK_PROTOCOL", "TCP");

		// MAX_RETRY - how many times the quova client retries querying a
		// server, before it considers it unavailable
		geoDirectoryServerProperties.setProperty("MAX_RETRY", "1");

		// MAX_WAIT_BLOCKING - time in millis that a request will block while
		// waiting for a connection to the server
		geoDirectoryServerProperties.setProperty("MAX_WAIT_BLOCKING", "1");

		GeoDirectoryAPI oldGeoDirectoryConnection = null;
		try {
			// get a reference to the existing connection (if any)
			// so that we can close the old connection when we create the new
			// one
			oldGeoDirectoryConnection = geoDirectoryConnection;

			// get a new connection to the GeoDirectoryAPI
			geoDirectoryConnection = new GeoDirectoryAPI(
					geoDirectoryServerProperties);
		} catch (QuovaException qe) {
			// log the error
			String errorMessage = "GeoDirectoryAPI creation failed, unable to get new connection"
					+ qe;
			// HwLogger.syslog(SyslogFunctionalAreas.AREA_QUOVA, errorMessage);
			log.log(Level.SEVERE, errorMessage, qe);
		} finally {
			// close the old connection if there was one
			if (oldGeoDirectoryConnection != null) {
				try {
					oldGeoDirectoryConnection.close();
				} catch (QuovaException qe) {
					log.log(Level.SEVERE,
							"Exception closing old GeoDirectoryAPI connection after refresh "
									+ qe, qe);
				}
			}
		}
	}

	public void setHwProps(Properties hwProps) {
		this.hwProps = hwProps;
	}

	public static void main(String[] args) throws HwIP_GeoException,
			IOException {
		AppProperties app = new AppProperties();
		Properties prop = app.readProperties();

		QuovaIP_GeoServerAdapter q = new QuovaIP_GeoServerAdapter();
		q.setHwProps(prop);
		q.configurationChanged();
		IP_GeoResponse s = q.lookup("68.184.77.220");
		System.out.println(s.to_String());
		q.printDetails(s);
		
		List<String> ips = new ArrayList<>();
		ips.add("172.17.29.112");
		ips.add("192.172.150.56");
		for(String ip : ips) {
			IP_GeoResponse r = q.lookup(ip);
			System.out.println("---------------------------------------------------");
			System.out.println(ip);
			q.printDetails(r);
		}
	}
	
	public void printDetails(IP_GeoResponse response) {
		System.out.println("City : " + response.getCityName());
		System.out.println("State : " + response.getStateName());
		System.out.println("Country Code : " + response.getCountryCode());
		System.out.println("Zip code : " + response.getZipCode());
		System.out.println("Geo Info Source : " + response.getGeoInfoSource());
		System.out.println("GMT Offset : " + response.getGmtOffset());
		System.out.println("IP address : " + response.getIP_Address());
		System.out.println("Latitude : " + response.getLatitude());
		System.out.println("Longitude : " + response.getLongitude());
		System.out.println("NDMA Code : " + response.getNDMA_Code());
		
	}
}
