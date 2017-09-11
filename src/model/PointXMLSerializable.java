package model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * This class serves the same prupose than class Point but can be Serializable in XML through JAXB (class Point returns an infinite loop see the following link for further details: http://stackoverflow.com/questions/35157008/jaxb-marshalling-a-point-variable)
 * @author zach
 *
 */
public class PointXMLSerializable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2853360704529774353L;
	int x;
	int y;
	
	public PointXMLSerializable(){}
	
	public PointXMLSerializable(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	@XmlAttribute
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	@XmlAttribute
	public void setY(int y) {
		this.y = y;
	}
	
	public String toString(){
		return "(" + x + "," + y + ")";
	}
	
	

}
