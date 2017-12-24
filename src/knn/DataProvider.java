package knn;

import java.util.List;

public abstract class DataProvider
{
	public abstract void loadDataset(List<DataInstance> trainingSet, List<DataInstance> testSet);
}
