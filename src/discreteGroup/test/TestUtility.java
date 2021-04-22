package discreteGroup.test;

import org.junit.Test;

import de.jreality.scene.IndexedFaceSet;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class TestUtility {

//	@Test
//	public void test() {
//		double[] canpt = new double[4];
//		for (int i = 0; i<200; ++i)	{
//			CrystallographicGroup cg = CrystallographicGroup.instanceOfGroup(i%17);
//			cg.setConstraint(new DiscreteGroupSimpleConstraint(200));
//			cg.setCenterPoint(Rn.times(null, .1, new double[]{Math.random(), Math.random(), Math.random()}));
//			cg.update();
//			DirichletDomain dd = new DirichletDomain(cg);
//			dd.update();
//			DiscreteGroupUtility.getCanonicalRepresentative(canpt, Rn.times(null, 10.0, new double[]{Math.random(), Math.random(), Math.random()}), (WingedEdge) dd.getDirichletDomain(), cg);
//			System.err.println(cg.getName()+" test concluded");			
//		}
//	}

	@Test
	public void test2d() {
		double[] canpt = new double[4];
		for (int i = 0; i<200; ++i)	{
			WallpaperGroup cg = WallpaperGroup.instanceOfGroup(i%17);
			System.err.println(cg.getName()+" test beginning");			
			IndexedFaceSet fd = (IndexedFaceSet) cg.getDefaultFundamentalRegion();
			DiscreteGroupUtility.getCanonicalRepresentative2(canpt,new double[]{5*Math.random(), 5*Math.random(),0,1}, null, fd, cg);
		}
	}

}
