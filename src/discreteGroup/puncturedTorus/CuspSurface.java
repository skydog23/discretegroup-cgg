/*
 * Author	gunn
 * Created on Mar 15, 2006
 *
 */
package discreteGroup.puncturedTorus;

import charlesgunn.jreality.viewer.LoadableScene;
import charlesgunn.math.Complex;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class CuspSurface extends LoadableScene {

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent("theWorld");
		int num = 50;
		double[][][] verts = new double[num][][];
		double[][][] colors = new double[num][][];
		Complex c1 = new Complex(-2,.01);
		Complex c2 = new Complex(0, 2);
		for (int i = 0; i<num; ++i)	{
			double delta = (i)/(num-1.0);
			Complex  trace = Complex.linearCombination(null, (1-delta), c1, delta, c2);
			Complex[] cusps = PuncturedTorusUtility.cusps(8, trace);
			int m = cusps.length;
			verts[i] = new double[m][3];
			colors[i] = new double[m][3];
			for (int j = 0; j<m; ++j)	{
				verts[i][j][0] = cusps[j].re;
				verts[i][j][1] = cusps[j].im;
				verts[i][j][2] = 3*i/(num-1.0);
				colors[i][j][0] = delta;
				colors[i][j][1] = (j)/(m-1.0);
				colors[i][j][2] = .5;
			}
		}
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setULineCount(verts[0].length);
		qmf.setVLineCount(verts.length);
		qmf.setVertexCoordinates(verts);
		qmf.setVertexColors(colors);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		qmf.update();
		root.setGeometry(qmf.getIndexedFaceSet());
		return root;
	}

	public boolean isEncompass() {
		return true;
	}

	

}
