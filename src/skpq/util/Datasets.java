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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


public class Datasets {

	BufferedReader reader;
	public static char quotes = '"';
	public static boolean USING_GRAPH = false;
	private static boolean debug = false;
	static Model model = getTestModel();
	static String arquivoOSMLinkado = "features_linked.txt";
	//private static org.apache.log4j.Logger log = Logger.getLogger(); //create log
	protected String file;
	public Datasets(String file) throws UnsupportedEncodingException, FileNotFoundException{
		this.file = file;
		reader = new BufferedReader((new InputStreamReader(new FileInputStream(new File(file)), "ISO-8859-1")));
	}
	
	public static Model getTestModel() {

		Model model = ModelFactory.createDefaultModel();
		return model;

	}

	//Create a file with LGD links to OSM objects
	public void interestObjectCreateFile(String nomeArquivo) throws IOException{

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

		//System.out.println(queryString);

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

						//System.out.println(objLat + " " + objLon);
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
				+ "reamake.txt"), "ISO-8859-1");
		Writer health = new OutputStreamWriter(new FileOutputStream("DatasetsOutput\\"
				+ "health.txt"), "ISO-8859-1");
		String line = read.readLine();
		
		while(line != null){
			
			String[] lineVec = line.split("http:");
			//System.out.println(line);
			if(lineVec.length > 1){
				//System.out.println("Health \n");
				health.write(line + "\n");
			}else{
				//System.out.println("Remake \n");
				rmk.write(line + "\n");
			}
			line = read.readLine();
		}
		health.close();
		rmk.close();
	}
	
	//Examples of usage
	public static void main(String[] args) throws IOException {
		try {
			Datasets obj = new Datasets("hotel.txt");

			obj.interestObjectCreateFile("hotelLGD2.txt");
			//Datasets.fileHeallthCheck("C://Users//JoãoPaulo//Documents//NetBeansProjects//DatasetsOutput//OSM_features.txt");
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
