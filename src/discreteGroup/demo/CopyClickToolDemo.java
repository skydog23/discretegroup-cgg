/*
 * Created on Mar 8, 2021
 *
 */
package discreteGroup.demo;

import java.awt.Color;

import charlesgunn.jreality.viewer.Assignment;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.tutorial.util.FlyTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import discreteGroup.tools.CopyClickTool;

public class CopyClickToolDemo extends Assignment {

	DiscreteGroupSceneGraphRepresentation dgsgr;
	DiscreteGroup dg = new DiscreteGroup();

	@Override
	public SceneGraphComponent getContent() {
		dg.setFinite(true);
		dg.setMetric(Pn.EUCLIDEAN);
		dg.setDimension(3);
		DiscreteGroupElement dge[] = new DiscreteGroupElement[6];
		double k = .5;
		double[][] tlates = {{k,0,0,1},{0,k,0,1},{0,0,k,1}};
		String[] names = {"x","y","z"}; //,"X","Y","Z"};
		for (int i = 0; i<tlates.length; ++i)	{
			double[] mat = P3.makeTranslationMatrix(null, tlates[i], Pn.EUCLIDEAN);
			dge[2*i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat, names[i]);
			dge[2*i+1] = dge[2*i].getInverse();
		}
		dg.setGenerators(dge);
		dg.setCenterPoint(new double[]{k/2,k/2,k/2,1});
		dg.setConstraint(new DiscreteGroupSimpleConstraint(1));
		dg.update();
		System.err.println("dg count: "+dg.getElementList().length);

		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("geom");
//		sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		DirichletDomain dirdom = new DirichletDomain(dg);
		dirdom.setDirichletDomainOrbit(30);		
		dirdom.update();
		sgc.setGeometry(dirdom.getDirichletDomain());

		dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		dgsgr.setWorldNode(sgc);
		dgsgr.update();

		return dgsgr.getRepresentationRoot();
	}
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		Viewer viewer = jrviewer.getViewer();
		FlyTool ft = new FlyTool();
		ft.setGain(.15);
		CameraUtility.getCameraNode(viewer).addTool(ft);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR,Color.gray);
		CopyClickTool cct = new CopyClickTool(dg, dgsgr);
		SceneGraphComponent dirichletDomain = cct.getSceneGraphComponent();
		dirichletDomain.addTool(cct);
		dgsgr.getFundamentalRegion().addChild(dirichletDomain);
		System.err.println("dgsgr rep root = "+dgsgr.getRepresentationRoot().getName());
		SceneGraphPath path = SceneGraphUtility.getPathsBetween(
				viewer.getSceneRoot(), dgsgr.getRepresentationRoot()).get(0);
		System.err.println("path = "+path.toString());
		}

	public static void main(String[] args) {
		new CopyClickToolDemo().display();
	}

}
