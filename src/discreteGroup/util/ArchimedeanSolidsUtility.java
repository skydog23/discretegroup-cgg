/*
 * Created on Jan 21, 2011
 *
 */
package discreteGroup.util;

import java.util.List;
import java.util.Vector;

import charlesgunn.jreality.geometry.FullSphericalTriangleFactory;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.SphericalTriangleFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.Attribute;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.groups.TriangleGroup;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdge.Edge;

public class ArchimedeanSolidsUtility {
//	public static IndexedFaceSet polarForm(String group, String type, double radius) {
	public static SceneGraphComponent polarForm(String group, String type, double radius) {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		TriangleGroup tg = TriangleGroup.instanceOfGroup(group);
		WingedEdge we = ArchimedeanSolids.prepareArchimedeanSolid(tg, type);
		List<Edge >edges = we.getEdgeList();
//		double[] scale = P3.makeScaleMatrix(null, radius);
//		double[][] verts = new double[edges.size()*4][];
//		int i = 0;
//		int[][] edgeI = new int[edges.size()][];
		double[] p0 = new double[3], p1 = new double[3];
		for (Edge e : edges)	{
			SceneGraphComponent child = new SceneGraphComponent();
//			verts[4*i] = e.v0.point;
//			verts[4*i+1] = e.v1.point;
//			verts[4*i+2]=Rn.matrixTimesVector(null, scale, e.v0.point);
//			verts[4*i+3]=Rn.matrixTimesVector(null, scale, e.v1.point);
//			edgeI[i] = new int[]{4*i, 4*i+1, 4*i+3, 4*i+2};
//			i++;
			Pn.dehomogenize(p0, e.v0.point);
			Pn.dehomogenize(p1, e.v1.point);
			child.setGeometry(GeometryUtilityOverflow.fanFromSegment(p0, p1, 30, radius));
			world.addChild(child);
		}
//		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
//		ifsf.setVertexCount(verts.length);
//		ifsf.setVertexCoordinates(verts);
//		ifsf.setFaceCount(edgeI.length);
//		ifsf.setFaceIndices( edgeI);
//		ifsf.setGenerateFaceNormals(true);
//		ifsf.update();
//		return ifsf.getIndexedFaceSet();
		return world;
	}
	
	public static IndexedFaceSet polarFaceSet(String group, String type, double radius) {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		TriangleGroup tg = TriangleGroup.instanceOfGroup(group);
		WingedEdge we = ArchimedeanSolids.prepareArchimedeanSolid(tg, type);
		double[][] fco = we.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null);
		List<Edge >edges = we.getEdgeList();
		double[] scale = P3.makeScaleMatrix(null, radius);
		double[][] verts = new double[edges.size()*4][];
		int i = 0;
		int[][] edgeI = new int[2*edges.size()][];
		double[][] fc = new double[edgeI.length][];
		double[] p0 = new double[3], p1 = new double[3];
		for (Edge e : edges)	{
			SceneGraphComponent child = new SceneGraphComponent();
			verts[4*i] = e.v0.point;
			verts[4*i+1] = e.v1.point;
			verts[4*i+2]=Rn.matrixTimesVector(null, scale, e.v0.point);
			verts[4*i+3]=Rn.matrixTimesVector(null, scale, e.v1.point);
			edgeI[2*i] = new int[]{4*i, 4*i+1, 4*i+3, 4*i+2};
			edgeI[2*i+1] = new int[]{4*i, 4*i+1, 4*i+3, 4*i+2};
			fc[2*i] = fco[e.fL.index%fco.length];
			fc[2*i+1] = fco[e.fR.index%fco.length];
			i++;
//			Pn.dehomogenize(p0, e.v0.point);
//			Pn.dehomogenize(p1, e.v1.point);
//			child.setGeometry(GeometryUtilityOverflow.fanFromSegment(p0, p1, 30, radius));
//			world.addChild(child);
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(edgeI.length);
		ifsf.setFaceColors(fc);
		ifsf.setFaceIndices( edgeI);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
//		return world;
	}

	public static SceneGraphComponent polarPolygon(String group, String type, double radius) {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		TriangleGroup tg = TriangleGroup.instanceOfGroup(group);
		double[][] tri = tg.getTriangle();
		IndexedFaceSet disk = Primitives.regularPolygon(50);
		SphericalTriangleFactory stf = new SphericalTriangleFactory();
		stf.setVerts(tri);
		stf.update();
		FullSphericalTriangleFactory fstf = new FullSphericalTriangleFactory(stf);
		fstf.update();
		return fstf.getDualTriangle().getSceneGraphComponent();
	}

	static double[] zaxis = {0,0,1};
	public static SceneGraphComponent polarOrbit(String group, String type, double[] point, SceneGraphNode repn) {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		TriangleGroup tg = TriangleGroup.instanceOfGroup(group);
		DiscreteGroupElement[] ellist = tg.getElementList();
		double[] flipM = P3.makeScaleMatrix(null, -1);
		int i = 0;
		double[] rmat = P3.makeRotationMatrix(null, zaxis, point);
		double[] point2 = new double[point.length];
		Vector<double[]> orbit = new Vector<double[]>();
		for (DiscreteGroupElement dge : ellist)	{
			point2 = Rn.matrixTimesVector(null, dge.getArray(), point);
			boolean newone = true;
			for (double[] found : orbit)	{
				double d = Rn.euclideanDistance(found, point2);
				if (d < 10E-8)	{
					newone = false;
					break;
				}
				d = Rn.euclideanDistance(found, Rn.matrixTimesVector(null, flipM, point2));
				if (d < 10E-8)	{
					newone = false;
					break;
				}
			}
			if (newone) {
				orbit.add(point2);
				SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child");
				if (repn instanceof SceneGraphComponent) child.addChild((SceneGraphComponent) repn);
				else if (repn instanceof Geometry) child.setGeometry((Geometry) repn);
				double[] mat = Rn.times(null, dge.getArray(), rmat );
				child.getTransformation().setMatrix(mat);
				SceneGraphComponent child2 = new SceneGraphComponent();
				child2.addChild(child);
				world.addChild(child2);
			}
		}
		System.err.println("Found # "+orbit.size());
		return world;
	}

	public static SceneGraphComponent polarOrbit(double[][] points, SceneGraphNode repn) {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		double[] flipM = P3.makeScaleMatrix(null, -1);
		int i = 0;
		Vector<double[]> orbit = new Vector<double[]>();
		for (double[] point : points)	{
			double[] point2 = new double[point.length];
			boolean newone = true;
			for (double[] found : orbit)	{
				double d = Rn.euclideanDistance(found, point);
				if (d < 10E-8)	{
					newone = false;
					break;
				}
				d = Rn.euclideanDistance(found, Rn.matrixTimesVector(null, flipM, point));
				if (d < 10E-8)	{
					newone = false;
					break;
				}
			}
			if (newone) {
				orbit.add(point);
				SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("child");
				if (repn instanceof SceneGraphComponent) child.addChild((SceneGraphComponent) repn);
				else if (repn instanceof Geometry) child.setGeometry((Geometry) repn);
				double[] rmat = P3.makeRotationMatrix(null, zaxis, point);
				child.getTransformation().setMatrix(rmat);
				SceneGraphComponent child2 = new SceneGraphComponent();
				child2.addChild(child);
				world.addChild(child2);
			}
		}
		System.err.println("Found # "+orbit.size());
		return world;
	}

}
