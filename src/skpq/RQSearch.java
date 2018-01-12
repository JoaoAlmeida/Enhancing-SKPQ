package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

import cosinesimilarity.LuceneCosineSimilarity;
import node.Sparql;
import skpq.util.SpatioTreeHeapEntry;
import util.experiment.ExperimentException;
import xxl.core.cursors.Cursor;
import xxl.util.StarRTree;

/**
 * Process a top-k Range Query (RQ) using LOD.
 * 
 * The top-k RQ retrieves ranked objects based on their text relevance and the
 * query region.
 * 
 * @author João Paulo
 */

public class RQSearch extends SpatialQueryLD {

	static final boolean debug = false;
	private String queryLocation;	
	boolean evaluate = true;

	public RQSearch(int k, String keywords, String queryLocation, double radius, StarRTree objectsOfInterest)
			throws IOException {
		super(k, keywords, objectsOfInterest);

		this.queryLocation = queryLocation;		
	}

	@Override
	protected TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		ArrayList<double[]> evaluations = new ArrayList<>();

		try {
			interestObjectSet = loadObjectsInterest("hotel_LGD.txt");
		} catch (IOException e) {
			System.out.println("Objects of interest loading failed!");
			e.printStackTrace();
		}

		System.out.println("Processing Range query...\n");

		if (debug) {
			printQueryName();
		}

		int a = 0;
		int iterations = 4;

		TreeSet<SpatialObject> topK = null;

		Cursor leaves = objectsOfInterest.query(1);
		SpatioTreeHeapEntry leafEntry = new SpatioTreeHeapEntry(leaves.next());
		Cursor interestPointer = objectsOfInterest.query(leafEntry.getMBR());

		while (interestPointer.hasNext() && a < iterations) {					

			SpatioTreeHeapEntry point = new SpatioTreeHeapEntry(interestPointer.next());

			String coordinates = String.valueOf(point.getMBR().getCorner(false).getValue(0)) + ", "
					+ String.valueOf(point.getMBR().getCorner(false).getValue(1));

			queryLocation = coordinates;

			SpatialObject queryLocationObj = identifyURILocation(queryLocation, interestObjectSet);

			topK = findFeatureLGD(queryLocationObj, keywords);

			//Preparação para preencher as outras posições
			Cursor dummy = null; 

			if(topK.size() < k){

				dummy = objectsOfInterest.query(leafEntry.getMBR());									

				while (topK.size() < k && dummy.hasNext()) {										

					point = new SpatioTreeHeapEntry(dummy.next());

					coordinates = String.valueOf(point.getMBR().getCorner(false).getValue(0)) + ", "
							+ String.valueOf(point.getMBR().getCorner(false).getValue(1));												 

					topK.add(identifyURILocation(coordinates, interestObjectSet));
				}
			}

			try {
				saveResults(topK);
				if(evaluate){
					evaluations.add(evaluateQuery(keywords, queryLocation, k));	
				}							
			} catch (IOException e) {
				System.out.println("We can't save the results on your disk!");
				e.printStackTrace();
			}
			a++;
		}

		System.out.println("\nSomatório " + keywords);
		//Calculate evaluation arithmetic mean
		double[] ndcg = new double[evaluations.get(0).length];

		for(int b = 0; b < evaluations.get(0).length; b++){
			for(int c = 0; c < evaluations.size(); c++){		
				ndcg[b] = ndcg[b] + evaluations.get(c)[b];
			}
			//			ndcg[b] = ndcg[b] / evaluations.size();
			System.out.print(ndcg[b] + " ");
		}

		if (debug) {

			System.out.println("\n\nPrinting top-k result set.....\n");

			Iterator<SpatialObject> it = topK.iterator();

			int i = 0;

			while (it.hasNext()) {

				i++;
				SpatialObject aux = it.next();

				if (aux != null) {
					System.out.println(i + " - " + aux.getURI() + " --> " + aux.getScore());
				} else {
					System.out.println("No objects to display.");
				}
			}
		}

		return topK;
	}

	public static void main(String[] args) throws IOException {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();

		interestObjectSet = loadObjectsInterest("hotel_LGD.txt");

		System.out.println("Processing Range query...\n");

		double start = System.currentTimeMillis();	

		SpatialObject queryLocation = identifyURILocation("24.4903228, 54.3578107", interestObjectSet);

		RQSearch search = new RQSearch(20, "amenity", queryLocation.getURI(), 0, null);

		if (debug) {
			System.out.println("\nk = " + search.k + " | keywords = [ " + search.keywords + " ]\n\n");
		}

		TreeSet<SpatialObject> topK = search.findFeatureLGD(queryLocation, search.keywords);

		System.out.println("\n\nPrinting top-k result set.....\n");

		int i = 0;

		Iterator<SpatialObject> objSet = interestObjectSet.iterator();

		while (topK.size() < search.k && objSet.hasNext()) {
			topK.add(objSet.next());
		}

		search.saveResults(topK);

		if (debug) {
			
			Iterator<SpatialObject> it = topK.iterator();

			i = 0;

			while (it.hasNext()) {

				i++;
				SpatialObject aux = it.next();

				if (aux != null) {
					System.out.println(i + " - " + aux.getURI() + " --> " + aux.getScore());
				} else {
					System.out.println("No objects to display.");
				}
			}
		}
		search.searchCache.store();
		System.out.println("\n\nQuery processed in " + ((System.currentTimeMillis() - start) / 1000) / 60 + " mins");
	}

	@Override
	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {

		/* Imprime 5 */			
		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("RQ-LD [" + "k=" + "5" + ", kw=" + getKeywords() + ", loc=" + queryLocation + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.iterator();

		for (int a = 1; a <= 5; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "] " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();

		/* Imprime 10 */
		outputFile = new OutputStreamWriter(
				new FileOutputStream("RQ-LD [" + "k=" + "10" + ", kw=" + getKeywords() + ", loc=" + queryLocation + "].txt"), "ISO-8859-1");

		it = topK.iterator();
		;

		for (int a = 1; a <= 10; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "] " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();

		/* Imprime 15 */
		outputFile = new OutputStreamWriter(
				new FileOutputStream("RQ-LD [" + "k=" + "15" + ", kw=" + getKeywords() + ", loc=" + queryLocation +  "].txt"), "ISO-8859-1");

		it = topK.iterator();

		for (int a = 1; a <= 15; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "] " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();

		/* Imprime 20 */
		outputFile = new OutputStreamWriter(
				new FileOutputStream("RQ-LD [" + "k=" + "20" + ", kw=" + getKeywords() + ", loc=" + queryLocation + "].txt"), "ISO-8859-1");

		it = topK.iterator();

		for (int a = 1; a <= 20; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "] " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}

	// Searches for features in OpenStreetMap dataset
	public TreeSet<SpatialObject> findFeatureLGD(SpatialObject interestObject, String keywords) {

		List<SpatialObject> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();

		String serviceURI = "http://linkedgeodata.org/sparql";

		if (debug) {
			System.out.print("Objeto de interesse: " + interestObject.getURI());
		}

		featureSet = new ArrayList<>();

		// Find features within 200 meters (200m = 0.2)
		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
		+ "SELECT DISTINCT ?resource ?location ?nome WHERE { <" + interestObject.getURI()
		+ "> <http://geovocab.org/geometry#geometry>  ?point ."
		+ "?point <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo."
		+ "?resource <http://geovocab.org/geometry#geometry> ?loc."
		+ "?loc <http://www.opengis.net/ont/geosparql#asWKT> ?location." + "?resource rdfs:label ?nome."
		+ "filter(bif:st_intersects( ?location, ?sourcegeo, 0.2)).}" + Sparql.addServiceClosing(USING_GRAPH);

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
					RDFNode y = rb.get("location");
					RDFNode z = rb.get("nome");

					if (x.isResource()) {

						SpatialObject obj = new SpatialObject(z.asLiteral().getString(), x.asResource().getURI());
						String[] array = y.asLiteral().getString().split(",")[0].split(" ");
						String lat = array[0].split("\\(")[1];
						String lgt = array[1];

						obj.setLat(lat);
						obj.setLgt(lgt);
						featureSet.add(obj);
					}
				}
			} finally {
				qexec.close();
			}
		}

		for (int b = 0; b < featureSet.size(); b++) {

			String abs;

			if (searchCache.containsKey(featureSet.get(b).getURI())) {
				abs = searchCache.getDescription(featureSet.get(b).getURI());

			} else {
				abs = getTextDescriptionLGD(featureSet.get(b).getURI());
				searchCache.putDescription(featureSet.get(b).getURI(), abs);
			}

			double score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);

			featureSet.get(b).setScore(score);

			if (debug) {
				System.out.print("Name = " + featureSet.get(b).getName() + " | Score = " + score + "\n");
			}

			if (topK.size() < k) {
				topK.add(featureSet.get(b));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (featureSet.get(b).getScore() > topK.first().getScore()
					|| (featureSet.get(b).getScore() == topK.first().getScore()
					&& featureSet.get(b).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(featureSet.get(b));
			}
		}
		return topK;
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
		System.out.println("executou " + label);
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
						System.out.println(abs);
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

	public static SpatialObject identifyURILocation(String queryCoordinates, List<SpatialObject> interestObjectSet) {

		SpatialObject obj = null;
		Iterator<SpatialObject> objSet = interestObjectSet.iterator();

		if (queryCoordinates.equals("free")) {
			return objSet.next();
		}

		while (objSet.hasNext()) {

			SpatialObject interestObject = objSet.next();

			if (interestObject.getLat().equals(queryCoordinates.split(",")[0].trim())
					&& interestObject.getLgt().equals(queryCoordinates.split(",")[1].trim())) {

				obj = interestObject;

				return obj;
			}
		}

		if (obj == null) {
			System.out.println("Erro ao identificar a URI equivalente ao query location");
		}

		return obj;
	}
}
