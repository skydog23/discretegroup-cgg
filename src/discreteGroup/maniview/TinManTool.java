package discreteGroup.maniview;

import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import charlesgunn.jreality.geometry.OneArmedTinManFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.SystemProperties;

public class TinManTool extends AbstractTool {

	  static final transient InputSlot headSlot = InputSlot.getDevice("ShipHeadTransformation");
	  static final transient InputSlot wandSlot = InputSlot.getDevice("PointerShipTransformation");
	  int metric = Pn.EUCLIDEAN;
	  EffectiveAppearance eap;
	  int count = 0;
	  boolean active = true, addedTinMan = false;
	  double[] headMatrix = Rn.identityMatrix(4), wandMatrix = Rn.identityMatrix(4);
	  OneArmedTinManFactory oatmf;
	  SceneGraphComponent tm;
	  public TinManTool() {
		 addCurrentSlot(headSlot, "the current head matrix in PORTAL coordinates");
		 //addCurrentSlot(wandSlot, "the current wand matrix in PORTAL coordinates");
		 //addCurrentSlot(InputSlot.getDevice("SystemTime"));
		 oatmf = new OneArmedTinManFactory();
		 tm = oatmf.getTinMan();
		 //oatmf.setFlatten(true);
		 tm.getAppearance().setAttribute("singlePeer", true);
	  }
		  
	  public void perform(ToolContext tc) {
		  if (!active) return;
		  count++;
//		  if ((count % 10) == 0) 
//			  System.err.println("Count is "+count);
//		  else return;
		  if (eap == null || !EffectiveAppearance.matches(eap, tc.getRootToToolComponent())) {
		      eap = EffectiveAppearance.create(tc.getRootToToolComponent());
		  }
		  metric = eap.getAttribute("metric", Pn.EUCLIDEAN);
		  tc.getTransformationMatrix(headSlot).toDoubleArray(headMatrix);
		  tc.getTransformationMatrix(wandSlot).toDoubleArray(wandMatrix);
		  oatmf.setHeadTransformation(headMatrix);
		  wandMatrix[3] -= headMatrix[3];
		  wandMatrix[7] -= headMatrix[7];
		  wandMatrix[11] -= headMatrix[11];
		  oatmf.setHandTransformation(wandMatrix);
		  oatmf.update();
		  if (!SystemProperties.isPortal) tc.getViewer().renderAsync();
//		  System.err.println("Wand tlate is "+Rn.toString(new Matrix(wandMatrix).getColumn(3)));
	  }
	  
	  public void setActive(boolean b)	{
		  active = b;
		  System.err.println("Active is "+active);
	  }
	  
	  public boolean getActive()	{
		  return active;
	  }

	  public OneArmedTinManFactory getTinManFactory()	{
		  return oatmf;
	  }

		static Vector<ChangeListener> listeners = new Vector<ChangeListener>();
		

		public  void addChangeListener(ChangeListener l)	{
			if (listeners.contains(l)) return;
			listeners.add(l);
		}
		
		public  void removeChangeListener(ActionListener l)	{
			listeners.remove(l);
		}
		public  void broadcastChange()	{
			if (listeners == null) return;
			ChangeEvent e = new ChangeEvent(this);
			//SyJOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: broadcasting"+listeners.size()+" listeners");
			if (!listeners.isEmpty())	{
				//JOGLConfiguration.theLog.log(Level.INFO,"SelectionManager: broadcasting"+listeners.size()+" listeners");
				for (ChangeListener l : listeners)	{
					l.stateChanged(e);
				}
			}
		}


}
