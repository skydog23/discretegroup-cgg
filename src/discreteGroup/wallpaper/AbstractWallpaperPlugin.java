package discreteGroup.wallpaper;

import java.awt.Component;
import java.awt.Graphics2D;
import java.io.Serializable;

import javax.swing.JPopupMenu;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.tool.Tool;
import de.jreality.util.SceneGraphUtility;

abstract public class AbstractWallpaperPlugin  implements Serializable{
	WallpaperPluggedIn wallpaper;
	transient Viewer viewer;
	String name;
	transient SceneGraphComponent singleTile;
	
	public AbstractWallpaperPlugin(WallpaperPluggedIn wp)	{
		wallpaper = wp;
		viewer = wallpaper.viewer;
		singleTile = SceneGraphUtility.createFullSceneGraphComponent(getName());
		init();
	}
	
	public void init()	{
		viewer = wallpaper.viewer;
	}
	
	abstract public String getName();
	
	public SceneGraphComponent getSceneGraphComponent()	{
		return singleTile;
	}
	
	public void replaceGroup()	{
		singleTile.setGeometry(wallpaper.getDefaultFundamentalRegion());
	}
	
	public void paint(Graphics2D g, double[] currentPoint) {
	}
	
	public Tool getTool() 	{
		return null;
	}
	
	public Component getInspectorPanel() {
		return null;
	}
	
	public void activate() {
	}
	
	public void deactivate() {
	}
	
	public void reset()	{
	}
	
	public void update()	{
	}
	
	public void setupContextMenu(JPopupMenu contextMenu)	{
		
	}
	
	// for xstream
	public Object readResolve()	{
//		init();
		singleTile = SceneGraphUtility.createFullSceneGraphComponent(getName());
//		singleTile.setGeometry(wallpaper.getDefaultFundamentalRegion());
		return this;
	}
}
