package discreteGroup.maniview;

import java.util.logging.Level;

import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.util.LoggingSystem;

public class OverflowCode {

	private void checkmetric(SceneGraphComponent sceneRoot) {
		final SceneGraphComponent root = sceneRoot;
		SceneGraphVisitor checker = new SceneGraphVisitor()	{
			int metric = (Integer) root.getAppearance().getAttribute("metric");
			SceneGraphComponent cs;
			@Override
			public void visit(Appearance a) {super.visit(a);}

			@Override
			public void visit(SceneGraphComponent c) {
				if (c.getName().endsWith("DG Parent"))return;
				cs = c;
				c.childrenAccept(this);
			}

			@Override
			public void visit(Transformation t) {
				if (!isValidMatrix(t.getMatrix(), 10E-4, metric))
					System.err.println("invalid matrix in "+cs.getName()+"\n"+Rn.matrixToString(t.getMatrix()));
				else System.err.println("Matrix ok "+cs.getName());
			}
			
		};
		checker.visit(sceneRoot);
	}

	 public static boolean  isValidMatrix(double[] m, double tolerance, int metric)		{
			double[] diagnosis = Rn.subtract(null, P3.Q_LIST[metric+1], 
					Rn.times(null, Rn.transpose(null, m), Rn.times(null, P3.Q_LIST[metric+1], m )));
//			if (Rn.maxNorm(diagnosis) < tolerance)		{
//				return null;
//			}
			boolean mydebug = false;
			if (mydebug)	{
				LoggingSystem.getLogger(P3.class).log(Level.FINER,"m =");
				LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.matrixToString(m));
				LoggingSystem.getLogger(P3.class).log(Level.FINER,"Original is");
				LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.matrixToString(diagnosis));			
			}
			double[][] basis = new double[4][4];
			double[] Q = P3.Q_LIST[metric+1];
			// the columns of m are the basis vectors (image of canonical basis under the isometry)
			for (int i = 0; i<4; ++i)	 for (int j = 0; j<4; ++j)	basis[i][j] = m[j*4+i];
			// first orthogonalize
			for (int i = 0; i<3; ++i)		
				for (int j = i+1; j<4; ++j)	{
					if (Q[5*j] == 0.0) continue;
					if (Math.abs(diagnosis[4*i+j]) > tolerance)	{
						return false;
					}
				}
			return true;
	 }


}
