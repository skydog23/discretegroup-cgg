/*
 * Author	gunn
 * Created on Mar 16, 2006
 *
 */
/*
 * Author	gunn
 * Created on Mar 15, 2006
 *
 */
package discreteGroup.puncturedTorus;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuBar;

import charlesgunn.jreality.SelectionComponent;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.Complex;
import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;

public class CuspRoots extends LoadableScene {

	int numSets = 0;
	private SelectionComponent selC;
	private SceneGraphComponent allVisible;
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		root.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		Complex trace = new Complex(2,0);
		Complex[][] isolatedPts = PuncturedTorusUtility.isolatedPoints(6);
		numSets = isolatedPts.length;
		selC = new SelectionComponent();
		allVisible = SceneGraphUtility.createFullSceneGraphComponent("foog");
		allVisible.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		root.addChild(allVisible); //selC);
		for (int i=0;i<numSets; ++i)	{
			int n = isolatedPts[i].length;
			double[][] pts = new double[n][3];
			pts = Complex.toDouble(pts, isolatedPts[i]);
			PointSetFactory ilsf = new PointSetFactory();
			ilsf.setVertexCount(n);
			ilsf.setVertexCoordinates(pts);
			ilsf.update();
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
			sgc.getAppearance().setAttribute("pointShader.polygonShader.diffuseColor", new Color(.5f, (float) (i/(numSets-1.0)), .5f));
			sgc.getAppearance().setAttribute("pointShader.diffuseColor", new Color(.5f, (float) (i/(numSets-1.0)), .5f));
			PointSet ils = ilsf.getPointSet();
			sgc.setGeometry(ils);
			allVisible.addChild(sgc);
			SceneGraphComponent sgc3 = new SceneGraphComponent();
			sgc3.setAppearance(sgc.getAppearance());
			sgc3.setGeometry(ils);
			selC.addChild(sgc3);
		}
		selC.setSelectedChild(0);
		root.addChild(selC);
		root.setGeometry(new Sphere());
		return root;
	}

	public boolean isEncompass() {
		return false;
	}

	public void customize(JMenuBar menuBar, final Viewer viewer) {
		((Component) viewer.getViewingComponent()).addKeyListener( new KeyAdapter()	{
			int count = 0;
			public void keyPressed(KeyEvent e)	{ 
				switch(e.getKeyCode())	{
					
				case KeyEvent.VK_2:
					allVisible.setVisible( !allVisible.isVisible());
					break;
			
				case KeyEvent.VK_LEFT:
					count--;
					if (count < 0) count = numSets;
					selC.setSelectedChild(count);
					viewer.render(); 
					break;
			
				case KeyEvent.VK_RIGHT:
					count++;
					if (count >= numSets) count = 0;
					selC.setSelectedChild(count);
					viewer.render(); 
					break;
				}
			}
		});
	}

	

}