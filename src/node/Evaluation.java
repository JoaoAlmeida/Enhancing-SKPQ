package node;


public class Evaluation {
	

	public Evaluation(String uri,int correct, int incorrect,String similarityMethod,int userId) {
		super();
		this.similarityMethod = similarityMethod;
		this.userId = userId;
		this.correct = correct;
		this.incorrect = incorrect;
		this.uri = uri;
	}

	String similarityMethod;
	
	int userId;
	
	int correct;
	
	int incorrect;
	
	String uri;	
	
	public String getSimilarityMethod() {
		return similarityMethod;
	}

	public void setSimilarityMethod(String similarityMethod) {
		this.similarityMethod = similarityMethod;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getCorrect() {
		return correct;
	}

	public void setCorrect(int correct) {
		this.correct = correct;
	}

	public int getIncorrect() {
		return incorrect;
	}

	public void setIncorrect(int incorrect) {
		this.incorrect = incorrect;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
