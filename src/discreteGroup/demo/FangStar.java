/*
 * Created on Feb 12, 2006
 *
 */
package discreteGroup.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.Format;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.ColorSelectionModel;

import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StringArray;
import de.jreality.scene.data.StringArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.util.WingedEdge;


public class FangStar extends LoadableScene {
	SceneGraphComponent theWorld, scaledFangSGC, quadkit;
	SceneGraphComponent oneFourth, fourFourths;
	DiscreteGroupSceneGraphRepresentation theMainRepn;
	double phi = .5 * (-1 + Math.sqrt(5));
	double[][] verts = {
			{0,0,0,1},
			{1,0, 0,1},
			{1, 1-phi, 0,1},
			{1,0,phi,1}
	};
	final double[][] outverts = {verts[1], verts[2], verts[3]};
	int[][] indices = {{0,1,2},{0,2,3},{0,3,1},{3,2,1}};
	private SceneGraphComponent honeycombCell;
	private SceneGraphComponent triacontahedronSGC;
	private SceneGraphComponent[] starPieces;
	private double[][] starPieceCenters; 
//	double[][] dcolors = new double[colors.length][];
//	{
//		for (int i = 0; i<colors.length; ++i)	
//			dcolors[i] = colorToDouble(null, colors[i]);
//	}

	public SceneGraphComponent makeWorld() {
		SceneGraphNode.setThreadSafe(false);
		theWorld =  SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		oneFourth = new SceneGraphComponent("One fourth");
		fourFourths = new SceneGraphComponent("fourFourths");
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .015);
		theWorld.getAppearance().setAttribute(CommonAttributes.BACKEND_RETAIN_GEOMETRY, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		theWorld.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(150,150,150));
		theWorld.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(150,150,250));
		double[][] mats = {Rn.identityMatrix(4), P3.makeReflectionMatrix(null, new double[]{0,1,0,0}, Pn.EUCLIDEAN),
				P3.makeReflectionMatrix(null, new double[]{0,0,1,0}, Pn.EUCLIDEAN),
				P3.makeRotationMatrix(null, new double[]{1,0,0}, Math.PI)};
		
		for (int i = 0; i<4; ++i)	{
				SceneGraphComponent sgc = new SceneGraphComponent(i+"fourth");
				sgc.addChild(oneFourth);
				fourFourths.addChild(sgc);
				sgc.setTransformation(new Transformation(mats[i]));
		}

		DiscreteGroupElement[] gens = new DiscreteGroupElement[3];
		
		for (int i = 0; i < 3; ++i)	{
			double[] plane = P3.planeFromPoints(null, verts[0], verts[i+1], verts[1+((i+2)%3)]);
			double[] mat = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
			gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
		}
		DiscreteGroupConstraint triv = new DiscreteGroupConstraint() {
			public boolean acceptElement(DiscreteGroupElement dge) {
				double[][] tverts =Rn.matrixTimesVector(null, dge.getArray(), outverts);
				Rectangle3D bnds = BoundingBoxUtility.calculateBoundingBox(tverts);
				return bnds.getMaxY() > 0.01 && bnds.getMaxZ() > 0.01 && bnds.getMinX() > 0.0;
			}

			public int getMaxNumberElements() {
				return 200;
			}

			public void setMaxNumberElements(int i) {
				
			}

			public void update() {
				// TODO Auto-generated method stub
				
			}
			
		};

		DiscreteGroup tg = makeFangGroup(gens, triv);

		DiscreteGroup tgFull = makeFangGroup(gens, new DiscreteGroupSimpleConstraint(200));

		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setFaceCount(4);
		ifsf.setVertexCount(4);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceIndices(indices);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
//		tg.setDefaultFundamentalDomain(ifsf.getIndexedFaceSet());
//		tgFull.setDefaultFundamentalDomain(ifsf.getIndexedFaceSet());
		
		scaledFangSGC = SceneGraphUtility.createFullSceneGraphComponent("ScaledFang");
		scaledFangSGC.setGeometry(ifsf.getIndexedFaceSet());
		scaledFangSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white); //new Color(200,175,0));
		double[] center = Rn.average(null, verts);
		MatrixBuilder.euclidean().translate(center).scale(.995).translate(-center[0], -center[1], -center[2]).assignTo(scaledFangSGC);
		honeycombCell = new SceneGraphComponent();
		honeycombCell.addChild(scaledFangSGC);
		theMainRepn = new  DiscreteGroupSceneGraphRepresentation(tg, true, "Fang");
		theMainRepn.setWorldNode(honeycombCell);
		theMainRepn.update();
		SceneGraphComponent oneTriacontahedron = theMainRepn.getRepresentationRoot();
		oneTriacontahedron.setAppearance(new Appearance());
		oneFourth.addChild(oneTriacontahedron);
		
		double[] xeq1Plane = {1,0,0,-1};
		SceneGraphComponent fangFamily = new SceneGraphComponent("fang family");
		Appearance ap = new Appearance();
		fangFamily.setAppearance(ap);
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		DefaultPolygonShader dps  = (DefaultPolygonShader) dgs.getPolygonShader();
		RenderingHintsShader rhs = ShaderUtility.createDefaultRenderingHintsShader(ap, true);
//		dps.setTransparency(.6);
//		rhs.setTransparencyEnabled(true);
		ap.setAttribute(CommonAttributes.TRANSPARENCY, .25);
		ap.setAttribute(CommonAttributes.TEXT_SCALE, .005);
		ap.setAttribute(CommonAttributes.TEXT_OFFSET, new double[]{0,0,.2});
		ap.setAttribute(CommonAttributes.TEXT_ALIGNMENT,0);
//		ap.setAttribute(CommonAttributes.TEXT_SHADER+"."+CommonAttributes.TEXT_FONT,new Font("Marker Felt", Font.PLAIN, 12)); ;
		int count=0;
		starPieces = new SceneGraphComponent[tg.getElementList().length];
		starPieceCenters = new double[starPieces.length][];
		// label with numbers and colors given by Einar's models
		String[] labels = {"1","2","5","3","4","6","8","7"};
		int[] redirect = {0,1,4,2,3,5,7,6};
		Color[] colors = {Color.yellow,
				Color.orange,
				Color.red,
				new Color(100,100,255),
				new Color(255,100,255),
				new Color(255,75,150),
				Color.cyan,
				new Color(0,255,200)
		};
		
		for (DiscreteGroupElement dge : tg.getElementList())	{
			starPieces[count] = SceneGraphUtility.createFullSceneGraphComponent("fang child"+count);
			ap = starPieces[count].getAppearance();
			ap.setAttribute("polygonShader.diffuseColor", colors[redirect[count]]);
			ap.setAttribute("polygonShader.textShader.diffuseColor", Color.white);
			double[][] planes = new double[4][];
			planes[3] = xeq1Plane;
			double[][] tverts = Rn.matrixTimesVector(null, dge.getArray(), verts);
			center = Rn.average(null, tverts);
			WingedEdge fangGeom = new WingedEdge();
			fangGeom.setColoredFaces(false);
			fangGeom.cutWithPlane(xeq1Plane, count);
			for (int i=0;i<3;++i)	{
				planes[i] = P3.planeFromPoints(null, tverts[0], tverts[i+1], tverts[(i+2)%3+1]);
				if (Rn.innerProduct(planes[i], center) > 0) Rn.times(planes[i], -1, planes[i]);
				fangGeom.cutWithPlane(planes[i], count);
			}
			String[] faceLabels = {labels[count], "", "",""};
			fangGeom.setFaceAttributes(Attribute.LABELS, new StringArray(faceLabels));
			tverts = fangGeom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			starPieceCenters[count] = Rn.average(null, tverts);
			starPieces[count].setGeometry(fangGeom);
			fangFamily.addChild(starPieces[count]);
			count++;
		}
		scaleFangs(.8);
		oneFourth.addChild(fangFamily);
		scaledFangSGC = SceneGraphUtility.createFullSceneGraphComponent("ScaledFang");
		scaledFangSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		scaledFangSGC.setGeometry(ifsf.getIndexedFaceSet());
		scaledFangSGC.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white); //new Color(200,175,0));
		MatrixBuilder.euclidean().translate(center).scale(.99).translate(-center[0], -center[1], -center[2]).assignTo(scaledFangSGC);
		honeycombCell = new SceneGraphComponent();
		honeycombCell.addChild(scaledFangSGC);
		theMainRepn = new  DiscreteGroupSceneGraphRepresentation(tgFull, true, "Fang");
		theMainRepn.setWorldNode(honeycombCell);
		theMainRepn.update();
		theWorld.addChild(theMainRepn.getRepresentationRoot());
		theWorld.addChild(fourFourths);
		return theWorld;
	}

	private double[] colorToDouble(double[] dst, Color c)	{
		float[] cc = new float[4];
		c.getRGBComponents(cc);
		for (int i = 0; i<4; ++i)  dst[i] = (double) cc[i];
		return dst;
	}
	private void scaleFangs(double d) {
		int n = starPieces.length;
		
		for (int i = 0; i<n; ++i)	{
			double[] m = P3.makeTranslationMatrix(null, starPieceCenters[i], Pn.EUCLIDEAN);
			MatrixBuilder.euclidean().scale(d).conjugateBy(m).assignTo(starPieces[i]);
		}
	}

	private DiscreteGroup makeFangGroup(DiscreteGroupElement[] gens, DiscreteGroupConstraint triv) {
		DiscreteGroup tg = new DiscreteGroup();
		tg.setGenerators(gens);
		tg.setMetric(Pn.EUCLIDEAN);
		tg.setConstraint(triv);
//		tg.setElementList(DiscreteGroup.generateElements(tg, triv));
		tg.setColorPicker(null);
		return tg;
	}

	public boolean isEncompass() {
		return true; 
	}

	@Override
	public void customize(JMenuBar menuBar, Viewer viewer) {
		JMenu testM = new JMenu("Actions");
//		JMenuItem jca = new JMenuItem("Cycle selection");
//		testM.add(jca);
//		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
//		jca.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent e)	{
//				theSelection.setSelectedChild( (theSelection.getSelectedChild()+1) % 2);
//			}
//		});
		JMenuItem jca = new JMenuItem("Toggle honeycomb visible");
		testM.add(jca);
		jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
		jca.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e)	{
				honeycombCell.setVisible(!honeycombCell.isVisible());
			}
		});
		 jca = new JMenuItem("Toggle triacontahedron visible");
			testM.add(jca);
			jca.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
			jca.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					triacontahedronSGC.setVisible(!triacontahedronSGC.isVisible());
				}
			});
		menuBar.add(testM);
		final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
		final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
		final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
		final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
		Color[] bg = new Color[4];
		bg[0] = URBackground;
		bg[1] = ULBackground;// bg[1];
		bg[2] = LLBackground;
		bg[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", bg);
	}
}
