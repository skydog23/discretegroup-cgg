package discreteGroup.imulogo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import charlesgunn.anim.gui.AnimationPanel;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.ImageUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class AnimateIMULogo extends Assignment {

	transient IMULogo imulogo = new IMULogo();
	@Override
	public SceneGraphComponent getContent() {
		return imulogo.makeWorld();
	}

	public void doYourThing() {
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AnimateIMULogo().display();
	}

	@Override
	public void display() {
		super.display();
		Viewer viewer = jrviewer.getViewer();
		ImageData id = null;
		try {
			id = ImageData.load(Input.getInput("http://page.math.tu-berlin.de/~gunn/Pictures/textures/borrRingBlueFaded.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		imulogo.initializeViewer(viewer);
//		Texture2D tex2d = TextureUtility.createTexture(viewer.getSceneRoot().getAppearance(), "polygonShader", id);
		Appearance appearance = viewer.getSceneRoot().getAppearance();
//		TextureUtility.setBackgroundTexture(appearance, id);
//		Texture2D tex2d = TextureUtility.getBackgroundTexture(appearance);
//		Matrix tm = MatrixBuilder.euclidean().scale(-1,1,1).getMatrix();
//		tex2d.setTextureMatrix(tm);
//		   //		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_TEXTURE2D, id);
		appearance.setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(0,0,0,0));
//		appearance.setAttribute(CommonAttributes.BACKGROUND_COLORS, Appearance.INHERITED);
		ViewMenuBar vmb = jrviewer.getController().getPlugin(ViewMenuBar.class);
		try {
			animationPlugin.getAnimationPanel().read(new Input(this.getClass().getResource("IMULogo-anim-01.xml")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		animationPlugin.getAnimationPanel();
		animationPlugin.getAnimationPanel().setResourceDir("src/discreteGroup/imulogo");

	}

	@Override
	public void setValueAtTime(double d) {
		imulogo.setValueAtTime(7.0+d);
	}

	@Override
	public Component getInspector() {
		// TODO Auto-generated method stub
		return imulogo.getInspector(jrviewer.getViewer());
	}

}
