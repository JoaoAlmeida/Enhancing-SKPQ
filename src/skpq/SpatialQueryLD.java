package skpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.commons.text.similarity.JaroWinklerDistance;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.library.max;

import cosinesimilarity.LuceneCosineSimilarity;
import node.Sparql;
import skpq.util.QueryEvaluation;
import skpq.util.RatingExtractor;
import skpq.util.WebContentArrayCache;
import skpq.util.WebContentCache;
import util.experiment.Experiment;
import util.experiment.ExperimentException;
import util.experiment.ExperimentResult;
import xxl.core.spatial.points.DoublePoint;
import xxl.util.StarRTree;

/**
 * Essentials to process a top-k query using LOD
 * 
 * @author Joao Paulo
 */

public abstract class SpatialQueryLD implements Experiment {

	protected WebContentCache searchCache;
	protected ArrayList<ExperimentResult> result;	
	protected static BufferedReader reader;
	protected boolean USING_GRAPH;
	protected Model model = getTestModel();
	protected int k;
	protected String keywords;
	protected char quotes = '"';
	private final String cacheFileName = "descriptions.ch";
	protected StarRTree objectsOfInterest;
	protected boolean debug;


	public SpatialQueryLD(int k, String keywords, StarRTree objectsOfInterest, boolean debug) throws IOException {
		this.k = k;
		this.keywords = keywords;
		this.debug = debug;
		this.USING_GRAPH = false; // default option		

		searchCache = new WebContentCache(cacheFileName);
		searchCache.load();	
		
		this.objectsOfInterest = objectsOfInterest;
	}

	public SpatialQueryLD(String keywords, StarRTree objectsOfInterest, boolean debug) throws IOException {
		this.keywords = keywords;
		this.debug = debug;
		this.USING_GRAPH = false; // default option

		searchCache = new WebContentCache(cacheFileName);
		searchCache.load();

		this.objectsOfInterest = objectsOfInterest;
	}

	protected boolean isUSING_GRAPH() {
		return USING_GRAPH;
	}

	protected void setUSING_GRAPH(boolean uSING_GRAPH) {
		USING_GRAPH = uSING_GRAPH;
	}

	protected int getK() {
		return k;
	}

	protected String getKeywords() {
		return keywords;
	}

	protected Model getTestModel() {

		Model model = ModelFactory.createDefaultModel();
		return model;
	}

	protected static ArrayList<SpatialObject> loadObjectsInterest(String inputFileName) throws IOException {

		ArrayList<SpatialObject> objectsInterest = new ArrayList<>();
		SpatialObject obj;

		reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8")));

		String line = reader.readLine();

		int i = 1;

		while (line != null) {
			String uri = line.substring(line.indexOf("http") - 1).trim();

			String osmLabel = "(tourism) (hotel) " +line.substring(line.indexOf(" ", line.indexOf(" ") + 1), line.indexOf("http") - 1).trim();

			String[] lineVec = line.split(" ");

			String lat = lineVec[0];
			String lgt = lineVec[1];
			obj = new SpatialObject(i, osmLabel, uri, lat, lgt);
			objectsInterest.add(obj);
			line = reader.readLine();

			i++;
		}
		return objectsInterest;
	}

	// Euclidean distance (return distance in meters). Distance method online verified at <https://gps-coordinates.org/distance-between-coordinates.php>
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {

		double earthRadius = 6371000;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
		* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = (double) (earthRadius * c);

		return dist;
	}

	@Override
	public void close() throws ExperimentException {
		try {
			objectsOfInterest.close();
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}

	@Override
	public ExperimentResult[] getResult() {
		return result.toArray(new ExperimentResult[result.size()]);
	}

	@Override
	public void open() throws ExperimentException {
		try {
			objectsOfInterest.open();
		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}

	protected abstract TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException, IOException;

	@Override
	public void run() throws ExperimentException {
		try {			
			TreeSet<SpatialObject> topK = new TreeSet<>();

			topK = execute(keywords, k);			

			searchCache.store();			

			if (debug) {
				printResults(topK.iterator());
			}

		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}

	protected abstract void saveResults(TreeSet<SpatialObject> topK) throws IOException;

	public void printQueryName() {
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]\n\n");
	}

	protected double[] evaluateQuery(String keywords, String radius, int numResult, boolean personalized) throws IOException{

		System.out.println("\n");
		System.out.println("=========================");
		System.out.println("  Evaluating Query...  ");
		System.out.println("=========================");					

		String fileName;
		ArrayList<String> rateResults = null;

		double[] ndcg = new double[4];

		int k_max = numResult;
		int inc = 5, k = 5, a = 0;

		System.out.println("NDCG");

		while(k <= k_max){
			//mudar nome dos arquivos
			if(personalized) {
				fileName = "PSKPQ-LD [k="+k+", kw="+ keywords +"].txt";
			}else {			
				if(radius == null){			
					fileName = "SKPQ-LD [k="+k+", kw="+ keywords +"].txt";
				} else{
					fileName = "SKPQ-LD [k="+k+", kw="+ keywords + ", radius=" + radius + "].txt";			
				}
			}
			
			boolean arquivoCriado = false;					
			
			
			if(!arquivoCriado){

				Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split("\\.txt")[0] + " --- ratings.txt"), "ISO-8859-1");

				/*Evaluation methods: 
				 * default --> using only Google Maps rate
				 * cosine --> considers cosine similarity score and Google Maps rate 
				 * tripAdvisor --> using an opinRank query, it searches for user's judgment related to the query. It is necessary to set the rate file manually.
				 * personalized --> searches for the rate related to the the user preference. Each user preference is represented by a profile. The user preference must be described manually in the method.				 
				 * */				
				//RatingExtractor obj = new RatingExtractor("tripAdvisor");
				RatingExtractor obj = new RatingExtractor("personalized");
				
				if(radius == null){
					rateResults = obj.rateLODresult(fileName);			
				}else{
					rateResults = obj.rateRangeLODresult(fileName);
				}

				for (String x : rateResults) {

					output.write(x + "\n");	
				}		
				output.close();
			}

			QueryEvaluation q = new QueryEvaluation(fileName.split("\\.txt")[0] + " --- ratings.txt");

			ndcg[a] = q.execute();
			System.out.print(ndcg[a] + " ");

			k = k + inc;
			a++;
		}
		System.out.println("\n");
		return ndcg;
	}	

	// Searches for features in OpenStreetMap dataset. Actually using this because the match method is important in second article.
	public TreeSet<SpatialObject> findFeaturesLGD(List<SpatialObject> interestSet, String keywords, double radius, String match){

		//Resource is not serializable, for this reason there are two features set
		ArrayList<SpatialObject> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();
//		ArrayList<String> features;

		String serviceURI = "http://linkedgeodata.org/sparql";

		for (int a = 0; a < interestSet.size(); a++) {					 
			
			//if (debug) {
			// System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			//	System.out.print("POI #" + a + " - " + interestSet.get(a).getURI());
			//}
			
			featureSet = new ArrayList<>();
			
			// Find features within 200 meters (200m = 0.2)
			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource WHERE { <"
					+ interestSet.get(a).getURI() + "> <http://geovocab.org/geometry#geometry>  ?point ."
					+ "?point <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo."
					+ "?resource <http://geovocab.org/geometry#geometry> ?loc."
					+ "?loc <http://www.opengis.net/ont/geosparql#asWKT> ?location." + "?resource rdfs:label ?nome."
					+ "filter(bif:st_intersects( ?location, ?sourcegeo, " + radius + ")).}"
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

					for (; rs.hasNext();) {QuerySolution rb = rs.nextSolution();

					RDFNode x = rb.get("resource");
					RDFNode location = rb.get("location");
					RDFNode nome = rb.get("name");

					if (x.isResource()) {
						// Set of objects neighbors to the object of interest (POI)
//						featureSet.add((Resource) x);
						
						Resource r = (Resource) x;
																			
						String[] array = location.asLiteral().getString().split(",")[0].split(" ");
						String lat = array[0].split("\\(")[1];
						String lgt = array[1];	
						
//						System.out.println("Location from LGD: " + location.asLiteral().getString() + " " + lat + " " + lgt);
//						System.out.println("Nome from LGD: " + nome.asLiteral().getString());
//						System.out.println("URI from LGD: " + r.getURI());
						
						SpatialObject feature = new SpatialObject(0, nome.asLiteral().getString(), r.getURI(), lat, lgt);
						featureSet.add(feature);
					}
					}
				} finally {					
					qexec.close();					
				}
			}
			
			//if (debug) {
			//	System.out.println(" | Number of features: " + featureSet.size());
			//	System.out.println("\nSelecting the best feature...");
			//}
			
			double maxScore = 0;

			WebContentArrayCache featuresCache = new WebContentArrayCache("pois/POI["+ a +"].cache", radius); ;

//			features  = new ArrayList<String>();

			//compute the textual score for each feature
			for (int b = 0; b < featureSet.size(); b++) {

//				features.add(featureSet.get(b).getURI());

				String abs;				

				if (searchCache.containsKey(featureSet.get(b).getURI())) {
					abs = searchCache.getDescription(featureSet.get(b).getURI());
				} else {
					abs = getTextDescriptionLGD(featureSet.get(b).getURI());
					searchCache.putDescription(featureSet.get(b).getURI(), abs);
				}

				double score = 0;

				if(match.equals("default")){					
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}else if(match.equals("fuzzy")){
					FuzzyScore f = new FuzzyScore(Locale.ENGLISH);
					score = f.fuzzyScore(abs, keywords);
				}else if(match.equals("jw")){
					//System.out.println("\nUsing Levenshtein Distance ...\n");
					JaroWinklerDistance jw = new JaroWinklerDistance();
					score = jw.apply(abs, keywords);					

				}else{
					System.out.println("WARN -- Unknown similarity measure! Default measure used instead. ");
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}

				if (score > maxScore) {
					maxScore = score;
				}
			}				
			featuresCache.putArray(interestSet.get(a).getURI(), featureSet);

			try {
				featuresCache.store();
			} catch (IOException e) {
				e.printStackTrace();
			}

//			features.clear();
//			features = new ArrayList<>();

			//set the highest score from one feature in the interest object
			interestSet.get(a).setScore(maxScore);

			//if (debug) {
			//	System.out.println("\nPOI Score = " + maxScore + "\n");
			//	System.out.println("\n::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			//}
			
			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
		}		
		return topK;
	}
	
	/* Search including the best feature in the POI object. It is necessary verify the need to maintain this method in future versions of the program. 
	 * 
	 * Change note 0.1: included best neighbor latitude and longitude in the sparql query. Updated during third article.
	 * 
	 * */
	public TreeSet<SpatialObject> findFeaturesLGDBN(List<SpatialObject> interestSet, String keywords, double radius, String match){

		//Resource is not serializable, for this reason there are two features set
//		ArrayList<Resource> featureSet;
		ArrayList<SpatialObject> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();
//		ArrayList<String> features;

		String serviceURI = "http://linkedgeodata.org/sparql";

		for (int a = 392; a < interestSet.size(); a++) {					 
			if (debug) {
			 System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				System.out.print("POI #" + a + " - " + interestSet.get(a).getURI());
			}
			
			featureSet = new ArrayList<>();
			
			// Find features within 200 meters (200m = 0.002)
			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource ?location ?name WHERE { <"
					+ interestSet.get(a).getURI() + "> <http://geovocab.org/geometry#geometry>  ?point ."
					+ "?point <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo."
					+ "?resource <http://geovocab.org/geometry#geometry> ?loc."
					+ "?loc <http://www.opengis.net/ont/geosparql#asWKT> ?location." 
					+ "?resource rdfs:label ?name."					
					+ "filter(bif:st_intersects( ?location, ?sourcegeo, " + radius + ")).}"
					+ Sparql.addServiceClosing(USING_GRAPH);

			Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

			try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

				Map<String, Map<String, List<String>>> serviceParams = new
						HashMap<String, Map<String, List<String>>>();
				Map<String, List<String>> params = new HashMap<String,
						List<String>>();
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
						RDFNode location = rb.get("location");
						RDFNode nome = rb.get("name");

						if (x.isResource()) {
							// Set of objects neighbors to the object of interest (POI)
//							featureSet.add((Resource) x);
							
							Resource r = (Resource) x;
																				
							String[] array = location.asLiteral().getString().split(",")[0].split(" ");
							String lgt = array[0].split("\\(")[1];
							String lat = array[1];	
		
							if(lat.contains(")")){
								lat = lat.split("\\)")[0];
							}
							
//							System.out.println("Location from LGD: " + location.asLiteral().getString() + " " + lat + " " + lgt);
//							System.out.println("Nome from LGD: " + nome.asLiteral().getString());
//							System.out.println("URI from LGD: " + r.getURI());
							
							SpatialObject feature = new SpatialObject(0, nome.asLiteral().getString(), r.getURI(), lat, lgt);
							featureSet.add(feature);
						}
					}
				} finally {					
					qexec.close();					
				}
			}
			
						if (debug) {
							System.out.println(" | Number of features: " + featureSet.size());
							System.out.println("\nSelecting the best feature...");
						}
			
			/* Store the features that are spatially close to the POI in the cache to speed up the next queries */			
			WebContentArrayCache featuresCache = new WebContentArrayCache("pois/POI["+ a +"].cache", radius); 			
			featuresCache.putArray(interestSet.get(a).getURI(), featureSet);					
			
			//Save the cache
			try {
				featuresCache.store();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			/* ============== End =============== */
			
			double maxScore = 0;
			SpatialObject bestFeature = null;
					
//			features  = new ArrayList<String>();

			//compute the textual score for each feature
			for (int b = 0; b < featureSet.size(); b++) {

//				features.add(featureSet.get(b).getURI());

				String abs;				

				if (searchCache.containsKey(featureSet.get(b).getURI())) {
					abs = searchCache.getDescription(featureSet.get(b).getURI());
				} else {
					abs = getTextDescriptionLGD(featureSet.get(b).getURI());
					searchCache.putDescription(featureSet.get(b).getURI(), abs);
				}		
				
				double score = 0;

				if(match.equals("default")){					
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}else if(match.equals("fuzzy")){
					FuzzyScore f = new FuzzyScore(Locale.ENGLISH);
					score = f.fuzzyScore(abs, keywords);
				}else if(match.equals("jw")){
					//System.out.println("\nUsing Levenshtein Distance ...\n");
					JaroWinklerDistance jw = new JaroWinklerDistance();
					score = jw.apply(abs, keywords);					

				}else{
					System.out.println("WARN -- Unknown similarity measure! Default measure used instead. ");
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}
				
				try {
					searchCache.store();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (score > maxScore) {
					maxScore = score;
					bestFeature = featureSet.get(b);		
				}
			}								

			// set the highest score from one feature in the interest object
			interestSet.get(a).setScore(maxScore);
			interestSet.get(a).setBestNeighbor(bestFeature);
//			interestSet.get(a).bestNeighbor = bestFeature;

			if (debug) {
				System.out.println("\nPOI Score = " + maxScore + "\n");
				System.out.println("\n::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
			}
			
			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
		}		
		return topK;
	}
	
	/* Used in the second article to process the query faster using cache of features in folder pois. 
	 * Change note 0.1: include the best neighbor into the POI object. FeatureSet now stores SpatialObjects */
	public TreeSet<SpatialObject> findFeaturesLGDFast(List<SpatialObject> interestSet, String keywords, double radius, String match){
		 
		TreeSet<SpatialObject> topK = new TreeSet<>();
		
		for (int a = 0; a < interestSet.size(); a++) {
				
			WebContentArrayCache featuresCache = new WebContentArrayCache("pois/POI["+ a +"].cache", radius);

			try {
				featuresCache.load();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
						
			if (debug) {
				System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				System.out.println("POI #" + a + " - " + interestSet.get(a).getURI());
			}
			
			ArrayList<SpatialObject> featureSet = featuresCache.getArray(interestSet.get(a).getURI());											

			double maxScore = 0;
			SpatialObject bestFeature = null;
			
			// compute the textual score for each feature
			for (int b = 0; b < featureSet.size(); b++) {					
				
				String abs;				
				
				if (searchCache.containsKey(featureSet.get(b).getURI())) {									
					abs = searchCache.getDescription(featureSet.get(b).getURI());
					
				} else {
					abs = getTextDescriptionLGD(featureSet.get(b).getURI());
					searchCache.putDescription(featureSet.get(b).getURI(), abs);
				}
				
				double score = 0;
				
				if(match.equals("default")){						
					 score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}else if(match.equals("fuzzy")){
					FuzzyScore f = new FuzzyScore(Locale.ENGLISH);
					score = f.fuzzyScore(abs, keywords);
				}else if(match.equals("jw")){
					JaroWinklerDistance jw = new JaroWinklerDistance();
					score = jw.apply(abs, keywords);					
				}else{
					System.out.println("WARN -- Unknown similarity measure! Default measure used instead. ");
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}
				
				if (score > maxScore) {
					maxScore = score;	
					bestFeature = featureSet.get(b);
				}
			}			
			
			// set the highest score from one feature in the interest object
			if(maxScore != 0) {
				interestSet.get(a).setScore(maxScore);	
				interestSet.get(a).setBestNeighbor(bestFeature);
			}else {
				interestSet.get(a).setScore(0);
				SpatialObject nb = new SpatialObject(0, "empty", "empty", "0", "0");
				nb.setScore(0);
				interestSet.get(a).setBestNeighbor(nb);
			}

			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
			featureSet.clear();
		}
		
		return topK;
	}
	
	/* findfeaturesLGDFast using pareto in score equation. 25/03/2020
	 * cache must be created first executing the findFeaturesLGDBN(List<SpatialObject> interestSet, String keywords, double radius, String match)
	 */
	public TreeSet<SpatialObject> findFeaturesPareto(List<SpatialObject> interestSet, String keywords, double radius, String match){
		 
		TreeSet<SpatialObject> topK = new TreeSet<>();
		
		for (int a = 0; a < interestSet.size(); a++) {
				
			WebContentArrayCache featuresCache = new WebContentArrayCache("pois/POI["+ a +"].cache", radius);

			try {
				featuresCache.load();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
						
			if (debug) {
				System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				System.out.println("POI #" + a + " - " + interestSet.get(a).getURI());
			}
			
			ArrayList<SpatialObject> featureSet = featuresCache.getArray(interestSet.get(a).getURI());											

			double maxProb = -Double.MAX_VALUE;
			double minProb = Double.MAX_VALUE;
			
			for (int b = 0; b < featureSet.size(); b++) {
				
				double dist = SpatialQueryLD.distFrom(Double.parseDouble(featureSet.get(b).getLat()),
						Double.parseDouble(featureSet.get(b).getLgt()), Double.parseDouble(interestSet.get(a).getLat()),
						Double.parseDouble(interestSet.get(a).getLgt()));

				ParetoDistribution par = new ParetoDistribution();
				
				double probability = par.logDensity(dist);
								
				//não da pra fazer issso. Tem que ser negativo mesmo. Ou normalizar a outra função de probabilidade
//				probability = -probability;
				
//				System.out.println(probability);

				if (probability == Double.POSITIVE_INFINITY) {
					featureSet.get(b).setParetoProbability(Double.POSITIVE_INFINITY);					
				}else {					
					featureSet.get(b).setParetoProbability(probability);
					
					if(probability < minProb) {
						minProb = probability;
					}
					if(probability > maxProb) {
						maxProb = probability;
					}
				}												
			}			
			
			double maxScore = 0;
			SpatialObject bestFeature = null;
						
			// compute the textual score for each feature
			for (int b = 0; b < featureSet.size(); b++) {					
				
				String abs;				
				
				if (searchCache.containsKey(featureSet.get(b).getURI())) {									
					abs = searchCache.getDescription(featureSet.get(b).getURI());
					
				} else {
					abs = getTextDescriptionLGD(featureSet.get(b).getURI());
					searchCache.putDescription(featureSet.get(b).getURI(), abs);
				}
				
				double score = 0;
				
				if(match.equals("default")){						
					 score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}else if(match.equals("fuzzy")){
					FuzzyScore f = new FuzzyScore(Locale.ENGLISH);
					score = f.fuzzyScore(abs, keywords);
				}else if(match.equals("jw")){
					JaroWinklerDistance jw = new JaroWinklerDistance();
					score = jw.apply(abs, keywords);					
				}else{
					System.out.println("WARN -- Unknown similarity measure! Default measure used instead. ");
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}
								
				// Sometimes the probability will be negative infinite. Here we deal with this
				// ignoring it.
				if (!(featureSet.get(b).getParetoProbability() == Double.POSITIVE_INFINITY) && score != 0) {

					double normProb = (featureSet.get(b).getParetoProbability() - minProb) / (maxProb - minProb);					
					score = (0.5 * score) + ((1 - 0.5) * normProb);	
				}
											
				if (score > maxScore) {					
					maxScore = score;	
					bestFeature = featureSet.get(b);
				}
			}			

			// set the highest score from one feature in the interest object
			if(maxScore != 0) {
				interestSet.get(a).setScore(maxScore);	
				interestSet.get(a).setBestNeighbor(bestFeature);
			//in case no feature with textual relevance is found
			}else {
				interestSet.get(a).setScore(0);
				SpatialObject nb = new SpatialObject(0, "empty", "empty", "0", "0");
				nb.setScore(0);
				interestSet.get(a).setBestNeighbor(nb);
			}

			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
			featureSet.clear();
		}
		
		return topK;
	}
	
	public TreeSet<SpatialObject> findFeaturesInfluence(List<SpatialObject> interestSet, String keywords, double radius, String match){
		 
		TreeSet<SpatialObject> topK = new TreeSet<>();
		
		//Radius converted to meteres hardcoded. Must create a method to this in the future
		double radiusMeters = 6378137 * Math.PI * radius/180;
//		double radiusMeters = 22252.61131; apagar
		
		for (int a = 0; a < interestSet.size(); a++) {
				
			WebContentArrayCache featuresCache = new WebContentArrayCache("pois/POI["+ a +"].cache", radius);

			try {
				featuresCache.load();
			} catch (IOException e1) {				
				e1.printStackTrace();
				System.exit(0);
			}
						
			if (debug) {
				System.out.println("::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
				System.out.println("POI #" + a + " - " + interestSet.get(a).getURI());
			}
			
			ArrayList<SpatialObject> featureSet = featuresCache.getArray(interestSet.get(a).getURI());																				
			
			double maxScore = 0;
			SpatialObject bestFeature = null;
						
			// compute the textual score for each feature
			for (int b = 0; b < featureSet.size(); b++) {					
				
				String abs;				
				
				if (searchCache.containsKey(featureSet.get(b).getURI())) {									
					abs = searchCache.getDescription(featureSet.get(b).getURI());
					
				} else {
					abs = getTextDescriptionLGD(featureSet.get(b).getURI());
					searchCache.putDescription(featureSet.get(b).getURI(), abs);
				}
				
				double score = 0;
				
				if(match.equals("default")){						
					 score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}else if(match.equals("fuzzy")){
					FuzzyScore f = new FuzzyScore(Locale.ENGLISH);
					score = f.fuzzyScore(abs, keywords);
				}else if(match.equals("jw")){
					JaroWinklerDistance jw = new JaroWinklerDistance();
					score = jw.apply(abs, keywords);					
				}else{
					System.out.println("WARN -- Unknown similarity measure! Default measure used instead. ");
					score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);
				}										
							
				//calculate the influence score: score = score * 2^(-dist(p,f) / Q.r)
				if(score !=0) {
					double dist = SpatialQueryLD.distFrom(Double.parseDouble(featureSet.get(b).getLat()),
							Double.parseDouble(featureSet.get(b).getLgt()), Double.parseDouble(interestSet.get(a).getLat()),
							Double.parseDouble(interestSet.get(a).getLgt()));
					
					score = score * Math.pow(2, -dist / radiusMeters);					
				}
				
				if (score > maxScore) {					
					maxScore = score;	
					bestFeature = featureSet.get(b);
				}
			}			

			// set the highest score from one feature in the interest object
			if(maxScore != 0) {
				interestSet.get(a).setScore(maxScore);	
				interestSet.get(a).setBestNeighbor(bestFeature);
			//in case no feature with textual relevance is found
			}else {
				interestSet.get(a).setScore(0);
				SpatialObject nb = new SpatialObject(0, "empty", "empty", "0", "0");
				nb.setScore(0);
				interestSet.get(a).setBestNeighbor(nb);
			}

			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
			featureSet.clear();
		}
		
		return topK;
	}
	
	//Search for hotels using hotels as features. Employed as Experiment 2 at first article.
	public TreeSet<SpatialObject> findFeaturesExperiment(List<SpatialObject> interestSet, String keywords, double radius) throws IOException {

		ArrayList<SpatialObject> featureSet = loadObjectsInterest("hotel_LGD.txt");			
		TreeSet<SpatialObject> topK = new TreeSet<>();				
		
		for(int a = 0; a < interestSet.size(); a++){
		
			SpatialObject objectInterest  = interestSet.get(a);
			
			if (debug) {
				System.out.print("Objeto de interesse: " + interestSet.get(a).getURI());
			}
			
			DoublePoint oiPoint = new DoublePoint(new double[]{Double.parseDouble(objectInterest.getLat()), Double.parseDouble(objectInterest.getLgt())});
			
			Iterator<SpatialObject> featureIT = featureSet.iterator();
		
			double maxScore = -1;
			@SuppressWarnings("unused")
			SpatialObject bestFeature = null;
			
			while(featureIT.hasNext()){
							
				SpatialObject feature = featureIT.next();
				
				double distance = oiPoint.distanceTo(new DoublePoint(new double[]{Double.parseDouble(feature.getLat()), Double.parseDouble(feature.getLgt())}));
					
				if(distance <= radius){
					
					String abs;				

					if (searchCache.containsKey(feature.getURI())) {
						abs = searchCache.getDescription(feature.getURI());
						feature.setCompleteDescription(abs);
					} else {
						abs = getTextDescriptionLGD(feature.getURI());
						searchCache.putDescription(feature.getURI(), abs);
						feature.setCompleteDescription(abs);
					}

					double score = LuceneCosineSimilarity.getCosineSimilarity(abs, keywords);

					if (score > maxScore) {
						maxScore = score;
						bestFeature = new SpatialObject(0, feature.getURI());						
					}
				}
			}		

			// set the highest score from one feature in the interest object
			interestSet.get(a).setScore(maxScore);			
//			interestSet.get(a).bestNeighbor = bestFeature;
			
			if (debug) {
				System.out.print(" | Score = " + maxScore + "\n");
			}

			if (topK.size() < k) {
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
		}
		return topK;
	}

	// Searches for features in OpenStreetMap dataset for one POI only
	protected TreeSet<SpatialObject> findFeaturesLGD(SpatialObject interestObject, String keywords, double radius) {

		boolean debugMethod = false;
		List<SpatialObject> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();

		String serviceURI = "http://linkedgeodata.org/sparql";

		if (debug) {
			System.out.print("Objeto de interesse: " + interestObject.getURI());
		}

		featureSet = new ArrayList<>();

		// Find features within 200 meters (200m = 0.2)
		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
		+ "SELECT DISTINCT ?resource ?location ?name WHERE { <" + interestObject.getURI()
		+ "> <http://geovocab.org/geometry#geometry>  ?point ."
		+ "?point <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo."
		+ "?resource <http://geovocab.org/geometry#geometry> ?loc."
		+ "?loc <http://www.opengis.net/ont/geosparql#asWKT> ?location." + "?resource rdfs:label ?name."
		+ "filter(bif:st_intersects( ?location, ?sourcegeo, " + radius + ")).}" + Sparql.addServiceClosing(USING_GRAPH);

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

				for (;rs.hasNext();) {

					QuerySolution rb = rs.nextSolution();

					RDFNode resource = rb.get("resource");
					RDFNode location = rb.get("location");
					RDFNode name = rb.get("name");
					
					if (resource.isResource()) {

						SpatialObject obj = new SpatialObject(name.asLiteral().getString(), resource.asResource().getURI());
						String[] array = location.asLiteral().getString().split(",")[0].split(" ");
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

			String description;

			if (searchCache.containsKey(featureSet.get(b).getURI())) {
				description = searchCache.getDescription(featureSet.get(b).getURI());

			} else {
				description = getTextDescriptionLGD(featureSet.get(b).getURI());
				searchCache.putDescription(featureSet.get(b).getURI(), description);				
			}

			double score = LuceneCosineSimilarity.getCosineSimilarity(description, keywords);

			featureSet.get(b).setScore(score);

			if (debugMethod) {
				System.out.print("Name = " + featureSet.get(b).getName() + " | Score = " + score + "\n");
			}

			// keeps the best objects, if they have the same scores, keeps
			// the objects with smaller ids
			if (topK.size() < k) {
				topK.add(featureSet.get(b));			
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
	protected String getTextDescriptionLGD(String uri) {

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

					RDFNode type = rb.get("type");

					if (!type.isLiteral()) {
						String[] split = type.asResource().toString().split("/");
						description = description + " " + split[split.length - 1];

					} else {
						System.out.println("No type found!");
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

	protected String getLabelLGD(String uri) {

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

					RDFNode labelNode = rb.get("label");

					if (labelNode.isLiteral()) {

						label = labelNode.asLiteral().toString();
					} else {
						System.out.println("No label found!");
						label = " ";
					}

				} else {
					System.out.println("No label found!");
				}
			} finally {
				qexec.close();
			}
		}
		return label;
	}

	// Looks for the description of the object which has this label at Dbpedia
	protected String lgdIntersectsDBpedia(String label) {

		String description = new String();

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

					RDFNode abs = rb.get("abstract");
					RDFNode comment = rb.get("comment");

					if (abs.isLiteral() && comment.isLiteral()) {
						description = abs.asLiteral().getValue().toString();
						description = description + comment.asLiteral().getValue().toString();

					} else {
						//Case no description is found, returns an empty description
						description = "";
					}
				}
			} finally {
				qexec.close();
			}
		}
		return description;
	}

	protected static SpatialObject identifyURILocation(String queryCoordinates, List<SpatialObject> interestObjectSet) {

		SpatialObject obj = null;
		Iterator<SpatialObject> objSet = interestObjectSet.iterator();

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

	//Finds resources that belongs to an ontology. It is possibly do this task using lgdo prefix too (more easy).	
	public List<Resource> searchObjectofInterest(String object) {

		List<Resource> resources = new ArrayList<Resource>();

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

	// Searches for features only in DBpedia dataset.
	public TreeSet<SpatialObject> findFeatures(List<SpatialObject> interestSet, String keywords) {

		List<Resource> featureSet;
		TreeSet<SpatialObject> topK = new TreeSet<>();

		String serviceURI = "http://dbpedia.org/sparql";

		for (int a = 0; a < interestSet.size(); a++) {

			if (debug) {
				System.out.println("Objeto de interesse: " + interestSet.get(a).getURI());
			}

			featureSet = new ArrayList<>();

			// Find features within 200 meters (200m = 0.2)
			String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT DISTINCT ?resource WHERE { <"
					+ interestSet.get(a).getURI() + "> geo:geometry  ?sourcegeo ."
					+ " ?resource geo:geometry ?location ;" + "rdfs:label ?label ."
					+ " FILTER ( bif:st_intersects(?location,?sourcegeo, 0.2) )" + "FILTER( lang( ?label ) =" + quotes
					+ "en" + quotes + ")}" + Sparql.addServiceClosing(USING_GRAPH);

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

				if (score > maxScore) {
					maxScore = score;
				}
			}

			interestSet.get(a).setScore(maxScore);

			if (topK.size() < k) {
				System.out.println(interestSet.get(a).getScore());		
				topK.add(interestSet.get(a));
				// keeps the best objects, if they have the same scores, keeps
				// the objects with smaller ids
			} else if (interestSet.get(a).getScore() > topK.first().getScore()
					|| (interestSet.get(a).getScore() == topK.first().getScore()
					&& interestSet.get(a).getId() > topK.first().getId())) {
				System.out.println(interestSet.get(a).getScore());
				topK.pollFirst();
				topK.add(interestSet.get(a));
			}
		}
		return topK;

	}

	//Returns the object description id DBpedia
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

	//Returns the abstract property of an object in DBpedia
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

					if (x.isLiteral()) {
						abs = x.asLiteral().getValue().toString();			
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
	
	protected void printResults(Iterator<SpatialObject> iterator){

		System.out.println("\n\nPrinting top-k result set.....\n");

		int i = 0;

		while (iterator.hasNext()) {

			i++;
			SpatialObject aux = iterator.next();

			if (aux != null) {
				System.out.println(i + " - " + aux.getURI() + " " + aux.getCompleteDescription() + " --> " + aux.getScore());
				System.out.println("BN: " + aux.getBestNeighbor().getName() + " " + aux.getBestNeighbor().getLat() + " " + aux.getBestNeighbor().getLgt() );

			} else {
				System.out.println("No objects to display.");
			}
		}
	}
}
