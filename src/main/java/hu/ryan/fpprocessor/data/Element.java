package hu.ryan.fpprocessor.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Element implements Serializable {
	
	private static final long serialVersionUID = 6895730322948383516L;
	private List<Node> nodes;
	
	public Element() {
		nodes = new ArrayList<Node>();
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("Element: ");
		result.append(nodes.toString());
		return result.toString();
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public void addNode(Node node) {
		nodes.add(node);
	}
}