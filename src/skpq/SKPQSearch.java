package skpq;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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

/**
 * Process a Spatial Preference Keyword Query using LOD.
 * 
 * @author  João Paulo
 */

public class SKPQSearch extends SpatialQueryLD{
		
	public static boolean USING_GRAPH = false;	
	//static final int k = 300;
	static final boolean debug = false;	
	//private String keywords;
	
	public SKPQSearch(int k, String keywords, String neighborhood) throws IOException {
		super(k, keywords);		
	}

	public static void main(String[] args) throws IOException {

			//k=20 já gera os arquivos necessários para o experimento (k=5, k=10, k=15 e k=20)
			SKPQSearch search = new SKPQSearch(20, "sunset", "range");
	
			Writer outputFile = new OutputStreamWriter(new FileOutputStream("SPKQ-LD [" + "k=" + search.getK() + ", kw=" + search.getKeywords() + "].txt"), "ISO-8859-1");
			
			List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
			TreeSet<SpatialObject> topK = new TreeSet<>();
	
			interestObjectSet = loadObjectsInterest("hotel_LGD.txt");	
			
			System.out.println("Processing SKPQ query...\n");
			
			if(debug){			
				System.out.println("\nk = " + search.k + " | keywords = [ " + search.keywords + " ]" + " | type = range\n\n");
			}
			
			double start = System.currentTimeMillis();
			
			topK = search.findFeaturesLGD(interestObjectSet, search.keywords);
	
			//Iterator<SpatialObject> it = topK.descendingIterator();
			
			System.out.println("\n\nPrinting top-k result set.....\n");
			
			search.saveResults(topK);
			/*
			int i = 0;
			
			while (it.hasNext()) {
				i++;
				SpatialObject aux = it.next();
	
				if (aux != null) {
					//System.out.println("-->[" + i + "]  " + "[OSMlabel=" + aux.getName() + ", uri=" + aux.getURI() + ", score=" + " --> " + aux.getScore());	
					System.out.println("-->[" + i + "]  " + "[OSMlabel=" + aux.getName() + ", lat=" + aux.getLat() + ", lgt=" + aux.getLgt() + ", score=" + aux.getScore() + "]");
					outputFile.write("-->[" + i + "]  " + "[OSMlabel=" + aux.getName() + ", lat=" + aux.getLat() + ", lgt=" + aux.getLgt() + ", score=" + aux.getScore() + "]\n");
				} else {
					System.out.println("No objects to display.");
				}
			}*/
			
			System.out.println("\n\nQuery processed in " + ((System.currentTimeMillis() - start)/1000)/60 + " mins");
			//o armazenamento pode ser feito após cada busca de descrição para se prevenir de httpexceptions
			search.searchCache.store();	
			outputFile.close();		
	}
	
	private void saveResults(TreeSet<SpatialObject> topK) throws IOException{
		
		/* Imprime 5 */
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("SPKQ-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		Iterator<SpatialObject> it = topK.descendingIterator();
		
		for(int a = 1; a <= 5; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 10 */
		outputFile = new OutputStreamWriter(new FileOutputStream("SPKQ-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 10; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 15 */
		outputFile = new OutputStreamWriter(new FileOutputStream("SPKQ-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 15; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 20 */
		outputFile = new OutputStreamWriter(new FileOutputStream("SPKQ-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 20; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
	}

	// Searches for features in DBpedia dataset.
	public TreeSet<SpatialObject> findFeatures(List<SpatialObject> interestSet, String keywords) {

		List<Resource> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();

		String serviceURI = "http://dbpedia.org/sparql";

		for (int a = 0; a < interestSet.size(); a++) {
			
			if(debug){
				System.out.println("Objeto de interesse: " + interestSet.get(a).getURI());
			}
			
			featureSet = new ArrayList<>();

			// Find features within 200 meters (200m = 0.2)
			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource WHERE { <"
					+ interestSet.get(a).getURI() + "> geo:geometry  ?sourcegeo ."
					+ " ?resource geo:geometry ?location ;" + "rdfs:label ?label ."
					+ " FILTER ( bif:st_intersects(?location,?sourcegeo, 0.2) )" + "FILTER( lang( ?label ) =" + quotes
					+ "en" + quotes + ")}" + Sparql.addServiceClosing(USING_GRAPH);

			// System.out.println(queryString);
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
						}
					}
				} finally {
					qexec.close();
				}

			}

			double maxScore = 0;

			for (int b = 0; b < featureSet.size(); b++) {

				String abs = getTextDescription(featureSet.get(b).getURI());

				double score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);

				// System.out.println("Score: " + score + " Obj: " +
				// featureSet.get(b).getURI());
				if (score > maxScore) {
					maxScore = score;
				}
			}

			interestSet.get(a).setScore(maxScore);

			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore() || (interestSet.get(a).getScore() == topK.first().getScore()
							&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
		}
		return topK;
		
	}

	// Searches for features in OpenStreetMap dataset
	public TreeSet<SpatialObject> findFeaturesLGD(List<SpatialObject> interestSet, String keywords) {

		List<Resource> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();

		String serviceURI = "http://linkedgeodata.org/sparql";

		for (int a = 0; a < interestSet.size(); a++) {
			
			if(debug){
				System.out.print("Objeto de interesse: " + interestSet.get(a).getURI());
			}
			
			featureSet = new ArrayList<>();

			// Find features within 200 meters (200m = 0.2)

			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource WHERE { <"
					+ interestSet.get(a).getURI() + "> <http://geovocab.org/geometry#geometry>  ?point ."
					+ "?point <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo."
					+ "?resource <http://geovocab.org/geometry#geometry> ?loc."
					+ "?loc <http://www.opengis.net/ont/geosparql#asWKT> ?location." + "?resource rdfs:label ?nome."
					+ "filter(bif:st_intersects( ?location, ?sourcegeo, 0.2)).}"
					+ Sparql.addServiceClosing(USING_GRAPH);

			//System.out.println(queryString);

			Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

//				Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
//				Map<String, List<String>> params = new HashMap<String, List<String>>();
//				List<String> values = new ArrayList<String>();
//				values.add("2000000");
//				params.put("timeout", values);
//				serviceParams.put(serviceURI, params);
//				qexec.getContext().set(ARQ.serviceParams, serviceParams);
				try {
					ResultSet rs = qexec.execSelect();

					for (; rs.hasNext();) {

						QuerySolution rb = rs.nextSolution();

						RDFNode x = rb.get("resource");

						if (x.isResource()) {
							//Set of objects neighbors to object of interest
							featureSet.add((Resource) x);
							// System.out.println(featureSet.get(0).getURI());
						}
					}
				} finally {
					qexec.close();
				}

			}

			double maxScore = 0;

			//compute the textual score for each feature
			for (int b = 0; b < featureSet.size(); b++) {

				String abs;

				if (searchCache.containsKey(featureSet.get(b).getURI())) {
					abs = searchCache.getDescription(featureSet.get(b).getURI());
				} else {
					abs = getTextDescriptionLGD(featureSet.get(b).getURI());
					searchCache.putDescription(featureSet.get(b).getURI(), abs);
				}
//				 System.out.println("abstract: " + abs);
				// System.out.println("key: " + keywords);
				double score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				// System.out.println("Score: " + score);
				// System.out.println("Score: " + score + " Obj: " +
				// featureSet.get(b).getURI());
				if (score > maxScore) {
					maxScore = score;
				}
			}

			//set the highest score from one feature in the interest object
			interestSet.get(a).setScore(maxScore);
			
			if(debug){
				System.out.print(" | Score = " + maxScore + "\n");
			}
			
			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore() || (interestSet.get(a).getScore() == topK.first().getScore()
							&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
		}		
		return topK;
	}

	// SKPQ to one object of interest
	public List<Resource> praUm(String URI, String keywords) {

		List<Resource> aux = new ArrayList<>();
		List<Resource> featureSet = new ArrayList<>();

		String serviceURI = "http://dbpedia.org/sparql";

		for (int a = 0; a < 1; a++) {

			// Find features within 200 meters
			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource WHERE { <"
					+ URI + "> geo:geometry  ?sourcegeo ." + " ?resource geo:geometry ?location ;"
					+ "rdfs:label ?label ." + " FILTER ( bif:st_intersects(?location,?sourcegeo, 0.5) )"
					+ "FILTER( lang( ?label ) =" + quotes + "en" + quotes + ")}"
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
							aux.add((Resource) x);
						}
					}
				} finally {
					qexec.close();
				}

			}

			double maxScore = 0;
			featureSet.add(null);
			// System.out.println(aux.size());
			for (int b = 0; b < aux.size(); b++) {

				String abs = getAbstract(aux.get(b).getURI());
				// System.out.println("\n\n" + abs);
				double score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);

				System.out.println("Score: " + score + " Obj: " + aux.get(b).getURI());
				if (score > maxScore) {

					maxScore = score;
					featureSet.set(a, aux.get(b));

				}
			}
			System.out.println("\nMaior Score: " + maxScore);

		}
		return featureSet;
	}

	public String getAbstract(String uri) {

		String abs = new String();

		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { <" + uri
				+ "> <http://dbpedia.org/ontology/abstract> ?abstract ." + "FILTER( lang( ?abstract ) =" + quotes + "en"
				+ quotes + ")}" + Sparql.addServiceClosing(USING_GRAPH);

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

					RDFNode x = rb.get("abstract");

					if (x.isLiteral()) {
						abs = x.asLiteral().getValue().toString();
						// System.out.println(uri);
						// System.out.println("\n" + abs + "\n\n\n");
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

	// Get text description from LGD AND DBpedia
	public String getTextDescriptionLGD(String uri) {

		String description = new String();

		String serviceURI = "http://linkedgeodata.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { <" + uri
				+ "> rdf:type ?type ." + "}" + Sparql.addServiceClosing(USING_GRAPH);

		// System.out.println(queryString);

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
						// System.out.println(uri);
						// System.out.println("\n" + abs + "\n\n\n");
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

		// System.out.println(queryString);

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
						// System.out.println(uri);
						// System.out.println("\n" + abs + "\n\n\n");
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

	// query example on LGD endpoint
	public String linkedGeoData(String uri) {

		String abs = new String();

		String serviceURI = "http://linkedgeodata.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT ?l WHERE{ "
				+ "?s owl:sameAs <http://dbpedia.org/resource/Leipzig_Hauptbahnhof> ;"
				+ "geom:geometry [ ogc:asWKT ?sg ] ." + "?x a lgdo:Amenity ;" + "rdfs:label ?l ;"
				+ "geom:geometry [ ogc:asWKT ?xg ] ." + "FILTER(bif:st_intersects (?sg, ?xg, 0.1)) .}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		System.out.println(queryString);
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

				System.out.println(rs.getResultVars().toString());

				for (; rs.hasNext();) {

					QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("l");
					// RDFNode y = rb.get("comment");

					if (x.isLiteral()) {
						abs = x.asLiteral().getValue().toString();
						// abs = abs + y.asLiteral().getValue().toString();
						// System.out.println(uri);
						// System.out.println("\n" + abs + "\n\n\n");
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

	public String getTextDescription(String uri) {

		String abs = new String();

		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { <" + uri
				+ "> <http://dbpedia.org/ontology/abstract> ?abstract ;" + "rdfs:comment ?comment. "
				+ "FILTER( lang( ?abstract ) =" + quotes + "en" + quotes + "&&lang( ?comment) =" + quotes + "en"
				+ quotes + ")}" + Sparql.addServiceClosing(USING_GRAPH);

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

					RDFNode x = rb.get("abstract");
					RDFNode y = rb.get("comment");

					if (x.isLiteral() && y.isLiteral()) {
						abs = x.asLiteral().getValue().toString();
						abs = abs + y.asLiteral().getValue().toString();
						System.out.println(uri);
						System.out.println("\n" + abs + "\n\n\n");
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

	// procura por resources que pertencem a ontologia passada como parâmetro. É
	// possível fazer isso mais fácil usando o prefixo lgdo
	public List<Resource> searchObjectofInterest(String object) {

		List<Resource> resources = new ArrayList<Resource>();
		// String serviceURI = "http://dbpedia-live.openlinksw.com/sparql";
		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?hotel " + "WHERE {  "
				+ " ?hotel a <http://dbpedia.org/ontology/" + object + ">. }" + " LIMIT 10"
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

					RDFNode x = rb.get("hotel");
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

	// Given a spatial coordinate, finds objects near the coordinate that has
	// the given name in its URI -- broken, fiz direto no snorql.
	@Deprecated
	public List<Resource> searchObjectofInterest(double lat, double lon, String object) {

		List<Resource> resources = new ArrayList<Resource>();

		String serviceURI = "http://dbpedia.org/sparql";

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT distinct ?m "
				+ "bif:st_distance " + "(?geo, bif:st_point (" + lon + "," + lat + ")) WHERE {"
				+ " ?m geo:geometry ?geo." + "?m a ?prop." + "filter( regex(str(?m), " + quotes + object + quotes
				+ " ))" + "FILTER (bif:st_intersects (?geo, bif:st_point (" + lon + "," + lat + "), 5))}"
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

					RDFNode x = rb.get("m");
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
}
