package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Cache used to store Web content that has access limits Cache usually used to
 * improve query performance, storing the textual descriptions from objects at
 * LinkedGeoData and DBpedia.
 * 
 * @author João Paulo
 */

public class WebContentCache {

	private HashMap<String, String> cache;
	private String cacheFileName;
	private boolean debug = false;

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
			if (debug) {
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

			try (FileInputStream fis = new FileInputStream(cacheFileName);
					ObjectInputStream reader = new ObjectInputStream(fis)) {

				cache = (HashMap<String, String>) reader.readObject();

				if (debug)
					System.out.println("\nINFO: Cache loaded successfully...\n\n");

				reader.close();
				fis.close();

				if (cache.isEmpty()) {
					System.out.println("\nWARNING: The cache is empty. It is you first time executing the SKPQ query? This first execution may be slow.\n\n");
					System.out.println("Exiting the program ...");
					System.exit(0);
					return;
				}
			} catch (java.io.EOFException e) {
				System.out.println("\nWARNING: Empty cache");
				System.out.println("Exiting the program ...");
				System.exit(0);
				return;
			}
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

		Writer fileWrt = new OutputStreamWriter(new FileOutputStream(cacheFileName + ".exported", true), "ISO-8859-1");

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

	// Values fixed manually in the code
	public void removeValues() throws IOException, FileNotFoundException {

		BufferedReader exportedCache = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File("./madrid/ratings.ch.exported")), "ISO-8859-1")));

		String line = exportedCache.readLine();

		if (!cache.isEmpty()) {

			while (line != null) {

				String key = line.substring(line.indexOf("key: ") + 5, line.indexOf("|")).trim();

				if (line.contains("0.0")) {
					System.out.println("Key 0.0: " + key);
					cache.remove(key);
				} else if (line.contains("0.1")) {
					System.out.println("Key 0.1: " + key);
					cache.remove(key);
				} else if (line.contains("-1.0")) {
					System.out.println("Key -1.0: " + key);
					cache.remove(key);
				}
				line = exportedCache.readLine();
			}

			store();

			exportedCache.close();

			System.out.println("Values removed.");
		} else {
			System.out.println("WARNING: Cache not loaded!");
			exportedCache.close();
		}
	}

	public void mergecache(String cacheFileName) throws IOException, ClassNotFoundException {

		load();

		// load the cache to merge
		FileInputStream fis = new FileInputStream(cacheFileName);
		ObjectInputStream reader = new ObjectInputStream(fis);

		@SuppressWarnings("unchecked")
		HashMap<String, String> secondCache = (HashMap<String, String>) reader.readObject();

		reader.close();
		fis.close();

		if (secondCache.isEmpty()) {
			System.out.println("\nWARNING: You are trying to merge an empty cache. Exiting...\n\n");
			System.exit(0);
		}
		// end loading

		@SuppressWarnings("rawtypes")
		Set set = secondCache.entrySet();
		@SuppressWarnings("rawtypes")
		Iterator iterator = set.iterator();

		while (iterator.hasNext()) {

			@SuppressWarnings("rawtypes")
			Map.Entry mentry = (Map.Entry) iterator.next();

			if (!cache.containsKey(mentry.getKey())) {
				cache.put(mentry.getKey().toString(), mentry.getValue().toString());
			}
		}
		store();
	}

	public HashMap<String, String> getObject() {
		return cache;
	}

	public static void main(String[] args) throws IOException {

//		String lat = "51.5147591";
//		String lgt = "-0.1648277";
//		
//		boolean lat2 = lat.regionMatches(0, Double.toString(51.5148056), 0, 5);
//		boolean lgt2 =  lgt.regionMatches(0, Double.toString(-0.1647972), 0, 5);
//		
//		System.out.println(lat2);
//		System.out.println(lgt2);

		WebContentCache cache = new WebContentCache("descriptions.ch");

//		try {
//			cache.mergecache("descriptionsBerlin.ch");
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		System.out.println(cache.containsKey("null"));

		cache.load();
//		cache.exportCache();
//		cache.removeValues();
		// cache.putDescription("Test", "Description");

		// cache.store();

		cache.exportCache();

//		cache.printCache();		
	}
}
