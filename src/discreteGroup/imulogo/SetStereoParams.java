package discreteGroup.imulogo;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.TriangleGroup;

public class SetStereoParams extends LoadableScene {

	double focus = IMULogo.initialFocus;
	double frontOfScreenFactor = IMULogo.frontOfScreenFactor;
	double eyeSeparationFactor = IMULogo.eyeSeparationFactor;
	double eyeSeparation = focus / eyeSeparationFactor;
	double initialFOV =IMULogo.initialFOV;
	final double initialZ = IMULogo.initialZ;
	static Viewer viewer;
	Camera cam;
	InfoOverlay info;
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent oneFace = SceneGraphUtility.createFullSceneGraphComponent("oneFace");
		Appearance ap = oneFace.getAppearance();
		ap.setAttribute("polygonShader.diffuseColor", Color.yellow);
		ap.setAttribute("lineShader.diffuseColor", Color.red);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("pointShader.diffuseColor", Color.blue);
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(3);
		double val = .5*Math.sqrt(2);
		double[][] verts = {{0,val,val},{val,0,val},{val,val,0}};
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(1);
		ifsf.setFaceIndices(new int[][]{{0,1,2}});
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		oneFace.setGeometry(ifsf.getGeometry());
		DiscreteGroup pointGroupS222 = TriangleGroup.instanceOfGroup("*222");
		DiscreteGroupSceneGraphRepresentation sgr = new DiscreteGroupSceneGraphRepresentation(pointGroupS222);
		sgr.setWorldNode(oneFace);
		sgr.update();
		return sgr.getRepresentationRoot();
	}

	final Color URBackground = new Color(.8f, .85f, .68f); //new Color(215, 215, 190);
	final Color ULBackground  = new Color(1f, .98f, .8f); //new Color(255, 255, 200);  // bg[1];
	final Color LLBackground  = new Color(.1f, .1f, .25f); //new Color(20,20,60);
	final Color LRBackground  = new Color(0.05f, .15f, .35f); //new Color(25, 25, 100);  //bg[2];
	CameraListener cl;
	@Override
	public void customize(JMenuBar menuBar, final Viewer viewer) {
		this.viewer = viewer;
		cam = CameraUtility.getCamera(viewer);
		Color[] backgroundArray = new Color[4];
		backgroundArray[0] = URBackground;
		backgroundArray[1] = ULBackground;// bg[1];
		backgroundArray[2] = LLBackground;
		backgroundArray[3] = LRBackground;  //bg[2];
		viewer.getSceneRoot().getAppearance().setAttribute("backgroundColors", backgroundArray);
		setCameraDistance(focus);
		CameraUtility.getCamera(viewer).setFieldOfView(initialFOV);
		info = new InfoOverlay();
		info.setInstrumentedViewer((de.jreality.jogl.InstrumentedViewer) viewer);
		info.setPosition(InfoOverlay.LOWER_LEFT);
		info.setVisible(true);
		info.setInfoProvider(new InfoOverlay.InfoProvider() {

			List<String> infoStrings = new Vector<String>();
			public void updateInfoStrings(InfoOverlay io)	{
				//JOGLConfiguration.theLog.log(Level.INFO,"Providing info strings");
				infoStrings.clear();
				Camera cam = CameraUtility.getCamera(viewer);
				infoStrings.add("fov: "+cam.getFieldOfView());
				infoStrings.add("focus:"+cam.getFocus());
				infoStrings.add("eye sep: "+cam.getEyeSeparation());
				Matrix m = new Matrix(CameraUtility.getCameraNode(viewer).getTransformation());
				infoStrings.add("distance: "+m.getEntry(2, 3));
					//System.err.println(theMainRepn.getCopyCatCount());
				io.setInfoStrings(infoStrings);
			}
		});
		cam.addCameraListener(cl = new CameraListener() {

			public void cameraChanged(CameraEvent ev) {
				cam.removeCameraListener(cl);
				double oldFOV = cam.getFieldOfView();
				cam.setFieldOfView(.6*oldFOV);
				CameraUtility.encompass(viewer, viewer.getSceneRoot(), false, Pn.EUCLIDEAN);
				cam.setFieldOfView(oldFOV);
				double tan0 = Math.tan(.5*initialFOV*Math.PI/180.0);
				double tann = Math.tan(.5* oldFOV*Math.PI/180.0);
				double z = initialZ*tan0/tann;
				// TODO why aren't stereo parameters adjusted here?
				CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(
						P3.makeTranslationMatrix(null, new double[]{0,0,z},Pn.EUCLIDEAN));
				cam.addCameraListener(cl);
			}
			
		});
	}
	
	void setCameraDistance(double d)	{
		CameraUtility.getCameraNode(viewer).getTransformation().setMatrix(
				P3.makeTranslationMatrix(null, new double[]{0,0,d},Pn.EUCLIDEAN));
		CameraUtility.getCamera(viewer).setFocus(frontOfScreenFactor*d);
		CameraUtility.getCamera(viewer).setEyeSeparation(frontOfScreenFactor*d/eyeSeparationFactor);
	}
	
	@Override
	public Component getInspector(Viewer v) {
		Box box = Box.createVerticalBox();
		box.setName("stereo");
		TextSlider<Double> focusSl = new TextSlider.Double("focus",SwingConstants.HORIZONTAL,0,30,focus);
		focusSl.addActionListener(new ActionListener()	{

			public void actionPerformed(ActionEvent e) {
				
			}
			
		});
		box.add(focusSl);
		TextSlider<Double> ratioSl = new TextSlider.Double("ratioSl",SwingConstants.HORIZONTAL,0,30,1.0/frontOfScreenFactor);
		ratioSl.addActionListener(new ActionListener()	{

			public void actionPerformed(ActionEvent e) {
				
			}
			
		});
		box.add(ratioSl);
		TextSlider<Double> fovSL = new TextSlider.Double("FOV",SwingConstants.HORIZONTAL,0,60, initialFOV);
		fovSL.addActionListener(new ActionListener()	{

			public void actionPerformed(ActionEvent e) {
				
			}
			
		});
		box.add(fovSL);
		return box;
	}

	@Override
	public boolean hasInspector() {
		return false;
	}

	public static void main(String[] args) {
		SetStereoParams ssp = new SetStereoParams();
		SceneGraphComponent content = ssp.makeWorld();
		JRViewer jrv = new JRViewer();
		jrv.addBasicUI();
		jrv.addContentSupport(ContentType.Raw);
		jrv.setContent(content);
		jrv.startup();
		viewer = jrv.getPlugin(View.class).getViewer();
		ssp.setCameraDistance(ssp.focus);
		CameraUtility.getCamera(viewer).setFieldOfView(ssp.initialFOV);
	}

}
