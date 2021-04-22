package discreteGroup.methaneHydrate;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.groups.PointGroup3S2;
import de.jtem.discretegroup.groups.TriangleGroup;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;

public class MethaneHydrateUtility {

	static double[][] vs = {
			{0,0,0,1},
			{1,1,1,1},
	};
	static double[][] planes = {
			{1,0,0,0},
			{1,0,0,-2},
			{0,1,0,0},
			{0,1,0,-2},
			{0,0,1,0},
			{0,0,1,-2}
	};
	static double[][] points = {
			{1,2,0}, {1,0,2},
			{2,1,0}, {0,1,2}
	};
	

	
	public static DiscreteGroup getFullGroup2() {
//		if (fullGroup != null) return fullGroup;
		DiscreteGroupElement[] gens = new DiscreteGroupElement[5];
		int gcount = 0;
//		for (; gcount < 1; ++gcount)	{
			double[] mat = P3.makeReflectionMatrix(null, planes[4], Pn.EUCLIDEAN);
			gens[gcount] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
			gens[gcount].setWord(DiscreteGroupUtility.genNames[gcount]);
//		}
			gcount++;
		mat = P3.makeRotationMatrix(null, points[0], points[1], Math.PI, Pn.EUCLIDEAN);
		gens[gcount] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
		gens[gcount].setWord(DiscreteGroupUtility.genNames[gcount]);
		gcount++;
		mat = P3.makeRotationMatrix(null, points[2], points[3], Math.PI, Pn.EUCLIDEAN);
		gens[gcount] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
		gens[gcount].setWord(DiscreteGroupUtility.genNames[gcount]);
		gcount++;
		mat = P3.makeRotationMatrix(null, vs[0], vs[1], 2*Math.PI/3.0,Pn.EUCLIDEAN);
		gens[gcount] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
		gens[gcount].setWord(DiscreteGroupUtility.genNames[gcount]);
		gcount++;
		gens[gcount] = (DiscreteGroupElement) gens[gcount-1].getInverse();
//		for (int i = 0; i<4; ++i)	{
//			gens[i+4] = (DiscreteGroupElement) gens[i].getInverse();
//		}
		DiscreteGroup fullGroup = new DiscreteGroup();
		fullGroup.setGenerators(gens);
		fullGroup.setDimension(3);
		fullGroup.setMetric(Pn.EUCLIDEAN);
//		fullGroup.setDirichletDomainOrbit(200);
//		FiniteStateAutomaton fsa = new FiniteStateAutomaton(System.getenv("HOME")+"/tarfiles/methaneHydrate.wa");
//		fullGroup.setFsa(fsa);
//		FiniteStateAutomaton.generateFiniteStateAutomatonForGroup(fullGroup);
		fullGroup.setConstraint(new DiscreteGroupSimpleConstraint(500)); //setMaxNumberElements(500);
		return fullGroup;
	}

	public static DiscreteGroup getFullGroup() {
		if (true) return getFullGroup2();
//		if (fullGroup != null) return fullGroup;
		DiscreteGroupElement[] gens = new DiscreteGroupElement[8];
		for (int i = 0; i < 6; ++i)	{
			double[] mat = P3.makeReflectionMatrix(null, planes[i], Pn.EUCLIDEAN);
			gens[i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
			gens[i].setWord(DiscreteGroupUtility.genNames[i]);
		}
		double[] mat = P3.makeRotationMatrix(null, points[0], points[1], Math.PI, Pn.EUCLIDEAN);
		gens[6] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
		gens[6].setWord(DiscreteGroupUtility.genNames[6]);
		mat = P3.makeRotationMatrix(null, vs[0], vs[1], 2*Math.PI/3.0,Pn.EUCLIDEAN);
		gens[7] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
		gens[7].setWord(DiscreteGroupUtility.genNames[7]);
//		for (int i = 0; i<4; ++i)	{
//			gens[i+4] = (DiscreteGroupElement) gens[i].getInverse();
//		}
		DiscreteGroup fullGroup = new DiscreteGroup();
		fullGroup.setGenerators(gens);
		fullGroup.setDimension(3);
		fullGroup.setMetric(Pn.EUCLIDEAN);
//		fullGroup.setDirichletDomainOrbit(200);
//		FiniteStateAutomaton fsa = new FiniteStateAutomaton(System.getenv("HOME")+"/tarfiles/methaneHydrate.wa");
//		fullGroup.setFsa(fsa);
//		FiniteStateAutomaton.generateFiniteStateAutomatonForGroup(fullGroup);
		fullGroup.setConstraint(new DiscreteGroupSimpleConstraint(500)); //setMaxNumberElements(500);
		return fullGroup;
	}

	public static DiscreteGroup getQuotientGroup() {
//		if (quotientGroup != null) return quotientGroup;
		DiscreteGroupElement[] generators = new DiscreteGroupElement[7], ellist = null;
		double[][] ms = new double[7][0];
		ms[0] = P3.makeRotationMatrix(null, points[0], points[1], Math.PI, Pn.EUCLIDEAN);
		ms[1] =  P3.makeRotationMatrix(null, vs[0], vs[1], 2*Math.PI/3.0,Pn.EUCLIDEAN);
		ms[2] =  P3.makeRotationMatrix(null, vs[0], vs[1], 4*Math.PI/3.0,Pn.EUCLIDEAN);
		for (int i = 0; i<3; ++i)
			ms[i+3] = P3.makeReflectionMatrix(null, planes[2*i], Pn.EUCLIDEAN);
		generators[0] = new DiscreteGroupElement();
		generators[0].setWord("");
		for (int i = 0; i<6; ++i)	
			generators[i+1] = new DiscreteGroupElement(Pn.EUCLIDEAN, ms[i], DiscreteGroupUtility.genNames[i]);
		DiscreteGroupElement[] r2 = new DiscreteGroupElement[2];
		r2[0] = generators[0]; r2[1] = generators[1];
		DiscreteGroupElement[] r3 = new DiscreteGroupElement[3];
		r3[0] = generators[0]; r3[1] = generators[2];  r3[2] = generators[3];
		ellist = DiscreteGroupElement.cartesianProduct(null, r2, r3);
		DiscreteGroupElement[] m2 = new DiscreteGroupElement[2];
		m2[0] = generators[0];  m2[1] = generators[4];
		ellist = DiscreteGroupElement.cartesianProduct(null, m2, ellist);
		m2[1] = generators[5];
		ellist = DiscreteGroupElement.cartesianProduct(null, m2, ellist);
		m2[1] = generators[6];
		ellist = DiscreteGroupElement.cartesianProduct(null, m2, ellist);
		System.err.println("small group has "+ellist.length+" elements.");
		DiscreteGroup quotientGroup = new DiscreteGroup();
		quotientGroup.setElementList(ellist);
		quotientGroup.setDimension(3);
		quotientGroup.setMetric(Pn.EUCLIDEAN);
		return quotientGroup;
	}

	public static DiscreteGroup getTranslationGroup() {
//		if (translationGroup != null) return translationGroup;
		double[][] tlates = {{4,0,0},{0,4,0},{0,0,4}};
		DiscreteGroup tgp = new DiscreteGroup();
		DiscreteGroupElement[] gens = new DiscreteGroupElement[6];
		for (int i = 0; i<3; ++i)	{
			double[] mat = P3.makeTranslationMatrix(null, tlates[i], Pn.EUCLIDEAN);
			gens[2*i] = new DiscreteGroupElement(Pn.EUCLIDEAN, mat);
			gens[2*i].setWord(DiscreteGroupUtility.genNames[i]);
			gens[2*i+1] = (DiscreteGroupElement) gens[2*i].getInverse();
		}
		tgp.setGenerators(gens);
		tgp.setDimension(3);
		tgp.setMetric(Pn.EUCLIDEAN);
		tgp.setConstraint(new DiscreteGroupSimpleConstraint(500)); //setMaxNumberElements(500);
		tgp.setFsa(new FiniteStateAutomaton("3DP.wa"));
		return tgp;
	}

	static double hrad = .25, crad = .7, orad = .6;
	public static Color methaneBondColor = new Color(150,250,150),
	pentDDBondColor = Color.yellow,
	side14BondColor = new Color(255, 180, 0), //Color.blue,
	carbonColor = new Color(50,50,50),
	hydrogenColor = Color.white, 
	oxygenColor = Color.red;
	static float scale = .5f;
	public static Color[] carbonColors = {
		new Color(scale*1f,scale*.2f,scale*.2f), 
		new Color(scale*.3f,scale*1f,scale*.5f), 
		new Color(scale*.3f,scale*.5f,scale*1f), 
		new Color(40, 40, 40)};
	static Color hcolor = hydrogenColor, //new Color(200,200, 0), 
		ccolor = carbonColor, //new Color(255, 50, 50), 
		ocolor = oxygenColor; //new Color(255, 255, 255);
	static Color ocolor1 = new Color(255,10,10),
		ocolor2 = new Color(10,255,10),
		ocolor3 = new Color( 10,10, 255),
		edgeColor1 = new Color(255,255,10),
		edgeColor2 = new Color(255,10,255),
		edgeColor3 = new Color(10,255,255),
		faceColor1 = new Color(.5f, 0, .8f,1f),
		faceColor2 = new Color(.7f, .7f, 0, 1f),
		faceColor3 = new Color(.1f, .1f, .8f, 1f);
	static double[] radii = {hrad, hrad, hrad, hrad, crad};
	public static IndexedFaceSetFactory getVoronoiGeometry(double y, double z, double a) {
		return getVoronoiGeometry(null, y, z, a);
	}
	public static IndexedFaceSetFactory getVoronoiGeometry(IndexedFaceSetFactory ifsf, double y, double z, double a) {
		double[][] verts = {
				{0,1+y,0}, {0,1+y,-z+1}, {1-a,1-a,1-a},{1,1,1}, 
				{1+y,1-z,0}, {1+y,0,0}, {2,0,0}, {2,1,0}, 
				{(3+y)/2, (2-z)/2, 0}, 
				{(2-a+y)/2,(2-a-z)/2,(1-a)/2}};
		int[] vind = {1,1,1,1,1,0,0,1,1,1};
		Color[] vcolor = {hcolor, ocolor1,ocolor2, hcolor, ocolor1, hcolor, hcolor, ocolor3, hcolor, hcolor};
		double[] radii = {hrad, orad, orad, hrad, orad, hrad, hrad, orad, hrad, hrad};
		int[][] faceI =  {{0, 2, 4}, {4, 2, 5}, {3,2,4,7}}; //2,4,6}, {6,4,7}, {5,4,3,1}}; //{0,1,3,2}, 
		Color[] fcolor = {faceColor2, faceColor2, faceColor1}; //new Color(0,.7f, .4f),
		int[][] edgeI = {{4,7},{4,5}, {2,4}, {2,3}}; //6,9},{6,7},{4,6}, {4,5}};
		Color[] ecolor = { edgeColor2, ocolor1, edgeColor1, ocolor2};
		if (ifsf == null)  ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setVertexColors(vcolor);
		ifsf.setVertexAttribute(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
		ifsf.setFaceCount(faceI.length);
		ifsf.setFaceIndices(faceI);
		ifsf.setFaceColors(fcolor);
		ifsf.setEdgeCount(edgeI.length);
		ifsf.setEdgeIndices(edgeI);
		ifsf.setEdgeColors(ecolor);
		ifsf.setGenerateEdgesFromFaces(false);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		IndexedFaceSet vor = ifsf.getIndexedFaceSet();
		vor.setVertexAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.createReadOnly(vind));
		return ifsf;
	}
	static int[][] pentaDodecFaces = null;
	public static int[][] getPentaDodecFaces()	{
		double y, z, a;
		if (pentaDodecFaces == null)	{
			a = .1882;
			y = (1-a) * (1+phi) - 1;
			z = 1 - (1-a)*phi;
			System.err.println("a,y,z:"+a+" "+y+" "+z);
			IndexedFaceSetFactory dodecFactory = getPentaDodecGeometry(null, y, z, a);
			dodecFactory.update();
			IndexedFaceSet oneDodec = dodecFactory.getIndexedFaceSet();
			DiscreteGroup g3s2 = (PointGroup3S2) TriangleGroup.instanceOfGroup("3*2");
			g3s2.setConstraint(new DiscreteGroupSimpleConstraint(24)); //setMaxNumberElements(24);
			g3s2.update();
			WingedEdge we = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(DiscreteGroupUtility.actOnIndexedFaceSet(g3s2, oneDodec));
			pentaDodecFaces = we.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		}
		return pentaDodecFaces;
	}
	public static double[][] getPentaDodecverts(double y, double z, double a)	{
		return new double[][] {
				{1+y,1-z,0},
				{1-a,1-a,1-a},
				{1-a,1-a,a-1},
				{1-z,0,1+y},
				{1-z,0,-1-y},
				{1+y, -1+z, 0},
				{1-a,-1+a,1-a},
				{1-a,-1+a,-1+a},
				{0, 1+y, 1-z},
				{0, 1+y, z-1},
				{0, -1-y, -1+z},
				{0,-1-y, 1-z},
				{-1+z,0,1+y},
				{-1+a,1-a, 1-a},
				{-1+a,-1+a,1-a},
				{-1+z,0,-1-y},
				{-1+a,1-a, -1+a},
				{-1+a,-1+a, -1+a},
				{-1-y,-1+z,0},
				{-1-y, 1-z, 0}};
		
	}
	
	public static double[][] getTetradekverts(double y, double z, double a)	{
		return new double[][] {
				{1+y,1-z,0},
				{1-a,1-a,1-a},
				{1-a,1-a,a-1},
				{1+a,1+a,1+a},
				{1+a,1+a,-1-a},
				{2,1,0},
				{2,1+z,1-y},
				{2,1+z,y-1},
				{0,1+y,1-z},
				{0,1+y,z-1},
				{2,3-z,y-1},  // 10
				{2,3-z,1-y},
				{1-y,2,1+z},
				{0,2,1},
				{1+a,3-a,1+a},
				{1-y,2,-1-z},
				{0,2,-1},
				{1+a,3-a,-1-a},
				{1+y,3+z,0},
				{0,3-y,1-z},
				{0,3-y,z-1},
				{1-a,3+a,1-a},
				{1-a,3+a,a-1},
				{2,3,0}};
	}
	
	public static double phi = .5*(Math.sqrt(5.0)-1);
	public static IndexedFaceSetFactory getPentaDodecGeometry(IndexedFaceSetFactory ifsf, double y, double z, double a) {
		double[][] verts = {
				{0,1+y,0}, {0,1+y,-z+1}, {1-a,1-a,1-a},{1,1,1}, 
				{1+y,1-z,0}, {1+y,0,0}, {2,0,0}, {2,1,0}, 
				{(3+y)/2, (2-z)/2, 0}, 
				{(2-a+y)/2,(2-a-z)/2,(1-a)/2}};
		int[] vind = {1,1,1,1,1,0,0,1,1,1};
		Color[] vc = {hcolor, ocolor,ocolor, hcolor, ocolor, hcolor, hcolor, ocolor, hcolor, hcolor};
		double[] radii = {hrad, orad, orad, hrad, orad, hrad, hrad, orad, hrad, hrad};
		int[][] faceI =  {{2,0, 4}, {2,4, 5}}; //2,4,6}, {6,4,7}, {5,4,3,1}}; //{0,1,3,2}, 
		Color[] fc = {new Color(.7f, .7f, 0, 1f),new Color(.7f, .7f, 0, 1f)}; //new Color(0,.7f, .4f),
		if (ifsf == null) ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setVertexColors(vc);
		ifsf.setVertexAttribute(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
		ifsf.setFaceCount(faceI.length);
		ifsf.setFaceIndices(faceI);
		ifsf.setFaceColors(fc);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		IndexedFaceSet vor = ifsf.getIndexedFaceSet();
		vor.setVertexAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.createReadOnly(vind));
		return ifsf;
	}
	
	static double[][] methv = {{1,1,1},{1,-1,-1},{-1,1,-1},{-1,-1,1},{0,0,0}};
	static int[][] methEdges = {{4,0},{4,1},{4,2},{4,3}};
	static Color[] methvcolors = {hcolor, hcolor, hcolor, hcolor, ccolor};

	public static SceneGraphComponent getMethaneMolecule(double scale, Color cc)	{
		SceneGraphComponent sgc = new SceneGraphComponent("Methane");
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		double[][] verts = Rn.times(null, scale, methv);
		ilsf.setVertexCount(verts.length);
		ilsf.setVertexCoordinates(verts);
		ilsf.setVertexAttribute(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
		if (cc != null) methvcolors[4] = cc;
		ilsf.setVertexColors(methvcolors);
		ilsf.setEdgeCount(methEdges.length);
		ilsf.setEdgeIndices(methEdges);
		ilsf.update();
//	    ilsf.getGeometry().setGeometryAttributes(CommonAttributes.PICKABLE, new Boolean(false));
		sgc.setGeometry(ilsf.getGeometry());
		return sgc;
	}

	static double[][] waterv = {{1,1,1}, {-1,-1,1},{0,0,0}};
	static int[][] waterEdges = {{2,0},{2,1}};
	static Color[] watervcolors = {hcolor, hcolor, hcolor, hcolor, ccolor};
	public static SceneGraphComponent getWaterMolecule(double scale, Color cc)	{
		SceneGraphComponent sgc = new SceneGraphComponent("Water");
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		double[][] verts = Rn.times(null, scale, waterv);
		ilsf.setVertexCount(verts.length);
		ilsf.setVertexCoordinates(verts);
		ilsf.setVertexAttribute(Attribute.RELATIVE_RADII, StorageModel.DOUBLE_ARRAY.createReadOnly(radii));
		if (cc != null) watervcolors[2] = cc;
		ilsf.setVertexColors(watervcolors);
		ilsf.setEdgeCount(methEdges.length);
		ilsf.setEdgeIndices(methEdges);
		ilsf.update();
//	    ilsf.getGeometry().setGeometryAttributes(CommonAttributes.PICKABLE, new Boolean(false));
		sgc.setGeometry(ilsf.getGeometry());
		return sgc;
	}
	

}
