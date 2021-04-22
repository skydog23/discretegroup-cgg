/*
 * Created on Jun 24, 2004
 *
 */
package discreteGroup.demo;


import java.util.Vector;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.util.WingedEdge;



/**
 * @author gunn
 *
 */
public class ConwayThurston extends LoadableScene {
	SceneGraphComponent theWorld;
	/**
	 * 
	 */
	SceneGraphComponent icokit;
	WingedEdge we;
	/**
	 * 
	 */
	public ConwayThurston() {
		super();
	}
	
	public SceneGraphComponent makeWorld()	{
		theWorld = new SceneGraphComponent();
		theWorld.setTransformation(new Transformation());
		theWorld.setAppearance(new Appearance());
		
		double[] p0 = {0,0,0,1};
		double[] p1 = {1,1,1,1};
		double[] p2 = {0,1,0,1};
		double[] p3 = {1,0,-1,1};
		
		double[] rot1 = P3.makeRotationMatrix(null, p0, p1, Math.PI*2.0/3.0, Pn.EUCLIDEAN);
		double[] rot2 = P3.makeRotationMatrix(null, p2, p3, Math.PI*2.0/3.0, Pn.EUCLIDEAN);
		
		DiscreteGroupElement[] gens = new DiscreteGroupElement[4];
		gens[0] = new DiscreteGroupElement();
		gens[0].setArray(rot1);
		gens[0].setWord("a");
		
		gens[1] = new DiscreteGroupElement();
		gens[1].setArray(rot2);
		gens[1].setWord("b");
		
		gens[2] = (DiscreteGroupElement) gens[0].getInverse();
		gens[3] = (DiscreteGroupElement) gens[1].getInverse();
		
		DiscreteGroup dg = new DiscreteGroup();
		dg.setGenerators(gens);
		DiscreteGroupSimpleConstraint dgc = new DiscreteGroupSimpleConstraint(6.0, 5);
		dgc.setMaxNumberElements(300);
		dg.setConstraint(dgc);
		dg.update();
		
		//QuadMeshShape line1 = new Wand(.1, 2.0 * Math.sqrt(3.0), 16);
		double[][] profile = {{0,.1,0},{Math.sqrt(3.0),.1,0}};
		IndexedFaceSet line1 = GeometryUtilityOverflow.surfaceOfRevolutionAsIFS(profile,6, 2.0*Math.PI/3.0);
		//GeometryUtility.calculateAndSetNormals(line1);
		double[] zaxis = {1,0,0,1};
		double[] axis2 = {1,-1,-1,1};
		double[] rot3 = P3.makeRotationMatrix(null, zaxis, p1);
		double[] rot4 = P3.makeRotationMatrix(null, zaxis, axis2);

		
		SceneGraphComponent c3 = SceneGraphUtility.createFullSceneGraphComponent();
		c3.getTransformation().setMatrix(rot3);
		c3.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,java.awt.Color.BLUE);
		SceneGraphComponent c4 = SceneGraphUtility.createFullSceneGraphComponent();
		c4.getTransformation().setMatrix(rot4);
		c4.getTransformation().multiplyOnLeft(P3.makeTranslationMatrix(null, p2, Pn.EUCLIDEAN));
		c4.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,java.awt.Color.RED);
		c3.setGeometry(line1);
		c4.setGeometry(line1);
		SceneGraphComponent c5 = SceneGraphUtility.createFullSceneGraphComponent();
		c5.setGeometry(Primitives.cube());
		MatrixBuilder.euclidean().translate(.5,.5,.5).scale(.5).assignTo(c5);
//		c5.getTransformation().setStretch(.5);
//		c5.getTransformation().setTranslation(.5d, .5d, .5d);
		c5.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		
		Vector geom = new Vector();
		geom.add(c3);
		geom.add(c4);
		geom.add(c5);
		
		DiscreteGroupSceneGraphRepresentation dgr = new DiscreteGroupSceneGraphRepresentation(dg);
		dgr.setWorldNode( DiscreteGroupUtility.collectGeometry(geom, null));
		dgr.update();
		SceneGraphComponent dgrepn = dgr.getRepresentationRoot(); //theWorld.addChild(c5);
		theWorld.addChild(dgrepn);
	
		return theWorld;
	}

	public void customize(JMenuBar menuBar, Viewer viewer) {
		MatrixBuilder.euclidean().translate(0,0,10).assignTo(viewer.getCameraPath().getLastComponent());
	}
}
