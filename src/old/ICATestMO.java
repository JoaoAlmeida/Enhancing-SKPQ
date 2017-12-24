package old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import knn.DataTransferTest;
import knn.WekaKNN;
import node.IConstants;
import node.Node;
import node.NodePrediction;
import similarity.CBSim;
import similarity.LDSD;

public class ICATestMO {
	
	public static void main(String[] args) {
		ICATestMO ica = new ICATestMO();
		ica.runICA();
	}

	public void runICA() {

		List<Node> allnodes = new ArrayList<Node>();

		List<Node> originalUnlabelledNodes = new ArrayList<Node>();

		Node node1 = new Node("1", IConstants.LIKE, LDSD.resourceA);
		Node node2 = new Node("2",null, LDSD.resourceB);
		Node node3 = new Node("3",null, LDSD.resourceC);
		Node node4 = new Node("4", IConstants.DISLIKE, LDSD.resourceD);
		Node node5 = new Node("5",null, LDSD.resourceE);

		connect2Nodes(node1, node2);
		connect2Nodes(node1, node4);
		connect2Nodes(node1, node3);

		connect2Nodes(node2, node4);
		connect2Nodes(node2, node5);
				
		connect2Nodes(node3, node4);			

		
/*		node1.loadObservedFeatures();
		node2.loadObservedFeatures();
		node3.loadObservedFeatures();
		node4.loadObservedFeatures();
		node5.loadObservedFeatures();*/
		
		

		/*node1.setAttribute1("1");
		node1.setAttribute2("1");
		node1.setAttribute3("1");
		node1.setAttribute4("1");

		node2.setAttribute1("1");
		node2.setAttribute2("1");
		node2.setAttribute3("0");
		node2.setAttribute4("1");		

		node3.setAttribute1("1");
		node3.setAttribute2("0");
		node3.setAttribute3("0");
		node3.setAttribute4("1");		

		node4.setAttribute1("1");
		node4.setAttribute2("0");
		node4.setAttribute3("0");
		node4.setAttribute4("0");
		
		node5.setAttribute1("0");
		node5.setAttribute2("1");
		node5.setAttribute3("1");
		node5.setAttribute4("0");*/

			

		allnodes.add(node1);
		allnodes.add(node2);	
		allnodes.add(node3);
		allnodes.add(node4);
		allnodes.add(node5);

		
		originalUnlabelledNodes = this.getUnlabledNodes(allnodes);

		
		System.out.println("------------------------------------BOOTSTRAPPING----------------------------------------- \n");
		//bootstrapByRandomLabelling(allnodes);
		bootstrapByInformedLabel(allnodes,IConstants.LIKE);
		printNodes(allnodes);
		System.out.println("");
		
		System.out.println("------------Step 1: INITIAL CLASSIFICATION WITH ONLY OBSERVED ATTRIBUTES------------------ \n");
		//Classify using only observed attributes
		IConstants.USE_ICA = false;
		classifyUsingByObservedAttributes(originalUnlabelledNodes, allnodes,false, false);
		IConstants.USE_ICA = true;
		System.out.println("");
		
		System.out.println("------------Step 2: ITERATIVE CLASSIFICATION USING ALL ATTRIBUTES-------------------------- \n");
		System.out.println();
		int numberOfIteraction = 3;
		for (int i = 0; i < numberOfIteraction; i++) {
			System.out.println("ITERATION "+(i+1)+"/"+numberOfIteraction+"--------------------------------------------- \n");
			if (!IConstants.computeSemanticSimilarityOfRelationalAttributes) {
				contructRelationalAttributesBasedOnLinkedNeighbours(allnodes);
				printRelationalAttributesForAllNodes(allnodes);
			}
			
			classifyUsingByObservedAttributes(originalUnlabelledNodes, allnodes,true, true);	
		}
		
		//contructRelationalAttributes(allnodes);
		// printRelationalAttributesForAllNodes(allnodes);
		// classifyUsingByObservedAttributes(originalUnlabelledNodes,
		// allnodes,false,false);
		// classifyUsingByObservedAttributes(originalUnlabelledNodes,
		// allnodes,false,true);
		//classifyUsingByObservedAttributes(originalUnlabelledNodes, allnodes,true, true);

	}
	
	
	/**
	 * @param node
	 * @param nodes
	 * @return
	 */
	public List<Node> selectMostSemanticLimilarNodesForTraining(Node node, List<Node> nodes){
		List<Node> selectedNodes = new ArrayList<Node>();
		for (Node nodeTraing : nodes) {
			double semanticSimilarity = 1 -(LDSD.LDSDregular(node.getURI(), nodeTraing.getURI()));
			System.out.println("SEMANTIC SIMILARITY:"+node.getURI().replace("http://dbpedia.org/resource/","")+ " X " + nodeTraing.getURI().replace("http://dbpedia.org/resource/","")+ " == "+semanticSimilarity);
			if (semanticSimilarity > IConstants.semanticSimilarityThreshold) {
				selectedNodes.add(nodeTraing);
			}
		}
		
		return selectedNodes;
		
	}
	
	

	/**
	 * @param originalUnlabelledNodes
	 * @param nodes
	 * @param useRelationalAttributes
	 * @param addPredictedLabel
	 */
	private void classifyUsingByObservedAttributes(
			List<Node> originalUnlabelledNodes, List<Node> nodes,
			boolean useRelationalAttributes, boolean addPredictedLabel) {
		
		List<Node> unLabelledNodesForTesting = new ArrayList<Node>();
		List<Node> labeledNodesForTraining = new ArrayList<Node>();
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();

		for (Node node : nodes) {
			for (Node originalUnlabelledNode : originalUnlabelledNodes) {
				if (node.getId().equals(originalUnlabelledNode.getId())) {
					unLabelledNodesForTesting.add(node);
				}
			}
		}
		
		if (IConstants.USE_ICA) {

			for (Node nodeTest : unLabelledNodesForTesting) {
				List<Node> nodeTests = new ArrayList<Node>();
				nodeTests.add(nodeTest);
				labeledNodesForTraining = nodeTest.getNodes();
				
				//SEMANTIC SIMILARITY
				//labeledNodesForTraining = calculateSemanticSimilarity(
					//	labeledNodesForTraining, nodeTests);
				
				System.out.println("CLASSIFYING TEST NODE"+nodeTests.toString() +" using NEIGHBOURS AS TRAINING NODES"+labeledNodesForTraining.toString());
				System.out.println();
				List<NodePrediction> nodePredictions = callClassifier(useRelationalAttributes, addPredictedLabel,nodeTests, labeledNodesForTraining);
				//labeledNodesForTraining.add(nodeTest);
				
				//This creates a memory of predictions for latter labeling
				nodePredictions.get(0).setNode(nodeTest);
				memoryPredictions.add(nodePredictions.get(0));
			}
		
		} else if (IConstants.useNeighboursSelectedBySemanticSimilarityForTraningSet) {
			
			for (Node nodeTest : unLabelledNodesForTesting) {
				List<Node> nodeTests = new ArrayList<Node>();
				nodeTests.add(nodeTest);
				nodes.removeAll(nodeTests);
				labeledNodesForTraining = nodes;
				
				//SEMANTIC SIMILARITY
				labeledNodesForTraining = calculateSemanticSimilarity(
						labeledNodesForTraining, nodeTests);
				
				System.out.println("CLASSIFYING TEST NODE"+nodeTests.toString() +" using TRAINING NODES"+labeledNodesForTraining.toString());
				System.out.println();
				List<NodePrediction> nodePredictions = callClassifier(useRelationalAttributes, addPredictedLabel,nodeTests, labeledNodesForTraining);
				labeledNodesForTraining.add(nodeTest);
				
				//This creates a memory of predictions for latter labeling
				nodePredictions.get(0).setNode(nodeTest);
				memoryPredictions.add(nodePredictions.get(0));
			}	
			
		} else if (IConstants.computeSemanticSimilarityOfRelationalAttributes) {
			
			for (Node nodeTest : unLabelledNodesForTesting) {
				List<Node> nodeTests = new ArrayList<Node>();
				nodeTests.add(nodeTest);
				labeledNodesForTraining = nodeTest.getNodes();
				
				//compute Semantic Similarity Of Relational Attributes
				contructRelationalAttributesBasedOnSimilarity(nodeTests,labeledNodesForTraining);
				
				System.out.println("CLASSIFYING TEST NODE"+nodeTests.toString() +" using TRAINING NODES"+labeledNodesForTraining.toString());
				System.out.println();
				List<NodePrediction> nodePredictions = callClassifier(useRelationalAttributes, addPredictedLabel,nodeTests, labeledNodesForTraining);
				labeledNodesForTraining.add(nodeTest);
				
				//This creates a memory of predictions for latter labeling
				nodePredictions.get(0).setNode(nodeTest);
				memoryPredictions.add(nodePredictions.get(0));
			}
		}
			
		
		//Labels updated after classification STEP2a
		updateLabelsAfterClassification(memoryPredictions);
		
	}

	/**
	 * @param nodeTests
	 * @param trainingNodes
	 */
	private void contructRelationalAttributesBasedOnSimilarity(List<Node> nodeTests, List<Node> trainingNodes) {

		for (Node testNode : nodeTests) {
			testNode.relationalFeatures = new HashMap<String, Double>();
			//iterating over neighbor nodes
			for (Node trainingNode : trainingNodes) {
				trainingNode.relationalFeatures = new HashMap<String, Double>();
				double cbsimilarityScore = CBSim.simVSMbyProperty(testNode.getURI(), trainingNode.getURI());
				trainingNode.relationalFeatures.put(IConstants.getLabels().get(0), cbsimilarityScore);
				double semanticSimilarityScore = LDSD.LDSDdirect(testNode.getURI(), trainingNode.getURI());
				trainingNode.relationalFeatures.put(IConstants.getLabels().get(1), semanticSimilarityScore);				
			}
			// normalizing the vector size
			for (String label : IConstants.getLabels()) {
				testNode.relationalFeatures.put(label,testNode.getTopValueByKey(label));
			}
		}
		
	}

	private List<Node> calculateSemanticSimilarity(
			List<Node> labeledNodesForTraining, List<Node> nodeTests) {
		//SEMANTIC PART
		if (IConstants.useSemanticSimilarityForSelectingTrainingSet) {
			List<Node> mostSimilarNodes = selectMostSemanticLimilarNodesForTraining(nodeTests.get(0),labeledNodesForTraining); 
			if (mostSimilarNodes.isEmpty()) {
				System.out.println("NOT ENOUGHT SEMANTIC SIMILARITY FOR BUILDING A TRAINING SET");		
			}else{
				labeledNodesForTraining = mostSimilarNodes;
			}
		}
		return labeledNodesForTraining;
	}

	/**
	 * @param memoryPredictions
	 */
	private void updateLabelsAfterClassification(
			List<NodePrediction> memoryPredictions) {
		System.out.println("AFTER CLASSIFICATION: \n");
		for (NodePrediction nodePrediction : memoryPredictions) {
			nodePrediction.getNode().setLabel(nodePrediction.getPredictedLabel());
			if (!nodePrediction.getEvaluationLabel().equals(nodePrediction.getPredictedLabel())) {
				System.out.println("NODE:"+nodePrediction.getNode().getId()+" PREVIOUS LABEL:"+nodePrediction.getEvaluationLabel() +"	PREDICTED_LABEL:"+nodePrediction.getPredictedLabel());				
			}else{
				System.out.println("NODE:"+nodePrediction.getNode().getId()+" PREDICTED SAME LABEL:	"+nodePrediction.getEvaluationLabel());
			}
		}
		System.out.println();
	}

	/**
	 * @param useRelationalAttributes
	 * @param addPredictedLabel
	 * @param unLabelledNodesForTesting
	 * @param labeledNodesForTraining
	 * @return
	 */
	private List<NodePrediction> callClassifier(boolean useRelationalAttributes,
			boolean addPredictedLabel, List<Node> unLabelledNodesForTesting,
			List<Node> labeledNodesForTraining) {
		
		List<NodePrediction> nodePredictions = null ;		
		try {

/*			nodePredictions =  WekaKNN
					.classifyWithOutCrossValidation(DataTransferTest
							.createTrainingTestDataset(
									new ArrayList<Node>(unLabelledNodesForTesting),
									new ArrayList<Node>(labeledNodesForTraining),
									useRelationalAttributes));*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodePredictions;
	}
	
	/**
	 * @param nodes
	 */
	public void contructRelationalAttributesBasedOnSimilarity(List<Node> nodes) {

		List<Node> localNnodes = nodes;
		for (Node node : localNnodes) {
			node.relationalFeatures = new HashMap<String, Double>();
			//iterating over neighbor nodes
			for (Node internalNode : node.getNodes()) {
				double cbsimilarityScore = CBSim.simVSMbyProperty(node.getURI(), internalNode.getURI());
				node.relationalFeatures.put(IConstants.getLabels().get(0), cbsimilarityScore);
				double semanticSimilarityScore = LDSD.LDSDdirect(node.getURI(), internalNode.getURI());
				node.relationalFeatures.put(IConstants.getLabels().get(1), semanticSimilarityScore);				
			}
			// normalizing the vector size
			for (String label : IConstants.getLabels()) {
				if (!node.relationalFeatures.containsKey(label)) {
					node.relationalFeatures.put(label, 0d);
				}

			}
		}
	}

	/**
	 * @param nodes
	 */
	public void contructRelationalAttributesBasedOnLinkedNeighbours(List<Node> nodes) {

		List<Node> localNnodes = nodes;
		for (Node node : localNnodes) {
			node.relationalFeatures = new HashMap<String, Double>();
			//iterating over neighbor nodes
			for (Node internalNode : node.getNodes()) {
				if (node.relationalFeatures.containsKey(internalNode.getLabel())) {
					double oldValue = (double)node.relationalFeatures.get(internalNode.getLabel());
					node.relationalFeatures.replace(internalNode.getLabel(),oldValue, ++oldValue);
				} else {
					node.relationalFeatures.put(internalNode.getLabel(), 1d);
				}
			}
			// normalizing the vector size
			for (String label : IConstants.getLabels()) {
				if (!node.relationalFeatures.containsKey(label)) {
					node.relationalFeatures.put(label, 0d);
				}

			}
		}
	}

	public void bootstrapByRandomLabelling(List<Node> nodes) {
		for (Node node : nodes) {
			if (node.isUnlabeled()) {
				node.setLabel(IConstants.getLabels().get(
						new Random().nextInt(IConstants.getLabels().size())));
			}
		}
	}
	
	public void bootstrapByInformedLabel(List<Node> nodes, String label) {
		for (Node node : nodes) {
			if (node.isUnlabeled()) {
				node.setLabel(label);
			}
		}
	}	

	public void printNodes(List<Node> nodes) {
		for (Node node : nodes) {
			printNode(node);
		}
	}

	public void printRelationalAttributesForAllNodes(List<Node> nodes) {
		for (Node node : nodes) {
			this.printRelationalAttributes(node);
		}
	}

	public void printNode(Node node) {
		System.out.println("NodeID:" + node.getId() + "	label:"
				+ node.getLabel() + "	links:" + node.getNodes().toString());
	}

	public void printRelationalAttributes(Node node) {
		System.out.println("NodeID:" + node.getId() + "	label:"
				+ node.getLabel() + "	Relational Attributes:"
				+ node.getRelationalFeatures().toString());
		System.out.println();
	}

	public void bootstrapByStartNode(Node startNode) {
		for (Node node : this.getUnlabledNodes(startNode.getNodes())) {
			node.setLabel(startNode.getLabel());
		}
	}

	public void bootstrap2(List<Node> nodes) {

		for (Node unlablednode : this.getUnlabledNodes(nodes)) {

			Map<String, Integer> labelCountMap = new LinkedHashMap<String, Integer>();

			printNode(unlablednode);

			for (Node node : unlablednode.getNodes()) {
				if (labelCountMap.keySet().contains(node.getLabel())) {
					int labelCount = labelCountMap.get(node.getLabel());
					labelCountMap.remove(node.getLabel());
					labelCountMap.put(node.getLabel(), labelCount + 1);
				} else {
					labelCountMap.put(node.getLabel(), 1);
				}
			}

			System.out.println(labelCountMap);

			labelCountMap = sortByValue(labelCountMap);
			unlablednode.setLabel((String) labelCountMap.keySet().toArray()[0]);
		}
	}

	/**
	 * @param node
	 * @return
	 */
	public List<Node> getLabeledNodes(List<Node> nodes) {

		List<Node> labeledNodes = new ArrayList<Node>();

		for (Node localNode : nodes) {
			if (localNode.getLabel() != null) {
				labeledNodes.add(localNode);
			}
		}

		return labeledNodes;
	}

	/**
	 * @param node
	 * @return
	 */
	public List<Node> getLinkedLabeledNodes(Node node) {

		List<Node> linkedLabeledNodes = new ArrayList<Node>();

		for (Node localNode : node.getNodes()) {
			if (localNode.getLabel() != null) {
				linkedLabeledNodes.add(node);
			}
		}

		return linkedLabeledNodes;
	}

	/**
	 * @param node
	 * @return
	 */
	public List<Node> getLinkedUnLabeledNodes(Node node) {

		List<Node> linkedUnLabeledNodes = new ArrayList<Node>();

		for (Node localNode : node.getNodes()) {
			if (localNode.getLabel() == null) {
				linkedUnLabeledNodes.add(node);
			}
		}

		return linkedUnLabeledNodes;
	}

	public List<Node> getUnlabledNodes(List<Node> nodes) {
		List<Node> unlablednodes = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node.getLabel() == null) {
				unlablednodes.add(node);
			}
		}
		return unlablednodes;
	}

	public void connect2Nodes(Node nodeOut, Node nodeIn) {
		nodeOut.addNode(nodeIn);
		nodeIn.addNode(nodeOut);
	}

	public void connect3Nodes(Node node1, Node node2, Node node3) {
		node1.addNode(node2);
		node2.addNode(node1);

		node1.addNode(node3);
		node3.addNode(node3);

		node2.addNode(node3);
		node3.addNode(node2);
	}

	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
