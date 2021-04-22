/*
 * Created on May 14, 2008
 *
 */
package discreteGroup.wallpaper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.thoughtworks.xstream.XStream;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.ImageUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class ImportExport {

	Date timestamp = new Date();
	WallpaperPluggedIn wallpaperPluggedIn;
	WallpaperGroup wallpaperGroup;
	SceneGraphComponent sceneGraph;
	BufferedImage bufferedImage;
	
	public void write( File output)	{
		XStream xstream = setupXStream();
		OutputStream os = null;
		try {
			os = new FileOutputStream(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xstream.toXML(this, os );
		
	}

	public  void read(InputStream is)	{
		// first set up
		XStream xstream = setupXStream();
		ImportExport foo = (ImportExport) xstream.fromXML(is);
		System.err.println("Date = "+foo.timestamp);
		wallpaperPluggedIn = foo.wallpaperPluggedIn;
		sceneGraph = foo.sceneGraph;
		wallpaperGroup = foo.wallpaperGroup;
		bufferedImage = foo.bufferedImage;
		if (bufferedImage != null)
			ImageUtility.writeBufferedImage(new File("/tmp/foo.png"), bufferedImage);
//		return wp; 
	}
	
	public  void read( File input)	{

		InputStream is = null;
		try {
			is = new FileInputStream(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		read(is);
//		return read(is);
	}
	
	public static XStream setupXStream() {
		XStream xstream = discreteGroup.io.ImportExport.setupXStream();
//		xstream.registerConverter(new SceneGraphNodeConverter(xstream.getMapper()));
		xstream.registerConverter(new BufferedImageConverter(null));
		return xstream;
	}

	public SceneGraphComponent getSceneGraph() {
		return sceneGraph;
	}

	public void setSceneGraph(SceneGraphComponent sceneGraph) {
		this.sceneGraph = sceneGraph;
	}

	public WallpaperPluggedIn getWallpaperPluggedIn() {
		return wallpaperPluggedIn;
	}

	public void setWallpaperPluggedIn(WallpaperPluggedIn wallpaperPluggedIn) {
		this.wallpaperPluggedIn = wallpaperPluggedIn;
	}

	public WallpaperGroup getWallpaperGroup() {
		return wallpaperGroup;
	}

	public void setWallpaperGroup(WallpaperGroup wallpaperGroup) {
		this.wallpaperGroup = wallpaperGroup;
	}

	public static void main(String[] args) {
		ImportExport io = new ImportExport();
		io.setSceneGraph(new SceneGraphComponent("test1"));
		io.setWallpaperGroup(WallpaperGroup.instanceOfGroup("XX"));
		io.write(new File("/tmp/foo.xml"));
		io = new ImportExport();
		io.read(new File("/tmp/foo.xml"));
		System.err.println("sgc name = "+io.getSceneGraph().getName());
		System.err.println("wallpaper group name = "+io.getWallpaperGroup().getName());
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
	
}
