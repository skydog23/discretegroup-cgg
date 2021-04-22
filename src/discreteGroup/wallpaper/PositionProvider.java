package discreteGroup.wallpaper;

import javax.swing.JComponent;

import de.jtem.discretegroup.groups.WallpaperGroup;

public interface PositionProvider {

	public void update();
	public double[] getPosition();
	public void setPosition(double[] p);
	public void applyMatrix(double[] m);
	public JComponent getInspector();
	public void setWallpaperGroup(WallpaperGroup dg);
}
