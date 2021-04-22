package discreteGroup.test;

import java.util.Iterator;

import junit.framework.TestCase;

import org.gavrog.joss.geometry.SpaceGroupCatalogue;


public class TestGavrog extends TestCase {

	public void testGroupNames()		{
		SpaceGroupCatalogue sgc = new SpaceGroupCatalogue();
		Iterator iter = sgc.settingNames(3);
		while (iter.hasNext())	{
			String str = (String) iter.next();
			System.err.println("Group is "+str);
		}
	}
}
