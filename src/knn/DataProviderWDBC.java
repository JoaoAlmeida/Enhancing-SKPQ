package knn;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class DataProviderWDBC extends DataProvider
{
	public void loadDataset(List<DataInstance> trainingSet, List<DataInstance> testSet)
	{
		//Loads file with dataset
		Scanner s = null;
    	try
    	{
			s = new Scanner(new File(Main.dataset));
		}
    	catch (FileNotFoundException e)
    	{
			e.printStackTrace();
		}
    	
    	List<DataInstance> dataset = new ArrayList<DataInstance>();
    	while(s.hasNextLine()) {
    		String line = s.nextLine();
    		String tokens[] = line.split(",");
    		
    		if(!(tokens.length > 0))
    		{
    			continue;
    		}
    		
    		//Get attributes according to the columns they occupy in the dataset file
    		List<Double> attributes = new LinkedList<Double>();
    		for(int i=2; i<tokens.length-1; i++)
    		{
    			attributes.add(Double.valueOf(tokens[i]));
    		}
    		//Get category of the current instance of data
    		String category = tokens[1];
    		
    		DataInstance inst = new DataInstance(attributes, category);
    		dataset.add(inst);
    	}

		//Splits dataset between training and test
		for(int i=0; i<dataset.size(); i++)
		{
			if(i%10 < 7)
			{
				trainingSet.add(dataset.get(i));
			}
			else
			{
				testSet.add(dataset.get(i));
			}
		}
	}
}
