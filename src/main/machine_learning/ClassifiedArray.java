package main.machine_learning;

public class ClassifiedArray {
	
	public String aClass;
	public double[] features;
	
	/** Id which corresponds to the ClassifiedArray index in the distances tables */
	public int id;
	
	public ClassifiedArray(String aClass, double[] features){
		
		this.aClass = aClass;
		this.features = features;
		
	}
	
	public double distance(ClassifiedArray ca){
		
		double result = 0.0;
		
		if(ca.features.length == features.length){
			
			for(int i = 0 ; i < features.length ; ++i)
				result += Math.pow(features[i]-ca.features[i], 2);
		}
		
		return Math.sqrt(result);
		
	}
	

}
