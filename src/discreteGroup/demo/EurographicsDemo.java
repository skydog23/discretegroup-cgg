/*
  * Created on 13.10.2015
 *
 */
package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.geometry.BezierCurve;
import charlesgunn.jreality.geometry.BezierPatchMeshTubeFactory;
import charlesgunn.jreality.geometry.WovenQuadSetFactory;
import charlesgunn.jreality.geometry.projective.CurveCollector;
import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.viewer.Assignment;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetUtility;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;

public class EurographicsDemo extends Assignment {

	boolean justOne = true, 
			reallyJustOne = false, 
			screwOnly = false,
			pureRotation = false,
			pureTranslation = true,
			pureSomething = pureRotation || pureTranslation;
	Color c1 = screwOnly ? Color.green : Color.red, c2 = screwOnly ? Color.yellow : Color.blue;
	@Override
	public SceneGraphComponent getContent() {
		DiscreteGroup d12 = new DiscreteGroup();
		d12.setDimension(3);
		d12.setFinite(true);
		DiscreteGroupElement gens[] = new DiscreteGroupElement[2];
		gens[0] = new DiscreteGroupElement();
		double[] refl1 = P3.makeReflectionMatrix(null, new double[]{0,1,0,0}, Pn.EUCLIDEAN);
		gens[0].setArray(refl1);
		gens[0].setWord("a");
		
		int order = 6;
		double angle = Math.PI/order;
		gens[1] = new DiscreteGroupElement();
		double s = Math.sin(angle),
				c = Math.cos(angle);
		double[] refl2 = P3.makeReflectionMatrix(null, new double[]{s, c,0,0}, Pn.EUCLIDEAN);
		gens[1].setArray(refl2);
		gens[1].setWord("b");
		
		d12.setGenerators(gens);
		d12.update();
		
		// choose two points in the planes y = 0 and sx + cy = 0
		double[][] pts = {{.7, 0, .3, 1}, {c , -s, -.1, 1}};
		IndexedLineSet ils = IndexedLineSetUtility.createCurveFromPoints(pts, false);

		double bezscale = .3, ff = .7, zval = .2;
		double[][] bezpts = { {1, bezscale, zval, 1},
				{1, -bezscale, zval, 1},  
				{ff*(c + s*bezscale) , ff*(-s + c*bezscale), -zval, 1},
				{ff*(c - s*bezscale) , ff*(-s - c*bezscale), -zval, 1}
				},
				mirrorpts = {{-.4,0,.5,1}, {1.2,0,.5,1},{1.2,0,-.5,1},{-.4,0,-.5,1}};
		BezierCurve bc = new BezierCurve(3, bezpts);
		bc.refine();
		bc.refine();
		ils = IndexedLineSetUtility.createCurveFromPoints(bc.getPolygonPoints(), false);

		BezierPatchMeshTubeFactory pbmtf = new BezierPatchMeshTubeFactory(bezpts);
		pbmtf.setFrameFieldType(FrameFieldType.PARALLEL);
		pbmtf.setRadius(.075);
		pbmtf.setCrossSection(WovenQuadSetFactory.crossSection);
		pbmtf.update();
		BezierPatchMesh pbm = pbmtf.getTube();
		pbm.refine();
		pbm.refine();
		pbm.refine();
//		pbm.refine();
		IndexedFaceSet ifs = BezierPatchMesh.representBezierPatchMeshAsQuadMesh(pbm);
		fundDom = SceneGraphUtility.createFullSceneGraphComponent("world");
		fundDom.setGeometry(ifs);
		dgsgr = new DiscreteGroupSceneGraphRepresentation(d12);
		dgsgr.setWorldNode(fundDom);
		dgsgr.update();
		Appearance ap = fundDom.getAppearance();
//		ap.setAttribute("polygonShader.diffuseColor", Color.white);
//		ap.setAttribute("lineShader.diffuseColor", Color.white);
//		ap.setAttribute("pointShader.diffuseColor", Color.white);
//		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		
		int screwN = pureSomething ? 12 : 25;
		double transScale = screwOnly? .2 : (pureTranslation ? 1.6 : 1.0);
		double[] p1 = {2,2,0,1}, 
				v1 = {-transScale*.5, 0, transScale*.5, 0};
		double screwRot = screwOnly? .2 : .3;
		p1 = screwOnly ? new double[]{0,0,0,1} : p1;
		v1 = screwOnly? new double[]{0,0,transScale,0} : v1;
		DiscreteGroup screw = new DiscreteGroup();
		screw.setDimension(3);
		screw.setFinite(true);
		DiscreteGroupElement sGens[] = new DiscreteGroupElement[1];
		sGens[0] = new DiscreteGroupElement();
		sGens[0].setWord("a");
		sGens[0].setArray(
				pureSomething ?
				(pureTranslation ?
				P3.makeTranslationMatrix(null, p1, Rn.add(null, p1, v1), Pn.EUCLIDEAN) :
				P3.makeRotationMatrix(null, p1, v1, 2.0*Math.PI/screwN, Pn.EUCLIDEAN)):
				P3.makeScrewMotionMatrix(null, p1, Rn.add(null, p1, v1), screwRot, Pn.EUCLIDEAN));
		screw.setGenerators(sGens);
		screw.setConstraint(new DiscreteGroupSimpleConstraint(screwN));
		screw.update();
		
		PointRangeFactory prf = new PointRangeFactory();
		prf.setElement0(p1);
		prf.setElement1(v1);
		prf.setFiniteSphere(true);
		prf.setSphereRadius(50.0);
		prf.update();
		screwAxisSGC = SceneGraphUtility.createFullSceneGraphComponent("screwAxis");
		screwAxisSGC.setGeometry(prf.getLine());
		ap = screwAxisSGC.getAppearance();
		ap.setAttribute("lineShader.tubeRadius", .03);
		ap.setAttribute("lineShader.diffuseColor", Color.yellow);
		ap.setAttribute(GeometryUtility.BOUNDING_BOX, Rectangle3D.EMPTY_BOX);
		sdgsgr = new DiscreteGroupSceneGraphRepresentation(screw);
		sdgsgr.setWorldNode(dgsgr.getRepresentationRoot());
		if (pureSomething) 
			MatrixBuilder.euclidean().scale(.6).assignTo(dgsgr.getRepresentationRoot());
		Appearance aplist[] = new Appearance[screwN];
		sdgsgr.update();
		DiscreteGroupElement dge[] = sdgsgr.getElementList();
		for (int i = 0; i<screwN; ++i)	{
			double d = ((double) i)/(screwN-1.0);
			Color ct = AnimationUtility.linearInterpolation(c1, c2, d);
			dge[i].setColorIndex(i);
			aplist[i] = new Appearance();
			aplist[i].setAttribute("polygonShader.diffuseColor", ct);
			System.err.println("setting color to "+ct.toString());
		}
		sdgsgr.setAppList(aplist);
		sdgsgr.update();
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world2");
		sceneChooser = new SceneGraphComponent();
		sceneChooser.addChildren(fundDom, dgsgr.getRepresentationRoot(), sdgsgr.getRepresentationRoot());
		if (screwOnly)	sceneChooser = getScrewGeometry(sGens[0].getArray(), 12, 4, screwN);
		world.addChildren(sceneChooser, screwAxisSGC);
//		sceneChooser.setSelectedChild(0);
		world.getAppearance().setAttribute("polygonShader.diffuseColor", Color.red);
		
		mirrors = SceneGraphUtility.createFullSceneGraphComponent("mirrors");
		SceneGraphComponent mirror1 = SceneGraphUtility.createFullSceneGraphComponent("mirror1"), mirror2 = SceneGraphUtility.createFullSceneGraphComponent("mirror2"), axis = SceneGraphUtility.createFullSceneGraphComponent("mirror2");
		mirrors.addChildren(axis, mirror1, mirror2);
		IndexedFaceSet mirrorIFS = IndexedFaceSetUtility.constructPolygon(mirrorpts);
		mirror1.setGeometry(mirrorIFS);
		mirror2.setGeometry(mirrorIFS);
		IndexedLineSet axisILS = IndexedLineSetUtility.createCurveFromPoints(
				new double[][]{{0,0,1,1},{0,0,-1,1}}, false);
		axis.setGeometry(axisILS);
		axis.getAppearance().setAttribute("lineShader.diffuseColor", Color.green);
		MatrixBuilder.euclidean().rotate(-Math.PI/order, new double[]{0,0,1}).assignTo(mirror2);
		ap = mirrors.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("lineShader.tubeRadius", .005);
		ap.setAttribute("pointShader.pointRadius", .005);
		ap.setAttribute("pointShader.diffuseColor", Color.black);
		ap.setAttribute("lineShader.diffuseColor", Color.black);
		ap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY, .5);
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		mirror2.getAppearance().setAttribute("polygonShader.diffuseColor", Color.cyan);
		if (!screwOnly) world.addChild(mirrors);
		
		return world;
	}

	private SceneGraphComponent getScrewGeometry(double[] screw, int numCircs, int numRings, int count) {
		SceneGraphComponent helicesSGC = SceneGraphUtility.createFullSceneGraphComponent("helices");
		int circleSize = 25;
		for (int k = 0; k<numRings; ++k)	{
			double R = 1.0 - k/(numRings-.001);
			for (int j = 0; j<numCircs; ++j)	{
				double[] screwPow = Rn.identityMatrix(4);
				double //R = Math.random() * 1,
						r = .08* R,
						angle = Math.random() * 2.0 *Math.PI;
//				R = 1;
				r = .04 * R;
				angle = 2.0*Math.PI * (j/(numCircs-1.0));
				double		c = R * Math.cos(angle),
						s = R * Math.sin(angle);
				
				IndexedLineSet circle = IndexedLineSetUtility.circle(circleSize, c, s, r);
				double[][] circlePoints = IndexedLineSetUtility.extractCurve(null, circle, 0); //circle.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
				CurveCollector cc = new CurveCollector(count, circlePoints.length, circlePoints[0].length);
				cc.setMetric(Pn.EUCLIDEAN);
				cc.reset();
				for (int i = 0; i<count; ++i)	{
					double[][] tmp = Rn.matrixTimesVector(null, screwPow, circlePoints);
					cc.addCurve(tmp);
					Rn.times(screwPow, screwPow, screw);
				}
				SceneGraphComponent oneHelix = SceneGraphUtility.createFullSceneGraphComponent("oneHelix");
				IndexedFaceSetUtility.calculateAndSetVertexNormals(cc.getMesh());
				oneHelix.setGeometry(cc.getMesh());
//				QuadMeshUtility.generateAndSetEdgesFromQuadMesh(cc.getMesh());
				Color ct = AnimationUtility.linearInterpolation(c1, c2, 1.0-R);
				Appearance ap = oneHelix.getAppearance();
				ap.setAttribute("polygonShader.diffuseColor", ct);
				helicesSGC.addChild(oneHelix);
			}
			helicesSGC.getAppearance().setAttribute(CommonAttributes.FLIP_NORMALS_ENABLED, true);
		}
		return helicesSGC;
	}

	int whichMode = 0;
	private SceneGraphComponent sceneChooser;
	private DiscreteGroupSceneGraphRepresentation sdgsgr;
	private DiscreteGroupSceneGraphRepresentation dgsgr;
	private SceneGraphComponent fundDom;
	private SceneGraphComponent mirrors;
	private SceneGraphComponent screwAxisSGC;
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		jrviewer.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
		Component canvas = (Component) (jrviewer.getViewer().getViewingComponent());
		canvas.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				switch(arg0.getKeyCode())	{
				case KeyEvent.VK_1:
					whichMode++;
					whichMode = whichMode%4;
					switch(whichMode) {
					case 0:
						fundDom.setVisible(true);
						mirrors.setVisible(true);
						screwAxisSGC.setVisible(false);
						dgsgr.getRepresentationRoot().setVisible(false);
						break;
					case 1:
//						fundDom.setVisible(false);
						mirrors.setVisible(true);
						dgsgr.getRepresentationRoot().setVisible(true);
						sdgsgr.getRepresentationRoot().setVisible(false);
						break;
					case 2:
//						fundDom.setVisible(false);
						mirrors.setVisible(false);
						dgsgr.getRepresentationRoot().setVisible(true);
						sdgsgr.getRepresentationRoot().setVisible(false);
						break;
					case 3:
//						fundDom.setVisible(false);
						mirrors.setVisible(false);
						screwAxisSGC.setVisible(true);
						dgsgr.getRepresentationRoot().setVisible(true);
						sdgsgr.getRepresentationRoot().setVisible(true);
						break;
					}
//					sceneChooser.setSelectedChild(whichMode);
					break;
				default:
					break;
				}
					
			}
		});
	}

	public static void main(String[] args) {
		new EurographicsDemo().display();
	}
}
