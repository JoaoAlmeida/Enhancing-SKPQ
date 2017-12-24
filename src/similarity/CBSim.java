package similarity;

import java.math.BigInteger;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import node.SparqlWalk;

public class CBSim {

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
	

	//static Resource[] resources = {resourceA,resourceB,resourceC,resourceD,resourceE,resourceF,resourceG,resourceH,resourceI};
	
	static Resource[] resources = {resourceBRA,resourceIRE};
	//static Resource[] properties = {p1,p2};
	static Resource[] properties = null;//{p1,p2};
	
	public static void main(String[] args) {
		
		
		//System.out.println(cosineSimilarity("fred is here","fred is here"));
		//System.out.println(cosineSimilarity("fred is paul john here","fred is here"));
		
		String str1 = "This is a sentence. It is made of words";
	    String str2 = "This sentence is similar. It has almost the same words";
	
		System.out.println(TextSimilarity.computeConsineSimilarity(str1,str2));
		
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
		
/*		int i = 0;
		
		List<Resource> ps = SparqlWalk.getDataTypePropertiesSharedByResources(resourceBRA.getURI(), resourceIRE.getURI());
		
		
		Resource[] pp = new Resource[ps.size()];
		
		for (Resource resource : ps) {
			pp[i] = resource;
			i++;
		}
		
		properties = pp;
		
		simVSMbyProperty(Arrays.asList(resources));*/
	
	}
	
	

		
	public static double simVSMbyProperty(String uri1, String uri2){
		List<Resource> sim = new ArrayList<Resource>();
		sim.add(new ResourceImpl(uri1));
		sim.add(new ResourceImpl(uri2));
		return simVSMbyProperty(sim).get(0).totalScore();
	}
	
	public static List<VSMSimilarity> simVSMbyProperty(List<Resource> uris){
		
		List<VSMSimilarity> vSMSimilaritys = new ArrayList<VSMSimilarity>();
		
		List<String> control = new ArrayList<String>();
		
		for (Resource r1 : uris) {
			for (Resource r2 : uris) {
				if (r1!=r2  && (!(control.contains(r2.getLocalName()+r1.getLocalName())||control.contains(r1.getLocalName()+r2.getLocalName())))) {
					//List<Resource> propertyResources = 	SparqlWalk.getDataTypePropertiesSharedByResources(r1.getURI(), r2.getURI());
					List<Resource> propertyResources = Arrays.asList(properties);
					VSMSimilarity vsmSimilarity = new VSMSimilarity(r1, r2);
					for (Resource propertyResource : propertyResources) {
						//System.out.println(propertyResource.getURI());
						vsmSimilarity.addPropertyScore(propertyResource.getURI(), Double.valueOf(CBSim.cbSimbyAllProperty(r1.getURI(),r2.getURI(),propertyResource.getURI())));
					}
					vSMSimilaritys.add(vsmSimilarity);
				}
				control.add(r1.getLocalName()+r2.getLocalName());
				control.add(r2.getLocalName()+r1.getLocalName());
			}
		}
		
		for (VSMSimilarity vsmSimilarity : vSMSimilaritys) {
			for (String keyProperty : vsmSimilarity.getPropertyScore().keySet()) {
					System.out.println("VSM:"+vsmSimilarity.getResource1().getLocalName()+ " X " + vsmSimilarity.getResource2().getLocalName()+ " for property " + new ResourceImpl(keyProperty).getLocalName()+ " has score " + vsmSimilarity.getPropertyScore().get(keyProperty)+ " and total score == "+String.format( "%.2f", (vsmSimilarity.totalScore())));				
			}
			
		}

		return vSMSimilaritys;
	}
	
	
	public static double cbSimbyAllProperty(String uri1, String uri2, String property){

		double cbSim = 0;

		List<Literal> literals = SparqlWalk.getLiteralByResourceAndProperty(uri1, property);	
		List<Literal> literals2 = SparqlWalk.getLiteralByResourceAndProperty(uri2, property);
		
		
		System.out.println("COMPARING "+ property);
		
		
		for (Literal literal : literals) {
			for (Literal literal2 : literals2) {
				if (literal==null || literal2 == null) {
					continue;
				}
				
				
					System.out.println(literal);
					
					if (literal.getDatatype().getURI().equals("http://www.openlinksw.com/schemas/virtrdf#Geometry")) {
						//cbSim = cbSim + SparqlWalk.getDistanceBetweenTwoPoints(literal,literal2);
					}
					
					if (literal.getDatatype().getURI().equals("http://www.w3.org/2001/XMLSchema#nonNegativeInteger")) {

						cbSim = cbSim + Math.abs(literal.getInt() - literal2.getInt());
						continue;
					}
					
					
					

				if (literal.getDatatype().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")) {
					cbSim = cbSim + TextSimilarity.computeConsineSimilarity(TextSimilarity.filterStoppingsKeepDuplicates((String)literal.getValue()), TextSimilarity.filterStoppingsKeepDuplicates((String)literal2.getValue()));
					continue;
				}
				

				
				if(literal.getDatatype().getURI().equals("http://www.w3.org/2001/XMLSchema#date" )){
					System.out.println(                       "entrou........");
					cbSim = cbSim + Math.abs((Long)literal.getValue() - (Long)literal2.getValue());
					
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
					
					
				//} else if (StringUtils.isNumeric(((Literal)literal).getString())) {
				} else if (true) {
					System.out.println(isDecimal(literal));
					//System.out.println(literal.getValue().getClass());

					//cbSim = cbSim + Math.abs(literal.getDouble() - literal2.getDouble());
				}
				
				//System.out.println(cbSim);
				
			}
		}
		
		return cbSim;
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
	
	public static List<String> getWords(String text) {
	    List<String> words = new ArrayList<String>();
	    BreakIterator breakIterator = BreakIterator.getWordInstance();
	    breakIterator.setText(text);
	    int lastIndex = breakIterator.first();
	    while (BreakIterator.DONE != lastIndex) {
	        int firstIndex = lastIndex;
	        lastIndex = breakIterator.next();
	        if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(text.charAt(firstIndex))) {
	            words.add(text.substring(firstIndex, lastIndex));
	        }
	    }

	    return words;
	}
	
	
	public static Map<String, Double> getTF(String s) {
		Map<String, Double> m=new HashMap<String, Double>();
	    String[] splitedString=s.split(" ");
	    
	    int count=1;
	    for(String s1 :splitedString){
	         count=m.containsKey(s1)?count+1:1;
	          m.put(s1, (double)count);
	        }
	    return m;
	   }

	
	
	
	public static double cosineSimilarity(String one, String two) {
/*		Map<String,Double> tf = new HashMap<String, Double>();
		List<String> words1 = getWords(one);
		List<String> words2 = getWords(two);
		Set<String> terms = new HashSet<String>(words1);
		terms.addAll(words2);
		
		
*/		return cosineSimilarity2(new ArrayList(getTF(one).values()), new ArrayList(getTF(two).values()));
	
		
		
		
		
		
		
	}
	public static double cosineSimilarity2(List<Double> vector1, List<Double> vector2) {

		    double dotProduct = 0.0;
		    double normA = 0.0;
		    double normB = 0.0;
		    for (int i = 0; i < vector1.size(); i++) {
		      dotProduct += vector1.get(i) * vector2.get(i);
		      normA += Math.pow(vector1.get(i), 2);
		      normB += Math.pow(vector2.get(i), 2);
		    }
		    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}	
	
	
	
	
	}
