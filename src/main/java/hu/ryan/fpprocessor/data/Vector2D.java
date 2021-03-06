package hu.ryan.fpprocessor.data;

import java.io.Serializable;

public class Vector2D implements Serializable  {

	private static final long serialVersionUID = 3644865883460495428L;
	private double x;
	private double y;

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getMagnitude() {
		return Math.sqrt((x * x) + (y * y));
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
}
