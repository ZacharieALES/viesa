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
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;


public class Coordinate<T extends Number>implements Serializable{

	private static final long serialVersionUID = 2766530833360261331L;

//	static final Logger log = Logger.getLogger("");
	
	@XmlAttribute
	public T x;
	@XmlAttribute
	public T y;
	
	public Coordinate(T i, T j){
		x = i;
		y = j;
	}
	
	public Coordinate(Coordinate<T> c){
		x = c.x;
		y = c.y;
	}
	
	public Coordinate() {
		// TODO Auto-generated constructor stub
	}

	public T get(int i){
		
		T result;
		
		switch(i){
		case 0:
			result = x;
			break;
		default :
			result = y;
			System.err.println("get( i) : i must be equal to 0 or 1 (i = " + i + ")");
				
		}
		
		return result;
	}
	
	public void set(int i, T value){
		
		switch(i){
		case 0:
			this.x = value;
		case 1:
			this.y = value;
		}
	}
	
	public String toString(){
		
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		buff.append(x);
		buff.append(",");
		buff.append(y);
		buff.append(")");
		
		return buff.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		
		boolean result = false;
		
		if(o instanceof Coordinate) {
			Coordinate<T> c = (Coordinate<T>)o;
			result = c.x.equals(x) && c.y.equals(y);
		}
		
		return result;
	}

	public void setValue(Object v) {
		
	}

}
