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
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.jena.sparql.function.library.min;

import skpq.util.Datasets;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import xxl.util.StarRTree;

/**
 * Process a Spatial Preference Keyword Query using LOD and Pareto distribution.
 * Rank rank-order
 * 
 * @author João Paulo
 */
public class SKPQPareto extends SpatialQueryLD {

	private double radius;
	String city;
	// GooglePlaces googleAPI;
	// private WebContentCache reviewCache;

	public SKPQPareto(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest,
			boolean debug, String city) throws IOException {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;
		this.city = city;
		// reviewCache = new WebContentCache("reviews.ch");
		// reviewCache.load();

		// try {
		// googleAPI = new GooglePlaces(RatingExtractor.readGoogleUserKey());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	public TreeSet<SpatialObject> execute(String queryKeywords, int k) throws IOException {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		TreeSet<SpatialObject> topK = new TreeSet<>();
		TreeSet<SpatialObject> pTopK = new TreeSet<>();

		try {
			interestObjectSet = loadObjectsInterest("./"+city.toLowerCase()+"/"+city+"LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing PSKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		// topK = findFeaturesLGDFast(interestObjectSet, keywords, radius, "default");

		Datasets data = new Datasets();

		try {
			topK = data.loadResultstoPersonalizeBN("SKPQ", keywords, k);		
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		// User profile
		// User u = new User(1);
		// u.loadCheckinProfile();

		// Pareto
		ParetoDistribution par = new ParetoDistribution();

		// KNN
//		BufferedReader reader = new BufferedReader((new InputStreamReader(
//				new FileInputStream(new File("./profiles/check-ins/New York/util/coordinates.txt")), "ISO-8859-1")));
//		Instances instances = new Instances(reader);
//
//		SimpleKMeans kmeans = new SimpleKMeans();
//
//		kmeans.setSeed(0);
//
//		try {
//			kmeans.setNumClusters(50);
//			kmeans.buildClusterer(instances);
//		} catch (Exception e2) {
//			e2.printStackTrace();
//		}

		Iterator<SpatialObject> it = topK.iterator();

		SpatialObject obj;

		obj = it.next();
		
		// If the query didn't find anything, there is no need to re-order
		if (!(obj.getScore() == 0)) {

			// Cria coordenadas do POI presente no rank
			double[] coords = { Double.parseDouble(obj.getLat()), Double.parseDouble(obj.getLgt()) };

			// encontra o check-in mais próximo do POI
			// double dist = u.findClosest(coords);
			double dist;

			dist = SpatialQueryLD.distFrom(Double.parseDouble(obj.getBestNeighbor().getLat()),
					Double.parseDouble(obj.getBestNeighbor().getLgt()), coords[0], coords[1]);

			double probability = par.logDensity(dist);
			
			double maxProb = probability;
			double minProb = probability;
			int id = 1;

			// Define o maior e menor valor de probability para normalização
			while (it.hasNext()) {
				
				obj = it.next();
				
				// Cria coordenadas do POI presente no rank
				// double[] coords = {Double.parseDouble(obj.getLat()),
				// Double.parseDouble(obj.getLgt())};

				// encontra o check-in mais próximo do POI
				// double dist = u.findClosest(coords);

				if (!obj.getBestNeighbor().getName().equals("empty")) {

					obj.setId(id);
					id--;
					pTopK.add(obj);
				} else {
					dist = SpatialQueryLD.distFrom(Double.parseDouble(obj.getBestNeighbor().getLat()),
							Double.parseDouble(obj.getBestNeighbor().getLgt()), Double.parseDouble(obj.getLat()),
							Double.parseDouble(obj.getLgt()));

					probability = par.logDensity(dist);
					
					// Sometimes the probability will be negative infinite. Here we deal with this
					// ignoring it. A probabilidade e infinita quando o proprio hotel e retornado no conjunto de features. Entao ignoramos ele.
					if (!(probability == Double.NEGATIVE_INFINITY)) {

						if (probability > maxProb) {
							maxProb = probability;
						}

						if (probability < minProb) {
							minProb = probability;
						}
					}
				}
			}

//			System.out.println("Max: " + maxProb);
//			System.out.println("MIN: " + minProb);
			
			it = topK.iterator();

			while (it.hasNext()) {

				obj = it.next();

				if (!obj.getBestNeighbor().getName().equals("empty")) {
					dist = SpatialQueryLD.distFrom(Double.parseDouble(obj.getBestNeighbor().getLat()),
							Double.parseDouble(obj.getBestNeighbor().getLgt()), Double.parseDouble(obj.getLat()),
							Double.parseDouble(obj.getLgt()));

					// probability é somado ao score para re-ordenar o rank
					probability = par.logDensity(dist);

					// Sometimes the probability will be negative infinite. Here we deal with this
					// ignoring it.
					if (probability == Double.NEGATIVE_INFINITY) {
						probability = maxProb;
					}

					double normProb = (probability - minProb) / (maxProb - minProb);

					obj.setScore((normProb / 2 + obj.getScore()));

					pTopK.add(obj);
				}
			}

			try {
				if (!pTopK.isEmpty()) {
					// saveGroupResults(pTopK);
					saveResultsBN(pTopK);
				}
				// Retirei porque o NDCG não me interessa por agora
				// evaluateQuery(keywords, null, k);
			} catch (IOException e) {
				e.printStackTrace();
			}

			reader.close();
			return pTopK;

		} else {
			// this function is just to mantain the rank order. SKPQ and Pareto have the
			// same query order when SKPQ does not find textually relevant results.
			it = topK.iterator();
			int id = 0;
			while (it.hasNext()) {
				obj = it.next();
				obj.setId(id);
				id--;
				pTopK.add(obj);
			}
			saveResultsBN(pTopK);
			reader.close();
			return pTopK;
		}
	}

	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {

		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("pskpq/Pareto-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= topK.size(); a++) {

			SpatialObject obj = it.next();

			if (debug) {
				System.out.println("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat()
						+ ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
			}

			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}

	protected void saveResultsBN(TreeSet<SpatialObject> topK) throws IOException {

		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("pskpq/Pareto-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= topK.size(); a++) {

			SpatialObject obj = it.next();

			if (debug) {
				System.out.println("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat()
						+ ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
			}

			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");

			outputFile.write("[BN]  " + "[OSMlabel=" + obj.getBestNeighbor().getName() + ", lat="
					+ obj.getBestNeighbor().getLat() + ", lgt=" + obj.getBestNeighbor().getLgt() + ", score="
					+ obj.getBestNeighbor().getScore() + "]\n");
		}

		outputFile.close();
	}

	/*
	 * Don't use saveGroupResults in personalized queries! Passando os top-20 vai
	 * colocar valores que nao deveria estar no top-5 mas que estao no top 20 Quando
	 * personaliza, o elemento que estava no rank 20 pode subir para top-5, o que
	 * não é desejado. Rank-reorder os top-5 devem ser mantidos apos personalização
	 */

	@Override
	public void printQueryName() {
		System.out.println(
				"\nk = " + k + " | keywords = [ " + keywords + " ]" + " | type = range [radius = " + radius + "]\n\n");
	}
}