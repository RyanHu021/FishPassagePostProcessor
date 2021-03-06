package hu.ryan.fpprocessor.graphics;

import hu.ryan.fpprocessor.data.Condition;
import hu.ryan.fpprocessor.data.PropertyType;

public class RenderItem {

	Condition condition;
	int timestamp;
	PropertyType property;
	String title;

	public RenderItem(Condition condition, int timestamp, PropertyType property, String title) {
		this.condition = condition;
		this.timestamp = timestamp;
		this.property = property;
		this.title = title;
	}

	public RenderItem(Condition condition, int timestamp, PropertyType property) {
		this(condition, timestamp, property, null);
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		if (title != null) {
			result.append("Full Export: " + title);
		} else {
			result.append("Image Only: ").append(condition.getName()).append(", timestamp ").append(timestamp).append(", ")
					.append(property.getName());
		}
		return result.toString();
	}

	public Condition getCondition() {
		return condition;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public PropertyType getProperty() {
		return property;
	}

	public String getTitle() {
		return title;
	}
}
