package clustering.exact_resolution;

import clustering.AbstractClusteringMethod;
import clustering.ClusteringSolution;
import model.Corpus;

public class CuttingPlane extends AbstractClusteringMethod
{

	@Override
	public ClusteringSolution cluster() {

		int n = Corpus.getCorpus().getPatternSize();
		
		double[][] sim = new double[n][n];
		
		for(int j = 0 ; j < n ; ++j){
			for (int k = j+1; k < n; k++){
				sim[j][k] = Corpus.getCorpus().similarity(j, k);
				sim[k][j] = sim[j][k];
			}
		}
		
		
		return null;
	}

}
