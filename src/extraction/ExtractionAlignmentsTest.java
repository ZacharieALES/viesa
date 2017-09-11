package extraction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import model.Alignment;

public class ExtractionAlignmentsTest {
	
	@Test
	public void orderOfAlignments(){
		
		SABRE.getInstance().setParam(new SABREParameter(0, 0));
		
		ExtractionAlignments ea = new ExtractionAlignments();
		
		/* Set the desired number of alignments to 100 */
		SABRE.getInstance().getParam().desired_number_of_alignments = 100;

		ea.addAlignment(new Alignment(null, null, 1));
		ea.addAlignment(new Alignment(null, null, 4));
		ea.addAlignment(new Alignment(null, null, 2));
		ea.addAlignment(new Alignment(null, null, 3));		
		ea.addAlignment(new Alignment(null, null, 0));
		ea.addAlignment(new Alignment(null, null, 5));
		ea.addAlignment(new Alignment(null, null, 0));
		ea.addAlignment(new Alignment(null, null, 10));
		ea.addAlignment(new Alignment(null, null, 0));
		ea.addAlignment(new Alignment(null, null, 2));
		ea.addAlignment(new Alignment(null, null, 0));
		ea.addAlignment(new Alignment(null, null, -20));

		/* Test the number of alignments */
		assertEquals(12, ea.currentNumberOfAlignments);
		
		/* Test the order and organization of the alignments */
		assertEquals(-20, ea.getAlignments().get(0).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(0).size());

		assertEquals(0, ea.getAlignments().get(1).get(0).getScore(), 0.01);
		assertEquals(4, ea.getAlignments().get(1).size());

		assertEquals(1, ea.getAlignments().get(2).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(2).size());

		assertEquals(2, ea.getAlignments().get(3).get(0).getScore(), 0.01);
		assertEquals(2, ea.getAlignments().get(3).size());

		assertEquals(3, ea.getAlignments().get(4).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(4).size());

		assertEquals(4, ea.getAlignments().get(5).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(5).size());

		assertEquals(5, ea.getAlignments().get(6).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(6).size());

		assertEquals(10, ea.getAlignments().get(7).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(7).size());
		
		assertEquals(-Double.MAX_VALUE, ea.minimumScore, 0.01);

	}
	
	@Test
	public void exceedMaxNumberOfAlignments(){
		
		/* Part 1 add alignment */
		SABRE.getInstance().setParam(new SABREParameter(0, 0));
		ExtractionAlignments ea = new ExtractionAlignments();
		SABRE.getInstance().getParam().desired_number_of_alignments = 4;

		ea.addAlignment(new Alignment(null, null, 40));
		ea.addAlignment(new Alignment(null, null, 30));
		ea.addAlignment(new Alignment(null, null, 20));
		ea.addAlignment(new Alignment(null, null, 10));		
		ea.addAlignment(new Alignment(null, null, 10));

		/* Test the number of alignments */
		assertEquals(5, ea.currentNumberOfAlignments);
		
		/* Test the order and organization of the alignments */
		assertEquals(10, ea.getAlignments().get(0).get(0).getScore(), 0.01);
		assertEquals(2, ea.getAlignments().get(0).size());

		assertEquals(20, ea.getAlignments().get(1).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(1).size());

		assertEquals(30, ea.getAlignments().get(2).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(2).size());

		assertEquals(40, ea.getAlignments().get(3).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(3).size());
		
		/* Test the value of the minimum score */
		assertEquals(10.0, ea.minimumScore, 0.01);
		
		
		/* Exceed the number of alignments */
		ea.addAlignment(new Alignment(null, null, 30));
		

		/* Test the number of alignments */
		assertEquals(4, ea.currentNumberOfAlignments);
		
		/* Test the order and organization of the alignments */
		assertEquals(20, ea.getAlignments().get(0).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(0).size());

		assertEquals(30, ea.getAlignments().get(1).get(0).getScore(), 0.01);
		assertEquals(2, ea.getAlignments().get(1).size());

		assertEquals(40, ea.getAlignments().get(2).get(0).getScore(), 0.01);
		assertEquals(1, ea.getAlignments().get(2).size());
		
		/* Test the value of the minimum score */
		assertEquals(20.0, ea.minimumScore, 0.01);

	}

}
