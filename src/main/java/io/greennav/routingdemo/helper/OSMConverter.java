package io.greennav.routingdemo.helper;

import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

public abstract class OSMConverter implements Sink {

	public abstract void onVertex(long id, double lat, double lon);

	public abstract void onEdge(long from, long to);

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
			onVertex(node.getId(), node.getLatitude(), node.getLongitude());
			break;
		case Way:
			Way way = (Way) entity;
			List<WayNode> nodes = way.getWayNodes();
			for (int i = 1; i < nodes.size(); i++) {
				onEdge(nodes.get(i - 1).getNodeId(), nodes.get(i).getNodeId());
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

}
