package discreteGroup.demo;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import charlesgunn.jreality.geometry.BezierTrianglePatchFactory;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.ParametricTriangularSurfaceFactory;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.TriangleGroup;

/**
 * @author Charles Gunn
 *
 */
public class RationalQuadraticBezierTriangleDemo extends LoadableScene {
	int subdivision = 8;
	private ParametricTriangularSurfaceFactory ptsf;
	private BezierTrianglePatchFactory btpf;
	private double homogeneousFactor;
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		Appearance ap1 = root.getAppearance();
		ap1.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap1.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER,"twoSide");
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		homogeneousFactor = Math.sqrt(2.0)/2.0;
		double[][] cp = {{0,0,1,1},
				{homogeneousFactor,0,homogeneousFactor,homogeneousFactor},
				{1,0,0,1},
				{0,homogeneousFactor,homogeneousFactor,homogeneousFactor},
				{homogeneousFactor,homogeneousFactor,0,homogeneousFactor},
				{0,1,0,1}};
		btpf = new BezierTrianglePatchFactory();
		btpf.setSubdivision(10);
		btpf.setControlPoints(cp);
		ptsf = btpf.getSurfaceFactory();
		ptsf.setGenerateVertexNormals(true);
		ptsf.setGenerateFaceNormals(true);
		ptsf.setGenerateEdgesFromFaces(true);
		btpf.update();
		IndexedFaceSet bar = btpf.getIndexedFaceSet();
		SceneGraphComponent facesNode = new SceneGraphComponent();
		Appearance ap = new Appearance();
		facesNode.setAppearance(ap);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TEXT_SHADER+"."+CommonAttributes.TEXT_SCALE,.001);
		Transformation gt = new Transformation();
		facesNode.setTransformation(gt);
		facesNode.setGeometry(bar);
		DiscreteGroup dg = TriangleGroup.instanceOfGroup("*222");
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg,false);
		dgsgr.setWorldNode(facesNode);
		dgsgr.update();
		root.addChild(dgsgr.getRepresentationRoot());
			return root;
	}
 
	public boolean addBackPlane()	{return false;}

	public int getMetric() {
		return Pn.EUCLIDEAN;
	}
	public boolean isEncompass() {
		return true;
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_1:
					if (e.isShiftDown()) subdivision--;
					else subdivision++;
					ptsf.setSubdivision(subdivision);
					btpf.update();
					break;
				case KeyEvent.VK_2:
					if (e.isShiftDown()) homogeneousFactor /= 1.1;
					else homogeneousFactor *= 1.1;
					btpf.setControlPoints(		new double[][] {{0,0,1,1},
							{homogeneousFactor,0,homogeneousFactor,homogeneousFactor},
							{1,0,0,1},
							{0,homogeneousFactor,homogeneousFactor,homogeneousFactor},
							{homogeneousFactor,homogeneousFactor,0,homogeneousFactor},{0,1,0,1}});
					btpf.update();
					break;
				}
			}
		});
	}
	
}

