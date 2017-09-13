package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exception.InvalidArgumentsToCreateAnAAColumnFormat;
import exception.UndefinedColumnFormatException;
import extraction.LPCA4D;
import extraction.LPCALimitedLines;
import extraction.PositiveScoreTable;
import extraction.SABRE;
import extraction.SABREParameter;
import model.AnnotatedArray;
import model.Corpus;

public class MainChanoniTestLPCA4D {

	public static void run(){

		//		StandardView sv = StandardView.getInstance();
		//		Corpus.getCorpus().addObserver(sv);
		//		SABRE.getInstance().addObserver(sv);

		PositiveScoreTable st;

		String fo_path = "data/Dialogues_parents_enfants/";						
		st = new PositiveScoreTable(fo_path + "couples_scores_positifs.csv");


		double scoreMin = 10;
		double desynch = 5;
		int desired_number_of_alignments = 25;

		double gap = 2*desynch;

		SABRE.getInstance().setParam(new SABREParameter(gap, desynch, desired_number_of_alignments)); 

		ArrayList<Integer> al_comment = new ArrayList<Integer>();
		ArrayList<Integer> al_annot = new ArrayList<Integer>();

		al_comment.add(0);
		al_comment.add(1);
		al_comment.add(2);

		al_annot.add(3);
		al_annot.add(4);
		al_annot.add(5);
		al_annot.add(6);

		try {
			Corpus.getCorpus().setColumnFormat(al_comment, al_annot, null);
			Corpus.getCorpus().setAnnotationSimilarities(st);
			Corpus.getCorpus().setMaxDistance(1.0);
			Corpus.getCorpus().add(fo_path + "/csv", false);
		} catch (InvalidArgumentsToCreateAnAAColumnFormat e) {
			e.printStackTrace();
		} catch (UndefinedColumnFormatException e) {
			e.printStackTrace();
		}

		List<Results> results = new ArrayList<>();

		MainChanoniTestLPCA4D instance = new MainChanoniTestLPCA4D();


		for(int i = 0 ; i < Corpus.getCorpus().getAASize() ; ++i)
			for(int j = i+1 ; j < Corpus.getCorpus().getAASize() ; ++j){

				System.out.println(i + " / " + j + " / " + Corpus.getCorpus().getAASize());

				AnnotatedArray aa1 = Corpus.getCorpus().getAA().get(i);
				AnnotatedArray aa2 = Corpus.getCorpus().getAA().get(j);
				
				for(int l = 10 ; l < Math.min(101,  Math.min(aa1.getNumberOfLines(), aa2.getNumberOfLines())) ; l+=10)
				{

					Results r = null;
					int id = 0;

					while(r == null && id < results.size()){

						if(results.get(id).numberOfLines == l)
							r = results.get(id);
						else
							id++;

					}

					if(r == null){
						r = instance.new Results(l);
						results.add(r);
					}

					if(r.LPCATimes.size() < 400){
						long start = System.currentTimeMillis();

						LPCALimitedLines ll = new LPCALimitedLines();
						ll.extractionInsDelScore = 10.0;
						ll.align(aa1, aa2, 100, scoreMin, l);

						long duree = System.currentTimeMillis() - start;
						//					System.out.println(duree);

						LPCA4D.doStupidTraceback = false;
						start = System.currentTimeMillis();

						LPCA4D ll2 = new LPCA4D();
						ll2.extractionInsDelScore = 10.0;
						ll2.align(aa1, aa2, 100, scoreMin, l);
						
						long dureeLPCA4D = System.currentTimeMillis() - start;

						LPCA4D.doStupidTraceback = true;
						start = System.currentTimeMillis();

						ll2.extractionInsDelScore = 10.0;
						ll2.align(aa1, aa2, 100, scoreMin, l);

						r.addValue(duree, dureeLPCA4D, System.currentTimeMillis() - start);

//						System.out.println(duree + " - " + dureeLPCA4D + " - " +  (System.currentTimeMillis() - start));
//						System.out.println("----");

						if(l == 100)
							for(Results r2: results)
								System.out.println(r2.numberOfLines + " : " + r2.meanLPCATime() + " / " + r2.meanLPCA4DTime() + " / " + r2.meanLPCA4DSTTime() + " (" + r2.LPCATimes.size() + " valeurs)");
					}

				}

			}
		//			sv.process_extraction();
		//			sv.process_extraction_and_clustering();

		Collections.sort(results, new Comparator<Results>(){

			@Override
			public int compare(Results arg0, Results arg1) {
				return arg0.numberOfLines - arg1.numberOfLines;
			}			
		});

		for(Results r: results)
			System.out.println(r.numberOfLines + " : " + r.meanLPCATime() + "(" + Math.round(r.ecartTypeLPCA()) + ") / " + r.meanLPCA4DTime() + "(" + Math.round(r.ecartTypeLPCA4D()) + ") / " + r.meanLPCA4DSTTime() + "(" + Math.round(r.ecartTypeLPCA4DST()) + ")");
		
		for(Results r: results)
			r.detailedTimes();
			
		

	}

	public class Results{

		public int numberOfLines;

		public List<Long> LPCATimes = new ArrayList<>();
		public List<Long> LPCA4DTimes = new ArrayList<>();
		public List<Long> LPCA4DSTTimes = new ArrayList<>();

		public Results(int numberOfLines){
			this.numberOfLines = numberOfLines;
		}

		public void addValue(long LPCAtime, long LPCA4Dtime, long LPCA4DSTTime){
			LPCATimes.add(LPCAtime);
			LPCA4DTimes.add(LPCA4Dtime);
			LPCA4DSTTimes.add(LPCA4DSTTime);

			//			System.out.println("Add value for " + numberOfLines + " : " + LPCAtime + " / " + LPCA4Dtime);
		}

		public long meanLPCATime(){
			long mean = 0;
			
			for(Long l: LPCATimes)
				mean += l;
			
			return mean / LPCATimes.size();
		}

		public long meanLPCA4DTime(){
			long mean = 0;
			
			for(Long l: LPCA4DTimes)
				mean += l;
			
			return mean / LPCA4DTimes.size();
		}

		public long meanLPCA4DSTTime(){
			long mean = 0;
			
			for(Long l: LPCA4DSTTimes)
				mean += l;
			
			return mean / LPCA4DSTTimes.size();
		}
		
		public double ecartTypeLPCA(){
			
			long m = meanLPCATime();
			double ecartType = 0.0;
			
			for(Long l: LPCATimes)
				ecartType += Math.pow(Math.abs(m - l), 2);
			
			return Math.sqrt(ecartType/LPCATimes.size());
			
		}
		
		public void detailedTimes(){
			
			System.out.println("Number of lines : " + this.numberOfLines);
			for(int i = 0 ; i < LPCATimes.size() ; ++i){
				System.out.println(Math.round(LPCATimes.get(i)) + " / " + Math.round(LPCA4DTimes.get(i)) + " / " + Math.round(LPCA4DSTTimes.get(i)));
			}
			System.out.println("-------------");
			
		}
		
		public double ecartTypeLPCA4D(){
			
			long m = meanLPCA4DTime();
			double ecartType = 0.0;
			
			for(Long l: LPCA4DTimes)
				ecartType += Math.pow(Math.abs(m - l), 2);
			
			return Math.sqrt(ecartType/LPCA4DTimes.size());
			
		}
		
		public double ecartTypeLPCA4DST(){
			
			long m = meanLPCA4DSTTime();
			double ecartType = 0.0;
			
			for(Long l: LPCA4DSTTimes)
				ecartType += Math.pow(Math.abs(m - l), 2);
			
			return Math.sqrt(ecartType/LPCA4DSTTimes.size());
			
		}

	}
}
