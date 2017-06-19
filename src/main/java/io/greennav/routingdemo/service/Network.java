package io.greennav.routingdemo.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import crosby.binary.osmosis.OsmosisReader;
import io.greennav.routingdemo.entities.Edge;
import io.greennav.routingdemo.entities.Vertex;
import io.greennav.routingdemo.service.Routing.SearchTree;

@Repository
public class Network {

	@Autowired
	private Routing routing;

	/**
	 * A mapping from vertex ids to vertex objects, which contain coordinates.
	 */
	private Map<Long, Vertex> vertices = new HashMap<>();

	/**
	 * A multi map from vertices to outgoing edges, which have a length and a
	 * target vertex.
	 */
	private Multimap<Long, Edge> edges = MultimapBuilder.hashKeys().hashSetValues().build();

	/**
	 * The initializtion loads the PBF file. Then, compute the edge distances.
	 * Finally, compute the major component, because usually OSM data has a lot
	 * of smaller disconnected components due to the slicing process.
	 * 
	 * @throws Exception
	 *             should only be thrown, if file does not exists or is corrupt
	 */
	public void initialize() throws Exception {
		readOSMFile("test.osm.pbf");
		computeEdgeDistances();
		extractMajorComponent();
	}

	private void readOSMFile(String filename) throws FileNotFoundException {
		// On booting up, load the data from file
		File testFile = new File(filename);
		FileInputStream fis = new FileInputStream(testFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		OsmosisReader reader = new OsmosisReader(bis);
		// The sink serves as a callback, reacting on any nodes and ways found
		reader.setSink(new Sink() {

			@Override
			public void initialize(Map<String, Object> arg0) {
				// do nothing
			}

			@Override
			public void process(EntityContainer entityContainer) {
				Entity entity = entityContainer.getEntity();
				switch (entity.getType()) {
				case Node:
					Node node = (Node) entity;
					vertices.put(node.getId(), new Vertex(node.getLatitude(), node.getLongitude()));
					break;
				case Way:
					Way way = (Way) entity;
					List<WayNode> nodes = way.getWayNodes();
					for (int i = 1; i < nodes.size(); i++) {
						long from = nodes.get(i - 1).getNodeId();
						long to = nodes.get(i).getNodeId();
						edges.put(from, new Edge(Double.POSITIVE_INFINITY, to));
						edges.put(to, new Edge(Double.POSITIVE_INFINITY, from));
					}
					break;
				default:
					break;
				}

			}

			@Override
			public void complete() {
				// do nothing
			}

			@Override
			public void release() {
				// do nothing
			}

		});
		reader.run();
	}

	private void extractMajorComponent() {
		Set<Long> visitedVertices = new HashSet<>();
		Set<Long> majorComponent = null;
		int networkSize = edges.keySet().size();
		// Find major component
		for (long vertex : vertices.keySet()) {
			if (!visitedVertices.contains(vertex)) {
				// explore the search space of the current component
				SearchTree searchTree = routing.route(vertex, null);
				int searchSize = searchTree.getDistances().size();
				if (searchSize >= networkSize / 5) {
					// This is the major comp, because it has >20% of all
					// vertices, and this is a sufficient distinction
					majorComponent = searchTree.getDistances().keySet();
					break;
				}
				// mark component as visited, so we don't visit it again
				searchTree.getDistances().entrySet().stream()
						.filter(e -> e.getValue() < Double.POSITIVE_INFINITY)
						.map(e -> e.getKey())
						.forEach(key -> visitedVertices.add(key));
			}
		}
		if (majorComponent == null) {
			throw new RuntimeException("data does not have a major component, looks fishy");
		}
		// Filter vertices and edges
		vertices.keySet().retainAll(majorComponent);
		edges.keySet().retainAll(majorComponent);
	}

	private void computeEdgeDistances() {
		for (Long from : edges.keys()) {
			for (Edge edge : edges.get(from)) {
				edge.setDistance(computeDistance(vertices.get(from), vertices.get(edge.getTo())));
			}
		}
	}

	private Double computeDistance(Vertex from, Vertex to) {
		// TODO do some actual computation here
		return 1d;
	}

	/**
	 * Get the set of vertex IDs, that are successors to the given vertex.
	 * 
	 * @param vertex
	 * @return
	 */
	public Set<Long> getSuccessors(long vertex) {
		return edges.get(vertex).stream()
				.map(e -> e.getTo())
				.collect(Collectors.toSet());
	}

	/**
	 * Get the edge distance between vertex 'from' and vertex 'to'. If the
	 * vertices are not connected, the distance is set to positive infinity.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public double getEdgeDistance(long from, long to) {
		return edges.get(from).stream()
				.filter(e -> e.getTo() == to)
				.findAny()
				.map(e -> e.getDistance())
				.orElse(Double.POSITIVE_INFINITY);
	}

	/**
	 * Gets a random vertex id, which is quite inefficient, but it works
	 */
	public Long getRandomVertexId() {
		return new ArrayList<>(vertices.keySet()).get(new Random().nextInt(vertices.size()));
	}

	/**
	 * Get the coordinates of a particular vertex.
	 */
	public Vertex getVertex(Long vertexId) {
		return vertices.get(vertexId);
	}

}
