package node;

//https://weka.wikispaces.com/Use+WEKA+in+your+Java+code
//http://weka.wikispaces.com/Use+Weka+in+your+Java+code
//hote code: http://forums.pentaho.com/showthread.php?198222-Numerical-and-nominal

//http://www.programcreek.com/java-api-examples/index.php?api=weka.classifiers.CheckClassifier

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import similarity.LDSD;
import similarity.LDSD_LOD;
import similarity.NodeSim;
import util.StringUtilsNode;



public class Classifier {
	
	public static void main(String[] args) {
		try {
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param nodeSimilarityMap
	 * @param k
	 * @return
	 */
	public static String getKNNLabelByMajoritaryVoting(Map<Node,Double> nodeSimilarityMap,int k){

		nodeSimilarityMap = StringUtilsNode.sortByValueDescending(nodeSimilarityMap);
		
		int count = 0;
		double like = 0;
		double dislike = 0;		
		for (Node node : nodeSimilarityMap.keySet()) {
			if (count==k) {
				break;
			}
			if (node.getLabel().equals(IConstants.LIKE)) {
				like = like + nodeSimilarityMap.get(node);
			}else if (node.getLabel().equals(IConstants.DISLIKE)) {
				dislike = dislike + nodeSimilarityMap.get(node);
			}
			count++;
		}
		//System.out.println("like"+like);
		//System.out.println("dislike"+dislike);
		if (like>dislike) {
			return IConstants.LIKE;	
		}else{
			return IConstants.DISLIKE;
		}
	}
	

	/**
	 * @param nodeTest
	 * @param nodeSimilarityMap
	 * @return
	 */
	public static String getMaxMinDistance(Node nodeTest, Map<Node,Double> nodeSimilarityMap){
		nodeSimilarityMap = StringUtilsNode.sortByValueAscending(nodeSimilarityMap);
		
		//System.out.println("nodeSimilarityMap "+nodeSimilarityMap);
		
		Node nearestNeigbour = null;
		Node nearestOfNearestNeigbour = null;
		
		
		Map<Node, Double> directNeighboursMap  = new HashMap<Node, Double>();
		Map<Node, Double> indirectNeighboursMap  = new HashMap<Node, Double>();

		Set<Node> labelledNodes = NodeUtil.getLabeledNodes(nodeTest.getNodes());
		if (labelledNodes.isEmpty()) {
			return nodeTest.getLabel();
		}
		for (Node neighbour : labelledNodes) {
			double dist = Double.MAX_VALUE;
			if (nodeSimilarityMap.get(neighbour)==null) {
				if (IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA) {
					dist = calculateSemanticDistance(neighbour, nodeTest,IConstants.LDSD_LOD);
					//System.out.println("LOD node1 "+neighbour.getUri()+" LOD node 2 "+nodeTest.getUri()+" dist"+dist);
				}else if (IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
					dist = calculateSemanticDistance(neighbour, nodeTest,IConstants.LDSD);					
					//System.out.println("node1 "+neighbour.getUri()+" node 2 "+nodeTest.getUri()+" dist"+dist);
				}
				directNeighboursMap.put(neighbour, dist);
			}else{
				directNeighboursMap.put(neighbour, nodeSimilarityMap.get(neighbour));	
			}			
		}
		
		directNeighboursMap = StringUtilsNode.sortByValueAscending(directNeighboursMap);
		//System.out.println("directNeighboursMap "+directNeighboursMap);
		
		for (Node node : directNeighboursMap.keySet()) {
			nearestNeigbour =  node;
			break;
		}
		
		//System.out.println("nearestNeigbour "+nearestNeigbour);
		
		List<Node> nodes = new ArrayList<Node>();
		if (nearestNeigbour.getNodes()!=null) {
			nodes = new ArrayList<Node>(NodeUtil.getLabeledNodes(nearestNeigbour.getNodes()));
		}
		LodicaOldVersion.contructRelationalUnobservedAttributesBasedOnNeighbourhood(nodes);
		//for (Node indirectIndirectNeighbour : ICAFredFinal.getLabeledNodes(nearestNeigbour.getNodes())) {
		for (Node indirectIndirectNeighbour : nodes) {
			double dist = Double.MAX_VALUE;
			if (IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA) {
				dist = calculateSemanticDistance(nearestNeigbour, indirectIndirectNeighbour,IConstants.LDSD_LOD);
				//System.out.println("LOD node1 "+nearestNeigbour.getUri()+" LOD node 2 "+indirectIndirectNeighbour.getUri()+" dist"+dist);
			}else if (IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
				dist = calculateSemanticDistance(nearestNeigbour, indirectIndirectNeighbour,IConstants.LDSD);				
				//System.out.println("node1 "+nearestNeigbour.getUri()+" node 2 "+indirectIndirectNeighbour.getUri()+" dist"+dist);
			}
			indirectNeighboursMap.put(indirectIndirectNeighbour, dist);
		}
		
		indirectNeighboursMap = StringUtilsNode.sortByValueAscending(indirectNeighboursMap);
		
		//System.out.println("indirectNeighboursMap "+indirectNeighboursMap);
		
		for (Node node : indirectNeighboursMap.keySet()) {
			nearestOfNearestNeigbour =  node;
			break;
		}
		
		double directNeighboursDistance  = directNeighboursMap.get(nearestNeigbour);
		
		double indirectNeighbourDistance = 1d;
	
		if (IConstants.N>1 && nearestOfNearestNeigbour!=null) {
			indirectNeighbourDistance = indirectNeighboursMap.get(nearestOfNearestNeigbour);	
		}

		if (directNeighboursDistance <= indirectNeighbourDistance) {
			return IConstants.LIKE;	
		}else{
			return nodeTest.getLabel();
			//return ILabel.DISLIKE;
		}
	}	

	
	/**
	 * @param nodeSimilarityMap
	 * @param k
	 * @return
	 */
	public static String getKNNLabelByMinoritaryVoting(Map<Node,Double> nodeSimilarityMap,int k){

		nodeSimilarityMap = StringUtilsNode.sortByValueAscending(nodeSimilarityMap);
		
		int count = 0;
		double like = 0;
		double dislike = 0;		
		for (Node node : nodeSimilarityMap.keySet()) {
			if (count==k) {
				break;
			}
			if (node.getLabel().equals(IConstants.LIKE)) {
				like = like + nodeSimilarityMap.get(node);
			}else if (node.getLabel().equals(IConstants.DISLIKE)) {
				dislike = dislike + nodeSimilarityMap.get(node);
			}
			count++;
		}
		if (like<dislike) {
			return IConstants.LIKE;	
		}else{
			return IConstants.DISLIKE;
		}
	}
	
		
	
	/**
	 * @param testNodes
	 * @param trainingNodes
	 * @return
	 */
	

	/**
	 * @param testNodes
	 * @param trainingNodes
	 * @return
	 */
	public static List<NodePrediction> classifyOriginal(List<Node> testNodes, List<Node> trainingNodes) {
		List<NodePrediction> nodePredictions = new ArrayList<NodePrediction>();
		DecimalFormat df = new DecimalFormat("#.####");

		for (Node testNode : testNodes) {
			double classificationScoreTestNode = 0d;
			Map<Node,Double> nodeSimilarityMap = new HashMap<Node,Double>();
			for (Node trainingNode : trainingNodes) {
				//System.out.println("trainingNode.getUri() "+trainingNode.getUri());
				//System.out.println("testNode.getUri() "+testNode.getUri());
				if (IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA) {
					Double dist = calculateSemanticDistance(testNode, trainingNode,IConstants.LDSD_LOD);
					nodeSimilarityMap.put(trainingNode, dist);
					//Lodica.print("LOD  node1 "+trainingNode.getURI()+" LOD node 2 "+testNode.getURI()+" dist"+dist);
				}else if (IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
					Double dist = calculateSemanticDistance(testNode, trainingNode,IConstants.LDSD);	
					nodeSimilarityMap.put(trainingNode, dist);
					//Lodica.print("LDSD node1 "+trainingNode.getURI()+" LDSD node 2 "+testNode.getURI()+" dist"+dist);
				}else if (!IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
					//nodeSimilarityMap.put(trainingNode, NodeSim.nodeSimbyAllProperty(trainingNode, testNode));
					nodeSimilarityMap.put(trainingNode, NodeSim.nodeSimSum(trainingNode, testNode));
				}else if (!IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA){
					//nodeSimilarityMap.put(trainingNode, NodeSim.nodeSimbyAllProperty(trainingNode, testNode));
					nodeSimilarityMap.put(trainingNode, NodeSim.nodeSimSum(trainingNode, testNode));
				}
				//System.out.println("totalTrainingSetSimilarity "+   totalTrainingSetSimilarity);
				//System.out.println("nodeSimilarityMap.get(trainingNode) "+   nodeSimilarityMap.get(trainingNode));
				classificationScoreTestNode = classificationScoreTestNode + nodeSimilarityMap.get(trainingNode);
				//System.out.println("totalTrainingSetSimilarity "+   totalTrainingSetSimilarity);
			}
			
			//System.out.println("totalTrainingSetSimilarity "+   totalTrainingSetSimilarity);
			//System.out.println("trainingNodes.size() "+   trainingNodes.size());
			double distanceMean = (double)classificationScoreTestNode/(double)trainingNodes.size();
			
			distanceMean = Double.valueOf(distanceMean);
			if (Double.isNaN(distanceMean)) {
				distanceMean = 0d;
			}

			//DEFINE THE NEW LABEL AFTER CLASSIFICAION
			String newLabel = null;
			if (IConstants.USE_MAX_MIN_CLASSIFIER) {
				newLabel = getMaxMinDistance(testNode,nodeSimilarityMap);	
			}

			//System.out.println("totalTrainingSetSimilarity final "+   totalTrainingSetSimilarity);
			if (distanceMean >= IConstants.CLASSIFICATION_THRESHOLD) {
				NodePrediction nodePrediction = new NodePrediction(testNode, testNode.getLabel(),newLabel,Double.valueOf(df.format(distanceMean)));
				//Lodica.printPrediction(nodePrediction);
		        nodePredictions.add(nodePrediction);				
			}else{
				//NodePrediction nodePrediction = new NodePrediction(testNode, testNode.getLabel(),getOpositeLabel(newLabel),Double.valueOf(df.format(classificationMean)));
				NodePrediction nodePrediction = new NodePrediction(testNode, testNode.getLabel(),testNode.getLabel(),Double.valueOf(df.format(distanceMean)));
		        nodePredictions.add(nodePrediction);
			}
		}
		
		return nodePredictions;
	}

	public static Double calculateSemanticDistance(Node testNode, Node trainingNode, String type) {
		//Lodica.printNode(testNode);
		Double dist = null;
		if (Lodica.isLocalTest && type.equals(IConstants.LDSD)) {
			return LDSD.LDSDweighted(trainingNode.getURI(), testNode.getURI());
		}else if (Lodica.isLocalTest && type.equals(IConstants.LDSD_LOD)) {
			return LDSD_LOD.LDSDweighted(trainingNode.getURI(), testNode.getURI());
		}else{
			dist = Lodica.getDatabaseConnection().getLDSD(trainingNode.getURI(), testNode.getURI());
			if (dist==null) {
				dist = LDSD.LDSDweighted(trainingNode.getURI(), testNode.getURI());
				Lodica.getDatabaseConnection().insertSemanticDistance(trainingNode.getURI(), testNode.getURI(),type,dist);
			}
		}
		return dist;
	}		
	


	/**
	 * @param newLabel
	 * @return
	 */
	public static String getOpositeLabel(String newLabel){
		for (String label : IConstants.getLabels()) {
			if (!label.equals(newLabel)) {
				return label;
			}
		}
		return null;
	}
	

}
