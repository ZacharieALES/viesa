package main.machine_learning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import extraction.ExtractionAlignments;
import extraction.SABRE;
import model.Alignment;
import model.AnnotatedArray;
import model.Pattern;

public class FrequencedPattern implements Serializable{

	private static final long serialVersionUID = -8769457047080440949L;
	private HashMap<String, Double> frequenceByClass = new HashMap<>();
	private Pattern p;

	/* Class in which the pattern has been found */
	private AAClass patternClass;

	public FrequencedPattern(){}

	public FrequencedPattern(Pattern p, AAClass patternClass){

		this.p = p;
		this.patternClass = patternClass;

	}

	public String getMostFrequentClass(){

		String mostFrequentClass = null;
		Double highestFrequence = -1.0;

		for(Map.Entry<String, Double> entry: frequenceByClass.entrySet())
			if(entry.getValue() > highestFrequence){
				mostFrequentClass = entry.getKey();
				highestFrequence = entry.getValue();
			}

		return mostFrequentClass;

	}

	public double getFrequencyDifferenceBetweenTwoMostFrequentClass(){

		Double highestFrequence = 0.0;
		Double secondHighestFrequence = 0.0;

		for(Map.Entry<String, Double> entry: frequenceByClass.entrySet()){

			//			System.out.println("f" + entry.getKey() + ": " + entry.getValue());

			if(entry.getValue() > highestFrequence){
				secondHighestFrequence = highestFrequence;
				highestFrequence = entry.getValue();
			}
			else if(entry.getValue() > secondHighestFrequence)
				secondHighestFrequence = entry.getValue();
		}

		//		System.out.println("Diff: " +(highestFrequence - secondHighestFrequence) + " = " + highestFrequence +  " - " +  secondHighestFrequence );

		return highestFrequence - secondHighestFrequence;
	}

	public HashMap<String, Double> getFrequenceByClass() {
		return frequenceByClass;
	}

	@XmlElementWrapper
	@XmlElement (name = "frequenceByClass")
	public void setFrequenceByClass(HashMap<String, Double> frequenceByClass) {
		this.frequenceByClass = frequenceByClass;
	}

	public Pattern getP() {
		return p;
	}

	@XmlElement
	public void setP(Pattern p) {
		this.p = p;
	}

	public double computeFrequencyForItsClass(double minPatternPercentage, double minScore){
		return computeFrequencyForClass(patternClass, minPatternPercentage, minScore);
	}

	public void computeFrequencyForOtherClass(List<AAClass> c_aaclass, double minPatternPercentage, double minScore){

		for(AAClass aac : c_aaclass)
			if(!aac.className.equals(patternClass.className))
				computeFrequencyForClass(aac, minPatternPercentage, minScore);
	}

	public void computeFrequencyForEachClass(List<AAClass> c_aaclass, double minPatternPercentage, double minScore){
		for(AAClass aac : c_aaclass)
			computeFrequencyForClass(aac, minPatternPercentage, minScore);
	}

	public double computeFrequencyForClass(AAClass c_class, double minPatternPercentage, double minScore){

		double frequency = 0.0;

		//			System.out.println("\t pattern (max score: " + maxScore + "):\n"  + p);
		//			System.out.println("Pattern size: " + p.cAA.size());

		for(int i = 0 ; i < c_class.aaSize() ; ++i)
			if(isContainedInCAA(c_class.getAA(i), minPatternPercentage, minScore))
				frequency++;

		frequency /= c_class.aaSize();

		frequenceByClass.put(c_class.className, frequency);

		return frequency;

	}

	public boolean isContainedInCAA(AnnotatedArray caa, double minPatternPercentage, double minScore){
		return percentageContainedInCAA(caa, minScore) > minPatternPercentage;
	}

	public double percentageContainedInCAA(AnnotatedArray caa, double minScore){
		return maxScoreInAA(caa, minScore)/p.maximalScoreOfPattern();
	}

	/**
	 * Maximal score of a pattern found when aligning <p> with an annotated array
	 * @param aa The annotated array
	 * @return The score
	 */
	public double maxScoreInAA(AnnotatedArray aa, double MINIMUM_SCORE){

		double result = -Double.MAX_VALUE;
		ExtractionAlignments ea = SABRE.getInstance().align(p.getPatternAA(), aa, MINIMUM_SCORE, false);

		//		Alignment bestAl = null;
		if(ea.getAlignments().size() > 0)
			for(ArrayList<Alignment> al_a : ea.getAlignments())
				for(Alignment a : al_a){
					if(a.getScore() > result){
						result = a.getScore();
						//					bestAl = a;
					}
				}
		else
			result = 0.0;

		//		System.out.println("Aligning the following pattern: \n" + this );
		//		System.out.println("With AA: " + aa.getFileName());
		//		System.out.println("Max score: " + p.maximalScoreOfPattern());
		//		if(bestAl != null){
		//			System.out.println("Found a max score of " + result);
		//			System.out.println("Pattern 1 : \n" + bestAl.getP1());
		//			System.out.println("----");
		//			System.out.println("Pattern 2 : \n" + bestAl.getP2());
		//		}
		//		else{
		//			System.out.println("nothing found");
		//		}


		return result;

	}


	/**
	 * Greatest difference in frequency for this pattern between two classes
	 * @return
	 */
	public double greatestDifferenceInFrequency() {

		double minFreq = Double.MAX_VALUE;
		double maxFreq = -Double.MAX_VALUE;

		for(Map.Entry<String, Double> freq: frequenceByClass.entrySet()){
			if(freq.getValue() > maxFreq)
				maxFreq = freq.getValue();
			if(freq.getValue() < minFreq)
				minFreq = freq.getValue();
		}

		return maxFreq - minFreq;
	}

	@Override
	public String toString(){
		return p.toString();
	}

	/**
	 * Test if the pattern is already contained in the patterns of a class
	 * @param c_class The class considered
	 * @param minPatternPercentage The score percentage above which the pattern is considered to be equal to another pattern
	 * @return
	 */
	public boolean isAlreadyContainedInClass(AAClass c_class, double minPatternPercentage, double minScore) {

		boolean isContained = false;
		int i = 0;

		while(i < c_class.getL_p().size() && !isContained){

			FrequencedPattern fp = c_class.getL_p().get(i);

			if(isContainedInCAA(fp.p.getPatternAA(), minPatternPercentage, minScore))
				isContained = true;
			else
				i++;

		}

		return isContained;
	}

	public boolean isAlreadyConsideredUnsuitable(List<FrequencedPattern> unsuitablePatterns,
			double minPatternPercentage, double minScore) {

		boolean isContained = false;
		int i = 0;

		while(i < unsuitablePatterns.size() && !isContained){

			FrequencedPattern fp = unsuitablePatterns.get(i);

			if(isContainedInCAA(fp.p.getPatternAA(), minPatternPercentage, minScore))
				isContained = true;
			else
				i++;

		}

		return isContained;
	}


}