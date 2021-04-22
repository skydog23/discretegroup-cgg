package discreteGroup.methaneHydrate;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.groups.PointGroup3S2;
import de.jtem.discretegroup.groups.TriangleGroup;

public class DodecCluster {
	private  IndexedFaceSetFactory animateFactory, fixedFactory;
	private  double[][] pentaDodecVerts;
	SceneGraphComponent dodecSGC;
	MethaneHydrate methaneHydrate;
	boolean dolabels = false;
	public DodecCluster(MethaneHydrate mh)	{
		super();
		methaneHydrate = mh;
		dodecGroup = getDodecClusterGroup();
			dodecRepn = new  DiscreteGroupSceneGraphRepresentation(dodecGroup, false, "Dodec bundle" );
			dodecSGC =SceneGraphUtility.createFullSceneGraphComponent("voronoi");
			Appearance ap = dodecSGC.getAppearance();
			ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
			SceneGraphComponent collector = new SceneGraphComponent("Collector");
			collector.addChild(dodecSGC);
			
			dodecRepn.setWorldNode(collector);
			dodecRepn.update();
			tcell = new SceneGraphComponent();
			tcell.setAppearance(new Appearance());
			tcell.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
			tcell.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(1,1,1,0));
//			root.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, .00001);
			tcell.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);			
			dodec1 = new SceneGraphComponent();
			tcell.addChild(dodec1);
			dodec2 = new SceneGraphComponent();
			tcell.addChild(dodec2);
			tcell.addChild(dodecRepn.getRepresentationRoot());
	}
	public  DiscreteGroupSceneGraphRepresentation getDodecCluster()	{
		return dodecRepn;
	}
	
	public SceneGraphComponent getTranslationCell()	{
		getDodecCluster();
		dodec1.setGeometry(fixedFactory.getGeometry());
		MatrixBuilder.euclidean().translate(2,2,2).rotate(Math.PI,1,1,0).assignTo(dodec2);
		dodec2.setGeometry(fixedFactory.getGeometry());
		tcell.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);			
		
		return tcell;
	}
	
	public void setDodecClusterElements(boolean full)	{
//		DiscreteGroup dg = getDodecClusterGroup();
		if (dodecRepn == null) getDodecCluster();
		if (!full)	{
			DiscreteGroupElement[] direct = dodecGroup.getElementList();
			int[] mysix = {0,1,2,3,6,8};
			DiscreteGroupElement[] six = new DiscreteGroupElement[6];
			for (int i = 0; i<6; ++i)  six[i] = direct[mysix[i]];
			dodecGroup.setElementList(six);
		}  else {
			dodecGroup.setElementList(directElements);
		}
		dodec1.setVisible(!full);
		dodec2.setVisible(!full);
		dodecRepn.setElementList(dodecGroup.getElementList());
		dodecRepn.update();
	}
	
	private DiscreteGroup getDodecClusterGroup() {
		DiscreteGroup g3s2 = (PointGroup3S2) TriangleGroup.instanceOfGroup("3*2");
		g3s2.setConstraint(new DiscreteGroupSimpleConstraint(24)); //setMaxNumberElements(24);
		g3s2.update();
		
		directElements = new DiscreteGroupElement[12];
		DiscreteGroupElement[] all = g3s2.getElementList();
		int count = 0;
		int lim = 12;
		for (DiscreteGroupElement el : all)	{
			if (Rn.determinant(el.getArray()) > 0) directElements[count++] = el;
			if (count >= lim) break;
		}
		DiscreteGroup dodecGroup = new DiscreteGroup();
		dodecGroup.setMetric(Pn.EUCLIDEAN);
		dodecGroup.setDimension(3);
		dodecGroup.setElementList(directElements); //six);
		return dodecGroup;
	}

	private  void updateDodecCluster() {
		fixedFactory = DodecCluster.getPentagonDodec(fixedFactory, true, dolabels, y, z, a);
		animateFactory = DodecCluster.getPentagonDodec(animateFactory, false, dolabels, y, z, a);
		IndexedFaceSet geometry = null;
		for (int k = 0; k<2;++k)	{
			geometry = k == 0 ? (IndexedFaceSet) animateFactory.getGeometry() : (IndexedFaceSet) fixedFactory.getGeometry();
			if (dolabels)	{
			      int n=geometry.getNumPoints();
			      String[] labels=new String[n];
			      for (int i = 0; i<n; i++) labels[i] = ""+i;
			      geometry.setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
			      n = geometry.getNumFaces();
			      labels=new String[n];
			      for (int i = 0; i<n; i++) labels[i] = ""+i;
			      geometry.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
			      Appearance ap = k==0 ? dodecSGC.getAppearance() : tcell.getAppearance();
			      DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, false);
			      DefaultTextShader pts = (DefaultTextShader) ((DefaultPointShader)dgs.getPointShader()).getTextShader();
			      DefaultTextShader ets = (DefaultTextShader) ((DefaultLineShader)dgs.getLineShader()).getTextShader();
			      DefaultTextShader fts = (DefaultTextShader) ((DefaultPolygonShader)dgs.getPolygonShader()).getTextShader();
			      pts.setDiffuseColor(Color.blue);
			      ets.setDiffuseColor(Color.green);
			      fts.setDiffuseColor(Color.black);
			      Double scale = new Double(0.003);
			      pts.setScale(scale);
			      ets.setScale(scale);
			      fts.setScale(scale);
			      double[] offset = new double[]{.10,0,0.01};
			      pts.setOffset(offset);
			      ets.setOffset(offset);
			      fts.setOffset(offset);			
			}
			
		}
		geometry = (IndexedFaceSet) animateFactory.getIndexedFaceSet();
		dodecSGC.setGeometry(geometry);
		double[][] verts = geometry.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int[][] faces = geometry.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[] plane = P3.planeFromPoints(null, verts[faces[0][0]], verts[faces[0][1]], verts[faces[0][2]]);
		double[] dodecReflections = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
		pentaDodecVerts = Rn.matrixTimesVector(null, dodecReflections, verts);
		double[][] tmp = new double[24][3];
		if (verts[0].length == 4) {
			Pn.dehomogenize(tmp, pentaDodecVerts);
			pentaDodecVerts = tmp;
		}

	}

	public  void animateDodec(double time)	{
		System.err.println("Time is "+time);
		double[][] interpverts = new double[24][];
		double[][] tetradekverts = MethaneHydrateUtility.getTetradekverts(y,z,a);
		for (int i = 0; i<24; ++i)	{
			interpverts[i] = Rn.linearCombination(null, 1-time, pentaDodecVerts[i], time, tetradekverts[i]);
		}
		animateFactory.setVertexCoordinates(interpverts); 
		animateFactory.update();
		System.err.println("Updating polyhedron");
	}
	double y, z, a;
	private DiscreteGroup dodecGroup;
	private DiscreteGroupElement[] directElements;
	private DiscreteGroupSceneGraphRepresentation dodecRepn;
	public void updateVerts(double yy, double zz, double aa)	{
		y = yy;  z = zz;  a = aa;
		updateDodecCluster();
	}
	static Color ocolor1 = new Color(255,10,10),
	ocolor2 = new Color(10,255,10),
	ocolor3 = new Color( 10,10, 255),
	edgeColor1 = new Color(255,255,10),
	edgeColor2 = new Color(255,10,255),
	edgeColor3 = new Color(10,255,255),
	faceColor1 = new Color(.5f, 0, .8f,1f),
	faceColor2 = new Color(.7f, .7f, 0, 1f),
	faceColor3 = new Color(.1f, .1f, .8f, 0f);
	static Color[] vc = {ocolor1, ocolor2, ocolor3};
	static Color[] ec = {ocolor2, edgeColor1, edgeColor2,  ocolor1};
	static Color[] fc = {faceColor1, faceColor2, faceColor3};
	public static Color[] tetradekVertexColors = {
		vc[0], vc[1], vc[1], vc[1], vc[1],
		vc[2], vc[0], vc[0], vc[0], vc[0],
		vc[0], vc[0], vc[0], vc[2], vc[1],
		vc[0], vc[2], vc[1], vc[0], vc[0],
		vc[0], vc[1], vc[1], vc[2]
	};
	public static Color[] tetradekEdgeColors = {
		ec[0], ec[2], ec[3], ec[1], ec[1],
		ec[3], ec[0], ec[2], ec[1], ec[1],
		ec[0], ec[1], ec[1], ec[2], ec[2],
		ec[3], ec[2], ec[2], ec[1], ec[0], 
		ec[1], ec[2], ec[1], ec[2], ec[1], 
		ec[2], ec[2], ec[2], ec[1], ec[1], 
		ec[1], ec[3], ec[1], ec[2], ec[1], 
		ec[1]};
	public static Color[] tetradekFaceColors = {
		fc[1], fc[0], fc[0], fc[0], fc[0], fc[1], fc[1], fc[2], fc[2], fc[0], fc[0], fc[0], fc[1], fc[0]
	};
	public static Color[] dodecVertexColors = {
		vc[0], vc[1], vc[1], vc[0], vc[0],
		vc[0], vc[1], vc[1], vc[0], vc[0],
		vc[0], vc[0], vc[0], vc[1], vc[1],
		vc[0], vc[1], vc[1], vc[0], vc[0]
	};
	public static Color[] dodecEdgeColors = {
		ec[1], ec[3], ec[1], ec[1], ec[1],
		ec[1], ec[1], ec[1], ec[1], ec[1],
		ec[1], ec[1], ec[1], ec[1], ec[1],
		ec[3], ec[1], ec[1], ec[1], ec[1], 
		ec[1], ec[1], ec[1], ec[1], ec[3], 
		ec[1], ec[3], ec[3], ec[1], ec[3]};
	public static Color[] dodecFaceColors = {
		fc[1], fc[1], fc[1], fc[1], fc[1], fc[1], fc[1], fc[1], fc[1], fc[1], fc[1], fc[1]
	};
	private SceneGraphComponent tcell;
	private SceneGraphComponent dodec1;
	private SceneGraphComponent dodec2;
	public static IndexedFaceSetFactory getPentagonDodec(IndexedFaceSetFactory ifs, boolean fixed, boolean dolabels, double y, double z, double a)	{
			double[][] verts = null;
			int[][] faces = null;
			verts = MethaneHydrateUtility.getPentaDodecverts(y, z, a);
			faces = MethaneHydrateUtility.getPentaDodecFaces();
			double[][] nverts =  null;
			int[][] nfaces = null;
			if (fixed)	{
				nverts = verts; nfaces = faces;
			} else {
				nverts = new double[24][];
				nfaces = new int[14][];
				for (int i = 0; i<verts.length; ++i)	{
					nverts[i] = verts[i];
				}
				nverts[20] = Rn.copy(null, verts[19]);
				nverts[21] = Rn.copy(null, verts[18]);
				nverts[22] = Rn.copy(null, verts[18]);
				nverts[23] = Rn.average(null, new double[][]{verts[10], verts[11]});
				for (int i = 0; i<faces.length; ++i)	{
					nfaces[i] = new int[faces[i].length]; //faces[i];
					System.arraycopy(faces[i], 0, nfaces[i], 0, faces[i].length);
				}
				nfaces[12] = new int[]{20,19,21,18,22};
				nfaces[13] = new int[]{18,23,10,17,22};
				nfaces[9] = new int[]{14,11,23,18,21};
				nfaces[7] = new int[]{9,8,13,19,20,16};
				nfaces[8] = new int[]{11,6,5,7,10,23};
				nfaces[11] = new int[]{15,16,20,22,17};
				nfaces[10] = new int[]{13,12,14,21,19};				
			}
			boolean firsttime = false;
			if (ifs == null) {
				ifs = new IndexedFaceSetFactory();
				ifs.setGenerateEdgesFromFaces(true);
				firsttime = true;
			}
			ifs.setVertexCount(nverts.length);
			ifs.setVertexCoordinates(nverts);
			ifs.setFaceCount(nfaces.length);
			ifs.setFaceIndices(nfaces);
			ifs.setGenerateFaceNormals(true);
			ifs.setGenerateVertexLabels(dolabels);
			ifs.setGenerateEdgeLabels(dolabels);
			ifs.setGenerateFaceLabels(dolabels);
			if (firsttime)	{
				ifs.update();
				int[][] indices = ifs.getIndexedFaceSet().getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
				System.err.println("line count is "+indices.length);
				ifs.setGenerateEdgesFromFaces(false);
				ifs.setEdgeCount(indices.length);
				ifs.setEdgeIndices(indices);				
			}
			if (!fixed) {
				ifs.setVertexColors(tetradekVertexColors);
				ifs.setFaceColors(tetradekFaceColors);
				ifs.setEdgeColors(tetradekEdgeColors);
			} else {
				ifs.setVertexColors(dodecVertexColors);
				ifs.setFaceColors(dodecFaceColors);
				ifs.setEdgeColors(dodecEdgeColors);
			}
			ifs.update();
			return ifs;
		}

}
