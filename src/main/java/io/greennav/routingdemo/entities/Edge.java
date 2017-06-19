package io.greennav.routingdemo.entities;

public class Edge {

	private double distance;
	private long to;

	public Edge(double distance, long to) {
		this.distance = distance;
		this.to = to;
	}

	public double getDistance() {
		return distance;
	}

	public long getTo() {
		return to;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

}