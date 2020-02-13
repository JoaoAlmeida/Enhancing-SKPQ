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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import node.Sparql;
import skpq.SpatialObject;


public class Datasets {

	BufferedReader reader;
	public static char quotes = '"';
	public static boolean USING_GRAPH = false;
	private static boolean debug = false;
	static Model model = getTestModel();
	static String arquivoOSMLinkado = "features_linked.txt";
	//private static org.apache.log4j.Logger log = Logger.getLogger(); //create log
	protected String file;
	public int featureID;

	public Datasets(String file) throws UnsupportedEncodingException, FileNotFoundException{
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

		String fileName = queryName + "-LD [k="+k+", kw="+keyword+"].txt";

		BufferedReader read = new BufferedReader((new InputStreamReader(new FileInputStream(new File("skpq/frequent/default/"+fileName)), "ISO-8859-1")));			

		String line = read.readLine();

		int i = 1;

		while (line != null) {								

			String osmLabel = line.substring(line.indexOf("(tourism) (hotel)"), line.indexOf(", lat")).trim();	

			String lat = line.substring(line.indexOf("lat=")+4, line.indexOf(", lgt")).trim();
			String lgt = line.substring(line.indexOf("lgt=")+4, line.indexOf(", score=")).trim();
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


	//Create a file with LGD links to OSM objects
	public void interestObjectCreateFile(String nomeArquivo) throws IOException{

		System.out.println("Creating the POIs File...");

		Writer fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\"
				+ nomeArquivo, true), "ISO-8859-1");

		String line = reader.readLine();

		while(line != null){

			String[] lineVec = line.split(" ");

			String rawLat = lineVec[1];
			String rawlgt = lineVec[2];

			String lat  = rawLat.substring(0, rawLat.indexOf('.')+3);
			String lgt  = rawlgt.substring(0, rawlgt.indexOf('.')+3);	

			String label;		

			/*if(lineVec[3].equals("-")){
				label = line.split("-")[1].trim();
				System.out.println("Label = " + label);
			}*/

			int fim = line.indexOf(')');

			int inicioSub = line.indexOf('(', fim + 1);
			int fimSub = line.indexOf(')', inicioSub + 1);

			//String subCategoria = line.substring(inicioSub + 1, fimSub);               

			label = line.substring(fimSub + 1).replace('"', ' ').trim();

			if(!label.equals(" ")){
				///"http://linkedgeodata.org/vsparql";
				String link = getOSMObject("http://linkedgeodata.org/sparql", line, label, lat, lgt);

				fileWrt.append(rawLat + " " + rawlgt + " " + label + " " + link + "\n");

				fileWrt.close();

				fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\"
						+ nomeArquivo, true), "ISO-8859-1");
			}
			line = reader.readLine();		
		}
		System.out.println("\nFile created!");
	}

	public static String getOSMObject(String service, String line, String label, String lat, String lon) throws IOException {

		if(debug)
			System.out.println(label + " " + lat + " " + lon + " ");		

		//String serviceURI = "http://linkedgeodata.org/vsparql";
		String serviceURI = service;

		String queryString = "" + Sparql.addService(USING_GRAPH, serviceURI) + "SELECT * WHERE { ?var rdfs:label " + quotes + label + quotes +"."
				+ "?var geo:lat ?lat."
				+ "?var geo:long ?lon."
				+ "}" + Sparql.addServiceClosing(USING_GRAPH);

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

						if(objLat.contains(lat) && objLon.contains(lon)){
							if(uri.isResource()){														
								if(debug){
									System.out.print(uri.asResource().getURI().toString());	
									System.out.println();
								}
								Writer fileWrt = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\"
										+ arquivoOSMLinkado, true), "ISO-8859-1");

								fileWrt.append(line + "\n");
								fileWrt.close();

								return uri.asResource().getURI().toString();
							}else{								
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

	//Work in progress
	public static void osm_matches_URI(String filePath1) throws IOException{

		@SuppressWarnings("resource")
		BufferedReader read = new BufferedReader((new InputStreamReader(new FileInputStream(new File(filePath1)), "ISO-8859-1")));

		String line = read.readLine();

		while(line != null){

		}
	}

	//Separate that objects which have a LGD link from those that have not 
	public static void fileHeallthCheck(String filePath) throws IOException{

		@SuppressWarnings("resource")
		BufferedReader read = new BufferedReader((new InputStreamReader(new FileInputStream(new File(filePath)), "ISO-8859-1")));

		Writer rmk = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\"
				+ "unhealth.txt"), "ISO-8859-1");
		Writer health = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\"
				+ "health.txt"), "ISO-8859-1");
		String line = read.readLine();

		while(line != null){

			String[] lineVec = line.split("http:");

			if(lineVec.length > 1){
				health.write(line + "\n");
			}else{
				rmk.write(line + "\n");
			}
			line = read.readLine();
		}
		health.close();
		rmk.close();
	}

	public void hotelProfiler(String filePath, String hotelName) throws IOException{

		try (Stream<Path> paths = Files.walk(Paths.get("C://Users//JoãoPaulo//Documents//GitHub//Enhancing-SKPQ//dubai"))) {
			paths
			.filter(Files::isRegularFile)
			.forEach(System.out::println);
		}

		BufferedReader read = new BufferedReader((new InputStreamReader(new FileInputStream(new File(filePath)), "ISO-8859-1")));

		Writer profile = new OutputStreamWriter(new FileOutputStream(hotelName+".arff"), "ISO-8859-1");			

		profile.write("@relation "+ hotelName + "\n\n");
		profile.write("@attribute class {1,5}" + "\n");
		profile.write("@attribute description string" + "\n\n");
		profile.write("@data" + "\n");

		String line = read.readLine();

		while(line != null){
			line = line.replaceAll("'", " ");
			int index = line.indexOf(" ", 4);
			profile.write("1,'"+ line.substring(index+5).trim() + "'\n");
			line = read.readLine();
		}

		read.close();
		profile.close();
	}

	//Read a folder and create a profile hotel for each file in the folder	
	public void hotelGroupProfiler() throws IOException{		

		String city = "london";
		//	File folder = new File("C://Users//JoãoPaulo//Documents//GitHub//Enhancing-SKPQ//london");
		File folder = new File("D://Documents//GitHub//Enhancing-SKPQ//"+city);

		String[] files = new String[folder.listFiles().length];

		files = folder.list();

		for(int i = 0; i < folder.listFiles().length; i++){
			System.out.println(files[i]);
			System.out.println(city+"/"+files[i]);
			BufferedReader read = new BufferedReader((new InputStreamReader(new FileInputStream(new File(city+"/"+files[i])), "ISO-8859-1")));

			Writer profile = new OutputStreamWriter(new FileOutputStream("all/"+files[i]+".arff"), "ISO-8859-1");			

			profile.write("@relation "+ files[i] + "\n\n");
			profile.write("@attribute class {1,5}" + "\n");
			profile.write("@attribute description string" + "\n\n");
			profile.write("@data" + "\n");

			String line = read.readLine();

			while(line != null){
				line = line.replaceAll("'", " ");
				int index = line.indexOf(" ", 4);
				profile.write("1,'"+ line.substring(index+5).trim() + "'\n");
				line = read.readLine();
			}

			read.close();
			profile.close();
		}		
	}
	
	/* ============= Methods regarding the thir article: "" ============= */
	public void createUserProfile(String DatasetFileName) throws IOException {			

		//jump the file header
		String line = reader.readLine();
		
		line = reader.readLine();
		
		while(line != null) {
			
			String[] lineVec = line.split("\\t");
			
			String userID = lineVec[0];
			System.out.println("Building user profile #" + userID);
//			String dateTime = lineVec[1];
//			String venueID = lineVec[2];
//			String venueName = lineVec[3];
//			String venueLocation = lineVec[4];
//			String venueCategory = lineVec[5];
						
			String currentID = userID;
			
			Writer userProfile = new OutputStreamWriter(new FileOutputStream("profiles\\check-ins\\user profile " + userID + ".txt", true), "ISO-8859-1");
			
			while(currentID.equals(userID) && line != null) {
				
				userProfile.write(line + "\n");				
				
				line = reader.readLine();
				
				if(line != null) {
					lineVec = line.split("\\t");
					
					userID = lineVec[0];
				}
			}
			userProfile.flush();
			userProfile.close();
		}

		reader.close();
	}
	

	//Examples of usage
	public static void main(String[] args) throws IOException {		
		//		String line = "-->[1]  [OSMlabel=(tourism) (hotel) The Five Arrows Hotel, lat=51.8454561, lgt=-0.9254593, score=0.3799783574435262]";
		//		String score[] = line.split("score=")[1].split("]");
		//		System.out.println(Double.parseDouble(score[0]));
		//		int index = line.indexOf(" ", 4);
		//		
		//		System.out.println(line.split("\\(hotel\\)")[1].trim().toLowerCase());
		//		try {
		Datasets obj = new Datasets("Doutorado\\terceiro journal\\Datasets\\CA\\checkin_CA_venues.txt");
//		obj.loadResultstoPersonalize("SKPQ", "amenity", 5);
		obj.createUserProfile(" ");
		//			obj.hotelGroupProfiler();
		//			hotelProfiler replaced by groupProfiler
		//			obj.hotelProfiler("are_dubai_chelsea_tower_hotel_apartments", "Chelsea Gardens Hotel");
		//			obj.interestObjectCreateFile("hotelLondon.txt");
		//			Datasets.fileHeallthCheck("D://Documents//GitHub//Enhancing-SKPQ//DatasetsOutput//hotelLondon.txt");				

		//		} catch (UnsupportedEncodingException | FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
	}

}
