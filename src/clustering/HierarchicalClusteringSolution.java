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

import model.Pattern;

/**
 * Solution of a hierarchical clustering algorithm
 * @author zach
 *
 */
public class HierarchicalClusteringSolution extends ClusteringSolution implements Serializable{

	private static final long serialVersionUID = -3731466297822232710L;
	private ArrayList<ClusterSet> data;
	
	public HierarchicalClusteringSolution(){
		data = new ArrayList<ClusterSet>();
	}
	
	public HierarchicalClusteringSolution(ArrayList<ClusterSet> data){
		this.data = data;		
	}
	
	public HierarchicalClusteringSolution(ClusterSet c){
		data = new ArrayList<ClusterSet>();
		data.add(c);
	}
	
	public ClusterSet getClusterSet(int i){	
		return data.get(i);
	}
	
	public ArrayList<ClusterSet> getClusterSets(){
		return data;
	}
	
	public Cluster getCluster(int i_clustering, int i_cluster){
		return data.get(i_clustering).get(i_cluster);
	}
	
	public Pattern getPattern(int i_clustering, int i_cluster, int i_pattern){
		return data.get(i_clustering).get(i_cluster, i_pattern);
	}
	
	public void addClusterSet(ClusterSet cs){
		data.add(cs);
	}
	
	public int size(){
		return data.size();
	}
	
}
