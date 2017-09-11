package clustering;

import javax.swing.SwingWorker;

import model.Corpus;

public abstract class AbstractClusteringMethod extends SwingWorker<Void, Void>{
	
	public static int remainingClusteringMethodToProcess;
	
	public ClusteringSolution result = null;

//	static final Logger log = Logger.getLogger("");
	protected boolean done = false;

	/*
	 * This value tracks the progress of the processing, for feedback to connected Views 
	 */
	protected double progress;
	
	public Void doInBackground() throws Exception{
		
		try{
			result = cluster();
		}catch(Exception e){e.printStackTrace();}
		
		if(result != null)
			Corpus.getCorpus().add(result);
		else
			System.err.println("No clustering solution returned");
			
		remainingClusteringMethodToProcess--;
//		System.out.println("ACM: remaining: " + remainingClusteringMethodToProcess);
		if(remainingClusteringMethodToProcess == 0){
			Corpus.getCorpus().notifyObserverEndOfClusteringProcess();
			System.out.println("Corpus: notify end of clustering process");
		}
		
		done = true;
		
		return null;
	}
	
	public abstract ClusteringSolution cluster();
	
	/*
	 * Increase the progress indicator bar by (double) step percent.
	 * 
	 */
	protected void progress(double step){
		this.progress += step;
		this.setProgress((int) Math.min(progress, 99));
	}
	
	public boolean getDone() {
		return done;
	}


}
