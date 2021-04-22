package discreteGroup.wallpaper;

import de.jreality.geometry.PointSetFactory;

public interface Bouncer {

	public void setPoint(double[] p);	
	public double[] getPoint();	
	public void setDirection(double[] d);	
	public double[] getDirection();	
	public void setSpeed(double s);	
	public double getSpeed();
	public PointSetFactory getPointSetFactory();
	public void update();
}
