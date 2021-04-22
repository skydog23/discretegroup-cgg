package discreteGroup.imulogo;

public class BorromeanEvolutionDescriptor {
	double[][] curve;
	double tubeRadius, curveRadius, curveLength, scaleFactor;
	Object rendermanProxy;
	public Object getRendermanProxy() {
		return rendermanProxy;
	}
	public void setRendermanProxy(Object rendermanProxy) {
		this.rendermanProxy = rendermanProxy;
	}
	public double[][] getCurve() {
		return curve;
	}
	public void setCurve(double[][] curve) {
		this.curve = curve;
	}
	public double getCurveLength() {
		return curveLength;
	}
	public void setCurveLength(double curveLength) {
		this.curveLength = curveLength;
	}
	public double getCurveRadius() {
		return curveRadius;
	}
	public void setCurveRadius(double curveRadius) {
		this.curveRadius = curveRadius;
	}
	public double getScaleFactor() {
		return scaleFactor;
	}
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
	public double getTubeRadius() {
		return tubeRadius;
	}
	public void setTubeRadius(double tubeRadius) {
		this.tubeRadius = tubeRadius;
	}
}
