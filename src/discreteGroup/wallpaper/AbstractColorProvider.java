package discreteGroup.wallpaper;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.JComponent;

import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

abstract public class AbstractColorProvider implements ColorProvider, Serializable {

	protected Color color = Color.white;
	protected double alpha;
	public transient ShrinkPanel shrinkPanel;
	public Color getRGBColor() {
		return color;
	}

	public void setRGBColor( Color c)	{
		color = c;
	}

	public Color getColor() {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), ((int) (255*alpha)));
	}
	
	abstract public void update() ;

//	public JComponent getInspector() {
//		if (inspector != null) return inspector;
//		ShrinkPanel sp = new ShrinkPanel(this.getClass().getSimpleName());
//		inspector = sp; //new JPanel();
//		GridBagLayout gl = new GridBagLayout();
//		inspector.setLayout(gl);
//		return inspector;
//	}

	public JComponent getInspector() {
		if (shrinkPanel != null) return shrinkPanel;
		shrinkPanel = new ShrinkPanel(this.getClass().getName());
		
		shrinkPanel.removeAll();
		shrinkPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
		return shrinkPanel;
	}
	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double d) {
		alpha = d;
	}


}
