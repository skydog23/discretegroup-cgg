package discreteGroup.wallpaper;

import de.jreality.geometry.PointSetFactory;

/**
 * An abstract class designed for handling common tasks related to bouncing a point around
 * inside a 2D polygon.
 * <p>
 * Notice that all points and vectors are restricted to being 2-vectors, to remove any confusion
 * about what kind of coordinates are being used.
 * <p>
 * If you use homogeneous coordinates to do your calculations in a subclass, then be sure to copy
 * the final result into the fields <i>point</i> and <i>direction</i> so that the calling 
 * classes get updated properly.  
 * <p>
 * If you do work in homogeneous coordinates, notice you also have a choice whether to do so
 * in 2-dimensions (where a point looks like (x,y,1)) or in 3-dimensions (where a point of the z=0 plane 
 * looks like (x,y,0,1)). The latter choice has the advantage that there are more methods available
 * in the class {@link de.jreality.math.P3} for calculating reflection matrices, etc., which may make
 * your life easier.
 * @author Charles Gunn
 *
 */
public abstract class AbstractBouncer implements Bouncer {
	protected double[] point = new double[2];
	protected double[] direction = new double[2];
	protected PointSetFactory psf = new PointSetFactory();
	protected double speed = .01;
	
	abstract public void update();
	
	public double[] getDirection() {
		return direction;
	}

	public void setDirection(double[] direction) {
		if (direction.length != 2)
			throw new IllegalArgumentException("Must be a 2-vector");
		this.direction = direction;
	}

	public double[] getPoint()	{
		return point;
	}
	
	public void setPoint(double[] p) {
		if (p.length != 2)
			throw new IllegalArgumentException("Must be a 2-vector");
		point=p;
	}
	
	public void setSpeed(double s)	{
		speed = s;
	}
	
	public double getSpeed()	{
		return speed;
	}

	public PointSetFactory getPointSetFactory()	{
		return psf;
	}
	

}
