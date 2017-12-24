package knn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	
	public static String dataset =  "test.data.txt";
	
	public static void main(String[] args)
	{
		//Number of neighbors
		Integer k = 3;
		runPaperTissueExample(k);
		//runWDBCExample(k);
		//runIrisExample(k);
	}
	
	
	public static void runPaperTissueExample(Integer k)
	{
		List<DataInstance> trainingSet = new ArrayList<DataInstance>();
		List<DataInstance> testSet = new ArrayList<DataInstance>();
		
		//new DataProviderPaperTissue().loadDataset(trainingSet, testSet);
		
		Double[] d1 = new Double[]{1d,1d,1d};
		Double[] d2 = new Double[]{1d,0d,0d};
		Double[] d3 = new Double[]{1d,0d,1d};
		Double[] d4 = new Double[]{0d,1d,0d};
		Double[] d5 = new Double[]{0d,1d,1d};
		Double[] d6 = new Double[]{0d,1d,0d};
		Double[] d7 = new Double[]{0d,1d,1d};	
		
				

		DataInstance di1 = new DataInstance(Arrays.asList(d1),"LIKE");
		DataInstance di2 = new DataInstance(Arrays.asList(d2),"DISLIKE");
		DataInstance di3 = new DataInstance(Arrays.asList(d3),"DISLIKE");
		DataInstance di4 = new DataInstance(Arrays.asList(d4),"DISLIKE");
		DataInstance di5 = new DataInstance(Arrays.asList(d5),"DISLIKE");
		DataInstance di6 = new DataInstance(Arrays.asList(d6),"DISLIKE");
		DataInstance di7 = new DataInstance(Arrays.asList(d7),"DISLIKE");
		
		
		trainingSet.add(di1);
		trainingSet.add(di2);

		testSet.add(di3);
		testSet.add(di4);
		testSet.add(di5);
		testSet.add(di6);	
		testSet.add(di7);	
		
		Knn.runClassifier(trainingSet, testSet, k);
		Knn.printSets(trainingSet, testSet);
	}

	/**
	 * Dataset from http://archive.ics.uci.edu/ml/datasets/Breast+Cancer+Wisconsin+%28Diagnostic%29
	 */
	public static void runWDBCExample(Integer k)
	{
		List<DataInstance> trainingSet = new ArrayList<DataInstance>();
		List<DataInstance> testSet = new ArrayList<DataInstance>();
		new DataProviderWDBC().loadDataset(trainingSet, testSet);
		
		Knn.runClassifier(trainingSet, testSet, k);
		Knn.printSets(trainingSet, testSet);
	}

	/**
	 * Dataset from http://archive.ics.uci.edu/ml/datasets/Iris
	 */
	public static void runIrisExample(Integer k)
	{
		List<DataInstance> trainingSet = new ArrayList<DataInstance>();
		List<DataInstance> testSet = new ArrayList<DataInstance>();
		new DataProviderIris().loadDataset(trainingSet, testSet);
		
		Knn.runClassifier(trainingSet, testSet, k);
		Knn.printSets(trainingSet, testSet);
	}


}