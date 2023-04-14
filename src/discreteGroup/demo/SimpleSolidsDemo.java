/*
 * Created on Apr 21, 2004
 *
 */
package discreteGroup.demo;


import java.awt.Color;
import java.io.IOException;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.reader.Readers;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Input;



/**
 * @author gunn
 *
 */
public class SimpleSolidsDemo extends Assignment    {
	SceneGraphComponent theWorld;
	SceneGraphComponent icokit;
	public SimpleSolidsDemo() {
		super();
	}
	
	@Override
	public SceneGraphComponent getContent()	{
		theWorld = new SceneGraphComponent();
		SceneGraphComponent skin = null, bones = null;
		try {
			bones  = Readers.read(
					Input.getInput("/Volumes/SamsungSSD1T/gunn_local/Models/geomview/dodec2.xyzr")); //OBJ/Genus2_stl/Polyhedron_2_10_20332.jvx"));// 
			skin  = Readers.read(
					Input.getInput("/Volumes/SamsungSSD1T/gunn_local/Models/Molecules/dodec.msms")); //OBJ/Genus2_stl/Polyhedron_2_10_20332.jvx"));// 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		theWorld.addChild(bones);
		theWorld.addChild(skin);
		skin.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		skin.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .5);
		skin.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
//		viewer.getCameraPath().getLastComponent().getTransformation().setTranslation(0,0,10.0);
		return theWorld;
	}


	public static void main(String[] args) {
		new SimpleSolidsDemo().display();
	}
}
