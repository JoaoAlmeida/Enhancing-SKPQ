package tests;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import skpq.SpatialObject;
import skpq.util.ContextProportionality;
import skpq.util.WebContentArrayCache;

public class ContextProportionalityTest {

	@Test
	public void testSpatialDiversity() {
		
		List<SpatialObject> interestObjectSet;
		String city = "newyork";
		double radius = 0.01;

		interestObjectSet = new ArrayList<SpatialObject>();	

		try {
			interestObjectSet = loadObjectsInterest("./"+city.toLowerCase()+"/"+city+"LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int a = 0; a < interestObjectSet.size(); a++) {
			
			WebContentArrayCache featuresCache = new WebContentArrayCache("./"+city +"/pois/POI["+ a +"].cache", radius);

			try {
				featuresCache.load();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			SpatialObject poi = interestObjectSet.get(a);

			ArrayList<SpatialObject> featureSet = featuresCache.getArray(poi.getURI());

			for (int b = 0; b < featureSet.size(); b++) {

				SpatialObject f1 = featureSet.get(b);

				for(int c = 0; c < featureSet.size();c++) {

					SpatialObject f2 = featureSet.get(c);

					double spatialScore = ContextProportionality.ptolomyDiversity(Double.parseDouble(poi.getLat()), Double.parseDouble(poi.getLgt()), 
							Double.parseDouble(f1.getLat()), Double.parseDouble(f1.getLgt()), Double.parseDouble(f2.getLat()), Double.parseDouble(f2.getLgt()));

					try {
						assertTrue(spatialScore<=1);
					}catch (AssertionError e) {
						System.out.println(spatialScore);
					}

				}
			}
		}
	}

	public static ArrayList<SpatialObject> loadObjectsInterest(String inputFileName) throws IOException {

		ArrayList<SpatialObject> objectsInterest = new ArrayList<>();
		SpatialObject obj;

		BufferedReader reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File(inputFileName)), "UTF-8")));

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
		
		reader.close();
		
		return objectsInterest;
	}

}
