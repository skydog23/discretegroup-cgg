/*
 * Author	gunn
 * Created on Feb 9, 2006
 *
 */
package discreteGroup.puncturedTorus;

import java.util.Vector;

import charlesgunn.math.CP1;
import charlesgunn.math.Cn;
import charlesgunn.math.Complex;
import charlesgunn.math.PSL2C;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Lock;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;

public class PuncturedTorusGroupFactory {
	// independent parameters

	Complex 
		Ta = new Complex(2,0), 
		Tb = new Complex(2,0),
		Tab = new Complex(2,0); 
	FiniteStateAutomaton fsa = null;
	double epsilonForDrawing = .005, epsilonForAlmostParabolic = .1;
	int depthForDrawing = 20, depthForAlmostParabolic=10;
	int maxNumberPoints = 500000;
	boolean minimalDrawAtMaximalDepth = true,
		drawFlat = false,
		multiThreaded = true,
		progressiveRefinement = true,
		lookForAlmostParabolics = false;
	int generatorFamily = TYPE_PARABOLIC_COMMUTATOR;
	
	// derived quantities 
	transient DiscreteGroup dg;
	transient PSL2C[] genspsl2cs;
	transient Complex[][] allFixedPoints, commutatorFixedPoints;
	transient PSL2C[][] commutators;
	transient PSL2C[][] almostParabolics;		// include parabolics!
	transient String[][] allFixedPointsLabels;
	transient boolean 
		debug = true, 
		allFixedPointsDirty = true,
		pleatedSurfaceDirty = true,
		generatorsChanged = true, 
		newParameters = true,
		interrupt = false,
		calculating = false;

	transient IndexedLineSet theLimitSet = new IndexedLineSet();
	transient PointSetFactory[] fixedPointFactory;
	transient PointSet[] fixedPointSets;
	transient IndexedFaceSetFactory puncturedSurfaceTileFactory = null;
	
	// types of generator constraints
	public final static int TYPE_MASKIT = 0;
	public final static int TYPE_PARABOLIC_COMMUTATOR = 1;
	public final static int TYPE_UNCONSTRAINED = 2;
	
	public final static int TYPE_GRANDMA = 1;
	public final static int TYPE_JORGENSEN = 2;
	public final static int TYPE_GUNN =3;

	// locking and threads
	transient private Lock lock = new Lock();
	transient private Thread currentThread = null;
	transient private Runnable currentRunnable = null;
	transient private final Object MUTEX=new Object();

	public PuncturedTorusGroupFactory() {
		super();
		dg = new DiscreteGroup();
		dg.setMetric(Pn.HYPERBOLIC);
		dg.setDimension(3);
		dg.setFree(true);
	}
	
	public PuncturedTorusGroupFactory copy() {
		PuncturedTorusGroupFactory copy = new PuncturedTorusGroupFactory();
		copy.setGeneratorFamily(getGeneratorFamily());
		copy.setTa(getTa());
		copy.setTb(getTb());
		copy.setDepth(getDepth());
		copy.setEpsilon(getEpsilon());
		copy.setEpsilonForAlmostParabolic(getEpsilonForAlmostParabolic());
		copy.setDepthForAlmostParabolic(getDepthForAlmostParabolic());
		copy.setMaxNumberPoints(getMaxNumberPoints());
		copy.setPleatedSurfaceMaxElements(getPleatedSurfaceMaxElements());
		copy.setMinimalDrawAtMaximalDepth(isMinimalDrawAtMaximalDepth());
		copy.setDrawFlat(isDrawFlat());
		copy.setWordAcceptor(getWordAcceptor());
		return copy;
	}
	
	public void setTa(Complex c)	{
		Complex.copy(Ta, c);
		generatorsChanged=true;
		fsa = null;
	}
	
	public Complex getTa()	{ return Ta; }

	public void setTb(Complex c)	{
		Complex.copy(Tb, c);
		generatorsChanged=true;
		fsa = null;
	}

	public Complex getTab()	{ return Tab; }

	public void setTab(Complex c)	{
		Complex.copy(Tab, c);
		generatorsChanged=true;
		fsa = null;
	}

	public Complex getTb()	{ return Tb; }

	public DiscreteGroup getDiscreteGroup() {
		return dg;
	}

	public void setWordAcceptor(FiniteStateAutomaton fsa) {
		this.fsa = fsa;
	}
	
	public FiniteStateAutomaton getWordAcceptor()	{
		return fsa;
	}
	
	public int getDepth() {
		return depthForDrawing;
	}

	public void setDepth(int depth) {
		this.depthForDrawing = depth;
		newLimitSet();
	}

	public double getEpsilon() {
		return epsilonForDrawing;
	}

	public void setEpsilon(double epsilon) {
		this.epsilonForDrawing = epsilon;
		newLimitSet();
	}
	
	public int getMaxNumberPoints() {
		return maxNumberPoints;
	}

	public void setMaxNumberPoints(int maxNumberPoints) {
		this.maxNumberPoints = maxNumberPoints;
		if (maxNumberPoints < 0) {
		} else {
		}
		newLimitSet();
	}

	public double getEpsilonForAlmostParabolic() {
		return epsilonForAlmostParabolic;
	}

	public void setEpsilonForAlmostParabolic(double e) {
		this.epsilonForAlmostParabolic = e;
		allFixedPointsDirty = true;
		newLimitSet();
	}


	public int getDepthForAlmostParabolic() {
		return depthForAlmostParabolic;
	}

	public void setDepthForAlmostParabolic(int depthForAlmostParabolic) {
		this.depthForAlmostParabolic = depthForAlmostParabolic;
		allFixedPointsDirty = true;
		newLimitSet();
	}

	public int getGeneratorFamily() {
		return generatorFamily;
	}

	public void setGeneratorFamily(int generatorFamily) {
		this.generatorFamily = generatorFamily;
	}

	public boolean isMinimalDrawAtMaximalDepth() {
		return minimalDrawAtMaximalDepth;
	}

	public void setMinimalDrawAtMaximalDepth(boolean b) {
		if (b == minimalDrawAtMaximalDepth) return;
		this.minimalDrawAtMaximalDepth = b;
		newLimitSet();
	}

	public boolean isDrawFlat() {
		return drawFlat;
	}

	public void setDrawFlat(boolean b) {
		if (b == drawFlat) return;
		drawFlat = b;
		// not quite true but the drawing will change
		allFixedPointsDirty = true;
		newLimitSet();
		}

	public IndexedLineSet getLimitSet()	{
		return theLimitSet;
	}
	
	public PointSet[] getFixedPointSets() {
		calculateAllFixedPoints();
		return fixedPointSets;
	}

	public PSL2C[][] getAlmostParabolics() {
		return almostParabolics;
	}

	private void newLimitSet() {
		if (!multiThreaded) {
			newParameters = true;
			return;
		}
		synchronized (MUTEX) {
			newParameters = true;
			MUTEX.notify();
		}
	}
	
	public void interrupt() {
		if (!multiThreaded) return;
	
		synchronized (MUTEX) {
			interrupt = true;
			MUTEX.notify();
		}
	}

	public boolean isCalculating() {
		synchronized (MUTEX) {
			return calculating;
		}
	}

	public boolean isMultiThreaded() {
		return multiThreaded;
	}

	public void setMultiThreaded(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	public boolean isProgressiveRefinement() {
		return progressiveRefinement;
	}

	public void setProgressiveRefinement(boolean progressiveRefinement) {
		this.progressiveRefinement = progressiveRefinement;
	}
		
	public boolean isLookForAlmostParabolics() {
		return lookForAlmostParabolics;
	}

	public void setLookForAlmostParabolics(boolean b) {
		if (lookForAlmostParabolics == b) return;
		this.lookForAlmostParabolics = b;
		allFixedPointsDirty = true;
	}

	public void update()		{
		if (debug) System.err.println("Updating ptgf, generators changed is "+generatorsChanged);
		if (generatorsChanged)	updateGroup();
		if (pleatedSurfaceDirty) updatePleatedSurface();
		if (newParameters) calculateLimitSet();
	}

	public void updateGroup() {
		lock.writeLock();	
		genspsl2cs = PuncturedTorusUtility.getGrandmaGenerators(Ta, Tb);
		
		DiscreteGroupElement[] gens = PuncturedTorusUtility.convertGeneratorsToSO31(genspsl2cs);
		dg.setGenerators(gens);
		
		commutators = PuncturedTorusUtility.calculateCommutators(genspsl2cs);
		commutatorFixedPoints = PSL2C.attractiveFixedPoints(null, commutators);

		allFixedPointsDirty = true;
		calculateAllFixedPoints();
		
		calculateFundamentalTile();		
		updatePleatedSurface();
		
		generatorsChanged = false;
		newLimitSet();
		lock.writeUnlock();
	}

	public int getPleatedSurfaceMaxElements() {
		return pleatedSurfaceMaxElements;
	}

	public void setPleatedSurfaceMaxElements(int pleatedSurfaceMaxElements) {
		this.pleatedSurfaceMaxElements = pleatedSurfaceMaxElements;
		pleatedSurfaceDirty = true;
	}
	
	public SceneGraphComponent getPleatedSurface()	{
		updatePleatedSurface();
		return pleatedSurface;
	}
	
	SceneGraphComponent pleatedSurfaceTile, pleatedSurface;
	DiscreteGroupSceneGraphRepresentation theMainRepn;
	private int pleatedSurfaceMaxElements = 200;
	private void updatePleatedSurface()	{
		if (!pleatedSurfaceDirty) return;
		DiscreteGroup tg = getDiscreteGroup();
		if (pleatedSurfaceTile == null)	{
			pleatedSurfaceTile = new SceneGraphComponent();
			pleatedSurfaceTile.setAppearance(new Appearance());
			pleatedSurfaceTile.getAppearance().setAttribute("vertexShader","simple");
			theMainRepn = new  DiscreteGroupSceneGraphRepresentation(tg);
			theMainRepn.setWorldNode(pleatedSurfaceTile);
			pleatedSurface = theMainRepn.getRepresentationRoot();
		}
//		tg.setMaxNumberElements(pleatedSurfaceMaxElements);
		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(pleatedSurfaceMaxElements);
		tg.setConstraint(dgsc);
		tg.update();
		pleatedSurfaceTile.setGeometry(puncturedSurfaceTileFactory.getIndexedFaceSet());
		theMainRepn.setElementList(DiscreteGroupUtility.generateElements(tg, null));
		System.err.println("Resetting pleated surface with "+theMainRepn.getElementList().length);
		theMainRepn.setWorldNode(pleatedSurfaceTile);
		theMainRepn.update();
		System.err.println("Updating pleated surface");
		pleatedSurfaceDirty = false;
	}
	/**
	 * @param fp
	 * @return
	 */
	private IndexedFaceSet calculateFundamentalTile() {
		double[][] fp = new double[4][];		
		for (int i = 0; i<4; ++i)	
			fp[i] = CP1.r2ToUnitSphere3(null, commutatorFixedPoints[i][1]);
		if (debug) System.err.println("Points are:\n"+Rn.toString(fp));
		int[][] indices =  { {0, 1, 2},{0,2, 3}};
		double[][] faceColors = {{1,0,0},{1,1,1}};
		if (puncturedSurfaceTileFactory == null) {
			puncturedSurfaceTileFactory = new IndexedFaceSetFactory();
			puncturedSurfaceTileFactory.setVertexCount(4);
			puncturedSurfaceTileFactory.setFaceCount(2);
			puncturedSurfaceTileFactory.setFaceIndices(indices);
			puncturedSurfaceTileFactory.setFaceColors(faceColors);
			puncturedSurfaceTileFactory.setGenerateEdgesFromFaces(true);
			puncturedSurfaceTileFactory.setGenerateFaceNormals(true);
		}
		puncturedSurfaceTileFactory.setVertexCoordinates(fp);
		puncturedSurfaceTileFactory.update();
		IndexedFaceSet ps = puncturedSurfaceTileFactory.getIndexedFaceSet();
		return ps;
	}

	private void calculateAllFixedPoints()	{
		if (!allFixedPointsDirty) return;
		ParabolicCollector pc = new ParabolicCollector(this, depthForAlmostParabolic, epsilonForAlmostParabolic);
		pc.visit();
		PSL2C[] sortedParabolics = pc.getParabolics();
		if (almostParabolics == null) almostParabolics = new PSL2C[4][];
		if (allFixedPointsLabels == null) allFixedPointsLabels = new String[4][];
		lock.writeLock();
		allFixedPoints = fixedPointsForParabolics(sortedParabolics);
		lock.writeUnlock();		
		if (fixedPointSets == null)	{
			fixedPointFactory = new PointSetFactory[4];
			for (int i = 0; i<4; ++i)	
				fixedPointFactory[i] = new PointSetFactory();
			fixedPointSets = new PointSet[4];
		}
		for (int i = 0; i<4; ++i)	{
			int m = allFixedPoints[i].length;
			String[] labels = new String[m];
			double[][] rs = new double[m][3];
			if (drawFlat) for (int j=0;j<m; ++j)  {
				rs[j][0]=allFixedPoints[i][j].re;
				rs[j][1]=allFixedPoints[i][j].im;
				rs[j][2]=0.0;
			}
			else CP1.r2ToUnitSphere3(rs, allFixedPoints[i]);
			if (debug) System.err.println("Creating point set with "+rs.length+" vertices");
			fixedPointFactory[i].setVertexCount(rs.length);
			fixedPointFactory[i].setVertexCoordinates(rs);
//			if (allFixedPointsLabels != null && allFixedPointsLabels[i] != null) 
//				fixedPointFactory[i].setVertexLabels(allFixedPointsLabels[i]);
			fixedPointFactory[i].update();	
			fixedPointSets[i] = fixedPointFactory[i].getPointSet();
			fixedPointSets[i].setVertexAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(allFixedPointsLabels[i]));
		}
		allFixedPointsDirty = false;
	}
	
	private Complex[][] fixedPointsForParabolics(PSL2C[] sortedParabolics) {
		Vector[] fp = new Vector[4];
		int n = sortedParabolics.length;
		Complex[][] fixedPoints = new Complex[4][];
	
		int whichList = 0;
		char currChar = genspsl2cs[0].getWord().charAt(0);
		for (int i = 0; i<4; ++i)	fp[i] = new Vector();
		for (int k = 0; k<n; ++k)	{
			String w = sortedParabolics[k].getWord();
			char lastChar = w.charAt(w.length()-1);
			if (lastChar != currChar)	{
				currChar = w.charAt(w.length()-1);
				whichList++;
				if (currChar != genspsl2cs[whichList].getWord().charAt(0) )	{
					throw new IllegalStateException("Something's wrong with list of parabolics");
				}
			}
			fp[whichList].add(sortedParabolics[k]);
		}
		for (int i = 0; i<4; ++i)	{
			int m = fp[i].size();
			if (debug) System.err.println("i: "+i+" # of fixedpoints: "+m);
			fixedPoints[i] = new Complex[m];
			allFixedPointsLabels[i] = new String[m];
			for (int j=0; j<m; ++j) {
				PSL2C tmp = ((PSL2C) fp[i].get(j));
				fixedPoints[i][j] = Cn.attractiveFixedPoint(null, tmp.m);
				allFixedPointsLabels[i][j] = tmp.getWord();
			}
			//			if (debug) System.err.println();
			almostParabolics[i] = new PSL2C[m];
			almostParabolics[i] = (PSL2C[])fp[i].toArray(almostParabolics[i]);
		}
		return fixedPoints;
	}


	private void calculateLimitSet()	{
		if (!multiThreaded)		{
			setLookForAlmostParabolics(true);
			calculateAllFixedPoints();
			TreeExplorer te = new TreeExplorer(theLimitSet, genspsl2cs, allFixedPoints, epsilonForDrawing, depthForDrawing, maxNumberPoints, drawFlat);
			te.explore();
			return;
		}
		if (currentThread == null) {
			currentRunnable = new Runnable()	{
				public void run()  {
					loop: while(true)	{
						synchronized (MUTEX) {
							calculating = newParameters;
							if (calculating) newParameters = false;
						}
						if (calculating)	 {
							System.out.println("calculating");
							double eps = epsilonForDrawing*32.0;
							setLookForAlmostParabolics(false);
							for (int i = 0; i<5; ++i)	{
								TreeExplorer te = new TreeExplorer(theLimitSet, genspsl2cs, commutatorFixedPoints, eps, depthForDrawing, maxNumberPoints, drawFlat);
								te.explore();
								eps /= 2.0;
								synchronized (MUTEX) {
									if (newParameters || interrupt) {
										System.out.println("interrupted MUTEX");
//										newParameters = false;
										interrupt = false;
										calculating = false;
										continue loop;
									}
								}
							}	
							setLookForAlmostParabolics(true);
							calculateAllFixedPoints();
							TreeExplorer te = new TreeExplorer(theLimitSet, genspsl2cs, allFixedPoints, epsilonForDrawing, depthForDrawing,maxNumberPoints, drawFlat);
							te.explore();
							calculating = false;
						} else {
							synchronized (MUTEX) {
								try {
									MUTEX.wait();
									System.out.println("woke up MUTEX");
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			};
			currentThread = new Thread(currentRunnable);
			currentThread.start();
		}
	}

	public class TreeExplorer	{
		PSL2C[] gens;
		Complex[][] fixedPoints;
		double eps2;
		int maxDepth;
		boolean _drawFlat;
		int vertexFiberLength = 3;
		double[] vertices = new double[vertexFiberLength*100000];
		int[] breakPoints = new int[100000];
		int vertCount;
		int maxNumberIntermediatePoints = 0;
		int _maxNumberPoints;
		int numberCurves;
		boolean _drawLimited;
		boolean _minimalDraw = false;
		double[][] intermediatePoints = null;
		boolean emergencyExit = false;
		IndexedLineSet _theLimitSet;
		
		TreeExplorer(IndexedLineSet ils, PSL2C[] gs, Complex[][] fp, double e, int d, int maxP, boolean df)	{
			super();
			lock.writeLock();
			gens = PSL2C.copy(null,gs);
			fixedPoints = Cn.copy(null, fp);
			for (int i = 0; i<fixedPoints.length; ++i)	{
				if (fixedPoints[i].length > maxNumberIntermediatePoints) 
					maxNumberIntermediatePoints = fixedPoints[i].length;
			}
			intermediatePoints = new double[maxNumberIntermediatePoints][vertexFiberLength];
			_theLimitSet = ils;
			_maxNumberPoints = maxP;
			if (_maxNumberPoints < 0) _drawLimited = false;
			else _drawLimited = true;
			eps2 = e*e;
			maxDepth = d;
			_drawFlat = df;
			_minimalDraw = minimalDrawAtMaximalDepth;
			if (debug) System.err.println("Created explorer with max no points: "+_maxNumberPoints);
			lock.writeUnlock();
		}
		
		private void updateILS()	{
			double[] exactVerts = new double[emergencyExit ? (vertCount*vertexFiberLength) : (vertCount+1)*vertexFiberLength];
			System.arraycopy(vertices, 0, exactVerts, 0, vertCount*vertexFiberLength);
			if (!emergencyExit) System.arraycopy(vertices, 0, exactVerts, vertCount*vertexFiberLength, vertexFiberLength);
			vertCount++;
			
			int[][] inds = new int[numberCurves][];
			int vertexCounter = 0;
			for (int i = 0; i<numberCurves; ++i)	{
				int size = breakPoints[i+1]- breakPoints[i];
				inds[i] = new int[size];
				for (int j = 0 ; j < size; j++)	{
					inds[i][j] = vertexCounter++;
				}
			}
			IndexedLineSetUtility.createCurveFromPoints(_theLimitSet,exactVerts, vertexFiberLength, inds);								
		}
		
		public void explore() 	{
			vertCount = 0;
			firsttime = true;
			emergencyExit = false;
			breakPoints[numberCurves++] = 0;
			long milli = System.currentTimeMillis();
			if (_drawFlat) for (int i = 0; i<maxNumberIntermediatePoints; ++i) intermediatePoints[i][2] = 0.0;
			try {
				explore(PSL2C.IDENTITY, 0, 0);
			} catch (IndexOutOfBoundsException e) {
				emergencyExit = true;
				DiscreteGroupUtility.logger.warning("Max elements exceeded, exiting explore");
			} catch (InterruptedException e) {
				synchronized (MUTEX) {
					emergencyExit = true;
//					interrupt = false;
//					newParameters = false;
					DiscreteGroupUtility.logger.warning("Interrupted, exiting explore");
				}
			}
			breakPoints[numberCurves] = vertCount;
			long milli2 = System.currentTimeMillis();
			updateILS();
			System.err.println("Collected "+vertCount+" points in "+(milli2-milli)/1000.0+" seconds");
		}
		boolean firsttime;

		public void explore(PSL2C oldM, int which, int _depth) throws IndexOutOfBoundsException, InterruptedException	{
			PSL2C newM = null;
			Complex newP = new Complex();
			int lim = firsttime ? 4 : 3;
			firsttime = false; 
			for (int i = 0; i<lim; ++i)	{
				int k = (which+i+3)%4;
				newM = PSL2C.times(newM, oldM, gens[k]);
				if (fsa != null && !fsa.accepts(newM.getWord())) {
					breakPoints[numberCurves++] = vertCount;
					continue; //{begin = n-1; reject = true;}
				}
				int n = fixedPoints[k].length;
				int begin = 4-lim;
				if (_minimalDraw && _depth == maxDepth) begin = n-1;
				boolean goDeeper = false;
				for (int j =  begin; j<n; ++j)	{
					CP1.times(newP, newM.m, fixedPoints[k][j]);
					if (!_drawFlat) CP1.r2ToUnitSphere3(intermediatePoints[j], newP);
					else {intermediatePoints[j][0] = newP.re; intermediatePoints[j][1] = newP.im; }
					if (_depth==maxDepth || j == 0) continue;  // we don't care in these cases about distances
					double d2 = Rn.euclideanDistanceSquared(intermediatePoints[j], intermediatePoints[j-1]);
					if (d2 > eps2) { goDeeper = true; break; }
				}
				if (_depth == maxDepth || !goDeeper) {
					// write out the intermediate points
					for (int j = begin; j<n; ++j)	{
						System.arraycopy(intermediatePoints[j], 0, vertices, vertCount*vertexFiberLength, vertexFiberLength);
						vertCount++;
						if (vertCount*vertexFiberLength >= vertices.length)	{
							double[] newV = new double[vertices.length*2];
							System.arraycopy(vertices,0,newV,0,vertices.length);
							vertices = newV;
						}
						if (_drawLimited && vertCount > _maxNumberPoints)	{
							throw new IndexOutOfBoundsException();
						}
						// check to see if we've been interrupted
						if ( (vertCount % 1000) == 0)	{
							synchronized (MUTEX) {
								if ( interrupt) {
									System.out.println("interrupted MUTEX");
									throw new InterruptedException();
								}
							}							
						}
					}
					// the end point is now the beginning point for the next segment
					// we only get here if we really calculated the end point
					System.arraycopy(intermediatePoints[n-1], 0, intermediatePoints[0], 0, vertexFiberLength);
				} else {
					explore( newM, k, _depth+1);
				}
			}
		}
	}

	public void setParabolicCommutators(boolean parabolicCommutators) {
		// TODO Auto-generated method stub
		
	}

}
