package io.greennav.routingdemo.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Routing {

	@Autowired
	private Network network;

	public static class SearchTree {
		private Map<Long, Long> predecessors = new HashMap<>();
		private Map<Long, Double> distances = new HashMap<>();

		public Map<Long, Long> getPredecessors() {
			return predecessors;
		}

		public Map<Long, Double> getDistances() {
			return distances;
		}
	}

	public SearchTree route(long source, Long target) {

		SearchTree t = new SearchTree();
		Map<Long, Long> p = t.getPredecessors();
		Map<Long, Double> d = t.getDistances();
		p.put(source, null);
		d.put(source, 0d);

		PriorityQueue<Long> q = new PriorityQueue<Long>(Comparator.comparing(vertex -> d.get(vertex)));
		q.add(source);

		while (!q.isEmpty()) {
			long current = q.poll();
			if (target != null && current == target) {
				break;
			}
			Set<Long> successors = network.getSuccessors(current);
			for (long next : successors) {
				double edgeDistance = network.getEdgeDistance(current, next);
				double currentDistance = d.get(current);
				double nextDistance = d.getOrDefault(next, Double.POSITIVE_INFINITY);
				if (currentDistance + edgeDistance < nextDistance) {
					d.put(next, currentDistance + edgeDistance);
					p.put(next, current);
					q.add(next);
				}
			}
		}

		return t;
	}

}
