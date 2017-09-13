package extraction;

public class SABREParameter {
	
	public SABREParameter(double gap_cost,
			double desynch_cost, int desiredNumberOfAlignments) {
		super();
		this.gap_cost = gap_cost;
		this.desynch_cost = desynch_cost;
		this.desired_number_of_alignments = desiredNumberOfAlignments;
	}

	/** Minimal score above which a diagonal is considered to be a seed */
	public double min_seed_score;
		
	/** Cost of one gap */
	public double gap_cost;
	
	/** Cost of one desynchronization */
	public double desynch_cost;
	
	/** Number of alignments desired by the user */
	public int desired_number_of_alignments = 0;

}
