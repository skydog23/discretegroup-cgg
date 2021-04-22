/*
 * Created on May 14, 2008
 *
 */
package discreteGroup.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.thoughtworks.xstream.XStream;

import de.jreality.io.jrs.XStreamFactory;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;

public class ImportExport {

	Date timestamp = new Date();
	DiscreteGroup dg;
	
	public void write(DiscreteGroup ap, File output)	{
		dg = ap;
		// first set up
		XStream xstream = setupXStream();
		OutputStream os = null;
		try {
			os = new FileOutputStream(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		xstream.toXML(this, os );
		
	}

	public static DiscreteGroup readDiscreteGroup(InputStream is)	{
		// first set up
		XStream xstream = setupXStream();
		ImportExport foo = (ImportExport) xstream.fromXML(is);
		System.err.println("Date = "+foo.timestamp);
		return foo.dg; 
	}
	
	public static DiscreteGroup readDiscreteGroup( File input)	{

		InputStream is = null;
		try {
			is = new FileInputStream(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return readDiscreteGroup(is);
	}
	
	public static XStream setupXStream() {
		XStream xstream = XStreamFactory.forVersion(0.2); //new XStream();
//		xstream.registerConverter(new SceneGraphNodeConverter(xstream.getMapper()));
		xstream.alias("DiscreteGroupElement", DiscreteGroupElement.class);
		return xstream;
	}
	
}
