package node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NodeLoad {
	
	public static String resourceA = "http://dbpedia.org/resource/Finding_Nemo";
	
	
	public static String resourceB = "http://dbpedia.org/resource/Andrew_Stanton";
	public static String resourceB1 = "http://dbpedia.org/resource/Voice_acting";
	public static String resourceB2 = "http://dbpedia.org/resource/Film_director";
	
	
	public static String resourceC = "http://dbpedia.org/resource/Walt_Disney_Studios_Motion_Pictures";
	public static String resourceC1 = "http://dbpedia.org/resource/Division_(business)";
	public static String resourceC2 = "http://dbpedia.org/resource/Touchstone_Pictures";
	
	public static String resourceD = "http://dbpedia.org/resource/Willem_Dafoe";
	public static String resourceD1 = "http://dbpedia.org/resource/Giada_Colagrande";
	public static String resourceD2 = "http://dbpedia.org/resource/Elizabeth_LeCompte";

	
	/**
	 * @return
	 */
	public static Set<Node> loadTestNodes(){
		
		Set<Node> allnodes = new HashSet<Node>();
		Node nodeA = new Node("1",IConstants.LIKE,resourceA);

		Node nodeB = new Node("2",null,resourceB);
		Node nodeB1 = new Node("3",null,resourceB1);
		Node nodeB2 = new Node("4",null,resourceB2);
		
		Node nodeC = new Node("5",null,resourceC);
		Node nodeC1 = new Node("6",null,resourceC1);
		Node nodeC2 = new Node("7",null,resourceC2);
		
		Node nodeD = new Node("8",null,resourceD);
		Node nodeD1 = new Node("9",null,resourceD1);
		Node nodeD2 = new Node("10",null,resourceD2);
		
		NodeUtil.connect2Nodes(nodeA, nodeB);
		NodeUtil.connect2Nodes(nodeA, nodeC);
		NodeUtil.connect2Nodes(nodeA, nodeD);
		
		NodeUtil.connect2Nodes(nodeB, nodeB1);
		NodeUtil.connect2Nodes(nodeB, nodeB2);
		
		NodeUtil.connect2Nodes(nodeC, nodeC1);
		NodeUtil.connect2Nodes(nodeC, nodeC2);
		
		NodeUtil.connect2Nodes(nodeD, nodeD1);
		NodeUtil.connect2Nodes(nodeD, nodeD2);	
		
		allnodes.add(nodeA);
		
		allnodes.add(nodeB);
		allnodes.add(nodeB1);
		allnodes.add(nodeB2);
		
		allnodes.add(nodeC);
		allnodes.add(nodeC1);
		allnodes.add(nodeC2);
		
		allnodes.add(nodeD);
		allnodes.add(nodeD1);
		allnodes.add(nodeD2);
		
		Collections.sort(new ArrayList<Node>(allnodes));
		return allnodes;
	}
	
	
	public static Set<Node> loadTestMiniNodes(){
		
		Set<Node> allnodes = new HashSet<Node>();
		Node nodeA = new Node("1",IConstants.LIKE,resourceA);

		Node nodeB = new Node("2",null,"http://dbpedia.org/resource/Alexander_Gould");
		
		Node nodeC = new Node("3",null,"http://dbpedia.org/resource/Albert_Brooks");

		NodeUtil.connect2Nodes(nodeA, nodeB);
		
		NodeUtil.connect2Nodes(nodeA, nodeC);

		allnodes.add(nodeA);
		allnodes.add(nodeB);
		allnodes.add(nodeC);

		Collections.sort(new ArrayList<Node>(allnodes));
		return allnodes;
	}	
	
}
