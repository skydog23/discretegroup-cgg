/*
 * Created on Jan 29, 2004
 *
 */
package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.groups.TriangleGroup;


public class SoccerBall extends LoadableScene {
	SceneGraphComponent icokit;
	boolean tryFlatten = true;
	int refineLevel = 3;
	public void setRefineLevel(int rf)	{
		refineLevel = rf;
	}
	public SceneGraphComponent makeWorld()	{
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("soccerball");
		TriangleGroup tg = TriangleGroup.instanceOfGroup("*235");
		ArchimedeanSolids.prepareArchimedeanSolid(tg,  "101");
		IndexedFaceSet foo = (IndexedFaceSet) TriangleGroup.getSplitFundamentalRegion(tg);
		double[][] verts = foo.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		//System.out.println("Verts are \n"+Rn.toString(verts));
		double[][] verts3 = new double[verts.length][3];
		Pn.dehomogenize(verts3, verts);
		IndexedFaceSet hexifs = new IndexedFaceSet();
		IndexedFaceSet pentifs = new IndexedFaceSet();
		int[][] hexind = new int[][]{{0,2,1},{0,4,2}};
		int[][] pentind = new int[][]{{0,3,4}};
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateVertexNormals(false);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setVertexCount(verts3.length);
		ifsf.setFaceCount(hexind.length);
		ifsf.setVertexCoordinates(verts3);
		ifsf.setFaceIndices(hexind);
		ifsf.update();
		hexifs = ifsf.getIndexedFaceSet();
		ifsf = new IndexedFaceSetFactory();
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateVertexNormals(false);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setVertexCount(verts3.length);
		ifsf.setFaceCount(pentind.length);
		ifsf.setVertexCoordinates(verts3);
		ifsf.setFaceIndices(pentind);
		ifsf.update();
		pentifs = ifsf.getIndexedFaceSet();
//			IndexedFaceSetUtility.setIndexedFaceSetFrom(hexifs, hexind, verts3,null,null,null,null,null);
//			GeometryUtility.calculateAndSetFaceNormals(hexifs);
//			IndexedFaceSetUtility.setIndexedFaceSetFrom(pentifs, pentind, verts3, null, null, null,null,null);
//			GeometryUtility.calculateAndSetFaceNormals(pentifs);
//			pentifs.buildEdgesFromFaces();
//			hexifs.buildEdgesFromFaces();
		for (int i = 0; i<refineLevel; ++i)	{
			pentifs = IndexedFaceSetUtility.binaryRefine(pentifs);
			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(pentifs); //pentifs.buildEdgesFromFaces();
			hexifs = IndexedFaceSetUtility.binaryRefine(hexifs);
			IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(hexifs); //pentifs.buildEdgesFromFaces();
		}
		verts = pentifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int vlength = GeometryUtility.getVectorLength(pentifs);
		Rn.normalize(verts, verts);
		pentifs.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
		pentifs.setVertexAttributes(Attribute.NORMALS,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
		verts = hexifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		vlength = GeometryUtility.getVectorLength(hexifs);
		Rn.normalize(verts, verts);
		hexifs.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
		hexifs.setVertexAttributes(Attribute.NORMALS,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));

		SceneGraphComponent pentsgc = SceneGraphUtility.createFullSceneGraphComponent("pentagon");
		pentsgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		pentsgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(0,0,0));
		pentsgc.setGeometry(pentifs);

		SceneGraphComponent hexsgc = SceneGraphUtility.createFullSceneGraphComponent("hexagon");
		hexsgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		hexsgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
		hexsgc.setGeometry(hexifs);

		int[][] tubeind = new int[][]{{1,0},{0,4}};
		IndexedLineSet tubeils = new IndexedLineSet(verts3.length, tubeind.length);
		tubeils.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts3));
		tubeils.setEdgeAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(tubeind));
		
//			IndexedFaceSetUtility.setIndexedLineSetFrom(tubeils, tubeind, verts3, null, null);
		tubeils = IndexedLineSetUtility.refine(tubeils, 20);
		verts = tubeils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		vlength = GeometryUtility.getVectorLength(tubeils);
		Rn.normalize(verts, verts);
		tubeils.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
		
		SceneGraphComponent tubesgc = SceneGraphUtility.createFullSceneGraphComponent("tubes");
		tubesgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		tubesgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, false);
		tubesgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.BLUE);
		tubesgc.setGeometry(tubeils);

		SceneGraphComponent fundDomain = SceneGraphUtility.createFullSceneGraphComponent("FD");
		fundDomain.addChild(pentsgc);
		fundDomain.addChild(hexsgc);
		fundDomain.addChild(tubesgc);
		DiscreteGroupSceneGraphRepresentation sgr = new DiscreteGroupSceneGraphRepresentation(tg);
		sgr.setWorldNode(fundDomain);
		sgr.update();
		SceneGraphComponent DGRepn =  sgr.getRepresentationRoot();
		theWorld.addChild(DGRepn);

		
//			SceneGraphComponent flatt = GeometryUtility.flatten(theWorld);
//			AbstractDeformation ad = new AbstractDeformation()		{
//				public double[] valueAt(double[] in, double[] out)	{
//					if (out == null || out.length != in.length) out = new double[in.length];
//					for (int i = 0; i<in.length; ++i)	out[i] = in[i];
//					out[1] *= 2;
//					out[2] *= .5;
//					return out;
//				}
//			};
//			AbstractDeformation.deform(flatt, ad);
//		SceneGraphComponent earth =SceneGraphUtility.createFullSceneGraphComponent();
//		earth.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
//		earth.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
//		earth.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
//		earth.setGeometry(de.jreality.geometry.Primitives.texturedQuadrilateral());
//		MatrixBuilder.euclidean().translate(-1,-1,0).scale(2).assignTo(earth);
//		theWorld.addChild(earth);
//		try {
//			Texture2D tex2d = TextureUtility.createTexture(earth.getAppearance(), "polygonShader", 
//					"/Users/gunn/Pictures/Textures/earth.png");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		return theWorld;
	}

	public boolean isEncompass() {return true;}
	public boolean addBackPlane() {return true; }
	
}
