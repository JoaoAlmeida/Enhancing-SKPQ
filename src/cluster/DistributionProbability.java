package cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.ParetoDistribution;

import skpq.SpatialObject;
import skpq.SpatialQueryLD;
import skpq.util.WebContentArrayCache;

public class DistributionProbability {

	private static ArrayList<SpatialObject> loadObjectsInterest(String inputFileName) throws IOException {

		ArrayList<SpatialObject> objectsInterest = new ArrayList<>();
		SpatialObject obj;

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8")));

		String line = reader.readLine();

		int i = 1;

		while (line != null) {
			String uri = line.substring(line.indexOf("http") - 1).trim();

			String osmLabel = "(tourism) (hotel) "
					+ line.substring(line.indexOf(" ", line.indexOf(" ") + 1), line.indexOf("http") - 1).trim();

			String[] lineVec = line.split(" ");

			String lat = lineVec[0];
			String lgt = lineVec[1];
			obj = new SpatialObject(i, osmLabel, uri, lat, lgt);
			objectsInterest.add(obj);
			line = reader.readLine();

			i++;
		}
		reader.close();
		return objectsInterest;
	}

	public static void getDistribution() throws IOException {
		
		Writer writer = new OutputStreamWriter(new FileOutputStream("./datasetsOutput/distancesNewYork.txt", true),
				"ISO-8859-1");

		ArrayList<SpatialObject> pois = loadObjectsInterest("./newyork/NewYorkLGD.txt");

		for (int a = 0; a < pois.size(); a++) {

			WebContentArrayCache featuresCache = new WebContentArrayCache("./newyork/pois/POI[" + a + "].cache", 0.01);
			featuresCache.load();

			ArrayList<SpatialObject> featureSet = featuresCache.getArray(pois.get(a).getURI());

			// compute the distance for each feature
			for (int b = 0; b < featureSet.size(); b++) {			
				
				double dist = SpatialQueryLD.distFrom(Double.parseDouble(featureSet.get(b).getLat()),
						Double.parseDouble(featureSet.get(b).getLgt()), Double.parseDouble(pois.get(a).getLat()),
						Double.parseDouble(pois.get(a).getLgt()));

				ParetoDistribution par = new ParetoDistribution();

				double probability = par.logDensity(dist);				
//				double probability = par.density(dist);
				
				if (probability != Double.NEGATIVE_INFINITY) {
					String line = dist + " " + probability;
					writer.append(line + "\n");
					writer.flush();
				}
			}		
		}
		
		writer.close();
	}
	
	public static void main(String[] args) throws IOException {

		getDistribution();
	}
	
	public static void normalizedDistribution() throws IOException {

		Writer writer = new OutputStreamWriter(new FileOutputStream("./datasetsOutput/unico.txt", true),
				"ISO-8859-1");

		ArrayList<SpatialObject> pois = loadObjectsInterest("./london/LondonLGD.txt");

		for (int a = 3; a < 4; a++) {

			WebContentArrayCache featuresCache = new WebContentArrayCache("./london/pois/POI[" + a + "].cache", 0.01);
			featuresCache.load();

			ArrayList<SpatialObject> featureSet = featuresCache.getArray(pois.get(a).getURI());

			double maxProb = -Double.MAX_VALUE;
			double minProb = Double.MAX_VALUE;
			
			for (int b = 0; b < featureSet.size(); b++) {
				
				double dist = SpatialQueryLD.distFrom(Double.parseDouble(featureSet.get(b).getLat()),
						Double.parseDouble(featureSet.get(b).getLgt()), Double.parseDouble(pois.get(a).getLat()),
						Double.parseDouble(pois.get(a).getLgt()));

				ParetoDistribution par = new ParetoDistribution();
				
				double probability = par.logDensity(dist);

				//acontece quando feature == POI
				if (probability == Double.NEGATIVE_INFINITY) {
					featureSet.get(b).setParetoProbability(Double.NEGATIVE_INFINITY);					
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
			
			// compute the distance for each feature
			for (int b = 0; b < featureSet.size(); b++) {							
				
				double dist = SpatialQueryLD.distFrom(Double.parseDouble(featureSet.get(b).getLat()),
						Double.parseDouble(featureSet.get(b).getLgt()), Double.parseDouble(pois.get(a).getLat()),
						Double.parseDouble(pois.get(a).getLgt()));

				ParetoDistribution par = new ParetoDistribution();

//				double probability = par.logDensity(dist);				
//				double probability = par.density(dist);			
				
				if (!(featureSet.get(b).getParetoProbability() == Double.NEGATIVE_INFINITY)) {

					double normProb = (featureSet.get(b).getParetoProbability() - minProb) / (maxProb - minProb);
					
					String line = dist + " " + normProb;
					
					writer.append(line + "\n");
					writer.flush();	
				}
				
//				if (probability != Double.NEGATIVE_INFINITY) {
//					String line = dist + " " + probability;
//					writer.append(line + "\n");
//					writer.flush();
//				}
			}			
		}

		writer.close();
	}

}
