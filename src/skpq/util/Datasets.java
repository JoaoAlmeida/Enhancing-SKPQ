package skpq.util;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Stream;

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
import org.apache.jena.sparql.function.library.leviathan.e;

import node.Sparql;
import skpq.SpatialObject;
import skpq.SpatialQueryLD;

public class Datasets {

	BufferedReader reader;
	public static char quotes = '"';
	public static boolean USING_GRAPH = false;
	private static boolean debug = true;
	static Model model = getTestModel();
	static String arquivoOSMLinkado = "features_linked.txt";
	// private static org.apache.log4j.Logger log = Logger.getLogger(); //create log
	protected String file;
	public int featureID;

	public Datasets(String file) throws UnsupportedEncodingException, FileNotFoundException {
		this.file = file;
		reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File(file)), "ISO-8859-1")));
	}

	public Datasets() {
		this.featureID = 0;
	}

	public static Model getTestModel() {

		Model model = ModelFactory.createDefaultModel();
		return model;

	}

	public TreeSet<SpatialObject> loadResultstoPersonalize(String queryName, String keyword, int k) throws IOException {

		System.out.println("Loading Results...");

		TreeSet<SpatialObject> topk = new TreeSet<>();
		SpatialObject obj;

		String fileName = queryName + "-LD [k=" + k + ", kw=" + keyword + "].txt";

		BufferedReader read = new BufferedReader((new InputStreamReader(
				new FileInputStream(new File("skpq/frequent/default/" + fileName)), "ISO-8859-1")));

		String line = read.readLine();

		int i = 1;

		while (line != null) {

			String osmLabel = line.substring(line.indexOf("(tourism) (hotel)"), line.indexOf(", lat")).trim();

			String lat = line.substring(line.indexOf("lat=") + 4, line.indexOf(", lgt")).trim();
			String lgt = line.substring(line.indexOf("lgt=") + 4, line.indexOf(", score=")).trim();
			String score[] = line.split("score=")[1].split("]");
			obj = new SpatialObject(i, osmLabel, null, lat, lgt);
			obj.setScore(Double.parseDouble(score[0]));
			topk.add(obj);
			line = read.readLine();

			i++;
		}
		read.close();
		return topk;
	}

	// Load results with POIs containing the best neighbor
	public TreeSet<SpatialObject> loadResultstoReOrderBN(String queryName, String keyword, int k)
			throws IOException {

		System.out.println("Loading Results...");

		TreeSet<SpatialObject> topk = new TreeSet<>();
		SpatialObject obj;

		String fileName = queryName + "-LD [k=" + k + ", kw=" + keyword + "].txt";

		BufferedReader read = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File("./skpq/" + fileName)), "ISO-8859-1")));

		String line = read.readLine();

		int i = 1;

		while (line != null) {

			String osmLabel = line.substring(line.indexOf("(tourism) (hotel)"), line.indexOf(", lat")).trim();

			String lat = line.substring(line.indexOf("lat=") + 4, line.indexOf(", lgt")).trim();
			String lgt = line.substring(line.indexOf("lgt=") + 4, line.indexOf(", score=")).trim();
			String score[] = line.split("score=")[1].split("]");
			obj = new SpatialObject(i, osmLabel, null, lat, lgt);
			obj.setScore(Double.parseDouble(score[0]));

			/* Collect the Best Neighbor (BN) */
			line = read.readLine();

			String bnLabel = line.substring(line.indexOf("OSMlabel=") + 9, line.indexOf(", lat")).trim();
			String bnLat = line.substring(line.indexOf("lat=") + 4, line.indexOf(", lgt")).trim();
			String bnLgt = line.substring(line.indexOf("lgt=") + 4, line.indexOf(", score=")).trim();

			SpatialObject bn = new SpatialObject(0, bnLabel, null, bnLat, bnLgt);
			bn.setScore(Double.parseDouble(score[0]));
			obj.setBestNeighbor(bn);

			topk.add(obj);

			// New POI
			line = read.readLine();

			i++;
		}
		read.close();
		return topk;
	}

	// Create a file with LGD links to OSM objects
	public void interestObjectCreateFile(String nomeArquivo) throws IOException {

		System.out.println("Creating the POIs File...");

		Writer fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + nomeArquivo, true),
				"ISO-8859-1");

		String line = reader.readLine();

		while (line != null) {

			String[] lineVec = line.split(" ");
			
			String rawLat = lineVec[1];
			String rawlgt = lineVec[2];

			String lat = rawLat.substring(0, rawLat.indexOf('.') + 3);
			String lgt = rawlgt.substring(0, rawlgt.indexOf('.') + 3);

			String label;

			/*
			 * if(lineVec[3].equals("-")){ label = line.split("-")[1].trim();
			 * System.out.println("Label = " + label); }
			 */

			int fim = line.indexOf(')');

			int inicioSub = line.indexOf('(', fim + 1);
			int fimSub = line.indexOf(')', inicioSub + 1);

			// String subCategoria = line.substring(inicioSub + 1, fimSub);

			label = line.substring(fimSub + 1).replace('"', ' ').trim();

			if (!label.equals(" ")) {
				/// "http://linkedgeodata.org/vsparql";
				String link = getOSMObject("http://linkedgeodata.org/sparql", line, label, lat, lgt);

				fileWrt.append(rawLat + "\t" + rawlgt + "\t" + label + "\t" + link + "\n");

				fileWrt.close();

				fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + nomeArquivo, true),
						"ISO-8859-1");
			}
			line = reader.readLine();
		}
		System.out.println("\nFile created!");
		
		System.out.println("\nFile created!");

		fileHeallthCheck("./DatasetsOutput/"+nomeArquivo);
		
		System.out.println("\nFile checked!");
	}

	// usando tab para separar os valores
	public void interestObjectCreateFileTAB(String nomeArquivo) throws IOException {

		System.out.println("Creating the POIs File...\n");

		Writer fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + nomeArquivo, true),
				"ISO-8859-1");

		String line = reader.readLine();

		while (line != null) {

			String[] lineVec = line.split("\t");

			if (lineVec.length == 5) {
				
				String lat = lineVec[1];
				String lgt = lineVec[2];

				String label = lineVec[4];

//				if (!label.equals(" ")) {
					/// "http://linkedgeodata.org/vsparql";
//				System.out.println(label + " " + lat + " " + lgt);
					String link = getOSMObject2("http://linkedgeodata.org/sparql", line, label, lat, lgt);

					fileWrt.append(lat + "\t" + lgt + "\t" + label + "\t" + link + "\n");

					fileWrt.close();

					fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + nomeArquivo, true),
							"ISO-8859-1");
//				}
			}
			line = reader.readLine();
		}
		System.out.println("\nFile created!");

		fileHeallthCheck("./datasetsOutput/"+nomeArquivo);
		
		System.out.println("\nFile checked!");
	}

	// usando outra query mais abrangente (nao foi completamente comprovado a
	// abrangecia)
	public static String getOSMObject2(String service, String line, String label, String lat, String lon)
			throws IOException {

		if (debug)
			System.out.println(label + " " + lat + " " + lon + " ");

		// String serviceURI = "http://linkedgeodata.org/vsparql";
		String serviceURI = service;

//		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { ?var rdfs:label "
//				+ quotes + label + quotes + "." + "?var geo:lat ?lat." + "?var geo:long ?lon." + "}"
//				+ Sparql.addServiceClosing(USING_GRAPH);

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI)
				+ "SELECT DISTINCT ?link ?sourcegeo WHERE { " + "?link rdfs:label" + quotes + label + quotes + "."
				+ "?link <http://geovocab.org/geometry#geometry> ?t."
				+ "?t <http://www.opengis.net/ont/geosparql#asWKT> ?sourcegeo.}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		System.out.println(queryString);
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("9000000000000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);

			try {
				ResultSet rs = qexec.execSelect();

				for (; rs.hasNext();) {

					QuerySolution rb = rs.nextSolution();

					RDFNode uri = rb.get("link");
					RDFNode location = rb.get("sourcegeo");
//					RDFNode nodeLon = rb.get("lon");
					String flat, flgt;

					if (uri.isResource()) {

//						Resource r = (Resource) uri;

						String[] array = location.asLiteral().getString().split(",")[0].split(" ");
						flgt = array[0].split("\\(")[1];
						flat = array[1];

						if (flat.contains(")")) {
							flat = lat.split("\\)")[0];
						}

						if (flat.contains(lat.substring(0, 5)) && flgt.contains(lon.substring(0, 5))) {

//							Writer fileWrt = new OutputStreamWriter(
//									new FileOutputStream("./DatasetsOutput/" + arquivoOSMLinkado, true), "ISO-8859-1");
//
//							fileWrt.append(line + " " + uri.asResource().getURI().toString() + "\n");
//							fileWrt.flush();
//							fileWrt.close();

							return uri.asResource().getURI().toString();
						}
					}
				}
			} finally {
				qexec.close();
			}
		}
		return "Vazio";
	}

	public static String getOSMObject(String service, String line, String label, String lat, String lon)
			throws IOException {

		if (debug)
			System.out.println(label + " " + lat + " " + lon + " ");

		// String serviceURI = "http://linkedgeodata.org/vsparql";
		String serviceURI = service;

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { ?var rdfs:label "
				+ quotes + label + quotes + "." + "?var geo:lat ?lat." + "?var geo:long ?lon." + "}"
				+ Sparql.addServiceClosing(USING_GRAPH);

		System.out.println(queryString);
		Query query = QueryFactory.create(Sparql.addPrefix().concat(queryString));

		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {

			Map<String, Map<String, List<String>>> serviceParams = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			values.add("9000000000000");
			params.put("timeout", values);
			serviceParams.put(serviceURI, params);
			qexec.getContext().set(ARQ.serviceParams, serviceParams);

			try {
				ResultSet rs = qexec.execSelect();

				for (; rs.hasNext();) {

					QuerySolution rb = rs.nextSolution();

					RDFNode uri = rb.get("var");
					RDFNode nodeLat = rb.get("lat");
					RDFNode nodeLon = rb.get("lon");

					if (nodeLat.isLiteral() && nodeLon.isLiteral()) {
						String objLat = nodeLat.asLiteral().getString();
						String objLon = nodeLon.asLiteral().getString();

						float flat = Float.parseFloat(objLat);
						float flon = Float.parseFloat(objLon);

						objLat = Float.toString(flat);
						objLon = Float.toString(flon);

						if ((objLat.contains(lat) && objLon.contains(lon))
								|| lat.contains(objLat) && lon.contains(objLon)) {
							if (uri.isResource()) {
								if (debug) {
									System.out.print(uri.asResource().getURI().toString());
									System.out.println();
								}

								Writer fileWrt = new OutputStreamWriter(
										new FileOutputStream("./DatasetsOutput/" + arquivoOSMLinkado, true),
										"ISO-8859-1");

								fileWrt.append(line + " " + uri.asResource().getURI().toString() + "\n");
								fileWrt.flush();
								fileWrt.close();

								return uri.asResource().getURI().toString();
							} else {
								System.out.println("URI malformed or inexistent!");
							}
						}
					}
				}
			} finally {
				qexec.close();
			}
		}
		return "Vazio";
	}

	// Work in progress
	public static void osm_matches_URI(String filePath1) throws IOException {

		@SuppressWarnings("resource")
		BufferedReader read = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(filePath1)), "ISO-8859-1")));

		String line = read.readLine();

		while (line != null) {

		}
	}

	// Separate that objects which have a LGD link from those that have not
	public static void fileHeallthCheck(String filePath) throws IOException {

		@SuppressWarnings("resource")
		BufferedReader read = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(filePath)), "ISO-8859-1")));

		Writer rmk = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + "unhealth.txt"), "ISO-8859-1");
		Writer health = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + "health.txt"), "ISO-8859-1");
		String line = read.readLine();

		while (line != null) {

			String[] lineVec = line.split("http:");

			if (lineVec.length > 1) {
				health.write(line + "\n");
			} else {
				rmk.write(line + "\n");
			}
			line = read.readLine();
		}
		health.close();
		rmk.close();
	}

	public void hotelProfiler(String filePath, String hotelName) throws IOException {

		try (Stream<Path> paths = Files.walk(Paths.get("./dubai"))) {
			paths.filter(Files::isRegularFile).forEach(System.out::println);
		}

		BufferedReader read = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(filePath)), "ISO-8859-1")));

		Writer profile = new OutputStreamWriter(new FileOutputStream(hotelName + ".arff"), "ISO-8859-1");

		profile.write("@relation " + hotelName + "\n\n");
		profile.write("@attribute class {1,5}" + "\n");
		profile.write("@attribute description string" + "\n\n");
		profile.write("@data" + "\n");

		String line = read.readLine();

		while (line != null) {
			line = line.replaceAll("'", " ");
			int index = line.indexOf(" ", 4);
			profile.write("1,'" + line.substring(index + 5).trim() + "'\n");
			line = read.readLine();
		}

		read.close();
		profile.close();
	}

	// Read a folder and create a profile hotel for each file in the folder
	public void hotelGroupProfiler() throws IOException {

		String city = "london";
		// File folder = new
		// File("C://Users//JoãoPaulo//Documents//GitHub//Enhancing-SKPQ//london");
		File folder = new File("D://Documents//GitHub//Enhancing-SKPQ//" + city);

		String[] files = new String[folder.listFiles().length];

		files = folder.list();

		for (int i = 0; i < folder.listFiles().length; i++) {
			System.out.println(files[i]);
			System.out.println(city + "/" + files[i]);
			BufferedReader read = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File(city + "/" + files[i])), "ISO-8859-1")));

			Writer profile = new OutputStreamWriter(new FileOutputStream("all/" + files[i] + ".arff"), "ISO-8859-1");

			profile.write("@relation " + files[i] + "\n\n");
			profile.write("@attribute class {1,5}" + "\n");
			profile.write("@attribute description string" + "\n\n");
			profile.write("@data" + "\n");

			String line = read.readLine();

			while (line != null) {
				line = line.replaceAll("'", " ");
				int index = line.indexOf(" ", 4);
				profile.write("1,'" + line.substring(index + 5).trim() + "'\n");
				line = read.readLine();
			}

			read.close();
			profile.close();
		}
	}

	/* ============= Methods regarding the third article: "" ============= */

	// Every profile is named as user profile <profile number>
	public void createUserProfile(String datasetFileName) throws IOException {

		// jump the file header
		String line = reader.readLine();

		line = reader.readLine();

		int numCheckIns = 1;

		while (line != null) {

			String[] lineVec = line.split("\\t");
			String userID = lineVec[0];
			String file = "";

//			String dateTime = lineVec[1];
//			String venueID = lineVec[2];
//			String venueName = lineVec[3];
//			String venueLocation = lineVec[4];
//			String venueCategory = lineVec[5];

			String currentID = userID;

			file = "";
//			System.out.println("\nUser profile #" + currentID);
			while (currentID.equals(userID) && line != null) {

				numCheckIns++;

//				userProfile.write(line + "\n");				
				file = file + line + "\n";

				line = reader.readLine();

				if (line != null) {
					lineVec = line.split("\\t");

					userID = lineVec[0];
				}
			}

			if (numCheckIns > 4) {
//				System.out.println("Building user profile #" + currentID);

				System.out.println(file);
				Writer userProfile = new OutputStreamWriter(new FileOutputStream(
						"profiles\\check-ins\\" + datasetFileName + "\\user profile " + currentID + ".txt", true),
						"ISO-8859-1");

				userProfile.write(file);

				userProfile.flush();
				userProfile.close();
			}
			numCheckIns = 0;
		}
		reader.close();
	}

	// WARN: sdf.format(date) is necessary to print the date object using the same
	// pattern as the user profile
	public void groupCheckinsByDateThreshold(int profileNumber) throws IOException, ParseException {

		reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File(
				"D:\\Documents\\GitHub\\Enhancing-SKPQ\\profiles\\check-ins\\user profile " + profileNumber + ".txt")),
				"ISO-8859-1")));
		Writer writer = new OutputStreamWriter(
				new FileOutputStream("profiles\\check-ins\\new\\user profile " + profileNumber + "-Agruped.txt", true),
				"ISO-8859-1");

		String line = reader.readLine();

		writer.write(line + "\n");

		String[] lineVec = line.split("\\t");

		String dateString = lineVec[1];

		// Date pattern in user profile
		String pattern = "EEE MMM dd HH:mm:ss Z yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);

		while (line != null) {
			// Create a date object from the string in user profile
			Date date = sdf.parse(dateString);
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
			String nextDateString = lineVec[1];
			Date nextDate = sdf.parse(nextDateString);

			System.out.println("Day 1: " + sdf.format(date));
			System.out.println("Day 2: " + sdf.format(nextDate));

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			formatter = formatter.withLocale(Locale.ENGLISH);

			LocalDate firstDate = LocalDate.parse(dateString, formatter);
			LocalDate secondDate = LocalDate.parse(nextDateString, formatter);

			if (Math.abs(ChronoUnit.DAYS.between(firstDate, secondDate)) < dateThreshold) {
				System.out.println("DAYS: " + Math.abs(ChronoUnit.DAYS.between(firstDate, secondDate)));
				System.out.println("Date occurs inside the time gap");

				writer.write(line + "\n");
			} else {
				System.out.println("DAYS: " + Math.abs(ChronoUnit.DAYS.between(firstDate, secondDate)));
				System.out.println("Date occurs OUTSIDE the time gap: " + profileNumber);

				writer.write("===\n");
				writer.write(line + "\n");
			}
			dateString = nextDateString;
		}
		writer.flush();
		writer.close();
		reader.close();
	}

	public void calculteSequentialPOISDistance(String profileName) throws IOException, FileNotFoundException {

		reader = new BufferedReader((new InputStreamReader(
				new FileInputStream(new File(
						"D:\\Documents\\GitHub\\Enhancing-SKPQ\\profiles\\check-ins\\new\\" + profileName + ".txt")),
				"ISO-8859-1")));
		Writer writer = new OutputStreamWriter(
				new FileOutputStream("profiles\\check-ins\\new\\" + profileName + "-Dist.txt", true), "ISO-8859-1");

		String line = reader.readLine();

		while (line != null) {

			String[] lineVec = line.split("\\t");
			String[] venueLocationVec = lineVec[4].split(",");

			String lat = venueLocationVec[0].split("\\{")[1];
			String lgt = venueLocationVec[1];

			line = reader.readLine();

			String distLine = "[";
			while (!line.contains("===")) {

				lineVec = line.split("\\t");
				venueLocationVec = lineVec[4].split(",");

				String lat2 = venueLocationVec[0].split("\\{")[1];
				String lgt2 = venueLocationVec[1];

				// Distance in meters. Distance method online verified at
				// <https://gps-coordinates.org/distance-between-coordinates.php>
				double dist = SpatialQueryLD.distFrom(Double.parseDouble(lat), Double.parseDouble(lgt),
						Double.parseDouble(lat2), Double.parseDouble(lgt2));

				distLine = distLine.concat(Double.toString(dist));

				line = reader.readLine();

				if (line == null) {
					break;
				}
				if (!line.contains("===")) {
					distLine = distLine + ",";
					lat = lat2;
					lgt = lgt2;
				}
			}

			distLine = distLine + "]";
			System.out.println(distLine);
			writer.write(distLine + "\n");
			line = reader.readLine();
		}
		writer.flush();
		writer.close();
		reader.close();
	}

	// Prepare coordinates for analyze in Python
	public void parseSpatialCoordinates() throws IOException, FileNotFoundException {

		TreeSet<String> coords = new TreeSet<>();

		reader = new BufferedReader((new InputStreamReader(
				new FileInputStream(new File("./profiles/check-ins/New York/coordinates.txt")), "ISO-8859-1")));

		Writer writer = new OutputStreamWriter(
				new FileOutputStream("profiles\\check-ins\\New York\\coordinates.txt", true), "ISO-8859-1");

		String line = reader.readLine();
		line = reader.readLine();

		while (line != null) {
//			System.out.println(line);
			String[] lineVec = line.split("\\t");
			String[] venueLocationVec = lineVec[4].split(",");

			String[] aux = venueLocationVec[0].split("\\{");

			if (aux.length > 1) {

				String lat = venueLocationVec[0].split("\\{")[1];
				String lgt = venueLocationVec[1];

				coords.add(lat + " " + lgt + "\n");
//			writer.write(lat + " " + lgt + "\n");						
			} else {
//				lines without coordinates
//				System.out.println(line);
//				writer.write("0" + "\n");
//				
			}
			line = reader.readLine();
		}

		Iterator<String> it = coords.iterator();

		while (it.hasNext()) {
			writer.write(it.next());
		}

		writer.flush();
		writer.close();
		reader.close();

		System.out.println("Parse completed!");
	}

	/*
	 * City options: Los Angeles, San Francisco, San Diego, New York
	 */
	public void splitPOISbyCity(String city) throws IOException {

		Writer writer = new OutputStreamWriter(new FileOutputStream("./datasetsOutput/" + city + ".txt", false),
				"ISO-8859-1");

		String line = reader.readLine();

		line = reader.readLine();

		while (line != null) {
			String[] lineVec = line.split("\\t");
			String[] venueLocationVec = lineVec[4].split(",");

			String poiCity = venueLocationVec[2].trim();

			if (poiCity.equals(city)) {
				writer.write(line + "\n");
			}

			line = reader.readLine();
		}

		writer.flush();
		writer.close();

		reader.close();

		System.out.println("Dataset splitted!");
	}

	// Isolate POI and count its check-ins
	private HashMap<String, SpatialObject> isolatePOI(String foursquareData) throws IOException, FileNotFoundException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(foursquareData)), "ISO-8859-1")));
		BufferedReader aux = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(foursquareData)), "ISO-8859-1")));

		HashMap<String, SpatialObject> objMap = new HashMap<>();

		String line = reader.readLine();

		int id = 0;
//		apagar
//		int lines = 1;
		int t = 0;
		while (line != null) {

			String[] lineVec = line.split("\\t");
			String name = lineVec[3].split("\\{|\\}")[1];
			String[] venueLocationVec = lineVec[4].split(",");

			// Some check-ins does not have the coordinates
			if (venueLocationVec[0].split("\\{").length > 1) {

				String actualPOIAdress = lineVec[4];

				String lat = venueLocationVec[0].split("\\{")[1];
				String lgt = venueLocationVec[1];

				// Count the number of check-ins in the actual POI
				int count = 0;
				String auxLine = aux.readLine();

				SpatialObject obj = new SpatialObject(id, name, "", lat, lgt);

				if (!objMap.containsKey(name)) {

					// Count check-ins
					while (auxLine != null) {

						String[] auxVec = auxLine.split("\\t");
						String auxName = auxVec[3].split("\\{|\\}")[1];

						if (auxVec[4].equals(actualPOIAdress) && name.equals(auxName)) {
							count++;
						}
						auxLine = aux.readLine();
					}

					aux.close();
					aux = new BufferedReader(
							(new InputStreamReader(new FileInputStream(new File(foursquareData)), "ISO-8859-1")));

					obj.setNumberCheckin(count);
					objMap.put(name, obj);
					t++;
//					System.out.println(lines + " " + obj.getName() + " " + obj.getLat() + " " + obj.getLgt() + "," + obj.getNumberCheckin());
				}
			}
			line = reader.readLine();
//			lines++;
		}
		reader.close();
		System.out.println("POIs: " + t);
		return objMap;
	}

	// It does not load category description as default
	private HashMap<String, SpatialObject> loadPOIs(String dataset) throws IOException, FileNotFoundException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(dataset)), "ISO-8859-1")));

		HashMap<String, SpatialObject> objMap = new HashMap<>();

		String line = reader.readLine();

		while (line != null) {

			String[] lineVec = line.split("\t");

			if (lineVec.length > 4) {

				String id = lineVec[0];
				String lat = lineVec[1];
				String lgt = lineVec[2];
				String category = lineVec[3];
				String label = lineVec[4];

//			SpatialObject obj = new SpatialObject(Integer.parseInt(id), category + " " + label, null, lat, lgt);
				SpatialObject obj = new SpatialObject(Integer.parseInt(id), label, null, lat, lgt);
//			System.out.println(obj.getCompleteDescription());
				objMap.put(label, obj);
			}
			line = reader.readLine();
		}

		reader.close();

		return objMap;
	}

	// Remove POIs without label
	public void cleanDataset(String dataset) throws IOException, FileNotFoundException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(dataset)), "ISO-8859-1")));

		Writer writer = new OutputStreamWriter(new FileOutputStream(dataset + "clean.txt", true), "ISO-8859-1");

		String line = reader.readLine();

		while (line != null) {

			String[] lineVec = line.split("\t");

			if (lineVec.length == 4) {
				line = reader.readLine();
			} else {
				writer.append(line + "\n");
			}

			line = reader.readLine();
		}

		reader.close();
		writer.close();
	}

	// Create a file with LGD links to Foursquare check-ins. Ready?
	public void interestObjectCreateFoursquare(String foursquareData, String osmData) throws IOException {

		HashMap<String, SpatialObject> foursquare = isolatePOI(foursquareData);
		HashMap<String, SpatialObject> osm = loadPOIs(osmData);

		Set fSet = foursquare.entrySet();
		Iterator<SpatialObject> i = fSet.iterator();

//			Set osmSet = osm.entrySet();
//			Iterator<SpatialObject> o = osmSet.iterator();

		while (i.hasNext()) {
			Map.Entry fEntry = (Map.Entry) i.next();
			String name = (String) fEntry.getKey();

//				while(!name.equals("North East Medical Services")) {
//					 
//					fEntry = (Map.Entry) i.next();
//					 name = (String) fEntry.getKey();
//				}

			if (osm.containsKey(name)) {

				SpatialObject obj = osm.get(name);
				if (!name.contains("\"") && !name.contains("\\")) {

//				SpatialObject obj = foursquare.get(name);
					String link = getOSMObject("http://linkedgeodata.org/sparql", obj.getCompleteDescription(),
							obj.getName(), obj.getLat(), obj.getLgt());

					System.out.println("Achou: " + name + " " + link);
				}
			} else {
				System.out.println("Didn't find: " + name);
			}
		}
	}

	//Change the delimiter from cityLGD file from " " to "\t"
	public void changeDelimiter(String nameFile) throws IOException {

		Writer fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\" + nameFile + ".txt", true),
				"ISO-8859-1");

		String line = reader.readLine();

		while (line != null) {
			String[] lineVec = line.split(" ");

			String id = lineVec[0];
			String rawLat = lineVec[1];
			String rawlgt = lineVec[2];

			String category = lineVec[3] + " " + lineVec[4];
			String description = lineVec[5];

			for (int a = 6; a < lineVec.length; a++) {
				description = description + " " + lineVec[a];
			}

			fileWrt.append(id + "\t" + rawLat + "\t" + rawlgt + "\t" + category  + "\t" + description + "\n");
			line = reader.readLine();
		}
		fileWrt.flush();
		fileWrt.close();
		reader.close();
		
		System.out.println("File created successfully!");
	}
	
	// Examples of usage
	public static void main(String[] args) throws IOException {

		TreeSet<SpatialObject> t = new TreeSet<>();
		
		SpatialObject ob1 = new SpatialObject(1, "vazio");
		ob1.setScore(1);
		t.add(ob1);
		
		SpatialObject ob2 = new SpatialObject(2, "vazio1");
		ob2.setScore(2);
		t.add(ob2);
		
		SpatialObject ob3 = new SpatialObject(3, "vazio2");
		ob3.setScore(3);
		t.add(ob3);
		
		t.pollFirst();
		
		Iterator<SpatialObject> it = t.iterator();		
		
		while(it.hasNext()) {		
			System.out.println(it.next().getScore());
		}
		
//		Datasets o = new Datasets();
//		
//		o.isolatePOI("./DatasetsOutput/check-ins/San Francisco.txt");

//		o.loadPOIs("./DatasetsOutput/osm/New York.txt");

//		Datasets obj = new Datasets("./DatasetsOutput/osm/London hotel.txt");
//		obj.changeDelimiter("London hotel Tab");
//		obj.interestObjectCreateFile("LondonGrandeLGD.txt");
//		o.cleanDataset("./DatasetsOutput/osm/San Francisco hotels.txt");
//		o.interestObjectCreateFoursquare("./DatasetsOutput/check-ins/San Francisco.txt", "./DatasetsOutput/osm/San Francisco.txt");

//		obj.createUserProfile("New York");

//obj.parseSpatialCoordinates();

		// obj.hotelGroupProfiler();
		// hotelProfiler replaced by groupProfiler
		// obj.hotelProfiler("are_dubai_chelsea_tower_hotel_apartments", "Chelsea
		// Gardens Hotel");
//		 Datasets.fileHeallthCheck("./datasetsOutput/MadridLGD.txt");

	}

}
