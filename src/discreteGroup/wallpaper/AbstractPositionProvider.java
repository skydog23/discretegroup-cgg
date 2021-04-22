package discreteGroup.wallpaper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

abstract public class AbstractPositionProvider implements PositionProvider, Serializable {

	protected double[] position = {0,0,1};		// Note that this is a homogeneous point in the projective plane, not in space
	protected WallpaperGroup theGroup;
	protected SceneGraphComponent tile;
	protected transient JComponent inspector;
	PaintSource thePlugin;
	
	/**
	 * When the position moves outside the fundamental domain, the group element is found which
	 * moves it back within the fundamental domain. The matrix below represents this group element.
	 * If you have accessory points or directions which move with the position, call {@link Rn#matrixTimesVector(double[], double[], double[])}
	 * as is done here for the position.
	 */
	public void applyMatrix(double[] m)	{
		Rn.matrixTimesVector(position, m, position);
	}
	
	/**
	 * returns the current position 
	 */
	public double[] getPosition() {
		return position;
	}

	/**
	 * typically only used at initialization
	 */
	public void setPosition(double[] p) {
		position =p.clone();
	}

	/**
	 * all subclasses must implement this method.  Basically, move forward to the "next" position.
	 */
	abstract public void update() ;

	/**
	 * If your subclass has GUI elements for inspecting its variables, add them to this
	 * <i>inspector</i> field.  Recommended to use a GridLayout for your GUI, see 
	 * existing subclasses {@link CurlyQueuePositionProvider} and {@link CirclingPositionProvider}.
	 */
	public JComponent getInspector() {
		if (inspector != null) return inspector;
		inspector = new ShrinkPanel(this.getClass().getSimpleName());//Box.createVerticalBox(); //
		return inspector;
	}

	/**
	 * If your motion depends on the group, the field <i>theGroup</i> identifies it.
	 */
	public void setWallpaperGroup(WallpaperGroup dg) {
		theGroup = dg;
	}
	
	/**
	 * This is a short-term solution to the problem of registering different subclasses at
	 * run-time.  Instead of automatically searching for subclasses in the class path, which
	 * I could do, each subclass is required to register here on initialization if it wants
	 * to be listed in the menu.
	 */
	static List<AbstractPositionProvider> subclasses = new ArrayList<AbstractPositionProvider>();

	public static List<AbstractPositionProvider> getSubclasses() {
		return subclasses;
	}

	public static void registerSubclass(AbstractPositionProvider subclass) {
		subclasses.add(subclass);
	}
	public SceneGraphComponent getTile() {
		return tile;
	}
	public void setTile(SceneGraphComponent tile) {
		this.tile = tile;
	}
}
