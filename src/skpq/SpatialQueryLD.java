package skpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import skpq.util.QueryEvaluation;
import skpq.util.RatingExtractor;
import skpq.util.WebContentCache;
import util.Util;
import util.experiment.Experiment;
import util.experiment.ExperimentException;
import util.experiment.ExperimentResult;
import xxl.util.StarRTree;

/**
 * Essentials to process a top-k query using LOD
 * 
 * @author João Paulo
 */

public abstract class SpatialQueryLD implements Experiment {

	protected WebContentCache searchCache;	
	protected ArrayList<ExperimentResult> result;
	public final boolean debugMode = false;
	protected static BufferedReader reader;
	protected boolean USING_GRAPH;
	protected Model model = getTestModel();
	protected int k;
	protected String keywords;
	protected char quotes = '"';
	private final String cacheFileName = "descriptions.ch";
	protected StarRTree objectsOfInterest;

	public SpatialQueryLD(int k, String keywords, StarRTree objectsOfInterest) throws IOException {
		this.k = k;
		this.keywords = keywords;
		this.USING_GRAPH = false; // default option

		searchCache = new WebContentCache(cacheFileName);
		searchCache.load();	
		
		this.objectsOfInterest = objectsOfInterest;
	}

	public SpatialQueryLD(String keywords, StarRTree objectsOfInterest) throws IOException {
		this.keywords = keywords;
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

			String osmLabel = line.substring(line.indexOf(" ", line.indexOf(" ") + 1), line.indexOf("http") - 1).trim();

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

	// Distância euclidiana em metros
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; // meters
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

	protected abstract TreeSet<SpatialObject> execute(String queryKeywords, int k) throws ExperimentException;

	@Override
	public void run() throws ExperimentException {
		try {
			long time;

			TreeSet<SpatialObject> topK = new TreeSet<>();

			time = System.currentTimeMillis();

			double start = System.currentTimeMillis();

			topK = execute(keywords, k);

			if (debugMode) {
				System.out.println(Util.time(System.currentTimeMillis() - time) + "]");
			}
			
			System.out.println("\n\nQuery processed in " + ((System.currentTimeMillis() - start) / 1000) / 60 + " mins");
			// o armazenamento pode ser feito após cada busca de descrição para
			// se prevenir de httpexceptions
			searchCache.store();

		} catch (Exception e) {
			throw new ExperimentException(e);
		}
	}

	public void printQueryName() {
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]\n\n");
	}

	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {			
				
		/* Imprime 5 */
		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("SPKQ-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= 5; a++) {
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();

//		/* Imprime 10 */
//		outputFile = new OutputStreamWriter(
//				new FileOutputStream("SPKQ-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
//
//		it = topK.descendingIterator();
//
//		for (int a = 1; a <= 10; a++) {
//			SpatialObject obj = it.next();
//			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
//					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
//		}
//
//		outputFile.close();
//
//		/* Imprime 15 */
//		outputFile = new OutputStreamWriter(
//				new FileOutputStream("SPKQ-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
//
//		it = topK.descendingIterator();
//
//		for (int a = 1; a <= 15; a++) {
//			SpatialObject obj = it.next();
//			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
//					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
//		}
//
//		outputFile.close();
//
//		/* Imprime 20 */
//		outputFile = new OutputStreamWriter(
//				new FileOutputStream("SPKQ-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
//
//		it = topK.descendingIterator();
//
//		for (int a = 1; a <= 20; a++) {
//			SpatialObject obj = it.next();
//			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
//					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
//		}
//
//		outputFile.close();
	}
	
	protected double[] evaluateQuery(String keywords, String radius, int numResult) throws IOException{
		
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
				
				if(radius == null){			
					fileName = "SPKQ-LD [k="+k+", kw="+ keywords +"].txt";
				} else{
					fileName = "RQ-LD [k="+k+", kw="+ keywords + ", radius=" + radius + "].txt";			
				}
				
				boolean arquivoCriado = false;					
				
				if(!arquivoCriado){

					Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split("\\.txt")[0] + " --- ratings.txt"), "ISO-8859-1");
					RatingExtractor obj = new RatingExtractor("cossine");
					
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
				
				QueryEvaluation q = new QueryEvaluation(fileName.split("\\.txt")[0] + " --- ratings.txt", fileName.split("kw=")[1].split("\\]\\.txt")[0]);
						
				ndcg[a] = q.execute();
				System.out.print(ndcg[a] + " ");
				
				k = k + inc;
				a++;
		}
		System.out.println();
		return ndcg;
	}		
}
