package fpprocessor.data;

import java.io.Serializable;

public class Timestamp implements Serializable  {
	
	private static final long serialVersionUID = 3326856075633280905L;
	private double depth;
	private double wSEL;
	private double shearStress;
	private Vector2D velocity;
	
	public double getDepth() {
		return this.depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}
	
	public double getWSEL() {
		return wSEL;
	}

	public void setWSEL(double wSEL) {
		this.wSEL = wSEL;
	}

	public double getShearStress() {
		return shearStress;
	}

	public void setShearStress(double shearStress) {
		this.shearStress = shearStress;
	}

	public Vector2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2D velocity) {
		this.velocity = velocity;
	}
}
