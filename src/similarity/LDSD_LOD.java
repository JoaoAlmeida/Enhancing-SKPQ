package similarity;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import node.IConstants;
import node.Lodica;
import node.Node;
import node.NodeUtil;
import node.SparqlWalk;

public class LDSD_LOD {
	
	public static String resourceBRA = "http://dbpedia.org/resource/Brazil";
	public static String resourceIRE = "http://dbpedia.org/resource/Republic_of_Ireland";
	//static String resourceA = "http://dbpedia.org/resource/Finding_Nemo";
	public static String resourceA = "http://dbpedia.org/resource/Brazil";
	//static String resourceA = "http://dbpedia.org/resource/!!!";
	//static String resourceB = "http://dbpedia.org/resource/Finding_Dory";
	public static String resourceB = "http://dbpedia.org/resource/Republic_of_Ireland";
	
	
	//static String resourceC = "http://dbpedia.org/resource/AMC_Concord";
	public static String resourceC = "http://dbpedia.org/resource/Pixar";
	//static String resourceC = "http://dbpedia.org/resource/25_Years_On";
	
	
	public static String resourceD = "http://dbpedia.org/resource/Finding_Nemo";
	public static String resourceE = "http://dbpedia.org/resource/Finding_Dory";
	
	static String resourceJC = "http://dbpedia.org/resource/Johnny_Cash";
	static String resourceJCC = "http://dbpedia.org/resource/June_Carter_Cash";
	static String resourceKK = "http://dbpedia.org/resource/Al_Green";
	static String resourceEP = "http://dbpedia.org/resource/Elvis_Presley";
	
	
	public static void main(String[] args) {	
/*		//String resources[] = {resourceA,resourceB,resourceC,resourceD,resourceE};
		//String resources[] = {resourceJC,resourceEP,resourceKK};
		//simVSMbyProperty(resources);
		
		//System.out.println(LDSDweighted(resourceJC,resourceJC));
		//System.out.println(LDSDweighted("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/resource/June_Carter_Cash"));
		System.out.println(LDSDweighted("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/resource/Bob_Dylan"));
		System.out.println(LDSD.LDSDweighted("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/resource/Bob_Dylan"));
		//System.out.println(LDSDweighted("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/resource/Al_Green"));
		//System.out.println(LDSDweighted("http://dbpedia.org/resource/Johnny_Cash","http://dbpedia.org/resource/ElWWvis_Presley"));
		//System.out.println(LDSDweighted("http://dbpedia.org/resource/Diary","http://dbpedia.org/resource/Fight_Club"));
		//System.out.println(LDSDweighted(resourceJC,resourceJCC));
		//System.out.println(LDSDweighted(resourceKK,resourceJC));
		//System.out.println(LDSDweighted(resourceJC,resourceEP));
*/		
		
		
	/*	System.out.println("LDSDdirect	"+LDSDdirect("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		System.out.println("LDSDDirectweighted "+LDSDDirectweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		
		System.out.println("LDSDIndirect	"+LDSDIndirect("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		System.out.println("LDSDIndirectweighted "+LDSDIndirectweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		
		System.out.println("LDSDweighted	"+LDSDweighted("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));	*/	
		
		System.out.println("LDSDdirect	"+LDSDweighted("http://dbpedia.org/resource/Argentina","http://dbpedia.org/resource/Brazil"));		
		
		
	}
	public static List<VSMSimilarity> simVSMbyProperty(String[] resources){
		
		List<VSMSimilarity> vSMSimilaritys = new ArrayList<VSMSimilarity>();
		
		List<String> control = new ArrayList<String>();

		for (String r1 : resources) {
			for (String r2 : resources) {
				if (r1!=r2  && (!(control.contains(r2+r1)||control.contains(r1+r2)))) {
					VSMSimilarity vsmSimilarity = new VSMSimilarity(new ResourceImpl(r1), new ResourceImpl(r2));
					vsmSimilarity.setSimScore(LDSDweighted(r1,r2));
					vSMSimilaritys.add(vsmSimilarity);
				}
				control.add(r1+r2);
				control.add(r2+r1);
			}
		}
		
		for (VSMSimilarity vsmSimilarity : vSMSimilaritys) {
			System.out.println("VSM:"+vsmSimilarity.getResource1().getLocalName()+ " X " + vsmSimilarity.getResource2().getLocalName()+ " has score == "+String.format( "%.2f", (vsmSimilarity.getSimScore())));				
		}

		return vSMSimilaritys;
	}	
	

	
	
	public static double LDSDweighted(String resourceA, String resourceB){
		if (resourceA.trim().equals(resourceB.trim())) {
			return 1d;
		}	
		double ldsd = 1d / ((double)(1d + (double)nDDLR(resourceA,resourceB) + (double)nDDLR(resourceB,resourceA) + (double)nIDLI_LOD(resourceA,resourceB) + (double)nIDLO(resourceB,resourceA)));
		//System.out.println("LDSDweighted:"+ldsd);
		return ldsd;
	}
	
	
	public static double LDSDregular(String resourceA, String resourceB){
		double ldsd = 0;
		double direct = (double)LDSDdirect(resourceA,resourceB);
		double indirect = (double)LDSDIndirect(resourceA,resourceB);
		
		if (direct==0) {
			ldsd = indirect;
		}else{
			ldsd = direct;	
		}
		
		return ldsd;
	}
	
	public static double LDSD_LODIndirect(String resourceA, String resourceB){
		
		double totalIndireictIncoming =(double)SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween2Resources(resourceA, resourceB);
		double totalIndireictUnobserved = getTotalOfIncomingLinksOutOfUnobservedRelations(resourceA, resourceB);
		double totalIndireictIncomingICA = totalIndireictIncoming + totalIndireictUnobserved;
		
		double ldsd = 1d / ((double)(1d + totalIndireictIncomingICA + (double)SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween2Resources(resourceA, resourceB)));
		
		//System.out.println("LDSDindirect:"+ldsd);
		return ldsd;
	}	
	
	public static double LDSDdirect(String resourceA, String resourceB){
		double ldsd = 1d / ((double)(1d + (double)SparqlWalk.countDirectLinksBetween2Resources(resourceA, resourceB)+(double)SparqlWalk.countDirectLinksBetween2Resources(resourceB, resourceA)));
		//System.out.println("LDSDdirect:"+ldsd);
		return ldsd;
	}
	
	public static double LDSDIndirect(String resourceA, String resourceB){
		double ldsd = 1d / ((double)(1d + (double)SparqlWalk.countTotalNumberOfIndirectInconmingLinksBetween2Resources(resourceA, resourceB)+ (double)SparqlWalk.countTotalNumberOfIndirectOutgoingLinksBetween2Resources(resourceA, resourceB)));
		//System.out.println("LDSDindirect:"+ldsd);
		return ldsd;
	}
	
	public static double LDSDDirectweighted(String resourceA, String resourceB){
		double ldsd = 1d / ((double)(1d + (double)nDDLR(resourceA,resourceB) + (double)nDDLR(resourceB,resourceA)));
		//System.out.println("LDSDweighted:"+ldsd);
		return ldsd;
	}	
	
	public static double LDSDIndirectweighted(String resourceA, String resourceB){
		double ldsd = 1d / ((double)(1d + (double)nIDLI_LOD(resourceA,resourceB) + (double)nIDLO(resourceB,resourceA)));
		//System.out.println("LDSDweighted:"+ldsd);
		return ldsd;
	}
	
	/**
	 * DONE
	 * @param resourceA
	 * @param resourceB
	 * @param linkInstance
	 * @return
	 */
	public static double nDDLR(String resourceA, String resourceB){
		double sum = 0d;
		List<Resource> resources = new ArrayList<Resource>();
		resources = SparqlWalk.getDirectLinksBetween2Resources(resourceA,resourceB);
		for (Resource resource : resources) {
			sum = sum + ((calculateNumberDirectLinksBetween2Resources(resourceA,resourceB)/ (1 + Math.log(calculateTotalDirectLinksFromResourceAndLink(resourceA,resource.getURI())))));
		}
		return sum; 
	}
	
	
	
	/**
	 * DONE
	 * Function that compute the number of Indirect and Distinct Outcome Links
	 * @param resourceA
	 * @param resourceB
	 * @param linkInstance
	 * @return
	 */
	public static double nIDLO(String resourceA, String resourceB){
		double sum = 0d;
		List<Resource> resources = new ArrayList<Resource>();
		resources = SparqlWalk.getIndirectDistinctOutgoingLinksBetween2Resources(resourceA,resourceB);
		for (Resource resource : resources) {		
			sum = sum + ( SparqlWalk.getIndirectOutgoingLinkFrom2ResourcesAndLink(resourceA, resourceB,resource.getURI()) / (1 + Math.log(calculateTotalNumberIndirectOutgoingLinksFromResourceAndLink(resourceA,resource.getURI()))));
		}
		return sum; 
	}
	
	/**
	 * DONE
	 * Function that compute the number of Indirect and Distinct Income Links
	 * @param resourceA
	 * @param resourceB
	 * @param linkInstance
	 * @return
	 */
	public static double nIDLI_LOD(String resourceA, String resourceB){
		double sum = 0d;
		List<Resource> resources = new ArrayList<Resource>();
		resources = SparqlWalk.getIndirectDistinctInconmingLinksBetween2Resources(resourceA,resourceB);
		for (Resource resource : resources) {
			double totalIndireictIncoming =(double)SparqlWalk.getIndirectIncomingLinkFrom2ResourcesAndLink(resourceA, resourceB,resource.getURI());
			double totalIndireictUnobserved = getTotalOfIncomingLinksOutOfUnobservedRelations(resourceA, resourceB);
			double indirectIncomingLinkFrom2ResourcesAndLink = totalIndireictIncoming + totalIndireictUnobserved;
			sum = sum + ( indirectIncomingLinkFrom2ResourcesAndLink / (1 + Math.log(calculateTotalNumberIndirectInconmingLinksFromResourceAndLink_LOD(resourceA,resource.getURI()))));			
		}
		
		//System.out.println("nIDLI_LOD sim "+sum);System.out.println();
		return sum; 
	}
	
	
	private static double getTotalOfIncomingLinksOutOfUnobservedRelationsOld(String resourceA, String resourceB) {
		double total = 0d;
		Node nodeA = null;
		Node nodeB = null;	
		if (Lodica.cnns!=null) {
			nodeA = NodeUtil.getNodeByURI(resourceA,Lodica.cnns);
			nodeB = NodeUtil.getNodeByURI(resourceB,Lodica.cnns);
			if (nodeA!=null && nodeB!=null && nodeA.getRelationalFeatures()!=null && nodeB.getRelationalFeatures()!=null) {
				double totalLikeA=nodeA.getRelationalFeatures().get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.LIKE);
				double totalLikeB=nodeB.getRelationalFeatures().get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.LIKE);
				double totalDisikeA=nodeA.getRelationalFeatures().get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.DISLIKE);
				double totalDisikeB=nodeB.getRelationalFeatures().get(IConstants.RELATIONAL_UNOBSERVED_ATTRIBUTE+IConstants.DISLIKE);		
				//System.out.println("totalLikeA"+totalLikeA);
				//System.out.println("totalLikeB"+totalLikeB);
				//System.out.println("totalDisikeA"+totalDisikeA);
				//System.out.println("totalDisikeB"+totalDisikeB);
				//System.out.println();
				if (totalLikeA>0 && totalLikeB>0) {
					total = total+ totalLikeA;
				}
				
				//System.out.println("sumLIKE"+sum);System.out.println();
				
				if (totalDisikeA>0 && totalDisikeB>0) {
					total = total + totalDisikeA;
				}
			}else{
				System.out.println(" RELATIONAL_UNOBSERVED_ATTRIBUTE NOT CALCULATED ");
			}			
		}else{
				System.out.println(" GRAPH IS NOT CREATED ");
		}		
	//System.out.println("totalDisikeB"+sum);System.out.println();
	return total;
}	
	
	
	private static double getTotalOfIncomingLinksOutOfUnobservedRelations(String resourceA, String resourceB) {
		double total = 0d;
		Node nodeA = null;
		Node nodeB = null;	
		if (Lodica.cnns!=null) {
			nodeA = NodeUtil.getNodeByURI(resourceA,Lodica.cnns);
			nodeB = NodeUtil.getNodeByURI(resourceB,Lodica.cnns);
			if (nodeA!=null && nodeB!=null && nodeA.getLabel().equals(IConstants.LIKE) &&  nodeB.getLabel().equals( nodeA.getLabel())) {
				total = 1d;
			}
		}		
		//System.out.println("totalDisikeB"+sum);System.out.println();
		return total;
	}	
	
	
	
	/**
	 * DONE
	 * cdl1rarb
	 * @param resourceA
	 * @param resourceB
	 * @return
	 */
	public static int calculateNumberDirectLinksBetween2Resources(String resourceA, String resourceB){
		int numberDirectDistinctLinks = SparqlWalk.countDirectLinksBetween2Resources(resourceA,resourceB);
		return numberDirectDistinctLinks;
	}
	
	/**
	 * DONE
	 * @param resourceA
	 * @param link
	 * @return
	 */
	public static int calculateTotalDirectLinksFromResourceAndLink(String resourceA,String uri){
		int totalNumberDirectDistinctLinks = SparqlWalk.countTotalDirectLinksFromResourceAndProperty(resourceA,uri);
        return totalNumberDirectDistinctLinks;
	}	

	/**
	 * DONE
	 * ciiliran
	 * @param resourceA
	 * @param resourceB
	 * @return
	 */
	public static int calculateTotalNumberIndirectInconmingLinksFromResourceAndLink(String resource, String link){
		int numberResourcesIndirectLinkByResourceAndLink = SparqlWalk.countIndirectIncomingLinksFromResourceAndLink(resource,link);
		return numberResourcesIndirectLinkByResourceAndLink;
	}
	
	/**
	 * DONE
	 * ciiliran
	 * @param resourceA
	 * @param resourceB
	 * @return
	 */
	public static int calculateTotalNumberIndirectInconmingLinksFromResourceAndLink_LOD(String resource, String link){
		int numberResourcesIndirectLinkByResourceAndLink = SparqlWalk.countIndirectIncomingLinksFromResourceAndLink(resource,link);
		numberResourcesIndirectLinkByResourceAndLink = numberResourcesIndirectLinkByResourceAndLink + countIndirectIncomingLinksFromResourceAndLink_LOD(resource,IConstants.LIKE);
		return numberResourcesIndirectLinkByResourceAndLink;
	}	
	
	/**
	 * DONE
	 * ciiliran
	 * @param resourceA
	 * @param resourceB
	 * @return
	 */
	public static int countIndirectIncomingLinksFromResourceAndLink_LOD(String resource, String link){
		Node node = null;
		int numberResourcesIndirectLinkByResourceAndLink = 0;
		if (Lodica.cnns!=null) {
			node = NodeUtil.getNodeByURI(resource,Lodica.cnns);
			
			//N+
			if (Lodica.neighboursPlus!=null && Lodica.neighboursPlus.get(node)!=null) {
				numberResourcesIndirectLinkByResourceAndLink = Lodica.neighboursPlus.get(node).size();				
			}
			
			//Node likes in CNN
			if (node!=null && node.getRelationalFeatures()!=null) {
				for (Node otherNodeInCNN : Lodica.cnns) {
					if (NodeUtil.isDistinctURI(otherNodeInCNN, node) && node.getLabel().equals(link) && otherNodeInCNN.getLabel().equals(node.getLabel())) {
						numberResourcesIndirectLinkByResourceAndLink = numberResourcesIndirectLinkByResourceAndLink + 1;	
					}
				}
			}else{
				System.out.println(" RELATIONAL_UNOBSERVED_ATTRIBUTE NOT CALCULATED ");
			}			
		}else{
				System.out.println(" GRAPH IS NOT CREATED ");
		}		
		return numberResourcesIndirectLinkByResourceAndLink;
	}	
	
	/**
	 * DONE
	 * cioliran
	 * @param resourceA
	 * @param resourceB
	 * @return
	 */
	public static int calculateTotalNumberIndirectOutgoingLinksFromResourceAndLink(String resource, String link){
		int numberResourcesIndirectLinkByResourceAndLink = SparqlWalk.countIndirectOutgoingLinksFromResourceAndLink(resource,link);		
		return numberResourcesIndirectLinkByResourceAndLink;
	}
	
	

}
