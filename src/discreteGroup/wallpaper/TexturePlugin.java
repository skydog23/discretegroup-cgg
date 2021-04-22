package discreteGroup.wallpaper;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.iharder.dnd.FileDrop;
import charlesgunn.jreality.newtools.TexturePlacementTool;
import charlesgunn.jreality.texture.SimpleTextureFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.Input;

public class TexturePlugin extends AbstractWallpaperPlugin {

	transient private TexturePlacementTool texturePlacementTool;
	transient private JButton filedropButton;
	protected Image thumbnailImage = null;
	protected int thumbnailSize = 100;
	protected Appearance imageAp;
	transient protected Texture2D  imageTex;
	Matrix texMatrix;
	transient JPanel  insp;
	
	public TexturePlugin(WallpaperPluggedIn wp) {
		super(wp);
		createAppearance();
		return;
//		texturePlacementTool = new TexturePlacementTool(imageTex);
//		texturePlacementTool.setViewer(viewer);
//		singleTile.addTool(texturePlacementTool);
	}
	private void createAppearance() {
		imageAp = singleTile.getAppearance();
		imageAp.setAttribute(POLYGON_SHADER+"."+DIFFUSE_COLOR, Color.white);
		imageAp.setAttribute(EDGE_DRAW, false);
		imageAp.setAttribute(FACE_DRAW, true);
		
//  		try {
//  			ImageData id2 = ImageData.load(Input.getInput(
//  					"/Users/skydog/Desktop/P8220142.JPG"));
//  					//"http://www.math.tu-berlin.de/~gunn/Pictures/christmasTree.jpg"));//textures/weave-256.png"));//
   			SimpleTextureFactory stf = new SimpleTextureFactory();
   			stf.setType(SimpleTextureFactory.TextureType.GRADIENT);
			Color c1 = new Color(1f, .8f, 0f),  c2 = new Color(.4f, 1f, 0f);
			stf.setColor(0, c1);
			stf.setColor(1, c2);
  			stf.update();
  			ImageData id2 = stf.getImageData();
  			imageTex = //TextureUtility.createTexture(imageAp, "polygonShader", id2);
			(Texture2D) AttributeEntityUtility
 	       .createAttributeEntity(Texture2D.class, "polygonShader.texture2d", imageAp, true);
			setTextureImage(imageTex, id2);
			texturePlacementTool = new TexturePlacementTool(imageTex);
//			texturePlacementTool.setViewer(viewer);
			singleTile.addTool(texturePlacementTool);
// 		} catch (IOException e) {
//  			e.printStackTrace();
//  		}
 		imageTex.setMipmapMode(true);
		imageTex.setRepeatS(Texture2D.GL_REPEAT);
		imageTex.setRepeatT(Texture2D.GL_REPEAT);
 		imageTex.setPixelFormat(Texture2D.GL_RGBA);
		Matrix texMatrix = new Matrix();
		MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(texMatrix);
		imageTex.setTextureMatrix(texMatrix);
	}
	void setTextureImage(Texture2D tex2d, ImageData id2) {
		tex2d.setImage(id2);
//		System.err.println("bands of image = "+((BufferedImage) id2.getImage()).getRaster().getNumBands());
//		System.err.println("size of image = "+((BufferedImage) id2.getImage()).getRaster().getDataBuffer().getSize());
		thumbnailImage = SimpleTextureFactory.getScaledImage(id2.getImage(), thumbnailSize, (thumbnailSize*id2.getHeight())/id2.getWidth());
	}

    @Override
	public
	void activate() {
//		createAppearance();
	}

	@Override
	public
	void deactivate() {
	}

	@Override
	public
	Component getInspectorPanel() {
		if (insp != null) return insp;
			insp = new JPanel();
			JLabel label = new JLabel("Drag and drop image file on this button");
			insp.add(label);
			
			ImageIcon icon = new ImageIcon(thumbnailImage);
			filedropButton = new JButton(icon);
//			filedropButton.setPreferredSize(new Dimension(50,50));
//			filedropButton.setBackground(Color.red);
			insp.add(filedropButton);
			FileDrop fileDrop = new FileDrop(filedropButton, new FileDrop.Listener() {

				public void filesDropped(File[] arg0) {
					if (arg0.length == 0) return;
		 			try {
						ImageData id2 = ImageData.load(Input.getInput(arg0[0]));
						setTextureImage(imageTex,id2);
						filedropButton.setIcon(new ImageIcon(thumbnailImage));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.err.println("File dropped"+arg0[0].getName());
				}
				
			});
			insp.setName("Texture");
			return insp;
	}

	@Override
	public
	Tool getTool() {
		return texturePlacementTool;
	}

	@Override
	public void paint(Graphics2D g, double[] currentPoint) {

	}

	CurlyQueuePositionProvider pfac;
	private SinusoidColorProvider colorFactory;
	double time, rot, x, y;
	public void update()	{
		long newtime = System.currentTimeMillis();
		int diff = (int) (newtime - time);
		// frac will be one when we are doing 30 fps
		double frac = 30*diff/1000.0;
		if (time == 0) { 				time = newtime; return; }
		time = newtime;
		pfac.update();
		colorFactory.update();
		Color color = colorFactory.getColor();
		rot += frac*(color.getRed() + color.getGreen() -255)/10000f;
		x += frac*color.getGreen()/100000f;
		y +=  frac*color.getBlue()/100000f;
//		x = x%1.0;
//		y = y %1.0;
//		System.err.println("rot, x, y="+rot+":"+x+":"+y);
//		double[] icenter = Rn.times(null, -1, wallpaper.center);		
//		MatrixBuilder.euclidean().translate(x,y,0).translate(wallpaper.center).rotateZ(rot).translate(icenter).assignTo(texMatrix.getArray());

	}
	@Override
	public String getName() {
		return "texture";
	}
	@Override
	public void reset() {
		Matrix texMatrix = imageTex.getTextureMatrix();
		MatrixBuilder.euclidean().rotateZ(Math.PI).assignTo(texMatrix);
		imageTex.setTextureMatrix(texMatrix);
	}
}
