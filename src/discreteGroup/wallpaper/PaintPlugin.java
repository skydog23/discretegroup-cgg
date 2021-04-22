package discreteGroup.wallpaper;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.tool.Tool;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface XStreamOmitField {
}
public class PaintPlugin extends AbstractWallpaperPlugin {

	PaintSource paintSource;
	@XStreamOmitField
	transient ChangeListener cl;

	
	public PaintPlugin(WallpaperPluggedIn wp) {
		super(wp);
  		
		paintSource = new PaintSource(this);
		paintSource.setWallpaperGroup(wp.theGroup);
		cl = new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				if (wallpaper.viewer == null) return;
				wallpaper.viewer.renderAsync();
			}
			
			private Object readResolve() {
				return this;
			}
		};
		paintSource.addChangeListener(	cl);
		singleTile.addTool(paintSource.getTool());

	}
	@Override
	public void activate() {
		replaceGroup();
	}

	@Override
	public void deactivate() {
	}

	@Override
	public Component getInspectorPanel() {
		return paintSource.getInspector();
	}

	@Override
	public Tool getTool() {
		return paintSource.getTool();
	}

	@Override
	public void replaceGroup() {
		super.replaceGroup();
		paintSource.setWallpaperGroup(wallpaper.theGroup);
		paintSource.setPolygon((IndexedFaceSet) wallpaper.theGroup.getDefaultFundamentalRegion());
		paintSource.repaint();
	}

	@Override
	public String getName() {
		return "paint";
	}

	@Override
	public void reset() {
		paintSource.reset();
	}
	
	@Override
	public void update()	{
		paintSource.update();
	}
	public BufferedImage getTextureImage() {
		return paintSource.getMasterBufferedImage();
	}
	public void setTextureImage(BufferedImage textureImage) {
//		this.textureImage = textureImage;
//		ImageData id = initTexture();
 		paintSource.setMasterBufferedImage(textureImage);
		System.err.println("Setting texture image");
	}

}
