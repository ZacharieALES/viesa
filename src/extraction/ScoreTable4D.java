package extraction;

import model.Position;
import model.Position4D;

/**
 * Three dimension matrix used to compute the local similarity between two AnnotatedElement. It's edges are initialized to 0
 * @author zach
 *
 */
public class ScoreTable4D {
	private double[][][][] tS;
	
	public ScoreTable4D(int s1, int s2, int s3, int s4){
		
		tS = new double[s1][s2][s3][s4];
		
		/* tS edges initialization */
		for (int j = 0 ; j < s2 ; j++)
			for (int k = 0 ; k < s3 ; k++)
				for (int l = 0 ; l < s4 ; l++)
					tS[0][j][k][l] = 0;
		
		/* bis */
		for (int i = 0 ; i < s1 ; i++)
			for (int k = 0 ; k < s3 ; k++)
				for (int l = 0 ; l < s4 ; l++)
					tS[i][0][k][l] = 0;
		
		/* ter */
		for (int i = 0 ; i < s1 ; i++)
			for (int j = 0 ; j < s2 ; j ++)
				for (int l = 0 ; l < s4 ; l++)
					tS[i][j][0][l] = 0;
		
		/* quater ? */
		for (int i = 0 ; i < s1 ; i++)
			for (int j = 0 ; j < s2 ; j ++)
				for (int k = 0 ; k < s3 ; k++)
					tS[i][j][k][0] = 0;
		
	}
	
	public double get(Position4D pos){
		return tS[pos.getI()][pos.getJ()][pos.getK()][pos.getL()];		
	}
	
	public double get(int i, int j, int k, int l){
		return tS[i][j][k][l];
	}

	public void set(Position4D pos, double i){
		tS[pos.getI()][pos.getJ()][pos.getK()][pos.getL()] = i;
	}
	
	public void set(int i, int j, int k, int l, double value){
		tS[i][j][k][l] = value;
	}
	
	
}

