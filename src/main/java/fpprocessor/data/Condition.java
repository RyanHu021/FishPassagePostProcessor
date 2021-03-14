package fpprocessor.data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Condition implements Serializable {
	
	private static final long serialVersionUID = -856630095346247511L;
	private List<Node> nodes;
	private List<Element> elements;
	
	private String name;
	private int nodesSize;
	private int elementsSize;
	private File mapImage;
	private List<Double> georefData;
	private boolean hasImage;
	
	public Condition() {
		nodes = new ArrayList<Node>();
		elements = new ArrayList<Element>();
		nodesSize = 0;
		elementsSize = 0;
		hasImage = false;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("Condition: ");
		for (Element e : elements) {
			result.append(e.toString()).append("\n");
		}
		return result.toString();
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public Node getNode(int index) {
		if (index < 0 || index >= nodes.size()) {
			return null;
		}
		return nodes.get(index);
	}
	
	public void addNode(Node node) {
		nodesSize++;
		nodes.add(node);
	}
	
	public int getNodesSize() {
		return nodesSize;
	}
	
	public List<Element> getElements() {
		return elements;
	}
	
	public Element getElement(int index) {
		if (index < 0 || index >= elements.size()) {
			return null;
		}
		return elements.get(index);
	}
	
	public void addElement(Element element) {
		elementsSize++;
		elements.add(element);
	}

	public int getElementsSize() {
		return elementsSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getMapImage() {
		return mapImage;
	}

	public void setMapImage(File mapImage) {
		this.mapImage = mapImage;
		hasImage = true;
	}

	public List<Double> getGeorefData() {
		return georefData;
	}
	
	public void setGeorefData(List<Double> georefData) {
		this.georefData = georefData;
	}

	public boolean hasImage() {
		return hasImage;
	}
}