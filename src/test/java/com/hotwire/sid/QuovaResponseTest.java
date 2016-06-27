package com.hotwire.sid;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class QuovaResponseTest {

	private static QuovaIP_GeoServerAdapter q;
	private static AppProperties app;
	private static Properties props;
	private IP_GeoResponse response;
	private static HashMap<String, ArrayList<String>> testcases;
	private static List<String> ips;
	private static List<List<String>> values;
	
	@BeforeClass
	public static void init() throws IOException {
		app = new AppProperties();
		props = app.readProperties();
		q = new QuovaIP_GeoServerAdapter();
		q.setHwProps(props);
		q.configurationChanged();
		testcases = new HashMap<>();
		
		ips = new ArrayList<>();
		values = new ArrayList<List<String>>();
		
		ips.add("68.184.77.220");
		String[] vals = {"montgomery", "al", "US", "36107", "Q", "-6.0", "68.184.77.220", "32.38316", "-86.28197", "698" };
		ArrayList<String> v = new ArrayList<>();
		v.addAll(Arrays.asList(vals));
		values.add(v);
		
		ips.add("192.172.150.56");
		vals = new String[]{"columbus", "oh", "US", "43218", "Q", "-5.0", "192.172.150.56", "39.99558", "-82.99946", "535"};
		v = new ArrayList<String>();
		v.addAll(Arrays.asList(vals));
		values.add(v);
		
		for(int i=0; i<ips.size(); i++) {
			testcases.put(ips.get(i), (ArrayList<String>) values.get(i));
		}
	}
	
	@Test
	public void testResponse(){
		
		for(int i=0; i<testcases.size(); i++) {
			try {
				response = q.lookup(ips.get(i));
			} catch (HwIP_GeoException e) {
				fail("Got HwIP_GeoException");
			}
			ArrayList<String> expected = testcases.get(ips.get(i));
			assertEquals(expected.get(0), response.getCityName());
			assertEquals(expected.get(1), response.getStateName());
			assertEquals(expected.get(2), response.getCountryCode());
			assertEquals(expected.get(3), response.getZipCode());
			assertEquals(expected.get(4), String.valueOf(response.getGeoInfoSource()));
			assertEquals(expected.get(5), String.valueOf(response.getGmtOffset()));
			assertEquals(expected.get(6), response.getIP_Address());
			assertEquals(expected.get(7), String.valueOf(response.getLatitude()));
			assertEquals(expected.get(8), String.valueOf(response.getLongitude()));
			assertEquals(expected.get(9), String.valueOf(response.getNDMA_Code()));

		}
	}
	
	@Test(expected = HwIP_GeoException.class)
	public void testEmptyIP() throws HwIP_GeoException {
		q.lookup("");
	}
	
	@Test(expected = HwIP_GeoException.class)
	public void testInvalidIP() throws HwIP_GeoException {
		q.lookup("1.2.a.3");
	}
	
	@Test
	public void testNullResponse() {
		try {
			response = q.lookup("172.17.29.112");
		} catch (HwIP_GeoException e) {
			fail("Got HwIP_GeoException");
		}
		Assert.assertNull(response.getCityName());
		Assert.assertNull(response.getStateName());
		Assert.assertNull(response.getCountryCode());
		Assert.assertNull(response.getZipCode());
	}
}
