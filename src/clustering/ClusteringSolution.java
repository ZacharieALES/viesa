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

public class ClusteringSolution implements Serializable{

	private static final long serialVersionUID = -2871279816557860408L;
	private String methodName = "Unamed method";
	
	public void setMethodName(String s){
		methodName = s;
	}
	
	public String getMethodName(){
		return methodName;
	} 
	
	public String toString(){
		return methodName;
	}
}
