package similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import node.SparqlWalk;

public class VSM {

	static Resource resourceA = new ResourceImpl("http://dbpedia.org/resource/Ocean's_Twelve");
	static Resource resourceB = new ResourceImpl("http://dbpedia.org/resource/Ocean's_Thirteen");
	static Resource resourceC = new ResourceImpl("http://dbpedia.org/resource/Titanic_(1997_film)");
	static Resource resourceD = new ResourceImpl("http://dbpedia.org/resource/Holiday_Heart");
	static Resource resourceE = new ResourceImpl("http://dbpedia.org/resource/The_Extendables");	
	
	public static void main(String[] args) {
		
		Resource[] resources = {resourceA,resourceD};
		//String resources[] = {resourceA.getURI(),resourceE.getURI()};
/*
		for (String r1 : resources) {
			for (String r2 : resources) {
				if (r1!=r2) {
					List<Resource> propertyResources = 	SparqlWalk.getObjectPropertiesSharedByResources(r1, r2);
					for (Resource propertyResource : propertyResources) {
						System.out.println("VSM:"+r1+ " X " + r2+ " for property "+ propertyResource.getLocalName() + " == "+String.format( "%.2f", (VSM.simVSMbyProperty(r1,r2,propertyResource.getURI()) )));
					}
				}
			}
		}*/
		
		simVSMbyProperty(Arrays.asList(resources));
	
	}

		
	public static void simVSMbyProperty(List<Resource> uris){
		
		List<VSMSimilarity> vSMSimilaritys = new ArrayList<VSMSimilarity>();
		List<String> control = new ArrayList<String>();
		
		for (Resource r1 : uris) {
			for (Resource r2 : uris) {
				if (r1!=r2  && (!(control.contains(r2.getLocalName()+r1.getLocalName())||control.contains(r1.getLocalName()+r2.getLocalName())))) {
					List<Resource> propertyResources = 	SparqlWalk.getObjectPropertiesSharedByResources(r1.getURI(), r2.getURI());
					VSMSimilarity vsmSimilarity = new VSMSimilarity(r1, r2);
					for (Resource propertyResource : propertyResources) {
						
							vsmSimilarity.addPropertyScore(propertyResource.getURI(), Double.valueOf(VSM.simVSMbyProperty(r1.getURI(),r2.getURI(),propertyResource.getURI())));
							control.add(r1.getLocalName()+r2.getLocalName()+ propertyResource.getLocalName());
							control.add(r2.getLocalName()+r1.getLocalName()+ propertyResource.getLocalName());							
						
						vSMSimilaritys.add(vsmSimilarity);
					}
				}
				control.add(r1.getLocalName()+r2.getLocalName());
				control.add(r2.getLocalName()+r1.getLocalName());
				}
			}
		
		for (VSMSimilarity vsmSimilarity : vSMSimilaritys) {

			System.out.println("VSM:"+vsmSimilarity.getResource1().getLocalName()+ " X " + vsmSimilarity.getResource2().getLocalName()+ "  == "+String.format( "%.2f", (vsmSimilarity.totalScore())));
		}
		
		
		
		
		
	}
	
	
	public static double simVSMbyProperty(String uri1, String uri2, String property){
		
		//uri1 is a movie1   property is starring    uri2 a movie2
		
		double simVSM = 0;
		
		double sumUp = 0d;
		double down1 = 0d;
		double down2 = 0d;
		
		
		List<Resource> resources = SparqlWalk.getReachedResourcesDirectedLinkedFromResourceAndProperty(uri1, property);	
		List<Resource> resources2 = SparqlWalk.getReachedResourcesDirectedLinkedFromResourceAndProperty(uri2, property);
		Set<String> resoureceSet = new HashSet<String>();
		for (Resource r1 : resources) {
			resoureceSet.add(r1.getURI());
		}
		
		for (Resource r2 : resources2) {
			resoureceSet.add(r2.getURI());
		}
		
		for (String resourceSet : resoureceSet) {
			sumUp = sumUp + calculateW(uri1, property, resourceSet) * calculateW(uri2, property, resourceSet);
			down1 = down1 + Math.pow(calculateW(uri1, property, resourceSet),2);
			down2 = down2 + Math.pow(calculateW(uri2, property, resourceSet),2);
		}
		
			
		simVSM = sumUp/Math.sqrt(down2) * Math.sqrt(down1);
		
		return simVSM;
	}	
	
	
	
	/**
	 * @param uri1
	 * @param property
	 * @param uri2
	 * @return
	 */
	public static double calculateW(String uri1, String property, String uri2){
		
		//uri1 is a movie   property is starring    uri2 is the actor
		double TFIDF = calculateTF(uri1, property, uri2) * calculateIDF(uri1, property, uri2);
		
		return TFIDF;
	}	
	
	
	/**
	 * @param uri1
	 * @param property
	 * @param uri2
	 * @return
	 */
	public static double calculateIDF(String uri1, String property, String uri2){
		
		//uri1 is a movie   property is starring    uri2 is the actor
		
		double idf = 0d;

		//amount of films that an actor starred
		double numberOfResourcesInASetOfResources = SparqlWalk.getResourcesByPropertyAndObject(uri2, property).size();

		double totalAmountOfMovies = (double)SparqlWalk.getTotalResouresOfDomain(SparqlWalk.getWikidataResourceDomain(uri1).get(0).getURI());
		
		idf = Math.log(totalAmountOfMovies / numberOfResourcesInASetOfResources);

		return idf;
	}
	
	
	
	/**
	 * @param uri1
	 * @param property
	 * @param uri2
	 * @return
	 */
	public static double calculateTF(String uri1, String property, String uri2){
		
		//uri1 is a movie   property is starring    uri2 is the actor
		
		double tf = 0d;
		
		double numberOfResourcesInAResourceByProperty = SparqlWalk.getDirectLinkBetween2ResourcesAndProperty(uri1, property, uri2);
		
		double sumOfFrequencies = 0;
		
		if (numberOfResourcesInAResourceByProperty!=0) {
			List<Resource> resources = SparqlWalk.getReachedResourcesDirectedLinkedFromResourceAndProperty(uri1, property);
			for (Resource resource : resources) {
				sumOfFrequencies = sumOfFrequencies + SparqlWalk.getDirectLinkBetween2ResourcesAndProperty(uri1, property, resource.getURI());
			}
		}else{
			return 0d;
		}
		
		tf = numberOfResourcesInAResourceByProperty / sumOfFrequencies;
		
		return tf;
	}
	
	
	}
