package skpq;

public class ObjectInterest implements Comparable{
	
	private String uri;
	private double score;
	private int id;
	
	public ObjectInterest(int id, String uri){
		this.id = id;
		this.uri = uri;
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

	public void setScore(double score) {
		this.score = score;
	}
	
	 public String getURI() {
		return uri;
	}

	public int compareTo(Object other) {
	        if(other instanceof ObjectInterest){
	        	ObjectInterest otherDocument = (ObjectInterest) other;
	            double thisScore = this.getScore();
	            double otherScore = otherDocument.getScore();
	            if(thisScore > otherScore){
	                return 1;
	            }else if(thisScore < otherScore){
	                return -1;
	            }else{//They are equals
	            	ObjectInterest outro = (ObjectInterest) other;
	                return this.getId() -outro.getId();
	                
	            }
	        }
	        throw new UnsupportedOperationException("Not supported yet.");
	    }

}
