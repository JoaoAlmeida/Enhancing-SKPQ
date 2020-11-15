package skpq.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import node.Sparql;

public class LODUtil {

	private char quotes = '"';
	private boolean USING_GRAPH;
	private Model model = getTestModel();

	public LODUtil() {
		this.USING_GRAPH = false; // default option
	}

	public String getDBPediaAbs(String label) {

		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT ?abs WHERE {"
				+ "?var rdfs:label " + quotes + label + quotes + "@pt."
				+ "?var <http://dbpedia.org/ontology/abstract>  ?abs" + " FILTER langMatches( lang(?abs), " + quotes
				+ "pt" + quotes + " )}" + Sparql.addServiceClosing(USING_GRAPH);
		
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("2000000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);

			try {
				ResultSet rs = qexec.execSelect();

				for (; rs.hasNext();) {
					QuerySolution rb = rs.nextSolution();

					RDFNode labelRDF = rb.get("abs");

					if (labelRDF.isLiteral()) {

						return labelRDF.asLiteral().getValue().toString();
					}
				}
			} finally {
				qexec.close();
			}
			return " ";
		}
	}

	protected Model getTestModel() {

		Model model = ModelFactory.createDefaultModel();
		return model;
	}

	public boolean isUSING_GRAPH() {
		return USING_GRAPH;
	}

	public void setUSING_GRAPH(boolean uSING_GRAPH) {
		USING_GRAPH = uSING_GRAPH;
	}

	public static void main(String[] args) {
		LODUtil l = new LODUtil();

		System.out.println(l.getDBPediaAbs("Eldorado Business Tower"));
	}
}
