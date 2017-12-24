package skpq.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 	Cache used to store Web content that has access limits
 *  Cache usually used to improve query performance, storing the textual descriptions from objects at LinkedGeoData and DBpedia.
 * 
 * @author  João Paulo
 */

public class WebContentCache {
	
	private HashMap<String, String> cache;
	private String cacheFileName;

	public WebContentCache(String cacheFileName) {
		this.cacheFileName = cacheFileName;
		
		cache = new HashMap<String, String>();
	}

	public void putDescription(String uri, String description) {
		cache.put(uri, description);
	}

	public String getDescription(String uri) {
		return cache.get(uri);
	}

	public void store() throws IOException {

		FileOutputStream fostr = new FileOutputStream(cacheFileName);
		ObjectOutputStream writer = new ObjectOutputStream(fostr);

		try {
			writer.writeObject(cache);
		} finally {
			writer.close();
			fostr.close();
		}
	}

	public void load() throws IOException {
		
		try {
			
			FileInputStream fis = new FileInputStream(cacheFileName);
			ObjectInputStream reader = new ObjectInputStream(fis);
			
			cache = (HashMap<String, String>) reader.readObject();
			
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

	public boolean containsKey(String uri) {
		return cache.containsKey(uri);
	}

	public HashMap<String, String> getObject() {
		return cache;
	}

	public static void main(String[] args) throws IOException {
		
		WebContentCache cache = new WebContentCache("ratings_dubai.ch");
		
		System.out.println(cache.containsKey("null"));
		
		cache.load();
		
		//cache.putDescription("Test", "Description");
		 
		//cache.store();

//		cache.printCache();
		
		if(cache.containsKey("Test")){
			System.out.println("It is working.");				
		}		
	}
}
