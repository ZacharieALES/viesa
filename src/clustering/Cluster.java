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
import java.util.Collections;
import java.util.List;

import model.Corpus;
import model.Pattern;
import cern.colt.bitvector.BitVector;

public class Cluster implements Serializable{

	private static final long serialVersionUID = -3106192770007404456L;
	private ArrayList<Pattern> patterns;
	private BitVector bv;
	private String clusterName = null;
	
	/* A cluster is initialized if the initialize() method has
	 *  been called after the last pattern addition */ 
	private boolean isInitialized = false;
	
	public Cluster(){
		patterns = new ArrayList<Pattern>();
	}
	
	public boolean isInitialized(){
		return isInitialized;
	}
	
	public Cluster(ArrayList<Pattern> p){
		patterns = p;
	}
			
	public void add(Pattern p){
		patterns.add(p);
		isInitialized = false;
	}
	
	public void add(ArrayList<Pattern> al_p){
		for(Pattern p : al_p)
			patterns.add(p);
				
		isInitialized = false;
	}
	
	public Pattern get(int index){
		return patterns.get(index);
	}
	
	public double similarity(Pattern p1, Pattern p2){
		return Corpus.getCorpus().similarity(p1, p2);			
	}
	
	public int size(){
		return patterns.size();
	}
		

	
	public void initialize(){
		
		/* Each cell is initialized to false */
		bv = new BitVector(Corpus.getCorpus().getPatternSize());

		/* Set the cell which correspond to patterns into the cluster to yes */
		for(int i = 0 ; i < patterns.size() ; i++){
			bv.put(Corpus.getCorpus().getIndexOfPattern(Corpus.getCorpus().getPattern(i)), true);
		}
		
		isInitialized = true;
	}
	
//	/**
//	 * Get the rank of <p2> as neighbor of <p1>
//	 * @param p1
//	 * @param p2
//	 * @return
//	 */
//	public int getRank(Pattern p1, Pattern p2){
//		
//		return this.getNeighbors(p1).getIndex(p2);
//	}
//	
//	public int getRank(int i, int j){
//		return this.getRank(patterns.get(i), patterns.get(j));
//	}
	
//	/**
//	 * Represent the neighbors of a Pattern <p> sorted by increasing dissimilarity (patternSet.get(0) is the most similar pattern to <p>) 
//	 * @author zach
//	 *
//	 */
//	public class SortedPatternNeighbors implements Serializable{
//		
//		private static final long serialVersionUID = -5783924830363146117L;
//		private Pattern p;
//		ArrayList<Pattern> patternNeighbors = new ArrayList<Pattern>();
//				
//		/**
//		 * Create a SortedTree which contains the neighbors of a Pattern ordered by decreasing similarity
//		 * @param patterns Contains <p> and it's neighbors
//		 * @param index Index of <p> in <cluster>
//		 */
//		private SortedPatternNeighbors(ArrayList<Pattern> patterns, Pattern p_arg){
//				
//			p = p_arg;
//		
//			if(p != null){
//				
//				TreeMap<Double, Pattern> tempNeighbors = new TreeMap<Double, Pattern>();
//				
//				for(int i = 0 ; i < patterns.size() ; i++){
//					
//					Pattern pi = patterns.get(i);
//
//					if(pi != p){	
//						
//						Double diss = new Double(corpus.dissimilarity(p, pi));
//						
//						while(tempNeighbors.containsKey(diss))
//							diss += 0.0001;
//						
//						tempNeighbors.put(diss, pi);
//						
//					}
//				}
//				
//				Collection<Pattern> c= tempNeighbors.values();
//				Iterator<Pattern> itr = c.iterator();
//				int i = 0;
//			
//				while(i < Math.min(MAX_NEIGHBORS,tempNeighbors.size())){
//		
//					patternNeighbors.add(itr.next());
//					i++;
//					
//				}
//			}
//		}
//		
//		/**
//		 * Get <p>'s neighbor of rank <index> (0 is the rank of the first neighbor)
//		 * @param index
//		 * @return
//		 */
//		public Pattern getNeighbor(int index){
//			
//			return patternNeighbors.get(index);
////			if(index < patterns.size() && index < MAX_NEIGHBORS){
////
////				int i = 0;
////				
////				Collection<Pattern> c= patternNeighbors.values();
////				Iterator<Pattern> itr = c.iterator();
////				
////				while(i < index){
////			
////					itr.next();
////					i++;
////				}
////		
////				return (Pattern)itr.next();
////				
////			}
////			else
////				throw new IndexOutOfBoundsException();
//		}
//		
//		/**
//		 * Get the rank of <p_arg> as neighbor of <p>
//		 * @param p_arg
//		 * @return
//		 */
//		public int getIndex(Pattern p_arg){
//			
//			return patternNeighbors.indexOf(p_arg);
////			return patternNeighbors.headMap(new Double(corpus.dissimilarity(p, p_arg))).size();
//		}
//				
//	}
//	
	public int indexOf(Pattern p){
		return patterns.indexOf(p);
	}
	
	public List<Pattern> getPatterns(){
		return Collections.unmodifiableList(patterns);
	}
	
	public String getClusterName() {
		return this.clusterName;
	}
	
	public void setName(String name) {
		clusterName = name;
	}
	
	
}