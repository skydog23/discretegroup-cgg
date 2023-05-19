/*
 * Created on 9 May 2023
 *
 */
package discreteGroup.quartz;

import static discreteGroup.quartz.QuartzConstants.chan31Color;
import static discreteGroup.quartz.QuartzConstants.chan32Color;
import static discreteGroup.quartz.QuartzConstants.edge3Color;
import static discreteGroup.quartz.QuartzConstants.edge4Color;
import static discreteGroup.quartz.QuartzConstants.siliconColor;
import static discreteGroup.quartz.QuartzConstants.siliconRad;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import charlesgunn.jreality.newtools.FlyTool;
import charlesgunn.jreality.viewer.Assignment;
import charlesgunn.util.TextSlider;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.AbstractDGSGR;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.FiniteStateAutomaton;

public class DiamondCrystal extends Assignment {

	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world"),
	container = SceneGraphUtility.createFullSceneGraphComponent("container"),
		unitcell = SceneGraphUtility.createFullSceneGraphComponent("unit cell"),
		tetrasgc = SceneGraphUtility.createFullSceneGraphComponent("tetra sgc"),
			bassgc,
			tetraGeomSGC = SceneGraphUtility.createFullSceneGraphComponent("tetra geom");

	DiscreteGroupSceneGraphRepresentation dgsgr;
	
	protected static Color[] pointClr = { siliconColor, siliconColor, siliconColor, siliconColor, siliconColor },
			edgeClr = { chan31Color, edge3Color, chan32Color, edge4Color };
	protected static String[] vertexLabels = { "Si", "Si", "Si", "Si", "Si"};
	protected static double[] pointRadii = { siliconRad, siliconRad, siliconRad, siliconRad, siliconRad };
	protected QuartzGeometry qg = new QuartzGeometry(null);
	protected BASTetrahedron basTetra = new DiamondBASTetrahedron();
	
	double scale = .5;
	boolean doCutoff = true;
	int cutoff = 200;
	QuartzCrystal qc = null;
	
	public DiamondCrystal(QuartzCrystal owner) {
		qc = owner;
	}

	@Override
	public SceneGraphComponent getContent() {
		DiscreteGroup dg = getDiamondDiscreteGroup();
		dgsgr = new DiscreteGroupSceneGraphRepresentation(dg, true);
		
		DirichletDomain dd = new DirichletDomain(dg);
		dd.setDirichletDomainOrbit(50);
		dd.update();
		unitcell.setGeometry(dd.getDirichletDomain());
		Appearance ap = unitcell.getAppearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, true);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("lineShader.diffuseColor", Color.white);
		ap.setAttribute("pointShader.diffuseColor", Color.white);
//		ap.setAttribute("polygonShader.diffuseColor", Color.white);
		
		bassgc = basTetra.getBallAndStick(false, QuartzConstants.basScale, pointClr, edgeClr, vertexLabels, pointRadii);
		tetraGeomSGC.setGeometry(QuartzGeometry.getTetrahedron(true));
		tetraGeomSGC.setVisible(false);
		tetrasgc.addChildren(bassgc, tetraGeomSGC);
		container.addChildren(unitcell, tetrasgc);
		MatrixBuilder.euclidean().scale(.25).assignTo(tetrasgc);
		dgsgr.setWorldNode(container);
		dgsgr.getRepresentationRoot().setName("diamond crystal");
		dgsgr.getDropBox().setCutoff(doCutoff ? cutoff : -1);
		dgsgr.update();
//		L23SGR.setConstraint(pruneCL2);
		System.err.println("xyz DGSGR # = "+dgsgr.getElementList().length);
		world.addChild(dgsgr.getRepresentationRoot());
		return world;
	}

	public DiscreteGroupSceneGraphRepresentation getDGSGR() {
		return dgsgr;
	}
	
	public SceneGraphComponent getUnitcell() {
		return unitcell;
	}
	
	protected DiscreteGroup getDiamondDiscreteGroup() {
		DiscreteGroupElement[] gens = new DiscreteGroupElement[6];

		double[][] trans = {{scale,scale,0,1},{scale,0,scale,1},{0,scale, scale,1}};
		String[] names = {"a","b","c"};
		for (int i = 0; i<3; ++i)	{
			double[] tlate = P3.makeTranslationMatrix(null, trans[i], Pn.EUCLIDEAN);
			gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, tlate, names[i]);
			gens[i+3] = gens[i].getInverse();
		}
		DiscreteGroup dg = new DiscreteGroup();
		dg.setGenerators( gens);
		dg.setDimension(3);
		dg.setMetric(Pn.EUCLIDEAN);
		dg.setFinite(false);
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("c1.wa", ResourceClass.class);
		dg.setFsa(fsa);
		DiscreteGroupConstraint smallC = new DiscreteGroupSimpleConstraint(18,-1,15000);
		dg.setConstraint(smallC);
		dg.update();
		return dg;
	}
	LightUtility lu = new LightUtility();
	@Override
	public void display() {
		super.display();
		lu.setLightIntensity(.35);
		lu.setupLights();
		scene.getAvatarComponent().addChildren(lu.getLights());
		SceneGraphPath avatarPath;
		avatarPath = scene.getAvatarPath();

		FlyTool flytool = new FlyTool();
		flytool.setGain(.5);
		avatarPath.getLastComponent().addTool(flytool);
		((Component) viewer.getViewingComponent()).addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e)	{ 
				int m = e.getModifiers();
//				System.err.println("Modifiers = "+m);
				switch(e.getKeyCode())	{
				
				case KeyEvent.VK_1:
					unitcell.setVisible(!unitcell.isVisible());
					break;
					
				case KeyEvent.VK_2:
					doCutoff = !doCutoff;
					dgsgr.getDropBox().setCutoff(doCutoff ? cutoff : -1);
					break;
				}
			}
		});
		
	}
	
	Box vbox = null;

	@Override
	public Component getInspector() {
		if (vbox != null) {
			return inspector;
		}
		vbox = Box.createVerticalBox();
		inspector.add(vbox);
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "diamond crystal")));
		Box hbox = Box.createHorizontalBox();
		vbox.add(hbox);
		final TextSlider<Integer> lSlider = new TextSlider.Integer("xyz # cutoff",  SwingConstants.HORIZONTAL, -1, 12000 ,cutoff);
		lSlider.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cutoff = lSlider.getValue().intValue();
				dgsgr.getDropBox().setCutoff(doCutoff ? cutoff : -1);
				if (viewer == null) qc.getJrviewer().getViewer().renderAsync();
				else viewer.renderAsync();
			}
		});
		hbox.add(lSlider);
		
		final JCheckBox tcb = new JCheckBox("Do cutoff");
		tcb.setSelected(doCutoff);
		tcb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doCutoff = tcb.isSelected();
				dgsgr.getDropBox().setCutoff(doCutoff ? cutoff : -1);
			}
		});
		hbox.add(tcb);
		vbox.add(basTetra.getInspector());
		return inspector;
	}

	public static void main(String[] args) {
		Secure.setProperty(SystemProperties.JOGL_COPY_CAT, "true");
		new DiamondCrystal(null).display();
	}

}
