package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import skpq.SpatialObject;
import skpq.SpatialQueryLD;



/**
 *  WARN: cache must be used only when using the same query radius. The cache is not prepared to store different query results from different query radius. This issue
 *  might be resolved in future versions.
 *   
 * 	Cache used to store Web content that has access limits
 *  Cache usually used to improve query performance, storing the textual descriptions from objects at LinkedGeoData and DBpedia.
 * 
 * @author  Joao Paulo
 */

public class WebContentArrayCache {
	
	//hashmap pode ser removido. Troque apenas por um ArrayList de SpatialObjects. Cada arquivo armazena o conjunto de features, o nome do arquivo identifica o POI
	private HashMap<String, ArrayList<SpatialObject>> cache;
	private String cacheFileName;
	private boolean debug = false;
	private final double radius = 0.2;

	public WebContentArrayCache(String cacheFileName, double radius) {
		this.cacheFileName = cacheFileName;
		
		cache = new HashMap<String, ArrayList<SpatialObject>>();
		
		if(this.radius != radius) {
			System.out.println("The cache contains only query results obtained using query radius of " + radius + " km");
		}
	}

	public void putArray(String uri, ArrayList<SpatialObject> poiSet) {
		cache.put(uri, poiSet);
	}

	public ArrayList<SpatialObject> getArray(String uri) {
		return cache.get(uri);
	}

	public void store() throws IOException {

		FileOutputStream fostr = new FileOutputStream(cacheFileName);
		ObjectOutputStream writer = new ObjectOutputStream(fostr);

		try {
			writer.writeObject(cache);
			if(debug){
				System.out.println("\n[Cache stored successfully!]");	
			}			
		} finally {			
			writer.close();
			fostr.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void load() throws IOException {
		
		try {
			
			FileInputStream fis = new FileInputStream(cacheFileName);
			ObjectInputStream reader = new ObjectInputStream(fis);
			
			cache = (HashMap<String, ArrayList<SpatialObject>>) reader.readObject();
			
			if(debug)
			System.out.println("\nINFO: Cache loaded successfully...\n\n");
			
			reader.close();
			fis.close();

		} catch (IOException ioe) {
			System.out.println("\nWARNING: The cache is empty. It is you first time executing the SKPQ query? This first execution may be slow.\n\n");
			store();
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found");
			e.printStackTrace();
			return;
		}
	}

	@SuppressWarnings("rawtypes")
	public void printCache() {

		if (!cache.isEmpty()) {

			Set set = cache.entrySet();
			Iterator iterator = set.iterator();

			int i = 0;

			while (iterator.hasNext()) {

				i++;
				Map.Entry mentry = (Map.Entry) iterator.next();

				System.out.print("Object " + i + " -- key: " + mentry.getKey() + " | Value: ");
				System.out.println(mentry.getValue());
			}
		} else {
			System.out.println("WARNING: Cache is empty! There is nothing to print.");
		}
	}

	public void exportCache() throws IOException {

		Writer fileWrt = new OutputStreamWriter(new FileOutputStream(cacheFileName+".exported", true), "ISO-8859-1");
		
		if (!cache.isEmpty()) {

			@SuppressWarnings("rawtypes")
			Set set = cache.entrySet();
			@SuppressWarnings("rawtypes")
			Iterator iterator = set.iterator();

			int i = 0;

			while (iterator.hasNext()) {

				i++;
				@SuppressWarnings("rawtypes")
				Map.Entry mentry = (Map.Entry) iterator.next();
				
				String line = "Object " + i + " -- key: " + mentry.getKey() + " | Value: " + mentry.getValue() + "\n";	
				
				fileWrt.write(line);				
			}
			fileWrt.close();
		} else {
			System.out.println("WARNING: Cache is empty! There is nothing to export.");
		}
	}
	
	public boolean containsKey(String uri) {
		return cache.containsKey(uri);
	}

	public HashMap<String, ArrayList<SpatialObject>> getObject() {
		return cache;
	}

	public void restoreCache() throws IOException {
		load();
		
		
	}
	
	public static void main(String[] args) throws IOException {
		
		
//		BufferedReader reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File("hotelLondon_LGD.txt")), "UTF-8")));
//		
//		String line = reader.readLine();
//		int count = 0;
//		while (line != null) {
//			
//			String uri = line.substring(line.indexOf("http") - 1).trim();
//			WebContentArrayCache cache = new WebContentArrayCache("./pois/POI["+count+"].cache", 0.2);
//			cache.load();
//			
//			ArrayList<SpatialObject> set = cache.getArray(uri);
//			
//			Iterator<SpatialObject> it = set.iterator();
//			
//			while(it.hasNext()) {
//				SpatialObject obj = it.next();
//				System.out.println("Feature: " + obj.getCompleteDescription());
//				if(obj.getLat().contains(")")){
//					System.out.println("problema encontrado!!");
//					System.out.println("Cache ID:" + count);
//					System.out.println("URI POI: " + uri);
//					
//					System.exit(0);
//					break;
//				}
//			}			
//			count++;
//			line = reader.readLine();
//			break;
//		}
//		
//		reader.close();
		
		WebContentArrayCache cache = new WebContentArrayCache("./pois/POI[0].cache", 0.2);
//		System.out.println(cache.containsKey("null"));
		
		cache.load();
		
		//cache.putDescription("Test", "Description");
		 
		//cache.store();

//		cache.exportCache();
		
//		cache.printCache();
		
		//print Array
		ArrayList<SpatialObject> set = cache.getArray("http://linkedgeodata.org/triplify/node262778");
		
		Iterator<SpatialObject> it = set.iterator();
		
		while(it.hasNext()) {
			SpatialObject obj = it.next();
			System.out.println(obj.getCompleteDescription());
			
			double dist = SpatialQueryLD.distFrom(Double.parseDouble("50.9905"),
					Double.parseDouble("-0.825627"), Double.parseDouble(obj.getLat()),Double.parseDouble(obj.getLgt()));
			
			System.out.println(dist);
			
			if(obj.getLat().contains(")")){
				System.out.println("problema encontrado!!");
								
//				System.out.println("Feature: " + obj.getCompleteDescription());
				System.exit(0);
				break;
			}

		}
	
		
	}
}
