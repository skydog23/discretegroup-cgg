package discreteGroup.demo;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JMenuBar;
import javax.swing.SwingConstants;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.CP1;
import charlesgunn.math.Complex;
import charlesgunn.math.PSL2C;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.core.FiniteStateAutomatonUtility;
import discreteGroup.ResourceClass;
import discreteGroup.parser.ComplexListLexer;
import discreteGroup.parser.ComplexListParser;


public class PSL2CExample extends LoadableScene{
	private double[][] fdVertices;
    double alpha = 0.0, phi = 0.0;
	private DiscreteGroup originalGroup;
	private SceneGraphComponent theGroupRepn;
	private boolean changed = true;
	private SceneGraphComponent theWorld;
	private double[][] matrices;
	private SceneGraphComponent[] children = new SceneGraphComponent[4];
	private SceneGraphComponent sgc4;
	private DiscreteGroupSceneGraphRepresentation sgr;
	int numCopies = 300;
	public static void main(String[] args) throws Exception {
		
		PSL2CExample tp = new PSL2CExample();
		tp.makeWorld();
	}
	
	public SceneGraphComponent makeWorld()	{
		InputStream is = ResourceClass.class.getResourceAsStream("resources/groups/fabre-test.psl2c");
		BufferedReader bd = new BufferedReader(new InputStreamReader(is));
		ComplexListParser p=new ComplexListParser(new ComplexListLexer(bd)); //new FileReader(new File("testAll.m"))));
		Vector cmp = null;
		try {
			cmp = p.start();
		} catch (RecognitionException e1) {
			e1.printStackTrace();
		} catch (TokenStreamException e1) {
			e1.printStackTrace();
		}
		//SceneGraphComponent cmp =de.jreality.reader.Readers.read(Input.getInput("testAll.m"));
		if (cmp==null) System.out.println("kein Graph !!!!!!!!!!!!!!!!");
		else System.err.println("Read list of size "+cmp.size());
		Vector dim = p.getDim(new Vector(), cmp);
		for (int i = 0; i<dim.size(); ++i) System.err.print(" "+((Integer) dim.get(i)).intValue());
		DiscreteGroupElement[] gens = new DiscreteGroupElement[14];
		int n = cmp.size();
		Complex[] lft = new Complex[4];
		for (int i = 0; i<n; ++i)		{
			Vector sub = (Vector) cmp.get(i);
			lft[0] = (Complex) ((Vector) sub.get(0)).get(0);
			lft[1] = (Complex) ((Vector) sub.get(0)).get(1);
			lft[2] = (Complex) ((Vector) sub.get(1)).get(0);
			lft[3] = (Complex) ((Vector) sub.get(1)).get(1);
			gens[i] = new DiscreteGroupElement();
			gens[i].setArray(CP1.convertPSL2CToSO31(null, lft));
			if (i < 7) gens[i].setWord(DiscreteGroupUtility.genNames[i]);
			else gens[i].setWord(DiscreteGroupUtility.genInvNames[i-7]);
		}
		// a*F
		double[] p1 = Rn.times(null, gens[0].getArray(), gens[12].getArray());
		// a*F*e
		double[] p2 = Rn.times(null,p1,gens[4].getArray());
		System.err.println("Product aF is: "+Rn.matrixToString(p1));
		System.err.println("e is: "+Rn.matrixToString(gens[4].getArray()));
		System.err.println("Product aFe is: "+Rn.matrixToString(p2));
		is = ResourceClass.class.getResourceAsStream("resources/groups/fabre-hexagon.txt");
		bd = new BufferedReader(new InputStreamReader(is));
		p=new ComplexListParser(new ComplexListLexer(bd)); //new FileReader(new File("testAll.m"))));
		try {
			cmp =p.start();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TokenStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (cmp==null) System.out.println("kein Graph !!!!!!!!!!!!!!!!");
		else System.err.println("\nRead list of size "+cmp.size());
		dim = p.getDim(new Vector(), cmp);
		for (int i = 0; i<dim.size(); ++i) System.err.print(" "+((Integer) dim.get(i)).intValue());
		n = cmp.size();
		fdVertices = new double[n][4];
		for (int i = 0; i<n ; ++i)	{
			lft[0] = (Complex) cmp.get(i);
			double f = 2.0/(Complex.lengthSquared(lft[0])+1);
			fdVertices[i][0] = lft[0].re*f;
			fdVertices[i][1] = lft[0].im*f;
			fdVertices[i][2] = 0.0;
			fdVertices[i][3] = 1.0;
		}
		double[] midpoint = Pn.linearInterpolation(null, fdVertices[0], fdVertices[1], .5, Pn.HYPERBOLIC);
		double[] midpointz = new double[]{midpoint[0], midpoint[1], midpoint[2] + 1.0, midpoint[3]};
		double[] rot180 = P3.makeRotationMatrix( null, midpoint, midpointz, Math.PI, Pn.HYPERBOLIC);
		double[] mirrory = P3.makeReflectionMatrix(null, new double[]{1,0,0,0}, Pn.HYPERBOLIC);
		double[] mirrory180 = Rn.times(null, mirrory, rot180);
		matrices = new double[][]{Rn.identityMatrix(4), rot180, mirrory, mirrory180};
		originalGroup = new DiscreteGroup();
		originalGroup.setGenerators(gens);
		originalGroup.setConstraint(new DiscreteGroupSimpleConstraint(numCopies)); //setMaxNumberElements(2000);
		originalGroup.setDimension(3);
		originalGroup.setMetric(Pn.HYPERBOLIC);
		FiniteStateAutomaton fsa = FiniteStateAutomatonUtility.generateFiniteStateAutomatonForGroup(originalGroup);
//		FiniteStateAutomaton fsa = new FiniteStateAutomaton("fabre-14gens.wa");
		System.err.println("Found fsa "+fsa.toString());
		originalGroup.setFsa(fsa);
		// check for duplicates
//		String[] list = DiscreteGroupUtility.getDuplicates(originalGroup, null);
//		System.err.println("Found "+list.length+" duplicates");
		sgc4 = new SceneGraphComponent();
		sgc4.setName("four copies");
		Color[] colors = {new Color(1f,.4f,.4f), new Color(.4f,1f,.4f), new Color(.4f,.4f,1f), new Color(1f,1f,1f)};
		sgc4.setAppearance(new Appearance());
		sgc4.getAppearance().setAttribute(CommonAttributes.MANY_DISPLAY_LISTS, true);
		sgc4.getAppearance().setAttribute(CommonAttributes.ANY_DISPLAY_LISTS, false);
		for (int i = 0; i<4; ++i)	{
			children [i] = new SceneGraphComponent();
			children[i].setName("Hexagon");
			Appearance ap = new Appearance();
			ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,colors[i]);
			children[i].setTransformation(new Transformation(matrices[i]));
		//	ap.setAttribute(CommonAttributes.IMPLODE_FACTOR, -0.9);
			children[i].setAppearance(ap);
			sgc4.addChild(children[i]);
		}
		update();
		SceneGraphComponent sphere= Primitives.wireframeSphere();
		sphere.setPickable(false);
		theWorld.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		theWorld.addChild(sphere);
		return theWorld;
	}

	private void update() {
		if (!changed) return;
		DiscreteGroup dg = dehnTwist(originalGroup, alpha, phi);
		sgr = null;
		if (sgr == null) sgr = new DiscreteGroupSceneGraphRepresentation(dg, true);
		double[] dtmat = dehnTwistMatrix(alpha, phi);
//		fdVertices = Rn.matrixTimesVector(null, dtmat, fdVertices);
		IndexedFaceSet ifs = IndexedFaceSetUtility.constructPolygon(fdVertices);
		for (int i = 0; i<4; ++i)	{
			children[i].setGeometry(ifs);
		}
		sgr.setWorldNode(sgc4);
		sgr.setElementList(dg.getElementList());
		System.err.println("Group has "+dg.getElementList().length+" elements");
		sgr.getRepresentationRoot().getAppearance().setAttribute(CommonAttributes.MANY_DISPLAY_LISTS, false);
		sgr.update();
		if (theGroupRepn != null) theWorld.removeChild(theGroupRepn);
		theGroupRepn = sgr.getRepresentationRoot();
		if (theWorld == null) theWorld = SceneGraphUtility.createFullSceneGraphComponent();
		theWorld.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.VERTEX_SHADER,"simple");
		theWorld.addChild(theGroupRepn);
		changed = false;
		if (viewer != null) viewer.render();
	}

	private void setPhi(double d) {
		phi = d;
		changed  = true;
		
	}	       	
	private void setAlpha(double d) {
		alpha = d;
		changed  = true;
	}	       	

	private void setNumberCopies(int i) {
		numCopies = i;
		originalGroup.getConstraint().setMaxNumberElements(i);
		originalGroup.update();
		System.err.println("original group has "+originalGroup.getElementList().length+" elements");
		changed = true;
	}
	private double[] dehnTwistMatrix(double d, double e)		{
		PSL2C dehntwist = PSL2C.dehnTwist(d,e);
		// convert to 4x4 projectivev matrix
		double[] pp = 	CP1.convertPSL2CToSO31(null, dehntwist.m);
		return pp;
	}
	private DiscreteGroup dehnTwist(DiscreteGroup dg, double d, double e) {
		DiscreteGroup ndg = new DiscreteGroup();
		DiscreteGroupElement[] gens = dg.getGenerators();
		double[] pp = dehnTwistMatrix(d,e);
		PSL2C dehntwistmm = PSL2C.dehnTwist(-d,-e);
		double[] mm = 	CP1.convertPSL2CToSO31(null, dehntwistmm.m);
		double[] ipp = 	Rn.inverse(null, pp);
		
		double[] imm = 	Rn.inverse(null, mm);
		int n = gens.length;
		DiscreteGroupElement[] newgens = new DiscreteGroupElement[n];
		ndg.setGenerators(newgens);
		for (int i = 0; i<n; ++i)	{
				newgens[i] = new DiscreteGroupElement(gens[i]);
		}
		newgens[0].setArray(Rn.times(null, Rn.times(null, mm, gens[0].getArray()), ipp));
		newgens[1].setArray(Rn.times(null, Rn.times(null, mm, gens[1].getArray()), imm));
		newgens[2].setArray(Rn.times(null, Rn.times(null, mm, gens[2].getArray()), ipp));
		newgens[3].setArray(Rn.times(null, Rn.times(null, mm, gens[3].getArray()), imm));
		newgens[4].setArray(Rn.times(null, Rn.times(null, pp, gens[4].getArray()), imm));
		newgens[5].setArray(Rn.times(null, Rn.times(null, pp, gens[5].getArray()), ipp));
		newgens[6].setArray(Rn.times(null, Rn.times(null, pp, gens[6].getArray()), imm));
		for (int i = 0; i<n/2; ++i)	{
			newgens[7+i] = (DiscreteGroupElement) newgens[i].getInverse();
		}
		ndg.setMetric(dg.getMetric());
//		ndg.setMaxNumberElements(dg.getMaxNumberElements());
		ndg.setConstraint(dg.getConstraint());
		ndg.setDimension(dg.getDimension());
		for (int i = 0; i<4; ++i)	{
			children[i].getTransformation().setMatrix( Rn.times(null, ((i%2)==0)?pp:mm, matrices[i] ));
			DefaultMatrixSupport.getSharedInstance().storeAsDefault(children[i].getTransformation());
		}

	    return ndg;
	}
	public boolean hasInspector() {return true; }
	public Component getInspector(Viewer viewer) {
		Box container = Box.createVerticalBox();
		final TextSlider aSlider = new TextSlider.Double("alpha",  SwingConstants.HORIZONTAL, 0.0, 2.5, alpha);
		aSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setAlpha(aSlider.getValue().doubleValue());
				update();
			}
		});

		container.add(aSlider);
		final TextSlider bSlider = new TextSlider.Double("phi",  SwingConstants.HORIZONTAL, 0.0, 2.5, phi);
		bSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setPhi(bSlider.getValue().doubleValue());
				update();
			}
		});
		container.add(bSlider);

		final TextSlider nSlider = new TextSlider.Integer("number of copies",  SwingConstants.HORIZONTAL, 1, 2000, numCopies);
		nSlider.addActionListener(new ActionListener()	{
			public void actionPerformed(ActionEvent e)	{
				setNumberCopies(nSlider.getValue().intValue());
				update();
			}

		});
		container.add(nSlider);
		container.add(Box.createVerticalGlue());
		return container;
	}

	public boolean isEncompass() {
		// TODO Auto-generated method stub
		return true;
	}

	Viewer viewer;
	public void customize(JMenuBar menuBar, Viewer v) {
		// TODO Auto-generated method stub
		viewer = v;
	}
	    
}