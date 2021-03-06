package hu.ryan.fpprocessor.data;

public enum PropertyType {
	SHEAR_STRESS("Shear Stress", "lbs/sq ft"), VELOCITY("Velocity Magnitude", "ft/s"), DEPTH("Flow Depth", "ft"), WSEL("Water Surface Elevation", "ft NAVD88");

	String name;
	String unit;

	private PropertyType(String name, String unit) {
		this.name = name;
		this.unit = unit;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUnit() {
		return unit;
	}
}
