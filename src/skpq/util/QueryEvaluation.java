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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;

import skpq.SpatialObject;

public class QueryEvaluation {

	private String fileName;
	ArrayList<SpatialObject> results;
	ArrayList<SpatialObject> idealResults;
	private boolean debug = false;

	public QueryEvaluation(String fileName) throws IOException{
		this.fileName = fileName;
		
		results = new ArrayList<>();
		idealResults = new ArrayList<>();

		readResultSet();
	}
	
	public QueryEvaluation(){
		
	}

	private void readResultSet() throws IOException{

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();
		
		while(line != null){
		
			String rate = line.split("rate=")[1].trim();
			String label = line.split("[0-9]\\.[0-9]")[0].trim();
//			String googleDescription = line.split("googleDescription=")[1].split("score")[0].trim();
			String score = line.split("score=")[1].split("rate=")[0].trim();					
			
			SpatialObject obj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(score));
			System.out.println("Label: " + label);
			System.out.println("Rate: " + rate);
			System.out.println("Score: " + score);
//			SpatialObject idealObj = new SpatialObject(googleDescription, Double.parseDouble(rate), Double.parseDouble(rate));
			SpatialObject idealObj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(rate));
			
			results.add(obj);
			idealResults.add(idealObj);

			line = reader.readLine();
		}

		reader.close();
	}
	
	public double precision(){
		
		int k = results.size();
		
		int relevants = 0;
		
		for(int a = 0; a < results.size(); a++){
			//if the object's rate is higher than 3, it is considered a relevant object
			if(results.get(a).getScore() > 3){
				relevants++;
			}
		}
				
		return (double) relevants / k;
	}
	
	/*Computes P@N */
	public double precisionN(int n){
				
		int relevants = 0;
		
		for(int a = 0; a <= n; a++){
			//if the object's rate is higher than 3, it is considered a relevant object
			if(results.get(a).getScore() > 3){
				relevants++;
			}
		}				
		return (double) relevants / (n+1);
	}
	
	public double averagePrecision(){
		
		double ap = 0;
		int r = 0;
		int relevants = 0;
		
		for(int a = 0; a < results.size(); a++){
			if(results.get(a).getScore() > 3){
				r = 1;
			}
			ap = ap + (precisionN(a) * r);
			r = 0;
		}
		
		for(int a = 0; a < results.size(); a++){
			//if the object's rate is higher than 3, it is considered a relevant object
			if(results.get(a).getScore() > 3){
				relevants++;
			}
		}		
		return (double) ap / relevants;
	}
	
	public double[] cumulativeGain(){

		double[] cg = new double[results.size()];

		cg[0] = results.get(0).getScore();
		
		for(int a = 1; a < results.size(); a++){			
			cg[a] = results.get(a).getScore() + cg[a - 1];			
		}
		return cg;
	}
		
	/* Uso não necessário, implementado só para testes */
	@SuppressWarnings("unchecked")
	@Deprecated
	public double[] idealCumulativeGain(ArrayList<SpatialObject> results){

		double[] icg = new double[results.size()];

		Collections.sort(results);
		
		icg[0] = results.get(results.size()-1).getScore();
		
		int b = 1;		
		for(int a = results.size() - 2; a >= 0; a--){			
			icg[b] = results.get(a).getScore() + icg[b - 1];
			b++;
		}

		return icg;
	}
		

	@SuppressWarnings("unchecked")
	public double[] idealDiscountCumulativeGain(ArrayList<SpatialObject> resultsClone){

		double[] idcg = new double[idealResults.size()];

		Collections.sort(idealResults);		
		
		idcg[0] = idealResults.get(idealResults.size()-1).getScore();

		int b = 1;
		for(int a = idealResults.size()-2; a >= 0; a--){	
			idcg[b] = idcg[b - 1] + (idealResults.get(a).getScore() / (Math.log10(b+1) / Math.log10(2)));		
			b++;
		}

		return idcg;
	}

	public double[] discountCumulativeGain(){

		double[] dcg = new double[results.size()];
	
		dcg[0] = results.get(0).getRate();
				System.out.println(dcg[0]);
		for(int a = 1; a < results.size(); a++){			
			dcg[a] = dcg[a - 1] + (results.get(a).getRate() / (Math.log10(a+1) / Math.log10(2)));		
		}

		return dcg;
	}
	
	public double[] normalizedDiscountCumulativeGain(double[] idcg, double[] dcg){
		
		double[] ndcg = new double[results.size()];	

		for(int a = 0; a < ndcg.length; a++){
			ndcg[a] = dcg[a] / idcg[a];
		}					
		return ndcg;
	}
	
	private double outputVec(String fileName, double[] dcg, double[] idcg, double[] ndcg, double precision, double avPrecision) throws IOException{
		
		double acumulator1 = 0, acumulator2 = 0;
		
		String file = "evaluations\\" + fileName.split(" --- ratings")[0] + " Evaluation.txt";
				
		Writer output = new OutputStreamWriter(new FileOutputStream(file), "ISO-8859-1");			
			
		output.write("[DCG] = { ");		
		for(int a = 0; a < results.size(); a++){
			acumulator1 = acumulator1 + dcg[a];
			output.write(Double.toString(dcg[a]) + "  ");
		}		
		output.write(" } = " + acumulator1 + "\n");
				
		output.write("[IDCG] = { ");		
		for(int a = 0; a < results.size(); a++){
			acumulator2 = acumulator2 + idcg[a];
			output.write(Double.toString(idcg[a]) + "  ");
		}		
		output.write(" } = " + acumulator2 + "\n");
				
		output.write("[NDCG] =");		

		output.write(" " + acumulator1/acumulator2 + "\n");
			
		output.write("[Precision] = " + precision + "\n");
		output.write("[Average Precision] = " + avPrecision);
		
		if(debug)
		System.out.println("Evaluation data printed at: " + file);
		
		output.close();
		
		return acumulator1/acumulator2;
	}

	@SuppressWarnings("unused")
	private void output(String fileName, double[] dcg, double[] idcg, double[] ndcg, double precision, double avPrecision) throws IOException{
		
		Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split(" --- ratings.txt")[0] + " Evaluation.txt"), "ISO-8859-1");
		
		output.write("#\tDCG \t\t\t\t IDCG \t\t\t\t NDCG\n");
		
		for(int a = 0; a < results.size(); a++){
			output.write("[" + a + "] " + dcg[a] + "  " + idcg[a] + "  " + ndcg[a] + "\n");
		}
		
		output.write("[Precision] = " + precision + "\n");
		output.write("[Average Precision] = " + avPrecision + "\n");
		
		System.out.println("Evaluation data printed at: " + fileName.split(" --- ratings.txt")[0] + " Evaluation.txt");
		
		output.close();
	}
	
	public double execute() throws IOException{
		
		double[] dcg = discountCumulativeGain();
		@SuppressWarnings("unchecked")
		double[] idcg = idealDiscountCumulativeGain((ArrayList<SpatialObject>) results.clone());
		
		double[] ndcg = normalizedDiscountCumulativeGain(idcg, dcg);
		
		double precision = precision();
		double avPrecision = averagePrecision();		
		
		return outputVec(fileName, dcg, idcg, ndcg, precision, avPrecision);
	}
	
	private void evaluateQueriesGroup(String queryName, String[] queryKeyword, int k_max) throws IOException{
		
		@SuppressWarnings("unused")
		int inc = 5, k = 20, a = 0;		
		double[] ndcg = new double[4];
		
		for(int ind = 0; ind < queryKeyword.length; ind++){
		System.out.println("Key: " + queryKeyword[ind]);
			while(k <= k_max){								
			
			boolean arquivoCriado = false;
			/* A cada experimento, mudar o diretorio de saida */

			String fileName = queryName + "-LD [k="+k+", kw="+queryKeyword[ind]+"].txt";
			
				if(!arquivoCriado){
				Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split("\\.txt")[0] + " --- ratings.txt"), "ISO-8859-1");
				/*Evaluation methods: 
				 * default --> using only Google Maps rate
				 * cosine --> considers cosine similarity score and Google Maps rate 
				 * tripAdvisor --> using an opinRank query, it searches for user's judgment related to the query. It is necessary to set the rate file manually.
				 * personalized --> searches for the rate related to the the user preference. Each user preference is represented by a profile. The user preference must be described manually in the method.				 
				 * */	
				RatingExtractor obj = new RatingExtractor("personalized");
					
				ArrayList<String> rateResults = obj.rateLODresult("evaluator/"+fileName);
	
				for (String x : rateResults) {
	
					output.write(x + "\n");	
				}		
				output.close();
			}
			
			QueryEvaluation q = new QueryEvaluation(fileName.split("\\.txt")[0] + " --- ratings.txt");
			
			ndcg[a] = q.execute();
			System.out.println(ndcg[a]);
			a++;					
			
			k = k + inc;		
	}
		
		
		double soma = 0;
		
		for(int b = 0; b < ndcg.length; b++){											
			soma = soma + ndcg[b];
			System.out.print(ndcg[b] + " ");
		}
		
		System.out.println("\nAverage: " + soma/4 + "\n\n");
		a = 0;
		k = 5;
		}		
	}
	
	
	public static void main(String[] args) throws IOException {	
		
		QueryEvaluation q = new QueryEvaluation();
//		String keys[] = {"pitseahall","mischief","nike","devons","crash","glenavon","cullings","laffans","thales","bradfields"};
		String keys[] = {"bradfields"};		
		
//		q.evaluateQueriesGroup("PSKPQ", keys, 20);		
		q.evaluateQueriesGroup("PSKPQ", keys, 20);
		
//		int k_max = 5;
//		@SuppressWarnings("unused")
//		int inc = 5, k = 5; 
////		a = 0, i = 1;
////		double radius = 0.005, radiusMax = 0.005;
//		while(k <= k_max){
//			
//		//	a = 0;
////			i = 1;
//			
//			boolean arquivoCriado = false;
//			/* A cada experimento, mudar o diretório de saída */
////			String fileName = "SPKQ-LD [k="+k+", kw=supermarket food].txt";
////			String fileName = "RQ-LD [k="+k+", kw=cafe].txt";
//			String fileName = "SKPQ-LD [k="+k+", kw=cafe].txt";
////			String fileName = "RQ [k="+k+", kw=amenity, radius="+radius+"].txt";
//			
////			while(radius <= radiusMax){
//
//				DecimalFormat df = new DecimalFormat("#.###");
//				DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
//				sym.setDecimalSeparator('.');
//				df.setDecimalFormatSymbols(sym);
////				String fileName = "RQ [k="+k+", kw=cafe, radius="+df.format(radius)+"].txt";
//			
//				if(!arquivoCriado){
//				Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split("\\.txt")[0] + " --- ratings.txt"), "ISO-8859-1");
//				RatingExtractor obj = new RatingExtractor("tripAdvisor");
//	
////				ArrayList<String> rateResults = obj.rateSKPQresults(fileName);
//				ArrayList<String> rateResults = obj.rateLODresult(fileName);
////				ArrayList<String> rateResults = obj.rateRangeLODresult(fileName);
////				ArrayList<String> rateResults = obj.rateRangeResults(fileName);
//	
////				System.out.println("\n\n --- Resultados ---\n");
//	
//				for (String x : rateResults) {
//	
//					output.write(x + "\n");	
//				}		
//				output.close();
//			}
//			
//			QueryEvaluation q = new QueryEvaluation(fileName.split("\\.txt")[0] + " --- ratings.txt");
//	
////			ndcg[a] = q.execute();
//			System.out.println(q.execute());
//		//	a++;	
////			radius = radius + 0.02;	
//			
//		}
//			k = k + inc;
//		}
//		
//		//double soma = 0;
//		
////		for(int b = 0; b < ndcg.length; b++){											
////			soma = soma + ndcg[b];
////			System.out.print(ndcg[b] + " ");
////		}
//		
////		System.out.println("\nSomat�rio " + k + ": " + soma);		
////		}
////		System.out.println("\nSomat�rio ");
////		//Calculate evaluation arithmetic mean
////		
////
////		for(int b = 0; b < evaluations.get(0).length; b++){
////			for(int c = 0; c < evaluations.size(); c++){		
////				ndcg[b] = ndcg[b] + evaluations.get(c)[b];
////			}
////			//			ndcg[b] = ndcg[b] / evaluations.size();
////			System.out.print(ndcg[b] + " ");
////		}
	}

}
