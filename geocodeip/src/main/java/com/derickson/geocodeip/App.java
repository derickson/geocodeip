package com.derickson.geocodeip;

import java.util.List;

/**
 * Hello world!
 * 
 */
public class App {
	public static void printLoc(final List<Double> loc) {
		System.out.println(String.format("Latitude: %f, Longitude: %f", loc
				.get(1).doubleValue(), loc.get(0).doubleValue()));
	}

	public static void main(String[] args) {
		System.out.println("Hello World!");
		GeoCodeIp geo = new GeoCodeIp();

		printLoc(geo.getLonLat("96.241.81.50"));

	}
}
