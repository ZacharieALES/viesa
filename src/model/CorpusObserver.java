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

import java.util.Observer;

import javax.swing.SwingWorker;

import clustering.ClusteringSolution;
import exception.AbstractException;
import exception.InvalidNumberOfColumnsInInputFilesException;
import extraction.PositiveScoreTable;

public interface CorpusObserver extends Observer{
	
	public abstract void updatePatterns();
	public abstract void updateClusters(ClusteringSolution cs);
	public abstract void updateSABREParam();
	public abstract void abstractException(AbstractException e);
	public abstract void endOfClusteringProcess();
	public abstract void endOfExtractionProcess();
	public abstract void cancelProcess();
	public abstract void updateScoreSimilarities(PositiveScoreTable p);
	public abstract void updateMaxSimilarity(double d);
	public abstract void updateAddAA(AnnotatedArray aa);
	public abstract void updateRemoveAA(int id);
	public abstract void updateSwingWorker(SwingWorker<?, ?> sw);
	public abstract void updateDesiredNumberOfAlignments(int v);
	public abstract void removeClusteringSolution(int i);
	public abstract void clearClusters();
	
}
