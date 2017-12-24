/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;

import util.StringUtilsNode;



public class SparqlWalk {

	public static boolean USING_GRAPH = false;
	
	static Model model = getTestModel();

	public static Model getTestModel() {
		Model model = ModelFactory.createDefaultModel();
		return model;
	}

	public static Model getTestModel2() {
		Model model = ModelFactory.createDefaultModel();
		if (USING_GRAPH) {
			System.out.println("WE ARE ON THE GRAPH !!! ");
			return model.read("values1.ttl");
			//return model.read("C:/Users/fdurao/Desktop/here/dbpedia.nt");
			//return model.read("C:/Users/fdurao/Desktop/here/article_categories_en.ttl/article_categories_en.ttl");
			//return model.read("C:/Users/fdurao/Desktop/here/dbpedia_2012_02_27.nt");
		}else{
			System.out.println("WE ARE ON THE WEB !!! ");
			return model;
		}
	}
	
	
	
	
	static public void main(String... argv) {
		
		System.out.println(findFeatures(null));
		
		//System.out.println(model);
		Set<String> ss = new HashSet<String>();
		ss.add("http://dbpedia.org/resource/Andrew_Stanton");
		ss.add("http://dbpedia.org/resource/Brazil");
		ss.add("http://dbpedia.org/resource/Spanish_language");
		
		Set<Node> ssNodes = new HashSet<Node>();
		Node node = new Node(""+3,IConstants.NO_LABEL,"http://dbpedia.org/resource/Category:English-language_films");
		ssNodes.add(node);
		
		//System.out.println(getDirectLinksBetween2ResourcesInBothWaysForNodes("http://dbpedia.org/resource/Category:English-language_films",ss));
		System.out.println(getDirectLinksBetween2ResourcesInBothWaysForNodes("http://dbpedia.org/resource/Category:English-language_films", ssNodes));
		//System.out.println(getDBpediaObjecstBySubject("http://dbpedia.org/resource/Category:English-language_films"));
		
		
		
		
		//System.out.println(getDbPediaSubjects("http://dbpedia.org/resource/Mike_Posner"));
		
		//System.out.println(countDirectLinksBetween2Resources("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		System.out.println(getIndirectDistinctInconmingLinksBetween2Resources("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		System.out.println(getIndirectDistinctOutgoingLinksBetween2Resources("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		System.out.println(countTotalNumberOfIndirectInconmingLinksBetween2Resources("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		System.out.println(countTotalNumberOfIndirectOutgoingLinksBetween2Resources("http://learningsparql.com/ns/expenses#r1","http://learningsparql.com/ns/expenses#r2"));
		//System.out.println(getDbpediaResourcesByDomain("http://dbpedia.org/ontology/Actor",3).size());
		
		//System.out.println(getDbpediaDomains(null).size());
		
		
		
		//System.out.println(getDBpediaObjecstBySubjectIncluingInverseRelation("http://dbpedia.org/resource/Brazil").size());
		
		//SparqlWalk.queryInstancesFromDbpediaOntologyClass("http://dbpedia.org/resource/Chengdu");
		
		//System.out.println(countDirectLinksBetween2Resources("http://dbpedia.org/resource/Finding_Nemo","http://dbpedia.org/resource/Andrew_Stanton"));
		// System.out.println(countDirectLinksBetween2Resources("http://dbpedia.org/resource/Andrew_Stanton","http://dbpedia.org/resource/Finding_Nemo"));

		String resourcez = "http://dbpedia.org/resource/Brazil";
		String resourceS = "http://dbpedia.org/resource/Republic_of_Ireland";
		String property1 = "http://dbpedia.org/property/country";

	}
	
	
	public static List<Resource> findFeatures(List<Resource> interestSet) {

		List<Resource> featureSet = new ArrayList<>();

		String serviceURI = "http://dbpedia.org/sparql";
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";

		for (int a = 0; a < 1; a++) {

			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
					+ "SELECT DISTINCT ?resource WHERE { <http://dbpedia.org/resource/Cadogan_Hotel> geo:geometry ?sourcegeo." + " ?resource "
							+ "geo:geometry ?location ;" + "rdfs:label ?label ."
					+ "FILTER( <http://www.openlinksw.com/schema/sparql/extensions#bif:st_intersects(?location,?sourcegeo,20)> ) ." + "FILTER( lang( ?label ) =" + quotes
					+ "en" + quotes + ")} " + Sparql.addServiceClosing(USING_GRAPH);

			Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));
			
			NodeUtil.print(query.toString());

			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

				Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
				Map<String, List<String>> params = new HashMap<String, List<String>>();
				List<String> values = new ArrayList<String>();
				values.add("20000");
				params.put("timeout", values);
				serviceParams.put(serviceURI, params);
				qexec.getContext().set(ARQ.serviceParams, serviceParams);
				try {
					ResultSet rs = qexec.execSelect();
					for (; rs.hasNext();) {
						QuerySolution rb = rs.nextSolution();
						RDFNode x = rb.get("resource");
						if (x.isResource()) {
							featureSet.add((Resource) x);
						}
					}
				} finally {
					qexec.close();
				}
			}
		}
		return featureSet;
	}	
	

	// JUSTIFICATION
	// http://dbpedia.org/snorql/?query=%0D%0A%0D%0ASELECT+%3Fr1+%3Fp1+%3Fr2+%3Fp2+%3Fr3++%3Frange_p1+%3Frange_p2++%3Fdomain_p1+%3Fdomain_p2++WHERE+%7B+%0D%0A%0D%0Avalues+%28%3Fr1+%3Fr3%29+%7B++%28+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FFinding_Dory%3E++%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FWillem_Dafoe%3E%29%7D+%0D%0A%0D%0A.+%3Fr1+%3Fp1+%3Fr2+%0D%0A.+%3Fr2+%3Fp2+%3Fr3%0D%0A.+%3Fp1+rdfs%3Arange+%3Frange_p1%0D%0A.+%3Fp2+rdfs%3Arange+%3Frange_p2%0D%0A.+%3Fp1+rdfs%3Adomain+%3Fdomain_p1%0D%0A.+%3Fp2+rdfs%3Adomain+%3Fdomain_p2%0D%0A%0D%0A%0D%0A%0D%0A%7D+

	// Select the TYPE and number of instances that use this type
	//http://dbpedia.org/snorql/?query=SELECT+%3Ftype+%28COUNT%28%3Fmember%29+as+%3FmemberCount%29+WHERE+%7B%0D%0A++++%3Fmember+rdf%3Atype+%3Ftype.%0D%0A+++%0D%0A+++%7B+SELECT+%3Ftype+WHERE+%7B%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FOcean%27s_Twelve%3E+rdf%3Atype+%3Ftype.+%7D+%7D%0D%0A%7D%0D%0AORDER+BY+%3FmemberCount	
	
	// Select same type - WE HAVE A PROBLEM HERE.
	//http://dbpedia.org/snorql/?query=SELECT+distinct+*+WHERE+%7B%0D%0A%0D%0A+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FU2%3E+rdf%3Atype+%3Ftype+.+%0D%0A%0D%0A+FILTER+not+exists+%7B++%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FMuse_%28band%29%3E+rdf%3Atype+%3Ftype+.%7D%0D%0A%0D%0A%0D%0A+%0D%0A%7D			

	public static int getOntologyDomains() {

		List<Resource> resources = new ArrayList<Resource>();
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";		

		String queryString = "SELECT count(?i) AS ?num ?c where { ?i a ?c. FILTER(regex(?c,''^http//:dbpedia.org/ontology'')) . } order by desc (?num)";
		
		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources.size();
		}
	}
	
	
	

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static double getDistanceBetweenTwoPoints(Literal coordinate1, Literal coordinate2) {

		
		
		List<Double> distances = new ArrayList<Double>();  
		
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";		


//				String queryString = "
				//+ ""
				//+ "SELECT * wHERE { "
			    //+ " SERVICE <"+ serviceURI+ "> { "
				//+ " SELECT  ( <bif:st_distance> (?r1, ?r2)  AS ?m ) "
				//+ "  WHERE { values (?r1 ?r2 ) {( "+coordinate1+" "+coordinate2+" )} "
				//+ "}}} ";	
		
		System.out.println(coordinate1.getString());
		
		String c1 = StringUtils.replace(StringUtils.remove(coordinate1.getString(), "POINT")," ",",");
		String c2 = StringUtils.replace(StringUtils.remove(coordinate2.getString(), "POINT")," ",",");
		
		String queryString = "SELECT (bif:st_distance(bif:st_point"+c1+",bif:st_point"+c2+") as ?dist) where {}";			
		
		System.out.println(queryString);
		
		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));
		
		

		try (QueryExecution qexec = QueryExecutionFactory.create(Sparql.addPrefix()
				.concat(queryString),
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("200000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("dist");
					
					System.out.println(x.asLiteral().toString());
					//distances.add(x.asLiteral().getFloat());
				}
			} finally {
				qexec.close();
			}
			
			if (distances.size()>0) {
				Double td = 0d;
				for (Double d : distances) {
					td = td + d;
				}
				return td/distances.size();
			
			} else {
				System.out.println("SOME THING WENT WRONG");
				return Double.MAX_VALUE;
			}
		}

	}				

	

	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static double getDistanceBetweenTwoPlaces(String placeURI1,String placeRUI2) {

		List<Double> distances = new ArrayList<Double>();  
		
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";		


		String queryString = ""
				+ ""
				+ "SELECT * WHERE { "
			    + " SERVICE <"+ serviceURI+ "> { "
				+ " SELECT  ( <bif:st_distance> (?lr1, ?lr2)  AS ?m ) "
				+ "  WHERE { values (?r1 ?r2 ) {( <"+placeURI1+"> <"+placeRUI2+"> )} "
				+ " ?r1  <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?lr1 . "
				+ " ?r2  <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?lr2 . "
				+ "}}} ";
		
		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));

		
		try (QueryExecution qexec = QueryExecutionFactory.create(queryString,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("200000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("m");
					distances.add(x.asLiteral().getDouble());
				}
			} finally {
				qexec.close();
			}
			
			if (distances.size()>0) {
				Double td = 0d;
				for (Double d : distances) {
					td = td + d;
				}
				return td/distances.size();
			
			} else {
				System.out.println("SOME THING WENT WRONG");
				return Double.MAX_VALUE;
			}
		}

	}
	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static Resource getMostSpecificSubclasseOfResource(String resourceURI) {
		
		List<Resource> resources = new ArrayList<Resource>();
		
		String serviceURI = "http://dbpedia.org/sparql";		

		String queryString = ""
				+ Sparql.addService(USING_GRAPH,serviceURI)
				+ "SELECT * WHERE { <"+resourceURI+"> a ?c1 ; a ?c2 . ?c1 rdfs:subClassOf ?c2  } "
				+ Sparql.addServiceClosing(USING_GRAPH);


		//System.out.println(queryString);
		
		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("c1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}							

				}
			} finally {
				qexec.close();
			}
			int least = resources.size();
			if (resources.isEmpty()) {
				resources.add(new ResourceImpl("https://www.w3.org/2002/07/owl#Thing"));
				least = resources.size();
			}
			
			
			return resources.get(least-1);

		}

	}	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static Resource getMostSpecificSubclasseOfDbpediaResource(String resourceURI) {
		
		List<Resource> resources = new ArrayList<Resource>();
		
		String serviceURI = "http://dbpedia.org/sparql";		

		String queryString = ""
				+ Sparql.addService(USING_GRAPH,serviceURI)
				+ "SELECT * WHERE { <"+resourceURI+"> a ?c1 ; a ?c2 . ?c1 rdfs:subClassOf ?c2   ."
				+ "FILTER(STRSTARTS(STR(?c2), "+quotes+"http://dbpedia.org/ontology"+quotes+")) }"
				+ Sparql.addServiceClosing(USING_GRAPH);


		//System.out.println(queryString);
		
		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("c1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}							

				}
			} finally {
				qexec.close();
			}
			int least = resources.size();
			if (resources.isEmpty()) {
				resources.add(getMostSpecificSubclasseOfResource(resourceURI));
				least = resources.size();
			}
			return resources.get(least-1);

		}

	}			
	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int getTotalResouresOfDomain(String classDomain) {
		
		int totalResouresOfDomain = 0;
		
		if (!classDomain.startsWith("http://dbpedia.org/ontology/")) {
			new Exception("Ontology is not given");
		}


		
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";		

String queryString = ""
		+ Sparql.addService(USING_GRAPH,serviceURI)
				+ " SELECT (count(distinct ?r1) as ?x)  where { ?r1 a <"+classDomain+"> }  "
				+ "} }";
		
		
		
		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					totalResouresOfDomain = (int)x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return totalResouresOfDomain;

		}

	}	
	
	
		
		/**
		 * @param uri1
		 * @param uri2
		 * @return
		 */
		public static List<Resource> getDbpediaOntologyObjectProperties(String ontologyURI) {

			List<Resource> resources = new ArrayList<Resource>();
			//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
			String serviceURI = "http://dbpedia.org/sparql";		

String queryString = ""
		+ Sparql.addService(USING_GRAPH,serviceURI)
					+ " select distinct ?property  "
					+ " WHERE { ?property <http://www.w3.org/2000/01/rdf-schema#domain> <"+ontologyURI+"> ."
							+ " ?property <http://www.w3.org/2000/01/rdf-schema#range> ?r . ?r rdf:type <http://www.w3.org/2002/07/owl#Class>  "
					+ "}}} ";
			
			//System.out.println(queryString);

			Query query = QueryFactory.create(Sparql.addPrefix()
					.concat(queryString));
			
			

			try (QueryExecution qexec = QueryExecutionFactory.create(query,
					model)) {
				// createDefaultModel(serviceURI, query, qexec);

				Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
				Map<String, List<String>> params = new HashMap<String, List<String>>();
				List<String> values = new ArrayList<String>();
				values.add("20000");
				params.put("timeout", values);
				serviceParams.put(serviceURI, params);
				qexec.getContext().set(ARQ.serviceParams, serviceParams);
				try {
					ResultSet rs = qexec.execSelect();
					for (; rs.hasNext();) {
						QuerySolution rb = rs.nextSolution();
						RDFNode x = rb.get("property");
						if (x.isResource()) {
							resources.add((Resource) x);
						}
					}
				} finally {
					qexec.close();
				}
				return resources;

			}

		}
		
	/**
	 * @param resources
	 * @return
	 */
	public static List<Resource> getDbpediaOntologyDatatypeProperties(List<Resource> resources) {
		List<Resource> propertyResources = new ArrayList<Resource>();
		for (Resource resource : resources) {
			propertyResources.addAll(getDbpediaOntologyDatatypeProperties(resource.getURI()));
		}
		return propertyResources;
	}
	
	/**
	 * @param resources
	 * @return
	 */
	public static List<Resource> getDbpediaOntologyObjectProperties(List<Resource> resources) {
		List<Resource> propertyResources = new ArrayList<Resource>();
		for (Resource resource : resources) {
			propertyResources.addAll(getDbpediaOntologyObjectProperties(resource.getURI()));
		}
		return propertyResources;
	}			
	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getDbpediaOntologyDatatypeProperties(String ontologyURI) {

		List<Resource> resources = new ArrayList<Resource>();
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";		

		String queryString = ""
		+ Sparql.addService(USING_GRAPH,serviceURI)
				+ " select distinct ?property  "
				+ " WHERE { ?property <http://www.w3.org/2000/01/rdf-schema#domain> <"+ontologyURI+"> ."
						+ " ?property <http://www.w3.org/2000/01/rdf-schema#range> ?r . ?r rdf:type rdfs:Datatype  "
				+ "}}} ";
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));
		
		

		try (QueryExecution qexec = QueryExecutionFactory.create(query,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("property");
					if (x.isResource()) {
						
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}			
	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getOntologyProperties(String ontologyURI) {

		List<Resource> resources = new ArrayList<Resource>();
		//String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";		

		String queryString = ""
		+ Sparql.addService(USING_GRAPH,serviceURI)
				+ " select distinct ?property  "
				+ " WHERE { ?property <http://www.w3.org/2000/01/rdf-schema#domain> <"+ontologyURI+"> . "
				+ "}"
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix()
				.concat(queryString));
		
		

		try (QueryExecution qexec = QueryExecutionFactory.create(query,
				model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("property");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int getResoureType(String resource) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + " SELECT (count (distinct ?r2) as ?x)"
				+ "  WHERE { values (?r1 ) {( <" + resource + "> )} " + ". ?r1 rdf:type ?r2 ." + "}}} ";
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources.size();

		}

	}

	/**
	 * @param resources
	 * @return
	 */
	public Resource getTopClass(List<Resource> resources) {

		for (Resource resource1 : resources) {
			for (Resource resource2 : resources) {
				if (resource1.getLocalName() != resource2.getLocalName()) {
					if (isSubClassOf(resource1.getLocalName(), resource2.getLocalName())) {

					}
				}
			}
		}

		return null;
	}

	/**
	 * @param classNameOne
	 * @param classNameTwo
	 * @return
	 */
	public boolean isSubClassOf(String classNameOne, String classNameTwo) {

		boolean isSubClassOf = false;

		String class1 = "http://dbpedia.org/ontology/".concat(classNameOne);
		String class2 = "http://dbpedia.org/ontology/".concat(classNameTwo);

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + " SELECT count(*) as ?x"
				+ " WHERE { values (?class1) (?class2) {( <" + class1 + ">  <" + class2 + ">  )} "
				+ " . ?class1  rdfs:subClassOf ?class2 ." + "}}} ";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					if (((Integer) (x.asLiteral().getValue())).toString().equals("1")) {
						isSubClassOf = true;
					}
				}
			} finally {
				qexec.close();
			}
		}

		return isSubClassOf;
	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getClassesByResource(String resource) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + " SELECT distinct ?class "
				+ " WHERE { values (?r1) {( <" + resource + ">   )} " + " . ?r1 rdf:type ?class ."
				+ " FILTER(STRSTARTS(STR(?class), " + quotes + "http://dbpedia.org/ontology" + quotes + ")) " + "}}} ";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("class");
					if (x.isResource()) {
						// System.out.println(x.asResource().getURI());
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int getDirectLinkBetween2ResourcesAndProperty(String uri1, String property, String uri2) {

		int total = 0;
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + " SELECT (count (distinct ?p1) as ?x)"
				+ "  WHERE { values (?r1 ?p1 ?r2) {( <" + uri1 + "> <" + property + "> <" + uri2 + ">  )} "
				+ ". ?r1 ?p1 ?r2 ." + "}}} ";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					total = x.asLiteral().getInt();
				}
			} finally {
				qexec.close();
			}
			return total;

		}

	}

	/**
	 * @param ini
	 * @param end
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static String createRelationalQuery(int ini, int end, String uri1, String uri2) {
		String q = "select ";

		StringBuilder stringBuilder = new StringBuilder(q);

		for (int i = ini; i < end; i++) {
			stringBuilder.append("?r" + i + " ?p" + i + " ");
		}
		stringBuilder.append("?r" + end + " ");

		stringBuilder.append(" where { values (?r1 ?r" + (end) + ") { (<" + uri1 + ">	<" + uri2 + ">  )} .");

		for (int i = ini; i < end; i++) {
			stringBuilder.append("?r" + i);
			stringBuilder.append(" ?p" + i);
			stringBuilder.append(" ?r" + (i + 1));
			stringBuilder.append(" . ");
		}

		stringBuilder.append("}");

		//System.out.println(stringBuilder.toString());

		return stringBuilder.toString();

	}

	public static int findPathsBetween2Resources(String uri1, String uri2, int limit) {
		List<Resource> resources = new ArrayList<Resource>();
		int max = limit;
		int totalPath = 0;
		for (int i = 1; i < max; i++) {
			resources = findPathsBetween2Resources(1, i + 1, uri1, uri2);
			if (resources.size() > 0) {
				totalPath = i;
				break;
			}
			resources = findPathsBetween2Resources(1, i + 1, uri2, uri1);
			if (resources.size() > 0) {
				totalPath = i;
				break;
			}

		}

		return totalPath;
	}

	/**
	 * generic
	 * 
	 * @param ini
	 * @param end
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> findPathsBetween2Resources(int ini, int end, String uri1, String uri2) {
		String serviceURI = "http://dbpedia.org/sparql";

		String relationalQuery = createRelationalQuery(ini, end, uri1, uri2);

		String queryString = "SELECT DISTINCT * WHERE { " + "    SERVICE <" + serviceURI + "> { " + relationalQuery + "}}";

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("200000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					for (Iterator<String> iter = rb.varNames(); iter.hasNext();) {
						String name = iter.next();
						RDFNode x = rb.get(name);
						if (x.isResource()) {
							resources.add((Resource) x);
						}
					}

				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	/**
	 * Same range getIndirectOutgoingLinkingFromResource
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 *//*
	public static List<Resource> getIndirectOutgoingLinkingFromResource(String uri1) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) +
				" SELECT distinct ?p1 ?p2 WHERE { "
				+ " values (?r1 ) { ( <" + uri1 + "> )} " 
				+ " ?r2 ?p1 ?r1 . " 
				+ "?r2 ?p2 ?r3 . "
				+ Sparql.addFilter(null, null, "r2") + "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("p1");
					if (x.isResource()) {
						//System.out.println(x.asResource().getLocalName());
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}*/

	/**
	 * Same range getIndirectOutgoingLinkingBetween2Resources
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getIndirectDistinctOutgoingLinkingBetween2Resources(String uri1, String uri3) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT * WHERE { "
				+ "values (?r1 ?r3) { ( <" + uri1 + "> <" + uri3 + "> )} " + "?r2 ?p1 ?r1 . " + "?r2 ?p2 ?r3 . " + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					/*
					 * System.out.println(rb.get("r1"));
					 * System.out.println(rb.get("p1"));
					 * System.out.println(rb.get("r2"));
					 * System.out.println(rb.get("p2"));
					 * System.out.println(rb.get("r3"));
					 */
					if (x.isResource()) {
						// System.out.println(x.asResource().getLocalName());
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}

	/**
	 * Same domain getIndirectInconmingLinkingFromResource
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getIndirectDistinctInconmingLinkingBetween2Resources(String uri1, String uri3) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "SELECT DISTINCT * WHERE { " + "SERVICE <" + serviceURI + "> { "
				+ "SELECT DISTINCT * WHERE { " + "values (?r1 ?r3) { ( <" + uri1 + "> <" + uri3 + "> )} "
				+ "?r1 ?p1 ?r2 . " + "?r3 ?p1 ?r2 . " + " }}}";

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					/*
					 * System.out.println(rb.get("r1"));
					 * System.out.println(rb.get("p1"));
					 * System.out.println(rb.get("r2"));
					 * System.out.println(rb.get("p2"));
					 * System.out.println(rb.get("r3"));
					 */
					if (x.isResource()) {
						// System.out.println(x.asResource().getLocalName());
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}

	/**
	 * Same domain getIndirectInconmingLinkingFromResource
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getIndirectInconmingLinkingFromResource(String uri1) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT * WHERE { "
				+ "values (?r1 ) { ( <" + uri1 + "> )} " + "?r1 ?p1 ?r2 . " + "?r3 ?p1 ?r2 .  }}}";

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("p1");
					/*
					 * System.out.println(rb.get("r1"));
					 * System.out.println(rb.get("p1"));
					 * System.out.println(rb.get("r2"));
					 * System.out.println(rb.get("p2"));
					 * System.out.println(rb.get("r3"));
					 */
					if (x.isResource()) {
						// System.out.println(x.asResource().getLocalName());
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}

	/**
	 * Same domain countIndirectInconmingLinksBetween2Resources
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getIndirectDistinctOutgoingLinksBetween2Resources(String uri1, String uri3) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT DISTINCT ?p1 WHERE { "
				+ "values (?r1 ?r3) { ( <" + uri1.trim() + ">  <" + uri3.trim() + ">  )} " 
				+ "?r2 ?p1 ?r1 . " 
				+ "?r2 ?p1 ?r3 . "
				//+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("p1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}
	
	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static List<Resource> getIndirectDistinctIncomingAndOutgoingLinksBetweenResourceAndListOfResources(String uri1, Set<String> uris) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT DISTINCT ?r3 WHERE { "
				+ Sparql.getStringQueryValuesUnionIncomingAndOutgoing(uri1, uris)
				//+ Sparql.addFilter(null, null, "r2") 
				+ " }" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r3");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}	
	
	
	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static List<Resource> getIndirectDistinctOutgoingLinksBetweenResourceAndListOfResources(String uri1, Set<String> uris) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT DISTINCT ?r3 WHERE { "
				+ Sparql.getStringQueryValuesUnionIndirectOutgoing(uri1, uris)
				//+ Sparql.addFilter(null, null, "r2") 
				+ " }" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r3");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}
	

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static List<Resource> getIndirectDistinctInconmingLinksBetweenResourceAndListOfResources(String uri1, Set<String> uris) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT DISTINCT ?r3 WHERE { "
				+ Sparql.getStringQueryValuesUnionIndirectIncoming(uri1, uris)
				//+ Sparql.addFilter(null, null, "r2") 
				+ " }" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r3");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}	

	/**
	 * DONE countIndirectInconmingLinksBetween2Resources
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getIndirectDistinctInconmingLinksBetween2Resources(String uri1, String uri3) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT DISTINCT ?p1 WHERE { "
				+ "values (?r1 ?r3) {( <" + uri1.trim() + ">  <" + uri3.trim() + ">  )} " 
				+ " ?r1 ?p1 ?r2 ." 
				+ " ?r3 ?p1 ?r2 ."
				//+ Sparql.addFilter(null, null, "r2") 
				+ " }" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("p1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}
	}
	
	
	/**
	 * Same domain countIndirectInconmingLinksBetween2Resources
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int countTotalNumberOfIndirectInconmingLinksBetween3Resources(String uri1, String uri2, String uri3) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI)
				+ "SELECT (count (*) as ?x) WHERE { " 
				+ " values (?r1 ?r2 ?r3) { ( <" + uri1.trim() + "> <" + uri2.trim() + "> <" + uri3.trim() + ">  )} "
				+ " ?r1 ?p1 ?r2 ." 
				+ " ?r3 ?p1 ?r2 . " 
				+ Sparql.addFilter(null, null, "r2") + "}"

				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}
	

	/**
	 * Same domain countIndirectInconmingLinksBetween2Resources
	 * 
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int countTotalNumberOfIndirectInconmingLinksBetween2Resources(String uri1, String uri3) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI)
				+ "SELECT (count (*) as ?x) WHERE { " 
				+ " values (?r1 ?r3) { ( <" + uri1 + ">  <" + uri3 + ">  )} "
				+ " ?r1 ?p1 ?r2 ." 
				+ " ?r3 ?p1 ?r2 . " 
				+ Sparql.addFilter(null, null, "r2") + "}"

				+ Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}

	/**
	 * Same range
	 * 
	 * @param uri1
	 * @return
	 */
	public static int countResourcesReachedByIndirectIncomingLinks(String uri1) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
				+ "SELECT (count (distinct ?r3) as ?x) WHERE { " + "values (?r1 ) { ( <" + uri1 + ">  )} "
				+ "?r1 ?p1 ?r2 ." + "?r3 ?p2 ?r2 ." + Sparql.addFilter(null, null, "r2") + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;
		}

	}

	/**
	 * @param uri1
	 * @return
	 */
	public static int countResourcesReachedByIndirectOutgoingLinks(String uri1) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
				+ "SELECT (count (distinct ?r3) as ?x) WHERE { "
				// + "SELECT distinct * WHERE { "
				+ "values (?r1 ) { ( <" + uri1 + ">  )} " + "?r2 ?p1 ?r1 ." + "?r2 ?p2 ?r3 ."
				+ Sparql.addFilter(null, null, "r2") + "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;
		}
	}
	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int countTotalNumberOfIndirectOutgoingLinksBetween3Resources(String uri1, String uri2, String uri3) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT (count (*) as ?x) WHERE { "
				+ "values (?r1 ?r2 ?r3) { ( <"+ uri1.trim() + ">  <"+ uri2.trim() + "> <" + uri3.trim() + ">  )} " 
				+ "?r2 ?p1 ?r1 . " 
				+ "?r2 ?p1 ?r3 . "
				+ Sparql.addFilter(null, null, "r2") 
				+ "}" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}	
	

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int countTotalNumberOfIndirectOutgoingLinksBetween2Resources(String uri1, String uri3) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT (count (*) as ?x) WHERE { "
				+ "values (?r1 ?r3) { ( <"+ uri1 + ">  <" + uri3 + ">  )} " 
				+ "?r2 ?p1 ?r1 . " 
				+ "?r2 ?p1 ?r3 . "
				+ Sparql.addFilter(null, null, "r2") 
				+ "}" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getDirectLinksBetween2Resources(String uri1, String uri2) {

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT distinct ?p1 WHERE {values(?r1 ?r2){(<"+uri1.trim()+"> <"+uri2.trim()+">)} " 
				+ ". ?r1 ?p1 ?r2 . " 
				+ "FILTER (?r1 != ?r2). } " 
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("?p1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	public static List<Resource> getDirectLinksBetween2ResourcesInBothWays(String uri1, String uri2) {
		Set<String> ss = new HashSet<String>();
		ss.add(uri2);
		return getDirectLinksBetween2ResourcesInBothWays(uri1, ss);
	}
	
	public static List<Resource> getDirectLinksBetween2ResourcesInBothWaysForNodes(String uri1, String uri2) {
		Set<String> ss = new HashSet<String>();
		ss.add(uri2);
		return getDirectLinksBetween2ResourcesInBothWays(uri1, ss);
	}	
	
	public static List<Resource> getIndirectDistinctIncomingAndOutgoingLinksBetweenResourceAndListOfResources(String uri1, String uri2) {
		Set<String> ss = new HashSet<String>();
		ss.add(uri2);
		return getIndirectDistinctIncomingAndOutgoingLinksBetweenResourceAndListOfResources(uri1, ss);
	}
	
	
	
	/**
	 * @param uri1
	 * @param uri2
	 * @return fred
	 */
	public static List<Resource> getDirectLinksBetween2ResourcesInBothWaysForNodes(String uri1, Set<Node> nodes) {

		String queryString = "";
		try {
			if (nodes == null || nodes.isEmpty()) {
				return new ArrayList<Resource>();
			}
			// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
			String serviceURI = "http://dbpedia.org/sparql";

			queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + " SELECT distinct ?r2  WHERE {"
					+ Sparql.getStringQueryValuesUnionDirectBothWaysForNodes(uri1, nodes) + "}"
					// + " FILTER (?r1 != ?r2)}"
					+ Sparql.addServiceClosing(USING_GRAPH);

			// System.out.println(queryString);

			Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
				List<Resource> resources = new ArrayList<Resource>();
				// createDefaultModel(serviceURI, query, qexec);
				Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
				Map<String, List<String>> params = new HashMap<String, List<String>>();
				List<String> values = new ArrayList<String>();
				values.add("20000");
				params.put("timeout", values);
				serviceParams.put(serviceURI, params);
				qexec.getContext().set(ARQ.serviceParams, serviceParams);
				try {
					ResultSet rs = qexec.execSelect();
					for (; rs.hasNext();) {
						QuerySolution rb = rs.nextSolution();
						RDFNode x = rb.get("r2");
						if (x.isResource() && x.isURIResource()) {
							resources.add(x.asResource());
						}
					}
				} catch (QueryExceptionHTTP ex) {
					NodeUtil.print(queryString);
					NodeUtil.print(ex.getMessage());
					return resources;
					/*
					 * if (ex.getResponseCode() == HttpStatus.SC_BAD_REQUEST) {
					 * 
					 * } if (ex.getResponseCode() == HttpStatus.SC_BAD_GATEWAY)
					 * {
					 * 
					 * }
					 */

				} catch (QueryParseException ex) {
					NodeUtil.print(queryString);
					NodeUtil.print(ex.getMessage());
					return resources;
				} finally {
					qexec.close();
				}
				return resources;

			}

		} catch (Exception e) {
			NodeUtil.print(queryString);
			NodeUtil.print(e.getMessage());
			return new ArrayList<Resource>();

		}

	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getDirectLinksBetween2ResourcesInBothWays(String uri1, Set<String> uris) {
		if (uris == null || uris.isEmpty()) {
			return new ArrayList<Resource>();
		}
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + " SELECT distinct ?r2  WHERE {"
				+ Sparql.getStringQueryValuesUnionDirectBothWays(uri1, uris) + "}"
				// + " FILTER (?r1 != ?r2)}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			List<Resource> resources = new ArrayList<Resource>();
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource() && x.isURIResource()) {
						resources.add(x.asResource());
					}
				}
			} catch (QueryExceptionHTTP ex) {
				// NodeUtil.print(ex.getMessage());
				if (ex.getResponseCode() == HttpStatus.SC_BAD_REQUEST) {
					return resources;
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	
			 
		 /**
			 * @param uri1
			 * @param uri2
			 * @return
			 */
		public static int countDirectLinksBetween2ResourcesInBothWays(String uri1, String uri2) {

			int totalDirectLinksBetween2Resources = 0;
			// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
			String serviceURI = "http://dbpedia.org/sparql";

			String queryString = "" + 
					Sparql.addService(USING_GRAPH, serviceURI) 
					+ " SELECT (count (distinct ?p1) as ?x)"
					+ "  WHERE { "
					+ "  { <" + uri1.trim() + "> ?p1  <" + uri2.trim() + "> } " 
					+ "  UNION "
					+ "  { <" + uri2.trim() + "> ?p1  <" + uri1.trim() + "> } "
					+ "}"						
					//+ " FILTER (?r1 != ?r2)}"
					
					+ Sparql.addServiceClosing(USING_GRAPH);


				//System.out.println(queryString);

				Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

				try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
					// createDefaultModel(serviceURI, query, qexec);
					Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
					Map<String, List<String>> params = new HashMap<String, List<String>>();
					List<String> values = new ArrayList<String>();
					values.add("20000");
					params.put("timeout", values);
					serviceParams.put(serviceURI, params);
					qexec.getContext().set(ARQ.serviceParams, serviceParams);
					try {
						ResultSet rs = qexec.execSelect();
						for (; rs.hasNext();) {
							QuerySolution rb = rs.nextSolution();

							RDFNode x = rb.get("x");
							totalDirectLinksBetween2Resources = (int) x.asLiteral().getValue();
						}
					} finally {
						qexec.close();
					}
					return totalDirectLinksBetween2Resources;

				}

			}		 
	
	

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static int countDirectLinksBetween2Resources(String uri1, String uri2) {

		int totalDirectLinksBetween2Resources = 0;
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + 
				Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT (count (distinct ?p1) as ?x)"
				+ "  WHERE { values (?r1 ?r2) {( <" + uri1.trim() + ">  <" + uri2.trim() + "> )} " 
				+ ". ?r1 ?p1 ?r2 ."
				+ " FILTER (?r1 != ?r2)}" 
				+ Sparql.addServiceClosing(USING_GRAPH);


		//System.out.println(queryString);
		

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					totalDirectLinksBetween2Resources = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return totalDirectLinksBetween2Resources;

		}

	}

	/**
	 * @param uri1
	 * @return
	 */
	public static List<Resource> getDbPediaSubjects(String uri1) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT distinct ?r2 "
				+ "WHERE { values (?r1) { ( <" + uri1 + ">   )} . " 
				+ "?r1 <http://purl.org/dc/terms/subject> ?r2 . "
				+ " FILTER(STRSTARTS(STR(?r2), " 
				+ quotes + "http://dbpedia.org/resource/Category" + quotes + ")) . "
				+ "}" + Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);
		
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	public static char quotes = '"';

	/**
	 * @param uri1
	 * @return
	 */
	public static List<Resource> getYagoTypes(String uri1) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + uri1 + ">   )} . " + "?r1 rdf:type ?r2 . "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes + "http://dbpedia.org/class/yago/" + quotes + ")) . " + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}
	
	
	/**
	 * @param uri1
	 * @return
	 */
	public static List<SimpleTriple> searchDbpediaResourcesTriples(String userQuery) {

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)

				+ "SELECT DISTINCT *   "
				// + "SELECT ?r2 (SAMPLE(?p1) AS ?pp1) "
				// + "SELECT *"
				+ "WHERE {  "

				+ " ?r1 ?p1 ?r2 . "

				+ " ?r2 rdfs:label ?label . "

				+ " FILTER langMatches( lang(?label), " + quotes + "en" + quotes + ") . "

				+ " FILTER regex(str(?label), " + quotes + userQuery + quotes + ")    . "

				+ " FILTER(STRSTARTS(STR(?r2), " + quotes + "http://dbpedia.org/resource/" + quotes + ")) . "

				+ "} LIMIT 20  } }";

		// + "} GROUP BY ?r2 }}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode r1 = rb.get("r1");
					RDFNode r2 = rb.get("r2");
					RDFNode p = rb.get("p1");
					if (r1 != null && r2 != null && r2.isResource() && p != null && p.isResource()) {
						SimpleTriple simpleTriple = new SimpleTriple(r1.asResource().getURI(), p.asResource().getURI(),
								r2.asResource().getURI());
						triples.add(simpleTriple);
					}
				}
			} finally {
				qexec.close();
			}
			return triples;
		}

	}	
	
	

	/**
	 * @param uri1
	 * @return
	 */
	public static List<SimpleTriple> searchDbpediaResourcesTriplesByDomain(String domainUri,Integer limit) {

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r1 "
				+ " WHERE { ?r1 a  <" + domainUri + "> " 
				+ "} "
				+  Sparql.checkLimitNull(limit)
				+ Sparql.addServiceClosing(USING_GRAPH);

		// + "} GROUP BY ?r2 }}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode r1 = rb.get("r1");
					SimpleTriple simpleTriple = new SimpleTriple(domainUri, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",r1.asResource().getURI());
					triples.add(simpleTriple);
					
				}
			} finally {
				qexec.close();
			}
			return triples;
		}

	}

	/**
	 * @param uri1
	 * @return
	 */
	public static List<Resource> searchDbpediaResources(String userQuery) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "SELECT DISTINCT ?r1 WHERE { " + "    SERVICE <" + serviceURI + "> { "
				+ "SELECT distinct ?r1 "
				// + "SELECT *"
				+ "WHERE {  " + " ?r1 rdfs:label ?label . " + " FILTER langMatches( lang(?label), " + quotes + "en"
				+ quotes + ") . " + " FILTER regex( str(?label), " + quotes + userQuery + quotes + ") . "

				+ "} LIMIT 20}}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}
	
	
	
	/**
	 * @param uri1
	 * @return
	 */
	public static List<Resource> getDbpediaDomains(Integer limit) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
	
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r1 {"
				+ "    ?r1 a owl:Class ." 
				+ " FILTER(!isLiteral(?r1)). "	
				+ " FILTER(STRSTARTS(STR(?r1), " + quotes + "http://dbpedia.org/ontology" + quotes + ")) . " 				
				+ "} "
				+  Sparql.checkLimitNull(limit)
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);
		
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}	
	
	/**
	 * @param uri1
	 * @return
	 */
	public static List<Resource> getDbpediaResourcesByDomain(String uri1, Integer limit) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
	
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r1 "
				+ " WHERE { ?r1 a  <" + uri1 + "> " 
				+ "} "
				+  Sparql.checkLimitNull(limit)
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);
		
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}	
	

	/**
	 * @param uri1
	 * @return
	 */
	public static List<Resource> getDbPediaTypes(String uri1) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
				+ " WHERE { values (?r1) { ( <" + uri1 + ">   )} . " 
				+ " ?r1 rdf:type ?r2 . "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes + "http://dbpedia.org/ontology" + quotes + ")) . " 
				+ "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	public static List<Resource> getResourceDomainByWikidata(String uri1) {

		String uri1Final = getWikidataResourceDomain(uri1).get(0).getURI();

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + uri1Final + ">   )} . "
				+ "?r1 <http://www.w3.org/2002/07/owl#equivalentClass> ?r2 . " + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
						//System.out.println(x.asResource().getLocalName());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	public static List<Resource> getWikidataResourceDomain(String uri1) {

		String wikidataPropertyBase = "http://www.wikidata.org/entity/";

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT ?r3 ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + uri1 + ">   )} . "
				+ "?r1 rdf:type ?r2 .   ?r2 owl:equivalentClass  ?r3 . "

				+ "}" + Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					RDFNode x2 = rb.get("r3");
					if (x.isResource()) {
						if (((Resource) x2).getURI().contains(wikidataPropertyBase)) {
							resources.add((Resource) x);
						}

					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	public static List<?> getObjectsBySubjectBySameAsProperty(String subject) {

		List<Object> resources = new ArrayList<Object>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subject + "> )} . " + "?r1 owl:sameAs ?r2 .  " + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					} else if (x.isLiteral()) {
						resources.add((Literal) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	public static List<?> getObjectsBySubjectAndProperty(String subject, String property) {

		List<Object> resources = new ArrayList<Object>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1 ?p1) { ( <" + subject + "> <" + property + ">   )} . " + "?r1 ?p1 ?r2 .  " + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					} else if (x.isLiteral()) {
						resources.add((Literal) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	public static String filterTextProperty(boolean filterTextProperty) {
		if (filterTextProperty) {
			return "   FILTER(?p1 != <http://dbpedia.org/ontology/abstract>) ."
					+ " FILTER(?p1 != <http://www.w3.org/2000/01/rdf-schema#comment>) .";
		} else {
			return " ";
		}

	}

	/**
	 * @param subjectURI
	 * @return
	 */
	public static List<SimpleTriple> getDBpediaFinestLiteralsTripletBySubjectURI(String subjectURI) {

		// tested with
		// http://wiki.dbpedia.org/services-resources/datasets/dataset-statistics

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT ?r2 ?p1 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subjectURI + ">   )} . " + "?r1 ?p1 ?r2 .  "
				// + " FILTER(STRSTARTS(STR(?r2),
				// "+quotes+"http://dbpedia.org/resource/"+quotes+")) . "
				+ " FILTER(isLiteral(?r2)). " + filterTextProperty(IConstants.REMOVE_COMMON_FEATURES)
				+ " FILTER(?p1 != <http://www.w3.org/2000/01/rdf-schema#label>) ."
				+ " FILTER(?p1 != <http://www.openlinksw.com/schemas/virtrdf#Geometry>) ."
				+ " FILTER(?p1 != <http://www.w3.org/2003/01/geo/wgs84_pos#geometry>) ."
				+ " FILTER(?p1 != <http://www.georss.org/georss/point>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/ontology/alias>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/filename>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/description>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/homepage>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/nickname>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/traffic>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/open>) ."
				+ " FILTER(?p1 != <http://xmlns.com/foaf/0.1/nick>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/ontology/unitedStatesNationalBridgeId>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/ontology/flag>) ."
				+ " FILTER(?p1 != <http://xmlns.com/foaf/0.1/name>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/imageFlag>) ."
				+ " FILTER(?p1 != <http://dbpedia.org/property/name>) ."
				// + " FILTER (!REGEX(?p1,
				// "+quotes+"date"+quotes+","+quotes+"i"+quotes+")) ."
				+ Sparql.addFilter(null, "p1", null) + Sparql.addLangFilter(null, null, "r2")

				+ "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					RDFNode p = rb.get("p1");
					if (x.isLiteral() && p.isResource() && !x.asLiteral().getValue().toString().contains("*")) {

						/*
						 * System.out.println(p.asResource().getURI());
						 * System.out.println(x.asLiteral().getDatatypeURI());
						 * System.out.println(x.asLiteral().getLexicalForm());
						 */

						String literalValue = (isScientificNotation(x.asLiteral().getLexicalForm().toString()))
								? new BigDecimal(x.asLiteral().getLexicalForm()).toPlainString()
								: x.asLiteral().getLexicalForm();
						// literalValue = StringUtils.remove(literalValue,"\"");
						// System.out.println(literalValue);
						// System.out.println(StringUtils.isNumeric(literalValue));

						Long dateMilliseconds = DateUtil.convertDateToLong(literalValue);
						if (dateMilliseconds != null) {
							literalValue = dateMilliseconds.toString();
						} else if (!StringUtils.isNumeric(literalValue)) {
							literalValue = StringUtilsNode.removeInvalidCharacteres(literalValue);
						}

						// System.out.println(literalValue);
						if (!literalValue.isEmpty()) {
							SimpleTriple simpleTriple = new SimpleTriple(subjectURI, p.asResource().getLocalName(),
									literalValue);
							triples.add(simpleTriple);
						}

					}
				}
			} finally {
				qexec.close();
			}
			return triples;
		}

	}

	public static boolean isScientificNotation(String numberString) {

		// Validate number
		try {
			new BigDecimal(numberString);
		} catch (NumberFormatException e) {
			return false;
		}

		// Check for scientific notation
		return numberString.toUpperCase().contains("E");
	}

	/**
	 * @param subjectURI
	 * @return
	 */
	public static List<SimpleTriple> getDBpediaLiteralsTripletBySubjectURI(String subjectURI) {

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT ?r2 ?p1 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subjectURI + ">   )} . " + "?r1 ?p1 ?r2 .  "
				// + " FILTER(STRSTARTS(STR(?r2),
				// "+quotes+"http://dbpedia.org/resource/"+quotes+")) . "
				+ " FILTER(isLiteral(?r2)). " + "}" + Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					RDFNode p = rb.get("p1");
					if (x.isLiteral() && p.isResource()) {
						SimpleTriple simpleTriple = new SimpleTriple(subjectURI, p.asResource().getURI(),
								x.asLiteral().getValue().toString());
						triples.add(simpleTriple);
					}
				}
			} finally {
				qexec.close();
			}
			return triples;
		}

	}

	/**
	 * @param subjectURI
	 * @return
	 */
	public static List<SimpleTriple> getDBpediaObjecsTripletBySubjectURI(String subjectURI) {

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT ?r2 ?p1 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subjectURI + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes + "http://dbpedia.org/resource/" + quotes + ")) . "
				+ Sparql.addFilter(null, null, "r2")

				+ "}" + Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					RDFNode p = rb.get("p1");
					if (x.isResource() && p.isResource()) {
						SimpleTriple simpleTriple = new SimpleTriple(subjectURI, p.asResource().getURI(),
								x.asResource().getURI());
						triples.add(simpleTriple);
					}
				}
			} finally {
				qexec.close();
			}
			return triples;
		}

	}
	
	public static List<Resource> getDBpediaObjecstBySubjectByDomain(String subjectURI) {

		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r2 WHERE { values (?r1){(<"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				+ Sparql.addFilter(null, null, "r2")
				+ Sparql.addFilterDomain(null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}	
	
	
	
public static List<Resource> getDBpediaSubjectsByObjects(String subjectURI,Set<String> uris) {
		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r1 WHERE "
				+ " { values (?r2){( <"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				
				//+ " FILTER(STR(?p1) != "+ quotes+ "http://purl.org/dc/terms/subject"+ quotes+ "). "
				+ Sparql.addFilterURIs("r2",uris) + "."
				+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}		
	
	public static List<Resource> getDBpediaSubjectsByObjects(String subjectURI) {
		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r1 WHERE "
				+ " { values (?r2){( <"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				
				//+ " FILTER(!STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/Category" + quotes + ")) . "				
				
				//+ " FILTER(STR(?p1) != "+ quotes+ "http://purl.org/dc/terms/subject"+ quotes+ "). "
				
				+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()&& x.isURIResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}
	
	
	/**
	 * @param uri
	 * @return fred
	 */
	public static List<Resource> getDBpediaSubjectsAndObjectsBothWays(String uri) {
		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

/*		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI)
				+ "  SELECT distinct ?r1 WHERE { "				
				+ "  {values (?r2){( <"+uri.trim()+">)}  ?r1 ?p1 ?r2 .  FILTER(STRSTARTS(STR(?r1),"+ quotes+ "http://dbpedia.org/resource/" + quotes + "))} "  
				+ "  UNION"
				+ "  {values (?r2){( <"+uri.trim()+">)}  ?r2 ?p1 ?r1 .  FILTER(STRSTARTS(STR(?r1),"+ quotes+ "http://dbpedia.org/resource/" + quotes + "))} " 
				+ "  } "
				+ Sparql.addServiceClosing(USING_GRAPH);*/
		
		String queryString = "" 
		+ Sparql.addService(USING_GRAPH, serviceURI)				
		+ "  SELECT  distinct ?r1 WHERE { "
		+ "  		  { <"+uri.trim()+"> ?property ?r1 .  FILTER(STRSTARTS(STR(?r1),"+ quotes+ "http://dbpedia.org/resource/" + quotes + "))} "
		+ "  		  UNION "
		+ "  		  { ?r1 ?property <"+uri.trim()+"> .  FILTER(STRSTARTS(STR(?r1),"+ quotes+ "http://dbpedia.org/resource/" + quotes + "))} "
		+ "  		}	"
		+  Sparql.addServiceClosing(USING_GRAPH);	
		
		
		
	//System.out.println(queryString);
		

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()&& x.isURIResource()) {
						//if (!x.asResource().getURI().contains("\"")) {
							resources.add(x.asResource());	
						//}
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}	
	
	
	public static List<Resource> getResourceSubjectsByObjects(String subjectURI) {
		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r1 WHERE "
				+ " { values (?r2){( <"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				//+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				
				//+ " FILTER(!STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/Category" + quotes + ")) . "				
				
				//+ " FILTER(STR(?p1) != "+ quotes+ "http://purl.org/dc/terms/subject"+ quotes+ "). "
				
				+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()&& x.isURIResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}	

	
	
	public static List<Resource> getDBpediaObjecstBySubject(String subjectURI,Set<String> uris) {

		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r2 WHERE { values (?r1){(<"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				+ Sparql.addFilterURIs("r2",uris) + "."
				+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}
	
	public static List<Resource> getResourceObjecstBySubject(String subjectURI) {

		List<Resource> resources = new LinkedList<Resource>();

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r2 WHERE { values (?r1){(<"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				
				//+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				
				//+ " FILTER(!STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/Category" + quotes + ")) . "
				
				//+ " FILTER(STR(?p1) != "+ quotes+ "http://purl.org/dc/terms/subject"+ quotes+ "). "
				
				+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource() && x.isURIResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			
			return resources;
		}

	}	
	
	public static List<Resource> getDBpediaObjecstBySubject(String subjectURI) {

		List<Resource> resources = new LinkedList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT distinct ?r2 WHERE { values (?r1){(<"+subjectURI.trim()+">)} . ?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				
				//+ " FILTER(!STRSTARTS(STR(?r2),"+ quotes+ "http://dbpedia.org/resource/Category" + quotes + ")) . "
				
				//+ " FILTER(STR(?p1) != "+ quotes+ "http://purl.org/dc/terms/subject"+ quotes+ "). "
				
				+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource() && x.isURIResource()) {
						resources.add(x.asResource());
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}
	
	public static int getCountDBpediaObjecstBySubject(String subjectURI) {

		int total = 0;
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT (count (distinct ?r2) as ?x) "
		// 			+ "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subjectURI + ">   )} . " 
				+ "?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				+ Sparql.addFilter(null, null, "r2") + "}" 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					if (x.isLiteral()) {
						total = x.asLiteral().getInt();
					}
				}
			} finally {
				qexec.close();
			}
			return total;
		}

	}
	
	
	public static int getCountDBpediaObjecstBySubjectBetween2Resources(String subjectURI, String objectURI) {

		int total = 0;
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT (count (distinct ?r2) as ?x) "
		// 			+ "SELECT *"
				+ "WHERE { values (?r1 ?r2) { ( <" + subjectURI + "> <" + objectURI + ">   )} . " 
				+ "?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				+ Sparql.addFilter(null, null, "r2") + "}" 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					if (x.isLiteral()) {
						total = x.asLiteral().getInt();
					}
				}
			} finally {
				qexec.close();
			}
			return total;
		}

	}	
	
	
	
	/**
	 * @param subjectURI
	 * @return
	 */
	public static List<Resource> getDBpediaObjecstBySubjectIncluingInverseRelation(String subjectURI) {

		Set<Resource> resources = new HashSet<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT distinct ?r2 ?r3 "
		// 			+ "SELECT *"
				+ " WHERE { values (?r1) { ( <" + subjectURI + ">   )} . " 
				+ " ?r1 ?p1 ?r2 .  "
				+ " ?r3 ?p2 ?r1 .  "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				+ " FILTER(STRSTARTS(STR(?r3), " + quotes+ "http://dbpedia.org/resource/" + quotes + ")) . "
				+ Sparql.addFilter(null, null, "r2")
				+ Sparql.addFilter(null, null, "r3") + "}" 
				+ Sparql.addServiceClosing(USING_GRAPH);
		
		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		Model m = model;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add(x.asResource());
					}
					x = rb.get("r3");
					if (x.isResource()) {
						resources.add(x.asResource());
					}					
				}
			} finally {
				qexec.close();
			}
			return new ArrayList<Resource>(resources);
		}

	}	

	/**
	 * @param subjectURI
	 * @return
	 */
	public static List<SimpleTriple> getDbpediaObjecstTriplesBySubjectURI(String subjectURI) {

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "SELECT *  WHERE { " + "    SERVICE <" + serviceURI + "> { "
				+ "SELECT  ?r2 (SAMPLE(?p1) AS ?pp1)  "
				// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subjectURI + "> )} . " + "?r1 ?p1 ?r2 .  "
				+ " FILTER(STRSTARTS(STR(?r2), " + quotes + "http://dbpedia.org/resource/" + quotes + ")) . "

				+ Sparql.addFilter(null, null, "r2") + "} GROUP BY ?r2 }}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					RDFNode p = rb.get("pp1");
					if (x != null && x.isResource() && p != null && p.isResource()) {
						SimpleTriple simpleTriple = new SimpleTriple(subjectURI, p.asResource().getURI(),
								x.asResource().getURI());
						triples.add(simpleTriple);
					}
				}
			} finally {
				qexec.close();
			}

			return triples;
		}

	}

	public static List<SimpleTriple> getObjecstTriplesBySubjectURI(String subjectURI) {

		List<SimpleTriple> triples = new ArrayList<SimpleTriple>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 ?p1 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subjectURI + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ Sparql.addFilter(null, null, "r2") + "}" + Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					RDFNode p = rb.get("p1");
					if (x.isResource() && p != null && p.isResource()) {
						SimpleTriple simpleTriple = new SimpleTriple(subjectURI, p.asResource().getURI(),
								x.asResource().getURI());
						triples.add(simpleTriple);
					}
				}
			} finally {
				qexec.close();
			}

			return triples;
		}

	}

	public static List<Resource> getObjecstBySubject(String subject) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1) { ( <" + subject + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ Sparql.addFilter(null, null, "r2") + "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}

			return resources;
		}

	}

	public static List<Resource> getReachedResourcesDirectedLinkedFromResourceAndProperty(String uri1,
			String property) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1 ?p1) { ( <" + uri1 + "> <" + property + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ Sparql.addFilter(null, null, "r2") + "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}		

	/**
	 * @param uri1
	 * @return
	 */
	public static int countTotalDirectLinksFromResourceAndProperty(String uri1, String property) {

		int totalDirectLinksBetween2Resources = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ " SELECT (count (distinct ?p1) as ?x) "
				+ "WHERE { values (?r1 ?p1) { ( <" + uri1.trim() + "> <" + property.trim() + ">   )} . " 
				+ "?r1 ?p1 ?r2 .  "
				+ "FILTER (?r1 != ?r2)." 
				+ Sparql.addFilter(null, null, "r2") 
				+ "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					totalDirectLinksBetween2Resources = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return totalDirectLinksBetween2Resources;

		}

	}		

	/**
	 *
	 * @param uri1
	 * @return
	 */
	public static List<Resource> getResourcesByPropertyAndObject(String object, String property) {

		List<Resource> resources = new ArrayList<Resource>();

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r1  "
		// + "SELECT *"
				+ "WHERE { values (?r2 ?p1) { ( <" + object + "> <" + property + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ Sparql.addFilter(null, null, "r1") + "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getDataTypePropertiesSharedByResources(String uri1, String uri2) {

		Set<Resource> resources = new HashSet<Resource>();

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?p1 "
		// + "SELECT *"
				+ "WHERE { values (?r1 ?r2) { ( <" + uri1 + "> <" + uri2 + ">   )} . " + "?r1 ?p1 ?x .  "
				+ "?r2 ?p1 ?y .  " + "FILTER(isLiteral(?x)) .	" + "FILTER(isLiteral(?y)) .  "
				+ Sparql.addFilter(null, "p1", null) + "}" + Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("p1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return new ArrayList<Resource>(resources);
		}

	}

	/**
	 * @param uri1
	 * @param uri2
	 * @return
	 */
	public static List<Resource> getObjectPropertiesSharedByResources(String uri1, String uri2) {

		List<Resource> resources = new ArrayList<Resource>();

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?p1 "
		// + "SELECT *"
				+ "WHERE { values (?r1 ?r2) { ( <" + uri1 + "> <" + uri2 + ">   )} . " + "?r1 ?p1 ?x .  "
				+ "?r2 ?p1 ?y .  " + Sparql.addFilter(null, null, "x") + Sparql.addFilter(null, null, "y") + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("p1");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;
		}

	}

	/**
	 * @param uri1
	 * @param property
	 * @return
	 */
	public static List<Literal> getLiteralByResourceAndProperty(String uri1, String property) {

		List<Literal> literals = new ArrayList<Literal>();

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?r2 "
		// + "SELECT *"
				+ "WHERE { values (?r1 ?p1) { ( <" + uri1 + "> <" + property + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ "FILTER(isLiteral(?r2)) .	" + Sparql.addLangFilter(null, null, "r2") + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("r2");
					if (x.isLiteral()) {
						literals.add((Literal) x);
					}
				}
			} finally {
				qexec.close();
			}
			return literals;
		}
	}

	/**
	 * @param uri1
	 * @param property
	 * @return
	 */
	public static int countReachedResourcesDirectedLinkedFromResourceAndProperty(String uri1, String property) {

		int totalDirectLinksBetween2Resources = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT (count (distinct ?r2) as ?x) "
		// + "SELECT *"
				+ "WHERE { values (?r1 ?p1) { ( <" + uri1 + "> <" + property + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ "FILTER (?r1 != ?r2) .  " + Sparql.addFilter(null, null, "r2") + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					totalDirectLinksBetween2Resources = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return totalDirectLinksBetween2Resources;

		}

	}

	/**
	 * @param uri1
	 * @return
	 */
	public static int countReachedResourcedDirectedLinkedFromResource(String uri1) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)

				+ "SELECT (count (distinct ?r2) as ?x) "
				// + "SELECT * "
				+ "WHERE { values (?r1) { ( <" + uri1 + ">   )} . " + "?r1 ?p1 ?r2 .  "
				+ Sparql.addFilter(null, null, "r2") + " }}}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("200000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}

	}

	/**
	 * @param uri
	 * @return
	 */
	public static List<Resource> queryResourceDistance(String uri) {
		String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";

		String queryString = "SELECT DISTINCT * WHERE { " + "    SERVICE <" + serviceURI + "> { "
				+ "        SELECT  ?property ?object WHERE { <" + uri + "> ?property ?object . "
				+ Sparql.addFilter(null, "property", "object") + " } " + " LIMIT 1000000" + "    }" + "}";

		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("property");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	public static List<String> loadOntologies() {
		List<String> list = new ArrayList<String>();
		list.add("http://dbpedia.org/ontology/");
		list.add("http://dbpedia.org/class/");
		list.add("http://schema.org/");
		list.add("http://dbpedia.org/class/yago/");
		list.add("http://www.wikidata.org/entity/");
		list.add("http://umbel.org/umbel/rc/");
		list.add("http://schema.org/");
		list.add("http://schema.org/");
		return list;
	}

	public static boolean hasInList(List<String> list, String uri) {
		for (String str : list) {
			if (uri.contains(str)) {
				return true;
			}
		}
		return false;
	}

	static public void read() {
		System.out.println("Enter your resource uri: ");
		Scanner sc = new Scanner(System.in);
		String uri = sc.next();

		if (hasInList(loadOntologies(), uri)) {
			queryInstancesFromDbpediaOntologyClass(uri);
			read();
		} else if (uri.contains("http://dbpedia.org/resource/")) {
			queryResourceInformation(uri);
			read();
		} else {
			System.out.println("No option selected but I offer you a selection of Cities");
			queryInstancesFromDbpediaOntologyClass("http://dbpedia.org/ontology/City");
			read();

		}
	}

	/**
	 * @param uri
	 */
	public static List<Resource> queryInstancesFromDbpediaOntologyClass(String uri) {
		String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String queryString = "SELECT * WHERE { " + "    SERVICE <" + serviceURI + "> { "
				+ "        SELECT DISTINCT ?subject ?property where {?subject a <" + uri + ">} LIMIT 20" + "    }"
				+ "}";

		Query query = QueryFactory.create(queryString);

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			List<Resource> resources = new ArrayList<Resource>();
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("2000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("subject");
					if (x.isResource()) {
						resources.add((Resource) x);
					}
				}
			} finally {
				qexec.close();
			}
			return resources;

		}

	}

	/**
	 * @param uri
	 */
	public static void queryResourceInformation(String uri) {

		String serviceURI = "http://dbpedia-live.openlinksw.com/sparql?timeout=2000";

		String queryString = "SELECT DISTINCT * WHERE { " + "    SERVICE <" + serviceURI + "> { "
				+ "        SELECT  ?property ?object WHERE { <" + uri + "> ?property ?object . "
				+ Sparql.addFilter(null, "property", "object") + " } " + " LIMIT 100000" + "    }" + "}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			createDefaultModel(serviceURI, query, qexec);
		}
	}

	/**
	 * @param uri
	 */
	public static void queryWikipediaPageByResource(String uri) {

		String serviceURI = "http://dbpedia.org/sparql";

		String queryStr = "SELECT  * WHERE {?subject foaf:primaryTopic <" + uri + ">}";

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryStr));
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			createDefaultModel(serviceURI, query, qexec);
		}
	}

	/**
	 * @param uri
	 */
	public static void queryResources(String uri) {
		String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String queryString = "SELECT * WHERE { " + "    SERVICE <" + serviceURI + "> { "
				+ "        SELECT DISTINCT ?subject where {?subject a <" + uri + ">} LIMIT 20" + "    }" + "}";

		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			createDefaultModel(serviceURI, query, qexec);
		}
	}

	/**
	 * @param serviceURI
	 * @param query
	 * @param qexec
	 */
	private static void createDefaultModel(String serviceURI, Query query, QueryExecution qexec) {
		Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();
		values.add("2000");
		params.put("timeout", values);
		serviceParams.put(serviceURI, params);
		qexec.getContext().set(ARQ.serviceParams, serviceParams);
		ResultSet rs = qexec.execSelect();
		ResultSetFormatter.out(System.out, rs, query);

	}

	/**
	 * @param uri1
	 * @param link
	 * @return
	 */
	public static int countIndirectIncomingLinksFromResourceAndLink(String uri1, String link) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT (count (*) as ?x) WHERE { "
				+ "values (?r1 ?p1) { ( <" + uri1.trim() + ">  <" + link.trim() + ">)} " 
				+ "?r1 ?p1 ?r2 . " 
				+ "?r3 ?p1 ?r2 . "
				+ "FILTER (?r1 != ?r3)." 
				+ Sparql.addFilter(null, null, "r3") 
				+ "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}

	/**
	 * @param uri1
	 * @param link
	 * @return
	 */
	public static int countIndirectOutgoingLinksFromResourceAndLink(String uri1, String link) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" 
				+ Sparql.addService(USING_GRAPH, serviceURI) 
				+ "SELECT (count (*) as ?x) WHERE { "
				+ "values (?r1 ?p1) { ( <" + uri1 + ">  <" + link + ">)} " 
				+ "?r2 ?p1 ?r1 . " 
				+ "?r2 ?p1 ?r3 . "
				+ "FILTER (?r1 != ?r3). " 
				//+ Sparql.addFilter(null, null, "r2") 
				+ " } "
				+ Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}

	/**
	 * @param uri1
	 * @param link
	 * @return
	 */
	public static int getIndirectIncomingLinkFrom2ResourcesAndLink(String uri1, String uri3, String link) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
				+ "SELECT (count (*) as ?x) "
				+ "WHERE { values (?r1 ?p1 ?r3) { ( <" + uri1.trim() + ">  <" + link.trim() + "> <"+ uri3.trim() + ">)} " 
				+ "?r1 ?p1 ?r2 . " 
				+ "?r3 ?p1 ?r2 . " 
				+ "FILTER (?r1 != ?r3)."
				//+ Sparql.addFilter(null, null, "r2") 
				+ " }" 
				+ Sparql.addServiceClosing(USING_GRAPH);

		//System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();
					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}

	/**
	 * @param uri1
	 * @param link
	 * @return
	 */
	public static int getIndirectOutgoingLinkFrom2ResourcesAndLink(String uri1, String uri3, String link) {

		int finding = 0;

		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
				// + "SELECT (count (*) as ?x) WHERE { "
				+ " SELECT (count (*) as ?x) WHERE { " 
				+ "values (?r1 ?p1 ?r3) { ( <" + uri1 + ">  <" + link + "> <"+ uri3 + ">)} " 
				+ "?r2 ?p1 ?r1 . " 
				+ "?r2 ?p1 ?r3 . " 
				+ "FILTER(?r1 != ?r3) . "
				//+ Sparql.addFilter(null, null, "r2") 
				+ " } " 
				+ Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			// createDefaultModel(serviceURI, query, qexec);
			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("20000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();
				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("x");
					finding = (int) x.asLiteral().getValue();
				}
			} finally {
				qexec.close();
			}
			return finding;

		}
	}
}

	
