package clustering;

import java.util.ArrayList;

import model.Corpus;
import model.Pattern;

public class ROCK extends AbstractClusteringMethod{

	private double[][] cluster_similarity;
	private int[][] links;
	private int size;
	private int maxSim[];
	
	/* If the best similarity between cluster is equal to zero, the process is stopped */
	private boolean isOver;
	
	/**
	 * Parameter in [0,1] which corresponds to the proportion of similarities between two patterns (among all the similarities between all the couple of patterns) that will denote two neighbors.
	 * Currently we set it to 0.5.
	 * To avoid computing the median, we use instead the mean value. 
	 */
	private double theta = 0.5;
	
	/**
	 * f(theta) is equal to 1 + 2 * (1-theta) / (1+theta)
	 */
	private double f_theta;
	
	private ArrayList<ClusterSet> al_cs;
	private ArrayList<Cluster> al_c;
	
	public ROCK(){
		
		f_theta = 1 + 2 * (1-theta) / (1+theta);
	}

	/* Merge patterns.size() - 1 clusters */
	private void merge(){
		

		
		ClusterSet cs = new ClusterSet(al_cs.get(al_cs.size()-1));
		
		/* Find the closest pair of patterns p1 p2 */
		int p1 = 0;
		double bestClusterSim = cluster_similarity[p1][maxSim[p1]];

		
		for (int j = 1; j < size; j++)
			if(cluster_similarity[j][maxSim[j]] > bestClusterSim){
				p1 = j;
				bestClusterSim = cluster_similarity[j][maxSim[j]];
			}

		if( bestClusterSim > 0){
			int p2 = maxSim[p1];
			
//			System.out.println("Rock: links before merge: ");
//			for(int i = 1 ; i < size ; ++i){
//				for(int j = 0 ; j < i ; ++j){
//					System.out.print(links[i][j] + " ");
//				}
//				System.out.println();
//			}
//			
//			for(int i = 0 ; i <size ; ++i)
//				System.out.println("Best sim for " + i + " : " + cluster_similarity[i][maxSim[i]]);
//			System.out.println("Cluster merged: " + p1 + " and " + p2);
//			System.out.println("Best cluster sim: " + bestClusterSim);
	
			/*
			 * Merge clusters p1 and p2 in p1 and set an empty
			 * cluster in p2
			 */
			
	
			/* Create an arrayList of patterns with all the patterns in the cluster of p1 and p2 */
			ArrayList<Pattern> al_p = new ArrayList<Pattern>(cs.get(p1).getPatterns());
			al_p.addAll(cs.get(p2).getPatterns());
			
			/* Set this new list as the cluster of p1, and set an empty cluster for p2 */
			cs.clusters.set(p1, new Cluster(al_p));
			cs.clusters.set(p2, new Cluster(new ArrayList<Pattern>()));
			al_cs.add(cs);
	
			/* Update p1's row and column */
			for (int j = 0; j < size; j++){
				
				links[p1][j] += links[p2][j];
				links[j][p1] = links[p1][j];
	
 				cluster_similarity[p1][j] = clusterSimilarity(j, p1);
				cluster_similarity[j][p1] = cluster_similarity[p1][j];
//				System.out.println("cluster sim: " + j + " et " + p1 + " = " + cluster_similarity[p1][j]);
				
			}
	
			/* Set p2 row and column to the min value */
			for (int j = 0; j < size; j++){
				
				links[p2][j] = 0;
				links[j][p2] = 0;
				
				cluster_similarity[p2][j] = Integer.MIN_VALUE;
				cluster_similarity[j][p2] = Integer.MIN_VALUE;
			}
	
			/* Update maxSim */
			maxSim[p1] = -1;
			
			for (int j = 0; j < size; j++) {
				
				if(j != p1)
					
					/* If the best neighbor of cluster j was not p2 */
					if(maxSim[j] != p2)
						
						/* Check if p1 is better */
						updateMaxSim(j, p1);
				
					/* If the best neighbor of cluster j was p2 */
					else{
						
						/* Set p1 as the best neighbor for cluster j */
						maxSim[j] = -1;
						
						/* Check all the clusters to find the new best neighbor */ 
						for(int i = 0 ; i < size ; ++i){
							if(i != j){
								updateMaxSim(i, j);
							}	
						}
					}
			}
		}
	
		else
			isOver = true;
	
	}
	
	/* For each ClusterSet */
	private void prune(ClusterSet cs){
		/* For each Cluster */
		for (int j = cs.clusters.size() - 1; j >= 0; j--) {

			Cluster c = cs.clusters.get(j);

			/* If the cluster is empty, remove it */
			if (c.getPatterns().size() == 0)
				cs.clusters.remove(j);

		}
	}
	
	public ArrayList<ClusterSet> getCS(){
		return al_cs;
	}
	
	public int getSize(){
		return size;
	}

	@Override
	public ClusteringSolution cluster() {
		
		isOver = false;
	
		firePropertyChange("description", "", "Clustering (ROCK)");

		size = Corpus.getCorpus().getPatternSize();
		links = new int[size][size];
		cluster_similarity = new double[size][size];
		maxSim = new int[size];

		al_cs = new ArrayList<ClusterSet>();
		al_c = new ArrayList<Cluster>();
		
		progress = 0.0;
		double invMaxProgress = 1.0/(2.0*(Corpus.getCorpus().getPatternSize()<<1));
		double step;
		
		if (invMaxProgress != 0)
			step = 100 * invMaxProgress;
		else
			step = 100;
		this.setProgress(0);
		
		/* Compute the mean value */
		double meanValue = 0.0;
		for(int j = 0 ; j < size ; ++j){
			for (int k = j+1; k < size; k++){
				meanValue += Corpus.getCorpus().similarity(j, k);
			}
		}
	
		meanValue /= (size * (size - 1))/2.0;
		
//		System.out.println("ROCK : mean value : " + meanValue);

		/* Set the similarity of the patterns which are not neighbors (i.e.: if sim < meanValue) to Integer.MIN_VALUE 
		 * and initialize the links */
		
		/* Similarity of 2 clusters reduced to one pattern which are neighbors */
		double clusterSim = 2.0 / (Math.pow(2,f_theta)- 2);
		
		for(int j = 0 ; j < size ; ++j)
			maxSim[j] = -1;
		
		for(int j = 0 ; j < size ; ++j){
			
			for (int k = j+1; k < size; k++){
				if(Corpus.getCorpus().similarity(j, k) < meanValue){
					links[j][k] = 0;
					links[k][j] = 0;
					cluster_similarity[j][k] = 0.0;
					cluster_similarity[k][j] = 0.0;
					
					
				}
				else{
					links[j][k] = 1;
					links[k][j] = 1;
					cluster_similarity[j][k] = clusterSim;
					cluster_similarity[k][j] = clusterSim;
				}
				
				updateMaxSim(j, k);

			}
		}
		
		for(int i = 0 ; i < size ; ++i){
			links[i][i] = 0;
			cluster_similarity[i][i] = Integer.MIN_VALUE;
		}
		

		/* Create the first partition (it only contains clusters reduce to one pattern) */
		int i = 0;

		while (i < size	&& !isCancelled() && !isOver) {

			Pattern p = Corpus.getCorpus().getPattern(i);
			
			/* Create the cluster */
			ArrayList<Pattern> al_p = new ArrayList<Pattern>();
			al_p.add(p);
			this.al_c.add(new Cluster(al_p));
				

			this.progress(step);

			i++;
		}

		al_cs.add(new ClusterSet(al_c));
		

		/* Merge corpus.getPatterns().size() - 1 clusters */
		i = 0;

		while (i < Corpus.getCorpus().getPatternSize() - 1
				&& !isCancelled()) {

			merge();
			
			this.progress(step);

			i++;
		}

		/* For each ClusterSet */
		for (ClusterSet cs : getCS()) {
			prune(cs);
		}

		HierarchicalClusteringSolution singleLinkSolution = new HierarchicalClusteringSolution(
				getCS());

		singleLinkSolution.setMethodName("ROCK");

		return singleLinkSolution;


	}
	
	private void updateMaxSim(int j, int k) {
		
		/* If the similarity between cluster j and k is better than the best currently known for cluster j 
		 * or if the 2 similarities are exactly the same
		 * and the similarity between j and k is better than the one between j and maxSim[j]
		 */
		if(maxSim[j] == -1 || cluster_similarity[j][maxSim[j]] < cluster_similarity[j][k]
				|| (cluster_similarity[j][maxSim[j]] == cluster_similarity[j][k] 
					&&  Corpus.getCorpus().similarity(j, maxSim[j]) < Corpus.getCorpus().similarity(j, k)
			       )
		)
			maxSim[j] = k;
			
		
		/* If the similarity between clusters j and k is better than the best currently known for cluster k 
		 * or if the 2 similarities are exactly the same
		 * and the similarity between j and k is better than the one between k and maxSim[k]
		 */
		if(maxSim[k] == -1 || cluster_similarity[k][maxSim[k]] < cluster_similarity[j][k]
				|| (cluster_similarity[k][maxSim[k]] == cluster_similarity[j][k] 
					&&  Corpus.getCorpus().similarity(k, maxSim[k]) < Corpus.getCorpus().similarity(j, k)
			       )
		)
			maxSim[k] = j;
		
	}
	
	public double clusterSimilarity(int id1, int id2){
		
		double link = this.links[id1][id2];
		ClusterSet cs = al_cs.get(al_cs.size()-1);
		
		int n1 = cs.get(id1).size();
		int n2 = cs.get(id2).size();
		
		double result; 
		
		if(n1 == 0 || n2 == 0)
			result = Integer.MIN_VALUE;
		else
			result = link  / (
					Math.pow((n1 + n2), f_theta)
					- Math.pow(n1, f_theta)
					- Math.pow(n2, f_theta));
		
//System.out.println("link: " + link + " (id1,id2) (" + id1 + "," + id2 + ") (n1,n2) (" + n1 + "," + n2 + ") result = " + result);		
		return result;
	}

}
