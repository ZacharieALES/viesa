package extraction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import model.Alignment;
import model.AnnotatedArray;
import model.AnnotationColumn;
import model.Pattern;
import model.PointXMLSerializable;
import model.Position;

/**
 * Extract alignments between two annotation arrays.
 * @author zach
 *
 */
public class LPCA {

	private AnnotatedArray aa1;
	private AnnotatedArray aa2;

	private Double extractionInsDelScore;
	private Double unwantedSubstitution;

	/**
	 * Store the alignments extracted from <aa1> and <aa2> when the method
	 * <align> is used
	 */
	private ExtractionAlignments alignments;

	/**
	 * For a couple of AnnotatedArray, save the columnScoreMatrix when computing
	 * cS in order to avoid recomputing them when tracing back
	 */
	transient private ArrayList<double[][]> columnScoreMatrix = new ArrayList<double[][]>();

	/*
	 * rS[i][j][k] contains the distance between - prefix of size j+1 of row i
	 * of ae1 and - prefix of size j+1 of row k of ae2
	 */
	transient private double[][][] rS;

	/*
	 * cS[i][j][k] contains the distance between - prefix of size i+1 of column
	 * j of ae1 and - prefix of size k+1 of column j of ae2
	 */
	transient private double[][][] cS;

	/*
	 * tS.get(i, j, k, l) contains the local distance from ae1[i-1,j-1] and
	 * ae2[k-1,l-1]
	 */
	transient private ScoreTable tS;

	/*
	 * tSMemory.get(i, j, k) contains an integer from -1 to 8 which represent
	 * the path followed to obtain ts[i][j][k]
	 * 
	 * -1 : already explored path during the traceback 0 : end of path 1 :
	 * tS.get(i-1, j , k ) 3 : tS.get(i , j , k-1) 5 : tS.get(i-1, j , k-1) 6 :
	 * tS.get(i , j-1, k ) 7 : tS.get(i-1, j-1, k-1) (substitution de ligne puis
	 * de colonne) 8 : tS.get(i-1, j-1, k-1) (substitution de colonne puis de
	 * ligne)
	 */
	transient private ScoreTable tSMemory;

	/** LPCA is a singleton */
	private LPCA(){}
	
	/**
	 * Holder used to implement the singleton design pattern 
	 * @author zach
	 *
	 */
	private static class LPCAHolder {

		private final static LPCA sabre = new LPCA();

	}

	public static LPCA getInstance() {
		return LPCAHolder.sabre;
	}

	private void computeR() {

		rS = new double[aa1.getNumberOfLines()][aa1.getNumberOfAnnotationColumns()][aa2.getNumberOfLines()];

		/* For each couple of lines in ae1 and ae2 */
		for (int i = 0; i < aa1.getNumberOfLines(); i++) {
			for (int k = 0; k < aa2.getNumberOfLines(); k++) {

				double[][] scoreM = computeLocalScoreMatrixForRows(getLine(aa1,i), getLine(aa2, k));
//				int[][] scoreM = computeLocalScoreMatrix(aa1
//						.getAnnotations().get(i), aa2.getAnnotations()
//						.get(k));

				/* For each column */
				for (int j = 0; j < aa1.getNumberOfAnnotationColumns(); j++)
					rS[i][j][k] = scoreM[j + 1][j + 1];
			}
		}
	}

	private List<Short> getLine(AnnotatedArray aa1, int i) {
		
		List<Short> result = new ArrayList<Short>();

		for (AnnotationColumn c : aa1.getAnnotationColumn())
			result.add(c.getValues().get(i));

		return result;
	}

	private void computeC() {

		cS = new double[aa1.getNumberOfLines()][aa1.getNumberOfAnnotationColumns()][aa2.getNumberOfLines()];

		/* For each column */
		for (int j = 0; j < aa1.getNumberOfAnnotationColumns(); j++) {

			double[][] simM = computeLocalScoreMatrixForColumns(aa1.getAnnotations().get(j).getValues(),
					aa2.getAnnotations().get(j).getValues());
			// int[][] simM = computeLocalScoreMatrix(aa1.getColumn(j),
			// aa2.getColumn(j));

			columnScoreMatrix.add(j, simM);

			/* For each couple of prefix strings in these columns */
			for (int i = 0; i < aa1.getNumberOfLines(); i++)
				for (int k = 0; k < aa2.getNumberOfLines(); k++)
					cS[i][j][k] = simM[i + 1][k + 1];
		}
	}

	private void computeT() {

		tSMemory = new ScoreTable(aa1.getNumberOfLines() + 1, aa1.getNumberOfAnnotationColumns() + 1,
				aa2.getNumberOfLines() + 1);
		tS = new ScoreTable(aa1.getNumberOfLines() + 1, aa1.getNumberOfAnnotationColumns() + 1,
				aa2.getNumberOfLines() + 1);

		/* For each other elements in tS (and tSMemory) */
		for (int i = 1; i < aa1.getNumberOfLines() + 1; i++)
			for (int j = 1; j < aa1.getNumberOfAnnotationColumns() + 1; j++)
				for (int k = 1; k < aa2.getNumberOfLines() + 1; k++) {

					{

						/* Contains the maximum score for tS[i][j][k] */
						double scoreMax = 0;

						/*
						 * Contains the indice of the better path (see tSMemory
						 * definition for further details)
						 */
						int pathMax = 0;

						double tempScore;

						/* Case 8 */
						if (cS[i - 1][j - 1][k - 1] != 0 && j != 1 && rS[i - 1][j - 2][k - 1] != 0)
							tempScore = tS.get(i - 1, j - 1, k - 1) + cS[i - 1][j - 1][k - 1] + rS[i - 1][j - 2][k - 1];
						else {
							tempScore = tS.get(i - 1, j - 1, k - 1) + 2 * extractionInsDelScore;
							/*
							 * Used if insertion and deletion costs are
							 * different
							 */
							// tempScore = tS.get(i-1, j-1, k-1, l-1) +
							// delScore + insScore;
						}

						if (tempScore > scoreMax) {
							scoreMax = tempScore;

							/* Case 7 and 8 give the same path */
							pathMax = 7;
						}

						/* Case 7 */
						if (i != 1 && k != 1 && cS[i - 2][j - 1][k - 2] != 0 && rS[i - 1][j - 1][k - 1] != 0)
							tempScore = tS.get(i - 1, j - 1, k - 1) + cS[i - 2][j - 1][k - 2] + rS[i - 1][j - 1][k - 1];
						else {
							tempScore = tS.get(i - 1, j - 1, k - 1) + 2 * extractionInsDelScore;
							/*
							 * Used if insertion and deletion costs are
							 * different
							 */
							// tempScore = tS.get(i-1, j-1, k-1, l-1) +
							// delScore + insScore;
						}

						if (tempScore > scoreMax) {
							scoreMax = tempScore;
							pathMax = 7;
						}

						/* Case 6 */
						if (cS[i - 1][j - 1][k - 1] != 0)
							tempScore = tS.get(i, j - 1, k) + cS[i - 1][j - 1][k - 1];
						else {
							tempScore = tS.get(i, j - 1, k) + 2 * extractionInsDelScore;
							/*
							 * Used if insertion and deletion costs are
							 * different
							 */
							// tempScore = tS.get(i, j-1, k, l-1) + delScore
							// + insDelScore;
						}

						if (tempScore > scoreMax) {
							scoreMax = tempScore;
							pathMax = 6;
						}

						/* Case 5 */
						if (rS[i - 1][j - 1][k - 1] != 0)
							tempScore = tS.get(i - 1, j, k - 1) + rS[i - 1][j - 1][k - 1];
						else {
							tempScore = tS.get(i - 1, j, k - 1) + 2 * extractionInsDelScore;
							/*
							 * Used if insertion and deletion costs are
							 * different
							 */
							// tempScore = tS.get(i-1, j, k-1, l) + delScore
							// + insDelScore;
						}

						if (tempScore > scoreMax) {
							scoreMax = tempScore;
							pathMax = 5;
						}

						/*
						 * There is no case 4 and 2 because it's not possible
						 * here to add or delete a column since assumed that
						 * column j in X correspond to column j in Y
						 */

						/* Case 3 */
						tempScore = tS.get(i, j, k - 1) + extractionInsDelScore;

						if (tempScore > scoreMax) {
							scoreMax = tempScore;
							pathMax = 3;
						}

						/* Case 1 */
						tempScore = tS.get(i - 1, j, k) + extractionInsDelScore;
						/*
						 * Used if insertion and deletion costs are different
						 */
						// tempScore = tS.get(i-1, j, k, l) + delScore;

						if (tempScore > scoreMax) {
							scoreMax = tempScore;
							pathMax = 1;
						}

						tS.set(i, j, k, scoreMax);
						tSMemory.set(i, j, k, pathMax);

					}

				}

	}

	public ExtractionAlignments align(AnnotatedArray aa1, AnnotatedArray aa2, double minimum_score) {

		if (SABRE.getInstance().getParam() != null) {

			alignments = new ExtractionAlignments(minimum_score);

			this.aa1 = aa1;
			this.aa2 = aa2;

			extractionInsDelScore = -SABRE.getInstance().getParam().desynch_cost;
			unwantedSubstitution = 2 * extractionInsDelScore;

			computeR();
			computeC();

			computeT();

			traceback();

			dealloc();

			return alignments;
		} else {
			System.err.println("Error in LPCA.java, at least one parameter have not been initialized");
			return null;
		}

	}
	
	/**
	 * Compute the matrix of local score between two strings (size of both
	 * string inputs must be > 0 otherwise return new int[0][0])
	 * 
	 * @param al1
	 *            1st string (size l1)
	 * @param al2
	 *            2nd string (size l2)
	 * @return A matrix 'd' of size (l1+1) x (l2+1) such that C[i+1][j+1] is the
	 *         local score between s1[i] and s2[j]
	 */
	private double[][] computeLocalScoreMatrixForRows(List<Short> al1, List<Short> al2) {

		int size1 = al1.size();
		int size2 = al2.size();

		if (size1 != 0 && size2 != 0) {

			double c_ins, c_sub, c_del;

			double[][] score = new double[size1 + 1][size2 + 2];

			for (int i = 0; i <= size1; i++)
				score[i][0] = 0;

			for (int i = 1; i <= size2; i++)
				score[0][i] = 0;

			/* for each position in s1 */
			for (int i = 1; i <= size1; i++) {

				/* for each position in s2 */
				for (int j = 1; j <= size2; j++) {

					c_ins = score[i][j - 1] + extractionInsDelScore;
					c_sub = score[i - 1][j - 1]
							+ similarity(al1.get(i - 1), al2.get(j - 1));
					c_del = score[i - 1][j] + extractionInsDelScore;

					score[i][j] = Math.max(Math.max(Math.max(c_ins, c_sub), c_del), 0.0);

				}

			}

			return score;

		} else {

			System.out.println("s1 or s2 is empty\n\t s1 : " + al1 + "\n\t s2 : " + al2);
			return new double[0][0];

		}
	}

	/**
	 * Compute the matrix of local score between two strings (size of both
	 * string inputs must be > 0 otherwise return new int[0][0])
	 * 
	 * @param al1
	 *            1st string (size l1)
	 * @param al2
	 *            2nd string (size l2)
	 * @param isDifferentLocutors
	 * @return A matrix 'd' of size (l1+1) x (l2+1) such that C[i+1][j+1] is the
	 *         local score between s1[i] and s2[j]
	 */
	private double[][] computeLocalScoreMatrixForColumns(List<Short> al1, List<Short> al2) {

		int size1 = al1.size();
		int size2 = al2.size();

		if (size1 != 0 && size2 != 0) {

			double c_ins, c_sub, c_del;

			double[][] score = new double[size1 + 1][size2 + 2];

			for (int i = 0; i <= size1; i++)
				score[i][0] = 0;

			for (int i = 1; i <= size2; i++)
				score[0][i] = 0;

			/* for each position in s1 */
			for (int i = 1; i <= size1; i++) {
				
//				if(!isAE1LineExpert && MainDemo.IS_DEMO)
//					System.out.println("LPCA: 3: Not expert in Demo");
				
				/* for each position in s2 */
				for (int j = 1; j <= size2; j++) {

					c_ins = score[i][j - 1] + extractionInsDelScore;

//					if(!isAE2LineExpert && MainDemo.IS_DEMO)
//						System.out.println("LPCA: 4: Not expert in Demo");
					
					c_sub = score[i - 1][j - 1] + similarity(al1.get(i - 1), al2.get(j - 1));
					
					c_del = score[i - 1][j] + extractionInsDelScore;

					score[i][j] = Math.max(Math.max(Math.max(c_ins, c_sub), c_del), 0.0);

				}

			}

			return score;

		} else {

			System.out.println("s1 or s2 is empty\n\t s1 : " + al1 + "\n\t s2 : " + al2);
			return new double[0][0];

		}
	}

	public double similarity(Short a, Short b) {

		double v = AnnotationColumn.pst.get(a, b);

		if (v <= 0.0)
			return unwantedSubstitution;
		else
			return v;

	}

	private void traceback() {

		TreeSet<Position> tracebackSeeds = getTSCompatiblePositions();

		// if(tracebackSeeds.size() > 0)
		// System.out.println(tracebackSeeds.size() + " seeds found");

		/* While there remain a valid position which have not been explored */
		while (tracebackSeeds.size() > 0) {

			Position lastPosition = tracebackSeeds.first();
			double positionScore = tS.get(lastPosition);

			// TODO voir si meilleur moyen de supprimer la premiere position
			tracebackSeeds.remove(lastPosition);
			
			if(positionScore >= alignments.minimumScore && positionScore > 0){

				/*
				 * Create a new ArrayList<Position> and remove the position from
				 * the seed's list
				 */
				ArrayList<Position> positions = new ArrayList<Position>();
				positions.add(lastPosition);

				// System.out.println("Number of remaining seeds : " +
				// tracebackSeeds.size());

				boolean isPathValid = true;
				boolean isPathOver = false;

				while (isPathValid && !isPathOver) {

					/* If the last position in tS is valid */
					if (tSMemory.get(lastPosition) != -1) {

						/* Get all the new positions from that position in tS */
						ArrayList<Position> newPositions = getNewPositions(lastPosition);

						/*
						 * If the new positions are valid (ie : if none of the
						 * new positions equal -1 in tsMemory)
						 */
						if (newPositions != null) {
							if (newPositions.get(0).equals(lastPosition))
								isPathOver = true;
							else {
								lastPosition = newPositions.get(0);

								/*
								 * Remove the previous position in tS which is
								 * already in currentAlignment
								 */
								newPositions.remove(0);
								positions.addAll(newPositions);
							}
						} else {
							isPathValid = false;
						}
					} else {
						isPathValid = false;
					}
				}
	
				/* Set all the position of the current path has already explored */
				for (int i = 0; i < positions.size(); i++) {
					tSMemory.set(positions.get(i), -1);
				}

				if (isPathValid) {

					ArrayList<PointXMLSerializable> c1 = new ArrayList<PointXMLSerializable>();
					ArrayList<PointXMLSerializable> c2 = new ArrayList<PointXMLSerializable>();
	
					// TODO see why I need this to avoid the repetition of the
					// 1st position
					positions.remove(0);

					for (int i = 0; i < positions.size(); i++) {
						
						Short s1 = aa1.getAnnotations().get(positions.get(i).get(1) - 1).getValues().get(positions.get(i).get(0) - 1);
						Short s2 = aa2.getAnnotations().get(positions.get(i).get(1) - 1).getValues().get(positions.get(i).get(2) - 1);
								
						if(similarity(s1, s2) > 0)
						{
							c1.add(new PointXMLSerializable(positions.get(i).get(0) - 1,
									positions.get(i).get(1) - 1));
							c2.add(new PointXMLSerializable(positions.get(i).get(2) - 1,
									positions.get(i).get(1) - 1));
						}
					}
					
					Pattern p1 = new Pattern(aa1, c1);
					Pattern p2 = new Pattern(aa2, c2);

					alignments.addAlignment(new Alignment(p1, p2, positionScore));
					// System.out.println("\tIn LPCA: number of alignments: " +
					// alignments.currentNumberOfAlignments + " min score: " +
					// alignments.minimumScore);
				}

			}
		}

	}

	private TreeSet<Position> getTSCompatiblePositions() {

		TreeSet<Position> result = new TreeSet<Position>(new Comparator<Position>() {

			/**
			 * Comparator to order the position by decreasing score. If two
			 * positions have the same score they are ordered according to their
			 * coordinates
			 * 
			 * @param p1
			 * @param p2
			 * @return -1 if p1 has a greater score in tS than p2 (or if the
			 *         score are the same and if p1 is greater than p2) ; 0 if
			 *         the position are the same ; 1 otherwise
			 */
			public int compare(Position p1, Position p2) {

				int result = -1;
				double score1 = tS.get(p1);
				double score2 = tS.get(p2);

				if (score2 > score1)
					result = 1;
				/*
				 * If the score are the same, compare the position according to
				 * their coordinates
				 */
				else if (score1 == score2)
					result = -p1.compareTo(p2);

				return result;

			}
		});

		/*
		 * For each element in tS (start with j in order to first give the
		 * solution which correspond to the maximum number of columns)
		 */
		for (int j = aa1.getNumberOfAnnotationColumns(); j >= 1; j--)
			for (int i = aa1.getNumberOfLines(); i >= 1; i--)
				for (int k = aa2.getNumberOfLines(); k >= 1; k--) {

					/* If the score is valid and better than <max> */
					if (tS.get(i, j, k) >= alignments.minimumScore) {
						Position pos = new Position(i, j, k);
						result.add(pos);
					}
				}

		return result;

	}

	/**
	 * Get an ArrayList which at index 0 contains the previous position of
	 * <pos> in the current path. The other elements represents the position of
	 * the aligned annotations obtained by going from <pos> to it's previous
	 * position
	 * 
	 * @param position
	 *            Position from which we want the previous position
	 * @return An ArrayList of positions which 1st element is the new position
	 *         in tS and the other elements are the aligned annotations (note
	 *         that the position of the 1st element will not be considered as
	 *         aligned if it does not appear a second time in the rest of the list);
	 *         or return null if the pos has already been explored
	 */
	private ArrayList<Position> getNewPositions(Position pos) {

		ArrayList<Position> result = new ArrayList<Position>();
		ArrayList<Position> tempPos = new ArrayList<Position>();
		Position temp = new Position(pos);

		switch ((int) (tSMemory.get(pos))) {
		case -1:
			result = null;
			break;

		/* If the position is a ending position, return the same position */
		case 0:
			result.add(temp);
			break;

		case 1:
			temp.setI(temp.getI() - 1);
			result.add(temp);
			break;

		/*
		 * There is no case 2 and 4 because it's not possible here to add or
		 * delete a column since assumed that column j in X correspond to column
		 * j in Y
		 */
		case 3:
			temp.setK(temp.getK() - 1);
			result.add(temp);
			break;

		case 5:
			/*
			 * Add all the annotations aligned by substitution of the ith line
			 * of X by the kth line of Y from column 0->j
			 */
			result = getRowAlignedAnnotations(temp.getI() - 1, temp.getJ() - 1, temp.getK() - 1);

			if (result != null) {

				temp.setI(temp.getI() - 1);
				temp.setK(temp.getK() - 1);

				result.add(0, temp);
			}

			break;

		case 6:
			/*
			 * Add all the annotations aligned by substitution of the jth column
			 * of X and Y from row 0->i of X and 0->k of Y
			 */
			result = getColAlignedAnnotations(temp.getI() - 1, temp.getJ() - 1, temp.getK() - 1);

			if (result != null) {

				temp.setJ(temp.getJ() - 1);
				result.add(0, temp);
			}

			break;

		case 7:
			/*
			 * Add all the annotations aligned by substitution of : - the ith
			 * line of X by the kth line of Y from column 0 to j - the jth
			 * column of X and Y from row 0 to i-1 of X and 0 to k-1 of Y
			 */
			result = getRowAlignedAnnotations(temp.getI() - 1, temp.getJ() - 1, temp.getK() - 1);

			if (result != null) {

				tempPos = getColAlignedAnnotations(temp.getI() - 2, temp.getJ() - 1, temp.getK() - 2);

				if (tempPos != null) {

					result.addAll(tempPos);

					temp.setI(temp.getI() - 1);
					temp.setJ(temp.getJ() - 1);
					temp.setK(temp.getK() - 1);

					result.add(0, temp);
				} else
					result = null;
			}
			break;

		case 8:
			/*
			 * Add all the annotations aligned by substitution of : - the jth
			 * column of X and Y from row 0->i of X and 0->k of Y - the ith line
			 * of X by the kth line of Y from column 0->j-1
			 */
			result = getColAlignedAnnotations(temp.getI() - 1, temp.getJ() - 1, temp.getK() - 1);

			if (result != null) {

				tempPos = getRowAlignedAnnotations(temp.getI() - 1, temp.getJ() - 2, temp.getK() - 1);

				if (tempPos != null) {
					result.addAll(tempPos);

					temp.setI(temp.getI() - 1);
					temp.setJ(temp.getJ() - 1);
					temp.setK(temp.getK() - 1);

					result.add(0, temp);
				}
			}

			break;

		default:
			System.err.println("The position have an invalid value in tSMemory\n Position : " + pos + "\n Value : "
					+ tSMemory.get(pos));

		}

		return result;

	}

	/**
	 * Get all the annotations locally aligned in the ith row of X and the kth
	 * row of Y from column 0->j.
	 * 
	 * @param i
	 *            line in X
	 * @param j
	 *            column in X and Y
	 * @param k
	 *            line in Y
	 * @return
	 */
	private ArrayList<Position> getRowAlignedAnnotations(int i, int j, int k) {

		ArrayList<Position> result = new ArrayList<Position>();

//		if (i != k) 
		{

			List<Short> subLine1 = getLine(aa1,i).subList(0, j + 1);
			List<Short> subLine2 = getLine(aa2,k).subList(0, j + 1);
			double[][] simM = computeLocalScoreMatrixForRows(subLine1, subLine2);
			ArrayList<PointXMLSerializable> coord = traceback1D(subLine1, subLine2, simM);

			int t = 0;
			boolean isValid = true;

			while (isValid && t < coord.size()) {

				/*
				 * If the annotation is a substitution of two annotations in the
				 * same column
				 */
				if (coord.get(t).getX() == coord.get(t).getY()) {

					Position newPos = new Position(i + 1, coord.get(t).getX() + 1, k + 1);

					if (tSMemory.get(newPos) != -1) {
						result.add(newPos);
					} else
						isValid = false;
				}

				t++;

			}

			if (!isValid) {

				for (t = 0; t < result.size(); t++) {
					tSMemory.set(result.get(t), -1);
				}
				result = null;
			}
		}

		return result;
	}

	public ArrayList<double[][]> getColumnScoreMatrix() {
		return columnScoreMatrix;
	}

	public void setColumnScoreMatrix(ArrayList<double[][]> ali_arg) {
		columnScoreMatrix = ali_arg;
	}

	/**
	 * Get all the annotations locally aligned in the jth column of X and Y from
	 * line 0 to i in X and 0 to j in Y.
	 * 
	 * @param i
	 *            line in X
	 * @param j
	 *            column in X and Y
	 * @param k
	 *            line in Y
	 * @return
	 */
	private ArrayList<Position> getColAlignedAnnotations(int i, int j, int k) {

		ArrayList<Position> result = new ArrayList<Position>();
		ArrayList<PointXMLSerializable> coord = traceback1DForColumns(
				aa1.getAnnotationColumn().get(j).getValues().subList(0, i + 1),
				aa2.getAnnotationColumn().get(j).getValues().subList(0, k + 1), columnScoreMatrix.get(j));

		int t = 0;
		boolean isValid = true;

		while (t < coord.size() && isValid) {

			Position newPos = new Position(coord.get(t).getX() + 1, j + 1, coord.get(t).getY() + 1);

			/* If the position is valid */
			if (tSMemory.get(newPos) != -1) {
				result.add(newPos);
			} else
				isValid = false;

			t++;

		}

		if (!isValid) {

			for (t = 0; t < result.size(); t++)
				tSMemory.set(result.get(t), -1);

			result = null;

		}

		return result;
	}

	private ArrayList<PointXMLSerializable> traceback1D(List<Short> al1, List<Short> al2, double[][] simM) {

		ArrayList<PointXMLSerializable> result = new ArrayList<PointXMLSerializable>();

		boolean isOver = false;
		PointXMLSerializable c = new PointXMLSerializable(al1.size(), al2.size());

		/* While the trace back is not completed */
		while (!isOver) {

			if (simM[c.getX()][c.getY()] != 0) {

				double cIns, cSub, cDel;

				cIns = simM[c.getX()][c.getY() - 1] + extractionInsDelScore;
				cSub = simM[c.getX() - 1][c.getY() - 1] + similarity(al1.get(c.getX() - 1), al2.get(c.getY() - 1));
				cDel = simM[c.getX() - 1][c.getY()] + extractionInsDelScore;

				if (simM[c.getX()][c.getY()] == cSub) {

					result.add(new PointXMLSerializable(c.getX() - 1, c.getY() - 1));
					c.setX(c.getX() - 1);
					c.setY(c.getY() - 1);

				} else if (simM[c.getX()][c.getY()] == cIns) {

					c.setY(c.getY() - 1);

				} else if (simM[c.getX()][c.getY()] == cDel) {

					c.setX(c.getX() - 1);

				} else
					System.err.println("No traceback have been found for the current position : \n\t s1 : " + al1
							+ "\n\t s2 : " + al2 + "÷\n Position (x, y) : (" + c.getX() + ", " + c.getY() + ")");
			}
			/* If the trace back is complete */
			else
				isOver = true;

		}

		return result;
	}

	private ArrayList<PointXMLSerializable> traceback1DForColumns(List<Short> al1, List<Short> al2, double[][] simM) {

		ArrayList<PointXMLSerializable> result = new ArrayList<PointXMLSerializable>();

		boolean isOver = false;
		PointXMLSerializable c = new PointXMLSerializable(al1.size(), al2.size());

		/* While the trace back is not completed */
		while (!isOver) {

			if (simM[c.getX()][c.getY()] != 0) {

				double cIns, cSub, cDel, cSub2;

				cIns = simM[c.getX()][c.getY() - 1] + extractionInsDelScore;

				cSub = simM[c.getX() - 1][c.getY() - 1] + similarity(al1.get(c.getX() - 1), al2.get(c.getY() - 1));
				cDel = simM[c.getX() - 1][c.getY()] + extractionInsDelScore;

				cSub2 = simM[c.getX() - 1][c.getY() - 1] + extractionInsDelScore;

				if (simM[c.getX()][c.getY()] == cSub) {

					result.add(new PointXMLSerializable(c.getX() - 1, c.getY() - 1));
					c.setX(c.getX() - 1);
					c.setY(c.getY() - 1);

				} else if (simM[c.getX()][c.getY()] == cIns) {

					c.setY(c.getY() - 1);

				} else if (simM[c.getX()][c.getY()] == cDel) {

					c.setX(c.getX() - 1);
				} else if (simM[c.getX()][c.getY()] == cSub2) {

					c.setX(c.getX() - 1);
					c.setY(c.getY() - 1);

				} else
					System.err.println("No traceback have been found for the current position : \n\t s1 : " + al1
							+ "\n\t s2 : " + al2 + "÷\n Position (x, y) : (" + c.getX() + ", " + c.getY() + ")");
			}
			/* If the trace back is complete */
			else
				isOver = true;

		}

		return result;
	}

	public void dealloc() {

		aa1 = null;
		aa2 = null;
		rS = null;
		cS = null;
		tS = null;
		tSMemory = null;

	}

	// public void resetExtractionInsDelScore() {
	//
	// double maximalValue = -Double.MAX_VALUE;
	//
	// double[][] values = AnnotationColumn.pst.values;
	//
	// for(int i = 0 ; i < values.length ; ++i)
	// for(int j = 0 ; j < i ; ++j)
	// if(values[i][j] > maximalValue)
	// maximalValue = values[i][j];
	//
	// extractionInsDelScore = - maximalValue;
	//
	//
	// }

}
