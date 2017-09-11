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

package model;


/**
 * Define a position in the tS matrix (and not in X and Y)
 * -> this.getI()   = position in the 1st dimension of tS
 * -> this.getI()-1 = position in the 1st dimension of X 
 * @author zach
 *
 */
public class Position {

	private int i;
	private int j;
	private int k;
	
//	static final Logger log = Logger.getLogger("");
	
	public Position (int x, int y, int z){
		
		i = x;
		j = y;
		k = z;
		
	}
	
	public Position (Position model){

		i = model.getI();
		j = model.getJ();
		k = model.getK();
	}
	
	public Position(Coordinate<Integer> ij, Coordinate<Integer> kl){
		
		i = ij.get(0)+1;
		j = ij.get(1)+1;
		k = kl.get(0)+1;
		
	}

	public int getI(){
		return i;
	}

	public int getJ(){
		return j;
	}

	public int getK(){
		return k;
	}
	
	public int get(int x){
		
		int result;
		
		switch(x){
		case 0: result = i ;break; 
		case 1: result = j ;break;
		case 2: result = k ;break;
		default: result = -1;
		}
		
		return result;
		
	}
	
	public void setI(int iI) {
		this.i = iI;
	}

	public void setJ(int iJ) {
		this.j = iJ;
	}

	public void setK(int iK) {
		this.k = iK;
	}
	
	public String toString(){
		return "\t i = " + i  +  "\n\t j = " + j  +  "\n\t k = " + k;
	}
	
	public boolean equals(Position pos){
		
		boolean result;
		
		if( this.i == pos.i && this.j == pos.j && this.k == pos.k)
			result = true;
		else
			result = false;
		
		return result;
		
	}
	
	/**
	 * Compare the position to another. The position is said to be greater if it's first coordinate is greater than the other one's first coordinate. If equal, the second coordinate is compared and so one. If all the coordinates are equal, the position are the same.
	 * @param p
	 * @return 1 if the current position is greater than <p> ; 0 if they are equal ; -1 otherwise
	 */
	public int compareTo(Position p) {
		int result = -1;
		
		if(this.i > p.i)
			result = 1;
		else if(this.i == p.i)
			if(this.j > p.j)
				result = 1;
			else if(this.j == p.j)
				if(this.k > p.k)
					result = 1;
				else if(this.k == p.k)
					result = 0;
		
		return result;
	}
}
