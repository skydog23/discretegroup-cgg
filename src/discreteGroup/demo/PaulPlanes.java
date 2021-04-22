/*
 * Created on Nov 20, 2009
 *
 */
package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;

public class PaulPlanes extends LoadableScene {

	@Override
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent();
		double[][] pts = {{1,1,0},{0,1,1},{-1,0,1},{-1,-1,0},{0,-1,-1},{1,0,-1}};
		IndexedFaceSet foo = IndexedFaceSetUtility.constructPolygon(pts);
		SceneGraphComponent child = new SceneGraphComponent();
		child.setGeometry(foo);
		world.addChild(child);
		Matrix one = new Matrix();
		MatrixBuilder.euclidean().rotate(2*Math.PI/3, 1,1,1).assignTo(one);
		Matrix two = new Matrix();
		MatrixBuilder.euclidean().rotate(4*Math.PI/3, 1,1,1).assignTo(two);
		DiscreteGroupElement[] gens = new DiscreteGroupElement[3];
		gens[0] = new DiscreteGroupElement();
		gens[1] = new DiscreteGroupElement(Pn.EUCLIDEAN, one.getArray(),"a");
		gens[2] = new DiscreteGroupElement(Pn.EUCLIDEAN, two.getArray(),"b");
		DiscreteGroup dg = new DiscreteGroup();
		dg.setFinite(true);
		dg.setDimension(3);
		dg.setMetric(0);
		dg.setElementList(gens);
		dg.update();
		DiscreteGroupSceneGraphRepresentation repn = new DiscreteGroupSceneGraphRepresentation(dg);
		repn.setWorldNode(world);
		Color[] colorlist = {
				Color.white,
				Color.red,
				Color.blue,
				Color.green,
				Color.black,
				Color.cyan,
				Color.yellow,
				new Color(255,0,255),
		};
		Appearance[] aplist = new Appearance[4];
		for (int i = 0; i<4; ++i)	{
			aplist[i] = new Appearance();
			aplist[i].setAttribute("lineShader.polygonShader.diffuseColor", colorlist[i]);
			aplist[i].setAttribute("pointShader.polygonShader.diffuseColor", colorlist[(i+4)%8]);
			aplist[i].setAttribute("polygonShader.diffuseColor", colorlist[i]);
		}
		repn.setAppList(aplist);
		repn.update();
		return repn.getRepresentationRoot();
	}

}
