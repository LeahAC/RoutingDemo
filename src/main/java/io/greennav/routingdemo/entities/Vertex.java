package io.greennav.routingdemo.entities;

public class Vertex {

	private double lat;
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
