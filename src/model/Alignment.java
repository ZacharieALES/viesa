package model;

/**
 * An alignment corresponds to two patterns extracted together
 * @author zach
 *
 */
/**
 * @author zach
 *
 */
public class Alignment {
	
	private Pattern p1;
	private Pattern p2;
	
	private double score;
	
	public Alignment(Pattern p1, Pattern p2, double score){
		this.p1 = p1;
		this.p2 = p2;
		this.score = score;
	}

	public Pattern getP1() {
		return p1;
	}

	public void setP1(Pattern p1) {
		this.p1 = p1;
	}

	public Pattern getP2() {
		return p2;
	}

	public void setP2(Pattern p2) {
		this.p2 = p2;
	}

	public double getScore() {
		return score;
	}

}
