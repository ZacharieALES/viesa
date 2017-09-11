//Copyright (C) 2012 Zacharie ALES and Rick MORITZ
//
//This file is part of Viesa.
//
//Viesa is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Viesa is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with Viesa.  If not, see <http://www.gnu.org/licenses/>.

package clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.Corpus;
import model.Pattern;

/**
 * Set of clusters which represent a partition of all the patterns in a CorpusAlignment
 * @author zach
 *
 */
public class ClusterSet implements Serializable{

	private static final long serialVersionUID = -6595032040866280529L;
	public List<Cluster> clusters;
	public boolean representsExtractedAlignments = false;

	public ClusterSet(List<Cluster> clusters, boolean representsExtractedAlignments){
		this.clusters = clusters;
		this.representsExtractedAlignments = representsExtractedAlignments;
	}
	
	public ClusterSet(List<Cluster> clusters){
		this(clusters, false);
	}
	
	public ClusterSet(ClusterSet model){
		clusters = new ArrayList<Cluster>(model.clusters);
		this.representsExtractedAlignments = model.representsExtractedAlignments;
	}
	public Cluster get(int i){		
		return clusters.get(i);
	}
	
	public Pattern get(int c_i, int p_j){
		return clusters.get(c_i).get(p_j);
	}
	
	public List<Cluster> getAll(){
		return clusters;
	}
	
	public int size(){
		return clusters.size();
	}
	
	/**
	 * Find the cluster which contain pattern p
	 * @param p Searched pattern
	 * @return The cluster index if p is in the ClusterSet ; -1 otherwise
	 */
	public int getClusterIndex(Pattern p){
		
		boolean found = false;
		int i = clusters.size() - 1;
		
		while(!found && i >= 0 ){
			
			if(clusters.get(i).indexOf(p) != -1){
				found = true;
			}
			
			if(!found)
				i--;
		}
		
		return i;
	}
	
	public Cluster getCluster(int i){
		return clusters.get(i);
	}
	
	/**
	 * Compute the Dunn index of the cluster set
	 * 
	 * @return
	 */
	public double dunnIndex(ClusterSet cs) {

		int result;
		double max_intra = 0;
		double min_inter = Integer.MAX_VALUE;

		double t1, mint1 = 1000000;

		/* For each cluster */
		for (int i = 0; i < cs.size(); i++) {

			Cluster c1 = cs.getCluster(i);
			/* For each pair of elements of the cluster */
			for (int j = 0; j < c1.size(); j++)
				for (int k = j + 1; k < c1.size(); k++) {
					double diss = Corpus.getCorpus().similarity(c1.get(j), c1.get(k)) + 1;
					if (diss > max_intra)
						max_intra = diss;
				}

			/* For each pair of clusters */
			for (int j = i + 1; j < cs.size(); j++) {

				t1 = 0;
				Cluster c2 = cs.getCluster(j);

				/* For each pair of elements in c1 and c2 */
				for (int k = 0; k < c1.size(); k++) {
					for (int l = 0; l < c2.size(); l++) {
						double diss = Corpus.getCorpus().similarity(c1.get(k), c2.get(l)) + 1;
						t1 += diss;
						if (diss < min_inter && diss > 10) {
							min_inter = diss;
						}
					}
				}
				t1 /= c1.size() * c2.size();
				if (t1 < mint1)
					mint1 = t1;
			}

		}

//		System.out.println("min inter : " + min_inter);
//		System.out.println("max intra : " + max_intra);

		result = (int) (min_inter / (max_intra+1E-7));
		return result;

	}
	
	
}
