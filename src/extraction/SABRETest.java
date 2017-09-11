package extraction;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import model.AnnotatedArray;

public class SABRETest {
	static PositiveScoreTable st;
	static String fo_path = "data/Tests/tests_sabre_similarities/";
	static String stPath = "scores_a_b.csv";
	
	static ArrayList<AnnotatedArray> al_aa;
	
	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//		st = new PositiveScoreTable(fo_path + stPath);
//		al_aa = defineAA();
//		Corpus.getCorpus().setAnnotationSimilarities(st);
//
//	}
//	
//	/**
//	 * 	AA1    AA2
//		a a    a a
//		a a    b b
//		b b		
//		
//		The method should extract 2 identical patterns :
//		a a
//		b b
//		
//		The similarity between those two patterns should be 8 (4 annotation couples with a score of 2)
//	 */
//	@Test
//	public void alignmentAA1_AA2_and_similarityOfThePattern(){
//		
//		double scoreMin = 8;
//		double min_seed = 4;
//		double desynch = 1;
//
//		double gap = 2*desynch;
//		SABRE.getInstance().setParam(new SABREParameter(scoreMin, min_seed, gap, desynch)); 
//		
//		ArrayList<Pattern> al_p = SABRE.getInstance().align(al_aa.get(0), al_aa.get(1));
//		
//		assertEquals(2, al_p.size());
//		assertEquals(8, SABRE.getInstance().similarity(al_p.get(0), al_p.get(1)), 0.01);
//		
//	}
//	
//	/**
//	 * 	AA1   AA3
//		a a   a a
//		a a   a a
//		b b   - -
//		      b b		
//		
//		The method should extract 2 patterns :
//		a a  a a
//		a a  a a
//		b b  
//		     b b
//		
//		The similarity between these two patterns should be 10 (6 annotation couples with a score of 2 minus one desynchronization of cost 1)
//	 */
//	@Test
//	public void alignmentAA1_AA3_and_similarityOfThePattern(){
//		
//		double scoreMin = 10;
//		double min_seed = 4;
//		double desynch = 1;
//
//		double gap = 2*desynch;
//		SABRE.getInstance().setParam(new SABREParameter(scoreMin, min_seed, gap, desynch)); 
//		
//		
//		
//		ArrayList<Pattern> al_p = SABRE.getInstance().align(al_aa.get(0), al_aa.get(2));
//		
//		assertEquals(2, al_p.size());
//		assertEquals(11, SABRE.getInstance().similarity(al_p.get(0), al_p.get(1)), 0.01);
//		
//	}
//	
//	/**
//	 * 	Third annotated array
//		a a
//		a a
//		- -
//		b b
//		
//		The method should extract 2 identical patterns :
//		a a
//		a a
//		- -
//		b b
//		
//		The similarity between the two patterns must be 10 (6 annotation couples with a score of 2 minus one gap of cost 2)
//	 */
//	@Test
//	public void alignmentAA3_AA3_and_similarityOfThePattern(){
//		
//		double scoreMin = 10;
//		double min_seed = 4;
//		double desynch = 1;
//
//		double gap = 2*desynch;
//		SABRE.getInstance().setParam(new SABREParameter(scoreMin, min_seed, gap, desynch)); 
//		
//		ArrayList<Pattern> al_p = SABRE.getInstance().align(al_aa.get(2), al_aa.get(2));
//		
//		assertEquals(2, al_p.size());
//		assertEquals(10, SABRE.getInstance().similarity(al_p.get(0), al_p.get(1)), 0.01);
//		
//	}
//	
//	/**
//	 * 	P4   P3
//		a a
//		a a
//		
//		a a  a a
//		a a  a a
//	  
//		b b  b b
//		
//		The similarity between the two patterns must be 10 (6 annotation couples with a score of 2 
//															minus one gap -> cost 2 * desynch
//															minus 3 non covered lines in P5 -> cost 3 * desynch
//															minus 4 non covered annotations in P5 -> cost 4 * desynch)
//	 */
//	@Test
//	public void similarityP3_P4(){
//		
//		double scoreMin = 10;
//		double min_seed = 4;
//		double desynch = 1;
//
//		double gap = 2*desynch;
//		SABRE.getInstance().setParam(new SABREParameter(scoreMin, min_seed, gap, desynch)); 
//
//		/* Create P3 */
//		ArrayList<Coordinate<Integer>> al_c = new ArrayList<Coordinate<Integer>>();
//		al_c.add(new Coordinate<Integer>(0, 0));
//		al_c.add(new Coordinate<Integer>(0, 1));
//		al_c.add(new Coordinate<Integer>(1, 0));
//		al_c.add(new Coordinate<Integer>(1, 1));
//		al_c.add(new Coordinate<Integer>(3, 0));
//		al_c.add(new Coordinate<Integer>(3, 1));
//		Pattern p3 = new Pattern(al_aa.get(2), al_c);
//
//		/* Create P4 */
//		al_c = new ArrayList<Coordinate<Integer>>();
//		al_c.add(new Coordinate<Integer>(0, 0));
//		al_c.add(new Coordinate<Integer>(0, 1));
//		al_c.add(new Coordinate<Integer>(1, 0));
//		al_c.add(new Coordinate<Integer>(1, 1));
//		al_c.add(new Coordinate<Integer>(3, 0));
//		al_c.add(new Coordinate<Integer>(3, 1));
//		al_c.add(new Coordinate<Integer>(4, 0));
//		al_c.add(new Coordinate<Integer>(4, 1));
//		al_c.add(new Coordinate<Integer>(6, 0));
//		al_c.add(new Coordinate<Integer>(6, 1));
//		Pattern p4 = new Pattern(al_aa.get(3), al_c);
//		
//		assertEquals(3, SABRE.getInstance().similarity(p3,p4), 0.01);
//		
//	}

//	private static ArrayList<AnnotatedArray> defineAA() {
//
//		ArrayList<AnnotatedArray> result = new ArrayList<AnnotatedArray>(); 
//		
//		ArrayList<ArrayList<Short>> currentAA;
//		ArrayList<Short> currentLine;
//
//		try {
//			
//			// First annotated array
//			// a a
//			// a a
//			// b b		
//			currentAA = new ArrayList<ArrayList<Short>>();
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//	
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//	
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 2);
//			currentLine.add((short) 2);
//			currentAA.add(currentLine);
//		
//			result.add(new AnnotatedArray(currentAA));
//			
//			//Second annotated array
//			// a a
//			// b b		
//			currentAA = new ArrayList<ArrayList<Short>>();
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//	
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 2);
//			currentLine.add((short) 2);
//			currentAA.add(currentLine);
//		
//			result.add(new AnnotatedArray(currentAA));
//			
//			//Third annotated array
//			// a a
//			// a a
//			// - - 
//			// b b		
//			currentAA = new ArrayList<ArrayList<Short>>();
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 0);
//			currentLine.add((short) 0);
//			currentAA.add(currentLine);
//	
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 2);
//			currentLine.add((short) 2);
//			currentAA.add(currentLine);
//		
//			result.add(new AnnotatedArray(currentAA));
//		
//			
//			//Fourth annotated array
//			// a a
//			// a a
//			// - -
//			// a a
//			// a a
//			// - -
//			// b b
//			currentAA = new ArrayList<ArrayList<Short>>();
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 0);
//			currentLine.add((short) 0);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 1);
//			currentLine.add((short) 1);
//			currentAA.add(currentLine);
//			
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 0);
//			currentLine.add((short) 0);
//			currentAA.add(currentLine);
//
//			currentLine = new ArrayList<Short>();
//			currentLine.add((short) 2);
//			currentLine.add((short) 2);
//			currentAA.add(currentLine);
//		
//			result.add(new AnnotatedArray(currentAA));
//			
//			
//			
//		} catch (InvalidAnnotatedArrayNumberOfColumnsException e) {
//			e.printStackTrace();
//		}
//		
//		
//		
//		return result;
//		
//	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		st = null;
		al_aa = null;
	}


}
