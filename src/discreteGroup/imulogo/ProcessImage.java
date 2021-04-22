package discreteGroup.imulogo;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.ImageUtility;
import de.jreality.util.Input;

public class ProcessImage extends Assignment {

	IMULogo imulogo = new IMULogo();
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
		new ProcessImage().display();
	}

	@Override
	public void display() {
		Viewer viewer = jrviewer.getViewer();
		ImageData  id2 = null;
		try {
			id2 = ImageData.load(Input.getInput("http://page.math.tu-berlin.de/~gunn/Pictures/textures/Abspann.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("abspann size = "+id2.getWidth()+" "+id2.getHeight());
		BufferedImage image = (BufferedImage) id2.getImage();
		byte[] data = id2.getByteArray();
		int index = 0;
		int minalpha = 255;
		for (int i = 0; i<id2.getHeight() * id2.getWidth(); ++i)	{
			int alpha = data[index],
					red = data[index+1],
					green = data[index+2],
					blue = data[index+3];
			if (alpha < 0) alpha += 256;
			if (red < 0) red += 256;
			if (green < 0) green += 256;
			if (blue < 0) blue += 256;
			int min = Math.min(alpha, Math.min(red, Math.min(blue,green)));
			if (minalpha > min) minalpha = min;
			data[index+0] = (byte) blue;
			data[index+1] = (byte) red;
			data[index+2] = (byte) (255-min);
			data[index+3] = (byte) blue;
//			data[index+3] = 0; //(byte) (255 - green);
			index += 4;
		}
		System.err.println("min alpha = "+minalpha);
		ImageData id3 = new ImageData(data, id2.getWidth(), id2.getHeight());
		
		image = (BufferedImage) id3.getImage();
		System.err.println("Image is of type "+image.getType());
		ImageUtility.writeBufferedImage(new File("/tmp/foo.png"), image);
	}

	@Override
	public void setValueAtTime(double d) {
		imulogo.setValueAtTime(7.0+d);
	}

}
