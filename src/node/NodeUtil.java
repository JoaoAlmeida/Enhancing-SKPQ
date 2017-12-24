package node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import xml.WriteXMLFile;

public class NodeUtil {

	public static List<Node> convertResourceInNodes(List<Resource> resources, int idCount, boolean checkLabel, List<Node> cnns) {

		List<Node> nodes = new LinkedList<Node>();

		int count = idCount + 1;

		if (cnns == null) {
			cnns = new ArrayList<Node>();
		}
		for (Resource resource : resources) {
			// NodeUtil.print("resource.getURI())"+resource.getURI());
			Node node = null;
			Node existing = NodeUtil.getNodeByURI(resource.getURI(), cnns);
			if (existing == null && !checkLabel) {
				node = new Node("" + count, IConstants.NO_LABEL, resource.getURI());
				cnns.add(node);
				nodes.add(node);
			} else if (checkLabel && existing != null && IConstants.getValidLabel(existing.getLabel()) != null) {
				node = existing;
				nodes.add(node);
			} else if (!checkLabel) {
				node = existing;
				nodes.add(node);
			}
			count++;
		}
		return nodes;
	}
	
	public static List<Node> convertResourceInNodes(List<Resource> resources, int idCount, String label, boolean checkLabel, List<Node> cnns) {

		List<Node> nodes = new LinkedList<Node>();

		int count = idCount + 1;

		if (cnns == null) {
			cnns = new ArrayList<Node>();
		}
		for (Resource resource : resources) {
			// NodeUtil.print("resource.getURI())"+resource.getURI());
			Node node = null;
			Node existing = NodeUtil.getNodeByURI(resource.getURI(), cnns);
			if (existing == null && !checkLabel) {
				node = new Node("" + count, label, resource.getURI());
				cnns.add(node);
				nodes.add(node);
			} else if (checkLabel && existing != null && IConstants.getValidLabel(existing.getLabel()) != null) {
				node = existing;
				nodes.add(node);
			} else if (!checkLabel) {
				node = existing;
				nodes.add(node);
			}
			count++;
		}
		return nodes;
	}	
	

	/**
	 * @param memoryPredictions
	 */
	public static void updateLabelsAfterClassification(List<NodePrediction> memoryPredictions) {
		//NodeUtil.print();
		//NodeUtil.print("AFTER CLASSIFICATION: \n");
		Collections.sort(memoryPredictions);
		for (NodePrediction nodePrediction : memoryPredictions) {
			nodePrediction.getNode().setLabel(nodePrediction.getPredictedLabel());
		}
	}
	
	public static void printPredictions(List<NodePrediction> predictions) {
		for (NodePrediction prediction : predictions) {
			printPrediction(prediction);
		}
	}

	private static void printPredictionNewLabel(NodePrediction nodePrediction) {
		NodeUtil.print("NODE:" + nodePrediction.getNode().getId() + ":"
				+ nodePrediction.getNode().getURI() + "\tSEED:"
				+ nodePrediction.getSeed() + "\tSCORE:"
				+ nodePrediction.getPredictionScore() + "\tPREVIOUS LABEL:"
				+ nodePrediction.getEvaluationLabel() + "\tPREDICTED_LABEL:"
				+ nodePrediction.getPredictedLabel());
				NodeUtil.print();
	}
	
	private static void printPredictionSameLabel(NodePrediction nodePrediction) {
		NodeUtil.print("NODE:" + nodePrediction.getNode().getId() + ":"
				+ nodePrediction.getNode().getURI() + "\tSEED:"
				+ nodePrediction.getSeed() + "\tSCORE:"
				+ nodePrediction.getPredictionScore() + "\tPREDICTED SAME LABEL:"
				+ nodePrediction.getEvaluationLabel());
		NodeUtil.print();		
	}	

	/**
	 * @param memoryPredictions
	 */
	public static void updateLabelsAfterClassification(NodePrediction memoryPrediction) {
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();
		memoryPredictions.add(memoryPrediction);
		updateLabelsAfterClassification(memoryPredictions);
	}

	public void bootstrapByRandomLabeling(List<Node> nodes) {
		for (Node node : nodes) {
			if (node.isUnlabeled()) {
				node.setLabel(IConstants.getLabels().get(new Random().nextInt(IConstants.getLabels().size())));
			}
		}
	}

	public static void bootstrapNullLabelsNodes(List<Node> nodes, String label) {
		for (Node node : nodes) {
			if (node.isUnlabeled()) {
				node.setLabel(label);
			}
		}
	}

	public static void printNodes(List<Node> nodes) {
		//Collections.sort(nodes);
		for (Node node : nodes) {
			printNode(node);
		}
	}

	public static void printNodes(Set<Node> nodes) {
		Collections.sort(new ArrayList<Node>(nodes));
		for (Node node : nodes) {
			printNode(node);
		}
	}

	public static void print(String message) {
		System.out.println(message);
	}

	public static void print() {
		System.out.println();
	}

	public static void print(boolean message) {
		System.out.println(message);
	}

	public static void print(int message) {
		System.out.println("" + message);
	}

	public static void print(double message) {
		System.out.println("" + message);
	}

	public void printRelationalAndObservedAttributesForAllNodes(List<Node> nodes) {
		Collections.sort(nodes);
		for (Node node : nodes) {
			this.printRelationalAndObservedAttributesForAllNodes(node);
		}
	}

	public static void printPrediction(NodePrediction nodePrediction) {
		if (!nodePrediction.getEvaluationLabel().equals(nodePrediction.getPredictedLabel())) {
			printPredictionNewLabel(nodePrediction);
		} else {
			printPredictionSameLabel(nodePrediction);
		}
	}

	public void printRelationalAndObservedAttributesForAllNodes(Node node) {
		System.out.println("NodeID:" + node.getId() + "	label:" + node.getLabel() + "	Relational Attributes("
				+ node.getRelationalFeatures().size() + "):" + node.getRelationalFeatures().toString()
				+ "	Observed Attributes(" + node.getObservedAtrributes().size() + "):"
				+ node.getObservedAtrributes().toString());
		System.out.println();
	}

	public static void printRelationalAttributesForAllNodes(List<Node> nodes) {
		Collections.sort(nodes);
		for (Node node : nodes) {
			printRelationalAttributes(node);
		}
		System.out.println();
	}

	public static void printObservedAttributesForAllNodes(List<Node> nodes) {
		Collections.sort(nodes);
		for (Node node : nodes) {
			printObservedAttributes(node);
		}
	}

	public static void printNode(Node node) {
		System.out.printf("" + "NodeID:" + node.getId() + "\tURI: " + new ResourceImpl(node.getURI()) + "\tlabel:"
				+ node.getLabel() + "\t direct links:" + node.getNodes().toString());
		System.out.println();
	}

	public static void printRelationalAttributes(Node node) {
		System.out.printf("NodeID:" + node.getId() + "	label:" + node.getLabel() + "	Relational Attributes("
				+ node.getRelationalFeatures().size() + "):" + node.getRelationalFeatures().toString());
		System.out.println();
	}

	public static void printObservedAttributes(Node node) {
		System.out.printf("" + "NodeID:" + node.getId() + "\tURI: " + new ResourceImpl(node.getURI()) + "	label:"
				+ node.getLabel() + "	Observed Attributes(" + node.getObservedAtrributes().size() + "):"
				+ node.getObservedAtrributes().toString());
		System.out.println();

	}
	
	public static void pritEvaluation(Evaluation evaluation) {
		NodeUtil.print("Node  "+evaluation.getUri()+" of user "+evaluation.getUserId()+" has "+evaluation.getCorrect()+" correct prediction(s) and "+evaluation.getIncorrect()+" incorrect prediction(s) using similarity method "+evaluation.getSimilarityMethod());
	}
	
	/**
	 * @param startNode
	 */
	public void bootstrapByStartNode(Node startNode) {
		for (Node node : getUnlabeledNodes(startNode.getNodes())) {
			node.setLabel(startNode.getLabel());
		}
	}

	public static Set<Node> getUnlabeledNodes(List<Node> nodes) {
		Set<Node> unlablednodes = new HashSet<Node>();
		for (Node node : nodes) {
			if (node.getLabel() == null || node.getLabel().equals(IConstants.NO_LABEL)) {
				unlablednodes.add(node);
			}
		}
		return unlablednodes;
	}

	public static void connect2Nodes(Node nodeOut, Node nodeIn) {
		if (!nodeIn.getNodes().contains(nodeOut) && (!nodeOut.getNodes().contains(nodeIn))) {
			nodeIn.addNode(nodeOut);
			nodeOut.addNode(nodeIn);
		}

	}

	public void connect3Nodes(Node node1, Node node2, Node node3) {
		node1.addNode(node2);
		node2.addNode(node1);

		node1.addNode(node3);
		node3.addNode(node3);

		node2.addNode(node3);
		node3.addNode(node2);
	}

	/**
	 * @param node
	 * @return
	 */
	public static Set<Node> getLabeledNodes(List<Node> nodes) {

		Set<Node> labeledNodes = new HashSet<Node>();
		for (Node localNode : nodes) {
			if (localNode.getLabel() != null && (IConstants.getLabels().contains(localNode.getLabel()))) {
				labeledNodes.add(localNode);
			}
		}

		return labeledNodes;
	}

	/**
	 * @param node
	 * @return
	 */
	public static Set<Node> getLabeledNodes(Set<Node> nodes) {

		Set<Node> labeledNodes = new HashSet<Node>();

		for (Node localNode : nodes) {
			if (localNode.getLabel() != null && (IConstants.getLabels().contains(localNode.getLabel()))) {
				labeledNodes.add(localNode);
			}
		}

		return labeledNodes;
	}

	/**
	 * @param node
	 * @return
	 */
	public static Set<Node> getLabeledNeighbourhood(Node node) {
		Set<Node> labelledNodes = new HashSet<Node>();
		Set<Node> tempNodes = new HashSet<Node>();
		tempNodes.add(node);
		for (int i = 0; i < IConstants.N; i++) {
			Set<Node> aux = null;
			for (Node nodeTemp : tempNodes) {
				aux = new HashSet<Node>();
				aux.addAll(getLabeledNodes(nodeTemp.getNodes()));
				labelledNodes.addAll(aux);
			}
			tempNodes = new HashSet<Node>(aux);
		}
		// printNodes(newNeighbours);
		return labelledNodes;
	}

	/**
	 * @param node
	 * @return
	 */
	public static void labelNodes(List<Node> nodes, String label) {
		for (Node localNode : nodes) {
			localNode.setLabel(label);
		}
	}

	public static void labelNodes(Set<Node> nodes, String label) {
		for (Node localNode : nodes) {
			localNode.setLabel(label);
		}
	}

	public static String getMaxNodeID(List<Node> cnns) {
		List<Integer> ids = new ArrayList<Integer>();
		if (cnns == null || cnns.isEmpty()) {
			return "1";
		}
		for (Node localNode : cnns) {
			ids.add(Integer.valueOf(localNode.getId()));
		}
		return "" + Collections.max(ids);
	}

	public static Node getNodeByURI(String uri, List<Node> cnns) {

		if (cnns == null) {
			return null;
		}
		for (Node node : cnns) {
			if (node.getURI().equals(uri)) {
				return node;
			}
		}
		return null;
	}

	public static Node getNodeByURI(String uri, Set<Node> nodes) {
		if (nodes == null) {
			return null;
		}

		for (Node node : nodes) {
			if (node.getURI().equals(uri)) {
				return node;
			}
		}
		return null;
	}

	public static Node getNodeByID(String id) {
		for (Node node : Lodica.cnns) {
			if (node.getId().equals(id)) {
				return node;
			}
		}
		return null;
	}

	public static Node getNodeByID(String id, Set<Node> nodes) {
		for (Node node : nodes) {
			if (node.getId().equals(id)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Checks if the neighbours are directly or indirectly linked in the LOD
	 * 
	 * @param nodeTest
	 * @param nodes
	 * @return
	 */
	public static Set<Node> getDirectAndIndirectLabeledNodes(Node nodeTest, List<Node> nodes) {
		Set<Node> labeledNodes = new HashSet<Node>();
		for (Node nodeExternalNode : NodeUtil.getLabeledNodes(new ArrayList<Node>(nodes))) {
			if (nodeExternalNode.getLabel() != null && (IConstants.getLabels().contains(nodeExternalNode.getLabel()))) {
				if (SparqlWalk.getCountDBpediaObjecstBySubjectBetween2Resources(nodeTest.getURI(),
						nodeExternalNode.getURI()) > 0) {
					labeledNodes.add(nodeExternalNode);
				} else if (SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween2Resources(nodeTest.getURI(),
						nodeExternalNode.getURI()) > 0) {
					labeledNodes.add(nodeExternalNode);
				} else if (SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween2Resources(nodeTest.getURI(),
						nodeExternalNode.getURI()) > 0) {
					labeledNodes.add(nodeExternalNode);
				}
			}
		}

		return labeledNodes;
	}

	/**
	 * Check if 2 nodes are different by URI
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static boolean isDistinctURI(Node node1, Node node2) {
		if (!node1.getURI().trim().equals(node2.getURI().trim())) {
			return true;
		} else {
			return false;
		}
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

	public static void labelXNodesRandomly(List<Node> nodes, int x, String label) {
		Random random = new Random();
		for (int i = 0; i < Math.min(nodes.size(), x); i++) {
			nodes.get(random.nextInt(nodes.size())).setLabel(label);
		}
	}

	public static void labelXNodesRandomly(List<Node> nodes, int x) {
		Random random = new Random();
		for (int i = 0; i < Math.min(nodes.size(), x); i++) {
			nodes.get(random.nextInt(nodes.size()))
					.setLabel(IConstants.getLabels().get(random.nextInt(IConstants.getLabels().size())));
		}
	}

	public static Set<String> getSetStringURI(Set<Node> userNodesProfile) {
		Set<String> stringUris = new HashSet<String>();
		for (Node node : userNodesProfile) {
			stringUris.add(node.getURI().trim());

		}
		return stringUris;
	}

	public static Set<Node> createNodesWithLabel(List<Resource> resources, String label) {
		Set<Node> labelledNodesLocal = new HashSet<Node>();
		for (Resource resource : resources) {
			labelledNodesLocal.add(new Node(label, resource.getURI()));
		}
		return labelledNodesLocal;
	}

	public static Set<Node> createNewLabeledNodesNotInNPlus(List<Resource> resources, String label,
			Set<Node> trainingSet, int newId) {
		Set<Node> labelledNodesLocal = new HashSet<Node>();
		for (Resource resource : resources) {
			Node newNode = null;
			if (newNode == null) {
				newNode = getNodeByURI(resource.getURI(), trainingSet);
			}
			if (newNode == null) {
				labelledNodesLocal.add(new Node(""+(++newId), label, resource.getURI()));
			} else {
				labelledNodesLocal.add(newNode);
			}

		}
		return labelledNodesLocal;
	}

	static public Map<Node, Set<Node>> neighboursPlus = new HashMap<Node, Set<Node>>();

	public static Set<Node> getNeighboursPlus(Map<Node, Set<Node>> neighboursPlus) {
		Set<Node> valuesNodes = new HashSet<Node>();
		for (Set<Node> values : neighboursPlus.values()) {
			valuesNodes.addAll(values);
		}
		return valuesNodes;
	}

	/**
	 * Very important method that removes items from a collection regardless the
	 * ids. Sometimes collections from database have distinct ids from items
	 * created in the Linked Data
	 * 
	 * @param nodes
	 * @param nodesToBeReduced
	 * @return
	 */
	public static Set<Node> removeNodeByURI(Set<Node> nodes, Set<Node> nodesToBeReduced) {
		Set<Node> userProfileReduced = new HashSet<Node>(nodes);
		for (Node nodeUserProfile : nodes) {
			for (Node trainingSetNode : nodesToBeReduced) {
				if (nodeUserProfile.getURI().equals(trainingSetNode.getURI())) {
					userProfileReduced.remove(nodeUserProfile);
				}

			}
		}
		return userProfileReduced;
	}

	/**
	 * Ensure that all nodes are of distinct URIs and Ids
	 * 
	 * @param nodes
	 * @throws Exception
	 */
	public static void checkForDistinctIdsAndURIs(List<Node> nodes) throws Exception {
		for (Node node : nodes) {
			for (Node node2 : nodes) {
				if ((!node.getId().equals(node2.getId())) && (node.getURI().equals(node2.getURI()))) {
					NodeUtil.print("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
					NodeUtil.print(node.getId());
					NodeUtil.print(node2.getId());
					NodeUtil.print(node.getURI());
					NodeUtil.print(node2.getURI());
					throw new Exception("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
				}
			}
		}
	}
	
	/**
	 * Ensure that all nodes are of distinct URIs and Ids
	 * 
	 * @param nodes
	 * @throws Exception
	 */
	public static void checkForDistinctIdsAndURIs(Set<Node> nodes) throws Exception {
		for (Node node : nodes) {
			for (Node node2 : nodes) {
				if ((!node.getId().equals(node2.getId())) && (node.getURI().equals(node2.getURI()))) {
					NodeUtil.print("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
					NodeUtil.print(node.getId());
					NodeUtil.print(node2.getId());
					NodeUtil.print(node.getURI());
					NodeUtil.print(node2.getURI());
					throw new Exception("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
				}
			}
		}
	}
	
	
	/**
	 * Ensure that all nodes are of distinct URIs
	 * 
	 * @param nodes
	 * @throws Exception
	 */
	public static void checkForDistinctURIs(List<Node> nodes, List<Node> nodes2) throws Exception {
		for (Node node : nodes) {
			for (Node node2 : nodes2) {
				if (node.getURI().equals(node2.getURI())) {
					NodeUtil.print("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
					NodeUtil.print(node.getId());
					NodeUtil.print(node2.getId());
					NodeUtil.print(node.getURI());
					NodeUtil.print(node2.getURI());
					throw new Exception("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
				}
			}
		}
	}
	
	/**
	 * Ensure that all nodes are of distinct URIs
	 * 
	 * @param nodes
	 * @throws Exception
	 */
	public static void checkForDistinctURIs(Set<Node> nodes, Set<Node> nodes2) throws Exception {
		for (Node node : nodes) {
			for (Node node2 : nodes2) {
				if (node.getURI().equals(node2.getURI())) {
					NodeUtil.print("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
					NodeUtil.print(node.getId());
					NodeUtil.print(node2.getId());
					NodeUtil.print(node.getURI());
					NodeUtil.print(node2.getURI());
					throw new Exception("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
				}
			}
		}
	}	
	
	/**
	 * Ensure that all nodes are of distinct URIs
	 * 
	 * @param nodes
	 * @throws Exception
	 */
	public static void checkForDistinctURIs(List<Node> nodes, Set<Node> nodes2) throws Exception {
		for (Node node : nodes) {
			for (Node node2 : nodes2) {
				if (node.getURI().equals(node2.getURI())) {
					NodeUtil.print("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
					NodeUtil.print(node.getId());
					NodeUtil.print(node2.getId());
					NodeUtil.print(node.getURI());
					NodeUtil.print(node2.getURI());
					throw new Exception("NODES WITH SAME URI IN THE DATASET - SOMETHING WRONG");
				}
			}
		}
	}	

	/**
	 * @param nodes
	 * @throws Exception
	 */
	public static void checkTrainingTestSet(Set<Node> trainingSet, Set<Node> testSet, List<Node> cnns)
			throws Exception {
		if (trainingSet.isEmpty()) {
			throw new Exception("Training Set is Empty. LODICA say good bye :(");
		}
		if (testSet.isEmpty()) {
			throw new Exception("Test Set is Empty. LODICA say good bye :(");
		}
		if (trainingSet.contains(testSet) || testSet.contains(testSet)) {
			throw new Exception("Trainset intersects TestSet");
		}
		if (cnns.size() != (trainingSet.size() + testSet.size())) {
			throw new Exception("allnodes size different from Trainset+TestSet Size");
		}
	}

	/**
	 * @param nodes
	 */
	public void makeAllNodesLinked(List<Node> nodes) {
		for (Node node : nodes) {
			for (Node node2 : nodes) {
				if (!node.getId().equals(node2.getId()) && !node.getNodes().contains(node2)
						&& !node2.getNodes().contains(node)) {
					NodeUtil.connect2Nodes(node, node2);
				}
			}
		}
	}

	/**
	 * @param nodes
	 */
	public void linkNodesFromLOD(List<Node> nodes) {
		for (Node node : nodes) {
			for (Node node2 : nodes) {
				if (!node.getId().equals(node2.getId()) && !node.getNodes().contains(node2)
						&& !node2.getNodes().contains(node)) {
					if (SparqlWalk.countDirectLinksBetween2Resources(node.getURI(), node2.getURI()) > 0) {
						NodeUtil.connect2Nodes(node, node2);
						continue;
					} else if (SparqlWalk.countDirectLinksBetween2Resources(node2.getURI(), node.getURI()) > 0) {
						NodeUtil.connect2Nodes(node2, node);
					}
				}
			}
		}
	}

	/**
	 * Plot a graph for a set of nodes
	 * 
	 * @param showGraph
	 * @param nodes
	 */
	public static void showGraph(boolean showGraph, List<Node> nodes) {
		if (showGraph) {
			WriteXMLFile.createXML(nodes);
			GraphView.start();
			// System.exit(0);
		}
	}

	/**
	 * @param uri
	 * @param label
	 */
	public static void updateLabelFromWeb(String uri, String label, List<Node> nodes) {
		Node node = getNodeByURI(uri, nodes);
		if (node == null) {
			NodeUtil.print("No label updates - nodes are null");
		} else {
			node.setLabel(label);
			// printNode(node);
		}

		LodicaOldVersion.externalClassifiedNodesForTrainingSet.add(node);
	}

	static public Map<Integer, String> stabMap = new HashMap<Integer, String>();

	public static boolean isStabel(int interation, Set<Node> cnns) {
		boolean stable = true;
		StringBuilder stabel = new StringBuilder();
		for (Node node : cnns) {
			stabel.append(node.getId() + ":" + node.getLabel());
		}
		stabMap.put(interation, stabel.toString());
		NodeUtil.print("stabMap " + stabMap);

		if (stabMap.size() <= IConstants.AMOUNT_OF_ITERATIONS_TO_STABALIZE) {
			return false;
		}

		int indexToCheck = stabMap.size() - IConstants.AMOUNT_OF_ITERATIONS_TO_STABALIZE;

		for (int i = indexToCheck; i < stabMap.size(); i++) {
			if (new ArrayList<String>(stabMap.values()).get(i).equals(stabMap)) {
				stable = stable && true;
			}
		}
		return stable;
	}

	public static void describeNode(Node node) {
		NodeUtil.print("Node:");
		printNode(node);
		NodeUtil.print();
		NodeUtil.print("Direct Nodes");
		printNodes(node.getNodes());
		NodeUtil.print();
		NodeUtil.print("Neighborhood");
	}

	private void labelXNodesRandomly(Integer max, Set<Node> cnns) {
		if (max != null) {
			// printNodes(allnodes);
			NodeUtil.print();
			int total = cnns.size() / max;
			NodeUtil.print(total);
			NodeUtil.labelXNodesRandomly(new ArrayList<Node>(cnns), total);
			NodeUtil.print();
			NodeUtil.printNodes(cnns);

		}
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
	public static List<String> getListByAttribute(String uriProperty, List<Node> cnns) {

		List<String> listByAttribute = new ArrayList<String>();

		for (Node localNode : cnns) {
			// NodeUtil.print("ss"+localNode.getObservedAtrributes().get(uriProperty));
			listByAttribute.add(localNode.getObservedAtrributes().get(uriProperty));
		}

		return listByAttribute;
	}

}
