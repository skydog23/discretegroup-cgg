package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

public class S245Demo extends LoadableScene {

	private int groupSize = 5;

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("world");
//		TriangleGroup tg = TriangleGroup.instanceOf(2, 4, 5, false);
		DiscreteGroup tg = new DiscreteGroup();
		tg.setDimension(2);
		tg.setMetric(Pn.HYPERBOLIC);
		DiscreteGroupElement[] gens = new DiscreteGroupElement[2];
		for (int i=0;i<gens.length;++i)  {
			gens[i] = new DiscreteGroupElement();
			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
		}
		double s = .945742, c = 1.37638;
		double[] center11 = {s,0,1,c};
		double[] center21 = {0,s,1,c};
		double[][] pts = {{s,0,0,c}, {0,s,0,c}, new double[]{-s,0,0,c},
					new double[]{0,-s,0,c}};
		double[] symline = {-c, -c, 0, s};
		gens[0].setArray(P3.makeRotationMatrix(null, pts[0], center11, 2*Math.PI/5.0, Pn.HYPERBOLIC));
		gens[1].setArray(P3.makeRotationMatrix(null, pts[1], center21, 2*Math.PI/5.0, Pn.HYPERBOLIC));
		for (int i = 0; i<gens.length; ++i)	{
			double[] l1 = Rn.matrixTimesVector(null, gens[0].getArray(), symline);
			System.err.println("symline = "+Rn.toString(l1));			
		}
		tg.setGenerators(gens);
		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(groupSize); // {
//			ArrayList<double[]> centers = new ArrayList<double[]>();
//			@Override
//			public boolean acceptElement(DiscreteGroupElement dge) {
//				if (!super.acceptElement(dge)) return false;
//				return true;
//			}
//			
//			@Override
//			public void update()	{
//				centers.clear();
//			}
//		};
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(tg, false);
		tg.setElementList(DiscreteGroupUtility.generateElements(tg, dgsc));
		tg.setConstraint(dgsc);
		SceneGraphComponent quadSGC = new SceneGraphComponent("quad");
		tg.setCenterPoint(new double[]{0.0,0.0,0});
		IndexedFaceSet quad = IndexedFaceSetUtility.constructPolygon(pts);
//		MatrixBuilder.euclidean().scale(.8).assignTo(quadSGC);
		quadSGC.setGeometry(getPolarFD(pts)); //quad);
		dgsgr.setWorldNode(quadSGC);
		dgsgr.update();
		dgsgr.getRepresentationRoot().getAppearance().setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.unitCube);			
		sgc.addChild(dgsgr.getRepresentationRoot());
//		sgc.addChild(dgsgr.getRepresentationRoot());
//		sgc.getAppearance().setAttribute(CommonAttributes.METRIC, Pn.HYPERBOLIC);
//		SceneGraphComponent flat = SceneGraphUtility.flatten(sgc);
//		GeometryMergeFactory gmf = new GeometryMergeFactory();
//		SceneGraphComponent result = SceneGraphUtility.createFullSceneGraphComponent("flatmerged");
//		PointSet merged =  RemoveDuplicateInfo.removeDuplicateVertices(gmf.mergeGeometrySets(flat), 10E-3);
//		double[][] verts = merged.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//		for (double[] v : verts)	{
//			Pn.dehomogenize(v, v);
//			double f = 1+Math.sqrt(1 - (v[0]*v[0]+v[1]*v[1]));
//			v[0] /= f; v[1] /= f;
//		}
//		merged.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(verts));
//		result.setGeometry(		merged);
//		result.getAppearance().setAttribute(CommonAttributes.METRIC, Pn.HYPERBOLIC);
		SceneGraphComponent circle = SceneGraphUtility.createFullSceneGraphComponent("circle");
		circle.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		circle.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, true);
		circle.setGeometry(IndexedLineSetUtility.circle(50, 0.0, 0.0,  1));
		sgc.addChild(circle);
		return sgc;
	}

	IndexedFaceSet getPolarFD(double[][] pts)		{
		IndexedFaceSetFactory polarFactory = new IndexedFaceSetFactory();
		int length = pts.length;
		double[][] polarPts = new double[length*2][];
		int[][] fInd = new int[length][4],
			eInd = {{0,1,2,3}};
		for (int i = 0; i<length; ++i)	{
			double[] p1 =  P2.projectP3ToP2(null, pts[i]);
			double[] p2 =  P2.projectP3ToP2(null, pts[(i+1)%length]);
			double[] line = P2.lineFromPoints(null, p1, p2);
			double[] polarPt = Pn.polarize(null, line, Pn.HYPERBOLIC);
			polarPts[i] = new double[]{polarPt[0], polarPt[1], 0, polarPt[2]};
			if (polarPt[2] < 0) Rn.times(polarPts[i], -1, polarPts[i]);
			polarPts[i+length] = polarPts[i].clone();
			polarPts[i+length][3] = polarPts[i][3]/2.0;
			fInd[i][0] = i;
			fInd[i][1] = (i+1)%length;
			fInd[i][2] = fInd[i][1]+length;
			fInd[i][3] = fInd[i][0]+length;
		}
		polarFactory.setVertexCount(polarPts.length);
		polarFactory.setVertexCoordinates(polarPts);
		polarFactory.setEdgeCount(1);
		polarFactory.setEdgeIndices(eInd);
		polarFactory.setFaceCount(length);
		polarFactory.setFaceIndices(fInd);
		polarFactory.setGenerateEdgesFromFaces(false);
		polarFactory.setGenerateFaceNormals(true);
		polarFactory.update();


		return polarFactory.getIndexedFaceSet();
	}
	
}
