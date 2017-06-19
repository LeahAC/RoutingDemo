package io.greennav.routingdemo.entities;

public class Vertex {

	/**
	 * Latitude.
	 */
	private double lat;

	/**
	 * Longitude.
	 */
	private double lon;

	public Vertex(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

}
