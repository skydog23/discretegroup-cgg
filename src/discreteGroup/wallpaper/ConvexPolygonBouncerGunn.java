package discreteGroup.wallpaper;

import de.jreality.math.Rn;

public class ConvexPolygonBouncerGunn extends AbstractBouncer {
	
	double[][] lineEquations = null, scaledLineEquations = null;
	int epsilon = 1;
	double[] C = null;
	double[] centroidToStandardLineTform = null;
	double scale = 1.5;
//	bouncer = new ConvexPolygonBouncer(
//	new double[][]{{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}}, 
//	new 

	private static double[][] square = {{0.0, 0.0}, {1.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}};
	private static int[][] indices = {{0, 1, 2, 3}};;
	public ConvexPolygonBouncerGunn()	{
		this(square, indices);
	}
	public ConvexPolygonBouncerGunn(double[][] points, int[][] indices)	{
		psf.setVertexCount(1);
		psf.setVertexCoordinates(new double[][]{point});
		
		double length;
		
		// claculate the line equations
		// the number of lineequations and the number of points are the same
		C = calculateCenterOfGravity(points);
		centroidToStandardLineTform = new double[]{1,0,0,	0,1,0,	-C[0], -C[1], 1};
		lineEquations = new double[points.length][3];
		scaledLineEquations = new double[points.length][3];
		setupLineEquations(points, indices);
		setupScaledLineEquations();
		epsilon = calculateEpsilon(C, lineEquations);
		point[0] = C[0];
		point[1] = C[1];
		direction[0] = 1;
		direction[1] = Math.sqrt(2.0);
	}
	
	private void setupScaledLineEquations() {
		double[] scaler = Rn.diagonalMatrix(null, new double[]{1,1,scale});
		double[] totalTform = Rn.conjugateByMatrix(null, scaler, centroidToStandardLineTform);
		Rn.matrixTimesVector(scaledLineEquations, totalTform, lineEquations);
		System.err.println("unscaled line equations are \n"+Rn.toString(lineEquations));
		System.err.println("scaled line equations are \n"+Rn.toString(scaledLineEquations));
	}
	
	private void setupLineEquations(double[][] points, int[][] indices) {
		double length;
		for (int i = 0; i < lineEquations.length; ++i) {
			lineEquations[i] = homCrossProduct(points[indices[0][i]], points[indices[0][(i+1)%points.length]]);
			length = Math.sqrt(lineEquations[i][0]*lineEquations[i][0] + lineEquations[i][1]*lineEquations[i][1]);
			lineEquations[i][0] = lineEquations[i][0] / length;
			lineEquations[i][1] = lineEquations[i][1] / length;
			lineEquations[i][2] = lineEquations[i][2] / length;
		}
	}
	
	public void update()	{
		double temp[]= new double[3];
		temp[0] = point[0];
		temp[1] = point[1];
		Rn.add(temp, temp, Rn.times(null, speed, direction));
		temp[2] = 1.0;
		point[0] = temp[0];
		point[1] = temp[1];
		// detect exits and correct
		int index = crossedBorder(temp);
		if (-1 != index) {
			double a = scaledLineEquations[index][0];
			double b = scaledLineEquations[index][1];
			double c = scaledLineEquations[index][2];
			double Rl[][] = new double[2][3];
			Rl[0][0] = 1-2*a*a;
			Rl[0][1] = -2*a*b;
			Rl[0][2] = -2*a*c;
			Rl[1][0] = -2*a*b;
			Rl[1][1] = 1-2*b*b;
			Rl[1][2] = -2*b*c;
			double[] newPoint = new double[2];
			newPoint[0] = Rl[0][0]*point[0] + Rl[0][1]*point[1] + Rl[0][2];
			newPoint[1] = Rl[1][0]*point[0] + Rl[1][1]*point[1]+ Rl[1][2];
			point = newPoint;
			// reflect direction with Rl manually
			double[] newDirection = new double[2];
			newDirection[0] = Rl[0][0]*direction[0] + Rl[0][1]*direction[1];
			newDirection[1] = Rl[1][0]*direction[0] + Rl[1][1]*direction[1];
			direction = newDirection;
		}
		psf.setVertexCoordinates(point);
		psf.update();
	}
	
	// calculate the center of gravity
	protected double[] calculateCenterOfGravity(double[][] vertices) {
		double[] t =  Rn.average(null, vertices);
		return new double[]{t[0], t[1], 1};
	}
	
	// calculate the inner product of the vectors v1 and v2
	protected double dotProduct(double[] v1, double[] v2) {
		return (v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2]);
	}
	
	protected int calculateEpsilon(double[] grav, double[][] lines) {
		return (Rn.innerProduct(grav, lines[0]) < 0) ? -1 : 1;
	}
	
	// cross product
	protected double[] homCrossProduct(double[] v0, double[] v1) {
		double result[] = {v0[1] - v1[1], v1[0] - v0[0], v0[0]*v1[1] - v1[0]*v0[1]};
		return result;
	}

	// returns the index of the borderline which the point p has crossed
	// otherwise returns -1
	protected int crossedBorder(double[] p) {
		int index = -1;
		for (int i = 0; i < scaledLineEquations.length; ++i) {
			if (epsilon == 1) {
				if (dotProduct(p, scaledLineEquations[i]) < 0) {
					index = i;
					break;
				}
			} else {
				if (dotProduct(p, scaledLineEquations[i]) > 0) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	public double getScale() {
		return scale;
	}
	public void setScale(double scale) {
		this.scale = scale;
		setupScaledLineEquations();
	}
}
