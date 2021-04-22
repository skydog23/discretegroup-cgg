package discreteGroup.wallpaper;

import java.awt.Color;
import java.awt.Component;

public interface ColorProvider {

	public void update();
	public Color getColor();
	public Color getRGBColor();
	public void setRGBColor(Color c);
	public double getAlpha();
	public void setAlpha(double d);
	public Component getInspector();
}
