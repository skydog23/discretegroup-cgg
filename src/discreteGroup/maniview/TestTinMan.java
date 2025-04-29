package discreteGroup.maniview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import charlesgunn.anim.core.FramedCurve;
import charlesgunn.jreality.geometry.OneArmedTinManFactory;
import charlesgunn.jreality.geometry.SnakeFactory;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.jreality.viewer.GlobalProperties;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Rectangle3D;

public class TestTinMan extends Assignment {
	private SceneGraphComponent tm, stickTipSGC;
	double height = 1.0, angle = 0.0, handAngle = 0.0, distance = .5;
	private OneArmedTinManFactory tmf;
	SnakeFactory sf;
	FramedCurve fc = new FramedCurve();
	Timer tt;
	@Override
	public SceneGraphComponent getContent() {
		if (GlobalProperties.isPortal)	{
			TinManTool tmt = new TinManTool();
			tm = new SceneGraphComponent("avatar Repn");
			tm.setAppearance(new Appearance());
			tm.addTool(tmt);
			tmt.setActive(false);
		} else {
			tmf = new OneArmedTinManFactory();
			tm = tmf.getTinMan();
			tmf.setFlatten(false);
			Rectangle3D bb = BoundingBoxUtility.calculateBoundingBox(tm);
			System.err.println("Bbox is "+bb);
		}
//		tm.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255,200,0));
//		tm.getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", new Color(255,200,0));
//		tm.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", Color.red);
		tm.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		SceneGraphComponent world = new SceneGraphComponent();
//		GeometryMergeFactory gmf = new GeometryMergeFactory();
//		Geometry g = gmf.mergeGeometrySets(tm);
//		world.setGeometry(g);
		world.addChild(tm);
		SceneGraphComponent circle = new SceneGraphComponent("circle");
		circle.setAppearance(new Appearance());
		circle.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		circle.setGeometry(IndexedLineSetUtility.circle(10));
		MatrixBuilder.euclidean().scale(.1).assignTo(circle);
		world.addChild(circle);
		if (GlobalProperties.isPortal) MatrixBuilder.euclidean().translate(0,1,-3).assignTo(world); 
		else MatrixBuilder.euclidean().rotateY(Math.PI).assignTo(world);
		stickTipSGC = new SceneGraphComponent("stick tip");
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, true);
		ap.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, false);
//		ap.setAttribute("pointShader.polygonShader.diffuseColor", new Color(0,255,255));
//		ap.setAttribute("pointShader.pointRadius", .1);
		ap.setAttribute("lineShader.polygonShader.diffuseColor", new Color(0,255,255));
		ap.setAttribute("lineShader.tubeRadius", .02);
		stickTipSGC.setAppearance(ap);
		sf = new SnakeFactory(500, 3);
		sf.update();
//		tip.setVertexCount(1);
//		tip.setVertexCoordinates(new double[]{0,0,0});
//		tip.update();
//		stickTipSGC.setGeometry(tip.getPointSet());
		stickTipSGC.setGeometry(sf.getSnake());
		world.addChild(stickTipSGC);
		int numCP = 10;
		double[][] mats = new double[numCP+1][];
		for (int i = 0; i<numCP; ++i)	{
			double[] axis = {myrandom(), myrandom(), myrandom()};
			double angle = Math.random() * Math.PI;
			double[] tlate = {myrandom()+.34, myrandom()-.3, myrandom()+.17};
			mats[i] = Rn.times(null, P3.makeTranslationMatrix(null, tlate, Pn.EUCLIDEAN ), 
						P3.makeRotationMatrix(null, axis, angle));
		}
		mats[10] = mats[0];
		fc.setControlPoints(mats);
		//update();
		tt = new Timer(20,new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				update();
				
			}
			
		});
		tt.start();
		world.addChild(fc.getSceneGraphRepresentation());
		return world;
	}
	private double myrandom()	{ return -1+2*Math.random(); }
	final double[] fd = Rn.normalize(null, new double[]{1,-1,-1});
	Matrix headMat = new Matrix(), wandMat = new Matrix();
	boolean automate = false;
	double time = 0.0, dt = .003;
	private void update()	{
		if (automate)	{
			double[] handTform = fc.getValueAtTime(time).getArray();
			tmf.setHandTransformation(handTform);
			time += dt;
			time = time % 1.0;
		} else {
			MatrixBuilder.euclidean().translate(0,1.7*height,0).rotate(-angle, 1,0,0).assignTo(headMat);
			tmf.setHeadTransformation(headMat.getArray());
			double x = distance*fd[0]+.34; //(.5+distance)*Math.sin(distance*Math.PI/2);
			double y = distance*fd[1]-.3; //(.5+distance)*Math.cos(distance*Math.PI/2);
			double z = distance*fd[2]+.17;
			MatrixBuilder.euclidean().translate(x,y,z).rotate(-handAngle, 0,1,0).assignTo(wandMat);
			tmf.setHandTransformation(wandMat.getArray());			
		}
		tmf.update();
//		tip.setVertexCoordinates(tmf.getStickTipWorldPosition());
//		tip.update();
		sf.addPoint(tmf.getStickTipWorldPosition());
		sf.update();
	}
	
	@Override
	public void display() {
		// TODO Auto-generated method stub
		super.display();
		
		Component comp = ((Component) viewer.getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: restart timer");
						break;
		
					case KeyEvent.VK_1:
						System.err.println("keystroke 1");
						tt.stop();
						sf.reset();
						sf.update();
						tt.start();
						update();
						break;
					}
				}
			});

	}
	@Override
	public Component getInspector() {
		Box panel = inspector; //Box.createVerticalBox();
		panel.setName("tin man");
		final TextSlider ts = new TextSlider.Double("height",SwingConstants.HORIZONTAL,0.0,1.0,height);
		ts.addActionListener(new ActionListener()	{

			public void actionPerformed(ActionEvent e) {
				height = ts.getValue().doubleValue();
				//tmf.setHeight(height);
				update();
			}
			
		});
		panel.add(ts);
		final TextSlider as = new TextSlider.Double("head angle",SwingConstants.HORIZONTAL,0.0,1.0,angle);
		as.addActionListener(new ActionListener()	{


			public void actionPerformed(ActionEvent e) {
				angle = as.getValue().doubleValue() * Math.PI/2;
				update();
			}
			
		});
		panel.add(as);
		final TextSlider hds = new TextSlider.Double("hand distance",SwingConstants.HORIZONTAL,0.0,1.0,0);
		hds.addActionListener(new ActionListener()	{


			public void actionPerformed(ActionEvent e) {
				distance = hds.getValue().doubleValue();
				update();
			}
			
		});
		panel.add(hds);
		final TextSlider has = new TextSlider.Double("hand angle",SwingConstants.HORIZONTAL,0.0,1.0,handAngle);
		has.addActionListener(new ActionListener()	{


			public void actionPerformed(ActionEvent e) {
				handAngle = has.getValue().doubleValue() * Math.PI - Math.PI/2;
				update();
			}
			
		});
		panel.add(has);
		panel.setPreferredSize(new Dimension(400,40));
		return panel;
	}

//	@Override
//	public boolean hasInspector() {
//		return true;
//	}
	
	public static void main(String[] args) {
		new TestTinMan().display();
	}
	
}
