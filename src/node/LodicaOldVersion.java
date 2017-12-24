package node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import database.DBFunctions;
import similarity.CBSim;
import similarity.LDSD;
import similarity.NodeSim;
import util.StringUtilsNode;

public class LodicaOldVersion implements Serializable {
	
	/**
	 * Map of neighbours out of training set.
	 */
	static public Map<Node,Set<Node>> neighboursPlus = new HashMap<Node,Set<Node>>();
	
	/**
	 * Includes the candidates and N-LIKE
	 */
	public static List<Node> cnns = null;
	
	/**
	 * Resources set as starting nodes
	 */
	public static List<Node> startingSet = new LinkedList<Node>();
	
	/**
	 * Set of resources that might be considered for training set
	 */
	public static Set<Node> externalClassifiedNodesForTrainingSet = new HashSet<Node>();
	
	/**
	 *  Neighbours out of training set are considered during the classification 
	 */
	public static Set<Node> neighboursOutOfCandidateSet = new HashSet<Node>();
	
	/**
	 * Set of liked resources that might be considered for training set
	 */
	public static Set<Node> userProfile = new HashSet<Node>();

	/**
	 * Enable any set of resources that might be considered for training set
	 */
	public static boolean useExternalClassifiedNodesForTrainingSet = false;
	
	/**
	 *  Enable neighbours out of training set are considered during the classification 
	 */
	public static boolean useNeighboursOutOfSetOfCandidateSet = true;
	
	/**
	 * Enable starting nodes to be classified by user profile or any other information.
	 */
	public static boolean clasiffyStartingNodes = false;

	public static boolean spamSet = false;
	
	/**
	 * Set of labeled nodes used for training set.
	 */
	public static Set<Node> originalLabelledNodes = null;
	
	/**
	 * Set of unlabeled nodes used for test set. This is actually the candidate set.
	 */
	public static Set<Node> originalUnlabelledNodesToClassify = null;
	
	/**
	 * round of the classification when navigating from web interface
	 */
	public static byte round = 0;	

	private static final long serialVersionUID = 1L;
	
	/**
	 * Used to penalize similarity of resources of same type considering the local data set
	 */
	public static Map<String,Set<String>> domainDatasetPropertySize = new HashMap<String,Set<String>>();

	/**
	 * Used to penalize similarity of resources of same type considering the ontology
	 */
	public static Map<String,Integer> domainOntologyDataTypePropertySize = new HashMap<String,Integer>();
	
	/**
	 * Tell LODICA the system is using a local dataset
	 */
	public static boolean isLocalTest = false;
	
	
	/**
	 * Tell LODICA the system is under evaluation
	 */
	public static boolean isEvaluation = false;	
	
	/**
	 * Give access to the query data in the database
	 */
	static private DBFunctions dbFunctions = null;

	/**
	 * Current user id
	 */
	static public Integer userId = null;
	
	static public Node nodeUnderEvaluation = null;

	static public Set<Node> candidates = null;

	static long init = 0;
	
	static long end = 0;
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		init = System.currentTimeMillis();

/*		IConstants.NEIGHBOURHOOD_DEGREE = 2;
		Set<Node> startingNodes = new HashSet<Node>();
		// Starting nodes with data from the database
		//userId = 1;
		//startingNodes.addAll(getNodesFromDatabase(userId, "http://dbpedia.org/resource/Katy_Perry"));
		userId = 3285;
		startingNodes.addAll(getNodesFromDatabase(userId, "http://dbpedia.org/resource/PS"));
		//startingNodes.add(new Node("0",IConstants.LIKE, "http://dbpedia.org/resource/Manga"));
		ica.runLODICALDSD(startingNodes);*/
		LodicaOldVersion ica = new LodicaOldVersion();
		//ica.checkDistance(6);
		
	}
	


	

	
	
	
	/**
	 * @param startingNodes
	 * @throws Exception 
	 */
	public static void classifyStartingNodes(Set<Node> startingNodes) throws Exception{
		for (Node node : startingNodes) {
			classifyNodeWithObservedAttributesOnly(node, userProfile);
		}
	}
	
	public static void addExternalLabelledNodesOfStartingNodesToGraph() {
		if(spamSet){
			startingSet.addAll(cnns);
			for (Node unlabelledNode : NodeUtil.getUnlabeledNodes(cnns)) {
				loadLODNeighborhoodAndConvertToNodes(IConstants.N, unlabelledNode, null,true);	
			}
		}
	}





	
	
	/**
	 * @param nodes
	 */
	public static void populateDomainLocalPropertySize(List<Node> nodes) {
		for (Node node : nodes) {
			//NodeUtil.print("aquiii "+ node.getUri()+ " aquiii"+node.getObservedAtrributes().size());
			Set<String> datatypeProperty = new HashSet<String>();
			datatypeProperty.addAll(node.getObservedAtrributes().keySet());
			if (!domainDatasetPropertySize.containsKey(node.getDomain())) {
				domainDatasetPropertySize.put(node.getDomain(), datatypeProperty);
			} else {
				Set<String> s1= domainDatasetPropertySize.get(node.getDomain());
				s1.addAll(datatypeProperty);
				domainDatasetPropertySize.replace(node.getDomain(), s1);
			}			
		}
		//this.printDomainPropetySize();
	}	
	
	/**
	 * @param nodes
	 */
	public static void populateDomainOntologyDataTypePropertySize(List<Node> nodes) {
		for (Node node : nodes) {
			if (!domainOntologyDataTypePropertySize.containsKey(node.getDomain())) {
				
				// if a resource has no datatype properties then it get the maximum of properties
				int total = SparqlWalk.getDbpediaOntologyDatatypeProperties(SparqlWalk.getClassesByResource(node.getURI())).size();
				if (total>0) {
					domainOntologyDataTypePropertySize.put(node.getDomain(), SparqlWalk.getDbpediaOntologyDatatypeProperties(SparqlWalk.getClassesByResource(node.getURI())).size());	
				}else{
					domainOntologyDataTypePropertySize.put(node.getDomain(), Integer.MAX_VALUE);
				}
				
			}			
		}
		//this.printDomainDataTypePropetySize();
 }	
	
	public static void loadObservedFeaturesMap(List<Node> nodes) {
			for (Node node : nodes) {
				 node.setDomain(SparqlWalk.getMostSpecificSubclasseOfDbpediaResource(node.getURI()).getURI());
				 List<SimpleTriple> triples = SparqlWalk.getDBpediaFinestLiteralsTripletBySubjectURI(node.getURI());
				 node.observedAtrributes = new HashMap<String,String>();
				 for (SimpleTriple simpleTriple : triples) {
					 node.observedAtrributes.put(simpleTriple.getPredicate(),simpleTriple.getObject());	
				}
			}
			//This needs to be called for normalization in the similarity metric calculation.
			populateDomainLocalPropertySize(nodes);
			populateDomainOntologyDataTypePropertySize(nodes);
	 }
	
	public static void loadObservedFeaturesMap(Node node) {

		 node.setDomain(SparqlWalk.getMostSpecificSubclasseOfDbpediaResource(node.getURI()).getURI());
		 List<SimpleTriple> triples = SparqlWalk.getDBpediaFinestLiteralsTripletBySubjectURI(node.getURI());
		 node.observedAtrributes = new HashMap<String,String>();
		 for (SimpleTriple simpleTriple : triples) {
			 node.observedAtrributes.put(simpleTriple.getPredicate(),simpleTriple.getObject());	
		}
		
		//This needs to be called for normalization in the similarity metric calculation.
         List<Node> nodes = new LinkedList<Node>();
         nodes.add(node);
		populateDomainLocalPropertySize(nodes);
		populateDomainOntologyDataTypePropertySize(nodes);
 }	

	/**
	 * @param nodes
	 */
	public static void normalizeRelationalAttributesMap(List<Node> nodes) {
		
		Set<String> propertyKeys = new HashSet<String>();
		
		Map<String,Double> propertyValueHighest = new LinkedHashMap<String,Double>();
		
		Map<String,Double> propertyValueLowest = new LinkedHashMap<String,Double>();
		
		
		for (Node node : nodes) {
			propertyKeys.addAll(node.getRelationalFeatures().keySet());
		}

		for (String propertyKey : propertyKeys) {
			for (Node node : nodes) {
				if(node.getRelationalFeatures().containsKey(propertyKey)){
					Double valueDouble = node.getRelationalFeatures().get(propertyKey);
					if (!propertyValueHighest.containsKey(propertyKey)&& !propertyValueLowest.containsKey(propertyKey)){
						propertyValueHighest.put(propertyKey, valueDouble);
						propertyValueLowest.put(propertyKey, valueDouble);
					}else{
						if (valueDouble>=propertyValueHighest.get(propertyKey)) {
							propertyValueHighest.put(propertyKey, valueDouble);
						}else if (valueDouble<propertyValueLowest.get(propertyKey)) {
							propertyValueLowest.put(propertyKey, valueDouble);
						}
					}
					
				}
				
			}		
		}
		
		
		//NodeUtil.print(propertyValueHighest);
		//NodeUtil.print(propertyValueLowest);
		
		for (String propertyKey : propertyKeys) {
			for (Node node : nodes) {
				if(node.getRelationalFeatures().containsKey(propertyKey)){
					Double value = node.getRelationalFeatures().get(propertyKey);
						Double valueNormalized = StringUtilsNode.getNormalized01(propertyValueLowest.get(propertyKey),propertyValueHighest.get(propertyKey),Double.valueOf(value));
						node.getRelationalFeatures().replace(propertyKey, valueNormalized);
				}
			}		
		}		
		
		
	}
	
	/**
	 * @param nodes
	 */
	public static void normalizeObservedAttributesMap(List<Node> nodes) {
		
		Set<String> propertyKeys = new HashSet<String>();
		
		Map<String,Double> propertyValueHighest = new LinkedHashMap<String,Double>();
		
		Map<String,Double> propertyValueLowest = new LinkedHashMap<String,Double>();
		
		
		for (Node node : nodes) {
			propertyKeys.addAll(node.getObservedAtrributes().keySet());
		}

		for (String propertyKey : propertyKeys) {
			for (Node node : nodes) {
				if(node.getObservedAtrributes().containsKey(propertyKey)){
					String value = node.getObservedAtrributes().get(propertyKey);
					if (NumberUtils.isNumber(value)) {
						double valueDouble = Double.valueOf(value);
						if (!propertyValueHighest.containsKey(propertyKey)&& !propertyValueLowest.containsKey(propertyKey)){
							propertyValueHighest.put(propertyKey, valueDouble);
							propertyValueLowest.put(propertyKey, valueDouble);
						}else{
							if (valueDouble>=propertyValueHighest.get(propertyKey)) {
								propertyValueHighest.put(propertyKey, valueDouble);
							}else if (valueDouble<propertyValueLowest.get(propertyKey)) {
								propertyValueLowest.put(propertyKey, valueDouble);
							}
						}
					}
				}
				
			}		
		}
		
		//NodeUtil.print(propertyValueHighest);
		//NodeUtil.print(propertyValueLowest);
		
		for (String propertyKey : propertyKeys) {
			for (Node node : nodes) {
				if(node.getObservedAtrributes().containsKey(propertyKey)){
					String value = node.getObservedAtrributes().get(propertyKey);
					if (NumberUtils.isNumber(value)) {
						Double valueNormalized = StringUtilsNode.getNormalized01(propertyValueLowest.get(propertyKey),propertyValueHighest.get(propertyKey),Double.valueOf(value));
						node.getObservedAtrributes().replace(propertyKey, valueNormalized.toString());
					}
				}
			}		
		}
	}	
	
	/**
	 * @param nodes
	 */
	public void nomalizeVectorSize(List<Node> nodes) {
		Set<String> propertyKeys = new HashSet<String>();
		
		for (Node node : nodes) {
			for (String key : node.getObservedAtrributes().keySet()) {
				propertyKeys.add(key);				
			}
		}
		
		for (Node node : nodes) {
			for (String propertyKeyPopulated : propertyKeys) {
				if(!node.getObservedAtrributes().keySet().contains(propertyKeyPopulated)){
					node.getObservedAtrributes().put(propertyKeyPopulated, null);
				}
			}
		}
		
		if (nodes.get(0).getObservedAtrributes().size() !=  propertyKeys.size()) {
			new Exception("VECTOR IS MAL FORMED");
		}

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
			NodeUtil.print("SEMANTIC SIMILARITY:"+node.getURI().replace("http://dbpedia.org/resource/","")+ " X " + nodeTraing.getURI().replace("http://dbpedia.org/resource/","")+ " == "+semanticSimilarity);
			if (semanticSimilarity > IConstants.semanticSimilarityThreshold) {
				selectedNodes.add(nodeTraing);
			}
		}
		
		return selectedNodes;
		
	}
	

	
	
	
	/**
	 * @param nodes
	 * @return
	 */
	private static List<NodePrediction> classifyICAByNeighboursForEvaluation() {
		
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();
		//NodeUtil.print("nodes"+ nodes);
		
		originalUnlabelledNodesToClassify.remove(nodeUnderEvaluation);
		NodeUtil.print(originalUnlabelledNodesToClassify.size()+" nodes to classify");

		for (Node nodeTest : originalUnlabelledNodesToClassify) {
			
			double shortestNeighbourDistance = 1;
			double shortestNeighbourOfNeighbourDistance = 1;
			
			// classify new ones only
			if(LodicaOldVersion.getDatabaseConnection().checkPrediction(nodeUnderEvaluation.getURI(),nodeTest.getURI(), userId, IConstants.USE_ICA)){
				continue;
			}

			NodeUtil.print("CLASSIFYING TEST NODE "+nodeTest.toString());			
			
			Set<Node> labeledNeighbours = NodeUtil.getLabeledNodes(nodeTest.getNodes());
			Node nearestNeighbour = null; 
			for (Node neighbour : labeledNeighbours) {
				Double dist = 1d;	
				if (IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA) {
					dist = Classifier.calculateSemanticDistance(nodeTest, neighbour,IConstants.LDSD_LOD);
					if (dist<=shortestNeighbourDistance) {
						shortestNeighbourDistance = dist;
						nearestNeighbour = neighbour;
					}					
				}else if (IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
					dist = Classifier.calculateSemanticDistance(nodeTest, neighbour,IConstants.LDSD);	
					if (dist<=shortestNeighbourDistance) {
						shortestNeighbourDistance = dist;
						nearestNeighbour = neighbour;
					}					
				}
			}

			if (nearestNeighbour!=null && nearestNeighbour.getNodes()!=null) {
				labeledNeighbours = NodeUtil.getLabeledNodes(nearestNeighbour.getNodes());	
				for (Node neighbour : labeledNeighbours) {
					Double dist = 1d;	
					if (IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA) {
						dist = Classifier.calculateSemanticDistance(nearestNeighbour,neighbour,IConstants.LDSD_LOD);
						if (dist<=shortestNeighbourOfNeighbourDistance) {
							shortestNeighbourOfNeighbourDistance = dist;
	
						}					
					}else if (IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
						dist = Classifier.calculateSemanticDistance(nearestNeighbour,neighbour,IConstants.LDSD);	
						if (dist<=shortestNeighbourOfNeighbourDistance) {
							shortestNeighbourOfNeighbourDistance = dist;
						}					
					}
				}	
			}
			
			NodePrediction prediction = null;
			
			if (shortestNeighbourDistance<=shortestNeighbourOfNeighbourDistance) {
				prediction = new NodePrediction(nodeUnderEvaluation.getURI(), nodeTest, nodeTest.getLabel(),IConstants.LIKE,DBFunctions.checkSimilarityMethod(IConstants.USE_ICA),shortestNeighbourDistance,userId);
			}else{
				prediction = new NodePrediction(nodeUnderEvaluation.getURI(), nodeTest, nodeTest.getLabel(),nodeTest.getLabel(),DBFunctions.checkSimilarityMethod(IConstants.USE_ICA),1d,userId);				
			}
			LodicaOldVersion.getDatabaseConnection().insertPrediction(nodeUnderEvaluation.getURI(),prediction.getNode().getURI(), prediction.getEvaluationLabel(), prediction.getPredictedLabel(), IConstants.USE_ICA,prediction.getPredictionScore(), userId);
			memoryPredictions.add(prediction);
		}
		
		NodeUtil.print("Classification has ended");
		
		return memoryPredictions;
		
	}	
	
		
	
	/**
	 * @param nodes
	 * @return
	 */
	private static List<NodePrediction> classifyICAByNeighbours() {
		
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();
		//NodeUtil.print("nodes"+ nodes);
		for (Node nodeTest : originalUnlabelledNodesToClassify) {
			Set<Node> unlabelledNodesForTesting = new HashSet<Node>();
			unlabelledNodesForTesting.add(nodeTest);
			
			Set<Node> labeledNodesForTraining = NodeUtil.getLabeledNodes(nodeTest.getNodes());
			if (IConstants.USE_ICA) {
				contructRelationalUnobservedAttributesOnLocalGraph(nodeTest,new ArrayList<Node>(labeledNodesForTraining));
			}
			labeledNodesForTraining.addAll(labeledNodesForTraining);

			//NodeUtil.print("CLASSIFYING TEST NODE "+unlabelledNodesForTesting.toString() +" using AS TRAINING NODES "+labeledNodesForTraining.toString());NodeUtil.print();
			List<NodePrediction> nodePredictions = Classifier.classifyOriginal(new ArrayList<Node>(unlabelledNodesForTesting), new ArrayList<Node>(labeledNodesForTraining));
			nodePredictions.get(0).setNode(nodeTest);
			memoryPredictions.add(nodePredictions.get(0));
		}
		
		NodeUtil.print("Classification has ended");

		//Labels updated after classification STEP2a
		updateLabelsAfterClassification(memoryPredictions);
		
		return memoryPredictions;
		
	}	


	/**
	 * @param nodes
	 * @return
	 */
	private static List<NodePrediction> classifyICAOriginalAllByAll() {
		
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();
		//NodeUtil.print("nodes"+ nodes);

		for (Node nodeTest : originalUnlabelledNodesToClassify) {
			
			Set<Node> unlabelledNodesForTesting = new HashSet<Node>();
			unlabelledNodesForTesting.add(nodeTest);
			
			Set<Node> labeledNodesForTraining = new HashSet<Node>();
			labeledNodesForTraining = new HashSet<Node>(originalLabelledNodes);
			
			
			if (useExternalClassifiedNodesForTrainingSet && IConstants.USE_ICA) {
				labeledNodesForTraining.addAll(externalClassifiedNodesForTrainingSet);
			}
			
			if (useNeighboursOutOfSetOfCandidateSet && !IConstants.USE_ICA) {

				if (isLocalTest && !neighboursOutOfCandidateSet.isEmpty()) {
					List<Node> externalLinkedNodes = new ArrayList<Node>();
					externalLinkedNodes.addAll(NodeUtil.getLabeledNodes(new ArrayList<Node>(neighboursOutOfCandidateSet)));
					labeledNodesForTraining.addAll(externalLinkedNodes);
				}else{
					if (neighboursPlus!=null && neighboursPlus.containsKey(nodeTest)) {
						contructRelationalUnobservedAttributesOnLocalGraph(nodeTest,new ArrayList<Node>(neighboursPlus.get(nodeTest)));
						labeledNodesForTraining.addAll(neighboursPlus.get(nodeTest));	
					}
					
				}				
			}
			if (useNeighboursOutOfSetOfCandidateSet && IConstants.USE_ICA) {
				if (isLocalTest) {
					List<Node> externalLinkedNodes = new ArrayList<Node>();
					externalLinkedNodes.addAll(NodeUtil.getLabeledNodes(new ArrayList<Node>(neighboursOutOfCandidateSet)));
					contructRelationalUnobservedAttributesOnLocalGraph(nodeTest,externalLinkedNodes);
					labeledNodesForTraining.addAll(externalLinkedNodes);
				}else{
					if (neighboursPlus!=null && neighboursPlus.containsKey(nodeTest)) {
						contructRelationalUnobservedAttributesOnLocalGraph(nodeTest,new ArrayList<Node>(neighboursPlus.get(nodeTest)));
						labeledNodesForTraining.addAll(neighboursPlus.get(nodeTest));	
					}
				}
			}			

			//NodeUtil.print("CLASSIFYING TEST NODE "+unlabelledNodesForTesting.toString() +" using AS TRAINING NODES "+labeledNodesForTraining.toString());NodeUtil.print();
			List<NodePrediction> nodePredictions = Classifier.classifyOriginal(new ArrayList<Node>(unlabelledNodesForTesting), new ArrayList<Node>(labeledNodesForTraining));
			nodePredictions.get(0).setNode(nodeTest);
			memoryPredictions.add(nodePredictions.get(0));
		}
		
		NodeUtil.print("Classification has ended");

		//Labels updated after classification STEP2a
		updateLabelsAfterClassification(memoryPredictions);
		
		return memoryPredictions;
		
	}
	
	/**
	 * @param memoryPredictions
	 */
	private static void updateLabelsAfterClassification(List<NodePrediction> memoryPredictions) {
		NodeUtil.print();
		NodeUtil.print("AFTER CLASSIFICATION: \n");
		Collections.sort(memoryPredictions);
		
		for (NodePrediction nodePrediction : memoryPredictions) {
			nodePrediction.getNode().setLabel(nodePrediction.getPredictedLabel());
			if (!nodePrediction.getEvaluationLabel().equals(nodePrediction.getPredictedLabel())) {
				NodeUtil.print("NODE:"+nodePrediction.getNode().getId()+":"+new ResourceImpl(nodePrediction.getNode().getURI()).getLocalName()+"\tSCORE:"+nodePrediction.getPredictionScore()+"\tPREVIOUS LABEL:"+nodePrediction.getEvaluationLabel() +"\tPREDICTED_LABEL:"+nodePrediction.getPredictedLabel());
				NodeUtil.print();
			}else{
				NodeUtil.print("NODE:"+nodePrediction.getNode().getId()+":"+new ResourceImpl(nodePrediction.getNode().getURI()).getLocalName()+"\tSCORE:"+nodePrediction.getPredictionScore()+"\tPREDICTED SAME LABEL:"+nodePrediction.getEvaluationLabel());
				NodeUtil.print();
			}
		}		
	}
	
	/**
	 * @param memoryPredictions
	 */
	private static void updateLabelsAfterClassification(NodePrediction memoryPrediction) {
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();
		memoryPredictions.add(memoryPrediction);
		updateLabelsAfterClassification(memoryPredictions);		
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

	public static Node getNodeByURI(String uri) {
		if (cnns==null) {
			return null;
		}		
		for (Node node : cnns) {
			if (node.getURI().equals(uri)) {
				return node;
			}
		}
		return null;
	}
	
	public static Node getNodeByURI(String uri, List<Node> nodes) {
		if (nodes==null) {
			return null;
		}		
		for (Node node : nodes) {
			if (node.getURI().equals(uri)) {
				return node;
			}
		}
		return null;
	}	
	
	public static Node getNodeByURI(String uri,Set<Node> nodes) {
	
		for (Node node : nodes) {
			if (node.getURI().equals(uri)) {
				return node;
			}
		}
		return null;
	}	

	public static Node getNodeByID(String id) {
		for (Node node : cnns) {
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
	 * TODO refactor this code or use the newest one
	 * @param degree
	 * @param nodeOriginal
	 * @param aux
	 * @param checkLabel
	 * @return
	 */
	public static List<Node> loadLODNeighborhoodAndConvertToNodes(int degree, Node nodeOriginal, List<Node> aux, boolean checkLabel) {
		List<Node> n4 = null;
		
		if (nodeOriginal.getNodes() == null || nodeOriginal.getNodes().isEmpty()  ||  (nodeOriginal.getNodes().size() < SparqlWalk.getCountDBpediaObjecstBySubject(nodeOriginal.getURI()))) {
			int max = Integer.valueOf(NodeUtil.getMaxNodeID(cnns));
			//NodeUtil.print(max);
			//nodeOriginal.setNodes(convertResourceInNodes(SparqlWalk.getDBpediaObjecstBySubject(nodeOriginal.getUri()),max,checkLabel));
			Set<Node> newNodes = new HashSet<Node>(convertResourceInNodes(SparqlWalk.getDBpediaObjecstBySubject(nodeOriginal.getURI()),max,checkLabel));
			for (Node newNode : newNodes) {
				if (!nodeOriginal.getNodes().contains(newNode)) {
					NodeUtil.connect2Nodes(newNode, nodeOriginal);	
				}
				
				//nodeOriginal.addNode(newNode);
			}
			//NodeUtil.print(nodeOriginal.getNodes());
			Collections.sort(nodeOriginal.getNodes());
			n4 = new LinkedList<Node>(nodeOriginal.getNodes());
		}else{
			n4 = new LinkedList<Node>(aux);
		}
		
		if (degree<=1) {
			
			return cnns;
		}
		
		aux = new ArrayList<Node>();
		
		for (Node nodeLocal : n4) {
			List<Node> n3 = new LinkedList<Node>();
			int max = Integer.valueOf(NodeUtil.getMaxNodeID(cnns));
			//NodeUtil.print(allnodes);
			//NodeUtil.print(max);
			
			n3.addAll(convertResourceInNodes(SparqlWalk.getDBpediaObjecstBySubject(nodeLocal.getURI()),Integer.valueOf(max).intValue(),checkLabel));
			nodeLocal.setNodes(n3);
			nodeLocal.getNodes().remove(nodeLocal);
			Collections.sort(nodeLocal.getNodes());
			aux.addAll(n3);
			
		}
		degree = degree-1;
		//Node a = nodeOriginal;
		loadLODNeighborhoodAndConvertToNodes(degree,nodeOriginal,aux,checkLabel);	
		
		return cnns;
	}	

	
	/**
	 * 
	 * TODO Refactor this poor code 
	 * @param degree
	 * @param nodeOriginal
	 * @param nodesAux
	 * @return
	 */
	public static List<Node> getNeighborhood(int degree, Node nodeOriginal, List<Node> nodesAux) {
		List<Node> n2 = new ArrayList<Node>();
		List<Node> n3 = new ArrayList<Node>();
		List<Node> n4 = null;
		if (nodesAux == null) {
			nodeOriginal.neighbours.addAll(nodeOriginal.getNodes());
			n2 = nodeOriginal.getNodes();
			n4 = new ArrayList<Node>(n2);
		}else{
			n4 = new ArrayList<Node>(nodesAux);
		}
		
		
		if (degree<=1) {
			
			return nodeOriginal.neighbours;
		}
		for (Node nodeLocal : n4) {
			//printNode(nodeLocal);
			nodeOriginal.neighbours.addAll(nodeLocal.getNodes());
			//NodeUtil.print(nodeOriginal.neighbours);
			nodeOriginal.neighbours = new ArrayList<Node>(new HashSet<Node>(nodeOriginal.neighbours));
			//NodeUtil.print(nodeOriginal.neighbours);
			n3.addAll(nodeLocal.getNodes());
		}
		Node a = nodeOriginal;
	
	
		getNeighborhood(degree-1,a,n3);
		nodeOriginal.neighbours.remove(nodeOriginal);
		return nodeOriginal.neighbours;
	}
	
	/**
	 * @param nodes
	 * @param neighbourhood
	 */
	public static void contructRelationalUnobservedAttributesBasedOnNeighbourhood(List<Node> nodes) {

		List<Node> localNnodes = nodes;
		List<Node> internalNodes = nodes;

		for (Node node : localNnodes) {
			//printNode(node);
			if (node.relationalFeatures==null) {
				node.relationalFeatures = new HashMap<String, Double>();
			}
			internalNodes = getNeighborhood(IConstants.N, node, null);
			for (Node internalNode : internalNodes) {
				if (!IConstants.getLabels().contains(internalNode.getLabel())) {
					continue;
				}

				if (NodeUtil.isDistinctURI(node, internalNode)) {
					if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+internalNode.getLabel())) {
						double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+internalNode.getLabel());
						node.relationalFeatures.replace(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+internalNode.getLabel(),oldValue, ++oldValue);
					} else {
						node.relationalFeatures.put(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+internalNode.getLabel(), 1d);
					}	
				}
			}
			// normalizing the vector size
			for (String label : IConstants.getLabels()) {
				if (!node.relationalFeatures.containsKey(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+label)) {
					node.relationalFeatures.put(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+label, 0d);
				}
			}

			node.relationalFeatures = new TreeMap<String, Double>(node.relationalFeatures);
			
		}
	}
	

	/**
	 * @param nodes
	 */
	public void contructRelationalAttributesBasedOnIndirectIncomingAndOutComingLinksOnLinkedData(List<Node> nodes) {
		List<Node> neighbourhood = new ArrayList<Node>();
		List<Node> localNnodes = nodes;
		for (Node node : localNnodes) {
			neighbourhood = getNeighborhood(IConstants.N, node, null);
			
			//NodeUtil.print(neighbourhood);
			
			node.relationalFeatures = new HashMap<String,Double>();
			for (Node internalNode : neighbourhood) {
				node.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING, Double.valueOf(SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween2Resources(node.getURI(),internalNode.getURI())));
				node.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING, Double.valueOf(SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween2Resources(node.getURI(),internalNode.getURI())));
			}
		}
	}	
	

	/**
	 * @param nodes
	 */
	public static void contructRelationalAttributesNeibouhood(List<Node> nodes) {
		List<Node> localNnodes = nodes;
		for (Node node1 : localNnodes) {
			node1.relationalFeatures = new HashMap<String,Double>();
			node1.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_DIRECT, Double.valueOf(node1.getNodes().size()));
			node1.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING, 0d);
			node1.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING, 0d);
			//printNode(node1);
			for (Node node2 : node1.getNodes()) {
				//printNode(node2);
				double countIndirectIncoming = 0d;
				double countIndirectOutgoing = 0d;
				for (Node node3 : node2.getNodes()) {
					if ( (NodeUtil.isDistinctURI(node1,node3)) && (NodeUtil.isDistinctURI(node1,node2)) && (NodeUtil.isDistinctURI(node2,node3))) {
						//printNode(node3);						
						countIndirectIncoming = countIndirectIncoming+Double.valueOf(SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween3Resources(node1.getURI(),node2.getURI(),node3.getURI()));
						node1.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING, countIndirectIncoming);
						countIndirectOutgoing = countIndirectOutgoing+Double.valueOf(SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween3Resources(node1.getURI(),node2.getURI(),node3.getURI()));
						node1.relationalFeatures.put(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING, countIndirectOutgoing);					
					}
				}				
			}
		}
	}
	
	
	

	
	/**
	 * @param node
	 * @param externalLinkedNodes
	 */
	public static void contructRelationalAttributesOnLOD(Node node, List<Node> externalLinkedNodes) {
			node.relationalFeatures = new HashMap<String,Double>();
			double totalDirectExternalNode = 0;
			double totalIndirectIncoming = 0;
			double totalIndirectOutgoing = 0;
			for (Node nodeExternalNode : externalLinkedNodes) {
				if (NodeUtil.isDistinctURI(node, nodeExternalNode)) {
					totalDirectExternalNode = totalDirectExternalNode + SparqlWalk.getCountDBpediaObjecstBySubjectBetween2Resources(node.getURI(), nodeExternalNode.getURI());
					totalIndirectIncoming = totalIndirectIncoming + SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween2Resources(node.getURI(), nodeExternalNode.getURI());
					totalIndirectOutgoing = totalIndirectOutgoing + SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween2Resources(node.getURI(), nodeExternalNode.getURI());
				}
			}
			
			if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_DIRECT)) {
				double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_DIRECT);
				node.relationalFeatures.replace(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_DIRECT,oldValue,(oldValue+totalDirectExternalNode));
			}
			
			if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING)) {
				double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING);
				node.relationalFeatures.replace(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_INCOMING,oldValue,(oldValue+totalIndirectIncoming));
			}
			
			if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING)) {
				double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING);
				node.relationalFeatures.replace(IConstants.RELATIONAL_OBSERVED_ATTRIBUTE_INDIRECT_OUTGOING,oldValue,(oldValue+totalIndirectOutgoing));
			}			
	}	

	/**
	 * @param node
	 * @param externalLinkedNodes
	 */
	public static void contructRelationalUnobservedAttributesOnLocalGraph(Node node, List<Node> externalLinkedNodes) {
		for (Node nodeExternalNode : externalLinkedNodes) {
			if (NodeUtil.isDistinctURI(node, nodeExternalNode)) {
				
				if (node.relationalFeatures==null){
					node.relationalFeatures = new HashMap<String, Double>();
				}
				
				if(node.getNodes().contains(nodeExternalNode)){
					if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel())) {
						double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel());
						node.relationalFeatures.replace(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel(),oldValue,oldValue++);
					}
				}
			}
		}
	}	
	
	/**
	 * @param node
	 * @param externalLinkedNodes
	 */
	public static void contructRelationalUnobsservedAttributesOnLOD(Node node, List<Node> externalLinkedNodes) {
		for (Node nodeExternalNode : externalLinkedNodes) {
			if (NodeUtil.isDistinctURI(node, nodeExternalNode)) {
				
				if (node.relationalFeatures==null){
					node.relationalFeatures = new HashMap<String, Double>();
				}
				NodeUtil.print(node.relationalFeatures.toString());
				if (SparqlWalk.getCountDBpediaObjecstBySubjectBetween2Resources(node.getURI(), nodeExternalNode.getURI())>0){
					if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel())) {
						double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel());
						node.relationalFeatures.replace(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel(),oldValue,oldValue++);
					}
				} else if (SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween2Resources(node.getURI(), nodeExternalNode.getURI())>0){
						if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel())) {
							double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel());
							node.relationalFeatures.replace(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel(),oldValue,oldValue++);
						}
				} else if(SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween2Resources(node.getURI(), nodeExternalNode.getURI())>0){
					if (node.relationalFeatures.containsKey(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel())) {
						double oldValue = (double)node.relationalFeatures.get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel());
						node.relationalFeatures.replace(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+nodeExternalNode.getLabel(),oldValue,oldValue++);
					}
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

	public void printDomainPropetySize() {
		for (String key : domainDatasetPropertySize.keySet()) {
			NodeUtil.print("Domain "+key+ " has "+ domainDatasetPropertySize.get(key).size() +" data type properties: "+domainDatasetPropertySize.get(key));
		}
		NodeUtil.print("");
	}
	
	public void printDomainDataTypePropetySize() {
		for (String key : domainOntologyDataTypePropertySize.keySet()) {
			NodeUtil.print("Domain "+key+ " has "+ domainOntologyDataTypePropertySize.get(key) +" data type properties ");
		}
		NodeUtil.print("");
	}
	
	public static  List<Node> convertResourceInNodes(List<Resource> resources, int idCount, boolean checkLabel) {

		List<Node> nodes = new LinkedList<Node>();
		
		int count = idCount+1;
		
		if (cnns==null) {
			cnns = new LinkedList<Node>();
		}
		for (Resource resource : resources) {
			//NodeUtil.print("resource.getURI())"+resource.getURI());
			Node node = null;
			Node existing = getNodeByURI(resource.getURI());
			if (existing==null && !checkLabel) {
				node = new Node(""+count,IConstants.NO_LABEL,resource.getURI());
				cnns.add(node);
				nodes.add(node);
			}else if (checkLabel &&  existing!=null && IConstants.getValidLabel(existing.getLabel())!=null) {
				node = existing;
				nodes.add(node);
			}else if (!checkLabel) {
				node = existing;
				nodes.add(node);
			}
			count++;
		}
		return nodes;
	}	


	
	

	/**
	 * @param node
	 * @return
	 */
	public static List<String> getListByAttribute(String uriProperty) {

		List<String> listByAttribute = new ArrayList<String>();

		for (Node localNode : cnns) {
			//NodeUtil.print("ss"+localNode.getObservedAtrributes().get(uriProperty));
			listByAttribute.add(localNode.getObservedAtrributes().get(uriProperty));
		}

		return listByAttribute;
	}
	
	
	public void calculateNodeSimilarity(List<Node> allnodes){
		NodeSim.simVSMbyProperty(allnodes);
	}

	
	/**
	 * @param node
	 * @param trainingSet
	 * @throws Exception 
	 */
	public static void classifyNodeWithObservedAttributesOnly(Node node, Set<Node> trainingSet) throws Exception {
		List<Node> allnodes = new ArrayList<Node>();
		allnodes.add(node);
		allnodes.addAll(new ArrayList<Node>(trainingSet));
		Collections.sort(allnodes);
		NodeUtil.checkForDistinctIdsAndURIs(allnodes);
		originalUnlabelledNodesToClassify = NodeUtil.getUnlabeledNodes(allnodes);
		originalLabelledNodes = NodeUtil.getLabeledNodes(allnodes);
		NodeUtil.checkTrainingTestSet(originalLabelledNodes,originalUnlabelledNodesToClassify,cnns);
		loadObservedFeaturesMap(allnodes);
		NodeUtil.printObservedAttributesForAllNodes(allnodes);
		normalizeObservedAttributesMap(allnodes);
		//printObservedAttributesForAllNodes(allnodes);

		NodeUtil.print("------------------------------------CLASSIFYING STARTING NODES WITH LOCAL CLASSIFIER----------------------------------------- \n");
		//bootstrapByRandomLabelling(allnodes);
		NodeUtil.bootstrapNullLabelsNodes(allnodes,IConstants.NO_LABEL);
		NodeUtil.print();
		NodeUtil.print("------------------------------------CLASSIFICATION WITH ONLY OBSERVED ATTRIBUTES----------------------------------------------\n");

		NodePrediction nodePrediction = classifyICAOriginalAllByAll().get(0);
		node.setLabel(nodePrediction.getPredictedLabel());
		originalUnlabelledNodesToClassify = new HashSet<Node>();
		originalLabelledNodes = new HashSet<Node>();
	}
	

	/**
	 * Old method with the first version
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public static  List<NodePrediction> runLODICAFistVersionWithObservedAttributes(String uri) throws Exception {
		
		NodeUtil.print("------------------------------------LODICA ON ROUND ("+(++round)+")----------------------------------------- \n");		
		
		List<NodePrediction> nodePrediction = null;

		Node node = getNodeByURI(uri);
		if (node==null) {
			node = new Node(NodeUtil.getMaxNodeID(cnns),IConstants.LIKE,uri);
		}
		
		
		Set<Node> startingNodes = new HashSet<Node>();
		startingNodes.add(node);
		classifyStartingNodes(NodeUtil.getUnlabeledNodes(new ArrayList<Node>(startingNodes)));
		
		
		cnns = new LinkedList<Node>();
		cnns.add(node);
		externalClassifiedNodesForTrainingSet.add(node);
		
		if (useExternalClassifiedNodesForTrainingSet) {
			cnns.addAll(externalClassifiedNodesForTrainingSet);
			cnns = new LinkedList<Node>(new HashSet<Node>(cnns));
		}
		
		if(!isLocalTest){
			loadLODNeighborhoodAndConvertToNodes(IConstants.N, node, null,false);	
		}
		
		//this adds external labeled nodes to the starting nodes 
		addExternalLabelledNodesOfStartingNodesToGraph();
		
		
		Collections.sort(cnns);
		//printNodes(allnodes);
		//labelXNodesRandomly(3);
		
		NodeUtil.checkForDistinctIdsAndURIs(cnns);

		NodeUtil.showGraph(false,cnns);
		
		
		//Load observed features
		loadObservedFeaturesMap(cnns);
		NodeUtil.printObservedAttributesForAllNodes(cnns);
		normalizeObservedAttributesMap(cnns);
		//printObservedAttributesForAllNodes(allnodes);
		
		
		//index before
		//List<String> docs = getListByAttribute("abstract");
		//docs.addAll(getListByAttribute("comment"));
		//LuceneCosineSimilarity.index(docs);
				
		
		NodeUtil.print("--------------------PRE-PROCESSING FINISHED---CLASSIFYCATION STARTS NOW------------------- \n");
		
		originalUnlabelledNodesToClassify = NodeUtil.getUnlabeledNodes(cnns);
		originalLabelledNodes = NodeUtil.getLabeledNodes(cnns);
		NodeUtil.checkTrainingTestSet(originalLabelledNodes,originalUnlabelledNodesToClassify,cnns);	
		

		NodeUtil.print("------------------------------------BOOTSTRAPPING----------------------------------------- \n");
		//bootstrapByRandomLabelling(allnodes);
		NodeUtil.bootstrapNullLabelsNodes(cnns,IConstants.NO_LABEL);
		//printNodes(allnodes);
		NodeUtil.print();

		NodeUtil.print("------------Step 1: INITIAL CLASSIFICATION WITH ONLY OBSERVED ATTRIBUTES------------------ \n");
		//Classify using only observed attributes
		IConstants.USE_SEMANTIC_DISTANCE = false;
		nodePrediction = classifyICAOriginalAllByAll();
		// this needs to change WHEN WE define the sense of neighborhood
		IConstants.USE_ICA = true;
		IConstants.USE_SEMANTIC_DISTANCE = false;
		
		NodeUtil.print("");
		
		NodeUtil.print("------------Step 2: ITERATIVE CLASSIFICATION USING ALL ATTRIBUTES-------------------------- \n");
		NodeUtil.print();
		int numberOfIteraction = 3;
		//for (int i = 0; ((i < numberOfIteraction) && !isStabel(i)) ; i++) {
		for (int i = 0; ((i < numberOfIteraction) && IConstants.USE_ICA) ; i++) {
			NodeUtil.print("ITERATION "+(i+1)+"/"+numberOfIteraction+"--------------------------------------------- \n");
			if (IConstants.USE_ICA){
				contructRelationalAttributesNeibouhood(cnns);
				//contructRelationalAttributesBasedOnIndirectIncomingAndOutComingLinksOnLinkedData(allnodes);
				contructRelationalUnobservedAttributesBasedOnNeighbourhood(cnns);
				//printRelationalAndObservedAttributesForAllNodes(allnodes);
				NodeUtil.printRelationalAttributesForAllNodes(cnns);
				normalizeRelationalAttributesMap(cnns);
				//NodeUtil.print(" agora vai ");
				//printRelationalAttributesForAllNodes(allnodes);
			}
			nodePrediction = classifyICAOriginalAllByAll();	
		}
		
		
		return nodePrediction;
		
	}
	
	/**
	 * @param nodeTests
	 * @param trainingNodes
	 */
	private void contructRelationalAttributesBasedOnPaths(List<Node> nodeTests, List<Node> trainingNodes) {

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

	public static DBFunctions getDatabaseConnection(){
		if(dbFunctions==null){
			dbFunctions = new DBFunctions();
		}
		
		return dbFunctions;
	}
	
	
}