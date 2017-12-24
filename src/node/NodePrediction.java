package node;


public class NodePrediction implements Comparable<NodePrediction> {
	
	
	public NodePrediction(String evaluationLabel, String predictedLabel) {
		super();
		this.evaluationLabel = evaluationLabel;
		this.predictedLabel = predictedLabel;
	}
	
	public NodePrediction(Node node, String evaluationLabel, String predictedLabel, Double predictionScore) {
		super();
		this.node = node;
		this.evaluationLabel = evaluationLabel;
		this.predictedLabel = predictedLabel;
		this.predictionScore = predictionScore;
	}
	
	
	public NodePrediction(Node node, String evaluationLabel, String predictedLabel, String similarityMethod, Double predictionScore, int userId) {
		super();
		this.node = node;
		this.evaluationLabel = evaluationLabel;
		this.predictedLabel = predictedLabel;
		this.predictionScore = predictionScore;
		this.similarityMethod = similarityMethod;
		this.userId = userId;
	}	
	
	public NodePrediction(String seed, Node node, String evaluationLabel, String predictedLabel, String similarityMethod, Double predictionScore, int userId) {
		super();
		this.seed = seed;
		this.node = node;
		this.evaluationLabel = evaluationLabel;
		this.predictedLabel = predictedLabel;
		this.predictionScore = predictionScore;
		this.similarityMethod = similarityMethod;
		this.userId = userId;
	}	
	
	public NodePrediction(String evaluationLabel, String predictedLabel, Double predictionScore) {
		super();
		
		this.evaluationLabel = evaluationLabel;
		this.predictedLabel = predictedLabel;
		this.predictionScore = predictionScore;
	}	

	public String getEvaluationLabel() {
		return evaluationLabel;
	}

	public void setEvaluationLabel(String evaluationLabel) {
		this.evaluationLabel = evaluationLabel;
	}

	public String getPredictedLabel() {
		return predictedLabel;
	}

	public void setPredictedLabel(String predictedLabel) {
		this.predictedLabel = predictedLabel;
	}

	Node node;
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	String seed;
	
	String evaluationLabel;
	
	String predictedLabel;
	
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

	String similarityMethod;
	
	int userId;
	
	Double predictionScore;

	public Double getPredictionScore() {
		return predictionScore;
	}

	public void setPredictionScore(Double predictionScore) {
		this.predictionScore = predictionScore;
	}

	@Override
	public int compareTo(NodePrediction o) {
	   if (this.predictionScore > o.predictionScore) {
		  return -1;
	   } else if (this.predictionScore < o.predictionScore) {
		  return 1;
	   }else{
		  return 0;
	   }
	}
	
	
}
