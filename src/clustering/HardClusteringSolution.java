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

import model.Pattern;

public class HardClusteringSolution extends ClusteringSolution implements Serializable{

	private static final long serialVersionUID = 729617603984761667L;
	private ClusterSet data;
	
	public HardClusteringSolution(ClusterSet cs){
		data = cs;
	}

	public ClusterSet getClusterSet(){
		return data;
	}
	
	public Cluster getCluster(int i){
		return data.get(i);
	}
	
	public Pattern getPattern(int i_cluster, int i_pattern){
		return data.get(i_cluster, i_pattern);
	}
	
	public void setCS(ClusterSet cs){
		data = cs;
	}
	
	
}
