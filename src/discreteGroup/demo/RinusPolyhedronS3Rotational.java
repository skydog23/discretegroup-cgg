package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import charlesgunn.jreality.newtools.FlyTool2;
import charlesgunn.jreality.plugin.TermesSpherePlugin;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.math.p5.PlueckerLineGeometry;
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
import de.jreality.scene.Camera;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

public class RinusPolyhedronS3Rotational extends Assignment {


	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
		world2 = SceneGraphUtility.createFullSceneGraphComponent("world2"),
		oneStrip = SceneGraphUtility.createFullSceneGraphComponent("strip"),
		triangles = SceneGraphUtility.createFullSceneGraphComponent("tris");
	protected double alpha = .6665, //Math.PI/5,
//		tlateLength = .2,
		implodeFactor = .25,
		pitch = 1.0;
	protected int family = 5,
			diagonal = 2,
			skew = 0,
			numStrips = 5,
			numBands = 5;
	IndexedFaceSetFactory triFact;

	int metric = Pn.ELLIPTIC;
	double[][] falloffs =   {{1.5,.25,0},{.5,.5,0},{.5, .5, 0}};
	double[][] cameraClips = {{.001,2},{.01, 1000},{.01,-.05}};
	double distance = .5;
	double[] unitD = {Math.tanh(distance), distance, Math.tan(distance)};
	private double sideLength;
	private double[][] verts;
	boolean bandedColors = false,
		showLines = false,
		onCliffordParallels = true;		

	Color[] colors = { Color.yellow, Color.blue, Color.red, Color.magenta, Color.green, Color.cyan, new Color(255,100,0)};
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

	private void setSkew()	{
		double error = 1.0;
		int count  = 0;
		alpha = 0.01;
		do {
			sideLength = calculateTriangleVertices(alpha);
			double ratio = calculateRatio();
			error = ratio - skew;
			if (Math.abs(error) < 10E-12) break;
			sideLength = calculateTriangleVertices(alpha+10E-5);
			double ratio2 = calculateRatio();
			double fdot = (ratio2 - ratio)/(10E-5);
			// restore actual values
			sideLength = calculateTriangleVertices(alpha);
			calculateRatio();
			alpha = alpha - .3*error/fdot;
			System.err.println("ratio = "+ratio+"\talpha ="+alpha+"\t error = "+error);
//			if (alpha < 0) alpha = -alpha;
			count++;
		} while(Math.abs(error) > 10E-12 && count < 200);
		aSlider.setValue(alpha);
		System.err.println("count = "+count);
	}

	protected double calculateRatio() {
		Matrix rotate = new Matrix(), translate = new Matrix();
		double[] brobear = Rn.matrixTimesVector(null, rotdnM, verts[0]);
		double skewmoo = Pn.distanceBetween(brobear, verts[2], Pn.ELLIPTIC);
		double[] target = getCliffordTlateYZ(skewmoo);
		MatrixBuilder.elliptic(new Matrix(target)).rotateZ(2*diagonal*Math.PI/family).assignTo(rotate);
		translate.assignFrom(getCliffordTlateYZ(sideLength));
		// check for "closedness"
		Matrix rotpow = Matrix.power(rotate, family);
		brobear = Rn.matrixTimesVector(null, rotpow.getArray(), verts[0]);
		skewmoo = Pn.distanceBetween(brobear, verts[0], Pn.ELLIPTIC);
		double ratio = skewmoo/sideLength;
		return ratio;
	}
	
	private double calculateTriangleVertices(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		double[] point0 = new double[]  {0, s, c, 0};
		double[] point1 = new double[] {s, 0, 0, c};
		double[] point2 = Rn.add(null, point0, point1);
		double[] normalPlane = new double[] {0, s,  c, 0};
		double[] t0 = Rn.matrixTimesVector(null, rotdnM, point1),
			t1 = Rn.matrixTimesVector(null, rotdnM, point2),
			tv = Rn.matrixTimesVector(null, rotdnM, point0);
		double[] plueckerLine = PlueckerLineGeometry.lineFromPoints(null, t0, t1);
		double[] midpoint = PlueckerLineGeometry.lineIntersectPlane(null, plueckerLine, normalPlane);
		double alt = Pn.distanceBetween(point1, midpoint, Pn.ELLIPTIC);
		System.err.println("Altitude = "+alt);
		double ss = sideFromAltitude(alt);
		verts = new double[4][];
		// triangle connectivity given by {0,1,2} and {0,2,3}
		verts[0] = point1;
		verts[2] = Pn.dragTowards(null, midpoint, tv, ss/2, Pn.ELLIPTIC);
		verts[1] = Pn.dragTowards(null, midpoint, Rn.times(null, -1, tv), ss/2, Pn.ELLIPTIC);
		verts[3] = Pn.dragTowards(null, point1, point0, -ss, Pn.ELLIPTIC);
		return ss;
	}
	double[] rotdnM, rothdnM, rot1nM;
	private void updateTriangle() {
		double zTranslate = 0, skewmoo = 0.0;
		rotdnM = MatrixBuilder.euclidean().rotateZ(2*diagonal*Math.PI/family).getArray();
		rothdnM = MatrixBuilder.euclidean().rotateZ(diagonal*Math.PI/family).getArray();
		rot1nM = MatrixBuilder.euclidean().rotateZ(2*Math.PI/family).getArray();
		Matrix rotate = new Matrix(), translate = new Matrix();
		if (onCliffordParallels)	{
			sideLength = calculateTriangleVertices(alpha);
			double[] brobear = Rn.matrixTimesVector(null, rotdnM, verts[0]);
			skewmoo = Pn.distanceBetween(brobear, verts[2], Pn.ELLIPTIC);
			double[] target = getCliffordTlateYZ(skewmoo);
			MatrixBuilder.elliptic(new Matrix(target)).rotateZ(2*diagonal*Math.PI/family).assignTo(rotate);
			translate.assignFrom(getCliffordTlateYZ(sideLength));
			// check for "closedness"
			zTranslate = sideLength;
		} else {		// rotational symmetry
			// solve first for the side length
			double c = Math.cos(alpha), s = Math.sin(alpha);
			sideLength = Math.acos(c*c + s*s*Math.cos(diagonal*2*Math.PI/family));
			double p =  Math.acos(c*c + s*s*Math.cos(diagonal*Math.PI/family)),
					h = Math.acos(c/Math.cos(p/2));
			double x = 2*Math.acos(Math.cos(sideLength/2)/Math.cos(p/2));
			zTranslate = 2*Math.asin(Math.sin(x/2)/Math.cos(h));
			System.err.println("r s p x d k = "+alpha+"\t"+sideLength+"\t"+p+"\t"+x+"\t"+zTranslate);
			double[] movedO = Pn.dragTowards(null, P3.originP3, new double[]{0,0,1,0}, zTranslate, Pn.ELLIPTIC);
			double[] tlateM = P3.makeTranslationMatrix(null, movedO, Pn.ELLIPTIC);
			pitch = (diagonal*Math.PI/family)/zTranslate;
			verts = new double[4][];
			verts[0] =  new double[]  {s,0,0,c};
			for (int i = 1; i<4; ++i)	{
				verts[i] = Rn.matrixTimesVector(null, rothdnM, verts[i-1]);
			}
			verts[0] = Rn.matrixTimesVector(null, tlateM, verts[0]);
			verts[2] = Rn.matrixTimesVector(null, tlateM, verts[2]);
			verts = new double[][]{verts[1], verts[2], verts[3], verts[0]};		
		}
		double n = 2*Math.PI/zTranslate;
		periodSlider.setValue(n);
		System.err.println("period = "+String.format("%16.10g", n));
//		System.err.println("verts = \n"+Rn.toString(verts));
		Pn.normalize(verts, verts, Pn.ELLIPTIC);
		double[] ds = new double[5];
		for (int i = 0; i<5; ++i)	{
			ds[i] = Pn.distanceBetween(verts[pairs[i][0]], verts[pairs[i][1]], Pn.ELLIPTIC);
		}
		System.err.println("Distances = "+Rn.toString(ds));
		implode(verts);
		
		Matrix acc = new Matrix();
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
			acc.assignTo(sgc);
			acc.multiplyOnLeft(rotate);
		}
		int maxStrips = (int) (n + .5);
		if (numBands > maxStrips) maxStrips = numBands;
		acc = new Matrix();
		for (int i = 0; i<maxStrips; ++i)	{
			SceneGraphComponent sgc = null;
			if (triangles.getChildComponentCount() <= i) {
				sgc = new SceneGraphComponent();
				sgc.addChild(oneStrip);
				sgc.setAppearance(new Appearance());
				sgc.getAppearance().setAttribute("polygonShader.diffuseColor", colors[i%colors.length]);				
				triangles.addChild(sgc);
			}
			else sgc = triangles.getChildComponent(i);
			acc.assignTo(sgc);
			acc.multiplyOnLeft(translate);
		}
		updateVisibility();
	}

	
	public void updateVisibility()		{
		for (int i = 0; i<oneStrip.getChildComponentCount(); ++i)	{
			oneStrip.getChildComponent(i).setVisible(i<numStrips);
		}
		for (int i = 0; i<triangles.getChildComponentCount(); ++i)	{
			triangles.getChildComponent(i).setVisible(i<numBands);
		}
		
	}
	int[][] indices = {{0,1,4,3},{1,2,5,4},{2,0,3,5},{6,7,10,9}, {7,8,11,10},{8,6,9,11}};
	int[][] edgeIndices = {{0,1},{1,2},{2,0},{7,8},{8,6}};
	private TextSlider periodSlider, aSlider;
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

	private double sideFromAltitude(double alt) {
		// according to my figuring this is a quadratic equation
		// 2x^2 - cos(alt)x - 1 = 0  where x = cos(s/2)
		double c = Math.cos(alt),
				det = Math.sqrt(c*c + 8),
				x = (c+det)/4,
				result = 2*Math.acos(x);
//		int count = 50;
//		double result = alt;
//		// use cos alt = cos s / (cos (s/2))
//		for (int i = 0; i<count; ++i)	{
//			double alt2 = Math.acos(Math.cos(result)/Math.cos(result/2));
//			if (Math.abs(alt2 - alt) < 10E-16) return result;
//			double diff = alt-alt2;
//			result += diff/2;
//		}
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
		
		aSlider = new TextSlider.Double("radius",  SwingConstants.HORIZONTAL, 0.0, Math.PI/6, alpha);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				alpha = aSlider.getValue().doubleValue();
				updateTriangle();
				//viewer.renderAsync();
			}
		});
		container.add(aSlider);
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
				0, 40, 0.0);
		container.add(periodSlider);
		
		final TextSlider skewSlider = new TextSlider.Integer("skew",  SwingConstants.HORIZONTAL,
				0, 5, skew);
		skewSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				skew = skewSlider.getValue().intValue();
				setSkew();
				updateTriangle();
			}
		});
		container.add(skewSlider);
		
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
				numBands = dSlider.getValue().intValue();
				updateVisibility();
			}
		});
		container.add(dSlider);
		final TextSlider eslider = new TextSlider.Integer("strips",  SwingConstants.HORIZONTAL,
				0,10,family);
		eslider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				numStrips = eslider.getValue().intValue();
				updateVisibility();
			}
		});
		container.add(eslider);
		return container;
	}
	protected double[] getCliffordTlateYZ(double tlate) {
		// TODO Auto-generated method stub
		double c = Math.cos(tlate), s = Math.sin(tlate);
		return new double[]{
				c,-s,0,0,
				s,c,0,0,
				0,0,c,s,
				0,0,-s,c};
	}
	
	public static void main(String[] args) {
		new RinusPolyhedronS3Rotational().display();
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
//		Scene scene = jrviewer.getPlugin(Scene.class);
//		scene.getAvatarComponent().setTransformation(null);
	}

	public void setupMetric()	{
		FlyTool2 flytool = new FlyTool2();
		flytool.setGain(.1);
		CameraUtility.getCameraNode(viewer).addTool(flytool);
		Camera cam = CameraUtility.getCamera(jrviewer.getViewer());
		cam.setFar(-.1);
		SceneGraphUtility.removeLights(viewer);
		SceneGraphComponent lightNode = makeLights();
		world.addChild(lightNode);
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		// this is all we have to do to tell the backend to use the non-euclidean vertex shader
		viewer.getSceneRoot().getAppearance().setAttribute("useGLSL",true);
//		viewer.getSceneRoot().getAppearance().setAttribute("oneGLSL",true);
//		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		Scene scene = jrviewer.getPlugin(Scene.class);
//		MatrixBuilder.euclidean().scale(2.0).assignTo(scene.getAvatarComponent());
//		final SceneGraphComponent cont = scene.getContentComponent();
		
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
