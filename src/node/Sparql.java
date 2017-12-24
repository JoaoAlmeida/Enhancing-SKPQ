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

import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class Sparql {

	public static char quotes = '"';

	static public void main(String... argv) {
		String queryStr = "select distinct ?Concept where {[] a ?Concept} LIMIT 10";
		Query query = QueryFactory.create(addPrefix().concat(queryStr));
		// Remote execution.
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query)) {
			// Set the DBpedia specific timeout.
			((QueryEngineHTTP) qexec).addParam("timeout", "10000");
			ResultSet rs = qexec.execSelect();
			ResultSetFormatter.out(System.out, rs, query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String getStringUnionDirectInBothWaysForCheckingCount(String uri1, Set<String> uris) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String stringUri : uris) {
			stringBuilder.append(" " + " UNION " + " { <" + uri1.trim() + "> ?p1  <" + stringUri.trim() + "> } "
					+ "  UNION " + " { <" + stringUri.trim() + "> ?p1  <" + uri1.trim() + "> } ");
		}
		String ss = stringBuilder.toString().replaceFirst("UNION", "");
		// Lodica.print(ss);
		return ss;
	}

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String getStringQueryValuesUnionIndirectIncoming(String uri1, Set<String> uris) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String stringUri : uris) {
			stringBuilder.append(" " + " UNION " + " { values (?r1 ?r3){(<" + uri1.trim() + "> <" + stringUri.trim()
					+ ">)}  ?r1 ?p1 ?r2 .  ?r3 ?p1 ?r2 } ");
		}
		String ss = stringBuilder.toString().replaceFirst("UNION", "");
		// Lodica.print(ss);
		return ss;
	}

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String getStringQueryValuesUnionIndirectOutgoing(String uri1, Set<String> uris) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String stringUri : uris) {
			stringBuilder.append(" " + " UNION " + " { values (?r1 ?r3){(<" + uri1.trim() + "> <" + stringUri.trim()
					+ ">)}  ?r2 ?p1 ?r1 .  ?r2 ?p1 ?r3 } ");
		}
		String ss = stringBuilder.toString().replaceFirst("UNION", "");
		// Lodica.print(ss);
		return ss;
	}

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String getStringQueryValuesUnionDirectOneWay(String uri1, Set<String> uris) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String stringUri : uris) {
			stringBuilder.append(" " + " UNION " + " { values (?r1 ?r2){(<" + uri1.trim() + "> <" + stringUri.trim()
					+ ">)}  ?r1 ?p1 ?r2 . } ");
		}
		String ss = stringBuilder.toString().replaceFirst("UNION", "");
		// Lodica.print(ss);
		return ss;
	}

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String getStringQueryValuesUnionDirectBothWays(String uri1, Set<String> uris) {
		StringBuilder stringBuilder = new StringBuilder();

		for (String stringUri : uris) {

			stringBuilder.append(" " + " UNION " + " { values (?r1 ?r2){(<" + uri1.trim() + "> <" + stringUri.trim()
					+ ">)}  ?r1 ?p1 ?r2  } UNION { values (?r1 ?r2){(<" + uri1.trim() + "> <" + stringUri.trim()
					+ ">)}  ?r2 ?p1 ?r1  }");
		}
		String ss = stringBuilder.toString().replaceFirst("UNION", "");
		// Lodica.print(ss);
		return ss;
	}
	
	
	//fred
	public static String getStringQueryValuesUnionDirectBothWaysForNodes(String uri1, Set<Node> nodes) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Node node : nodes) {
			stringBuilder.append(" " + " UNION " + " { values (?r1 ?r2){(<" + uri1 + "> <" + node.getURI()+ ">)}  ?r1 ?p1 ?r2} "
					+ "UNION { values (?r1 ?r2){(<" + uri1 + "> <" + node.getURI()+ ">)}  ?r2 ?p1 ?r1  }");
		}
		String ss = stringBuilder.toString().replaceFirst("UNION", "");
		// Lodica.print(ss);
		return ss;
	}	

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String getStringQueryValuesUnionIncomingAndOutgoing(String uri1, Set<String> uris) {
		String incoming = getStringQueryValuesUnionIndirectIncoming(uri1, uris);
		String outgoing = getStringQueryValuesUnionIndirectIncoming(uri1, uris);
		String finalString = " ";
		if (!incoming.trim().isEmpty() && !outgoing.trim().isEmpty()) {
			finalString = incoming.concat(" UNION ").concat(outgoing);
		} else if (!incoming.trim().isEmpty() && outgoing.trim().isEmpty()) {
			finalString = incoming;
		} else {
			finalString = outgoing;
		}
		return finalString;
	}

	/**
	 * @param uri1
	 * @param uris
	 * @return
	 */
	public static String addFilterURIs(String var, Set<String> uris) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String stringUri : uris) {
			stringBuilder.append(" " + " . " + " FILTER(STRSTARTS(STR(?" + var + "), <" + stringUri + ">))  ");
		}
		String ss = stringBuilder.toString().replaceFirst(".", "");
		// Lodica.print(ss);
		return ss;
	}

	/**
	 * @return
	 */
	public static String addPrefix() {
		String prefix = "" + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
				
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " 
				+ "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
				+ "PREFIX bif: <bif:>" //adicionado por João Paulo
				+ "PREFIX : <http://dbpedia.org/resource/> " 
				+ "PREFIX dbpedia2: <http://dbpedia.org/property/> "
				+ "PREFIX dbpedia: <http://dbpedia.org/> " 
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
				+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"
				+ "PREFIX e: <http://learningsparql.com/ns/expenses#> "
				+ "PREFIX d: <http://learningsparql.com/ns/data#> ";
		return prefix;

	}

	public static String addLangFilter(String subject, String property, String object) {
		String subject1 = " ";
		String property2 = " ";
		String object3 = " ";
		if (subject != null) { }

		if (property != null) {
			property2 = " FILTER(?" + property + " != <http://dbpedia.org/ontology/wikiPageID>)." + "FILTER(?"
					+ property + " != <http://dbpedia.org/ontology/wikiPageRevisionID>). ";
			// property2 = " " ;
		}

		if (object != null) {
			object3 = " FILTER(lang(?" + object + ") = " + quotes + "" + quotes + " || langMatches(lang(?" + object
					+ ")," + quotes + "en" + quotes + "))    . ";
		}
		String filter = subject1.concat(property2).concat(object3).trim();
		return filter;
	}

	public static String addService(boolean usingGraph, String serviceURI) {
		if (!usingGraph) {
			return "SELECT DISTINCT * WHERE { SERVICE <" + serviceURI + ">{";
		} else {
			return " ";
		}
	}

	public static String addServiceClosing(boolean usingGraph) {
		if (!usingGraph) {
			return "}}";
		} else {
			return " ";
		}
	}

	public static String checkLimitNull(Object object) {
		if (object == null) {
			return " ";
		} else {
			return "LIMIT " + object.toString();
		}
	}

	/**
	 * @return
	 */
	public static String addFilterDomain2(String subject, String object) {
		String subject1 = " ";
		String object3 = " ";

		if (subject != null) {
			subject1 = "((?" + subject + " a <http://dbpedia.org/ontology/Film> ) OR " + "(?" + subject
					+ " a <http://dbpedia.org/ontology/WrittenWork> ) OR " + "(?" + subject
					+ " a <http://dbpedia.org/ontology/Organisation> )) . ";

		}

		if (object != null) {
			object3 = "((?" + object + " a <http://dbpedia.org/ontology/Film> ) OR " + "(?" + object
					+ " a <http://dbpedia.org/ontology/WrittenWork> ) OR " + "(?" + object
					+ " a <http://dbpedia.org/ontology/Organisation> )) . ";

		}

		String filter = subject1.concat(object3).trim();

		return filter;

	}

	/**
	 * @return
	 */
	public static String addFilterDomain(String subject, String object) {
		String subject1 = " ";
		String object3 = " ";

		if (subject != null) {
			subject1 = "{?" + subject + " a ?type }. FILTER( ?type IN(" + " <http://dbpedia.org/ontology/Film>,"
					+ " <http://dbpedia.org/ontology/WrittenWork>," + " <http://dbpedia.org/ontology/Organisation>))";
		}

		if (object != null) {
			object3 = "{?" + object + " a ?type } . FILTER( ?type IN(" + " <http://dbpedia.org/ontology/Film>,"
					+ " <http://dbpedia.org/ontology/WrittenWork>," + " <http://dbpedia.org/ontology/Organisation>))";
		}
		String filter = subject1.concat(object3).trim();
		return filter;

	}

	/**
	 * @return
	 */
	public static String addFilter(String subject, String property, String object) {
		String subject1 = " ";
		String property2 = " ";
		String object3 = " ";
		if (subject != null) {

		}
		if (property != null) {
			property2 = " FILTER(?" + property + " != <http://dbpedia.org/ontology/wikiPageID>)." + "FILTER(?"
					+ property + " != <http://dbpedia.org/ontology/wikiPageRevisionID>). ";
			// property2 = " " ;
		}
		if (object != null) {
			object3 = " FILTER(!isLiteral(?" + object + ")). ";
		}
		String filter = subject1.concat(property2).concat(object3).trim();
		return filter;
	}

	/*
	 * http://dbpedia.org/ontology/Actor
	 * http://dbpedia.org/ontology/AdministrativeRegion
	 * http://dbpedia.org/ontology/Agent http://dbpedia.org/ontology/Album
	 * http://dbpedia.org/ontology/Animal
	 * http://dbpedia.org/ontology/ArchitecturalStructure
	 * http://dbpedia.org/ontology/Artist http://dbpedia.org/ontology/Athlete
	 * http://dbpedia.org/ontology/Book http://dbpedia.org/ontology/Building
	 * http://dbpedia.org/ontology/CelestialBody
	 * http://dbpedia.org/ontology/City http://dbpedia.org/ontology/Company
	 * http://dbpedia.org/ontology/Eukaryote http://dbpedia.org/ontology/Event
	 * http://dbpedia.org/ontology/Film
	 * http://dbpedia.org/ontology/Infrastructure
	 * http://dbpedia.org/ontology/Insect http://dbpedia.org/ontology/Location
	 * http://dbpedia.org/ontology/MeanOfTransportation
	 * http://dbpedia.org/ontology/Municipality
	 * http://dbpedia.org/ontology/MusicalArtist
	 * http://dbpedia.org/ontology/MusicalWork
	 * http://dbpedia.org/ontology/NaturalPlace
	 * http://dbpedia.org/ontology/Organisation
	 * http://dbpedia.org/ontology/Person http://dbpedia.org/ontology/Place
	 * http://dbpedia.org/ontology/Plant http://dbpedia.org/ontology/Politician
	 * http://dbpedia.org/ontology/PopulatedPlace
	 * http://dbpedia.org/ontology/Region http://dbpedia.org/ontology/Settlement
	 * http://dbpedia.org/ontology/SoccerPlayer
	 * http://dbpedia.org/ontology/SocietalEvent
	 * http://dbpedia.org/ontology/Species
	 * http://dbpedia.org/ontology/SportsTeam http://dbpedia.org/ontology/Work
	 * http://dbpedia.org/ontology/WrittenWork
	 */
}
