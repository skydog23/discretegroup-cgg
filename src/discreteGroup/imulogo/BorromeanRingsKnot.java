package discreteGroup.imulogo;

import java.awt.Color;
import java.util.LinkedList;

import charlesgunn.anim.util.AnimationUtility;
import charlesgunn.math.HomotopyFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;

public class BorromeanRingsKnot{
	public HomotopyFactory htf;	
	
	public BorromeanRingsKnot(){
		htf=new HomotopyFactory();
		htf.setCurves(calculateCurves());
		htf.setTimes(new double[] {0,0.5,1});
		htf.setRadia(calculateRadia());
		htf.setCurveClosed(true);
		htf.killDoubleVertices();
		htf.setNormalizedLength();
	}
	
	private double[][] currentCurve;
	private double currentRadius;
	
	// try adjusting the parameter so that the radius grows at a constant rate
	double[] pvalues = {0, 1/3.0, 2/3.0, 1}; //
	public double[][] getCurveOld(double t){		
		if(t< pvalues[1])	{ //(2.0/3.0)) {
			t=t/pvalues[1] * .5;			
			htf.setCurrentTime(t);
			currentCurve=htf.getInterpolatedCurve();
			currentRadius= htf.getInterpolatedRadius();
					//AnimationUtility.linearInterpolation(t, 0.0, .5, minimumRadius, (phi-1)/4) //;
					//, currentCurve);
		} else if (t < pvalues[2])	{
			double foo = (t-pvalues[1])/(pvalues[2]-pvalues[1]);
			t=.5 *foo +.5;			
			htf.setCurrentTime(t);
			currentCurve=htf.getInterpolatedCurve();
			currentRadius=htf.getInterpolatedRadius();
					//AnimationUtility.linearInterpolation(t, .5, 1.0, (phi-1)/4, phi/8)//
			//, currentCurve);
		}
		else{
			t=(t-pvalues[2])/(pvalues[3]-pvalues[2]);
			double[][] curve=getCurve3(t);
			curve=killDoubleVertices(curve);			
			currentRadius=htf.normalizeRadius(calculateRadia()[curvesCount-1],curve);
			currentCurve=htf.normalizeLength(curve);
		}		
		return currentCurve;
	}
	
	public double getRadius(double t){
		return currentRadius; 
	}

	public static void setCounts(int up, int lo, int hm)	{
		upperCircleVertexCount = up;
		lowerCircleVertexCount = lo;
		howmany = hm;
		vertexCount = upperCircleVertexCount + lowerCircleVertexCount + 1;	
	}
	private final double minimumRadius = 0.038;
	private final int curvesCount=3;
	private static int upperCircleVertexCount =  100; //50; //
	private static  int lowerCircleVertexCount =  49;  // 24;	//
	private static  int vertexCount = upperCircleVertexCount + lowerCircleVertexCount + 1;
	static int howmany = 50;
	private final static double phi = (1 + Math.sqrt(5))/2.0; 
	public double[] dkeys = {0, 1/3.0, 2/3.0, 1.0};
	public double[] values = {0, (phi-1)/4, phi/8};
	double[][] theCurve;
	static double standardLength = 2 + 2 * phi;
	private double[][] getCurve(double t)	{
		double length = 0;
		double tuberadius = 0;
		if(t< pvalues[1])	{ //(2.0/3.0)) {
			t=t/pvalues[1] * .5;	
			double rad = AnimationUtility.linearInterpolation(t, 0, .5, 0, (phi-1)/4);
			theCurve = new double[4*howmany+2][3];
			insertCircle(theCurve, .5-rad, phi/2-rad, rad);
			length = 2 + 2*phi +(2*Math.PI - 8) * rad;
			tuberadius = rad;
		} else if (t < pvalues[2])	{
			double foo = (t-pvalues[1])/(pvalues[2]-pvalues[1]);
			t=.5 *foo +.5;		
			double radTube = AnimationUtility.linearInterpolation(t, .5, 1, (phi-1)/4, phi/8);
			double rad = AnimationUtility.linearInterpolation(t, .5, 1, (phi-1)/4, phi/4);
			double x = AnimationUtility.linearInterpolation(t, .5, 1, .5, phi/4);
			theCurve = new double[4*howmany+2][3];
			insertCircle(theCurve, x-rad, phi/2-rad, rad);
			length = 4*phi +(2*Math.PI - 8) * rad - 8*radTube;
			tuberadius = radTube;
		}
		else{
			t=(t-pvalues[2])/(pvalues[3]-pvalues[2]);
			theCurve =getCurve3(t);
			theCurve=killDoubleVertices(theCurve);			
			//currentRadius=htf.normalizeRadius(calculateRadia()[curvesCount-1],curve);
			//theCurve=htf.normalizeLength(curve);
			length = curve3length;
			tuberadius= 0.5;
		}		
		double factor = standardLength/length;
		Rn.times(theCurve, factor, theCurve);
		currentRadius = tuberadius *factor;
		return theCurve;

	}
	
	double maxTheta = 24.2952*Math.PI/180.0;
	public BorromeanEvolutionDescriptor getCurveDescriptor(double t){
		return getCurveDescriptor(t, 1.0);
	}
	
	public BorromeanEvolutionDescriptor getCurveDescriptor(double t, double radiusScale)	{
		double length = 0;
		double tuberadius = 0, curveRadius = 0;
		Object rendermanProxy = null;
		System.err.println("radius scale is "+radiusScale);
		if(t< pvalues[1])	{ //(2.0/3.0)) {
			t=t/pvalues[1] * .5;	
			curveRadius = AnimationUtility.linearInterpolation(t, 0, .5, 0, (phi-1)/4);
			theCurve = new double[4*howmany+2][3];
			insertCircle(theCurve, .5-curveRadius, phi/2-curveRadius, curveRadius);
			length = 2 + 2*phi +(2*Math.PI - 8) * curveRadius;
			tuberadius = curveRadius;
			rendermanProxy = roundedRectangle(.5, phi/2, curveRadius, radiusScale*tuberadius);
		} else if (t < pvalues[2])	{
			double foo = (t-pvalues[1])/(pvalues[2]-pvalues[1]);
			t=.5 *foo +.5;		
			double radTube = AnimationUtility.linearInterpolation(t, .5, 1, (phi-1)/4, phi/8);
			curveRadius= AnimationUtility.linearInterpolation(t, .5, 1, (phi-1)/4, phi/4);
			double x = AnimationUtility.linearInterpolation(t, .5, 1, .5, phi/4);
			theCurve = new double[4*howmany+2][3];
			insertCircle(theCurve, x-curveRadius, phi/2-curveRadius, curveRadius);
			length = 4*phi +(2*Math.PI - 8) * curveRadius - 8*radTube;
			tuberadius = radTube;
			rendermanProxy = roundedRectangle(x, phi/2, curveRadius, radiusScale*tuberadius);
		}
		else{
			t=(t-pvalues[2])/(pvalues[3]-pvalues[2]);
			theCurve =getCurve3(t);
			theCurve=killDoubleVertices(theCurve);			
			//currentRadius=htf.normalizeRadius(calculateRadia()[curvesCount-1],curve);
			//theCurve=htf.normalizeLength(curve);
			length = curve3length;
			tuberadius= 0.5;
			rendermanProxy = peanut(t * maxTheta);
		}		
		double factor = standardLength/length;
		Rn.times(theCurve, factor, theCurve);
		currentRadius = radiusScale*tuberadius *factor;
		BorromeanEvolutionDescriptor bed = new BorromeanEvolutionDescriptor();
		bed.setTubeRadius(radiusScale*tuberadius);
		bed.setCurveRadius(curveRadius);
		bed.setCurveLength(length);
		bed.setScaleFactor(factor);
		bed.setCurve(theCurve);
		bed.setRendermanProxy(rendermanProxy);
		return bed;
		
	}
	private void insertCircle(double[][] c, double x, double y, double rad) {
		c[0][0] = c[c.length-1][0] = x+rad;
		double dangle = Math.PI/(2*(howmany-1));
		for (int i = 0; i<howmany; ++i)	{
			double angle = (  i *dangle);
			double cc = rad * Math.cos(angle);
			double ss = rad * Math.sin(angle);
			c[i+1][0] = x + cc;
			c[i+1][1] = y + ss;
			c[i+1+howmany][0] = -x - ss;
			c[i+1+howmany][1] = y + cc;
			c[i+1+2*howmany][0] = -x - cc;
			c[i+1+2*howmany][1] = -y - ss;
			c[i+1+3*howmany][0] = x + ss;
			c[i+1+3*howmany][1] = -y - cc;
		}
	}

	
	private double[][][] calculateCurves() {			
		double[][][] curves = new double[curvesCount][vertexCount][3];			
		//first curve
		double radius = minimumRadius;
		for (int i=0; i<lowerCircleVertexCount; i++)
			curves[0][i] = new double[]{1/2, i*(phi/2-radius)/lowerCircleVertexCount, 0.0};
		
		double[][] upperCircle = getCircle(radius, new double[]{1/2-radius, phi/2-radius, 0.0}, 0.0, Math.PI/2, upperCircleVertexCount);
		for (int i=0; i<upperCircleVertexCount; i++)
			curves[0][i+lowerCircleVertexCount] = (double[])upperCircle[i].clone();		
		curves[0][vertexCount-1] = new double[]{0.0, phi/2, 0.0};		
		//second curve
		radius = (phi-1)/4;
		for (int i=0; i<lowerCircleVertexCount; i++)
			curves[1][i] = new double[]{1/2, i*(phi/2-radius)/lowerCircleVertexCount, 0.0};		
		upperCircle = getCircle(radius, new double[]{1/2-radius, phi/2-radius, 0.0}, 0.0, Math.PI/2, upperCircleVertexCount);
		for (int i=0; i<upperCircleVertexCount; i++)
			curves[1][i+lowerCircleVertexCount] = (double[])upperCircle[i].clone();		
		curves[1][vertexCount-1] = new double[]{0.0, phi/2, 0.0};		
		//third curve
		radius = phi/4;
		for (int i=0; i<lowerCircleVertexCount; i++)
			curves[2][i] = new double[]{phi/4, i*(phi/2-radius)/lowerCircleVertexCount, 0.0};		
		upperCircle = getCircle(radius, new double[]{0.0, phi/2-radius, 0.0}, 0.0, Math.PI/2, upperCircleVertexCount);
		for (int i=0; i<upperCircleVertexCount; i++)
			curves[2][i+lowerCircleVertexCount] = (double[])upperCircle[i].clone();
		curves[2][vertexCount-1] = upperCircle[upperCircle.length-1];
		return quadruplicate2DCurves(curves);		
	}
	
	private double curve3length = 0;
	private double[][] getCurve3(double tt){	
		double t=(1-tt)*1/2+tt*(.5+24.2952/30);
		double radius= 1.0; //phi/4;			
		double theta=(t-1/2)*30/180*Math.PI;
		theta = AnimationUtility.linearInterpolation(tt, 0, 1, 0, 24.2952) * Math.PI/180.0;
		double[][] curve=new double[vertexCount][3];
		System.err.println("tt is "+tt);
		System.err.println("Theta is "+180 * theta/Math.PI);
		double d = ((Math.cos(theta)-Math.sin(theta))*2-1)/(Math.sin(theta)+Math.cos(theta));
		// a
		double upperCircleCenterY=2*Math.sin(theta)+Math.cos(theta)*d;
		// b
		double lowerCircleCenterX=upperCircleCenterY+radius;
		curve3length = 2*Math.PI + 8 * theta + 4 * d;
		double[][] lowerCircle = getCircle(radius, new double[]{lowerCircleCenterX, 0.0, 0.0}, Math.PI, Math.PI-theta, lowerCircleVertexCount);
		for(int i=0;i<lowerCircleVertexCount;i++)
			curve[i]=(double[])lowerCircle[i].clone();
		
		double[][] upperCircle = getCircle(radius, new double[]{0.0, upperCircleCenterY, 0.0}, -theta, Math.PI/2, upperCircleVertexCount);
		for(int i=0;i<upperCircleVertexCount;i++)
			curve[i+lowerCircleVertexCount]=(double[])upperCircle[i].clone();
		curve[vertexCount-1] = upperCircle[upperCircle.length-1];
		
		return quadruplicate2DCurve(curve);		
	}
	
	private double[][] getCircle(double radius, double[] center, double alphaMin, double alphaMax, int vertexCount) {
		
		double[][] circle = new double[vertexCount][3];
		double alpha;
		
		for (int i=0; i<vertexCount; i++) {
			alpha = alphaMin + i*(alphaMax-alphaMin)/(vertexCount-1);  //closed if full circle
			circle[i][0] = radius * Math.cos(alpha) + center[0];
			circle[i][1] = radius * Math.sin(alpha) + center[1];
			circle[i][2] = 0.0;
		}
		return circle;
	}
	
	private double[][] quadruplicate2DCurve(double[][] singleCurve){			
		double[][] quadrupleCurve=new double[4*singleCurve.length][singleCurve[0].length];
		
		int signX=1;
		int signY=1;
		double direction=1;
		int k;
		for(int n=0;n<4;n++){
			switch(n){
			case 0: {signX=1;	signY=1; break;}
			case 1:	{signX=-1;	signY=1; break;}
			case 2: {signX=-1;	signY=-1; break;}
			case 3: {signX=1;	signY=-1; break;}		
			}
			direction=Math.pow(-1,n);
			for(int j=0;j<singleCurve.length;j++){	
				k=j;
				if(direction==-1) k=singleCurve.length-1-k;
				quadrupleCurve[n*singleCurve.length+j][0]=signX*singleCurve[k][0];
				quadrupleCurve[n*singleCurve.length+j][1]=signY*singleCurve[k][1];
				quadrupleCurve[n*singleCurve.length+j][2]=0;
			}					
		}		
		return quadrupleCurve;
	}
	public double[][][] quadruplicate2DCurves(double[][][] singleCurves){
		double[][][] quadrupleCurve=new double[singleCurves.length][4*singleCurves.length][singleCurves[0][0].length];
		for(int n=0;n<singleCurves.length;n++){
			quadrupleCurve[n]=quadruplicate2DCurve(singleCurves[n]);
		}
		return quadrupleCurve;		
	}
	
	
	private boolean isClosed=true;
	private double[][] killDoubleVertices(double[][] curve){
		LinkedList doubleIndicesList=new LinkedList();	
		int countIFrom=0;
		if(isClosed) countIFrom++;
		for(int i=countIFrom;i<curve.length-1;i++){
			for(int j=i+1;j<curve.length;j++){
				if(Rn.euclideanDistanceSquared(curve[i],curve[j])<1E-6){
					if(!doubleIndicesList.contains(new Integer(i))){
						doubleIndicesList.add(new Integer(i));
					}
				}									
			}			
		}		
		double[][] newCurve=new double[curve.length-doubleIndicesList.size()][curve[0].length];
		int i=0;
		int j=0;
		while(i<curve.length){
			if(!doubleIndicesList.contains(new Integer(i))){
				newCurve[j]=(double[])curve[i].clone();
				j++;					
			}		
			i++;				
		}
		return newCurve;
	}

	private double[] calculateRadia(){
		double startRadius=0.02;
		double[] radia={startRadius,(phi-1)/4,phi/8};
		return radia;
	}
	static String roundRectTemplate = 
		"TransformBegin\n" +
		"Scale %f %f %f\n"+
		"TransformBegin\n" +
		"Translate .5 0 0\n"+
		"Rotate 90 1 0 0\n"+
		"TextureCoordinates [0 %f 0 %f 1 %f  1 %f]\n"+
		"Cylinder %f -%f %f 360\n" +
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate %f %f 0\n"+
		"Torus 1 %f 0 360 90\n" +
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate 0 .809017 0\n"+
		"Rotate 90 0 0 1\n"+
		"Rotate 90 1 0 0\n"+
		"Cylinder %f -%f %f 360\n" +
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate %f %f 0\n"+
		"Rotate 90 0 0 1\n"+
		"Torus 1 %f 0 360 90\n" +
		"TransformEnd\n" +
		"TransformEnd";

	/**
	 * Construct a scene graph repn of the rounded rectangle in the x-y plane
	 * @param x
	 * @param y
	 * @param rad
	 * @param tuberad
	 * @return
	 */
	public static SceneGraphComponent roundedRectangle(double x, double y, double rad, double tuberad) {
		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.setName("Rounded rectangle proxy");
		SceneGraphComponent[] children = new SceneGraphComponent[8];
		Double tubeRad = new Double(tuberad);
		Double curveRad = new Double(rad);
		double yy = y -rad;
		double xx = x - rad;
		Double cylx = new Double(xx);
		Double cyly = new Double(yy);
		double length = 4*xx + 4*yy + 2 * Math.PI * rad ;
		double scalefactor = standardLength/length;
		MatrixBuilder.euclidean().scale(scalefactor).assignTo(sgc);
		Double[]  texc = new Double[9];
		texc[0] = new Double(-(yy)/length);
		texc[1] = new Double((yy)/length);
		texc[2] = new Double(texc[1].doubleValue()+.5*Math.PI*rad/length);
		texc[3] = new Double(texc[2].doubleValue()+2*xx/length);
		texc[4] = new Double(texc[3].doubleValue()+.5*Math.PI*rad/length);
		texc[5] = new Double(texc[4].doubleValue()+2*yy/length);
		texc[6] = new Double(texc[5].doubleValue()+.5*Math.PI*rad/length);
		texc[7] = new Double(texc[6].doubleValue()+2*xx/length);
		texc[8] = new Double(texc[7].doubleValue()+.5*Math.PI*rad/length);
		String[] texcords = new String[8];
		for (int i = 0; i<8; ++i)	{
			children[i] = new SceneGraphComponent();
			children[i].setName("rmanProxySGC"+i);
			children[i].setGeometry(new  Geometry("BorromeanRingsKnot-bogus-geometry") {});
			sgc.addChild(children[i]);
			if ((i%2) == 0) 	// cylinder
				texcords[i] = String.format("TextureCoordinates [0 %f 1 %f 0 %f  1 %f]\n", new Object[]{
					texc[i+1], texc[i+1], texc[i], texc[i]});
			else 
				texcords[i] = String.format("TextureCoordinates [0 %f 0 %f 1 %f  1 %f]\n", new Object[]{
						texc[i], texc[i+1], texc[i], texc[i+1]});
		}
		MatrixBuilder.euclidean().translate(x,0,0).rotateX(Math.PI/2).assignTo(children[0]);
		children[0].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[0]+
				String.format("Cylinder %f -%f %f 360\n", new Object[]{tubeRad, cyly, cyly}));
		MatrixBuilder.euclidean().translate(x-rad,y-rad,0).assignTo(children[1]);
		children[1].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[1]+String.format("Torus %f %f 0 360 90", new Object[]{curveRad, tubeRad}));
		MatrixBuilder.euclidean().translate(0,y,0).rotateY(Math.PI/2).rotateZ(Math.PI/2).assignTo(children[2]);
		children[2].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[2]+String.format("Cylinder %f -%f %f 360\n", new Object[]{tubeRad, cylx, cylx}));
		MatrixBuilder.euclidean().translate(-(x-rad),y-rad,0).rotateZ(Math.PI/2).assignTo(children[3]);
		children[3].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[3]+String.format("Torus %f %f 0 360 90", new Object[]{curveRad, tubeRad}));
//		MatrixBuilder.euclidean().translate(-x,0,0).rotateX(Math.PI/2).assignTo(children[4]);
		MatrixBuilder.euclidean().rotateZ(Math.PI).translate(x,0,0).rotateX(Math.PI/2).assignTo(children[4]);
		children[4].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[4]+String.format("Cylinder %f -%f %f 360\n", new Object[]{tubeRad, cyly, cyly}));
		MatrixBuilder.euclidean().translate(-(x-rad),-(y-rad),0).rotateZ(Math.PI).assignTo(children[5]);
		children[5].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[5]+String.format("Torus %f %f 0 360 90", new Object[]{curveRad, tubeRad}));
//		MatrixBuilder.euclidean().translate(0,-y,0).rotateY(Math.PI/2).assignTo(children[6]);
		MatrixBuilder.euclidean().rotateZ(Math.PI).translate(0,y,0).rotateY(Math.PI/2).rotateZ(Math.PI/2).assignTo(children[6]);
		children[6].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[6]+String.format("Cylinder %f -%f %f 360\n", new Object[]{tubeRad, cylx, cylx}));
		MatrixBuilder.euclidean().translate((x-rad),-(y-rad),0).rotateZ(3*Math.PI/2).assignTo(children[7]);
		children[7].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[7]+String.format("Torus %f %f 0 360 90", new Object[]{curveRad, tubeRad}));
		if (xx == 0) {
			sgc.removeChild(children[2]);
			sgc.removeChild(children[6]);
		}
		return sgc;
	}
	/**
	 * Construct a scene graph repn of the peanut shape in x-y plane
	 * We normalize to have tubes of radius .5 and curve arcs of radius 1 (since it's tight)
	 * @param x
	 * @param y
	 * @param rad
	 * @param tuberad
	 * @return
	 */
	public static SceneGraphComponent peanut(double theta) {
		SceneGraphComponent sgc = new SceneGraphComponent();
		sgc.setName("Peanut proxy");
		SceneGraphComponent[] children = new SceneGraphComponent[8];
		double c = Math.cos(theta);
		double s = Math.sin(theta);
		double d = (2 * (c-s)-1)/(c+s);
		double a = 2 * s + d * c;
		double b = 1 + a;
		Double dD = new Double(d/2);
		Double thetaD = new Double(2*theta*180.0/Math.PI);
		Double bigthetaD = new Double((Math.PI+2*theta)*180.0/Math.PI);
		double length = 4*d + 2*Math.PI  + 8*theta;
		double scalefactor = standardLength/length;
		MatrixBuilder.euclidean().scale(scalefactor).assignTo(sgc);
		Double[]  texc = new Double[9];
		texc[0] = new Double(-(theta)/length);
		texc[1] = new Double((theta)/length);
		texc[2] = new Double(texc[1].doubleValue()+d/length);
		texc[3] = new Double(texc[2].doubleValue()+(2*theta+Math.PI)/length);
		texc[4] = new Double(texc[3].doubleValue()+d/length);
		texc[5] = new Double(texc[4].doubleValue()+2*theta/length);
		texc[6] = new Double(texc[5].doubleValue()+d/length);
		texc[7] = new Double(texc[6].doubleValue()+(2*theta+Math.PI)/length);
		texc[8] = new Double(texc[7].doubleValue()+d/length);
		String[] texcords = new String[8];
		for (int i = 0; i<8; ++i)	{
			children[i] = new SceneGraphComponent();
			children[i].setName("rmanProxySGC"+i);
			children[i].setGeometry(new  Geometry("BorromeanRingsKnot-bogus-geometry") {});
			sgc.addChild(children[i]);
			if ((i%2) == 1) 	// cylinder
				texcords[i] = String.format("TextureCoordinates [0 %f 1 %f 0 %f  1 %f]\n", new Object[]{
					texc[i+1], texc[i+1], texc[i], texc[i]});
			else if ( (i%4) == 0)	// reversed torus
				texcords[i] = String.format("TextureCoordinates [1.5 %f 1.5 %f .5 %f  .5 %f]\n", new Object[]{
						texc[i+1], texc[i], texc[i+1], texc[i]});
			else     // large torus
				texcords[i] = String.format("TextureCoordinates [0 %f 0 %f 1 %f  1 %f]\n", new Object[]{
						texc[i], texc[i+1], texc[i], texc[i+1]});
		}
		String cylinderPart = String.format("Cylinder .5 -%f %f 360\n", new Object[]{dD,dD});
		MatrixBuilder.euclidean().translate(b,0,0).rotateZ(Math.PI-theta).assignTo(children[0]);
		children[0].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[0]+String.format("Torus 1 .5 0 360 %f", new Object[]{thetaD}));
		MatrixBuilder.euclidean().translate(b-c+(d*s)/2,s+(d*c)/2,0).rotateZ(-theta).rotateX(Math.PI/2).assignTo(children[1]);
		children[1].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[1]+cylinderPart);
		MatrixBuilder.euclidean().translate(0,a,0).rotateZ((-theta)).assignTo(children[2]);
		children[2].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[2]+String.format("Torus 1 .5 0 360 %f", new Object[]{bigthetaD}));
		MatrixBuilder.euclidean().translate(-(b-c+(d*s)/2),s+(d*c)/2,0).rotateZ(-Math.PI+theta).rotateX(Math.PI/2).assignTo(children[3]);
		children[3].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[3]+cylinderPart);
		MatrixBuilder.euclidean().rotateZ(Math.PI).translate(b,0,0).rotateZ(Math.PI-theta).assignTo(children[4]);
		children[4].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[4]+String.format("Torus 1 .5 0 360 %f", new Object[]{thetaD}));
		MatrixBuilder.euclidean().rotateZ(Math.PI).translate(b-c+(d*s)/2,s+(d*c)/2,0).rotateZ(-theta).rotateX(Math.PI/2).assignTo(children[5]);
		children[5].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[5]+cylinderPart);
		MatrixBuilder.euclidean().rotateZ(Math.PI).translate(0,a,0).rotateZ(-theta).assignTo(children[6]);
		children[6].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[6]+String.format("Torus 1 .5 0 360 %f", new Object[]{bigthetaD}));
		MatrixBuilder.euclidean().rotateZ(Math.PI).translate(-(b-c+(d*s)/2),s+(d*c)/2,0).rotateZ(-Math.PI+theta).rotateX(Math.PI/2).assignTo(children[7]);
		children[7].getGeometry().setGeometryAttributes(CommonAttributes.RMAN_PROXY_COMMAND,
				texcords[7]+cylinderPart);
		if (Math.abs(d) < 10E-6) {
			sgc.removeChild(children[1]);
			sgc.removeChild(children[3]);
			sgc.removeChild(children[5]);
			sgc.removeChild(children[7]);
		}
		return sgc;
	}
	static String stadiumCurveRib = 
		"TransformBegin\n" +
		"Rotate 90 1 0 0\n"+
		"Scale %f %f %f\n"+
		"AttributeBegin\n"+
//		"Color %f %f %f\n"+
		"TransformBegin\n" +
		"Translate 1 0 0\n"+
		"Cylinder .25 -%f %f 360\n" +
		"Disk -%f .25 360\n"+
		"Disk %f .25 360\n"+		
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate -1 0 0\n"+
		"Cylinder .25 -%f %f 360\n" +
		"Disk -%f .25 360\n"+
		"Disk %f .25 360\n"+		
		"TransformEnd\n" +
		"AttributeEnd\n"+
//		"AttributeBegin\n"+
//		"Color %f %f %f\n"+
		"TransformBegin\n" +
		"Rotate 90 1 0 0\n"+
		"TransformBegin\n" +
		"Translate 0 1 0\n"+
		"Torus 1 .25 0 360 180\n" +
		"TransformBegin\n"+
		"Rotate 90 1 0 0\n"+
		"Translate 1 0 0\n"+
		"Disk 0 .25 360\n"+
		"TransformEnd\n"+
		"TransformBegin\n"+
		"Rotate 90 1 0 0\n"+
		"Translate -1 0 0\n"+
		"Disk 0 .25 360\n"+
		"TransformEnd\n"+
		"TransformEnd\n" +
		"TransformBegin\n" +
		"Translate 0 -1 0\n"+
		"Rotate 180 0 0 1\n"+
		"Torus 1 .25 0 360 180\n" +
		"TransformBegin\n"+
		"Rotate 90 1 0 0\n"+
		"Translate 1 0 0\n"+
		"Disk 0 .25 360\n"+
		"TransformEnd\n"+
		"TransformBegin\n"+
		"Rotate 90 1 0 0\n"+
		"Translate -1 0 0\n"+
		"Disk 0 .25 360\n"+
		"TransformEnd\n"+
		"TransformEnd\n"+
//		"AttibuteEnd\n"+
		"TransformEnd\n"+
		"TransformEnd";

	public static String coloredStadiumCurve(double scale, double cylR, double torR, Color straight)	{
		Double scaleD = new Double(scale);
		return String.format(stadiumCurveRib, new Object[]{
				scaleD, scaleD, scaleD,
				new Double(straight.getRed()/255.0),
				new Double(straight.getGreen()/255.0),
				new Double(straight.getBlue()/255.0),
				new Double(cylR*.25),
				new Double(cylR*.25),
				new Double(torR*.25),
				new Double(torR*.25)//,
/*				new Double(round.getRed()),
				new Double(round.getGreen()),
				new Double(round.getBlue())
*/		});
	}
	public static String stadiumCurve(double scale, double cylR, double torR)	{
		Double scaleD = new Double(scale);
		Double len = new Double(cylR);
		return String.format(stadiumCurveRib, new Object[]{
				scaleD, scaleD, scaleD, len, len, len, len, len, len, len, len
/*				new Double(round.getRed()),
				new Double(round.getGreen()),
				new Double(round.getBlue())
*/		});
	}
	final static String tightLinks = 
	"TransformBegin\n"+
	"Scale .7071 .7071 .7071\n"+
	"Scale .76528 .76528 .76528\n"+
	"TransformBegin\n"+
	"Translate 0 .822876 0\n"+
	"Rotate -24.2952 0 0 1\n"+
	"TextureCoordinates [0 .043825 0 .456174 1 .043825  1 .456174]\n"+
	"Torus [1 %f 0 360 228.5904] \n"+
	"TransformEnd\n"+
	"TransformBegin\n"+
	"Translate 0 -.822876 0\n"+
	"Rotate 180 0 0 1\n"+
	"Rotate -24.2952 0 0 1\n"+
	"TextureCoordinates [0 .543826 0 .956174 1 .543826 1 .956174]\n"+
	"Torus [1 %f 0 360 228.5904]  \n"+
	"TransformEnd\n"+
	"TransformBegin\n"+
	"Translate -1.822876 0 0\n"+
	"Rotate -24.2952 0 0 1\n"+
	"TextureCoordinates [ 1.5 .543826  1.5 .456174 .5 .543826 .5 .456174 ]\n"+
	"Torus 1 %f 0 360 48.5904\n"+
	"TransformEnd\n"+
	"TransformBegin\n"+
	"Translate 1.822876 0 0\n"+
	"Rotate 180 0 0 1\n"+
	"Rotate -24.2952 0 0 1\n"+
	"TextureCoordinates [  1.5  .043825  1.5 -.043825   .5 .043825  .5 -.043825]\n"+
	"Torus 1 %f 0 360 48.5904\n"+
	"TransformEnd\n"+
	"TransformEnd";

	public static String tightConfiguration(double rad)	{
		Double radD = new Double(.5*rad);
		return String.format(tightLinks, new Object[]{
				radD, radD, radD, radD
/*				new Double(round.getRed()),
				new Double(round.getGreen()),
				new Double(round.getBlue())
*/		});
		
	}

}
