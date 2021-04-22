package discreteGroup.demo;

import java.io.IOException;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.ParametricSurfaceFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class EscherButterflies extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		double rmin = .2, rmax = .98;
		ParametricSurfaceFactory psf = new ParametricSurfaceFactory();
		psf.setImmersion( new ParametricSurfaceFactory.Immersion() {

			public void evaluate(double u, double v, double[] xyz, int index) {
				xyz[0] = u;
				xyz[1] = -Math.log(v);
				xyz[2] = 0.0;
			}

			public int getDimensionOfAmbientSpace() {
				return 3;
			}

			public boolean isImmutable() {
				return false;
			}
			
		});
		psf.setClosedInUDirection(false);
		psf.setClosedInVDirection(false);
		psf.setUMin(0); psf.setUMax(2*Math.PI); psf.setVMin(rmin); psf.setVMax(rmax);
		psf.setULineCount(50);
		psf.setVLineCount(50);
		psf.setEdgeFromQuadMesh(true);
		psf.setGenerateEdgesFromFaces(true);
		psf.setGenerateFaceNormals(true);
		psf.update();
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(psf.getIndexedFaceSet());
		
		ParametricSurfaceFactory tcf = new ParametricSurfaceFactory();
		tcf.setImmersion( new ParametricSurfaceFactory.Immersion() {

			public void evaluate(double u, double v, double[] xyz, int index) {
				xyz[0] = .5+ .5*v * Math.cos(u);
				xyz[1] = .5+ .5*v * Math.sin(u);
			}

			public int getDimensionOfAmbientSpace() {
				return 2;
			}

			public boolean isImmutable() {
				return false;
			}
			
		});
		tcf.setClosedInUDirection(false);
		tcf.setClosedInVDirection(false);
		tcf.setUMin(0); tcf.setUMax(2*Math.PI); tcf.setVMin(rmin); tcf.setVMax(rmax);
		tcf.setULineCount(50);
		tcf.setVLineCount(50);
		tcf.setEdgeFromQuadMesh(false);
		tcf.setGenerateEdgesFromFaces(false);
		tcf.setGenerateFaceNormals(false);
		tcf.update();
		psf.getIndexedFaceSet().setVertexAttributes(Attribute.TEXTURE_COORDINATES, 
				tcf.getIndexedFaceSet().getVertexAttributes(Attribute.COORDINATES));

		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		Texture2D tex2d = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, 
				"polygonShader.texture2d", ap, true);
		ImageData id = null;
		try {
			id = ImageData.load(Input.getInput("/homes/geometer/gunn/Pictures/textures/butterfly2.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tex2d.setImage(id);
		tex2d.setApplyMode(Texture2D.GL_REPLACE);
		tex2d.setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
		tex2d.setMagFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);			
		return world;
	}

}
