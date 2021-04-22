package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;

public class Eva4344 extends LoadableScene {

	double x = Math.sqrt(2.0)/2.0, dx = x, y = x, z = 1;
	double[][] pts = {
			{x,y,0},
			{-x,y,0},
			{x+dx,0,z},
			{x+dx,0,-z},
			{-x-dx,0,z},
			{-x-dx,0,-z},
			{x,-y,0},
			{-x,-y,0}
	};
	
	int[][] faces = {
			{0,2,3},
			{1,5,4},
			{6,3,2},
			{7,4,5},
			{0,1,4,2},
			{1,0,3,5},
			{6,7,5,3},
			{7,6,2,4}
	};
	
	Color c4 = Color.yellow, c3 = Color.green;
	Color[] facecolors = {c3,c3,c3,c3,c4,c4,c4,c4};
	
	IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
	
	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("4344");
		ifsf.setVertexCount(8);
		ifsf.setVertexCoordinates(pts);
		ifsf.setFaceCount(8);
		ifsf.setFaceIndices(faces);
		ifsf.setFaceColors(facecolors);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		
		WingedEdge we = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(ifsf.getIndexedFaceSet());
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("polar");
		sgc.setGeometry(we);
		
		WingedEdge polar = we.polarize(1.0);
		SceneGraphComponent polarSGC = SceneGraphUtility.createFullSceneGraphComponent("polar");
		polarSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .4);
		polarSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		polarSGC.setGeometry(polar);
		world.addChildren(sgc, polarSGC);
		return world;
	}

	@Override
	public boolean isEncompass() {
		return true;
	}

	
}
