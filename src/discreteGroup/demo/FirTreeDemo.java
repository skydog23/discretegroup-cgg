package discreteGroup.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.jreality.geometry.GeometryUtilityOverflow;
import charlesgunn.jreality.geometry.IteratedTransform;
import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.jreality.viewer.LoadableScene;
import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.experimental.ViewerKeyListener;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;
import de.jreality.util.ImageUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

public class FirTreeDemo extends LoadableScene {

	private SceneGraphComponent filler;
	private SceneGraphComponent oneBranch;
	private SceneGraphComponent world;
	private SceneGraphComponent treeSGC, defaultOrnament;
	int tubeSides = 7;
	boolean animatingAll = false, 
		animatingOrns = false,
		showCredits = false,
		resetSnow = false,
		playSnow = false,
		saveFrames = false;
	double globalSpeed = 1.0;
	int framesPerSecond = 30;
	Dimension dim = new Dimension(640, 480);
	final static String textureRoot = "http://www.math.tu-berlin.de/~gunn/"; //"/Users/gunn/"; //
	Timer timer;
	Viewer viewer;
	@Override
	public SceneGraphComponent makeWorld() {
		defaultOrnament = new SceneGraphComponent();
		defaultOrnament.setGeometry(SphereUtility.tessellatedIcosahedronSphere(0));
		defaultOrnament.getGeometry().setName("ornament");
		MatrixBuilder.euclidean().scale(.5).assignTo(defaultOrnament);
		IteratedTransform it = getSmallBranch();
		it.setName("small");
		filler = new SceneGraphComponent();
		SceneGraphComponent left = new SceneGraphComponent();
		SceneGraphComponent right = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(.2,.06,0).rotateY(Math.PI/3).scale(.5).assignTo(left);
		MatrixBuilder.euclidean().translate(.6,.10,0).rotateY(-Math.PI/3).scale(.5).assignTo(right);
		left.addChild(it);
		right.addChild(it);
		filler.addChildren(left,right);
		SceneGraphComponent it2 = getLargeBranch();
		it2.setName("large");
		world = new SceneGraphComponent("world");
		oneBranch = new SceneGraphComponent("oneBranch");
		oneBranch.addChild(it2);
		world.addChild(getTree());
		SceneGraphNode.setThreadSafe(false);
		Appearance ap = new Appearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white); //new Color(117, 76, 35));
		ap.setAttribute("polygonShader.vertexShadername", "simple"); //new Color(117, 76, 35));
		ap.setAttribute("vertexShadername", "simple"); //new Color(117, 76, 35));
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		world.setAppearance(ap);
		SceneGraphComponent flatLeaves = SceneGraphUtility.createFullSceneGraphComponent("leaves");
		SceneGraphComponent flatStems = SceneGraphUtility.createFullSceneGraphComponent("stems");
		ap = flatStems.getAppearance();
		Texture2D tex2d2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(
		Texture2D.class, "polygonShader.texture2d", ap, true);
		try {
			ImageData id = ImageData.load(Input.getInput(
					textureRoot+"Pictures/textures/firBark.jpg")); // weaveRGBABright.png"));
			tex2d2.setImage(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tex2d2.setApplyMode(Texture2D.GL_MODULATE); //COMBINE);
		Matrix tm = new Matrix();
		MatrixBuilder.euclidean().scale(1,5,1).assignTo(tm);
		tex2d2.setTextureMatrix(tm);
		tex2d2.setCombineModeColor(Texture2D.GL_INTERPOLATE); // MODULATE); //
		tex2d2.setOperand2Color(Texture2D.GL_SRC_ALPHA);
//		tex2d2.setBlendColor(new Color(.5f, .5f, 0f, .5f));			
		ap.setAttribute("polygonShader.diffuseColor", new Color(234, 150, 70)); //Color.white);
		
		ap = flatLeaves.getAppearance();
		tex2d2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(
				Texture2D.class, "polygonShader.texture2d", ap, true);
		try {
			ImageData id = ImageData.load(Input.getInput(
					textureRoot+"Pictures/textures/firNeedles.jpg")); // weaveRGBABright.png"));
			tex2d2.setImage(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tex2d2.setImage(generateAlphaChannelFromWhite(tex2d2.getImage()) );
		tm = new Matrix();
		MatrixBuilder.euclidean().scale(-1,1,1).assignTo(tm);
		tex2d2.setTextureMatrix(tm);
		tex2d2.setApplyMode(Texture2D.GL_MODULATE); //COMBINE); //
//		tex2d2.setCombineModeColor(Texture2D.GL_MODULATE); //INTERPOLATE); // 
//		tex2d2.setSource2Color(Texture2D.GL_TEXTURE);
//		tex2d2.setOperand2Color(Texture2D.GL_ONE_MINUS_SRC_ALPHA);
//		tex2d2.setCombineModeAlpha(Texture2D.GL_REPLACE); // MODULATE); //
//		tex2d2.setSource0Alpha(Texture2D.GL_TEXTURE); // MODULATE); //
		tex2d2.setBlendColor(new Color(1f, 1f, 1f, 1f));	
		ap.setAttribute("polygonShader.diffuseColor", new Color(125,255,125 )); //Color.white);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true); //Color.white);
		
		OrnamentSplitter os = new OrnamentSplitter();
		os.setRoot(world);
		List<SceneGraphComponent> orns = os.visit();

		SceneGraphComponent flatworld = SceneGraphUtility.flatten(world);
		
		int n = flatworld.getChildComponentCount();
		for (int i = 0; i<n; ++i)	{
			SceneGraphComponent sgc = flatworld.getChildComponent(i);
			if (sgc.getGeometry() == null) continue;
			if (sgc.getGeometry().getName().indexOf("leaf") != -1)
				flatLeaves.addChild(sgc);
			else if (sgc.getGeometry().getName().indexOf("stem") != -1)
				flatStems.addChild(sgc);
		}
		GeometryMergeFactory gmf = new GeometryMergeFactory();
		IndexedFaceSet flatL = gmf.mergeIndexedFaceSets(flatLeaves);
		flatL.setVertexAttributes(Attribute.COLORS, null);
		flatL.setFaceAttributes(Attribute.COLORS, null);
		IndexedFaceSet flatS = gmf.mergeIndexedFaceSets(flatStems);
		flatS.setVertexAttributes(Attribute.COLORS, null);
		flatS.setFaceAttributes(Attribute.COLORS, null);
//		IndexedFaceSet flatO = gmf.mergeIndexedFaceSets(flatOrnaments);
//		System.err.println("Got ornaments: "+flatO.getNumFaces());
//		flatO.setVertexAttributes(Attribute.COLORS, null);
//		flatO.setFaceAttributes(Attribute.COLORS, null);
		SceneGraphUtility.removeChildren(flatLeaves);
		SceneGraphUtility.removeChildren(flatStems);
		flatLeaves.setGeometry(flatL);
		flatStems.setGeometry(flatS);

		// handle the ornaments
		final SceneGraphComponent flatOrnaments = SceneGraphUtility.createFullSceneGraphComponent("ornaments");
		ap = flatOrnaments.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		try {
			reflectionMap = TextureUtility.createReflectionMap(
			          ap,
			          "polygonShader",
			          textureRoot+"Pictures/textures/christmasTree/cm_", //textures/jms/jms_", //desertstorm/desertstorm_",
			          new String[]{"rt","lf","up", "dn","bk","ft"},
			          ".png"); //JPG");
			reflectionMap.setBlendColor(new Color(1f,1f,1f, 0.2f));
			CubeMap rm2 = TextureUtility.createReflectionMap(ap, "lineShader.polygonShader", 
					TextureUtility.getCubeMapImages(reflectionMap));
			rm2.setBlendColor(new Color(1f,1f,1f, 0.2f));
			CubeMap rm3 = TextureUtility.createReflectionMap(ap, "pointsShader.polygonShader", 
					TextureUtility.getCubeMapImages(reflectionMap));
			rm3.setBlendColor(new Color(1f,1f,1f, 0.2f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// get ornament 
		SceneGraphComponent matheonLogo = new SceneGraphComponent();
		MatrixBuilder.euclidean().scale(1.25).rotateFromTo(new double[]{1.6180, 1, 0}, new double[]{0,1,0}).assignTo(matheonLogo);
		IndexedFaceSet slab = Primitives.box(1.6180, 1, .1, false, Pn.EUCLIDEAN);
		matheonLogo.setAppearance(new Appearance());
		matheonLogo.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		for (int i = 0; i<3; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setGeometry(slab);
			MatrixBuilder.euclidean().rotate(i*2*Math.PI/3, 1, 1, 1).assignTo(sgc);
			sgc.setAppearance(new Appearance());
			sgc.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(i*127, i*127, 255));
			matheonLogo.addChild(sgc);
		}
		SceneGraphComponent matheonLogo2 = new SceneGraphComponent();
		matheonLogo2.addChild(matheonLogo);
		
		XmasArchimedeanSolids as = new XmasArchimedeanSolids();
		SceneGraphComponent archies = as.getShapes();
		archies.addChild(matheonLogo2);
		int m = archies.getChildComponentCount();
		double scale = 1.00;
		int count = orns.size();
		int done = 0;
		for (int i = count-1; i>=0; --i)	{
			if ( (i * Math.random()) > .7*(count)) continue;
			SceneGraphComponent one = orns.get(i);
			FactoredMatrix fm = new FactoredMatrix(one.getTransformation().getMatrix());
			fm.update();
			SceneGraphComponent newone = new SceneGraphComponent();
			MatrixBuilder.euclidean().translate(fm.getTranslation()).scale(.25*scale).rotateX(0).assignTo(newone);
			int index = (done >= m) ? (m-6)-(done%m) : done;
			SceneGraphComponent archio = archies.getChildComponent(index);
			MatrixBuilder.euclidean().assignTo(archio);
			newone.addChild(archio);
			flatOrnaments.addChild(newone);
			scale *= 1.005;
			done++;
		}
		SceneGraphComponent treeTipCopy = new SceneGraphComponent();
		treeTipCopy.setTransformation(new Transformation());
		treeTipCopy.getTransformation().setMatrix(treeTip.getTransformation().getMatrix());
		SceneGraphComponent placer = new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(0, 2.0, 0).scale(1.0).assignTo(placer);
		placer.addChild(archies.getChildComponent(19));
		treeTipCopy.addChild(placer);
		flatOrnaments.addChild(treeTipCopy);

		snowSGC = SceneGraphUtility.createFullSceneGraphComponent("snow");
		snowSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		snowSGC.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, 0.0);
		snowSGC.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED,	false);
		snowSGC.getAppearance().setAttribute("polygonShader.diffuseColor",new Color(220,220,255));
		Texture2D snowTex = (Texture2D) AttributeEntityUtility.createAttributeEntity(
				Texture2D.class, "polygonShader.texture2d", snowSGC.getAppearance(), true);
		try {
			ImageData id = ImageData.load(Input.getInput(
					textureRoot+"Pictures/textures/snowCrystals.jpg")); // weaveRGBABright.png"));
			snowTex.setImage(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		snowTex.setImage(generateAlphaChannelFromBlack(snowTex.getImage()));		
//		snowTex.setApplyMode(Texture2D.GL_REPLACE); //COMBINE);
		snowTex.setTextureMatrix(tm);
		snowTex.setApplyMode(Texture2D.GL_COMBINE); //MODULATE); //
		snowTex.setCombineModeColor(Texture2D.GL_MODULATE); //INTERPOLATE); // 
		snowTex.setSource2Color(Texture2D.GL_TEXTURE);
		snowTex.setOperand2Color(Texture2D.GL_ONE_MINUS_SRC_ALPHA);
		snowTex.setCombineModeAlpha(Texture2D.GL_REPLACE); // MODULATE); //
		snowTex.setSource0Alpha(Texture2D.GL_TEXTURE); // MODULATE); //
		
		int numFlakes = 100;
		double du = .33333, dv = .25;
		double snowScale = 10;
		snowStorms = new Matrix[numFlakes];
		final double[] snowRots = new double[numFlakes], 
			snowTlate = new double[numFlakes];
		final double[][] snowAxes = new double[numFlakes][];
		yrestart = new Matrix();
		MatrixBuilder.euclidean().translate(0,10.0,0).assignTo(yrestart);
		Transformation scaler = new Transformation();
		MatrixBuilder.euclidean().scale(.5).assignTo(scaler);
		for (int i = 0; i<numFlakes; ++i)	{
			IndexedFaceSet oneFlake = GeometryUtilityOverflow.plainQuadMesh(1, 1, 1, 1);
			int u = i % 3;
			int v = (i/3) % 4;
			double[][] tc = {{u*du, v*dv},{(u+1)*du, v*dv},{u*du, (v+1)*dv},{(u+1)*du, (v+1)*dv}};
			oneFlake.setVertexAttributes(Attribute.TEXTURE_COORDINATES, 
					StorageModel.DOUBLE_ARRAY.array(2).createReadOnly(tc));
			SceneGraphComponent sgc = new SceneGraphComponent();
			SceneGraphComponent sgc2 = new SceneGraphComponent();
			sgc2.setGeometry(oneFlake);
			sgc2.setTransformation(scaler);
			sgc.addChild(sgc2);
			snowSGC.addChild(sgc);
			snowStorms[i] = new Matrix();
			double[] tlate = null;
			double xz;
			// don't let snow fall on the tree directly
			do {
				tlate = new double[]{(Math.random()-.5),(Math.random()-.5),(Math.random()-.5)};
				xz = Math.sqrt(tlate[0]*tlate[0]+tlate[2]*tlate[2]);
			} while (xz < 1.6/snowScale);
			MatrixBuilder.euclidean().translate(0,3,0).translate(Rn.times(null, snowScale, tlate)).scale(1.2).assignTo(snowStorms[i]);
			snowStorms[i].multiplyOnLeft(yrestart);
			snowStorms[i].assignTo(sgc);
			snowTlate[i]  = 1+Math.random();
			snowRots[i] = 1+Math.random();
			snowAxes[i] = new double[] {Math.random()-.5, Math.random()-.5, Math.random()-.5};
		}
		
		baseSGC = Primitives.closedCylinder(50, 1.5, -.5, -.3, Math.PI*2);
		labelAp = new Appearance();
		labelAp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0,0,80));
		noLabelAp = new Appearance();
		noLabelAp.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0,0,80));
		MatrixBuilder.euclidean().rotateY(-Math.PI/2).rotateX(-Math.PI/2).assignTo(baseSGC);
		BufferedImage bi = LabelUtility.createImageFromString(
				" Matheon Festbaum  ** Done by Gunn / "+
				"Geometry and Visualization Group / TU Berlin ** Software: www.jreality.de *** ",
				new Font("Sans Serif",Font.BOLD,64),Color.white);
		ImageData id = new ImageData(bi);
		tex2d2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(
				Texture2D.class, "polygonShader.texture2d", labelAp, true);
		tex2d2.setImage(id);
//		tex2d2 = TextureUtility.createTexture(ap, "polygonShader", id);
		tex2d2.setApplyMode(Texture2D.GL_DECAL);
		tm = new Matrix();
		MatrixBuilder.euclidean().reflect(new double[]{0,1,0,-.5}).assignTo(tm);
		tex2d2.setTextureMatrix(tm);
		baseSGC.setAppearance(showCredits ? labelAp : noLabelAp);
		world.removeChild(world.getChildComponent(0));
		world.addChildren( flatStems, flatOrnaments, baseSGC, flatLeaves);
		
		final double[] rotationSpeeds = new double[flatOrnaments.getChildComponentCount()+1];
		int vv = flatOrnaments.getChildComponentCount();
		for (int i=0; i<vv; ++i) {
			 double f = .07*(Math.random()-.5);
			 f += f > 0 ? .02 : -.02;
			rotationSpeeds[i] = f;
		}
		rotationSpeeds[vv] = -.015;
		
		timer = new Timer(20, new ActionListener() {
			long lasttime = 0;
			int frameCount = 0;
			public void actionPerformed(ActionEvent e) {
				int n = flatOrnaments.getChildComponentCount();
				final double[] mat = new double[16];
				long newTime = System.currentTimeMillis();
				long diff =  newTime - lasttime;
				double timeStretch = lasttime == 0 ? 1.0 : diff/100.0;
				if (saveFrames)	{
					timeStretch = 20.0/framesPerSecond;
				}
				lasttime = newTime;
				if (animatingOrns)
					for (int i = 0; i<n; ++i)	{
						SceneGraphComponent child = flatOrnaments.getChildComponent(i);
						P3.makeRotationMatrixY(mat,rotationSpeeds[i]*timeStretch*globalSpeed);
						child.getTransformation().multiplyOnRight(mat);
					}
				if (animatingAll) {
					P3.makeRotationMatrixY(mat,rotationSpeeds[n]*timeStretch*globalSpeed);
					world.getTransformation().multiplyOnRight(mat);
					baseSGC.getTransformation().multiplyOnLeft(mat);
				}
				if (playSnow)	{
					// animate snowflakes
					int m = snowSGC.getChildComponentCount();
					double factor = timeStretch*globalSpeed;
					for (int i = 0; i<m; ++i)	{
						double[] ylate = {0, -.02*factor*snowTlate[i], 0};
						snowStorms[i].multiplyOnLeft(P3.makeTranslationMatrix(mat, ylate, Pn.EUCLIDEAN));
						snowStorms[i].multiplyOnRight(P3.makeRotationMatrix(mat, snowAxes[i], snowRots[i]*.04*factor));
						double yt = snowStorms[i].getEntry(1, 3);
						if (yt < -2) snowStorms[i].multiplyOnLeft(yrestart);
						SceneGraphComponent sgc = snowSGC.getChildComponent(i);
						snowStorms[i].assignTo(sgc);
					}					
				}
				if (saveFrames)	{
					if (viewer != null) viewer.render();
					String num = String.format("%04d",frameCount);
					File outfile = new File("/home/gunn/Pictures/XmasTree/tree-"+num+".png");
					BufferedImage img = ((de.jreality.jogl.JOGLViewer) viewer).renderOffscreen(2*dim.width, 2*dim.height);
					BufferedImage img2 = new BufferedImage(dim.width, dim.height,BufferedImage.TYPE_INT_RGB);
					Graphics2D g = (Graphics2D) img2.getGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
					img2.getGraphics().drawImage(
							img.getScaledInstance(
									dim.width,
									dim.height,
									BufferedImage.SCALE_SMOOTH
							),
							0,
							0,
							null
					);
					ImageUtility.writeBufferedImage(outfile, img2);
					frameCount++;
					System.err.println(GlobalProperties.getMemoryUsage());
					if (frameCount >= 750) timer.stop();
				}
			}
			
		});
		timer.start();
		SceneGraphComponent holder = new SceneGraphComponent();
		holder.addChildren(world, snowSGC);
		world.setTransformation(new Transformation());
		SceneGraphComponent ground = new SceneGraphComponent();
		return holder; //oneBranch; //
	}

	@Override
	public boolean isEncompass() {
		return false;
	}

	SceneGraphComponent getLeaf()	{
		SceneGraphComponent leafSGC = SceneGraphUtility.createFullSceneGraphComponent("leaf");
		leafSGC.setGeometry(GeometryUtilityOverflow.plainQuadMesh(-1, -1, 1, 1));
		Matrix theLeafItself = new Matrix();
		MatrixBuilder.euclidean().scale(2.0).scale(1.2,.5,1).translate(-.5,0,0).assignTo(theLeafItself);
		MatrixBuilder.euclidean().rotateY(Math.PI/2).rotateX(Math.PI/2).times(theLeafItself).assignTo(leafSGC);
		SceneGraphComponent rleafSGC = SceneGraphUtility.createFullSceneGraphComponent("leaf");
		leafSGC.setGeometry(GeometryUtilityOverflow.plainQuadMesh(-1, -1, 1, 1));
		leafSGC.getGeometry().setName("leaf");
		rleafSGC.setGeometry(leafSGC.getGeometry());
		MatrixBuilder.euclidean().rotateY(Math.PI/2).times(theLeafItself).assignTo(rleafSGC);
		SceneGraphComponent both = new SceneGraphComponent("both");
		both.addChildren(leafSGC, rleafSGC);
		return both;
	}
	private static int lim1 = 80, lim2 = 150;
	private SceneGraphComponent treeTip;
	private CubeMap reflectionMap;
	private Appearance labelAp;
	private Appearance noLabelAp;
	private SceneGraphComponent baseSGC;
	private SceneGraphComponent snowSGC;
	private Matrix yrestart;
	private Matrix[] snowStorms;
	private ImageData generateAlphaChannelFromWhite(ImageData image) {
		byte[] bytes = image.getByteArray();
		for (int i = 0; i<bytes.length/4; i++)	{
			int red = ((int)bytes[4*i]) & 0xff;
			int grb = ((int)bytes[4*i+1]) & 0xff;
			double green = (double) grb;
			double normalizedGreen = AnimationUtility.linearInterpolation((double) red, lim1, lim2,  0.0, 255.0);
			double factor = normalizedGreen/green;
			for (int k =0; k<3; ++k)	{
				double tmp = factor * (((int)bytes[4*i+k]) & 0xff);
				if (tmp > 255.0) tmp = 255.0;
				bytes[4*i+k] = (byte)   tmp;
			}
			
			normalizedGreen = 255-normalizedGreen;
//			System.err.println("gr = "+green+"\talpha = "+normalizedGreen);
//			if (gr > lim1) alpha = 0;
//			else alpha = 255*(1-((gr > lim2) ? lim1 - gr : 0)/(lim2-lim1*1.0));
			bytes[4*i+3] = (byte) normalizedGreen; //(byte) 255; //
		}
		return new ImageData(bytes, image.getWidth(), image.getHeight());
	}

	private ImageData generateAlphaChannelFromBlack(ImageData image) {
		byte[] bytes = image.getByteArray();
		int count = 0;
		for (int i = 0; i<bytes.length/4; i++)	{
			double r = (double) bytes[4*i];
			if (r < 0) r += 255;
			double alpha;
			if (r < 120) alpha = 0;
			else if (r > 200) alpha = 255;
			else alpha = (r-120)*(255.0/80);
			bytes[4*i+3] = (byte) alpha;
		}
		return new ImageData(bytes, image.getWidth(), image.getHeight());
	}

	private IteratedTransform getSmallBranch() {
		int length = 3;
		double scale = .8;
		double[][] profile = {{0,1,0},{1.3,scale,0}};
		IndexedFaceSet foo = GeometryUtilityOverflow.surfaceOfRevolutionAsIFS(profile, tubeSides, Math.PI*2);
		foo.setName("stem");
		SceneGraphComponent wrapper = SceneGraphUtility.createFullSceneGraphComponent();
		MatrixBuilder.euclidean().scale(1,.1,.1).assignTo(wrapper);
		wrapper.setGeometry(foo); 
		SceneGraphComponent thing = SceneGraphUtility.createFullSceneGraphComponent();
		MatrixBuilder.euclidean().rotate(Math.PI/36,0,0,1).assignTo(thing);
		SceneGraphComponent leaf = new SceneGraphComponent("leftleaf");
		leaf.addChild(getLeaf());
		MatrixBuilder.euclidean().rotateX(Math.PI).translate(1,0,0).assignTo(leaf);
		SceneGraphComponent rightLeaf = new SceneGraphComponent("rightleaf");
		rightLeaf.addChild(getLeaf());
		MatrixBuilder.euclidean().translate(.5,0,0).assignTo(rightLeaf);
		thing.addChildren(wrapper, leaf, rightLeaf);
		Transformation iteratedTform = new Transformation();
		MatrixBuilder.euclidean().scale(scale).rotate(Math.PI/36, 0, 0, 1).translate(1.2,0,0).assignTo(iteratedTform);
		IteratedTransform it = new IteratedTransform(iteratedTform, length, thing);
		it.setName("iteratedTransform");
		it.setColors(null);
		it.update();
		return it;
	}

	private SceneGraphComponent getLargeBranch() {
		int length = 2;
		double scale = .8;
		double[][] profile = {{0,1,0},{1,scale,0}};
		IndexedFaceSet foo = GeometryUtilityOverflow.surfaceOfRevolutionAsIFS(profile, tubeSides, Math.PI*2);
		foo.setName("stem");
		SceneGraphComponent wrapper = SceneGraphUtility.createFullSceneGraphComponent();
		MatrixBuilder.euclidean().rotateY(0).rotate(Math.PI/18,0,0,1).scale(1,.1,.1).assignTo(wrapper.getTransformation());
		wrapper.setGeometry(foo); 
		SceneGraphComponent holder = new SceneGraphComponent();
		holder.addChildren(wrapper, filler);
		Transformation iteratedTform = new Transformation();
		MatrixBuilder.euclidean().scale(scale).rotate(Math.PI/18, 0,0, 1).translate(.95/scale,0,0).assignTo(iteratedTform);
		IteratedTransform it = new IteratedTransform(iteratedTform, length, holder);
		it.setColors(null);
		it.update();
		it.setName("iteratedTransform");
		SceneGraphPath pathToEnd = it.getPathToEnd();
		System.err.println("Path to end is "+Rn.matrixToString(pathToEnd.getMatrix(null)));
		double[] matrix = pathToEnd.getMatrix(null);
		Matrix m = new Matrix();
		MatrixBuilder.euclidean().translate(1.0,-.55,0).rotate(-Math.PI/9, 0,0, 1).assignTo(m);
		m.multiplyOnLeft(matrix);
		SceneGraphComponent ornamentSGC= new SceneGraphComponent("ornament");
		ornamentSGC.setTransformation(new Transformation(m.getArray()));
		ornamentSGC.addChild(defaultOrnament);
		SceneGraphComponent holder2 = new SceneGraphComponent();
		holder2.addChildren(it, ornamentSGC);
		return holder2;
	}
	private SceneGraphComponent getTree() {
		int length = 30;
		double scale = .96;
		double baseRadius = .1;
		double[][] profile = {{0,baseRadius,0},{.4,baseRadius*scale,0}};
		IndexedFaceSet foo = GeometryUtilityOverflow.surfaceOfRevolutionAsIFS(profile, tubeSides, Math.PI*2);
		foo.setName("stem");
		SceneGraphComponent trunk = SceneGraphUtility.createFullSceneGraphComponent();
		MatrixBuilder.euclidean().rotateZ(Math.PI/2).scale(1).translate(-.3,0,0).assignTo(trunk);
		trunk.setGeometry(foo); 
		Matrix oneStep = new Matrix();
		Matrix collect = new Matrix();
		MatrixBuilder.euclidean().rotateY((223.0/180.0)*Math.PI).scale(scale).translate(0,.3,0).assignTo(oneStep);
		treeSGC = SceneGraphUtility.createFullSceneGraphComponent("tree");
		for (int i = 0; i<length; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent("child"+i);
			collect.assignTo(sgc);
			sgc.addChild(oneBranch);
			sgc.addChild(trunk);
			treeSGC.addChild(sgc);
			collect.multiplyOnRight(oneStep);
		}
		treeTip = new SceneGraphComponent("tip");
		collect.assignTo(treeTip);
		SceneGraphComponent tiptrunk = SceneGraphUtility.createFullSceneGraphComponent();
		tiptrunk.setGeometry(foo); 
		MatrixBuilder.euclidean().rotateZ(Math.PI/2).scale(15,1,1).translate(-.3,0,0).assignTo(tiptrunk);
		treeTip.addChild(tiptrunk);
		treeSGC.addChild(treeTip);
		Appearance ap = treeSGC.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor",new Color(234, 152, 70));
		return treeSGC;
	}
	
	class OrnamentSplitter extends SceneGraphVisitor {
		List<SceneGraphComponent> ornaments = new ArrayList<SceneGraphComponent>();
		SceneGraphPath path = new SceneGraphPath();
		SceneGraphComponent root;
		public void setRoot(SceneGraphComponent r)	{
			root = r;
		}
		public List<SceneGraphComponent> visit()	{
			path.clear();
			visit(root);
			return ornaments;
		}
		public void visit(SceneGraphComponent c)	{
			path.push(c);
			if (c.getName().indexOf("ornament") != -1)	{
				SceneGraphComponent sgc = new SceneGraphComponent(c.getName()+"copy");
				sgc.setTransformation(new Transformation(path.getMatrix(null)));
				ornaments.add(sgc);
				path.pop();
				return;
			}
			c.childrenAccept(this);
			path.pop();
		}
	}
	double eyeSep, focus, eyeSep2 = .2, focus2 = 40;
	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		Camera c = CameraUtility.getCamera(viewer);
		this.viewer = viewer;
		if (viewer instanceof de.jreality.jogl.JOGLViewer)
			System.err.println("Yes! its jogl");
		c.setFieldOfView(23);
		snowSGC.setVisible(false);
		CameraUtility.encompass(viewer);
		// encompass sets focus to be middle in the bounding box of the world
		// increasing it as follows "pulls" the object toward the viewer
		c.setFocus(c.getFocus()*1.33);
		// from the dimensions of the auditorium, we require an 150:1 ratio, focus to eyeseparation
		c.setEyeSeparation(.5); //c.getFocus()*.01);
		eyeSep = c.getEyeSeparation();
		focus = c.getFocus();
		System.err.println("eyeSep, focus: "+eyeSep+" "+focus);
		snowSGC.setVisible(true);
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(0,0,20));
		final Color URBackground = new Color(.8f, .85f, .68f);
		final Color ULBackground = new Color(1f, .98f, .8f);
		final Color LLBackground = new Color(.1f, .1f, .25f); 
		final Color LRBackground = new Color(0.05f, .15f, .35f); 
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground; // bg[2];
		ViewerKeyListener.setDefaultCorners(backgroundArray);
//		viewer.getSceneRoot().getAppearance().setAttribute(
//				"backgroundColors", backgroundArray);
//		TextureUtility.createSkyBox(viewer.getSceneRoot().getAppearance(), TextureUtility.getCubeMapImages(reflectionMap));
		
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_1:
					animatingAll = !animatingAll;
					break;

				case KeyEvent.VK_2:
					animatingOrns = !animatingOrns;
					break;

				case KeyEvent.VK_3:
					if (e.isShiftDown())	globalSpeed *= .9;
					else globalSpeed *= 1.111111;
					break;

				case KeyEvent.VK_4:
					showCredits = !showCredits;
					baseSGC.setAppearance(showCredits ? labelAp : noLabelAp);
					break;

				case KeyEvent.VK_5:
					playSnow = !playSnow;
					break;

				case KeyEvent.VK_6:
					resetSnow = true;
//					snowSGC.setVisible(resetSnow);
					int n = snowSGC.getChildComponentCount();
					for (int i= 0; i<n; ++i)	{
						snowStorms[i].multiplyOnLeft(yrestart);
						snowStorms[i].assignTo(snowSGC.getChildComponent(i));
					}					
					break;

				case KeyEvent.VK_7:
					saveFrames = !saveFrames;
					break;

				}
			}

			
		});
		
	}
	

	@Override
	public Component getInspector(Viewer v) {
		return getReadMePanel();
	}

	@Override
	public boolean hasInspector() {
		// TODO Auto-generated method stub
		return true;
	}

	public static Component getReadMePanel()	{
		JPanel mypanel = new JPanel();
		mypanel.setName("ReadMe");
		JTextArea textarea = new JTextArea(10,20);
		textarea.setEditable(false);
		textarea.append("This application shows the Matheon FestBaum\n"+
				"The following key strokes  have effects:\n"+
				"    '1':    toggle rotation of tree.\n"+
				"    '2':    toggle rotation of ornaments.\n"+
				"    '3':    increase rotation speed.\n"+
				"'shift 3':    decrease rotation speed.\n"+
				"    '4':    toggle display of credits.\n"+
				"    '5':    play/pause  snow.\n"+
				"    '6':    reset snow.\n"+
				"    'b':    toggle backgrounds.\n"+
				"    'h':    display help overlay.\n"+
				"Shift-cntl-f:  toggles fullscreen mode.\n"+
				"\nClick on the tab 'Scene Graph' to explore structure\n"+
				"\nSnow crystals from Bentley and Humphreys,\n" +
				"     \"Snow Crystals\", Dover, 1962\n"+
				"\nAuthor: Charles Gunn\n"+
				"    gunn at math.tu-berlin.de\n");
		mypanel.add(textarea);
		return mypanel;
	}
}
