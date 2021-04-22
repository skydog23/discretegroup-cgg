package discreteGroup.test;

import java.io.File;

import junit.framework.TestCase;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;
import discreteGroup.io.ImportExport;


public class TestArchive extends TestCase {

	public void testArchive()	{
		//DiscreteGroup dg = TriangleGroup.instanceOfGroup("*235");
		DiscreteGroup dg = WallpaperGroup.instanceOfGroup("*236");
		dg.setElementList(DiscreteGroupUtility.generateElements(dg, new DiscreteGroupSimpleConstraint(-1,-1, 100)));
		ImportExport io = new ImportExport();
		File f= new File("/tmp/testArchive.xml");
		io.write(dg, f);
		DiscreteGroup dg2 = io.readDiscreteGroup(f);
	}
}
