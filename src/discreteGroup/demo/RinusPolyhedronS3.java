package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.geometry.projective.PointRangeFactory;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.tools.RotateShapeTool;
import charlesgunn.jreality.tools.TranslateShapeTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class RinusPolyhedronS3 extends Assignment {


	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
		lines = SceneGraphUtility.createFullSceneGraphComponent("lines"),
		oneStrip = SceneGraphUtility.createFullSceneGraphComponent("strip"),
		triangles = SceneGraphUtility.createFullSceneGraphComponent("tris");
	protected double alpha = .6665, //Math.PI/5, 
		beta = alpha,
		originalBeta = alpha,
		tlateLength = .2,
		implodeFactor = .25;
	PointRangeFactory prf = new PointRangeFactory();
	IndexedFaceSetFactory triFact;
	private double[] point0, point1, point2, point3;
	int numStrips = 5;
	Viewer viewer;
	int metric = Pn.ELLIPTIC;
	double[][] falloffs =   {{1.5,.25,0},{.5,.5,0},{.5, .5, 0}};
	double[][] cameraClips = {{.001,2},{.01, 1000},{.01,-.05}};
	double distance = .5;
	double[] unitD = {Math.tanh(distance), distance, Math.tan(distance)};
	double[] rotM = MatrixBuilder.euclidean().rotateY(4*Math.PI/5.0).getArray();
	private double sideLength;
	private double[][] verts;
	boolean bandedColors = false,
		showLines = false,
		cliffordParallels = true;		// force alpha = beta

	Color[] colors = {Color.blue, Color.red, Color.yellow, Color.magenta, Color.green};
	@Override
	public SceneGraphComponent getContent() {
		world.addChildren(lines, triangles);
		setupLine();
		IndexedLineSet line = prf.getLine();
		lines.setVisible(false);
		for (int i = 0; i<5; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			lines.addChild(sgc);
			sgc.setGeometry(line);
			MatrixBuilder.euclidean().rotateY(i*2*Math.PI/5.0).assignTo(sgc);
		}
		for (int i = 0; i<numStrips; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			triangles.addChild(sgc);
			sgc.addChild(oneStrip);
//			if (bandedColors)	{
				sgc.setAppearance(new Appearance());
				sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i%colors.length]);				
//			}
		}
		update();
		world.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute("lineShader.tubeRadius", .005);
		return world;
	}
	int[][] pairs = {{0,1},{1,2},{2,0},{0,3},{1,3}};
	private void update() {
		System.err.println("In update");
		if (cliffordParallels) {
			beta = alpha;
		} else 
			beta = originalBeta;
		point0 = new double[]  {0, Math.cos(beta), Math.sin(beta), 0};
		point1 = new double[] {Math.sin(alpha), 0, 0, Math.cos(alpha)};
		point2 = Rn.add(null, point0, point1);
		verts = new double[4][];
		// all the points we are going to find will lie on this line and its rotated image 
		// tplueckerLine
		double[] plueckerLine = PlueckerLineGeometry.lineFromPoints(null, point1, point0),
			polarPlueckerLine = PlueckerLineGeometry.polarize(null, plueckerLine, Pn.ELLIPTIC);
		double[] tp1 = Rn.matrixTimesVector(null, rotM, point1),
		tp0 = Rn.matrixTimesVector(null, rotM, point0);
		double[] tplueckerLine = PlueckerLineGeometry.lineFromPoints(null, tp1, tp0);
		// begin with the first corner at the point of the line closest to the y-axis
		// this will later be adjusted to guarantee that its rotated image stays the
		// same distance from the y-axis
		verts[0] = point1.clone();
		for (int i = 0; i<10; ++i)	{
			double[] normalPlane = PlueckerLineGeometry.lineJoinPoint(null, polarPlueckerLine, verts[0]); //point0.clone();
			// rotate the points by 4Pi/5 -- the "neighbor" 
			// calculate the midpoint of the rotated side by cutting with normal plane
			// at verts[0]
			double[] midpoint = PlueckerLineGeometry.lineIntersectPlane(null, tplueckerLine, normalPlane);
			double alt = Pn.distanceBetween(verts[0], midpoint, Pn.ELLIPTIC);
			if (alt > Math.PI/2) {	// aargh! spherical space!
				Rn.times(midpoint, -1, midpoint);
				alt = Math.PI - alt; 
			}
			System.err.println("Altitude = "+alt);
			sideLength = sideFromAltitude(alt);
			System.err.println("side = "+sideLength);
			verts[1] = Pn.dragTowards(null, midpoint, tp0, sideLength/2, Pn.ELLIPTIC);
			verts[2] = Pn.dragTowards(null, midpoint, tp0, -sideLength/2, Pn.ELLIPTIC);
			verts[3] = Pn.dragTowards(null, verts[0], point0, sideLength, Pn.ELLIPTIC);
			Pn.normalize(verts, verts, Pn.ELLIPTIC);
			// compare the distances from the y-axis of v0 and v2: should be same
			double v0d = Math.asin(Math.sqrt(verts[0][0]*verts[0][0]+verts[0][2]*verts[0][2])),
				v2d = Math.asin(Math.sqrt(verts[2][0]*verts[2][0]+verts[2][2]*verts[2][2]));
			System.err.println("v0d = "+v0d+" v2d = "+v2d);
			if (Math.abs(v0d-v2d)<10E-8) break;
			verts[0] = Pn.dragTowards(null, point1, point0, .25*sideLength, Pn.ELLIPTIC);
		}
		double[] ds = new double[5];
		for (int i = 0; i<5; ++i)	{
			ds[i] = Pn.distanceBetween(verts[pairs[i][0]], verts[pairs[i][1]], Pn.ELLIPTIC);
		}
		System.err.println("Distances = "+Rn.toString(ds));
		implode(verts);
		
		tp1 = Rn.matrixTimesVector(null, rotM, verts[0]);
		tlateLength = Pn.distanceBetween(tp1, verts[2], Pn.ELLIPTIC);
		boolean firsttime = oneStrip.getChildComponentCount() == 0;
		for (int i = 0; i<5; ++i)	{
			SceneGraphComponent sgc = null;
			if (firsttime) {
				sgc = new SceneGraphComponent();
				oneStrip.addChild(sgc);
				sgc.setGeometry(triFact.getIndexedFaceSet());
				if (!bandedColors)	{
					sgc.setAppearance(new Appearance());
					sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i%colors.length]);				
				}
			}
			else sgc = oneStrip.getChildComponent(i);
//			double[] target = Pn.dragTowards(null, new double[]{0,0,0,1}, new double[]{0,1,0,0}, -i*tlateLength, Pn.ELLIPTIC);
			double[] target = {0, Math.sin(-i*tlateLength), 0, Math.cos(-i*tlateLength)};
			double[] moo = MatrixBuilder.elliptic().rotateY(i*4*Math.PI/5.0).getArray();
			double[] slide = getCliffordTlateYZ(i*tlateLength);	
			new Matrix(Rn.times(null, moo, slide)).assignTo(sgc);
		}
		for (int i = 0; i<numStrips; ++i)	{
			int index = -numStrips/2 + i;
			SceneGraphComponent sgc = triangles.getChildComponent(i);
			double[] target = {0, Math.sin(-index*sideLength), 0, Math.cos(-index*sideLength)};
			MatrixBuilder.elliptic().rotateY(index*sideLength).translate(target).assignTo(sgc);	
		}
	}
	private double sideFromAltitude(double alt) {
		int count = 10;
		double result = alt, diff = 0.0;
		// use cos alt = cos s / (cos (s/2))
		for (int i = 0; i<count; ++i)	{
			double alt2 = Math.acos(Math.cos(result)/Math.cos(result/2));
			diff = Math.abs(alt - alt2);
			if (diff < 10E-10) return result;
			double tdiff = alt-alt2;
			result += tdiff;
		}
		System.err.println("error = "+diff);		
		return result;
	}
	private void setupLine() {
		prf.setFiniteSphere(false);
		prf.setSphereRadius(5.0);
//		prf.setNumberOfSamples(12);
		updateLine();
	}
		
	private void updateLine() {
		point0 = new double[]  {0, Math.cos(alpha), Math.sin(alpha), 0};
		point1 = new double[] {Math.sin(alpha), 0, 0, Math.cos(alpha)};
		point2 = Rn.add(null, point0, point1);
		prf.setElement0(point1);
		prf.setElement1(point2);
		prf.update();
	}
	
	private void getDihedralAngle() {
		// print out angle
		double[] m5 = MatrixBuilder.elliptic().rotateY(2*Math.PI/5.0).getArray();
		double[] next = Rn.matrixTimesVector(null, m5, point1);
		double[] o3 = {0,0,1},
			here3 = {point1[0], point1[2], point1[3]},
			next3 = {next[0], next[2], next[3]};
		double[] line0 = P2.lineFromPoints(null, here3, o3),
			line1 = P2.lineFromPoints(null, here3, next3);
		double angle = Pn.angleBetween(line0, line1, Pn.ELLIPTIC);
		System.err.println("Angle = "+angle);

	}

	int[][] indices = {{0,1,4,3},{1,2,5,4},{2,0,3,5},{6,7,10,9}, {7,8,11,10},{8,6,9,11}};
	int[][] edgeIndices = {{0,1},{1,2},{2,0},{7,8},{8,6}};
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
		Box hbox = Box.createHorizontalBox();
		container.add(hbox);
		
		final TextSlider aSlider = new TextSlider.Double("alpha",  SwingConstants.HORIZONTAL, 0.0, Math.PI/2, alpha);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				alpha = aSlider.getValue().doubleValue();
				update();
				//viewer.renderAsync();
			}
		});
		container.add(aSlider);
		final TextSlider aaSlider = new TextSlider.Double("beta",  SwingConstants.HORIZONTAL, 0.0, Math.PI/2, originalBeta);
		aaSlider.setEnabled(!cliffordParallels);
		aaSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				originalBeta = aaSlider.getValue().doubleValue();
				update();
				//viewer.renderAsync();
			}
		});
		container.add(aaSlider);
		
		final TextSlider bSlider = new TextSlider.Double("implode",  SwingConstants.HORIZONTAL,
				-1, 1, implodeFactor);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				implodeFactor = bSlider.getValue().doubleValue();
				update();
//				triangles.getAppearance().setAttribute("polygonShader.implodeFactor", implodeFactor);
				//viewer.renderAsync();
			}
		});
		container.add(bSlider);
		final TextSlider cSlider = new TextSlider.Double("tlate",  SwingConstants.HORIZONTAL,
				-Math.PI, Math.PI, 0.0);
		cSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				double tlate = cSlider.getValue().doubleValue();
				double[] tlateM = getCliffordTlateYZ(tlate);
				new Matrix(tlateM).assignTo(world);
				//viewer.renderAsync();
			}
		});
		container.add(cSlider);
		final TextSlider dSlider = new TextSlider.Integer("bands",  SwingConstants.HORIZONTAL,
				0,5,5);
		dSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				int n = dSlider.getValue().intValue();
				for (int i = 0; i<5; ++i)	{
					triangles.getChildComponent(i).setVisible(i<n);
				}
			}
		});
		container.add(dSlider);
		final TextSlider eslider = new TextSlider.Integer("strips",  SwingConstants.HORIZONTAL,
				0,5,5);
		eslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				int n = eslider.getValue().intValue();
				for (int i = 0; i<5; ++i)	{
					oneStrip.getChildComponent(i).setVisible(i<n);
				}
			}
		});
		container.add(eslider);
		
		JCheckBox cliffB = new JCheckBox("Clifford parallels");
		cliffB.setSelected(cliffordParallels);
		cliffB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cliffordParallels = ((JCheckBox)arg0.getSource()).isSelected();
				aaSlider.setEnabled(!cliffordParallels);
				update();
			}
		});
		hbox.add(cliffB);
		
		JCheckBox colorB = new JCheckBox("Banded color");
		colorB.setSelected(bandedColors);
		colorB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				bandedColors = ((JCheckBox)arg0.getSource()).isSelected();
				for (int i = 0; i<5; ++i)	{
					SceneGraphComponent sgc = oneStrip.getChildComponent(i);
					sgc.getAppearance().setAttribute("polygonShader.diffuseColor",
							bandedColors ? Appearance.INHERITED : colors[i%colors.length]);				
				}

			}
		});
		hbox.add(colorB);

		return container;
	}
	protected double[] getCliffordTlateYZ(double tlate) {
		// TODO Auto-generated method stub
		double c = Math.cos(tlate), s = Math.sin(tlate);
		return new double[]{
				c,0,-s,0,
				0,c,0,s,
				s,0,c,0,
				0,-s,0,c};
	}
	
	public static void main(String[] args) {
		new RinusPolyhedronS3().display();
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
		
//		cont.addTool(new RotateShapeTool());
//		cont.addTool(new TranslateShapeTool());

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
