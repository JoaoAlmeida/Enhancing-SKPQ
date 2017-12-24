package knn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import util.Sorter;

public class Knn
{
	public static void categorize(DataInstance inst, List<DataInstance> dataset, Integer k)
	{
		List<DataInstance> neighbors = inst.getNeighbors(dataset, k);
		Map<String, Integer> categoryCount = new LinkedHashMap<String, Integer>();

		for(DataInstance n: neighbors)
		{
			if(categoryCount.containsKey(n.getCategory()))
			{
				categoryCount.put(n.getCategory(), categoryCount.get(n.getCategory())+1);
			}
			else
			{
				categoryCount.put(n.getCategory(), 1);
			}
		}
		
		List<Entry<String, Integer>> entryList = Sorter.sortEntrySet(categoryCount.entrySet(), false);

		inst.setPredictedCategory(entryList.get(0).getKey());
	}
	
	public static void runClassifier(List<DataInstance> trainingSet, List<DataInstance> testSet, Integer k)
	{
		for(DataInstance inst: testSet)
		{
			categorize(inst, trainingSet, k);
		}
	}
	
	public static void printSets(List<DataInstance> trainingSet, List<DataInstance> testSet)
	{
		System.out.println("Training set:");
		printSet(trainingSet);
		System.out.println("Test set:");
		printTestSet(testSet);
	}
	
	public static void printSet(List<DataInstance> dataset)
	{
		for(DataInstance inst: dataset)
		{
			for(Double attr: inst.getAttributes())
			{
				System.out.printf("%8.1f ",attr);
			}
			System.out.printf("%s\n",inst.getCategory());
		}
	}
	
	public static void printTestSet(List<DataInstance> dataset)
	{
		for(DataInstance inst: dataset)
		{
			for(Double attr: inst.getAttributes())
			{
				System.out.printf("%8.1f ",attr);
			}
			System.out.printf("%s\n",inst.getPredictedCategory());
		}
	}
}
