package extraction;

import model.Position;

/**
 * Three dimension matrix used to compute the local similarity between two AnnotatedElement. It's edges are initialized to 0
 * @author zach
 *
 */
public class ScoreTable {
	private double[][][] tS;
	
	public ScoreTable(int s1, int s2, int s3){
		
		tS = new double[s1][s2][s3];
		
		/* tS edges initialization */
		for (int j = 0 ; j < s2 ; j++)
			for (int k = 0 ; k < s3 ; k++)
					tS[0][j][k] = 0;
		
		/* bis */
		for (int i = 0 ; i < s1 ; i++)
			for (int k = 0 ; k < s3 ; k++)
					tS[i][0][k] = 0;
		
		/* ter */
		for (int i = 0 ; i < s1 ; i++)
			for (int j = 0 ; j < s2 ; j ++)
					tS[i][j][0] = 0;
		
	}
	
	public double get(Position pos){
		return tS[pos.getI()][pos.getJ()][pos.getK()];		
	}
	
	public double get(int i, int j, int k){
		return tS[i][j][k];
	}

	public void set(Position pos, double i){
		tS[pos.getI()][pos.getJ()][pos.getK()] = i;
	}
	
	public void set(int i, int j, int k, double value){
		tS[i][j][k] = value;
	}
	
	
}

