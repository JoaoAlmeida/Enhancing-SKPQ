package skpq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import cosinesimilarity.LuceneCosineSimilarity;
import node.Sparql;
import util.experiment.ExperimentException;
import xxl.util.StarRTree;

/**
 * Process a top-k Spatial Boolean range Query (BRQ) using LOD.
 * 
 * The top-k BRQ retrieves ranked objects based on their text relevance and the
 * query region. 
 * 
 * In this query k value limits the result size, indicating how many objects will be presented to the user.
 * 
 * @author João Paulo
 */

//FALTA ORGANIZAR O EXECUTE
//Deve conter todas as keywords na descrição textual
public class RankedBRQ extends SpatialQueryLD {

	static final boolean debug = true;
	private final int k = 1;
	
	public RankedBRQ(int k, String keywords, String queryRegion, StarRTree rTree, double radius) throws IOException {
		super(keywords, null);
	}

	@Override
	protected TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) throws IOException {		

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();

		interestObjectSet = loadObjectsInterest("hotel_LGD.txt");

		System.out.println("Processing RBRQ query...\n");

		double start = System.currentTimeMillis();

		Iterator<SpatialObject> objSet = interestObjectSet.iterator();

		//A consulta Ã© realizada uma vez para cada objeto de interesse
		while(objSet.hasNext()){

			SpatialObject interestObject = objSet.next();

			RankedBRQ search = new RankedBRQ(0, "food McDonald's", interestObject.getURI(), null, 0);

			if (debug) {
				System.out.println("Region = 200m from " + interestObject.getURI() + " | keywords = [ " + search.keywords + " ]\n\n");
			}

			Iterator<SpatialObject> topK = search.findFeatureLGD(interestObject, search.keywords);	

			System.out.println("\n\nPrinting top-k result set.....\n");

			for(int a = 1; a <= search.k; a++){
							
				SpatialObject aux = topK.next();

				if (aux != null) {
					System.out.println(a + " - " + aux.getURI() + " --> " + aux.getScore());
				} else {
					System.out.println("No objects to display.");
				}
			}
			
			search.searchCache.store();	
			
			System.out.println("\n\nQuery processed in " + ((System.currentTimeMillis() - start) / 1000) / 60 + " mins");
		}			
	}

	// Searches for features in OpenStreetMap dataset
	public Iterator<SpatialObject> findFeatureLGD(SpatialObject interestObject, String keywords) {

		List<Resource> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();

		String serviceURI = "http://linkedgeodata.org/sparql";		

		if (debug) {
			System.out.print("Objeto de interesse: " + interestObject.getURI());
		}

		featureSet = new ArrayList<>();

		// Find features within 200 meters (200m = 0.2)
		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource WHERE { <"
				+ interestObject.getURI() + "> <http://geovocab.org/geometry#geometry>  ?point ."
				+ "?point <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo."
				+ "?resource <http://geovocab.org/geometry#geometry> ?loc."
				+ "?loc <http://www.opengis.net/ont/geosparql#asWKT> ?location." + "?resource rdfs:label ?nome."
				+ "filter(bif:st_intersects( ?location, ?sourcegeo, 0.2)).}"
				+ Sparql.addServiceClosing(USING_GRAPH);

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

					RDFNode x = rb.get("resource");

					if (x.isResource()) {
						featureSet.add((Resource) x);
						// System.out.println(featureSet.get(0).getURI());
					}
				}
			} finally {
				qexec.close();
			}

		}			

		String[] terms = keywords.split(" ");
		
		for (int b = 0; b < featureSet.size(); b++) {

			String abs;

			if (searchCache.containsKey(featureSet.get(b).getURI())) {
				abs = searchCache.getDescription(featureSet.get(b).getURI());
			} else {
				abs = getTextDescriptionLGD(featureSet.get(b).getURI());
				searchCache.putDescription(featureSet.get(b).getURI(), abs);
			}
				
			
			//Feature's textual description must contain ALL query keywords
			boolean containAllTerms = true;
			
			for(int a = 0; a < terms.length; a++){
				if(!abs.contains(terms[a])){
					containAllTerms = false;
				}
			}
			
			double score;
			
			if(containAllTerms){
				score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
			}
			else{
				score = 0;
			}			

			SpatialObject feature = new SpatialObject(featureSet.get(b).getLocalName(), featureSet.get(b).getURI());			

			feature.setScore(score);
			
			if (debug) {
				System.out.print("Name = " + featureSet.get(b).getLocalName() + " | Score = " + score + "\n");
			}

			topK.add(feature);
		}

		Iterator<SpatialObject> it = topK.descendingIterator();

		//return all neighbors in descending score order 
		return it;
	}

	// Get text description from LGD AND DBpedia
	public String getTextDescriptionLGD(String uri) {

		String description = new String();

		String serviceURI = "http://linkedgeodata.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { <" + uri
				+ "> rdf:type ?type ." + "}" + Sparql.addServiceClosing(USING_GRAPH);

		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("2000000000000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);
			try {
				ResultSet rs = qexec.execSelect();

				for (; rs.hasNext();) {

					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("type");

					if (!x.isLiteral()) {
						String[] split = x.asResource().toString().split("/");
						description = description + " " + split[split.length - 1];

					} else {
						System.out.println("No type!");
						description = " ";
					}
				}
				String label = getLabelLGD(uri);
				description = description + " " + label;
				description = description + " " + lgdIntersectsDBpedia(label);
			} finally {
				qexec.close();
			}
		}
		return description;
	}

	public String getLabelLGD(String uri) {

		String label = new String();

		String serviceURI = "http://linkedgeodata.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { <" + uri
				+ "> rdfs:label ?label ;" + "dcterms:modified ?dateModified." + "} order by ?dateModified"
				+ Sparql.addServiceClosing(USING_GRAPH);

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

				if (rs.hasNext()) {

					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("label");

					if (x.isLiteral()) {

						label = x.asLiteral().toString();						
					} else {
						System.out.println("No label!");
						label = " ";
					}

				} else {
					System.out.println("No label");
				}
			} finally {
				qexec.close();
			}
		}
		return label;
	}

	// Looks for the description of the object which has this label at Dbpedia
	public String lgdIntersectsDBpedia(String label) {

		String abs = new String();

		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE {" + "?var rdfs:label"
				+ quotes + label + quotes + "@en." + "?var <http://dbpedia.org/ontology/abstract> ?abstract;"
				+ "rdfs:comment ?comment. " + "FILTER( lang( ?abstract ) =" + quotes + "en" + quotes
				+ "&&lang( ?comment) =" + quotes + "en" + quotes + ")}" + Sparql.addServiceClosing(USING_GRAPH);

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

				if (rs.hasNext()) {

					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("abstract");
					RDFNode y = rb.get("comment");

					if (x.isLiteral() && y.isLiteral()) {
						abs = x.asLiteral().getValue().toString();
						abs = abs + y.asLiteral().getValue().toString();
					} else {
						System.out.println("SEM ABSTRACT");
						abs = "";
					}
				}
			} finally {
				qexec.close();
			}
		}
		return abs;
	}
}
