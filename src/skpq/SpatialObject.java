package skpq;

import java.io.Serializable;

import org.apache.jena.rdf.model.Resource;

import cosinesimilarity.LuceneCosineSimilarity;

@SuppressWarnings("rawtypes")
public class SpatialObject implements Comparable, Serializable {
	
	//Change this id in case of big changes on this class. A exception will be launched to warn about the compatibility issues over serialization: https://blog.caelum.com.br/entendendo-o-serialversionuid/
	private static final long serialVersionUID = 1L;
	private String uri;
	private double score;
	private double rate;
	private int id;
	private String label;
	private String lat;
	private String lgt;
	private double alpha = 0.5;
	private SpatialObject bestNeighbor;
	private boolean isFeature;
	private String completeDescription;
//	private Resource lgdResource;
	private double paretoProbability;
	private int numberCheckin;
	
	public SpatialObject(int id, String uri) {
		this.id = id;
		this.uri = uri;
	}
	
	public SpatialObject(String googleDescription, double rate, String query_keywords) {
		this.label = googleDescription;
		this.rate = rate;
//		System.out.println("Rate: " + rate);
//		System.out.println("Gscore: " + getGoogleCossineSim(query_keywords));
//		System.out.println("final score " + getGoogleScore(query_keywords));
		this.score = getGoogleScore(query_keywords);
		
		this.isFeature = true;
	}
	
	public SpatialObject(String label, double rate, double score) {
		this.label = label;
		this.rate = rate;
//		System.out.println("Rate: " + rate);
//		System.out.println("score: " + score);
//		System.out.println("final score " + ((alpha * rate) + ((1 - alpha) * score)));
//		this.score = ((alpha * rate) + ((1 - alpha) * score));
		this.score = score;
		
		this.isFeature = true;
	}
	
	public SpatialObject(String name, String uri) {
		this.label = name;
		this.uri = uri;
		
		this.isFeature = true;
	}

	public SpatialObject(int id, String name, String uri, String lat, String lgt) {
		this.id = id;
		this.label = name;
		this.uri = uri;
		this.lat = lat;
		this.lgt = lgt;
		
		this.isFeature = true;
	}
		
	public double getRate() {
		
		if(score <= 0){
			return rate/2;
		}
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	private double getGoogleCossineSim(String query_keywords){
		return LuceneCosineSimilarity.getCosineSimilarity(label, query_keywords);
	}
	
	public void setURI(String uri) {
		this.uri = uri;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getScore() {
		
		return score;
	}

	private double getGoogleScore(String query_keywords) {
		return ((alpha * rate) + ((1 - alpha) * getGoogleCossineSim(query_keywords)));
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	public String getURI() {
		return uri;
	}
	
	public String getName() {
		return label;
	}
	
	public String getLat() {
		return lat;
	}

	protected void setLat(String lat) {
		this.lat = lat;
	}

	public String getLgt() {
		return lgt;
	}

	protected void setLgt(String lgt) {
		this.lgt = lgt;
	}

	protected void setName(String name) {
		this.label = name;
	}	

	public String getCompleteDescription() {
		if(completeDescription == null)
			completeDescription = id + " " + label + " " + lat + " " + lgt + " " + score;
		return completeDescription;
	}

	public void setCompleteDescription(String completeDescription) {
		this.completeDescription = completeDescription;
	}	

	public SpatialObject getBestNeighbor() {
		return bestNeighbor;
	}

	public void setBestNeighbor(SpatialObject bestNeighbor) {
		this.isFeature = false;
		this.bestNeighbor = bestNeighbor;
	}
	
//	public Resource getLgdResource() {
//		return lgdResource;
//	}
//
//	public void setLgdResource(Resource lgdResource) {
//		this.lgdResource = lgdResource;
//	}
	
	public double getParetoProbability() {
		return paretoProbability;
	}

	public void setParetoProbability(double paretoProbability) {
		this.paretoProbability = paretoProbability;
	}
	
	public int getNumberCheckin() {
		return numberCheckin;
	}

	public void setNumberCheckin(int numberCheckin) {
		this.numberCheckin = numberCheckin;
	}

	public int compareTo(Object other) {
		if (other instanceof SpatialObject) {
			SpatialObject otherDocument = (SpatialObject) other;
			double thisScore = this.getScore();
			double otherScore = otherDocument.getScore();
			if (thisScore < otherScore) {
				return -1;
			} else if (thisScore > otherScore) {
				return 1;
			} else {// They are equals
				SpatialObject outro = (SpatialObject) other;
				return this.getId() - outro.getId();
			}
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
