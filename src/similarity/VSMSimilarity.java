package similarity;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;

public class VSMSimilarity {

	public VSMSimilarity(Resource resource1, Resource resource2) {
		this.resource1 = resource1;
		this.resource2 = resource2;
	}
	public Resource getResource1() {
		return resource1;
	}
	public void setResource1(Resource resource1) {
		this.resource1 = resource1;
	}
	public Resource getResource2() {
		return resource2;
	}
	public void setResource2(Resource resource2) {
		this.resource2 = resource2;
	}

	public Resource resource1;
	public Resource resource2;
	public double simScore;

	public double getSimScore() {
		return simScore;
	}
	public void setSimScore(double simScore) {
		this.simScore = simScore;
	}

	public Map<String,Double> propertyScore = new HashMap<String,Double>();
	public Map<String, Double> getPropertyScore() {
		return propertyScore;
	}
	public void setPropertyScore(Map<String, Double> propertyScore) {
		this.propertyScore = propertyScore;
	}
	
	public void addPropertyScore(String property, Double score) {
		this.getPropertyScore().put(property, score);
	}
	
	public double totalScore() {
		double totalScore = 0d;
		for (String propertyURI : this.getPropertyScore().keySet()) {
			totalScore = totalScore + this.getPropertyScore().get(propertyURI);
		}

		totalScore = totalScore / this.getPropertyScore().keySet().size();

		return totalScore;
	}
}
