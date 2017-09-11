package extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.DefaultEditorKit.InsertBreakAction;

import main.machine_learning.ClassifiedArray;
import main.machine_learning.Data_converter;

/**
 * Corpus of classified arrays
 * @author zach
 *
 */
public class CACorpus {

	List<ClassifiedArray> dev, train, test;

	double[][] distTrain, distTrainToTest;

	public CACorpus(String trainPath){
		train = Data_converter.readPatternFile(trainPath);
	}

	public void setTest(String testPath){
		test = Data_converter.readPatternFile(testPath);
	}

	public void setDev(String devPath){
		dev = Data_converter.readPatternFile(devPath);
	}

	public void computeTestDistancesToTrain(){

		if(train != null && train.size() > 0){

			if(test != null && test.size() > 0){
				distTrain = new double[train.size()][test.size()];

				for(int i = 0 ; i < test.size() ; i++)
					test.get(i).id = i;
				
				for(int i = 0 ; i < train.size() ; i++){
					
					ClassifiedArray trainI = train.get(i);
					
					if(trainI.id != i){
						System.out.println("Error: id of ClassifiedArray " + i + " is not coherent");
					}
					for(int j = 0 ; j < test.size() ; ++j){
						distTrain[i][j] = trainI.distance(test.get(j));
					}
				}
			}
			else
				System.out.println("CACorpus.computeTestDistancesToTrain: the test is null or empty");
		}
		else
			System.out.println("CACorpus.computeTestDistancesToTrain: the train is null or empty");

	}

	public void computeDevDistancesToTrain(){

		if(train != null && train.size() > 0){

			if(dev != null && dev.size() > 0){
				distTrain = new double[train.size()][dev.size()];
				
				for(int i = 0 ; i < dev.size() ; i++)
					dev.get(i).id = i;

				for(int i = 0 ; i < train.size() ; i++){
					
					ClassifiedArray trainI = train.get(i);
					
					if(trainI.id != i){
						System.out.println("Error: id of ClassifiedArray " + i + " is not coherent");
					}
						
					for(int j = 0 ; j < dev.size() ; ++j){
						distTrain[i][j] = trainI.distance(dev.get(j));
					}
				}
			}
			else
				System.out.println("CACorpus.computeDevDistancesToTrain: the dev is null or empty");
		}
		else
			System.out.println("CACorpus.computeDevDistancesToTrain: the train is null or empty");

	}

	public void computeTrainDistances(){

		if(train != null && train.size() > 0){

			for(int i = 0 ; i < train.size() ; i++)
				train.get(i).id = i;
			
			distTrain = new double[train.size()][train.size()];

			for(int i = 0 ; i < train.size() ; i++){
				
				ClassifiedArray trainI = train.get(i);
				
				for(int j = i+1 ; j < train.size() ; ++j){
					distTrain[i][j] = trainI.distance(train.get(j));
					distTrain[j][i] = distTrain[i][j];
				}
			}
		}
		else
			System.out.println("CACorpus.computeTrainDistances: the train is null or empty");

	}

	/**
	 * 
	/**
	 * Classify the array according to its k nearest neighbors
	 * @param ca The array to classify
	 * @param distToCa An array of distances in which the lines corresponds to the train and column ca.id corresponds to ca
	 * @param k The number of neighbors to consider
	 * @return True if the document is correctly classified, false otherwise
	 */
	public boolean classifyKnnTrain(ClassifiedArray ca, double[][] distToCa, int k){

		System.out.println("Classify dialogue of class: "+ ca.aClass);
		
		/* Id of the neighbors from the train set ordered by distance to <ca> */
		List<ArrayList<Integer>> neighbors = new ArrayList<>();

		double worstNeighborDistance = Double.MAX_VALUE;

		for(int i = 0 ; i < train.size() ; ++i)	
			worstNeighborDistance = addNeighbors(neighbors, k, i, ca.id, distToCa, worstNeighborDistance);
		
		HashMap<String, Integer> hm = new HashMap<>();
		
		String classPredicted = "";
		int nbOfElementInPredictedClass = 0;
		
		System.out.print("Neighbors: ");
		/* For each neighbor */
		for(ArrayList<Integer> ali: neighbors){
			
			for(Integer i: ali){
				
				
				/* Count it in the HashMap */
				String iClass = train.get(i).aClass; 
				Integer nbOfArrays = hm.get(iClass);
				int newValue = 1;
				if(nbOfArrays != null)
					newValue = nbOfArrays + 1;

				hm.put(iClass, newValue);

				System.out.print(train.get(i).aClass + ", ");
				
				if(newValue > nbOfElementInPredictedClass){
					classPredicted = iClass;
					nbOfElementInPredictedClass = newValue;
				}
			}
			
		}
	
		return ca.aClass.equals(classPredicted);
	}

	private double addNeighbors(List<ArrayList<Integer>> neighbors, int k, int trainId, int caId, double[][] distToCa, double worstNeighborDistance) {

		/* Get the distance between the ith train array and ca */
		double dist = distToCa[trainId][caId];
		
//		System.out.println("Do I add dist: " + dist + " (class " + train.get(trainId).aClass + ")?");
		
		/* If the alignment has a distance low enough to be added in the list */ 
		if(dist <= worstNeighborDistance){
			
			/* Get the current number of neighbors */
			int nbOfNeighbors = 0;
			
			for(ArrayList<Integer> ali: neighbors)
				nbOfNeighbors += ali.size();
			
			int indexToInsert = 0;
			boolean positionFound = false;

			while(!positionFound && indexToInsert < neighbors.size()){

				int currentId = neighbors.get(indexToInsert).get(0);
				double currentDist = distToCa[currentId][caId];

				/* If the distance of the train arrays at position <indexToInsert> is lower than the one of <trainId> */
				if(currentDist < dist){

					/* Add the alignment at the previous position in a new ArrayList */
					ArrayList<Integer> new_al = new ArrayList<>();
					new_al.add(trainId);

					neighbors.add(Math.max(0, indexToInsert), new_al);
					nbOfNeighbors++;
					positionFound = true;
					
//					System.out.println("Add in new list at position " + Math.max(0, indexToInsert));
				}

				/* If the distance of the train arrays at position <indexToInsert> is equal to the one of <trainId> */
				else if(currentDist == dist){

					/* Add the alignments at this position and don't create a new ArrayList */
					neighbors.get(indexToInsert).add(trainId);
					positionFound = true;
					nbOfNeighbors++;
					
//					System.out.println("Add in a new list at position " + indexToInsert);
				}
				else
					indexToInsert++;

			}

			if(!positionFound){

				/* Add the alignment at the end in a new ArrayList */
				ArrayList<Integer> new_al = new ArrayList<>();
				new_al.add(trainId);

				neighbors.add(new_al);
				nbOfNeighbors++;

				System.out.println("Add in a new list at the end (" + indexToInsert + ")");
			}

			/* If the current number of alignments is higher than the number of alignments desired by the user */
			if(k > 0 && nbOfNeighbors > k){

				int numberOfRemovedAlignments = neighbors.get(0).size();

				/* If removing the alignments of highest distance does not lead to less than <k> arrays */
				if(nbOfNeighbors - numberOfRemovedAlignments >= k){
					nbOfNeighbors -= neighbors.get(0).size();
					neighbors.remove(0);
				}

			}

			/* If the number of desired alignments is limited and if this limit is reached */
			if(k != 0 && nbOfNeighbors >= k){
				worstNeighborDistance = distToCa[neighbors.get(0).get(0)][caId];
			}

		}
//		else
//			System.out.println("Do not add");
		

//		System.out.print("Current neighbors: ");
//		
//		/* For each neighbor */
//		for(ArrayList<Integer> ali: neighbors){
//			
//			for(Integer i: ali){
//				System.out.print("(" + Math.round(distToCa[i][caId]) + ", c" + train.get(i).aClass+ ") " );
//			}
//		}
//		System.out.println("\n----");
		
		
		return worstNeighborDistance;
	}

	public static void main(String[] args){

		String trainFile = "output_full_train";
		String devFile = "output_full_dev";
		String testFile = "output_full_test";
		
		
		System.out.print("Reading train file... ");
		CACorpus cac = new CACorpus(trainFile);
		System.out.println("done");
		
		System.out.print("Computing train distances...");
		cac.computeTrainDistances();
		System.out.println("done");

		HashMap<String, Integer> hmCorrectlyClassified = new HashMap<>();
		HashMap<String, Integer> hmElementByClassCount = new HashMap<>();
		
		int k = 10;
		
		for(ClassifiedArray ca: cac.train){
			if(cac.classifyKnnTrain(ca, cac.distTrain, k)){
				Integer i = hmCorrectlyClassified.get(ca.aClass);
				
				if(i != null)
					hmCorrectlyClassified.put(ca.aClass, i+1);
				else
					hmCorrectlyClassified.put(ca.aClass, 0);
			}

			Integer i = hmElementByClassCount.get(ca.aClass);
			
			if(i != null)
				hmElementByClassCount.put(ca.aClass, i+1);
			else
				hmElementByClassCount.put(ca.aClass, 0);
			
			System.out.println("\n----");	
		}
		
		for(Map.Entry<String, Integer> entry: hmElementByClassCount.entrySet()){
			
			Integer correctlyClassified = hmCorrectlyClassified.get(entry.getKey());
			
			if(correctlyClassified == null)
				correctlyClassified = 0;
			
			System.out.println(entry.getKey() + " : " + (Math.round(100*new Double(correctlyClassified)/entry.getValue())) + "% (" + correctlyClassified +  "/" + entry.getValue() + ")");
		}
		
		System.out.println();



	}

}
