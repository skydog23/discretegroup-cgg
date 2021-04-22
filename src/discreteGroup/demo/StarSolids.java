/*
 * Created on Mar 17, 2004
 *
 */
package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.util.WingedEdge;

/**
 * @author gunn
 *
 */
public class StarSolids extends LoadableScene {
	double[][] fc =
	{{0.8, 0.1, 0.1}, 
	{0.1, 0.65, 0.4}, 
	{0.1, 0.1, 0.8}, 
	{0.9, 0.6, 0}, 
	{0, 0.6, 0.8}, 
	{0.5, 0, 0.9}, 
	{.7, .15, .1},
	{.2, .2, .8},
	{.9, .6, .02},
	{.1, .3, .8},
	{.1, .7, .2},
	{.8, .8, .4}};
	java.awt.Color[] colors = {java.awt.Color.RED, java.awt.Color.GREEN, java.awt.Color.YELLOW, new Color(255, 20, 200),java.awt.Color.BLUE};
	double phi = (Math.sqrt(5.0)-1.0)/2.0;
	double phi2 = phi*phi;
	double[] axis = {phi, 0.0, 1.0};
	double[] rot = P3.makeRotationMatrix(null, axis, Math.PI * (2.0/5.0));
	double icoscale = 1.77;
	SceneGraphComponent icoScale;
	private SceneGraphComponent archkit;
	boolean interlockedCubesOnly = false;
	private WingedEdge dodec;
	public SceneGraphComponent makeWorld()	{
		SceneGraphComponent theWorld = SceneGraphUtility.createFullSceneGraphComponent("world");
		archkit = SceneGraphUtility.createFullSceneGraphComponent("Great dodecahedron");
		archkit.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);
		archkit.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		dodec = ArchimedeanSolids.archimedeanSolid("5.5.5");
		IndexedFaceSet ifs = IndexedFaceSetUtility.implode(IndexedFaceSetUtility.truncate(dodec), -3.235);
		ifs.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(fc));
		archkit.setGeometry(ifs);
		archkit.setTransformation(new Transformation());
		archkit.getTransformation().setMatrix(P3.makeTranslationMatrix(null,new double[]{4.0, 0.0, 0.0}, Pn.EUCLIDEAN));
		WingedEdge ico = ArchimedeanSolids.archimedeanSolid("3.3.3.3.3");
		BallAndStickFactory basf = new BallAndStickFactory(ico);
		basf.setBallColor(new java.awt.Color(255,255,100));
		basf.setBallRadius(.02);
		basf.setStickColor(java.awt.Color.WHITE);
		basf.setStickRadius(.01);
		basf.setMetric(Pn.EUCLIDEAN);
		basf.update();
		icoScale = basf.getSceneGraphComponent();
//		icoScale = TubeUtility.ballAndStick(ico, .02,.01, new java.awt.Color(255,255,100), java.awt.Color.WHITE, Pn.EUCLIDEAN);
		icoScale.setTransformation(new Transformation(P3.makeStretchMatrix(null,icoscale)));
		archkit.addChild(icoScale);
		
		double[][] oldverts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] newverts = new double[oldverts.length*2][oldverts[0].length];
		int[][] oldindices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		int[][] newindices = new int[5*oldindices.length][];
		double[][] nfc = new double[60][3];
		for (int i = 0; i<oldverts.length; ++i) 	{
			Rn.copy(newverts[2*i], oldverts[i]);
		}
		for (int i = 0; i<oldindices.length; ++i) 	{
			for (int j = 0; j<oldindices[i].length; ++j)	{
				int k = (j+2)%oldindices[i].length;
				double[] p0 = oldverts[oldindices[i][j]];
				double[] p1 = oldverts[oldindices[i][k]];
				double[] diff = Rn.subtract(null, p1, p0);
				Rn.add(newverts[i*10+2*j+1], p0, Rn.times(diff, phi2, diff));
				newindices[5*i+j] = new int[3];
				newindices[5*i+j][0] = 10*i + ((2*j + 1)%10);
				newindices[5*i+j][1] = 10*i + ((2*j + 2)%10);
				newindices[5*i+j][2] = 10*i + ((2*j + 3)%10);
				Rn.copy(nfc[5*i+j],fc[i]);
			}
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(newverts.length);
		ifsf.setFaceCount(newindices.length);
		ifsf.setVertexCoordinates(newverts);
		ifsf.setFaceIndices(newindices);
		ifsf.setFaceColors(nfc);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		IndexedFaceSet vs = ifsf.getIndexedFaceSet();
//		IndexedFaceSet vs = IndexedFaceSetUtility.createIndexedFaceSetFrom(newindices, newverts, null, null, null, null,nfc);
//		GeometryUtility.calculateAndSetFaceNormals(vs);
//		vs.buildEdgesFromFaces();
		SceneGraphComponent smallStellDodec = SceneGraphUtility.createFullSceneGraphComponent("small stellated dodecahedron");
		smallStellDodec.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
		smallStellDodec.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,.02*icoscale);
		smallStellDodec.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		smallStellDodec.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .01*icoscale);
		smallStellDodec.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
		smallStellDodec.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(255,255,100));

		smallStellDodec.setGeometry(vs);
		
		// try interlocking 5 cubes

		SceneGraphComponent interlockedCubes = getInterlockedCubes();
		if (!interlockedCubesOnly) interlockedCubes.getTransformation().setMatrix(P3.makeTranslationMatrix(null,new double[]{4.0, 4.0, 0.0}, Pn.EUCLIDEAN));

		SceneGraphComponent interlockedTetras = getInterlockedTetrahedra();
		interlockedTetras.getTransformation().setMatrix(P3.makeTranslationMatrix(null,new double[]{0.0, 4.0, 0.0}, Pn.EUCLIDEAN));
		if (interlockedCubesOnly) theWorld.addChild(interlockedCubes);
		else theWorld.addChildren(archkit, smallStellDodec, interlockedCubes, interlockedTetras);
		
		theWorld.getAppearance().setAttribute(CommonAttributes.AMBIENT_COEFFICIENT,0.0);
		theWorld.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.25);
		return theWorld;
	}

	private SceneGraphComponent getInterlockedCubes() {
		SceneGraphComponent interlockedCubes = SceneGraphUtility.createFullSceneGraphComponent("Five cubes interlocked");
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		//interlockedCubes.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER, "implode");
		interlockedCubes.getAppearance().setAttribute("implodeFactor",.2d);
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW, true);
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,.02*icoscale);
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .01*icoscale);
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.WHITE);
		interlockedCubes.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(255,255,100));
		double[] acc = Rn.identityMatrix(4);
		IndexedFaceSet cube = Primitives.cube();
		SceneGraphComponent dodecSGC =SceneGraphUtility.createFullSceneGraphComponent();
		dodecSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		dodecSGC.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		dodecSGC.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, .01);
		dodecSGC.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.yellow); //new Color(0,0,100));
		MatrixBuilder.euclidean().scale(1.74).assignTo(dodecSGC);
		dodecSGC.setGeometry(dodec);
		if (interlockedCubesOnly) {
			interlockedCubes.addChild(dodecSGC);
			SceneGraphComponent dodecSGC2 = SceneGraphUtility.createFullSceneGraphComponent();
			dodecSGC2.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
			dodecSGC2.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
			dodecSGC2.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, .01);
			dodecSGC2.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.yellow); //new Color(0,0,100));
			dodecSGC2.getAppearance().setAttribute("polygonShader.diffuseColor", Color.cyan); //new Color(0,0,100));
			dodecSGC2.setGeometry(dodec);
			MatrixBuilder.euclidean().translate(3,0,0).scale(.5*1.74).assignTo(dodecSGC2);
			interlockedCubes.addChild(dodecSGC2);
		}
	 	for (int i = 0; i<5; ++i)	{
			SceneGraphComponent cubekit = SceneGraphUtility.createFullSceneGraphComponent();
			cubekit.getTransformation().setMatrix(acc);
			cubekit.setGeometry(cube); 
			cubekit.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, colors[i]);
			if (interlockedCubesOnly)	{
				SceneGraphComponent cubekit2 = SceneGraphUtility.createFullSceneGraphComponent();
				cubekit2.addChildren(dodecSGC, cubekit);
				double cs = Math.cos((i+1)*Math.PI*2.0/6.0);
				double ss = Math.sin((i+1)*Math.PI*2.0/6.0);
				MatrixBuilder.euclidean().translate(3*cs,3*ss,0).scale(.5).assignTo(cubekit2);
				interlockedCubes.addChild(cubekit2);
			}
			interlockedCubes.addChild(cubekit);
			Rn.times(acc,acc,rot);
		}
		return interlockedCubes;
	}

	private SceneGraphComponent getInterlockedTetrahedra() {
		BallAndStickFactory basf;
		double[] acc;
		SceneGraphComponent interlockedTetras = SceneGraphUtility.createFullSceneGraphComponent();
		acc = Rn.identityMatrix(4);
		IndexedFaceSet tetra = Primitives.tetrahedron();
		IndexedFaceSet cyl = Primitives.cylinder(20, 1, -.5, .5, Math.PI*2);
		for (int i = 0; i<5; ++i)	{
			SceneGraphComponent cubekit = SceneGraphUtility.createFullSceneGraphComponent();
			cubekit.getTransformation().setMatrix(acc);
			basf = new BallAndStickFactory(tetra);
			basf.setBallColor(colors[i]);
			basf.setBallRadius(.12);
			basf.setStickColor(colors[i]);
			basf.setStickRadius(.12);
			basf.setMetric(Pn.EUCLIDEAN);
			basf.setStickGeometry(cyl);
			basf.update();
			cubekit.addChild(basf.getSceneGraphComponent());
			interlockedTetras.addChild(cubekit);
			Rn.times(acc,acc,rot);
		}
		return interlockedTetras;
	}

	public boolean isEncompass()	{ return true; }
	public boolean addBackPlane()	{ return false; }


	

}
