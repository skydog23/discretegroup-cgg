package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.ArchimedeanSolids;
import de.jtem.discretegroup.util.WingedEdge;

public class Zeolith extends Assignment {
	boolean simple = true;
	@Override
	public SceneGraphComponent getContent() {
		DiscreteGroup dg = new DiscreteGroup();
		dg.setDimension(3);
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		double c = 2.0/(Math.sqrt(2.0)+4), a = c;
		
		if (!simple) {
			DiscreteGroupElement[] gens = new DiscreteGroupElement[6];
			for (int i = 0; i<6; ++i) {
				gens[i] = new DiscreteGroupElement();
				gens[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			Matrix m = new Matrix();
			MatrixBuilder.euclidean().reflect(new double[]{1,0,0,0}).assignTo(m);
			gens[0].setArray(m.getArray());
			MatrixBuilder.euclidean().reflect(new double[]{0,1,0,0}).assignTo(m);
			gens[1].setArray(m.getArray());
			MatrixBuilder.euclidean().reflect(new double[]{0,1,0,-1}).assignTo(m);
			gens[2].setArray(m.getArray());
			MatrixBuilder.euclidean().reflect(new double[]{1,0,0,-1}).assignTo(m);
			gens[3].setArray(m.getArray());
			MatrixBuilder.euclidean().reflect(new double[]{1,-1,0,0}).assignTo(m);
			gens[4].setArray(m.getArray());
			MatrixBuilder.euclidean().reflect(new double[]{1,0,-1,0}).assignTo(m);
			gens[5].setArray(m.getArray());
			dg.setGenerators(gens);
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(2.0,-1,1000);
			dgsc.setManhattan(true);
			dg.setConstraint(dgsc);
			DiscreteGroupSceneGraphRepresentation repn = new DiscreteGroupSceneGraphRepresentation(dg, false);
			SceneGraphComponent foo = new SceneGraphComponent();
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(8);
			ifsf.setVertexCoordinates(new double[][]{
					{c,c,c}, 
					{3*c-a, a/2, a/2}, 
					{3*c-a, a,0}, 
					{1.5*c, 1.5*c,0}, 
					{3*c-a,0,a}, 
					{3*c-a,0,0},
					{1, a, 0},
					{1, 0, a}});
			ifsf.setFaceCount(3);
			ifsf.setFaceIndices(new int[][]{{0,1,2,3},{2,4, 7, 6},{2,4,5}});
			ifsf.setFaceColors(new Color[]{Color.yellow, Color.red, Color.yellow});
			ifsf.setEdgeCount(1);
			ifsf.setEdgeIndices(new int[][]{{1,2,3}});
			ifsf.setGenerateFaceNormals(true);
			ifsf.setGenerateVertexNormals(false);
//			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.update();
			IndexedFaceSet ifs = ifsf.getIndexedFaceSet();

			foo.setGeometry(ifs);
			repn.setWorldNode(foo);
			repn.update();
			world = repn.getRepresentationRoot();
//			SceneGraphComponent sgc = repn.getRepresentationRoot();
//			sgc = GeometryUtility.flatten(sgc);
		} else {
			dg = new DiscreteGroup();
			dg.setDimension(3);
			DiscreteGroupElement[] gens = new DiscreteGroupElement[3];
			for (int i = 0; i<3; ++i) {
				gens[i] = new DiscreteGroupElement();
				gens[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			double[][] trans = {{2,0,0},{0,2,0},{0,0,2}};
			for (int i = 0; i<3; ++i)
				gens[i].setArray(P3.makeTranslationMatrix(null,trans[i], Pn.EUCLIDEAN));
			dg.setGenerators(gens);
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(2,-1,50);
			dgsc.setManhattan(true);
			dg.setConstraint(dgsc);
			dg.update();
			SceneGraphComponent foo = new SceneGraphComponent();
			MatrixBuilder.euclidean().scale(3*c/1.33).rotateZ(Math.PI/4).assignTo(foo);
			WingedEdge archimedeanSolid = ArchimedeanSolids.archimedeanSolid("4.6.6");
			archimedeanSolid.setFaceAttributes(Attribute.COLORS, null);
			foo.setGeometry(archimedeanSolid);
			double[][] verts = archimedeanSolid.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//			System.err.println("Verts = "+Rn.toString(verts));
			DiscreteGroupSceneGraphRepresentation repn = new DiscreteGroupSceneGraphRepresentation(dg, false);
			repn.setWorldNode(foo);
			repn.update();
			foo = repn.getRepresentationRoot();
			foo.getAppearance().setAttribute("polygonShader.diffuseColor",Color.yellow);
			foo.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
			foo.getAppearance().setAttribute("lineShader.tubeRadius", .02);
			foo.getAppearance().setAttribute("pointShader."+CommonAttributes.POINT_RADIUS, .02);
			foo.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor",new Color(000,100,250));
			foo.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor",new Color(000,100,250));
			foo.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
			world.addChild(foo);
			
			SceneGraphComponent cubesSGC = SceneGraphUtility.createFullSceneGraphComponent();
			foo = new SceneGraphComponent("cube");
			foo.setGeometry(Primitives.cube());
			double[][] edgeMiddles = new double[12][3];
			double[] axis = new double[3];
			for (int i = 0; i<3; ++i)	{
				axis[i] = 1;
				axis[(i+1)%3] = 0;
				axis[(i+2)%3] = 0;
				for (int j = 0; j<2; ++j)	{
					for (int k = 0; k<2; ++k)	 {
						int index = k + 2*j + 4*i;
						edgeMiddles[index][i] = 1;
						edgeMiddles[index][(i+1)%3] = 2*j;
						edgeMiddles[index][(i+2)%3] = 2*k;
						SceneGraphComponent sgc = new SceneGraphComponent();
						MatrixBuilder.euclidean().translate(edgeMiddles[index]).
							rotate(Math.PI/4, axis).scale(.26).assignTo(sgc);
						sgc.addChild(foo);
						cubesSGC.addChild(sgc);
					}
				}
			}
			for (int i = 0; i<12; ++i)	{
			}
			cubesSGC.getAppearance().setAttribute("polygonShader.diffuseColor",Color.red);
			cubesSGC.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);
			world.addChild(cubesSGC);
		}
		return world;
	}

	public static void main(String[] args) {
		new Zeolith().display();
	}
	

}
