package hu.ryan.fpprocessor.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable  {

	private static final long serialVersionUID = -8633272832359713764L;
	private int id;
	private double x;
	private double y;
	private double z;
	private int timestampsSize;
	
	// [change to custom array or linked list implementation]
	private List<Timestamp> timestamps;

	public Node(int id, double x, double y, double z) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.z = z;
		timestamps = new ArrayList<Timestamp>();
		timestampsSize = 0;
	}

	public String toString() {
		StringBuilder result = new StringBuilder("Node: ");
		result.append(id).append(" (").append(x).append(", ").append(y).append(", ").append(z).append(")");
		return result.toString();
	}
	
	public int getID() {
		return id;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public List<Timestamp> getTimestamps() {
		return this.timestamps;
	}

	public Timestamp getTimestamp(int index) {
		if (index < 0 || index >= timestamps.size()) {
			return null;
		}
		return timestamps.get(index);
	}

	public void addTimestamp(Timestamp timestamp) {
		timestamps.add(timestamp);
		timestampsSize++;
	}
	
	public int getTimestampsSize() {
		return timestampsSize;
	}
}
