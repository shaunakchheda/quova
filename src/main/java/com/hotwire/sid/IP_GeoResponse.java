package com.hotwire.sid;

public class IP_GeoResponse {

	// //////////////////////////////////////////////////////////////
	// ATTRIBUTES

	/**
	 * The ipAddress that was used to create this IP_GeoResponse
	 */
	private String ipAddress;

	/**
	 * The source of this IP_GeoResponse (i.e. Quova - 'Q', Simulator - 'S',
	 * etc.)
	 */
	private char geoInfoSource = 'Q';

	/**
	 * Country code of the user
	 */
	private String countryCode;

	/**
	 * GMT offset of the user
	 */
	private float gmtOffset = Float.MAX_VALUE;

	/**
	 * ndma of the user
	 */
	private int ndmaCode;// = NDMA_JDO.INVALID_NDMA_CODE;

	/**
	 * ZIP code of the user
	 */
	private String zipCode;

	/**
	 * City name
	 */
	private String cityName;

	/**
	 * State name
	 */
	private String stateName;

	/**
	 * Latitude of the user
	 */
	private float latitude = Float.MAX_VALUE;

	/**
	 * Longitude of the user
	 */
	private float longitude = Float.MAX_VALUE;

	// //////////////////////////////////////////////////////////////
	// CONSTRUCTOR

	/**
	 * Create an instance of IP_GeoResponse
	 */
	public IP_GeoResponse(String ipAddress, char geoInfoSource) {
		this.ipAddress = ipAddress;
		this.geoInfoSource = geoInfoSource;
	}

	/**
	 * Copy constructor
	 *
	 * @param ipGeoResponse
	 */
	public IP_GeoResponse(IP_GeoResponse ipGeoResponse) {
		this(ipGeoResponse.getIP_Address(), ipGeoResponse.getGeoInfoSource());
		this.countryCode = ipGeoResponse.getCountryCode();
		this.gmtOffset = ipGeoResponse.getGmtOffset();
		this.ndmaCode = ipGeoResponse.getNDMA_Code();
		this.zipCode = ipGeoResponse.getZipCode();
		this.cityName = ipGeoResponse.getCityName();
		this.stateName = ipGeoResponse.getStateName();
	}

	// //////////////////////////////////////////////////////////////
	// METHODS

	/**
	 * Returns the ipAddress used to create this object
	 *
	 * @return ipAddress
	 */
	public String getIP_Address() {
		return this.ipAddress;
	}

	/**
	 * Returns the source used to create this object
	 *
	 * @return ipAddress
	 */
	public char getGeoInfoSource() {
		return this.geoInfoSource;
	}

	/**
	 * Return the countryCode of the user
	 *
	 * @return countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * Sets the countryCode of the user
	 *
	 * @param countryCode
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
		if (countryCode != null) {
			// if we are setting the country code to a non-null string, make
			// sure it is upper case
			this.countryCode = countryCode.toUpperCase();
		}
	}

	/**
	 * Return the gmtOffset of the user
	 *
	 * @return gmtOffset
	 */
	public float getGmtOffset() {
		return gmtOffset;
	}

	/**
	 * Sets the gmtOffset of the user
	 *
	 * @param gmtOffset
	 */
	public void setGmtOffset(float gmtOffset) {
		this.gmtOffset = gmtOffset;
	}

	/**
	 * Return the ndmaCode of the user
	 *
	 * @return ndmaCode
	 */
	public int getNDMA_Code() {
		return ndmaCode;
	}

	/**
	 * Sets the ndmaCode of the user
	 *
	 * @param ndmaCode
	 */
	public void setNDMA_Code(int ndmaCode) {
		this.ndmaCode = ndmaCode;
	}

	/**
	 * Return the zipCode of the user
	 *
	 * @return zipCode
	 */
	public String getZipCode() {
		return zipCode;
	}

	/**
	 * Sets the zipCode of the user
	 *
	 * @param zipCode
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	/**
	 * Return the cityName of the user
	 *
	 * @return cityName
	 */
	public String getCityName() {
		return cityName;
	}

	/**
	 * Sets the cityName of the user
	 *
	 * @param cityName
	 */
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	/**
	 * Return the stateName of the user
	 *
	 * @return stateName
	 */
	public String getStateName() {
		return stateName;
	}

	/**
	 * Sets the stateName of the user
	 *
	 * @param stateName
	 */
	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	/**
	 * Return the latitude of the user
	 *
	 * @return latitude
	 */
	public float getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude of the user
	 *
	 * @param latitude
	 */
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	/**
	 * Return the longitude of the user
	 *
	 * @return longitude
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude of the user
	 *
	 * @param longitude
	 */
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public String to_String() {
		return getCityName() + getCountryCode() + getStateName()
				+ getLatitude() + getLongitude() + getNDMA_Code();

	}

}
