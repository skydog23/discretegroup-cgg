package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.projgeom.PlueckerLineGeometry;

public class RinusPolyhedronS3Screw extends Assignment {


	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
		world2 = SceneGraphUtility.createFullSceneGraphComponent("world2"),
		oneStrip = SceneGraphUtility.createFullSceneGraphComponent("strip"),
		triangles = SceneGraphUtility.createFullSceneGraphComponent("tris");
	protected double alpha = .6665, //Math.PI/5,
			beta = 0.1,
//		tlateLength = .2,
		implodeFactor = .25,
		pitch = 1.0;
	protected int family = 5,
			diagonal = 2,
			period = 5,
			 numStrips = 5;
	IndexedFaceSetFactory triFact;

	Viewer viewer;
	int metric = Pn.ELLIPTIC;
	double[][] falloffs =   {{1.5,.25,0},{.5,.5,0},{.5, .5, 0}};
	double[][] cameraClips = {{.001,2},{.01, 1000},{.01,-.05}};
	double distance = .5;
	double[] unitD = {Math.tanh(distance), distance, Math.tan(distance)};
	private double sideLength;
	private double[][] verts;
	boolean bandedColors = false,
		showLines = false,
		cliffordParallels = true;		// force alpha = beta

	Color[] colors = {Color.blue, Color.red, Color.yellow, Color.magenta, Color.green, Color.cyan, new Color(255,100,0)};
	@Override
	public SceneGraphComponent getContent() {
		world.addChildren(triangles);
		for (int i = 0; i<numStrips; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			triangles.addChild(sgc);
			sgc.addChild(oneStrip);
//			if (bandedColors)	{
				sgc.setAppearance(new Appearance());
				sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i%colors.length]);				
//			}
		}
		updateTriangle();
		world.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute("lineShader.tubeRadius", .005);
		world.getAppearance().setAttribute(CommonAttributes.RADII_WORLD_COORDINATES, true);
		world2.addChild(world);
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(world2);
		return world2;
	}
	int[][] pairs = {{0,1},{1,2},{2,0},{0,3},{1,3}};

	private void updateTriangle() {
		double[] rotdnM = MatrixBuilder.euclidean().rotateZ(2*diagonal*Math.PI/family).getArray(),
				rothdnM = MatrixBuilder.euclidean().rotateZ(diagonal*Math.PI/family).getArray(),
				rot1nM = MatrixBuilder.euclidean().rotateZ(2*Math.PI/family).getArray();
		// solve first for the side length
//		double c = Math.cos(alpha), s = Math.sin(alpha);
//		sideLength = Math.acos(c*c + s*s*Math.cos(diagonal*2*Math.PI/family));
//		double p =  Math.acos(c*c + s*s*Math.cos(diagonal*Math.PI/family)),
//				h = Math.acos(c/Math.cos(p/2));
//		double x = 2*Math.acos(Math.cos(sideLength/2)/Math.cos(p/2));
//		double d = 2*Math.asin(Math.sin(x/2)/Math.cos(h));
		double d = (Math.PI*2.0/period);
		double[] P = {0,0,Math.sin(d/2),Math.cos(d/2)},
				R = {Math.sin(alpha), 0, 0, Math.cos(alpha)},
				V = {0, Math.sin(beta), Math.cos(beta), 0},
				S = Rn.add(null, R, V),
				m = PlueckerLineGeometry.lineFromPoints(null, R, S),
				q = {0, 0, 1, -Math.sin(d/2)},		// horiz plane through P
				A = PlueckerLineGeometry.lineIntersectPlane(null, m, q),
				q2 = {0, 0, 1, Math.sin(d/2)},		// horiz plane through P
				B = PlueckerLineGeometry.lineIntersectPlane(null, m, q2);
		double r = Pn.distanceBetween(P, A, Pn.ELLIPTIC);
		sideLength = 2 * Pn.distanceBetween(R, A, Pn.ELLIPTIC);
		double h = Math.acos(Math.cos(sideLength)/Math.cos(sideLength/2));	// altitude of equ. tri. with side sidelength
		double[] V1 = {1,0,0,0}, 
				V2 = {0, -Math.cos(beta), Math.sin(beta), 0};
		double angle = 0.0, da = .2, error = 1.0, olddis = 0.0;
		double[] C = null, D = null;
		do {
			double[] dir = Rn.add(null, Rn.times(null, Math.cos(angle), V1), Rn.times(null, Math.sin(angle), V2));
			double[] circlePoint = Pn.dragTowards(null, R, dir, h, Pn.ELLIPTIC);
			Pn.normalize(circlePoint, circlePoint, Pn.ELLIPTIC);
			double[] axisPoint = {0, 0, circlePoint[2], circlePoint[3]};
			double dis = r - Pn.distanceBetween(circlePoint, axisPoint, Pn.ELLIPTIC);
			System.err.println("distance = "+dis);
			if (Math.abs(dis) < 10E-5)	{
				C = circlePoint.clone();
				D = circlePoint.clone();
				D[1] = -D[1];
				D[2] = -D[2];
				break;
			}
			if ( dis * olddis < 0)		{
				// reverse direction
				da = da * -.5;
			}
			angle += da;
			olddis = dis;
			if (angle >= 2*Math.PI)	
				throw new IllegalStateException("No triangle is possible");
		} while (error > 10E-4);
		
		System.err.println("r s r d k = "+alpha+"\t"+sideLength+"\t"+r+"\t"+d+"\t"+period);
		double[] movedO = Pn.dragTowards(null, P3.originP3, new double[]{0,0,1,0}, d, Pn.ELLIPTIC);
		double[] tlateM = P3.makeTranslationMatrix(null, movedO, Pn.ELLIPTIC);
		pitch = (diagonal*Math.PI/family)/d;
		double[] das = {Math.atan2(A[1], A[0]), Math.atan2(C[1],C[0]), Math.atan2(A[2],A[3]),Math.atan2(C[2],C[3])},
			dasAB = {Math.atan2(A[1], A[0]), Math.atan2(B[1],B[0]), Math.atan2(A[2],A[3]),Math.atan2(B[2],B[3])};
		
		double[][] verts = {B, A, C, D};
		System.err.println("verts = \n"+Rn.toString(verts));
		Pn.normalize(verts, verts, Pn.ELLIPTIC);
		double[] ds = new double[5];
		for (int i = 0; i<5; ++i)	{
			ds[i] = Pn.distanceBetween(verts[pairs[i][0]], verts[pairs[i][1]], Pn.ELLIPTIC);
		}
		System.err.println("Distances = "+Rn.toString(ds));
		implode(verts);
		
		for (int i = 0; i<family; ++i)	{
			SceneGraphComponent sgc = null;
			if (oneStrip.getChildComponentCount() <= i) {
				sgc = new SceneGraphComponent();
				oneStrip.addChild(sgc);
				sgc.setGeometry(triFact.getIndexedFaceSet());
				if (!bandedColors)	{
					sgc.setAppearance(new Appearance());
					sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i%colors.length]);				
				}
			}
			else sgc = oneStrip.getChildComponent(i);
			double[] moo = getScrewMotion(i*(das[1]-das[0]),i*(das[3]-das[2]));
			new Matrix(moo).assignTo(sgc);	
		}
		numStrips = (int) (2*Math.PI/d + .5);
		for (int i = 0; i<numStrips; ++i)	{
			int index = -numStrips/2 + i;
			SceneGraphComponent sgc = null;
			if (triangles.getChildComponentCount() <= i) {
				sgc = new SceneGraphComponent();
				sgc.addChild(oneStrip);
					sgc.setAppearance(new Appearance());
					sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i%colors.length]);				
				triangles.addChild(sgc);
			}
			else sgc = triangles.getChildComponent(i);
			sgc.setVisible(true);
			double[] moo = getScrewMotion(i*(dasAB[1]-dasAB[0]),i*(dasAB[3]-dasAB[2]));
			new Matrix(moo).assignTo(sgc);
		}
		for (int i = numStrips; i<triangles.getChildComponentCount(); ++i)
			triangles.getChildComponent(i).setVisible(false);
		
	}
	int[][] indices = {{0,1,4,3},{1,2,5,4},{2,0,3,5},{6,7,10,9}, {7,8,11,10},{8,6,9,11}};
	int[][] edgeIndices = {{0,1},{1,2},{2,0},{7,8},{8,6}};
	private TextSlider periodSlider;
	private void implode(double[][] verts2) {
		Pn.normalize(verts2, verts2, Pn.ELLIPTIC);
		double[][] twelveVerts = new double[12][];
		double[][] t1 = shrinkTriangle( verts2[0], verts2[1], verts2[2]);
		for (int i = 0; i<3;++i) {
			twelveVerts[i] = verts2[i].clone();
			twelveVerts[3+i] = t1[i];
		}
		twelveVerts[6] = verts2[1].clone();
		twelveVerts[7] = verts2[0].clone();
		twelveVerts[8] = verts2[3].clone();
		double[][] t2 = shrinkTriangle( verts2[1], verts2[0], verts2[3]);
		for (int i = 0; i<3;++i) {
			twelveVerts[9+i] = t2[i];
		}
		if (triFact == null)
			triFact=new IndexedFaceSetFactory(); //.constructPolygonFactory(triFact, verts, Pn.ELLIPTIC);
		triFact.setVertexCount(twelveVerts.length);
		triFact.setVertexCoordinates(twelveVerts);
		triFact.setEdgeCount(edgeIndices.length);
		triFact.setEdgeIndices(edgeIndices);
		triFact.setFaceCount(indices.length);
		triFact.setFaceIndices(indices);
		triFact.setMetric(Pn.ELLIPTIC);
		triFact.setGenerateFaceNormals(true);
		triFact.update();	
	}
	private double[][] shrinkTriangle( double[] ds,
			double[] ds2, double[] ds3) {
		double[] center = Rn.add(null, Rn.add(null, ds, ds2), ds3);
		Pn.normalize(center, center, Pn.ELLIPTIC);
		double[][] result = new double[3][];
		result[0] = Pn.linearInterpolation(null, ds, center, implodeFactor, Pn.ELLIPTIC);
		result[1] = Pn.linearInterpolation(null, ds2,  center,implodeFactor, Pn.ELLIPTIC);
		result[2] = Pn.linearInterpolation(null,  ds3, center, implodeFactor, Pn.ELLIPTIC);
		return result;
	}

	@Override
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		JCheckBox colorB = new JCheckBox("Banded color");
		colorB.setSelected(bandedColors);
		colorB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				bandedColors = ((JCheckBox)arg0.getSource()).isSelected();
				for (int i = 0; i<oneStrip.getChildComponentCount(); ++i)	{
					SceneGraphComponent sgc = oneStrip.getChildComponent(i);
					sgc.getAppearance().setAttribute("polygonShader.diffuseColor",
							bandedColors ? Appearance.INHERITED : colors[i%colors.length]);				
				}

			}
		});
		container.add(colorB);
		
		final TextSlider aSlider = new TextSlider.Double("alpha",  SwingConstants.HORIZONTAL, 0.0, Math.PI/2, alpha);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				alpha = aSlider.getValue().doubleValue();
				updateTriangle();
				//viewer.renderAsync();
			}
		});
		container.add(aSlider);
		final TextSlider beSlider = new TextSlider.Double("beta",  SwingConstants.HORIZONTAL, 0.0, Math.PI/2, beta);
		beSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				beta = beSlider.getValue().doubleValue();
				updateTriangle();
				//viewer.renderAsync();
			}
		});
		container.add(beSlider);
		final TextSlider bSlider = new TextSlider.Double("implode",  SwingConstants.HORIZONTAL,
				-1, 1, implodeFactor);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				implodeFactor = bSlider.getValue().doubleValue();
				updateTriangle();
//				triangles.getAppearance().setAttribute("polygonShader.implodeFactor", implodeFactor);
				//viewer.renderAsync();
			}
		});
		container.add(bSlider);
		final TextSlider cSlider = new TextSlider.Double("tlate",  SwingConstants.HORIZONTAL,
				-Math.PI, Math.PI, 0.0);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double d = cSlider.getValue().doubleValue();
				double[] tlateM = Rn.identityMatrix(4);
				tlateM[10] = tlateM[15] = Math.cos(d);
				tlateM[14] = -(tlateM[11] = Math.sin(d));
				MatrixBuilder.elliptic(new Matrix(tlateM)).rotateZ(pitch*d).assignTo(world);
				//viewer.renderAsync();
			}
		});
		container.add(cSlider);
		periodSlider = new TextSlider.Double("period",  SwingConstants.HORIZONTAL,
				0, 40, period);
		container.add(periodSlider);
		
		final TextSlider fSlider = new TextSlider.Integer("family",  SwingConstants.HORIZONTAL,
				1,10, family);
		fSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				family = fSlider.getValue().intValue();
				updateTriangle();
			}
		});
		container.add(fSlider);
		final TextSlider ddSlider = new TextSlider.Integer("diagonal",  SwingConstants.HORIZONTAL,
				1,10,diagonal);
		ddSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				diagonal = ddSlider.getValue().intValue();
				updateTriangle();
			}
		});
		container.add(ddSlider);
		
		final TextSlider dSlider = new TextSlider.Integer("bands",  SwingConstants.HORIZONTAL,
				0,10,family);
		dSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				int n = dSlider.getValue().intValue();
				for (int i = 0; i<triangles.getChildComponentCount(); ++i)	{
					triangles.getChildComponent(i).setVisible(i<n);
				}
			}
		});
		container.add(dSlider);
		final TextSlider eslider = new TextSlider.Integer("strips",  SwingConstants.HORIZONTAL,
				0,10,family);
		eslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				int n = eslider.getValue().intValue();
				for (int i = 0; i<oneStrip.getChildComponentCount(); ++i)	{
					oneStrip.getChildComponent(i).setVisible(i<n);
				}
			}
		});
		container.add(eslider);
		return container;
	}
	protected double[] getScrewMotion(double alpha, double beta) {
		// TODO Auto-generated method stub
		double c = Math.cos(alpha), s = Math.sin(alpha), cb = Math.cos(beta), sb = Math.sin(beta);
		return new double[]{
				c,-s,0,0,
				s, c, 0, 0,
				0, 0,cb, sb, 
				0, 0, -sb, cb
				};
	}
	
	public static void main(String[] args) {
		new RinusPolyhedronS3Screw().display();
	}
	@Override
	public void setupJRViewer(JRViewer v) {
		// TODO Auto-generated method stub
		super.setupJRViewer(v);
		v.registerPlugin(new TermesSpherePlugin());
		v.setPropertiesFile("RinusPolyhedronS3.xml");
		// properties file for webstart and java preferences
		v.setPropertiesResource(RinusPolyhedronS3.class, "RinusPolyhedronS3.xml");
	}
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		viewer = jrviewer.getViewer();
		setupMetric();
		MatrixBuilder.elliptic().translate(new double[]{0,0,1,0}).
			assignTo(CameraUtility.getCameraNode(viewer));
		Scene scene = jrviewer.getPlugin(Scene.class);
		scene.getAvatarComponent().setTransformation(null);
	}

	public void setupMetric()	{
		FlyTool flytool = new FlyTool();
		flytool.setGain(.1);
		CameraUtility.getCameraNode(viewer).addTool(flytool);
		SceneGraphUtility.removeLights(viewer);
		SceneGraphComponent lightNode = makeLights();
		world.addChild(lightNode);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		// this is all we have to do to tell the backend to use the non-euclidean vertex shader
		viewer.getSceneRoot().getAppearance().setAttribute("useGLSL",true);
		viewer.getSceneRoot().getAppearance().setAttribute("oneGLSL",true);
		Scene scene = jrviewer.getPlugin(Scene.class);
//		MatrixBuilder.euclidean().scale(2.0).assignTo(scene.getAvatarComponent());
		final SceneGraphComponent cont = scene.getContentComponent();
		
		cont.addTool(new RotateShapeTool());
		cont.addTool(new TranslateShapeTool());

		resetScene();
	}

	/**
	 * resets the transformations of the scene camera, avatar, and light nodes.
	 */
	public void resetScene() {
		CameraUtility.getCamera(viewer).setNear(cameraClips[metric+1][0]);
		CameraUtility.getCamera(viewer).setFar( cameraClips[metric+1][1]);

//		MatrixBuilder.init(null, metric).translate(0,0,unitD[metric+1]).assignTo(lightNode);
	}

	public SceneGraphComponent makeLights() {
		SceneGraphComponent lightNode = new SceneGraphComponent();
		lightNode.setName("lights");
		double f = .6;
		double[][] lightlocations = {
				{1,1,1,f},
				{1,-1,-1,f},
				{-1,-1,1,f},
				{-1,1,-1,f},
				{-1,-1,-1,f},
				{1,1,-1,f},
				{1,-1,1,f},
				{-1,1,1,f}
		};
		for( int i = 0; i < 8; ++i )	{
//			double[] axis = {-1.2,-1.6,-2,1};
//			if( i > 0 )
//				axis[i-1] = 1; 
//			else 
//				axis = new double[]{1.8,1.3,1,1};
			SceneGraphComponent l0 = SceneGraphUtility.createFullSceneGraphComponent("light0");
			PointLight pointLight = new PointLight();
			int c[] = {255, 255, 255};
			if( i < 3 )
				c[i] = 200;
			pointLight.setColor(new Color(c[0], c[1], c[2]));
			pointLight.setIntensity(.75);
			l0.getTransformation().setMatrix( P3.makeTranslationMatrix(null, lightlocations[i], metric));		
			l0.setLight(pointLight);
			lightNode.addChild(l0);
			//			dl.setFalloff(falloffs[metric+1]);
		}
		return lightNode;
	}

}
