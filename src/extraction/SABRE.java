package extraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import model.Alignment;
import model.AnnotatedArray;
import model.AnnotationColumn;
import model.CorpusObserver;
import model.NumericalColumn;
import model.Observable;
import model.Observer;
import model.Pattern;
import model.PointXMLSerializable;
import model.SABREObserver;

/**
 * Extract alignments between two annotation arrays.
 * @author zach
 *
 */
public class SABRE implements Observable{
	
	private AnnotatedArray aa1;
	private AnnotatedArray aa2;
	
	/** Max distance around a seed in which we search additional annotation to add to this seed */
	public static int MAX_RADIUS = 10;
	
	/**
	 * Store the alignments extracted from <aa1> and <aa2> when the method <align> is used
	 */
	private ExtractionAlignments alignments;

	private transient List<SABREObserver> listObserver = new ArrayList<SABREObserver>();
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -4679637715310456302L;
	
	/**
	 * Table which contains the similarities between a line of aa1 and aa2
	 */
	SimilarCouplesTable sim; 
	Seed[][] seed_covering;
	
	private SABREParameter param = null;
	
	private double bestPatternScore = -Double.MAX_VALUE;

	public void setParam(SABREParameter param) {
		this.param = param;
		notifyObserversParameters();
	}

	/** SABRE is a singleton */
	private SABRE(){}
	
	/**
	 * Holder used to implement the singleton design pattern 
	 * @author zach
	 *
	 */
	private static class SABREHolder{
		
		private final static SABRE sabre = new SABRE();
		
	}
	
	public static SABRE getInstance(){
		return SABREHolder.sabre;
	}
	
	public double getBestPatternScoreBetween(AnnotatedArray aa1, AnnotatedArray aa2){
				
		bestPatternScore = -Double.MAX_VALUE;
		align(aa1, aa2, -Double.MAX_VALUE, true);
		return bestPatternScore;
		
	}
	
	/**
	 * Find the best similar subparts (i.e., the alignments of maximal score) in two annotated arrays
	 * @param aa1 First annotated array
	 * @param aa2 Second annotated array
	 * @param minimum_score Minimum score above which two similar subparts correspond to an alignment
	 * @return
	 */
	public ExtractionAlignments align(AnnotatedArray aa1, AnnotatedArray aa2, double minimum_score, boolean isToComputeSimilarity){				
						
		 if(param != null){
			this.aa1 = aa1;
			this.aa2 = aa2;

			param.min_seed_score = Math.max(1.0, minimum_score/10.0);
			
			alignments = new ExtractionAlignments(minimum_score);
			
			/* Compute the 2D table that will locate similar parts in the two annotated elements */
			sim = new SimilarCouplesTable(aa1, aa2);

			seed_covering = new Seed[aa1.getNumberOfLines()][aa2.getNumberOfLines()];

//			System.out.println(sim);
			/* Get the positive diagonals in C (i.e.: part which likely to represent an alignment) */
			TreeSet<Seed> treeSeed = getSeeds();
			
			/* If it is to compute the similarity, keep at most <maxSeeds> best seeds */
			int maxSeeds = 5;
			
			if(isToComputeSimilarity && treeSeed.size() > maxSeeds)
				treeSeed = keepBestSeeds(treeSeed, maxSeeds);
			
				
				
			
//if(treeSeed.size() > 0){
//	System.out.println(treeSeed.size() + " seeds found");
//	
//	for(Seed s: treeSeed){
//		System.out.println(s.score);
//		System.out.println("\t"  + aa1.getFileName() + " (lines " + s.l1_min + "-" + s.l1_max + ")");
//		System.out.println("\t"  + aa2.getFileName() + " (lines " + s.l2_min + "-" + s.l2_max + ")");
//	}
//}
//else
//	System.out.print(".");

			/* Remove duplicate seeds (i.e., seeds which are almost identical to a seed of better score) */
			List<Seed> t = filterSeeds(treeSeed);

//System.out.println(t.size() + " seeds after filtering");

			
			List<Seed> al_seed = mergeSeeds(t);
			
//	System.out.println(al_seed.size() + " after merge");	

			expandSeeds(al_seed);	
			
			dealloc();
			
			return alignments;
		}
		else{
			System.err.println("Error in SABRE.java, at least one parameter have not been initialized");
			return null;
		}

//System.out.println(patterns.size() + " patterns extracted");
	}
	
	private List<Seed> filterSeeds(TreeSet<Seed> treeSeed) {
		
		Iterator<Seed> it = treeSeed.iterator();
		List<Seed> t = new ArrayList<>();
		
		boolean[] toFilter = new boolean[treeSeed.size()];

		while(it.hasNext())
			t.add(it.next());

		for(int i = 0 ; i < t.size() ; ++i){
			
			Seed s1 = t.get(i);
			
			for(int j = i+1 ; j < t.size() ; ++j){
				Seed s2 = t.get(j);
				
				if(s1.isAlmostIdenticalTo(s2)){

					if(s1.score > s2.score 
							|| (s1.score == s2.score && s1.couples.size() > s2.couples.size())
					)
						toFilter[j] = true;
					else
						toFilter[i] = true;
				}
				
			}
				
		}

		for(int i = t.size() - 1 ; i >= 0 ; --i)
			if(toFilter[i])
				t.remove(i);

		return t;
	}

	private TreeSet<Seed> keepBestSeeds(TreeSet<Seed> treeSeed, int maxSeeds) {
		
		TreeSet<Seed> tempSet = new TreeSet<>(new Comparator<Seed>() {
			
			@Override
			public int compare(Seed s1, Seed s2) {
				
				int result = (int) (s2.score - s1.score);
				if(result == 0)
					result = 1;
				
				return result;
			}
		});
		
		Iterator<Seed> it = treeSeed.iterator();
		int i = 0;
		
		while(i < maxSeeds){
			
			Seed s = it.next();
			tempSet.add(s);
			++i;
		}
		
		return tempSet;
	}

	public void dealloc(){

		aa1 = null;
		aa2 = null;
		sim = null;
		seed_covering = null;
	}
	
	public void expandSeeds(List<Seed> al_seed) {
		
		/* For each seed */
		for(int i = 0 ; i < al_seed.size() ; ++i) {

			Seed si = al_seed.get(i);
			
			/* If the seed has not been removed (in the merge step or the expand step) */
			if(si != null) {
				
				/* Maximal distance around the seed under which an annotation or an alignment can be added to the seed */ 
				int radius = Math.min(MAX_RADIUS, (int) (si.score / param.gap_cost));
//				System.out.println("SABRE : radius: "+ radius);
				/* Get all the candidates couple position in this radius */
				ArrayList<Seed> candidates = getPositionsInRadius(si, radius);
				
				/* The best seed found while expanding the seed si (only updated when a local maximal seed is found) */
				Seed bestSeed = null;
				double bestSeedScore = -1;
				
				
				/* While the score of the seed is positive and there are candidates */ 
				while(si.score > 0 && candidates.size() > 0){	

					/* 1 - Find the best candidate */
					Seed bestCandidate = candidates.get(0);
					double bestScore = bestCandidate.score - bestCandidate.layoutCost;
					int bestId = 0;
					
					for(int j = 1 ; j < candidates.size() ; ++j) {
						
						Seed sj = candidates.get(j);
						
						double scj = sj.score - sj.layoutCost;
						
						if(bestScore < scj) {
							bestCandidate = sj;
							bestScore = scj;
							bestId = j;
						}
				 
					}
					

					double newSeedScore = si.score - bestCandidate.layoutCost;
					
					/* 2 - Update the best seed if necessary (i.e.: if the current seed si score is better than the new score and the best one */ 
					if(si.score > newSeedScore && si.score > bestSeedScore) {
						
						/* si is the new best seed */
						bestSeed = new Seed(si);
						bestSeedScore = si.score;
					}
					
					/* Evaluate the modifications of the bounds of the seed si */
					int[] bound = new int[4];
					int temp = si.l1_min - bestCandidate.l1_min;
					bound[0] = temp > 0 ? temp : 0;
					
					temp = si.l2_min - bestCandidate.l2_min;
					bound[2] = temp > 0 ? temp : 0;
					
					temp = bestCandidate.l1_max - si.l1_max;
					bound[1] = temp > 0 ? temp : 0;
					
					temp = bestCandidate.l2_max - si.l2_max;
					bound[3] = temp > 0 ? temp : 0;
					
					/* Add the best candidate */
					si.merge(bestCandidate, bestCandidate.layoutCost);
					
					/* Remove the best candidate from the list */ 
					candidates.remove(bestId);
					
					/* If the score of the seed is still positive */
					if(si.score > 0) {

						/* Check the compatibility of the remaining candidates and update the layout of the compatible one if necessary*/
						for(int j = candidates.size() - 1 ; j >= 0 ; --j) {
							Seed sj = candidates.get(j);
							
							/* Get the layout between sj and bestCandidate */
							double layout = sj.computeLayout(bestCandidate);
							
							/* If sj is not compatible with bestCandidate */
							if(layout == -Double.MAX_VALUE)
								candidates.remove(j);
							
							/* If sj is compatible with bestCandidate */
							else {
								
								/* If the cost of the layout with bestCandidate is better than the one with the previous seed, update it */
								if(layout < sj.layoutCost)
									sj.layoutCost = layout;
							}

						}
						
						/* Search new candidates in the new bounds that bestCandidate now enable to reach */
						for(int j = si.l2_min ; j <= si.l2_max ; ++j) {
							
							/* Above the old l1_min */
							for(int k = 0 ; k < bound[0] ; ++k){
								
								Seed c_seed = getSeedIfPositive(si.l1_min + k, j);

								if(c_seed != null) {
									c_seed.layoutCost = c_seed.computeLayout(si);
									
									if(c_seed.layoutCost != -Double.MAX_VALUE)
										candidates.add(c_seed);
								}
								
							}
							
							/* Under the old l1_max */
							for(int k = 0 ; k < bound[1] ; ++k){
								
								Seed c_seed = getSeedIfPositive(si.l1_max - k, j);

								if(c_seed != null) {
									c_seed.layoutCost = c_seed.computeLayout(si);
									
									if(c_seed.layoutCost != -Double.MAX_VALUE)
										candidates.add(c_seed);
								}
								
							}
							
							
						}
						

						for(int j = si.l1_min + bound[0] ; j <= si.l1_max - bound[1] ; ++j) {

							/* On the left of the old l2_min */
							for(int k = 0 ; k < bound[2] ; ++k){
								
								Seed c_seed = getSeedIfPositive(j, si.l2_min + k);

								if(c_seed != null) {
									c_seed.layoutCost = c_seed.computeLayout(si);
									
									if(c_seed.layoutCost != -Double.MAX_VALUE)
										candidates.add(c_seed);
								}
								
							}
							
							/* On the right of the old l2_max */
							for(int k = 0 ; k < bound[3] ; ++k){
								
								Seed c_seed = getSeedIfPositive(j, si.l2_max - k);

								if(c_seed != null) {
									c_seed.layoutCost = c_seed.computeLayout(si);
									
									if(c_seed.layoutCost != -Double.MAX_VALUE)
										candidates.add(c_seed);
								}	
							}
						}
					} // End: if the score of the seed is still positive
				} // End: while(si.score > 0 && candidates.size() > 0){
				
				/* If the last seed is the best ever */
				if(bestSeedScore < si.score)
					bestSeed = si;
				
				/* If the score is higher than the minimal score, patterns have been found */
				if(bestSeed != null) {
					
					if(bestSeed.score > bestPatternScore)
						bestPatternScore = bestSeed.score;
				
					if(bestSeed.score >= alignments.minimumScore) {
						Alignment al = bestSeed.getAlignment(bestSeed.score);
						alignments.addAlignment(al);
					}
					bestSeed.purge();
				}
				
				
			} // End: if(si != null) {
		} // End: for(int i = 0 ; i < al_seed.size() ; ++i) {
		
	}

	/**
	 * Given a seed, get all the candidates CouplePosition and Seeds in a given radius around the annotations of the seed
	 * @param si The seed
	 * @param radius The radius
	 * @return A list of all the seeds in the radius (if a couple position which is not a seed is encountered, a seed is created with all the annotations at this position)
	 */
	private ArrayList<Seed> getPositionsInRadius(Seed si, int radius) {
		
		ArrayList<Seed> result = new ArrayList<Seed>();

		/* For each positions (xi,xj) in the radius around the seed */
		for(int xi = Math.max(si.l1_min - radius, 0) ; xi < Math.min(si.l1_max + radius, aa1.getNumberOfLines()) ; ++xi)
			for(int xj = Math.max(si.l2_min - radius, 0) ; xj < Math.min(si.l2_max + radius, aa2.getNumberOfLines()) ; ++xj) {
				
				Seed c_seed = getSeedIfPositive(xi, xj);

				if(c_seed != null) {
					c_seed.layoutCost = c_seed.computeLayout(si);
					
					/* If the seed are compatible */
					if(c_seed.layoutCost != -Double.MAX_VALUE)
						result.add(c_seed);
					else{
						
						/* Consider a seed restricted to position [xi][xj] */
						ArrayList<Integer> columns = new ArrayList<Integer>();
						
						for(int j = 0 ; j < aa1.getNumberOfAnnotationColumns() ; ++j) {
							
							if(sim.C[xi][xj][j] > 0){
								columns.add(j);
							}
							
						}
						
						Seed s = new Seed(xi, xj, columns, sim.C[xi][xj][aa1.getNumberOfAnnotationColumns()]);
						s.layoutCost = si.computeLayout(s);
						/* If the seed is compatible */
						if(s.layoutCost != -Double.MAX_VALUE)
							result.add(s);
					}
						
				}
				
			}

		return result;
	}
	
	/**
	 * Get the seed which corresponds to a position in C (if the value of C is positive and the position is not already covered by a seed, create one)
	 * @param xi First coordinate in C
	 * @param xj Second coordinate in C
	 * @return
	 */
	public Seed getSeedIfPositive(int xi, int xj) {

		Seed c_seed = this.seed_covering[xi][xj];

		double score = sim.C[xi][xj][aa1.getNumberOfAnnotationColumns()];
		
		/* If the line xi in aa1 and xj in aa2 have something similar */
		if(score > 0) {
			
			/* If this position is not yet in a seed */
			if(c_seed == null) {
				
				/* Create the corresponding seed */
				ArrayList<Integer> columns = new ArrayList<Integer>();
								
				for(int j = 0 ; j < aa1.getNumberOfAnnotationColumns() ; ++j) {
					
					if(sim.C[xi][xj][j] > 0)
						columns.add(j);
					
				}

				c_seed = new Seed(xi, xj, columns, score);
				seed_covering[xi][xj] = c_seed;
			}			
		}
		
		return c_seed;
	}

	/* Merge close compatible seeds */
	private List<Seed> mergeSeeds(List<Seed> seeds) {
		

		/* For each couple of seeds which are not null (i.e. which have not been merged) */
		for(int i = 0 ; i < seeds.size() ; ++i) {
			
			Seed s1 = seeds.get(i);
			
			if(s1 != null)
				for(int j = i +1 ; j < seeds.size() ; ++j) {
					
					Seed s2 = seeds.get(j);
					
					/* If s2 has not been merged in another seed and if the seeds are not to far away (we use this first test to avoid computing the layout of all the couples of seeds) */
					if(s2 != null && !s1.isToCostlyToMergeWith(s2)) {
						
						double layoutScore = s1.computeLayout(s2);
						
						/* If the layoutScore is lower than the score of seed s2 and if the seeds are compatible, the seed are merged */
						if(layoutScore < s2.score && layoutScore != -Double.MAX_VALUE) {
							s1.merge(s2, layoutScore);
							seeds.set(j, null);
	
						}
					}
				}
		}
		return seeds;
	}

	/**
	 * Get the positive descending sub-diagonals in C (i.e.: part which are likely to represent an alignment)
	 */
	public TreeSet<Seed> getSeeds() {
		
		TreeSet<Seed> seeds = new TreeSet<Seed>(new Comparator<Seed>() {
			
			@Override
			public int compare(Seed s1, Seed s2) {
				
				int result = (int) (s2.score - s1.score);
				if(result == 0)
					result = 1;
				
				return result;
			}
		});
				
		/* The descending diagonals in C can be exhaustively represented by:
		 * 	- diagonals which starts from (i,0) - i line in aa1, 0 line in aa2.
		 * 	- diagonals which starts from (0,i) - 0 line in aa1, i line in aa2.
		 */
		
		/* For each diagonal which starts at (i,0) */
		for(int i = 0 ; i < aa1.getNumberOfLines() ; ++i)
			getSeedsOnDiagonal(seeds, i, 0);
		
		/* For each diagonal which starts at (0,i) */
		for(int i = 1 ; i < aa2.getNumberOfLines() ; ++i)
			getSeedsOnDiagonal(seeds, 0, i);
		
		return seeds;
	}

	public class Seed implements Serializable{

		private static final long serialVersionUID = 1638029465771120453L;

		/**
		 * Line of the first annotation in aa1 (minimum of the line of all annotations in the seed)
		 */
		int l1_min;
		
		/**
		 * Line of the last annotation in aa1 (max of the line of all annotations in aa1 in this seed)
		 */
		int l1_max;
		
		/**
		 * Line of the first annotation in aa2 (minimum of the line of all annotations in the seed)
		 */
		int l2_min;
		
		/**
		 * Line of the last annotation in aa2 (max of the line of all annotations in aa1 in this seed)
		 */
		int l2_max;
		
		/**
		 * Couple of annotations contained in the seed
		 */
		ArrayList<CouplePosition> couples = new ArrayList<CouplePosition>();
		
		double score = 0;
		
		/* Cost to merge this seed with another (only used in the expanding step) */
		double layoutCost = 0;
		
		/**
		 * Create a seed which includes one couple of annotation
		 * @param l1 Line of the annotation in aa1
		 * @param l2 Line of the annotation in aa2
		 * @param c  Columns of the annotations at position l1 and l2 which are similar
		 * @param s  Score of the couples of annotations
		 */
		public Seed(int l1, int l2, ArrayList<Integer> c, double s) {
			l1_min = l1;
			l1_max = l1;
			l2_min = l2;
			l2_max = l2;
			score  = s;
			couples.add(new CouplePosition(l1, l2, c));
		}		

		/* Purge the seed from sim.C et seed_covering */
		public void purge() {
			
			for(CouplePosition cp : couples) {
				sim.C[cp.l1][cp.l2][aa1.getNumberOfAnnotationColumns()] = 0;
				seed_covering[cp.l1][cp.l2] = null;
			}
		}

		public Alignment getAlignment(double alignmentScore) {

			ArrayList<PointXMLSerializable> p1 = new ArrayList<>();
			ArrayList<PointXMLSerializable> p2 = new ArrayList<>();
			
			for(CouplePosition cp : couples) {
				
				for(Integer c : cp.columns) {
					p1.add(new PointXMLSerializable(cp.l1, c));
					p2.add(new PointXMLSerializable(cp.l2, c));
				}
				
			}
			
			Pattern pa1 = new Pattern(aa1, p1);
			Pattern pa2 = new Pattern(aa2, p2);
			
			return new Alignment(pa1, pa2, alignmentScore);
		}

		public Seed(Seed si) {
			couples.addAll(si.couples);

			l1_min = si.l1_min;
			l2_min = si.l2_min;
			l1_max = si.l1_max;
			l2_max = si.l2_max;

			this.layoutCost = si.layoutCost;
			this.score = si.score;
			
		}
		
		public String toString() {
			
			String result = score + " : ";
			
			for(CouplePosition cp : couples) {
				
				result += cp.l1 + "," + cp.l2 + "(columns ";


				for(Integer i : cp.columns)
					result +=i + " ";
				
				result += "), ";
//for(Integer i : cp.columns)
////	try {
//		result += aa1.getAnnotation(cp.l1, i) + "," + aa2.getAnnotation(cp.l2, i) + "|";
////	} catch (InvalidAnnotationIndex e) {
////		e.printStackTrace();
////	}

	
			}
			return result;
			
		}


		/**
		 * Compute the distance between the bound of this seed and another
		 * @param s2 The other seed
		 * @return distance between [l1_min, l1_max] and [s2.l1_min, s2.l2_max]
		 */
		public int boundDistance(Seed s2) {
			
			int res1 = 0;
		
			/* Compute the distance between the bounds in aa1 */
			int t = s2.l1_min - l1_max;
			if(t > 0)
				res1 = t;
			else {
				t = l1_min - s2.l1_max;
				
				if(t > 0)
					res1 = t;
			}
			
			/* Compute the distance between the bounds in aa2 */
			int res2 = 0;
		
			t = s2.l2_min - l2_max;
			if(t > 0)
				res2 = t;
			else {
				t = l2_min - s2.l2_max;
				
				if(t > 0)
					res2 = t;
			}
			
			return Math.min(res1, res2);
			
		}
		
		/**
		 * Is the distance between this seed and another one to large to merge the seeds
		 * @param s2 The other seed
		 * @return True if the layout cost to merge the seeds is greater than the score of s2
		 */
		public boolean isToCostlyToMergeWith(Seed s2) {
						
			return param.gap_cost * boundDistance(s2) > s2.score;
			
		}
				
		/**
		 * Merge this seed to another
		 * @param s2 The second seed
		 * @param layoutCost The layout cost to merge these seeds
		 */
		public void merge(Seed s2, double layoutCost) {
			
			/* Update the score */
			score = score + s2.score - layoutCost;
			
			/* Update the annotation couples */
			couples.addAll(s2.couples);
			
			/* Update the bound of the seed */
			l1_min = Math.min(s2.l1_min, l1_min);
			l1_max = Math.max(s2.l1_max, l1_max);
			l2_min = Math.min(s2.l2_min, l2_min);
			l2_max = Math.max(s2.l2_max, l2_max);
			
			/* Update the seed covering */
			for(CouplePosition cp : s2.couples)
				seed_covering[cp.l1][cp.l2] = this; 
			
		}

		/**
		 * Compute the cost of the layout between two seeds
		 * @param s2
		 * @return the lowest cost layout between a couplePosition in this and a couplePosition in s2 if the seeds are compatible ; -Double.MAX_VALUE otherwise.
		 */
		public double computeLayout(Seed s2) {
			
			double bestLayoutCost = Double.MAX_VALUE;
			
			int i1 = 0;
			
			/* While all the couples in this have no been considered and while no incompatibility has been found */
			while(i1 < couples.size() && bestLayoutCost != -Double.MAX_VALUE) {
				
				CouplePosition c1 = couples.get(i1);
			
				int i2 = 0;
				
				/* While all the couples in s2 have no been considered and while no incompatibility has been found */
				while(i2 < s2.couples.size() && bestLayoutCost != -Double.MAX_VALUE) {
					
					CouplePosition c2 = s2.couples.get(i2);

					if(c1.isCompatible(c2)) {
						double v = c1.layoutCost(c2); 
						if(v < bestLayoutCost)
							bestLayoutCost = v; 
					}
					else
						bestLayoutCost = -Double.MAX_VALUE;
					
					++i2;
				}
				
				++i1;
			}
			
			return bestLayoutCost;
		}


		/** 
		 * Add a new annotation to the seed
		 * @param l1 Line of the annotation in aa1
		 * @param l2 Line of the annotation in aa2
		 * @param c Columns of the annotations at line l1 and l2 which are similar
		 * @param s Score of the couple
		 */
		public void add(int l1, int l2, ArrayList<Integer> c, double s) {
			
			/* Add the couple */
			couples.add(new CouplePosition(l1, l2, c));
			
			score += s;
			
			seed_covering[l1][l2] = this;
		}
		
		/**
		 * Used once the seed is sure to be kept, for each couple position say that it is covered by the seed
		 */
			
		public void addCover() {
			Iterator<CouplePosition> it_c = couples.iterator();
			CouplePosition cp = it_c.next();
			
			l1_min = cp.l1;
			l1_max = cp.l1;
			l2_min = cp.l2;
			l2_max = cp.l2;
			
			while(it_c.hasNext()) {
				
				CouplePosition pc = it_c.next();
				seed_covering[pc.l1][pc.l2] = this; 
				
				if(pc.l1 < l1_min)
					l1_min = pc.l1;
				else if(pc.l1 > l1_max)
					l1_max = pc.l1;
				
				if(pc.l2 < l2_min)
					l2_min = pc.l2;
				else if(pc.l2 > l2_max)
					l2_max = pc.l2;
			}
		}
		
		public boolean isAlmostIdenticalTo(Seed s){
			
			return (Math.abs(l1_min-s.l1_min) <= 1
					&& Math.abs(l2_min-s.l2_min) <= 1
					&& Math.abs(l1_max-s.l1_max) <= 1
					&& Math.abs(l2_max-s.l2_max) <= 1
				   );
		}
		
	}
	
	/**
	 * Position of a couple of annotations in aa1 and aa2
	 * @author zach
	 *
	 */
	public class CouplePosition implements Serializable{
		

		
		private static final long serialVersionUID = -4679637742310456302L;
		public int l1,l2;
		public ArrayList<Integer> columns;
		
		public CouplePosition(int line1, int line2, int column) {
			l1 = line1;
			l2 = line2;
			columns = new ArrayList<Integer>();
			columns.add(column);
			
		}
		
		public CouplePosition(int line1, int line2, ArrayList<Integer> columns) {
			l1 = line1;
			l2 = line2;
			this.columns = columns;
			
		}

		/**
		 * Compute the layout cost between two couples of annotations
		 * @param c2
		 * @return
		 */
		public double layoutCost(CouplePosition c2) {
			
			/* Count the number of gaps (we assume that it is always better to do one gap than two desynch) */
			int v1 = l1 - c2.l1;
			int v2 = l2 - c2.l2;
			
			int gap = Math.max(Math.min(Math.abs(v1), Math.abs(v2))-1, 0);
			int desynch = Math.max(Math.abs(v1 - v2), 0);
			
			return param.gap_cost * gap + param.desynch_cost * desynch;
			
		}

		/**
		 * Evaluate the compatibility of two couple positions (i.e.: can this two couple positions be in the same alignment)
		 * @param c2 The other couple
		 * @return True if the two couples position don't have an annotation in common in aa1 or aa2
		 */
		public boolean isCompatible(CouplePosition c2) {
			
			boolean isCompatible = true;
			
			/* If the couple position correspond to the same lines in aa1 or aa2 */
			if(l1 == c2.l1 || l2 == c2.l2) {
				
				/* Check if they contain the same columns */
				int id1 = 0;
				
				while(isCompatible && id1 < columns.size()) {
					
					int v1 = columns.get(id1);
					int id2 = 0;
					
					while(isCompatible && id2 < c2.columns.size()) {
											
						if(v1 == c2.columns.get(id2))
							isCompatible = false;
						
						id2++;
					}
					
					++id1;
				}
			}
			
			return isCompatible;
		}
		
		public String toString() {

			return "l1:" + l1 + " l2:" + l2 + columns.toString();
			
		}
	}
	
	public class SimilarCouplesTable implements Serializable{
		
		private static final long serialVersionUID = 8328008075729886930L;

		/**
		 * C[i][j][k] contains:
		 * 		- 0 if the annotations (i,k) in aa1 and (j,k) are not similar
		 * 		- the similarity of (i,k) and (j,k) otherwise
		 * 		(- the sum of the C[i][j][k] if k is maximal)
		 */
		transient public double[][][] C;
		
		public AnnotatedArray ae1;
		public AnnotatedArray ae2;
		
		public SimilarCouplesTable(AnnotatedArray ae1, AnnotatedArray ae2) {
			
			int colNb = ae1.getNumberOfAnnotationColumns();
			this.ae1 = ae1;
			this.ae2 = ae2;

			/* Initialization of the table C (1 is added to the number of column since the last cell will contain the sum of the similarity for two given lines) */
			C = new double[ae1.getNumberOfLines()][ae2.getNumberOfLines()][colNb+1];

	 		/* Initialize the values of <C> */
	 		for (int i = 0; i < ae1.getNumberOfLines(); i++)
	 			for (int k = 0; k < ae2.getNumberOfLines(); k++)
	 					C[i][k][colNb] = 0.0;
				
			/* For each annotation column in ae1 and ae2 */
			for(int j = 0 ; j < ae1.getAnnotations().size() ; ++j){
				
				AnnotationColumn c1 = ae1.getAnnotations().get(j);
				AnnotationColumn c2 = ae2.getAnnotations().get(j);
				
				/* For each lines in ae1 */
				for(int i = 0 ; i < c1.getValues().size() ; ++i){
					
					/* For each lines in ae2 */
					for(int k = 0 ; k < c2.getValues().size() ; ++k){
						
						C[i][k][j] = c1.sim(i, c2.getValues().get(k));
						C[i][k][colNb] += C[i][k][j]; 
					}
					
				}
				
			}
		
			/* For each numerical annotation column in ae1 and ae2 */
			for(int j = 0 ; j < ae1.getNumericalAnnotations().size() ; ++j){
			
				NumericalColumn c1 = ae1.getNumericalAnnotations().get(j);
				NumericalColumn c2 = ae2.getNumericalAnnotations().get(j);
			
				/* For each lines in ae1 */
				for(int i = 0 ; i < c1.getValues().size() ; ++i){
					
					/* For each lines in ae2 */
					for(int k = 0 ; k < c2.getValues().size() ; ++k){

						C[i][k][j+ae1.getAnnotations().size()] = c1.sim(i, c2.getValues().get(k));
						C[i][k][colNb] += C[i][k][j+ae1.getAnnotations().size()];
						
					}
					
				}
			}

			
		}
		
		public String toString() {
			
			String result = "";
			int colNb = ae1.getNumberOfAnnotationColumns();
			
			for(int i = 0 ; i < C.length ; ++i) {
				for(int j = 0 ; j < C[0].length ; ++j)
					result += C[i][j][colNb] + "\t";
				
				result += "\n";
			}
			
			return result;
		}
				
	}
	
	/**
	 * Get all the seeds on a diagonal of sim 
	 * @param seeds List of seeds that will be updated
	 * @param start_x Starting PointXMLSerializable in currentAE1 of the diagonal
	 * @param start_y Starting PointXMLSerializable in currentAE2 of the diagonal
	 */
	public void getSeedsOnDiagonal(TreeSet<Seed> seeds, int start_x, int start_y) {

		/* current line number of the considered diagonal in AE1 */ 
		int j = start_x;
		
		/* current line number of the considered diagonal in AE2 */
		int k = start_y;
		
		Seed currentSeed = null;
		
		while(j < aa1.getNumberOfLines() && k < aa2.getNumberOfLines()) {		

			double score = sim.C[j][k][aa1.getNumberOfAnnotationColumns()];
			
			/* If there is something similar in line j of ae1 and k of ae2 */
			if(score > 0) {

				ArrayList<Integer> columns = new ArrayList<Integer>();

				/* For each annotation column */
				for(int c = 0 ; c < aa1.getNumberOfAnnotationColumns() ; ++c) {
					
					/* If the couple of annotations in column c are similar */
					if(sim.C[j][k][c] > 0) 
						columns.add(c);
				}

				/* Add the couple position in the seed */
				
				/* If we start a new seed */
				if(currentSeed == null) 
					currentSeed = new Seed(j, k, columns, score);
			
				/* If we continue a seed */
				else
					currentSeed.add(j, k, columns, score);
				
			}
			
			
			/* If nothing is similar in line j of ae1 and k of ae2 */
			else {
				
				/* If it is the end of a seed */
				if(currentSeed != null) {
					
					/* If the diagonal score is sufficient to be considered as a seed */
					if(currentSeed.score > param.min_seed_score) {
						seeds.add(currentSeed);
						currentSeed.addCover();

						
//System.out.println("seed: " + currentSeed);		
						
						currentSeed.l1_max = j-1;
						currentSeed.l2_max = k-1;
						
						
					} // End: If the score is sufficient to have a seed
					
					currentSeed = null;
					
				} // End: If this is the end of a seed
				
			}
			
			j++;
			k++;
			
		} // End: while(j < currentAE1.getNumberOfLines() && k < currentAE2.getNumberOfLines()) {

		/* If at the end of the diagonal, the current seed is not null and has a sufficient score */
		if(currentSeed != null && currentSeed.score > param.min_seed_score) {
				seeds.add(currentSeed);
				currentSeed.addCover();
		}
		
	}
	
	public double similarity(Pattern p1, Pattern p2){

		return getBestPatternScoreBetween(p1.getPatternAA(), p2.getPatternAA());
//		System.out.println("dissimilarity " + c.getPatterns().indexOf(p) + " " + c.getPatterns().indexOf(this ));
		
//		aa1 = p1.getPatternAA();
//		aa2 = p2.getPatternAA();
//
//		seed_covering = new SABRE.Seed[aa1.getNumberOfLines()][aa2.getNumberOfLines()];
//
////long chrono = java.lang.System.currentTimeMillis();
//		sim = new SimilarCouplesTable(aa1, aa2);
////long chrono2 = java.lang.System.currentTimeMillis();
////SABRE.tempsSim += (chrono2 - chrono);
//
//
//		TreeSet<Seed> treeSeed = getSeedsForPatternSimilarity(aa1, aa2, sim);
////chrono = java.lang.System.currentTimeMillis();
////SABRE.tempsGetSeeds += (chrono - chrono2);
//					
////System.out.println(treeSeed.size() + " after getseeds");			
//		ArrayList<Seed> al_seed = mergeSeedsForPatternSimilarity(treeSeed);
//
////chrono2 = java.lang.System.currentTimeMillis();
////SABRE.tempsMergeSeeds += (chrono2 - chrono);
//		//			ArrayList<Seed> al_seed = new ArrayList<Corpus_AlignmentST.Seed>();
////			al_seed.addAll(treeSeed);
//
////System.out.println(al_seed.size() + " after merge");	
//		
//		Seed s = expandSeedsForPatternSimilarity(al_seed, aa1, aa2, aa1.getNumberOfAnnotationColumns(), sim, p1, p2);
//
////chrono = java.lang.System.currentTimeMillis();
////SABRE.tempsExpandSeeds += (chrono - chrono2);
//
////System.out.println(((Double)SABRE.tempsSim).intValue() + " - " + ((Double)SABRE.tempsGetSeeds).intValue() + " : " + ((Double)SABRE.tempsMergeSeeds).intValue() + " : " + ((Double)SABRE.tempsExpandSeeds).intValue());
////		
//////System.out.println("pen non covered: " + (Math.max(this.cAE.size(), p.cAE.size()) - annotationsCoverd));			
////System.out.println("an1 non covered: " + ((aa1.getNumberOfLines() - (s.l1_max - s.l1_min + 1)) * param.gap_cost/2.0));
////System.out.println("an2 non covered: " + ((aa2.getNumberOfLines() - (s.l2_max - s.l2_min + 1)) * param.gap_cost/2.0));		
////
////System.out.println("cae1 size : " + p1.cAE.size());
////System.out.println("cae2 size : " + p2.cAE.size());
////System.out.println("seed size : " + s.couples.size());
//////System.out.println("seed annotations covered: " + annotationsCoverd);
////System.out.println("ae1 size : " + aa1.getNumberOfLines());
////System.out.println("ae2 size : " + aa2.getNumberOfLines());
////System.out.println("seed l1m: " + s.l1_min);
////System.out.println("seed l1M: " + s.l1_max);
////System.out.println("seed l2m: " + s.l2_min);
////System.out.println("seed l2M: " + s.l2_max);
////System.out.println("score: " + s.score); 
////
////			System.out.println("final score: " + s.score + "\n=============");
//		
//
//		aa1 = null;
//		aa2 = null;
//		seed_covering = null;
//		sim = null;
//		
//		return (int) s.score;
	}
	
	public SABREParameter getParam(){
		return param;
	}

	@Override
	public void removeObserver() {
		listObserver = new ArrayList<SABREObserver>();		
	}
	
	public void notifyObserversParameters(){
		for(SABREObserver o : listObserver){
			o.updateSABREParameters();
		}
	}

	@Override
	public void addObserver(Observer obs) {

		if (listObserver == null) {
			listObserver = new ArrayList<SABREObserver>();
		}

		listObserver.add((SABREObserver)obs);
		
	}

	public void removeObserver(SABREObserver obs) {
	
		int id = listObserver.indexOf(obs);
		
		if(id != -1)
			listObserver.remove(obs);
		
	}

}
