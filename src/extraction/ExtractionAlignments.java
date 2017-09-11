package extraction;

import java.util.ArrayList;
import java.util.List;

import model.Alignment;
import model.Pattern;

/**
 * Represent the result of an extraction methods.
 * It contains alignments. When the number of alignments exceed <maxAlignments> the alignments of lowest score are removed.
 * 
 * Note: More than <maxAlignments> may appear if there are several alignments of minimal score and removing them lead to a list of less than <maxAlignments> alignments
 * @author zach
 *
 */
public class ExtractionAlignments {
	/**
	 * List of list of alignments.
	 * Each list correspond to a set of alignments of equal score.
	 * The list of alignments are ordered by increasing score (so that the alignments of lowest scores can be removed easily).
	 */
	public ArrayList<ArrayList<Alignment>> alignments;
	
	/**
	 * Current number of alignments in the list
	 */
	int currentNumberOfAlignments;

	/** Minimum score of an alignment in the ArrayList <alignments> */
	double minimumScore;
	
	public ExtractionAlignments(){
		alignments = new ArrayList<ArrayList<Alignment>>();
		currentNumberOfAlignments = 0;
		minimumScore = -Double.MAX_VALUE;
	}

	
	public ExtractionAlignments(double minimumScore){
		alignments = new ArrayList<ArrayList<Alignment>>();
		currentNumberOfAlignments = 0;
		this.minimumScore = minimumScore;
	}
	
	public void addAlignments(ArrayList<Alignment> toAdd){
		
		for(Alignment a : toAdd)
				addAlignment(a);
		
	}
	
	public void addAlignment(Alignment toAdd){

		/* If the alignment has a score high enough to be added in the list */ 
		if(toAdd.getScore() >= minimumScore){
			
			int indexToInsert = 0;
			boolean positionFound = false;
			
			while(!positionFound && indexToInsert < alignments.size()){
				
				double currentScore = alignments.get(indexToInsert).get(0).getScore();
				
				/* If the score of the alignments at position <indexToInsert> is greater than the one of <toAdd> */
				if(currentScore > toAdd.getScore()){
					
					/* Add the alignment at the previous position in a new ArrayList */
					ArrayList<Alignment> new_al = new ArrayList<Alignment>();
					new_al.add(toAdd);
					
					alignments.add(Math.max(0, indexToInsert), new_al);
					this.currentNumberOfAlignments++;
					positionFound = true;
				}
				
				/* If the score of the alignments at position <indexToInsert> is equal to the one of <toAdd> */
				else if(currentScore == toAdd.getScore()){
					
					/* Add the alignments at this position and don't create a new ArrayList */
					alignments.get(indexToInsert).add(toAdd);
					this.currentNumberOfAlignments++;
					positionFound = true;
				}
				else
					indexToInsert++;
				
			}
			
			if(!positionFound){
				
				/* Add the alignment at the end in a new ArrayList */
				ArrayList<Alignment> new_al = new ArrayList<Alignment>();
				new_al.add(toAdd);
				
				alignments.add(new_al);
				this.currentNumberOfAlignments++;
				
			}
			
			/* If the current number of alignments is higher than the number of alignments desired by the user */
			if(SABRE.getInstance().getParam().desired_number_of_alignments > 0 && currentNumberOfAlignments > SABRE.getInstance().getParam().desired_number_of_alignments){
				
				int numberOfRemovedAlignments = alignments.get(0).size();
				
				/* If removing the alignments of lowest score does not lead to less than <desired_number_of_alignments> alignments */
				if(currentNumberOfAlignments - numberOfRemovedAlignments >= SABRE.getInstance().getParam().desired_number_of_alignments){
					currentNumberOfAlignments -= alignments.get(0).size();
					alignments.remove(0);
				}
				
			}
			
			/* If the number of desired alignments is limited and if this limit is reached */
			if(SABRE.getInstance().getParam().desired_number_of_alignments != 0 && currentNumberOfAlignments >= SABRE.getInstance().getParam().desired_number_of_alignments){
				minimumScore = alignments.get(0).get(0).getScore();
//				System.out.println("EA New min score: " + minimumScore);
			}
			
		}
		
	}
	
	public ArrayList<ArrayList<Alignment>> getAlignments(){
		return alignments;
	}

	public List<Pattern> getPatterns() {

		List<Pattern> result = new ArrayList<Pattern>();
		
		for(ArrayList<Alignment> al_a : alignments)
			for(Alignment a : al_a){
				result.add(a.getP1());
				result.add(a.getP2());
			}
		
		return result;
	}

	public double getMinimumScore() {
		return minimumScore;
	}

	public void setMinimumScore(double minimumScore) {
		this.minimumScore = minimumScore;
	}


	public void addAlignments(ExtractionAlignments align) {
		
		for(ArrayList<Alignment> al_a : align.alignments)
			for(Alignment a : al_a)
				addAlignment(a);
	}


	public int getCurrentNumberOfAlignments() {
		return currentNumberOfAlignments;
	}


}
