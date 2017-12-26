package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import skpq.SpatialObject;

public class QueryEvaluation {

	private String fileName;
	ArrayList<SpatialObject> results;
	ArrayList<SpatialObject> idealResults;
	private String query_keywords;
	private boolean debug = false;

	public QueryEvaluation(String fileName, String query_keywords) throws IOException{
		this.fileName = fileName;
		this.query_keywords = query_keywords;
		
		results = new ArrayList<>();
		idealResults = new ArrayList<>();

		readResultSet();
	}

	private void readResultSet() throws IOException{

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();
		
		while(line != null){
		
			String rate = line.split("rate=")[1].trim();
			String label = line.split("[0-9]\\.[0-9]")[0].trim();
			String googleDescription = line.split("googleDescription=")[1].split("score")[0].trim();
			String score = line.split("score=")[1].split("rate")[0].trim();
			
//			System.out.println("label = " + label);
//			System.out.println("rate = " + rate);
//			System.out.println("score = " + score);
			SpatialObject obj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(score));
//			System.out.println("Gdesc = " + googleDescription);
//			System.out.println("rate = " + rate);
//			System.out.println("key = " + query_keywords);
			SpatialObject idealObj = new SpatialObject(googleDescription, Double.parseDouble(rate), Double.parseDouble(rate));
			
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
		
//		System.out.println("Printing ordered scores: IDCG");
//		Iterator<SpatialObject> it = idealResults.iterator();
//		while(it.hasNext()){
//			System.out.println(it.next().getScore());
//		}
		
		idcg[0] = idealResults.get(0).getScore();
		
		int b = 1;
		for(int a = 1; a < idealResults.size(); a++){			
			idcg[b] = idcg[b - 1] + (idealResults.get(a).getScore() / (Math.log10(b+1) / Math.log10(2)));			
			b++;
		}

		return idcg;
	}

	@SuppressWarnings("unchecked")
	public double[] discountCumulativeGain(){

		double[] dcg = new double[results.size()];

		Collections.sort(results);
		
//		System.out.println("Printing ordered scores: DCG");
//		Iterator<SpatialObject> it = results.iterator();
//		while(it.hasNext()){
//			System.out.println(it.next().getScore());
//		}
		dcg[0] = results.get(0).getRate();
		
		//System.out.println(dcg[0]);
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
		
//		String file = "C:\\Users\\Jo�oPaulo\\Dropbox\\Doutorado\\SKPQ enhanced"
//					+ "\\resultados\\range\\mais frequente\\evaluations\\" + fileName.split(" --- ratings.txt")[0] + " Evaluation.txt";
		
		String file = "evaluations\\" + fileName.split(" --- ratings")[0] + " Evaluation.txt";
		//String file = fileName.split(" --- ratings.txt")[0] + " Evaluation.txt";
				
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
		//for(int a = 0; a < results.size(); a++){
			//acumulator = acumulator + ndcg[a];
			//output.write(Double.toString(ndcg[a]) + "  ");
		//}
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
	
	/* A cada experimento, mudar o diretório de saída */
	public static void main(String[] args) throws IOException {

		int k_max = 20;
		int inc = 5, k = 5;
		
		while(k <= k_max){
			
			boolean arquivoCriado = false;
	
//			String fileName = "SPKQ-LD [k="+k+", kw=cafe].txt";
//			String fileName = "RQ-LD [k="+k+", kw=cafe].txt";
			String fileName = "SPKQ [k="+k+", kw=cafe].txt";
			
			if(!arquivoCriado){
				Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split("\\.")[0] + " --- ratings.txt"), "ISO-8859-1");
				RatingExtractor obj = new RatingExtractor("cossine");
	
//				ArrayList<String> rateResults = obj.rateSKPQresults2(fileName);
//				ArrayList<String> rateResults = obj.rateLODresult(fileName);
//				ArrayList<String> rateResults = obj.rateRangeLODresult(fileName);
				ArrayList<String> rateResults = obj.rateRangeResults(fileName);
	
				System.out.println("\n\n --- Resultados ---\n");
	
				for (String x : rateResults) {
	
					output.write(x + "\n");	
				}		
				output.close();
			}
			
			QueryEvaluation q = new QueryEvaluation(fileName.split("\\.")[0] + " --- ratings.txt", fileName.split("kw=")[1].split("\\]\\.txt")[0]);
	
			q.execute();
			k = k + inc;
		}
	}

}
