/*
 * Created on 10 Apr 2023
TODO:
    subclass QuartzCrystal to make different applications
    add buttons to display only the different spiral curves
    add an animated point/sphere that moves along the spiral curves
        at unit speed -- this shows that the two 3-fold channels are 
        polar to each other
    add the sodium chloride crystal as an example of a mirror group
        and contrast to quartz
    use clipping planes to slice through the structure
    control all the tetra edges and colors separately in GUI
    Refine the center camera command to be more surgical
8.5.23
    create 2 cameras, one for outside looking and one for flying inside
    use only one camera path, toggle between the two cameras and the avatar transformation
        (I'm uneasy about animating with two different camera paths)
    
 DONE
    de-saturated the axis colors to make them brighter
    animated the 3-fold generators
 26.04.23
    created SimpleDGSGR and AbstractDGSGR classes to provide light-weight scene graphs
    added a constraint to the DGSGR that allows SGC's to be selectively turned on and off
    	rather than gnerating new scene graphs. First generate a huge scene graph and then
    	use the DGSGR constraint to only show the parts you're interested in.
    cleaned up QuartzGroup to have 4 levels and simplified naming, so that QuartzCrystal
     	can wire the levels together as needed
    fixed problems with TermesSphere due to drawFaces=true
    got a short animation fly-through rendered, loaded into Final Cut Pro (new!), and recorded.
    straightened out the face and edge colors on tetra and molecule,
        so colors are consistent across models
    added axis around the hexagon channels
 *
 */
package discreteGroup.quartz;

import static de.jreality.util.SystemProperties.JREALITY_DATA;
import static discreteGroup.quartz.QuartzConstants.axis3Pts;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.SliceBoxFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Light;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.core.AbstractDGSGR;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;

public class QuartzCrystal extends Assignment {

	protected transient SceneGraphComponent 
		world = SceneGraphUtility.createFullSceneGraphComponent("world"),
			axis3sgc = SceneGraphUtility.createFullSceneGraphComponent("axis 3"),
				axis31sgc = SceneGraphUtility.createFullSceneGraphComponent("axis 3"),
				axis32sgc = SceneGraphUtility.createFullSceneGraphComponent("axis 3"),
			axis6sgc = SceneGraphUtility.createFullSceneGraphComponent("axis 6"),
				order3chan = SceneGraphUtility.createFullSceneGraphComponent("order 3 chan"),
			tetraGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("tetra geom"),
			tetraHalfGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("half tetra geom"),
				tetrasgc = SceneGraphUtility.createFullSceneGraphComponent("tetrasgc"),
				bassgc = SceneGraphUtility.createFullSceneGraphComponent("bassgc"),
				celloutlinesgc = SceneGraphUtility.createFullSceneGraphComponent("rhombsgc"),
		camsgc = SceneGraphUtility.createFullSceneGraphComponent("centered camera");
	
	transient boolean startWithTess = false,
			single = false,
			showAxes = !startWithTess,
			showRhomb = !startWithTess,
			showTetra = false,
			showBAS = true,
			doSliceBox = false,
			doHalfTetra = false,
			doFog = true,
			doCutoff = true,
			doDiamond = false;
	transient public QuartzGeometry quartzGeom = new QuartzGeometry(this);
	transient public QuartzGroup quartzGroup = new QuartzGroup(this);
	DiamondCrystal diamcry = new DiamondCrystal(this);
	LightUtility lu = new LightUtility();

	transient AbstractDGSGR[] sgrLevels = null;
	transient DiscreteGroupSceneGraphRepresentation diamondDGSGR = null;
	transient protected Transformation avatarT;
	transient protected ImplodePolygonShader implodeShader = null;
	transient double implodeFactor = .35,
			lightIntensity = .35;
	transient SliceBoxFactory sbf;	
	transient Camera centerCam = new Camera();
	transient SceneGraphPath centerCamSGP, standardCamSGP, path2World;
	
	@Override
	public SceneGraphComponent getContent() {
		quartzGroup.init();
		
		sgrLevels = quartzGroup.getLevels();
		
		tetrasgc.setVisible(showTetra);
		bassgc.addChild(quartzGeom.getBASTetrahedron().getBallAndStick());
		bassgc.setVisible(showBAS);
		Appearance ap  = tetrasgc.getAppearance();
		DefaultGeometryShader dgs = (DefaultGeometryShader) 
	   			ShaderUtility.createDefaultGeometryShader(ap, true);
		ImplodePolygonShader dps = (ImplodePolygonShader) dgs.createPolygonShader("implode");
		ap.setAttribute("implodeFactor", implodeFactor);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.pointRadius", .01);
		
		tetraGeomSGC.addChildren(tetrasgc, bassgc);
		tetraHalfGeomSGC.addChildren(tetrasgc, bassgc);
		setDoHalfTetra(doHalfTetra);
		
		axis3sgc.addChildren(axis31sgc, axis32sgc);
		ap = axis3sgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader.tubeRadius", .01);
		ap = axis31sgc.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", QuartzConstants.chan31Color);
		ap = axis32sgc.getAppearance();
		ap.setAttribute("lineShader.diffuseColor", QuartzConstants.chan32Color);
		ap = axis6sgc.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader.tubeRadius", .01);
		ap.setAttribute("lineShader.diffuseColor", QuartzConstants.chan6Color);
//		ap.setAttribute("lineShader.diffuseColor", Color.white);
		axis31sgc.setGeometry(quartzGeom.get3Axis());
		axis32sgc.setGeometry(quartzGeom.get3Axis());
		axis6sgc.setGeometry(quartzGeom.get6Axis());
		axis6sgc.setVisible(false);
		axis31sgc.setVisible(!startWithTess);
		axis32sgc.setVisible(false);

		celloutlinesgc.setGeometry(quartzGeom.getCellOutline());
		celloutlinesgc.setVisible(!startWithTess);
		ap = celloutlinesgc.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.pointRadius", .01);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.tubeRadius", .01);
	
		updateTetras();
				
		ap = world.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute(CommonAttributes.RADII_WORLD_COORDINATES, true);
		ap.setAttribute(SceneGraphAnimator.ANIMATED, false);

//		sgrLevels[1].getFundamentalRegion().addChild(sgrLevels[0].getRepresentationRoot());
		sgrLevels[1].getFundamentalRegion().addChildren(tetraGeomSGC, tetraHalfGeomSGC);
		sgrLevels[2].getFundamentalRegion().addChildren(axis3sgc, axis6sgc, celloutlinesgc, sgrLevels[1].getRepresentationRoot());
		sgrLevels[4].getFundamentalRegion().addChildren(axis3sgc, axis6sgc, celloutlinesgc, sgrLevels[1].getRepresentationRoot());
		sgrLevels[3].getFundamentalRegion().addChild(sgrLevels[2].getRepresentationRoot());
		sgrLevels[0].getFundamentalRegion().addChildren(sgrLevels[3].getRepresentationRoot());

		world.addChild(sgrLevels[0].getRepresentationRoot());
		diamcry.getContent();
		diamondDGSGR = diamcry.getDGSGR();
		diamondDGSGR.getRepresentationRoot().setVisible(doDiamond);
		sgrLevels[0].getRepresentationRoot().setVisible(!doDiamond);
		world.addChild(diamondDGSGR.getRepresentationRoot());
		
		setupTimers();
		
		if (doSliceBox) {
		  	sbf = new SliceBoxFactory(world);
		  	sbf.setSeparation(.4);
		  	sbf.update();
			inspector.add(sbf.getInspector());
			SceneGraphComponent foo = sbf.getSliceBoxSGC();
			foo.addTool(new ClickWheelCameraZoomTool());
			return foo;
		}
		
		return world;
	}
	
	public void updateTetras() {
		quartzGeom.updateTetras();
		quartzGeom.getTetraM().assignTo(tetraGeomSGC);
		quartzGeom.getTetraM().assignTo(tetraHalfGeomSGC);
		quartzGeom.getAxis3M().assignTo(axis32sgc);
	}
	
	public void setDoHalfTetra(boolean b) {
		doHalfTetra = b;
		tetrasgc.setGeometry(doHalfTetra ? quartzGeom.getHalfTetrahedron() : quartzGeom.getTetrahedron());
		tetraGeomSGC.setVisible(!doHalfTetra);
		tetraHalfGeomSGC.setVisible(doHalfTetra);
	}
	
	public void updateC(double c) {
		quartzGroup.updateC(c);

	}
	
	DiscreteGroupSimpleConstraint singleton = new DiscreteGroupSimpleConstraint(1),
			oldConstraint = null;


	@Override
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		inspector.add(container);
		final TextSlider<Double> imSlider = new TextSlider.Double("implode",  SwingConstants.HORIZONTAL,-1, 1, implodeFactor);
		imSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				implodeFactor = imSlider.getValue().doubleValue();
				tetrasgc.getAppearance().setAttribute("implodeFactor", implodeFactor);
			}
		});
		container.add(imSlider);
		final TextSlider<Double> liSlider = new TextSlider.Double("light intens",  SwingConstants.HORIZONTAL,0, 1, lightIntensity);
		liSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				lightIntensity = liSlider.getValue().doubleValue();
				lu.updateLights(lightIntensity, scene);
			}
		});
		container.add(liSlider);
		Box buttons = Box.createHorizontalBox();
		container.add(buttons);

		final JCheckBox tcb = new JCheckBox("Rotate channel");
		tcb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean sel = tcb.isSelected();
				if (sel) chan1Timer.start();
				else {
					sgrLevels[1].getRepresentationRoot().getTransformation().setMatrix(Rn.identityMatrix(4));
					chan1Timer.stop();
				}
			}
		});
		buttons.add(tcb);

		final JCheckBox dcb = new JCheckBox("Show diamond crystal");
		dcb.setSelected(doDiamond);
		dcb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doDiamond = dcb.isSelected();
				diamondDGSGR.getRepresentationRoot().setVisible(doDiamond);
				sgrLevels[0].getRepresentationRoot().setVisible(!doDiamond);
				quartzGeom.getInspector().setVisible(!doDiamond);
				quartzGroup.getInspector().setVisible(!doDiamond);
				diamcry.getInspector().setVisible(doDiamond);				
			}
		});
		buttons.add(dcb);

		inspector.add(quartzGeom.getInspector());
		inspector.add(quartzGroup.getInspector());
		inspector.add(diamcry.getInspector());
		quartzGeom.getInspector().setVisible(!doDiamond);
		quartzGroup.getInspector().setVisible(!doDiamond);
		diamcry.getInspector().setVisible(doDiamond);
		return inspector;
	}

	protected transient boolean animate2 = false, animate33 = false, animate36 = false;

	@Override
	public void display() {
		super.display();
		lu.setLightIntensity(lightIntensity);
		lu.setupLights();
		scene.getAvatarComponent().addChildren(lu.getLights());
		camsgc.addChildren(lu.getLights());

		viewer = jrviewer.getViewer();
		Appearance rap = viewer.getSceneRoot().getAppearance();
		Color bkgdclr = new Color(51,51,51);
		rap.setAttribute(CommonAttributes.BACKGROUND_COLOR,bkgdclr); 
		rap.setAttribute(CommonAttributes.TUBE_RADIUS, .01);

		rap.setAttribute(CommonAttributes.FOG_MODE,2);
		rap.setAttribute(CommonAttributes.FOG_BEGIN, 2.0);
		rap.setAttribute(CommonAttributes.FOG_END, 6.0);
		rap.setAttribute(CommonAttributes.FOG_DENSITY, .2);
		rap.setAttribute(CommonAttributes.FOG_COLOR, bkgdclr);
		updateFog();
		
		// set near and far clipping plane
		Camera cam = CameraUtility.getCamera(viewer);
		// these are settings for lying aroumd inside.
		updateCamera(cam);
		standardCamSGP = viewer.getCameraPath();
		path2World = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), world).get(0);
		viewer.getSceneRoot().addChild(camsgc);
		camsgc.setCamera(cam);
		centerCamSGP = new SceneGraphPath();
		centerCamSGP.push(viewer.getSceneRoot());
		centerCamSGP.push(camsgc);
		centerCamSGP.push(centerCam); //CameraUtility.getCamera(viewer));

		FlyTool flytool = new FlyTool();
		flytool.setGain(.5);

		camsgc.addTool(flytool);

		// activate scene graph animation selectively
		animationPlugin.setAnimateCamera(true);
		animationPlugin.setAnimateSceneGraph(true);
		animationPlugin.getAnimationPanel().getRecordPrefs().setCurrentDirectoryPath("/Volumes/SamsungSSD1T/gunn_local/Movies/quartz/");
		animationPlugin.getAnimationPanel().setResourceDir("src/discretegroup/quartz/");
		SceneGraphPath avatarPath;
		avatarPath = scene.getAvatarPath();
		avatarT = avatarPath.getLastComponent().getTransformation();
		flytool = new FlyTool();
		flytool.setGain(.5);
		avatarPath.getLastComponent().addTool(flytool);
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)	{ 
				int m = e.getModifiers();
//				System.err.println("Modifiers = "+m);
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_1:
					if ((m&1) == 0) axis31sgc.setVisible(!axis31sgc.isVisible());
					else axis32sgc.setVisible(!axis32sgc.isVisible());
					break;
				case KeyEvent.VK_2:
					axis6sgc.setVisible(!axis6sgc.isVisible());
					break;
				case KeyEvent.VK_3:
					showRhomb = !showRhomb;
					celloutlinesgc.setVisible(showRhomb);
					diamcry.getUnitcell().setVisible(showRhomb);
					break;
					
				case KeyEvent.VK_4:
					showTetra = !showTetra;
					tetrasgc.setVisible(showTetra);
					break;

				case KeyEvent.VK_5:
					showBAS = !showBAS;
					bassgc.setVisible(showBAS);
					diamcry.getBAS().setVisible(showBAS);
					break;

				case KeyEvent.VK_6:
					updateCamera(CameraUtility.getCamera(viewer));
					break;

				case KeyEvent.VK_7:
					jumpToCenter();
					break;
					
				case KeyEvent.VK_8:
					double cnear = centerCam.getNear();
					if ((m&1) == 0) cnear *= 2;
					else cnear *= .5;
					centerCam.setNear(cnear);
					System.err.println("Camera near = "+centerCam.getNear());
					break;

				case KeyEvent.VK_9:
					printInfo();
					break;

				case KeyEvent.VK_0:
					doFog = !doFog;
					updateFog();
					break;


				}
			}

		});
	
	}

	private void updateFog() {
		viewer.getSceneRoot().getAppearance().setAttribute(CommonAttributes.FOG_ENABLED, doFog);
	}
	
	private void printInfo() {
		int total = 1;
		int[] levels = new int[4];
		for (int i = 0; i<sgrLevels.length; ++i)	{
			SceneGraphComponent sgc = sgrLevels[i].getSceneGraphRepn();
			int n = sgc.getChildComponentCount();
			for (int j = 0; j<n; ++j)	{
				if (sgc.getChildComponent(j).isVisible()) levels[i]++;
			}
			total *= levels[i];
		}
		System.err.println("# of visible sgcs = "+total);
		
	}

	Timer chan1Timer, spiralBallTimer;
	boolean chan1Run = false;
	private void setupTimers() {
		chan1Timer = new Timer(10, new ActionListener() {
			double t = 0, dt = .01;
			@Override
			public void actionPerformed(ActionEvent e) {
				double[] mat = MatrixBuilder.euclidean().rotate(axis3Pts[0], axis3Pts[1], -t).getArray();
				sgrLevels[1].getRepresentationRoot().getTransformation().setMatrix(mat);
				t += dt;
			}
		}); 
		
		spiralBallTimer = new Timer(10, new ActionListener() {
			double t = 0, dt = .01;
			@Override
			public void actionPerformed(ActionEvent e) {
				double[] mat = MatrixBuilder.euclidean().rotate(axis3Pts[0], axis3Pts[1], -t).getArray();
				sgrLevels[1].getRepresentationRoot().getTransformation().setMatrix(mat);
				t += dt;
			}
		}); 

	}
	protected void centerCamera() {
		avatarT.setMatrix(Rn.identityMatrix(4));
	}

	private void updateCamera(Camera cam) {
		cam.setNear(.017);
		cam.setFar(12.0);
		cam.setEyeSeparation(.05);
		cam.setFocus(2.5);
		System.err.println("camera near = "+cam.getNear());
	}
	
	boolean isCenterCam = false;
	private void jumpToCenter() {
		isCenterCam = !isCenterCam;
		if (isCenterCam) {
			double[] r2w = path2World.getMatrix(null);
			Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(world);
			System.err.println("BBox = "+bbox.toString());
			double[] cc = bbox.getCenter();
			double[] w2center = MatrixBuilder.euclidean().translate(cc[0], cc[1], cc[2]).getArray();
			new Matrix(Rn.times(null, r2w, w2center)).assignTo(camsgc);
			updateCamera(centerCam);
		}
		viewer.setCameraPath(isCenterCam ? centerCamSGP : standardCamSGP);
		viewer.renderAsync();
		System.err.println("Toggled centered cam"+isCenterCam);
	}

	public static void main(String[] args) {
		Secure.setProperty(SystemProperties.JOGL_COPY_CAT, "true");
		Secure.setProperty(JREALITY_DATA, "/Volumes/SamsungSST1T/gunn_local/Movies/quartz/images/");;
		new QuartzCrystal().display();

	}

}
