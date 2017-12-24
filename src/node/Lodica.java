package node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import database.DBFunctions;
import util.StringUtilsNode;

public class Lodica implements Serializable {
	
	/**
	 * Map for the N+LIKE out of training set.
	 */
	static public Map<Node,Set<Node>> neighboursPlus = null;
	
	/**
	 * Neighbors in N+LIKE
	 */
	public static Set<Node> trainingSet = null;	
	
	/**
	 *  Enable neighbors out of training set are considered during the classification 
	 */
	public static boolean useNPLUS = true;	
	
	/**
	 * Includes the candidates and N-LIKE
	 */
	public static List<Node> cnns = null;

	/**
	 * Set of liked resources that might be considered for training set
	 */
	public static Set<Node> userProfile = null;

	/**
	 * Set of labeled nodes used for training set.
	 */
	public static Set<Node> originalLabelledNodes = null;
	
	/**
	 * Set of unlabeled nodes used for test set. This is actually the candidate set.
	 */
	public static Set<Node> originalUnlabeledNodesToClassify = null;
	
	/**
	 * round of the classification when navigating from web interface
	 */
	public static byte round = 0;	

	private static final long serialVersionUID = 1L;
	
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
	
	/**
	 * Node under evaluation
	 */
	static public Node nodeUnderEvaluation = null;

	/**
	 * The set of candidates
	 */
	static public Set<Node> candidates = null;

	/**
	 * init time
	 */
	static long init = 0;
	
	/**
	 * end time
	 */
	static long end = 0;
	
	
	/**
	 * 
	 * @return
	 */
	public static DBFunctions getDatabaseConnection(){
		if(dbFunctions==null){
			dbFunctions = new DBFunctions();
		}
		return dbFunctions;
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Lodica ica = null;
		for (int retries = 0;; retries++) {
			try {
				init = System.currentTimeMillis();
				ica = new Lodica();
				ica.evaluate();
			} catch (Exception e) {
				if (retries < 10000) {
					NodeUtil.print("restarting...#"+retries);
					continue;
				} else {
					throw e;
				}
			}
		}
	}
	
	/**
	 * @throws Exception
	 */
	private void evaluate() throws Exception {
		
		
		// A few settings
		Lodica ica = new Lodica();

		IConstants.USE_SEMANTIC_DISTANCE = true;
		
		IConstants.USE_ICA = false;
		
		IConstants.N = 2;

		useNPLUS = true;
		
		isEvaluation = true;

		// loading userIds from DB
		Set<Integer> userIds = Lodica.getDatabaseConnection().getUserIdsForLikedMoviesMusicsBooks();
		
		for (Integer userId : userIds) {
			
			Lodica.userId = userId;

			userProfile = getDatabaseConnection().getUnionLikedMoviesMusicsBooksByUserIdAndConvertToNode(Lodica.userId);			
			
			NodeUtil.print("User "+userId+" has "+userProfile.size()+" items to be evaluated");
			
			for (Node node : userProfile) {

				nodeUnderEvaluation = node; candidates = new HashSet<Node>(); candidates.add(node);

				//Classification start point. 
				ica.runLODICALDSD(candidates);

				//Classify only nodeUnderEvaluation with NO_LABEL
				nodeUnderEvaluation.setLabel(IConstants.NO_LABEL);
				ica.classify();

				int correctPrediction = 0; int incorrectPrediction = 0;

				NodePrediction predictedLeftOut = getDatabaseConnection().getPredictionEvaluation(nodeUnderEvaluation.getURI(),userId);
				
				if (!NodeUtil.isDistinctURI(predictedLeftOut.getNode(), node)) {
					if (predictedLeftOut.getPredictedLabel().equals(IConstants.LIKE)) {
						correctPrediction++;
					}else{
						incorrectPrediction++;
					}
					getDatabaseConnection().insertOrUpdateEvaluation(predictedLeftOut.getNode().getURI(), correctPrediction, incorrectPrediction, IConstants.USE_ICA, predictedLeftOut.getUserId());
				}else{
					NodeUtil.print("Something went wrong...");	
				}
				
				Evaluation evaluation = getDatabaseConnection().getEvaluation(predictedLeftOut.getNode().getURI(), IConstants.USE_ICA, predictedLeftOut.getUserId());
				NodeUtil.pritEvaluation(evaluation);
				//NodeUtil.print("leaving  bye bye");
				//System.exit(0);
			}
		}
	}
	
	/**
	 *  LOD + ICA + LDSD starts from here
	 * @param candidates
	 * @return
	 * @throws Exception
	 */
	public List<NodePrediction> runLODICALDSD(Set<Node> candidates) throws Exception {
		
		NodeUtil.print("------------------------------------LODICA ON ROUND ("+(++round)+")----------------------------------------- \n");
		
		NodeUtil.print("Candidate under evaluation "+nodeUnderEvaluation.getURI()+" for user "+userId);
		
		NodeUtil.print("\n------------------------------------LOADING CNNs--------------------- ------------------------------------- \n");
		
		//Building the CNNs. Including the candidate plus N-LIKE from the candidate set
		if(!isLocalTest){
			cnns = new LinkedList<Node>();
			cnns.addAll(candidates);
			loadNeighbourhoodFromLOD(candidates,false);
		}
		
		NodeUtil.checkForDistinctIdsAndURIs(cnns);
		NodeUtil.print("CNNs size "+cnns.size());
		NodeUtil.print("CNNs - Time elapse: "+(StringUtilsNode.getDuration(System.currentTimeMillis() - init)));
		NodeUtil.showGraph(false,cnns);
				
		//fred
		// Building N+LIKE set from each member of CNNs. We build the N+LIKE set separately due to performance issues
		if(useNPLUS){
			neighboursPlus = new HashMap<Node,Set<Node>>();
			trainingSet = new HashSet<Node>();
			NodeUtil.print("\n----------------------------------LOADING NEIGHBOURS+ PLUS-----------------------------------------------\n");
			addDirectLabeledNodesFromUserProfileOptimized(new HashSet<Node>(NodeUtil.getLabeledNodes(cnns)),new HashSet<Node>(userProfile));
			//NodeUtil.print("N+LIKE : "+neighboursPlus.toString());
			//NodeUtil.print("N+LIKE   direct key size : "+neighboursPlus.keySet().size());
			addIndirectLabeledNodesFromUserProfileOptimized(neighboursPlus);
			//NodeUtil.print("N+LIKE indirect key size : "+neighboursPlus.keySet().size());
			//NodeUtil.print("N+LIKE : - Time elapse: "+(System.currentTimeMillis() - init));
			
			NodeUtil.print("N+LIKE size: "+trainingSet.size());
			NodeUtil.print("N+LIKE : - Time elapse: "+(StringUtilsNode.getDuration(System.currentTimeMillis() - init)));
			NodeUtil.print("Labelled Nodes: "+NodeUtil.getLabeledNodes(cnns));
		}	

		NodeUtil.showGraph(false,cnns);
		NodeUtil.bootstrapNullLabelsNodes(cnns, IConstants.NO_LABEL);
				
        //fred
		NodeUtil.print("\n------------Step 1: INITIAL CLASSIFICATION USING ONLY LOCAL CLASSIFIER------------------------------------\n");
		originalUnlabeledNodesToClassify = NodeUtil.getUnlabeledNodes(cnns);
		List<NodePrediction> memoryPredictions = classify();
		//NodeUtil.printPredictions(memoryPredictions);
		NodeUtil.updateLabelsAfterClassification(memoryPredictions);
		NodeUtil.print("After classification: - Time elapse: "+(StringUtilsNode.getDuration(System.currentTimeMillis() - init)));

		if (IConstants.USE_ICA) {
			NodeUtil.print("------------Step 2: ITERATIVE CLASSIFICATION USING ICA---------------------------------------------------\n");
			for (int i = 0; ((i < IConstants.AMOUNT_OF_ITERATIONS_TO_STABALIZE) && IConstants.USE_ICA) ; i++) {
				NodeUtil.print("ITERATION "+(i+1)+"/"+IConstants.AMOUNT_OF_ITERATIONS_TO_STABALIZE+"---------------------------------\n");
				memoryPredictions = classify();
				NodeUtil.updateLabelsAfterClassification(memoryPredictions);
			}			
		}
		NodeUtil.print("\n-----------------------------"+StringUtilsNode.greeting()+"----------------------------------------------- \n");
		return memoryPredictions;
		
	}
	
	/**
	 * Creates adds incoming and outgoing links to create the Neighborhood from linked open data and convert to nodes
	 * @param nodeOriginal
	 * @param userId
	 * @param filterDomain
	 * @param iteration
	 * @return
	 */
	private Set<Node> addNeighborhoodFromLODAndConvertToNodes(Node nodeOriginal, int userId, boolean filterDomain, int iteration) {
        //fred
		List<Resource> resources = new ArrayList<Resource>();
		resources = getDatabaseConnection().getLinksIncomingAndOutcoming(nodeOriginal.getURI());

		if (resources.isEmpty()) {
			if (filterDomain) {
				resources = SparqlWalk.getDBpediaObjecstBySubjectByDomain(nodeOriginal.getURI());	
			}else{
				resources = SparqlWalk.getDBpediaObjecstBySubject(nodeOriginal.getURI());
				resources.addAll(SparqlWalk.getDBpediaSubjectsByObjects(nodeOriginal.getURI()));
			}
			
			getDatabaseConnection().insertLinks(nodeOriginal.getURI(),resources);
			NodeUtil.print("Link "+nodeOriginal.getURI()+" inserted "+resources.size()+ " links");
		}

		//NodeUtil.print(resources.size()+" resources retrieved from from LOD");		
		//getDatabaseConnection().getStatisticsDomain(nodeOriginal, iteration, resources);
		
		resources = new ArrayList<Resource>(new HashSet<Resource>(resources));
		//NodeUtil.print(resources.size()+" after cleaning");
		
		Set<Node> nodesToGraph = new HashSet<Node>(NodeUtil.convertResourceInNodes(resources, Integer.valueOf(NodeUtil.getMaxNodeID(cnns)), false,cnns));
		
		//Resources in the user profile should be part of CNNs
		for (Node node : userProfile) {
			for (Node resource : nodesToGraph) {
				if (node.getURI().trim().equals(resource.getURI().trim())) {
					resource.setLabel(IConstants.LIKE);
				}
			}
		}		

		for (Node newNode : nodesToGraph) {
			NodeUtil.connect2Nodes(newNode, nodeOriginal);	
		}

		return new HashSet<Node>(nodeOriginal.getNodes());
	}
	
	
	/**
	 * Creates adds incoming and outgoing links to create the Neighborhood from linked open data and convert to nodes
	 * @param nodeOriginal
	 * @param userId
	 * @param filterDomain
	 * @param iteration
	 * @return
	 */
	private Set<Node> addNeighborhoodTUFromLODAndConvertToNodes(Node nodeOriginal, int userId, boolean filterDomain, int iteration) {
        //fred
		List<Resource> resources = new ArrayList<Resource>();

		resources = getDatabaseConnection().getLinksIncomingAndOutcoming(nodeOriginal.getURI(),NodeUtil.getSetStringURI(userProfile));
		
		if (resources.isEmpty()) {
			resources = SparqlWalk.getDirectLinksBetween2ResourcesInBothWays(nodeOriginal.getURI(), NodeUtil.getSetStringURI(userProfile));
			if (!resources.isEmpty()) {
				getDatabaseConnection().insertLinks(nodeOriginal.getURI(),resources);
				NodeUtil.print("Link "+nodeOriginal.getURI()+" inserted "+resources.size()+ " links");
			}else{
				 return new HashSet<Node>();
			}
		}

		resources = new ArrayList<Resource>(new HashSet<Resource>(resources));

		//parei aqui
		Set<Node> nodesTU = new HashSet<Node>(NodeUtil.convertResourceInNodes(resources, Integer.valueOf(NodeUtil.getMaxNodeID(cnns)),IConstants.LIKE, false,cnns));
		

		for (Node newNode : nodesTU) {
			NodeUtil.connect2Nodes(newNode, nodeOriginal);	
		}

		return new HashSet<Node>(nodeOriginal.getNodes());
	}	
	
		
	
	/**
	 * Building indirect N+
	 * @param userId
	 * @param neighboursPlus
	 */
	public void addIndirectLabeledNodesFromUserProfileOptimized(Map<Node, Set<Node>> neighboursPlus) {
		Set<Node> keyNodesHere = neighboursPlus.keySet();
		Set<Node> keyNodesLocal = new HashSet<Node>();
		for (Node node : keyNodesHere) {
			keyNodesLocal.addAll(neighboursPlus.get(node));
		}
		Set<Node> userProfileReduced = NodeUtil.removeNodeByURI(userProfile,trainingSet);
		addDirectLabeledNodesFromUserProfileOptimized(keyNodesLocal,userProfileReduced);			
	}

	/**
	 * Building direct N+
	 * @param nodes
	 */
	public void addDirectLabeledNodesFromUserProfileOptimized(Set<Node> nodes, Set<Node> userProfile) {
		// Very important for efficiency.
		userProfile = NodeUtil.removeNodeByURI(userProfile,new HashSet<Node>(cnns));
		Set<String> uriUserProfilesLabeledNodes = NodeUtil.getSetStringURI(userProfile);
		for (Node node : nodes) {
			int nodeId = generateNextIdForNPlus(cnns);
			//NodeUtil.print("Before N+ direct - Time elapse: "+(System.currentTimeMillis() - init));
			
			
			List<Resource> resources = getDatabaseConnection().getLinksIncomingAndOutcoming(node.getURI(),uriUserProfilesLabeledNodes);
			if (resources.isEmpty()) {
				resources = SparqlWalk.getDirectLinksBetween2ResourcesInBothWays(node.getURI(), uriUserProfilesLabeledNodes);
				if (!resources.isEmpty()) {
					getDatabaseConnection().insertLinks(node.getURI(),resources);
					NodeUtil.print("Link "+node.getURI()+" inserted "+resources.size()+ " links");
				}else{
					continue;
				}
			}			
			Set<Node> existingUserNodesProfile = NodeUtil.createNewLabeledNodesNotInNPlus(resources,IConstants.LIKE,trainingSet,nodeId);			

			for (Node existingUserNodeProfile : existingUserNodesProfile) {
				if (NodeUtil.isDistinctURI(node, existingUserNodeProfile)) {

					//if neighboursPlus already exists	
					if (neighboursPlus.containsKey(node) && (!neighboursPlus.get(node).contains(existingUserNodeProfile)) && existingUserNodeProfile.getLabel().equals(IConstants.LIKE)) {
							Set<Node> labeledNodesLocal = new HashSet<Node>();
							labeledNodesLocal.addAll(neighboursPlus.get(node));
							labeledNodesLocal.add(existingUserNodeProfile);
							trainingSet.add(existingUserNodeProfile);
							//NodeUtil.printNodes(traininSet);							
							neighboursPlus.put(node, labeledNodesLocal);
							NodeUtil.connect2Nodes(node, existingUserNodeProfile);
					// first time when neighboursPlus is empty or null
					}else if (existingUserNodeProfile.getLabel().equals(IConstants.LIKE)) {
							Set<Node> labeledNodesLocal = new HashSet<Node>();
							labeledNodesLocal.add(existingUserNodeProfile);
							trainingSet.add(existingUserNodeProfile);
							neighboursPlus.put(node, labeledNodesLocal);
							NodeUtil.connect2Nodes(node, existingUserNodeProfile);							
					}

				}
			}
		}
	}
	

	/**
	 * Generate the ids of N+ new nodes
	 * @return
	 */
	private static int generateNextIdForNPlus(List<Node> cnns) {
		int neighboursPlusMaxSize = 0;
		for (Node node : neighboursPlus.keySet()) {
			neighboursPlusMaxSize = neighboursPlusMaxSize + (neighboursPlus.get(node).size());
		}
		int id = Integer.valueOf(NodeUtil.getMaxNodeID(cnns)) + neighboursPlusMaxSize;
		return id;
	}
	
	/**
	 * Load the CNNs set from linked data and convert them into nodes one the graph.
	 * @param candidates
	 * @param filterDomain - filter by the domains of the dataset i.e movies, music and books
	 */
	private Set<Node> loadNeighbourhoodFromLOD(Set<Node> candidates, boolean filterDomain) {
		//printNodes(allnodes);
		Set<Node> newNeighbours = new HashSet<Node>();
		Set<Node> tempNodes = new HashSet<Node>(candidates);
		for (int i = 0; i < IConstants.N; i++) {
			Set<Node> aux = null; 
			for (Node node : tempNodes) {
					//NodeUtil.print("On degree "+i+" loading neighbourhood for "+node.getURI()+ " from starting node "+nodeUnderEvaluation.getURI());
				    aux = new HashSet<Node>();
					aux.addAll(addNeighborhoodFromLODAndConvertToNodes(node, userId, filterDomain,i));
					newNeighbours.addAll(aux);
					//NodeUtil.print("On degree "+(i+1)+" neighbourhood size is "+newNeighbours.size()+ " for starting node "+nodeUnderEvaluation.getURI());
			}
			NodeUtil.print("For N = "+(i+1)+" and starting node "+nodeUnderEvaluation.getURI() +" the neighbourhood size is "+newNeighbours.size());
			tempNodes = new HashSet<Node>(aux);
		}
		//printNodes(newNeighbours);
		return newNeighbours;
	}
	
	/**
	 * Get Tu by NPRIME
	 */
	private Set<Node> getTubyNPrime(Node nodeToTU, boolean filterDomain) {
		//printNodes(allnodes);
		Set<Node> newNeighbours = new HashSet<Node>();
		Set<Node> tempNodes = new HashSet<Node>();
		tempNodes.add(nodeToTU);
		for (int i = 0; i < IConstants.N_PRIME; i++) {
			Set<Node> aux = null; 
			for (Node node : tempNodes) {
					//NodeUtil.print("On degree "+i+" loading neighbourhood for "+node.getURI()+ " from starting node "+nodeUnderEvaluation.getURI());
				    aux = new HashSet<Node>();
					aux.addAll(addNeighborhoodFromLODAndConvertToNodes(node, userId, filterDomain,i));
					newNeighbours.addAll(aux);
					//NodeUtil.print("On degree "+(i+1)+" neighbourhood size is "+newNeighbours.size()+ " for starting node "+nodeUnderEvaluation.getURI());
			}
			NodeUtil.print("For N_PRIME = "+(i+1)+" and node "+nodeToTU.getURI() +" the TU neighbourhood size is "+newNeighbours.size());
			tempNodes = new HashSet<Node>(aux);
		}
		//printNodes(newNeighbours);
		return newNeighbours;
	}	
	
	
	/**
	 * Classify all nodes. It calculate the LDSD distance Ndist
	 * @return
	 */
	private List<NodePrediction> classify() {
		
		List<NodePrediction> memoryPredictions = new ArrayList<NodePrediction>();
		
		List<Node> testNodes = new ArrayList<Node>(originalUnlabeledNodesToClassify);
		
		Collections.reverse(testNodes);

		if (isEvaluation) {
			NodeUtil.labelNodes(candidates, IConstants.NO_LABEL);
			NodeUtil.print(originalUnlabeledNodesToClassify.size()+" nodes to be classified");
		}			



		for (Node nodeTest : testNodes) {

			// classify new ones only, except the node under evaluatio
			if (isEvaluation && !NodeUtil.isDistinctURI(nodeTest, nodeUnderEvaluation)) {
				NodePrediction existingPrediction = Lodica.getDatabaseConnection().getPrediction(nodeUnderEvaluation.getURI(),nodeTest.getURI(), userId, IConstants.USE_ICA);
				if (existingPrediction!=null) {
					memoryPredictions.add(existingPrediction);
					continue;
				}				
			}
			
			
			// getting Tu of nodeTest. Here happens the NN-d(r,Tu,dist)
			double shortestNeighbourDistance = 1d;
			double shortestNeighbourOfNeighbourDistance = 1d;			
			Set<Node> labeledNeighbours = NodeUtil.getLabeledNodes(nodeTest.getNodes());
			//NodeUtil.print("Classifying "+nodeTest.getURI() +" using training nodes "+labeledNeighbours.toString());
			Node nearestNeighbour = null;
			for (Node neighbour : labeledNeighbours) {
				Double dist = getSemanticDistance(nodeTest, neighbour);
				if (dist<=shortestNeighbourDistance) {
					shortestNeighbourDistance = dist;
					nearestNeighbour = neighbour;
				}
			}
			if (nearestNeighbour!=null && nearestNeighbour.getNodes()!=null) {
				labeledNeighbours = NodeUtil.getLabeledNodes(nearestNeighbour.getNodes());	
				for (Node neighbour : labeledNeighbours) {
					Double dist = getSemanticDistance(nearestNeighbour, neighbour);
					if (dist<=shortestNeighbourOfNeighbourDistance) {
						shortestNeighbourOfNeighbourDistance = dist;
					}					
				}	
			}
			
			//Keep the predictions for late update (of labels)
			NodePrediction prediction = null;
			if (shortestNeighbourDistance<=shortestNeighbourOfNeighbourDistance) {
				if (isEvaluation && !NodeUtil.isDistinctURI(nodeTest, nodeUnderEvaluation)) {
					prediction = new NodePrediction(IConstants.SEED_EVALUATION, nodeTest, nodeTest.getLabel(),IConstants.LIKE,DBFunctions.checkSimilarityMethod(IConstants.USE_ICA),shortestNeighbourDistance,userId);					
				}else{
					prediction = new NodePrediction(nodeUnderEvaluation.getURI(), nodeTest, nodeTest.getLabel(),IConstants.LIKE,DBFunctions.checkSimilarityMethod(IConstants.USE_ICA),shortestNeighbourDistance,userId);
				}

			}else{
				if (isEvaluation && !NodeUtil.isDistinctURI(nodeTest, nodeUnderEvaluation)) {
					prediction = new NodePrediction(IConstants.SEED_EVALUATION, nodeTest, nodeTest.getLabel(),IConstants.NO_LABEL,DBFunctions.checkSimilarityMethod(IConstants.USE_ICA),shortestNeighbourDistance,userId);					
				}else{
					prediction = new NodePrediction(nodeUnderEvaluation.getURI(), nodeTest, nodeTest.getLabel(),IConstants.NO_LABEL,DBFunctions.checkSimilarityMethod(IConstants.USE_ICA),shortestNeighbourDistance,userId);
				}				
			}

			memoryPredictions.add(prediction);
		}
		
		NodeUtil.print("Classification has ended with "+memoryPredictions.size()+" predictions");
		getDatabaseConnection().insertOrUpdatePredictions(memoryPredictions);
		NodeUtil.print(memoryPredictions.size()+" predictions saved");
		
		return memoryPredictions;
		
	}
	
	/**
	 * @param nodeTest
	 * @param neighbour
	 * @param dist
	 * @return
	 */
	private Double getSemanticDistance(Node nodeTest, Node neighbour) {
		double dist = 1d;
		if (IConstants.USE_SEMANTIC_DISTANCE && IConstants.USE_ICA) {
			return Classifier.calculateSemanticDistance(nodeTest, neighbour,IConstants.LDSD_LOD);
		}else if (IConstants.USE_SEMANTIC_DISTANCE && !IConstants.USE_ICA){
			return Classifier.calculateSemanticDistance(nodeTest, neighbour,IConstants.LDSD);	
		}
		return dist;
	}

}