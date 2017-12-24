package similarity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import node.IConstants;
import node.LodicaOldVersion;
import node.Node;
import node.SparqlWalk;
import util.StringUtilsNode;

public class NodeSim {

	static Resource resourceBRA = new ResourceImpl("http://dbpedia.org/resource/Brazil");
	static Resource resourceIRE = new ResourceImpl("http://dbpedia.org/resource/Ireland");
	static Resource resourceA = new ResourceImpl("http://dbpedia.org/resource/Ocean's_Twelve");
	static Resource resourceB = new ResourceImpl("http://dbpedia.org/resource/Ocean's_Thirteen");
	static Resource resourceC = new ResourceImpl("http://dbpedia.org/resource/Pixar");
	static Resource resourceD = new ResourceImpl("http://dbpedia.org/resource/111_Murray_Street");
	static Resource resourceE = new ResourceImpl("http://dbpedia.org/resource/The_Extendables");
	static Resource resourceF = new ResourceImpl("http://dbpedia.org/page/Brian_Thompson");
	static Resource resourceG = new ResourceImpl("http://dbpedia.org/resource/A_Patriot's_Act");
	static Resource resourceH = new ResourceImpl("http://dbpedia.org/resource/U2");
	static Resource resourceI = new ResourceImpl("http://dbpedia.org/resource/11th_Ward,_Chicago");
	
	static Resource p1 = new ResourceImpl("http://dbpedia.org/ontology/abstract");
	static Resource p2 = new ResourceImpl("http://www.w3.org/2000/01/rdf-schema#comment");
	

	static Resource[] resources = {resourceA,resourceB,resourceC,resourceD,resourceE,resourceF,resourceG,resourceH,resourceI};
	
	//static Resource[] resources = {resourceBRA,resourceIRE};
	//static Resource[] properties = {p1,p2};
	static Resource[] properties = null;//{p1,p2};
	
	public static void main(String[] args) {
		
		
		
	}
	
	
	
	
	
	
	public static double getNodeIntersec(Node node1, Node node2){
		
		Set<String> node1Keys = node1.getObservedAtrributes().keySet();
		Set<String> node2Keys = node2.getObservedAtrributes().keySet();
		double intersec = 0d;
		if (node1Keys.size()>=node2Keys.size()) {
			node1Keys.retainAll(node2Keys);
			intersec = node1Keys.size();
		}else{
			node2Keys.retainAll(node1Keys);
			intersec = node2Keys.size();
		}
		return intersec;
	}
	
	
	/**
	 * Removing relational to observed to easy calculation
	 * @param node1
	 * @param node2
	 */
	public static void removeRelationalPropertyValuesToObservedPropertyMapping(Node node1, Node node2){
	
		int init = node1.getObservedAtrributes().size();
		for (String relKey : node1.getRelationalFeatures().keySet()) {
			node1.getObservedAtrributes().remove(relKey,node1.getRelationalFeatures().get(relKey).toString());	
		}
		int end = node1.getObservedAtrributes().size();
		//System.out.println("before1 "+init+" after1 "+end);
		
		if (end>init) {
			new Exception("RELATIONAL PROPERTIES ERROR DURING REMOVAL!");
		}
		
		init = node2.getObservedAtrributes().size();
		for (String relKey : node2.getRelationalFeatures().keySet()) {
			node2.getObservedAtrributes().remove(relKey,node2.getRelationalFeatures().get(relKey).toString());	
		}
		end = node2.getObservedAtrributes().size();
		//System.out.println("before2 "+init+" after2 "+end);
		if (end>init) {
			new Exception("RELATIONAL PROPERTIES ERROR DURING REMOVAL!");
		}
	}
	
	
	public static void checkRelationalAttributes(Node node1, Node node2){
		for (String relKey : node1.getRelationalFeatures().keySet()) {
			if((node1.getRelationalFeatures().get(relKey)==0d)&&(node2.getRelationalFeatures().get(relKey)==0d)){
				node1.getRelationalFeatures().remove(relKey);
				node2.getRelationalFeatures().remove(relKey);
			}
		}
	}
	
	
	
	
	/**
	 * Adding relational to observed to easy calculation
	 * @param node1
	 * @param node2
	 */
	public static void addRelationalPropertyValuesToObservedPropertyMapping(Node node1, Node node2){
		//checkRelationalAttributes(node1, node2);
	
		int init = node1.getObservedAtrributes().size();
		for (String relKey : node1.getRelationalFeatures().keySet()) {
			node1.getObservedAtrributes().put(relKey,node1.getRelationalFeatures().get(relKey).toString());	
		}
		int end = node1.getObservedAtrributes().size();
		//System.out.println("before1 "+init+" after1 "+end);
		
		if (end<=init) {
			new Exception("RELATIONAL PROPERTIES ERROR!");
		}
		
		init = node2.getObservedAtrributes().size();
		for (String relKey : node2.getRelationalFeatures().keySet()) {
			node2.getObservedAtrributes().put(relKey,node2.getRelationalFeatures().get(relKey).toString());	
		}
		end = node2.getObservedAtrributes().size();
		//System.out.println("before2 "+init+" after2 "+end);
		if (end<=init) {
			new Exception("RELATIONAL PROPERTIES ERROR!");
		}
	}
	
	
	
	
	public static void printValues(Object value1, Object value2, String propertyKeyPopulated){
		System.out.println(propertyKeyPopulated);
		System.out.println("value1 " + value1);
		System.out.println("value2 " + value2);
	}
	

	
	/**
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static double nodeSimbyAllProperty(Node node1, Node node2){

		double nodeSim = 0;
		
		if (IConstants.USE_ICA) {
			addRelationalPropertyValuesToObservedPropertyMapping(node1,node2);
		}

		Set<String> propertyKeys = new HashSet<String>();
		propertyKeys.addAll(node1.getObservedAtrributes().keySet());
		propertyKeys.addAll(node2.getObservedAtrributes().keySet());


		double totalProperties = propertyKeys.size(); 
		double totalOfCommongProperties = 0d;
		
		for (String propertyKeyPopulated : propertyKeys) {
			Set<String> node1KeySet = node1.getObservedAtrributes().keySet();
			Set<String> node2KeySet = node2.getObservedAtrributes().keySet();
			if(node1KeySet.contains(propertyKeyPopulated)&& node2KeySet.contains(propertyKeyPopulated)){
				if (NumberUtils.isNumber(node1.getObservedAtrributes().get(propertyKeyPopulated)) && NumberUtils.isNumber(node2.getObservedAtrributes().get(propertyKeyPopulated))) {
					totalOfCommongProperties++;
					double value1 = Double.valueOf(node1.getObservedAtrributes().get(propertyKeyPopulated));
					double value2 = Double.valueOf(node2.getObservedAtrributes().get(propertyKeyPopulated));
					//printValues(value1, value2, propertyKeyPopulated);					
					//distance
					double distance = 1;
					
					if (node1.relationalFeatures==null) {
						distance = 1;
					}else{
						if(node1.relationalFeatures.keySet().contains(propertyKeyPopulated)&&node2.relationalFeatures.keySet().contains(propertyKeyPopulated)&&node1.relationalFeatures.get(propertyKeyPopulated)==0d&&node2.relationalFeatures.get(propertyKeyPopulated)==0d) {
							distance = 1;
						}else{
							distance = StringUtilsNode.getDifferenceAndReturnNormalized01(value1, value2);
						}
					}
					//similarity
					double sim = (1 - distance);
					//System.out.println("distance "+distance);
					//System.out.println("numeric similarity "+sim);
					nodeSim = nodeSim + (Math.pow(sim,2));
				}else {
					totalOfCommongProperties++;
					String value1 = node1.getObservedAtrributes().get(propertyKeyPopulated);
					String value2 = node2.getObservedAtrributes().get(propertyKeyPopulated);
					//printValues(value1, value2, propertyKeyPopulated);
					double sim = calculateTextSimilarity(value1, value2,propertyKeyPopulated);
					//System.out.println("text similarity "+sim);
					nodeSim = nodeSim + (Math.pow(sim,2));
				}

			}
		}
		
		nodeSim = Math.sqrt(nodeSim);
		
		
		if (IConstants.USE_ICA) {
			removeRelationalPropertyValuesToObservedPropertyMapping(node1,node2);
		}
		
		//System.out.println("nodeSim BEFORE penalization "+nodeSim);
		//System.out.println("totalOfCommongProperties "+totalOfCommongProperties);
		//System.out.println("totalProperties "+totalProperties);
		//double penaltyLocal = totalOfCommongProperties / totalProperties;
		//System.out.println("penaltyLocal "+penaltyLocal);


		nodeSim = calculatePenalty(node1, node2, nodeSim);

		if (Double.isNaN(nodeSim)) {
			nodeSim = 0d;
		}
		
		//System.out.println("nodeSim AFTER penalization "+nodeSim);

		return nodeSim;

	}






	private static double calculateTextSimilarity(String value1, String value2,String property) {
		double sim = TextSimilarity.computeConsineSimilarity(value1,value2);
/*		double sim2 = 0d;
		
		if (property.equals("abstract")) {
			sim2 = LuceneCosineSimilarity.getCosineSimilarity(value1,value2);
			
			System.out.println("property "+property);
			System.out.println("sim		"+sim);
			System.out.println("sim2	"+sim2);
		}*/
		
		

		
		return sim;
	}

	private static double calculatePenalty(Node node1, Node node2, double nodeSim) {
		//SAME DOMAIN
		if (node1.getDomain().equals(node2.getDomain())) {
			if (IConstants.PENALIZE_SIMILARITY_BY_AMOUNT_OF_ONTOLOGY_DATATYPE_PROPERTY) {
				if (IConstants.USE_ICA) {
					nodeSim = ( nodeSim / (node1.getRelationalFeatures().size()+Double.valueOf(LodicaOldVersion.domainOntologyDataTypePropertySize.get(node1.getDomain()))));
				}else{
					nodeSim = ( nodeSim / (Double.valueOf(LodicaOldVersion.domainOntologyDataTypePropertySize.get(node1.getDomain()))));
				}
			
			}else{
				if (IConstants.USE_ICA) {
					nodeSim = ( nodeSim / (node1.getRelationalFeatures().size()+Double.valueOf(LodicaOldVersion.domainDatasetPropertySize.get(node1.getDomain()).size())));
				}else{
					nodeSim = ( nodeSim / Double.valueOf(LodicaOldVersion.domainDatasetPropertySize.get(node1.getDomain()).size()));
				}				
			}
		//DISTINCT DOMAIN, ARE WE GOING TO PENALIZE IT?
		}else if (!node1.getDomain().equals(node2.getDomain())) {

			if (IConstants.USE_ICA) {
				nodeSim = ( nodeSim / (node1.getRelationalFeatures().size()+Double.valueOf(LodicaOldVersion.domainOntologyDataTypePropertySize.get(node1.getDomain()))));
			}else{
				nodeSim = ( nodeSim / (Double.valueOf(LodicaOldVersion.domainOntologyDataTypePropertySize.get(node1.getDomain()))));
			}
		}
		return nodeSim;
	}	
	
	
	public static double nodeSimSum(Node node1, Node node2){
		double nodeSimbyObservedProperty = nodeSimbyObservedProperty(node1,node2);
		//System.out.println("nodeSimbyObservedProperty "+nodeSimbyObservedProperty);
		if (IConstants.USE_ICA) {
			double nodeSimbyRelationalProperty = nodeSimbyRelationalProperty(node1,node2);
			//System.out.println("nodeSimbyRelatinalProperty "+nodeSimbyRelationalProperty);
			double sizeObserved = (double)node1.getObservedAtrributes().size(); 
			double sizeRelational = (double)node1.getRelationalFeatures().size();
			return ((nodeSimbyObservedProperty*sizeObserved)+(nodeSimbyRelationalProperty*sizeRelational)/(sizeObserved+sizeRelational));
		}else{
			return nodeSimbyObservedProperty;
		}
		
	}
	
	
	/**
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static double nodeSimbyObservedProperty(Node node1, Node node2){

		double nodeSim = 0;
	
		Set<String> propertyKeys = new HashSet<String>();
		propertyKeys.addAll(node1.getObservedAtrributes().keySet());
		propertyKeys.addAll(node2.getObservedAtrributes().keySet());

		double totalProperties = propertyKeys.size(); 
		double totalOfCommongProperties = 0d;
		
		for (String propertyKeyPopulated : propertyKeys) {
			Set<String> node1KeySet = node1.getObservedAtrributes().keySet();
			Set<String> node2KeySet = node2.getObservedAtrributes().keySet();
			if(node1KeySet.contains(propertyKeyPopulated)&& node2KeySet.contains(propertyKeyPopulated)){
				if (NumberUtils.isNumber(node1.getObservedAtrributes().get(propertyKeyPopulated)) && NumberUtils.isNumber(node2.getObservedAtrributes().get(propertyKeyPopulated))) {
					totalOfCommongProperties++;
					double value1 = Double.valueOf(node1.getObservedAtrributes().get(propertyKeyPopulated));
					double value2 = Double.valueOf(node2.getObservedAtrributes().get(propertyKeyPopulated));
					//printValues(value1, value2, propertyKeyPopulated);					
					//distance
					//double distance = StringUtilsNode.getDifferenceAndReturnNormalized01(value1, value2);
					double distance = Math.abs(value1-value2);;
					//similarity
					double sim = (1 - distance);
					//System.out.println("distance "+distance);
					//System.out.println("numeric similarity "+sim);
					nodeSim = nodeSim + (Math.pow(sim,2));
				}else {
					totalOfCommongProperties++;
					String value1 = node1.getObservedAtrributes().get(propertyKeyPopulated);
					String value2 = node2.getObservedAtrributes().get(propertyKeyPopulated);
					double sim = calculateTextSimilarity(value1, value2,propertyKeyPopulated);
					//System.out.println("text similarity "+sim);
					nodeSim = nodeSim + (Math.pow(sim,2));
				}

			}
		}
		
		nodeSim = Math.sqrt(nodeSim);
				
		//System.out.println("nodeSim BEFORE penalization "+nodeSim);
		//System.out.println("totalOfCommongProperties "+totalOfCommongProperties);
		//System.out.println("totalProperties "+totalProperties);
		//double penaltyLocal = totalOfCommongProperties / totalProperties;
		//System.out.println("penaltyLocal "+penaltyLocal);


		nodeSim = calculatePenalty(node1, node2, nodeSim);
		
		//System.out.println("nodeSim AFTER penalization "+nodeSim);

		return nodeSim;

	}
	
	/**
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static double nodeSimbyRelationalProperty(Node node1, Node node2){

		double nodeSim = 0;

		Set<String> propertyKeys = new HashSet<String>();
		propertyKeys.addAll(node1.getRelationalFeatures().keySet());
		propertyKeys.addAll(node2.getRelationalFeatures().keySet());


		double totalProperties = propertyKeys.size(); 
		double totalOfCommongProperties = 0d;
		
		for (String propertyKeyPopulated : propertyKeys) {
			Set<String> node1KeySet = node1.getRelationalFeatures().keySet();
			Set<String> node2KeySet = node2.getRelationalFeatures().keySet();
			if(node1KeySet.contains(propertyKeyPopulated)&& node2KeySet.contains(propertyKeyPopulated)){
				//if (NumberUtils.isNumber(node1.getRelationalFeatures().get(propertyKeyPopulated)) && NumberUtils.isNumber(node2.getRelationalFeatures().get(propertyKeyPopulated))) {
					totalOfCommongProperties++;
					double value1 = Double.valueOf(node1.getRelationalFeatures().get(propertyKeyPopulated));
					double value2 = Double.valueOf(node2.getRelationalFeatures().get(propertyKeyPopulated));
					//printValues(value1, value2, propertyKeyPopulated);					
					//distance

					
					double distance = 1;
					if (node1.relationalFeatures.get(propertyKeyPopulated)==0d&&node2.relationalFeatures.get(propertyKeyPopulated)==0d) {
						distance = 1;
					}else{
						distance = StringUtilsNode.getDifferenceAndReturnNormalized01(value1, value2);
					}					
					
					//similarity
					double sim = (1 - distance);
					//System.out.println("distance "+distance);
					//System.out.println("numeric similarity "+sim);
					nodeSim = nodeSim + (Math.pow(sim,2));
				//}
				
/*				else {
					totalOfCommongProperties++;
					String value1 = node1.getRelationalFeatures().get(propertyKeyPopulated);
					String value2 = node2.getRelationalFeatures().get(propertyKeyPopulated);
					//printValues(value1, value2, propertyKeyPopulated);
					double sim = TextSimilarity.computeConsineSimilarity(value1,value2);
					//System.out.println("text similarity "+sim);
					nodeSim = nodeSim + sim;
				}*/

					
				}

			}
		
		
		nodeSim = Math.sqrt(nodeSim);
		
		
		
		//System.out.println("nodeSim BEFORE penalization "+nodeSim);
		//System.out.println("totalOfCommongProperties "+totalOfCommongProperties);
		//System.out.println("totalProperties "+totalProperties);
		//double penaltyLocal = totalOfCommongProperties / totalProperties;
		//System.out.println("penaltyLocal "+penaltyLocal);


		nodeSim = calculatePenalty(node1, node2, nodeSim);
		
		//System.out.println("nodeSim AFTER penalization "+nodeSim);

		return nodeSim;

	}	
	
		
	private  static boolean isDecimal(Literal literal) {
	    RDFDatatype dtype = literal.getDatatype() ;
	    if ( dtype == null )
	        return false ;
	    if ( ( dtype.equals(XSDDatatype.XSDfloat) ) ||
	         ( dtype.equals(XSDDatatype.XSDdecimal) ) ||  
	         ( dtype.equals(XSDDatatype.XSDdouble) ) )
	        return true ;
	    return false ;
	}
	
	public static Object getJavaValue(final Literal literal) {
		final RDFDatatype dataType = literal.getDatatype();
		if (dataType == null) {
			return literal.toString();
		}
		return dataType.parse(literal.getLexicalForm());
	}
	 
	
	
	public static double cbSimbyProperty(String uri1, String uri2, String property){

		double cbSim = 0;

		List<Literal> literals = SparqlWalk.getLiteralByResourceAndProperty(uri1, property);	
		List<Literal> literals2 = SparqlWalk.getLiteralByResourceAndProperty(uri2, property);
		
		for (Literal literal : literals) {
			for (Literal literal2 : literals2) {
				if (literal==null || literal2 == null) {
					continue;
				}

				if (literal.getDatatype().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")) {
					cbSim = cbSim + TextSimilarity.computeConsineSimilarity(TextSimilarity.filterStoppingsKeepDuplicates((String)literal.getValue()), TextSimilarity.filterStoppingsKeepDuplicates((String)literal2.getValue()));
					continue;
				}
				
				System.out.println("UPPPPPPPPPSSSSSS   SOMETHING WRONG");
				
				if(literal.getDatatype().getURI().equals("http://www.w3.org/2001/XMLSchema#date" )){
					cbSim = cbSim + Math.abs((Long)literal.getValue() - (Long)literal2.getValue());
					continue;
				}				
				
				Object classs = literal.getDatatype().getJavaClass();
				
				if (classs!=null) {
					if (classs.equals(Float.class)) {
						cbSim = cbSim + Math.abs(literal.getFloat() - (long)literal2.getFloat());
						continue;
					}else if (classs.equals(Long.class)) {
						cbSim = cbSim + Math.abs(literal.getLong() - (long)literal2.getLong());
						continue;
					}else if (classs.equals(Integer.class)   || (classs.equals(BigInteger.class))) {
						cbSim = cbSim + Math.abs(literal.getInt() - literal2.getInt());
						continue;
					}else if (classs.equals(Short.class) ) {
						cbSim = cbSim + Math.abs(literal.getShort() - literal2.getShort());
						continue;
					}else if (classs.equals(Byte.class) ) {
						cbSim = cbSim + Math.abs(literal.getByte() - literal2.getByte());
						continue;
					}
				}
				
				//System.out.println(cbSim);
				
			}
		}
		
		return cbSim;
	}	
	
	
	public static List<VSMSimilarity> simVSMbyProperty(List<Node> nodes){
		
		List<VSMSimilarity> vSMSimilaritys = new ArrayList<VSMSimilarity>();
		
		List<String> control = new ArrayList<String>();

		for (Node r1 : nodes) {
			for (Node r2 : nodes) {
				if (r1!=r2  && (!(control.contains(r2.getURI()+r1.getURI())||control.contains(r1.getURI()+r2.getURI())))) {
					VSMSimilarity vsmSimilarity = new VSMSimilarity(new ResourceImpl(r1.getURI()), new ResourceImpl(r2.getURI()));
					vsmSimilarity.setSimScore(nodeSimbyAllProperty(r1,r2));
					vSMSimilaritys.add(vsmSimilarity);
				}
				control.add(r1.getURI()+r2.getURI());
				control.add(r2.getURI()+r1.getURI());
			}
		}
		
		for (VSMSimilarity vsmSimilarity : vSMSimilaritys) {
			System.out.println("VSM:"+vsmSimilarity.getResource1().getLocalName()+ " X " + vsmSimilarity.getResource2().getLocalName()+ " has score == "+String.format( "%.2f", (vsmSimilarity.getSimScore())));				
		}

		return vSMSimilaritys;
	}	
	
	
	
	
	}
