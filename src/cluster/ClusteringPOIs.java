package cluster;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.math3.distribution.ParetoDistribution;

import skpq.SpatialQueryLD;
import weka.clusterers.SimpleKMeans;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ClusteringPOIs {

	private BufferedReader reader;
	private Instances data;

	public ClusteringPOIs(String poiDataset) throws IOException, FileNotFoundException {
		open(poiDataset);

	}

	private void open(String poiDataset) throws IOException, FileNotFoundException {

		reader = new BufferedReader((new InputStreamReader(
				new FileInputStream(new File("./profiles/check-ins/New York/util/coordinates.txt")), "ISO-8859-1")));

		data = new Instances(reader);

	}

	public void loadSequentialCheckIns(String poiDataset) throws Exception {

		//Create the clusters
		SimpleKMeans kmeans = new SimpleKMeans();

		kmeans.setSeed(0);
		kmeans.setNumClusters(50);

		kmeans.buildClusterer(data);
		
		File folder = new File("./profiles/check-ins/"+ poiDataset + "/");

		// Warn: files are not ordered
		File[] profiles = folder.listFiles();

		Writer writer = new OutputStreamWriter(
				new FileOutputStream("./profiles/check-ins/" + poiDataset + "/util/sequentialPOIs.txt", false),
				"ISO-8859-1");
		
		ParetoDistribution par = new ParetoDistribution();
		
		Writer par_wrt = new OutputStreamWriter(
				new FileOutputStream("./profiles/check-ins/" + poiDataset + "/util/paretoProbability.txt", false),
				"ISO-8859-1");
		
		for (int a = 0; a < profiles.length; a++) {
			if (profiles[a].isFile()) {
				
				reader = new BufferedReader((new InputStreamReader(new FileInputStream(profiles[a]), "ISO-8859-1")));			

				String line = reader.readLine();

				String[] lineVec = line.split("\\t");

				String dateString = lineVec[1];
				
				String[] firstLocation = lineVec[4].split(",");
				String lat = firstLocation[0].split("\\{")[1];
				String lgt = firstLocation[1];
				double[] firstCoords = {Double.parseDouble(lat), Double.parseDouble(lgt)};
				
				// Date pattern in user profile
				String pattern = "EEE MMM dd HH:mm:ss Z yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);

				while (line != null) {
					// Create a date object from the string in user profile
//					Date date = sdf.parse(dateString);
					// This line is needed to conserve the hour during parse. Without this, the time
					// will be converted to local computer time zone.
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

					int dateThreshold = 25;

					// Next POI visit date
					line = reader.readLine();

					if (line == null) {
						break;
					}

					lineVec = line.split("\\t");
					
					String[] secondLocation = lineVec[4].split(",");
					String secLat = secondLocation[0].split("\\{")[1];
					String secLgt = secondLocation[1];
					double[] secondCoords = {Double.parseDouble(secLat), Double.parseDouble(secLgt)};
					
					String nextDateString = lineVec[1];
//					Date nextDate = sdf.parse(nextDateString);				

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
					formatter = formatter.withLocale(Locale.ENGLISH);

					LocalDate firstDate = LocalDate.parse(dateString, formatter);
					LocalDate secondDate = LocalDate.parse(nextDateString, formatter);

					if (Math.abs(ChronoUnit.DAYS.between(firstDate, secondDate)) < dateThreshold) {
						System.out.println("DAYS: " + Math.abs(ChronoUnit.DAYS.between(firstDate, secondDate)));
						System.out.println("Date occurs inside the time gap");

						//conta coloca aqui
						//first POI as Instance object
						Instance p1 = new Instance(1, firstCoords);						
						
						int cent_index = kmeans.clusterInstance(p1);
												
//						System.out.println("Centroid: " + kmeans.getClusterCentroids().instance(cent_index));
						
						double[] centroid = {kmeans.getClusterCentroids().instance(cent_index).value(0), kmeans.getClusterCentroids().instance(cent_index).value(1)};					
						
						double dist = SpatialQueryLD.distFrom(centroid[0], centroid[1],
								secondCoords[0], secondCoords[1]);
						
						double probability = par.logDensity(dist);
						
						System.out.println("Dist: " + dist);
						System.out.println("Prob: " + probability);
						
						writer.write(dist + "\n");
						par_wrt.write(probability + "\n");
					} 					
					dateString = nextDateString;					
				}				
				reader.close();
			}else {
				System.out.println("Folder identified! Name: " + profiles[a].getName());
			}		
		}
		writer.flush();
		writer.close();
		
		par_wrt.flush();
		par_wrt.close();
	}

	public void cluster() throws Exception {

		SimpleKMeans kmeans = new SimpleKMeans();

		kmeans.setSeed(0);
		kmeans.setNumClusters(50);

		kmeans.buildClusterer(data);

//		Instances cent = kmeans.getClusterCentroids();			
	}

	public static void main(String[] args) throws Exception {
		
//		ClusteringPOIs x = new ClusteringPOIs(" ");
//
//		x.loadSequentialCheckIns("New York");
//		File reader = new File("./profiles/check-ins/New York/");
//
//		System.out.println(reader.listFiles()[1].getName());
	}
}
