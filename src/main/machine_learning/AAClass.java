package main.machine_learning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Class which contains all the annotated array associated to a given class
 * @author zach
 *
 */
public class AAClass implements Serializable{
	
	private static final long serialVersionUID = -3661953657005248132L;
	String className;
	private List<ClassifiedAA> l_aa;
	private List<FrequencedPattern> l_p;
	
	public String getClassName() {
		return className;
	}

	public ClassifiedAA  getAA(int id) {
		if(id >= 0 && id < l_aa.size())
			return l_aa.get(id);
		else
			return null;
	}

	public List<ClassifiedAA> getL_aa() {
		return l_aa;
	}

	@XmlElementWrapper
	@XmlElement (name = "ClassifiedAA")
	public void setL_aa(List<ClassifiedAA> l_aa) {
		this.l_aa = l_aa;
	}

	public List<FrequencedPattern> getL_p() {
		return l_p;
	}

	@XmlElementWrapper
	@XmlElement (name = "FrequencedPattern")
	public void setL_p(List<FrequencedPattern> l_p) {
		this.l_p = l_p;
	}

	@XmlAttribute
	public void setClassName(String className) {
		this.className = className;
	}
	
	public AAClass(){
		l_aa = new ArrayList<>();
		l_p = new ArrayList<>();
	}

	public AAClass(String className){
		this.className = className;
		l_aa = new ArrayList<>();
		l_p = new ArrayList<>();
		
	}
	
	public void addAA(ClassifiedAA classifiedAA){
		l_aa.add(classifiedAA);
	}
	
	@Override
	public boolean equals(Object o){
		
		boolean result = false;
		
		if(o instanceof AAClass)
			result = className.equals(((AAClass)o).getClassName());
		
		return result;
		
	}
	
	public int aaSize(){
		return l_aa.size();
	}
	
	public int pSize(){
		return l_p.size();
	}
	
	public void addPattern(FrequencedPattern p){
		l_p.add(p);
	}
	
	public FrequencedPattern getPattern(int id){
		return l_p.get(id);
	}
	
	
	
}
