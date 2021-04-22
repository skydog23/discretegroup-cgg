package discreteGroup.wallpaper;

import java.awt.geom.GeneralPath;
import java.io.Serializable;

import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

public class BilliardTable implements Serializable {
	double[][] lines;
	double[][] reflectionMatrices , matrices;
	double initialPoint[];
	GeneralPath ingr0;
	private double[][] corners;
	PositionProvider pfac;
	
	public BilliardTable(PositionProvider pf, double[][] polygon)	{
		this(pf, polygon, null);
	}
	public BilliardTable(PositionProvider pf, double[][] polygon, double[][] mm)	{
		pfac = pf;
		setTable(polygon);
		matrices = mm;
	}
	
	public void updateLines(){
	    // first update the corner array
		if (corners == null) throw new IllegalStateException("No table");
	    int n = corners.length;
	    lines = new double[n][3];
		int i = 0;
		reflectionMatrices = new double[n][];
	    for (i = 0; i<n; ++i)	{
	        Rn.crossProduct(lines[i], corners[i], corners[(i+1)%n]);
	        reflectionMatrices[i] = reflectionInLine(lines[i]);
	    }
	}
	public void update(){
	    int n = lines.length;
	    boolean foundSegment = false;
	    double[] pt0 = pfac.getPosition().clone();
	    pfac.update();
	    double[] pt1 = pfac.getPosition().clone(); //{x,y,1};
	    int count = 0;
	    do {
	     foundSegment = false;
	     for (int i = 0; i<n; ++i){
	        double dot = Rn.innerProduct(lines[i], pt1); 
//	        System.err.println("Dot is "+dot);
	        // the moving point crossed the ith line
	        if (dot < 0) {   
	           double[] line = P2.lineFromPoints(null, pt0, pt1);
	           // see if it crosses the ith line SEGMENT   
	           double dot0 = Rn.innerProduct(corners[i], line);   
	           double dot1 = Rn.innerProduct(corners[(i+1)%n], line);
	           if (dot0 * dot1 <= 0.0)  {   
	               double[] cept = P2.pointFromLines(null, lines[i], line);  
	               Pn.dehomogenize(cept, cept);
	               double[] mat = matrices == null ? reflectionMatrices[i] : matrices[i+1];
	               mat = Rn.inverse(null, mat);
	               pfac.applyMatrix(mat);
	               pt1 = pfac.getPosition().clone();
//	               System.err.println("Crossing segment "+Rn.toString(corners[i])+" : "+Rn.toString(corners[(i+1)%n]));
//	               System.err.println("Transforming with "+Rn.matrixToString(mat));
//	               System.err.println("Transformed point is "+Rn.toString(pt1));
	               foundSegment=true;
	          }
	        }
	       }
	     count++;
	     } while (count < 10 && foundSegment);
	}
	 
	public double[] reflectionInLine(double[] line){ 
	     double[] matrix = new double[9]; 
	     double[] direction = new double[3]; 
	     direction[0] = line[0]; 
	     direction[1] = line[1];   
	     direction[2] = 0.0; 
	     double f = 1.0/Rn.innerProduct(direction,direction); 
	     for (int i = 0; i<3; ++i)  {    
	         for (int j = 0; j<3; ++j) {
	              matrix[3*i+j] = (i==j? 1 : 0) - 2 * f * direction[i] * line[j];
	         }
	     }
	     return matrix;
	 
	}

	public void setTable(double[][] t) {
		corners = t;
		updateLines();
		
	}

	public PositionProvider getPositionFactory() {
		return pfac;
	}
}
