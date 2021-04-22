package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JMenuBar;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.groups.BorromeanUtility;

public class ScrewsAndBands extends LoadableScene {

	double stairRadius = .15;
	double stairThickness = .04;
	int stairSamples = 50,
		stairHalfTurns = 4;
	double ringWidth = .1, ringThickness = .04;
	int ringSamples = 20;
	double bandWidth = .2, bandThickness = .04;
	int bandSamples = 20;
	double bandTurnAngle = 0.0;
	double crossingCloseness = .1;
	Color[] colors = new Color[3];
	SceneGraphComponent dgworld;
	public boolean weirdSym = true;
	@Override
	public SceneGraphComponent makeWorld() {
		DiscreteGroup dg = new DiscreteGroup();
		dg.setDimension(3);
		dg.setMetric(Pn.EUCLIDEAN);
		DiscreteGroupElement gens[] = BorromeanUtility.borromeanGenerators(2);
		dg.setConstraint(new DiscreteGroupSimpleConstraint(-1.0, 2, 100));
		dg.setGenerators(gens);
		dg.update();
		DiscreteGroupSceneGraphRepresentation theRepn = new DiscreteGroupSceneGraphRepresentation(dg, false);
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		dgworld = getScrewsAndBeams(2);
		setNewColors();
		theRepn.setWorldNode(dgworld);
		theRepn.update();
		world.addChild(theRepn.getRepresentationRoot());
//		world.setGeometry(Primitives.cube(false));
		return world;
	}

	private SceneGraphComponent getScrewsAndBeams(int order) {
		SceneGraphComponent dgworld = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent singleDing = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent anotherDing = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent stairsSGC = SceneGraphUtility.createFullSceneGraphComponent("stairs");
		SceneGraphComponent ringSGC = SceneGraphUtility.createFullSceneGraphComponent("ring");
		SceneGraphComponent bandSGC = SceneGraphUtility.createFullSceneGraphComponent("band");
		singleDing.addChildren(stairsSGC);
		anotherDing.addChildren(ringSGC, bandSGC);

		QuadMeshFactory stairs = makeStairs();
		stairsSGC.setGeometry(stairs.getGeometry()); //Primitives.cylinder(10, .2, -1, 1, Math.PI));
		
		QuadMeshFactory ring = makeRing(ringSGC);
		ringSGC.setGeometry(ring.getGeometry()); 
//		Appearance ap = ringSGC.getAppearance();
//		SimpleTextureFactory tf = new SimpleTextureFactory();
//		tf.setType(SimpleTextureFactory.TextureType.WEAVE);
//		tf.update();
//		tex.setImage(tf.getImageData());
		bandSGC.setGeometry(makeBand()); //Primitives.box(bandWidth, bandThickness, 2, false)); //
		Appearance ap = bandSGC.getAppearance();
		Texture2D tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, 
				"polygonShader.texture2d", ap, true);
		try {
			ImageData id = ImageData.load(Input.getInput(
					"/net/MathVis/data/testData3D/textures/grid_small.jpeg")); // weaveRGBABright.png"));
			tex.setImage(id);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Matrix m = new Matrix();
		MatrixBuilder.euclidean().scale(10,4,1).assignTo(m);
		tex.setTextureMatrix(m);

//		ap = bandSGC.getAppearance();
//		tf = new SimpleTextureFactory();
//		tf.setType(SimpleTextureFactory.TextureType.WEAVE);
//		tf.update();
//		tex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, 
//				"polygonShader.texture2d", ap, true);
//
//		tex.setImage(tf.getImageData());
//		m = new Matrix();
//		MatrixBuilder.euclidean().scale(4,10,1).assignTo(m);
//		tex.setTextureMatrix(m);
		
		double s3grp[][] = BorromeanUtility.getS3Group();
		double s3grp2[][] = BorromeanUtility.getS3Group2();
		for (int i = 0; i<6; ++i) {
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent(""+i);
			sgc.addChild(singleDing);
			sgc.setTransformation(new Transformation(s3grp[i]));
			dgworld.addChild(sgc);
		}
		for (int i = 0; i<s3grp2.length; ++i) {
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent(""+i);
			sgc.addChild(anotherDing);
			sgc.setTransformation(new Transformation(s3grp2[i]));
			dgworld.addChild(sgc);
		}
		return dgworld;
	}
	double f = 0.0, F = 1.0-f;
	static Color[] defaultClrs = {new Color(255,255,0), new Color(255,20,20), new Color(0,255,20)};
	private void setNewColors()	{
		for (int i = 0; i<3; ++i) 
			colors[i] = new Color((float) (f+F*Math.random()), 
					(float) (f+F*Math.random()), 
					(float) (f+F*Math.random()));
		int n =dgworld.getChildComponentCount();
		for (int i = 0; i<n; ++i) {
			SceneGraphComponent sgc = dgworld.getChildComponent(i);
			sgc.getAppearance().setAttribute("polygonShader.diffuseColor", defaultClrs[(i/2)%3]); //colors[(i/2)%3]);
		}

	}
	private QuadMeshFactory makeStairs() {
		QuadMeshFactory stairs = new QuadMeshFactory();
		int count = stairSamples;
		double radius = stairRadius;
		double halfThick = stairThickness/2;
		double[][] verts = new double[4*count][3];
		for (int i=0; i<count; ++i)	{
			double f = (i)/(count-1.0);
			double height = 2*(f-.5);
			// f now is in range -1,1
			double angle = Math.PI*(f*stairHalfTurns+.5);
			double c = Math.cos(angle);
			double s = Math.sin(angle);
			verts[i][0] = radius*c+1;
			verts[i+count][0] = radius*c-s*halfThick+1;
			verts[i+2*count][0] = -radius*c-s*halfThick+1;
			verts[i+3*count][0] = -radius*c+1;
			verts[i][2] = radius*s;
			verts[i+count][2] = radius*s+c*halfThick;
			verts[i+2*count][2] = -radius*s+c*halfThick;
			verts[i+3*count][2] = -radius*s;
			verts[i][1] = verts[i+count][1] = verts[i+2*count][1] = verts[i+3*count][1] = height;
		}
		stairs.setClosedInUDirection(false);
		stairs.setClosedInVDirection(false);
		stairs.setGenerateFaceNormals(true);
		stairs.setGenerateVertexNormals(true);
		stairs.setULineCount(count);
		stairs.setVLineCount(4);
		stairs.setVertexCoordinates(verts);
		stairs.update();
		return stairs;
	}

	private QuadMeshFactory makeRing(SceneGraphComponent ringSGC) {
		QuadMeshFactory ring = new QuadMeshFactory();
		int count = ringSamples;
		double r = stairThickness+stairRadius, R=r+ringWidth;
		double hthick = ringThickness/2;
		double[][] verts = new double[5*count][3];
		for (int i=0; i<count; ++i)	{
			double f = (i)/(count-1.0);
			//f = 2*(f-.5);
			// f now is in range 0,1
			double angle = Math.PI*(f+.5);
			verts[i][0] = r*Math.cos(angle)+1;
			verts[i][2] = r*Math.sin(angle);
			verts[i][1] = 1-hthick;
			verts[i+count][0] = R*Math.cos(angle)+1;
			verts[i+count][2] = R*Math.sin(angle);
			verts[i+count][1] = 1-hthick;
			verts[i+2*count][0] = R*Math.cos(angle)+1;
			verts[i+2*count][2] = R*Math.sin(angle);
			verts[i+2*count][1] = 1+hthick;
			verts[i+3*count][0] = r*Math.cos(angle)+1;
			verts[i+3*count][2] = r*Math.sin(angle);
			verts[i+3*count][1] = 1+hthick;
			verts[i+4*count][0] = r*Math.cos(angle)+1;
			verts[i+4*count][2] = r*Math.sin(angle);
			verts[i+4*count][1] = 1-hthick;
		}
		ring.setClosedInUDirection(false);
		ring.setClosedInVDirection(false);
		ring.setGenerateFaceNormals(true);
		ring.setGenerateVertexNormals(true);
		ring.setGenerateTextureCoordinates(true);
		ring.setULineCount(count);
		ring.setVLineCount(5);
		ring.setVertexCoordinates(verts);
		ring.update();
		return ring;
	}

	private QuadMeshFactory makeBandOld() {
		QuadMeshFactory band = new QuadMeshFactory();
		int count = bandSamples;
		double radius = .95*(stairThickness+stairRadius)+ringWidth,
			hbandwidth = bandWidth/2;
		double[][] verts = new double[2*count][3];
		for (int i=0; i<count; ++i)	{
			double f = (i)/(count-1.0);
			double z = radius + (1-radius)*f;
			// y is now in range [radius, 1]
			double a = -Math.PI/4 * f; //+ Math.PI/2;
			verts[i][0] = 1-hbandwidth*Math.cos(a) + crossingCloseness*f;
			verts[i+count][0] = 1+hbandwidth*Math.cos(a) + crossingCloseness*f;
			verts[i][1] = 1-hbandwidth*Math.sin(a) - crossingCloseness*f;
			verts[i+count][1] = 1+hbandwidth*Math.sin(a) - crossingCloseness*f;
			verts[i][2] = verts[i+count][2] = z;
		}
		band.setClosedInUDirection(false);
		band.setClosedInVDirection(false);
		band.setGenerateFaceNormals(true);
		band.setGenerateVertexNormals(true);
		band.setULineCount(count);
		band.setVLineCount(2);
		band.setVertexCoordinates(verts);
		band.update();
		return band;
	}

	private IndexedFaceSet makeBand() {
		QuadMeshFactory band = new QuadMeshFactory();
		double radius = .95*(stairThickness+stairRadius)+ringWidth,
			hbandwidth = bandWidth/2,
			hbandthick = bandThickness/2;
		double[][][] verts = new double[4][5][3];
		double thicker =  - hbandthick;
		int count = 0;
		verts[0][count] = new double[]{1-hbandwidth, 1-thicker, radius};
		verts[1][count] = new double[]{1-hbandwidth, 1-thicker, radius + (1-radius)/3};
		verts[2][count] = new double[]{1-hbandwidth+crossingCloseness,1-crossingCloseness-thicker,1 - (1-radius)/3};
		verts[3][count] = new double[]{1-hbandwidth+crossingCloseness,1-crossingCloseness-thicker,1};
		count++;
		verts[0][count] = new double[]{1-hbandwidth, 1+thicker, radius};
		verts[1][count] = new double[]{1-hbandwidth, 1+thicker, radius + (1-radius)/3};
		verts[2][count] = new double[]{1-hbandwidth+crossingCloseness,1-crossingCloseness+thicker,1 - (1-radius)/3};
		verts[3][count] = new double[]{1-hbandwidth+crossingCloseness,1-crossingCloseness+thicker,1};
		count++;
		verts[0][count] = new double[]{1+hbandwidth, 1+thicker, radius};
		verts[1][count] = new double[]{1+hbandwidth, 1+thicker, radius + (1-radius)/3};
		verts[2][count] = new double[]{1+hbandwidth+crossingCloseness,1-crossingCloseness+thicker,1-(1-radius)/3};
		verts[3][count] = new double[]{1+hbandwidth+crossingCloseness,1-crossingCloseness+thicker,1};			
		count++;
		verts[0][count] = new double[]{1+hbandwidth, 1-thicker, radius};
		verts[1][count] = new double[]{1+hbandwidth, 1-thicker, radius + (1-radius)/3};
		verts[2][count] = new double[]{1+hbandwidth+crossingCloseness,1-crossingCloseness-thicker,1-(1-radius)/3};
		verts[3][count] = new double[]{1+hbandwidth+crossingCloseness,1-crossingCloseness-thicker,1};			
		count++;
		verts[0][count] = new double[]{1-hbandwidth, 1-thicker, radius};
		verts[1][count] = new double[]{1-hbandwidth, 1-thicker, radius + (1-radius)/3};
		verts[2][count] = new double[]{1-hbandwidth+crossingCloseness,1-crossingCloseness-thicker,1 - (1-radius)/3};
		verts[3][count] = new double[]{1-hbandwidth+crossingCloseness,1-crossingCloseness-thicker,1};
		BezierPatchMesh bpm = new BezierPatchMesh(1,3, verts);
		bpm.refineV(); bpm.refineV(); bpm.refineV();
		return BezierPatchMesh.representBezierPatchMeshAsQuadMesh(bpm);
	}
	
	@Override
	public boolean isEncompass() {
		return true;
	}

	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(20,20,20));
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter()	{
			
		    public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_H:
					System.out.println("	1:  new colors");
					break;

				case KeyEvent.VK_1:
					setNewColors();
				    viewer.renderAsync();
					break;
					}
			}
		});
}

}
