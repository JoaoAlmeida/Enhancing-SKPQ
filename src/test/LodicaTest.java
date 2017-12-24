package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import node.IConstants;
import node.Lodica;
import node.LodicaOldVersion;
import node.Node;
import node.NodeLoad;
import node.NodePrediction;
import node.NodeUtil;
import node.SparqlWalk;
import similarity.LDSD;
import similarity.LDSD_LOD;
import util.StringUtilsNode;

public class LodicaTest {
	
	@Test
	public void testNPlus() throws Exception {
		
		Set<Node> startingSet = new HashSet<Node>();
		
		Node node1 = new Node("1",IConstants.NO_LABEL,"http://dbpedia.org/resource/Willem_Dafoe");

		Node node2 = new Node("2",IConstants.NO_LABEL,"http://dbpedia.org/resource/Albert_Brooks");
		
		Node node3 = new Node("99",IConstants.LIKE,"http://dbpedia.org/resource/Category:Living_people");
		
		Node node4 = new Node("48",IConstants.LIKE,"http://dbpedia.org/resource/Finding_Nemo");
		
		Node node5 = new Node("58",IConstants.LIKE,"http://dbpedia.org/resource/Finding_Dory");
		
		Node node6 = new Node("6",IConstants.LIKE,"http://dbpedia.org/resource/Andrew_Stanton");

		//NodeUtil.connect2Nodes(node1, node2);
		
		Lodica ica = new Lodica();
		
		startingSet.add(node1);
		startingSet.add(node2);
		
		Set<Node> userProfileTest = new HashSet<Node>();
		
		userProfileTest.add(node3);
		userProfileTest.add(node4);
		userProfileTest.add(node5);
		userProfileTest.add(node6);
		
		ica.userProfile = userProfileTest;
		
		ica.cnns = new ArrayList<Node>(startingSet);
		//NodeUtil.printNodes(ica.cnns);
		ica.addDirectLabeledNodesFromUserProfileOptimized(startingSet,ica.userProfile);
		NodeUtil.print("NEIGHBOURS+ : "+ica.neighboursPlus.toString());
		ica.addIndirectLabeledNodesFromUserProfileOptimized(ica.neighboursPlus);
		NodeUtil.print("NEIGHBOURS+ : "+ica.neighboursPlus.toString());
		NodeUtil.showGraph(true,ica.cnns);
		//NodeUtil.printNodes(ica.cnns);
		
		
	    //NodeUtil.print("NEIGHBOURS+   direct key size : "+ica.neighboursPlus.keySet().size());
		//ica.addIndirectLabelledNodesFromUserProfileOptimized(ica.neighboursPlus);
		//NodeUtil.print("NEIGHBOURS+ indirect key size : "+ica.neighboursPlus.keySet().size());
		//NodeUtil.printNodes(ica.cnns);
		//NodeUtil.showGraph(true,ica.cnns);
		Lodica.useNPLUS = true;
	
		
		

	}		
	
	@Ignore
	@Test
	public void testICALocalGraphWithExternalNodes() throws Exception {
		
		Set<Node> startingSet = new HashSet<Node>();
		
		Node node1 = new Node("1",IConstants.LIKE,"http://dbpedia.org/resource/Finding_Nemo");

		Node node2 = new Node("2",IConstants.LIKE,"http://dbpedia.org/resource/Argentina");
		
		Node node3 = new Node("3",null,"http://dbpedia.org/resource/Portuguese_language");
		
		Node node4 = new Node("4",IConstants.LIKE,"http://dbpedia.org/resource/Beverly_Hills,_California");
		
		Node node5 = new Node("5",IConstants.LIKE,"http://dbpedia.org/resource/Brazil");
		
		Node node6 = new Node("6",IConstants.LIKE,"http://dbpedia.org/resource/Frank_Sinatra");

		NodeUtil.connect2Nodes(node1, node2);
		NodeUtil.connect2Nodes(node1, node3);
		NodeUtil.connect2Nodes(node3, node4);
		NodeUtil.connect2Nodes(node2, node5);
		NodeUtil.connect2Nodes(node2, node6);

		startingSet.add(node1);
		startingSet.add(node2);
		startingSet.add(node3);
		startingSet.add(node4);
		
		Lodica.useNPLUS = true;
		LodicaOldVersion.neighboursOutOfCandidateSet.add(node5);
		LodicaOldVersion.neighboursOutOfCandidateSet.add(node6);

		Collections.sort(new ArrayList<Node>(startingSet));
		
		Lodica ica = new Lodica();
		
		Lodica.isLocalTest = true;
		
		Lodica.userProfile.add(node4);
		
		Lodica.cnns = new ArrayList<Node>(startingSet);
		
		NodeUtil.printNodes(Lodica.cnns);

		Lodica lodica = new Lodica();

		Lodica.userId = 10;
		
		ica.runLODICALDSD(new HashSet<Node>(Lodica.cnns));

		Assert.assertTrue(NodeUtil.getNodeByID("3").getLabel()==IConstants.NO_LABEL);

	}	
	
	@Ignore
	@Test
	public void testICALocalGraphOneLineClassifier() throws Exception {
		
		Set<Node> startingSet = new HashSet<Node>();
		
		Node node1 = new Node("1",null,"http://dbpedia.org/resource/Finding_Nemo");

		Node node2 = new Node("2",IConstants.LIKE,"http://dbpedia.org/resource/Argentina");
		
		Node node3 = new Node("3",IConstants.LIKE,"http://dbpedia.org/resource/Portuguese_language");
		
		Node node4 = new Node("4",IConstants.LIKE,"http://dbpedia.org/resource/Beverly_Hills,_California");
		
		Node node5 = new Node("5",IConstants.LIKE,"http://dbpedia.org/resource/Brazil");
		
		Node node6 = new Node("6",IConstants.LIKE,"http://dbpedia.org/resource/Frank_Sinatra");

		NodeUtil.connect2Nodes(node1, node2);
		NodeUtil.connect2Nodes(node1, node3);
		NodeUtil.connect2Nodes(node3, node4);
		NodeUtil.connect2Nodes(node2, node5);
		NodeUtil.connect2Nodes(node2, node6);

		startingSet.add(node1);
		startingSet.add(node2);
		startingSet.add(node3);
		startingSet.add(node4);
		startingSet.add(node5);
		startingSet.add(node6);

		Collections.sort(new ArrayList<Node>(startingSet));
		
		Lodica.isLocalTest = true;
		
		Lodica.userProfile.add(node4);
		
		Lodica.cnns = new ArrayList<Node>(startingSet);
		
		NodeUtil.printNodes(Lodica.cnns);
		
		NodeUtil.showGraph(true,Lodica.cnns);

		Lodica iCAFredFinal = new Lodica();

		iCAFredFinal.userId=10;
		
		iCAFredFinal.runLODICALDSD(new HashSet<Node>(Lodica.cnns));

		Assert.assertTrue(NodeUtil.getNodeByID("1").getLabel()==IConstants.NO_LABEL);

	}	
	
	
	
	@Ignore
	@Test
	public void testICALocalGraph() throws Exception {
		
		Set<Node> startingSet = new HashSet<Node>();
		
		Node nodeA = new Node("1",null,"http://dbpedia.org/resource/Finding_Nemo");

		Node nodeB = new Node("2",IConstants.LIKE,"http://dbpedia.org/resource/Alexander_Gould");
		
		Node nodeC = new Node("3",IConstants.LIKE,"http://dbpedia.org/resource/Albert_Brooks");
		
		Node nodeD = new Node("4",IConstants.LIKE,"http://dbpedia.org/resource/Beverly_Hills,_California");
		
		Node nodeE = new Node("5",IConstants.LIKE,"http://dbpedia.org/resource/Brazil");

		NodeUtil.connect2Nodes(nodeA, nodeB);
		NodeUtil.connect2Nodes(nodeA, nodeC);
		NodeUtil.connect2Nodes(nodeC, nodeD);
		NodeUtil.connect2Nodes(nodeB, nodeE);

		startingSet.add(nodeA);
		startingSet.add(nodeB);
		startingSet.add(nodeC);
		startingSet.add(nodeD);
		startingSet.add(nodeE);

		Collections.sort(new ArrayList<Node>(startingSet));
		
		Lodica.isLocalTest = true;
		
		Lodica.userProfile.add(nodeD);
		Lodica.cnns = new ArrayList<Node>(startingSet);
		NodeUtil.printNodes(Lodica.cnns);
		
		
		Lodica iCAFredFinal = new Lodica();
		
		iCAFredFinal.userId=10;
		iCAFredFinal.runLODICALDSD(new HashSet<Node>(Lodica.cnns));

		Assert.assertTrue(NodeUtil.getNodeByID("1").getLabel()==IConstants.LIKE);
		
		
		System.out.println("SECOND TEST");
		System.out.println(" ");
		
		NodeUtil.getNodeByID("1").setLabel(null);
		NodeUtil.getNodeByID("2").setURI("http://dbpedia.org/page/Brasilia");
		NodeUtil.getNodeByID("3").setURI("http://dbpedia.org/page/Portuguese_language");
		NodeUtil.printNodes(Lodica.cnns);
		
		iCAFredFinal.userId=10;
		iCAFredFinal.runLODICALDSD(new HashSet<Node>(Lodica.cnns));
		
		Assert.assertTrue(NodeUtil.getNodeByID("1").getLabel()==IConstants.LIKE);
		
		
	}		
	
	
	@Ignore
	@Test
	public void testLDSD_LOD() {
		
		
		Node node1 = new Node("r1",IConstants.LIKE,"http://learningsparql.com/ns/expenses#r1");
		Map<String,Double> relationalAttributes = new LinkedHashMap<String,Double>();
		relationalAttributes.put(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.LIKE, 1d);
		relationalAttributes.put(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.DISLIKE, 0d);
		node1.setRelationalFeatures(relationalAttributes);
		
		
		Node node2 = new Node("r2",IConstants.LIKE,"http://learningsparql.com/ns/expenses#r2");
		relationalAttributes = new LinkedHashMap<String,Double>();
		relationalAttributes.put(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.LIKE, 1d);
		relationalAttributes.put(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.DISLIKE, 0d);
		node2.setRelationalFeatures(relationalAttributes);

		List<Node> allnodes = new LinkedList<Node>();
		
		allnodes.add(node1);
		allnodes.add(node2);
		Lodica.cnns = new ArrayList<Node>(allnodes);
		
		Assert.assertTrue(LDSD.LDSDIndirect("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2")>=LDSD_LOD.LDSD_LODIndirect("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		Assert.assertTrue(LDSD.LDSDweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2")>=LDSD_LOD.LDSDweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		//System.out.println("LDSD.LDSDIndirect	"+LDSD.LDSDIndirect("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));		
		//System.out.println("LDSD_LOD.LDSDIndirect	"+LDSD_LOD.LDSD_LODIndirect("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		//System.out.println("LDSD.LDSDweighted	"+LDSD.LDSDweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		//System.out.println("LDSD_LOD.LDSDweighted	"+LDSD_LOD.LDSDweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
	}
	
	@Ignore
	@Test
	public void testLoadDataSetFromDatabaseAndLod() {
		Lodica.cnns = new ArrayList<Node>();
		Lodica.userId = 1;
		IConstants.N = 1;
		String uri = "http://dbpedia.org/resource/The_Hangover";
		Set<Node> startingNodes = new HashSet<Node>();
		startingNodes.addAll(Lodica.getDatabaseConnection().getNodesFromDatabase(Lodica.userId,uri));
		Lodica.cnns.addAll(startingNodes);
		
		
		NodeUtil.showGraph(true,Lodica.cnns);
		
		
	}
	
	
	@Ignore
	@Test
	public void testLDSD() {
		Assert.assertTrue(LDSD.LDSDweighted("http://dbpedia.org/resource/California","http://dbpedia.org/resource/Nevada")>0d);	
	}
	
	@Ignore
	@Test
	public void testExternalNodes() {
		IConstants.N = 1;
		LodicaOldVersion.isLocalTest = true;
		//ICATestFred.allnodes.add(node1);
		Set<Node> loadTestSet = NodeLoad.loadTestMiniNodes();
		Node node4 = new Node("4",IConstants.LIKE,"http://dbpedia.org/resource/California");
		loadTestSet.add(node4);
		Lodica.cnns = new ArrayList<Node>(loadTestSet);
		
		LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N, NodeUtil.getNodeByID("1"),null,false);
		Assert.assertTrue((Lodica.cnns.size()-2)==SparqlWalk.getCountDBpediaObjecstBySubject(NodeUtil.getNodeByID("1").getURI()));
		
		LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N, NodeUtil.getNodeByID("2"),null,true);
		Assert.assertTrue(NodeUtil.getNodeByID("2").getNodes().size()==2);
		
		LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N, NodeUtil.getNodeByID("2"),null,false);
		Assert.assertTrue(NodeUtil.getNodeByID("2").getNodes().size()==(SparqlWalk.getCountDBpediaObjecstBySubject(NodeUtil.getNodeByID("2").getURI())+1));

	}	
	
	@Ignore
	@Test
	public void testStartingSet() {
		IConstants.N = 1;
		Lodica.isLocalTest = true;

		//ICATestFred.allnodes.add(node1);
		Set<Node> loadTestSet = NodeLoad.loadTestMiniNodes();
		Node node4 = new Node("4",IConstants.LIKE,"http://dbpedia.org/resource/California");
		loadTestSet.add(node4);
		Lodica.cnns = new ArrayList<Node>(loadTestSet);
		
		LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N, NodeUtil.getNodeByID("1"),null,false);
		Assert.assertTrue((Lodica.cnns.size()-2)==SparqlWalk.getCountDBpediaObjecstBySubject(NodeUtil.getNodeByID("1").getURI()));
		
		LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N, NodeUtil.getNodeByID("2"),null,true);
		Assert.assertTrue(NodeUtil.getNodeByID("2").getNodes().size()==2);
		
		LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N, NodeUtil.getNodeByID("2"),null,false);
		Assert.assertTrue(NodeUtil.getNodeByID("2").getNodes().size()==(SparqlWalk.getCountDBpediaObjecstBySubject(NodeUtil.getNodeByID("2").getURI())+1));

	}	
	
	
	@Ignore
	@Test
	public void testClassifyStartingDistinctNodes() throws Exception {
		
		
		Lodica.isLocalTest = true;
		
		Set<Node> startingSet = NodeLoad.loadTestNodes();
		Node startingNode = NodeUtil.getNodeByID("1",startingSet);
		startingNode.setLabel(null);
		Node startingNode2 = NodeUtil.getNodeByID("2",startingSet);
		Node startingNode5 = NodeUtil.getNodeByID("5",startingSet);
		Node startingNode8 = NodeUtil.getNodeByID("8",startingSet);

		Set<Node> startingNodes = new HashSet<Node>();
		startingNodes.add(startingNode);
		startingNodes.add(startingNode2);
		startingNodes.add(startingNode5);
		startingNodes.add(startingNode8);
		
		NodeUtil.bootstrapNullLabelsNodes(new ArrayList<Node>(startingNodes), IConstants.NO_LABEL);
		Lodica.userProfile.isEmpty();
		Lodica.userProfile.add(new Node("50",IConstants.LIKE,startingNode5.getURI()));
		Lodica.userProfile.add(new Node("20",IConstants.DISLIKE,startingNode2.getURI()));
		Lodica.userProfile.add(new Node("200",IConstants.DISLIKE,startingNode2.getURI()));
		Lodica.userProfile.add(new Node("2000",IConstants.DISLIKE,startingNode2.getURI()));

		LodicaOldVersion.classifyStartingNodes(NodeUtil.getUnlabeledNodes(new ArrayList<Node>(startingNodes)));
		Assert.assertTrue(startingNode.getLabel()==IConstants.LIKE);
		Assert.assertTrue(startingNode2.getLabel()==IConstants.DISLIKE);
		Assert.assertTrue(startingNode5.getLabel()==IConstants.LIKE);
		Assert.assertTrue(startingNode8.getLabel()==IConstants.DISLIKE);

	}	
	
	@Ignore
	@Test
	public void testClassifyStartingNodes() throws Exception {
		
		
		Lodica.isLocalTest = true;
		Lodica.userProfile.add(new Node("10",IConstants.LIKE,NodeLoad.resourceA));
		Set<Node> startingSet = NodeLoad.loadTestNodes();

		Node startingNode = NodeUtil.getNodeByID("1",startingSet);
		startingNode.setLabel(IConstants.NO_LABEL);
		LodicaOldVersion.classifyNodeWithObservedAttributesOnly(startingNode, Lodica.userProfile);

		Assert.assertTrue(startingNode.getLabel()==IConstants.LIKE);
		
		startingNode.setLabel(IConstants.NO_LABEL);
		Node startingNode2 = NodeUtil.getNodeByID("2",startingSet);

		
		Set<Node> startingNodes = new HashSet<Node>();
		startingNodes.add(startingNode);
		startingNodes.add(startingNode2);
		Lodica.userProfile.add(new Node("100",IConstants.DISLIKE,startingNode2.getURI()));
		Lodica.userProfile.add(new Node("101",IConstants.DISLIKE,startingNode2.getURI()));

		LodicaOldVersion.classifyStartingNodes(NodeUtil.getUnlabeledNodes(new ArrayList<Node>(startingNodes)));

		Assert.assertTrue(startingNode.getLabel()==IConstants.LIKE);
		Assert.assertTrue(startingNode2.getLabel()==IConstants.DISLIKE);
		
	}	
	
	
	@Ignore
	@Test
	public void testICA() throws Exception {
		
		LodicaOldVersion.isLocalTest = false;
		
		LodicaOldVersion.useExternalClassifiedNodesForTrainingSet = false;
		
		LodicaOldVersion ica = new LodicaOldVersion();
		
		IConstants.N = 1;
		
		//ICATestFred.allnodes = new ArrayList<Node>(NodeLoad.loadTestNodes());
		Node node1 = new Node("1",IConstants.LIKE,"http://dbpedia.org/resource/Brazil");
		List<NodePrediction> predictions = ica.runLODICAFistVersionWithObservedAttributes(node1.getURI());
		Assert.assertFalse(predictions.isEmpty());
		Assert.assertTrue(Lodica.originalLabelledNodes.size()==1);
		Assert.assertFalse(Lodica.cnns.size()!=(Lodica.originalLabelledNodes.size()+Lodica.originalUnlabeledNodesToClassify.size()));
		Assert.assertFalse(Lodica.originalLabelledNodes.contains(Lodica.originalUnlabeledNodesToClassify.size()));

		NodeUtil.updateLabelFromWeb(predictions.get(0).getNode().getURI(), IConstants.DISLIKE,Lodica.cnns);
		NodeUtil.updateLabelFromWeb(predictions.get(1).getNode().getURI(), IConstants.DISLIKE,Lodica.cnns);
		NodeUtil.updateLabelFromWeb(predictions.get(2).getNode().getURI(), IConstants.DISLIKE,Lodica.cnns);

		predictions = ica.runLODICAFistVersionWithObservedAttributes(predictions.get(0).getNode().getURI());
		Assert.assertTrue(Lodica.originalLabelledNodes.size()==3);
		Assert.assertFalse(Lodica.cnns.size()!=(Lodica.originalLabelledNodes.size()+Lodica.originalUnlabeledNodesToClassify.size()));
		Assert.assertFalse(Lodica.originalLabelledNodes.contains(Lodica.originalUnlabeledNodesToClassify.size()));

		NodeUtil.printNodes(new ArrayList<Node>(Lodica.originalLabelledNodes));

	}
	
	@Ignore
	@Test
	public void testNeighbourhoodOnLOD() {
		Node node1 = new Node("1",IConstants.LIKE,LDSD.resourceA);
		IConstants.N = 1;
		Lodica.cnns = new ArrayList<>();
		List<Node> neighbourhood = LodicaOldVersion.loadLODNeighborhoodAndConvertToNodes(IConstants.N,node1, null,false);
		Assert.assertTrue((neighbourhood.size())==SparqlWalk.getCountDBpediaObjecstBySubject(node1.getURI()));
		Assert.assertTrue((SparqlWalk.getCountDBpediaObjecstBySubject(node1.getURI())+1)==Integer.valueOf(NodeUtil.getMaxNodeID(Lodica.cnns)));
		Assert.assertTrue(true);
	}
	
	
	
	@Ignore
	@Test
	public void testNeighbourhoodOnLocalNodes() {

		Lodica.isLocalTest = true;
		IConstants.N = 2;
		Set<Node> startingSet = NodeLoad.loadTestNodes();
		//ICATestFred.printNodes(new ArrayList<Node>(startingSet));
		Lodica.cnns = new ArrayList<Node>(startingSet);
		
		Node node1 = NodeUtil.getNodeByID("1");
		List<Node> neighborhood = LodicaOldVersion.getNeighborhood(IConstants.N, node1, null);
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("2")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("3")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("4")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("5")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("6")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("7")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("8")));
		
		Node node5 = NodeUtil.getNodeByID("5");
		neighborhood = LodicaOldVersion.getNeighborhood(IConstants.N, node5, null);
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("1")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("2")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("6")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("7")));		
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("8")));		

		Node node9 = NodeUtil.getNodeByID("9");
		neighborhood = LodicaOldVersion.getNeighborhood(IConstants.N, node9, null);
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("1")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("8")));
		Assert.assertTrue(neighborhood.contains(NodeUtil.getNodeByID("10")));		
	}
	
	@Ignore
	@Test
	public void testShowGraph() throws Exception {

		LodicaOldVersion.useExternalClassifiedNodesForTrainingSet = true;
		
		LodicaOldVersion ica = new LodicaOldVersion(); 
		
		IConstants.N = 2;
		
		//ICATestFred.allnodes = new ArrayList<Node>(NodeLoad.loadTestNodes());
		Node node1 = new Node("1",IConstants.LIKE,"http://dbpedia.org/resource/Ant");
		ica.runLODICAFistVersionWithObservedAttributes(node1.getURI());
		
		Assert.assertTrue(true);
	}
	
	@Ignore
	@Test
	public void testNormalization() {
		
		
		Node node1 = new Node("1",IConstants.LIKE,LDSD.resourceA);
		Map<String,String> observedAtrributes1 = new LinkedHashMap<String,String>();
		observedAtrributes1.put("aaa", "this is a test");
		observedAtrributes1.put("bbb", "-10");
		node1.setObservedAtrributes(observedAtrributes1);
		
		
		Node node2 = new Node("2",IConstants.LIKE,LDSD.resourceB);
		Map<String,String> observedAtrributes2 = new LinkedHashMap<String,String>();
		observedAtrributes2.put("aaa", "this is a test");
		observedAtrributes2.put("bbb","10");
		node2.setObservedAtrributes(observedAtrributes2);
		
		
		Node node3 = new Node("3",IConstants.LIKE,LDSD.resourceC);
		Map<String,String> observedAtrributes3 = new LinkedHashMap<String,String>();
		observedAtrributes3.put("aaa", "this is a test");
		observedAtrributes3.put("bbb", "-1");
		node3.setObservedAtrributes(observedAtrributes3);
		
		Node node4 = new Node("4",IConstants.LIKE,LDSD.resourceD);
		Map<String,String> observedAtrributes4 = new LinkedHashMap<String,String>();
		observedAtrributes4.put("aaa", "this is a test");
		//observedAtrributes4.put("bbb", "90");
		node4.setObservedAtrributes(observedAtrributes4);		
		
		List<Node> allnodes = new LinkedList<Node>();
		
		allnodes.add(node1);
		allnodes.add(node2);
		allnodes.add(node3);
		allnodes.add(node4);

		LodicaOldVersion.normalizeObservedAttributesMap(allnodes);
		//ICATestFred.printObservedAttributesForAllNodes(allnodes);
		
		Assert.assertTrue(Double.valueOf(node4.getObservedAtrributes().get("bbb"))==1d);
		
		Assert.assertTrue(Double.valueOf(node3.getObservedAtrributes().get("bbb"))==0d);
		
		double value3 = Double.valueOf(node3.getObservedAtrributes().get("bbb"));
		double value4 = Double.valueOf(node4.getObservedAtrributes().get("bbb"));
		

		Assert.assertTrue(StringUtilsNode.getDifferenceAndReturnNormalized01(value3, value4)==Math.abs(value3-value4));

	}
	
	
	
	
	

	
	
	

}
