package io.greennav.routingdemo.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.greennav.routingdemo.entities.Vertex;
import io.greennav.routingdemo.service.Network;
import io.greennav.routingdemo.service.Routing;
import io.greennav.routingdemo.service.Routing.SearchTree;

@RestController
@RequestMapping("/greennav")
public class Controller {

	@Autowired
	private Routing routing;

	@Autowired
	private Network network;

	@RequestMapping("/route")
	public String route(@RequestParam(required = false, value = "source") Long source,
			@RequestParam(required = false, value = "target") Long target) {

		if (source == null) {
			source = network.getRandomVertexId();
		}
		if (target == null) {
			target = network.getRandomVertexId();
		}

		SearchTree searchTree = routing.route(source, target);
		double distance = searchTree.getDistances().getOrDefault(target, Double.POSITIVE_INFINITY);

		String result = "From " + source + " to " + target + " it takes " + distance + " kWh.<br>";
		result += "Search tree: " + searchTree.getDistances().size();
		return result;
	}

	// Add more, for example lat/lon translation to vertices
	// Detailed route results to show lines on the client

	/**
	 * Compute range polygon
	 * @param source
	 * @param threshold
	 * @return
	 */
	@RequestMapping("/range")
	public List<Vertex> range(@RequestParam(required = false, value = "source") Long source,
			@RequestParam(required = false, value = "threshold", defaultValue = "100.0") Double threshold) {

		if (source == null) {
			source = network.getRandomVertexId();
		}

		// Super inefficient, because it always explore the complete graph
		SearchTree searchTree = routing.route(source, null);

		// === Extraction method: TODO create your own component for that ===

		Set<Long> searchSpace = searchTree.getDistances().entrySet().stream()
				.filter(e -> e.getValue() < threshold)
				.map(e -> e.getKey())
				.collect(Collectors.toSet());

		// Get the most distant vertex in each cardinal direction
		Vertex north = network.getVertex(source);
		Vertex south = network.getVertex(source);
		Vertex west = network.getVertex(source);
		Vertex east = network.getVertex(source);
		for (long vertexId : searchSpace) {
			Vertex current = network.getVertex(vertexId);
			if (current.getLat() > north.getLat()) {
				north = current;
			}
			if (current.getLat() < south.getLat()) {
				south = current;
			}
			if (current.getLon() < west.getLon()) {
				west = current;
			}
			if (current.getLon() > east.getLon()) {
				east = current;
			}
		}
		List<Vertex> result = new ArrayList<>();
		result.add(north);
		result.add(east);
		result.add(south);
		result.add(west);
		return result;
	}

}
