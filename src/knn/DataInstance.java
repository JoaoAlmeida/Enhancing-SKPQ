package knn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import util.Sorter;

public class DataInstance {
	private List<Double> attributes;
	private String category;
	private String predictedCategory;
	
	public DataInstance(List<Double> attributes, String category)
	{
		this.attributes = attributes;
		this.category = category;
	}
	
	public final List<Double> getAttributes()
	{
		return this.attributes;
	}
	
	public Double getAttribute(Integer index)
	{
		return this.attributes.get(index);
	}
	
	public void setAttribute(Integer index, Double value)
	{
		this.attributes.set(index, value);
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getPredictedCategory() {
		return predictedCategory;
	}

	public void setPredictedCategory(String predictedCategory) {
		this.predictedCategory = predictedCategory;
	}

	/**
	 * @param other DataInstance of which the distance to is calculated
	 * @return Euclidian distance to the DataInstance other 
	 */
	public Double distanceTo(DataInstance other)
	{
		Double distance = 0d;

		for(int i=0; i<this.attributes.size(); i++)
		{
			distance += Math.pow((this.getAttribute(i) - other.getAttribute(i)), 2);
		}

		distance = Math.sqrt(distance);
		
		return distance;
	}
	
	/**
	 * @param dataset Dataset from which to get the neighbors
	 * @param k Number of neighbors to get
	 * @return List of neighbors of current DataInstance
	 */
	public List<DataInstance> getNeighbors(List<DataInstance> dataset, Integer k)
	{
		if(dataset.size() < k)
		{
			return dataset;
		}
		Map<DataInstance, Double> distances = new LinkedHashMap<DataInstance, Double>();
		for(DataInstance inst: dataset)
		{
			distances.put(inst, this.distanceTo(inst));
		}

		List<Entry<DataInstance, Double>> entryList = Sorter.sortEntrySet(distances.entrySet());

		List<DataInstance> neighbors = new ArrayList<DataInstance>();
		for(int i=0; i<k; i++)
        {
			neighbors.add(entryList.get(i).getKey());
        }
		return neighbors;
	}
}