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

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.tika.mime.ProbabilisticMimeDetectionSelector;

import skpq.SpatialObject;

public class QueryEvaluation {

	private String fileName;
	ArrayList<SpatialObject> results;
	ArrayList<SpatialObject> idealResults;
	private boolean debug = false;

	public QueryEvaluation(String fileName) throws IOException {
		this.fileName = fileName;

		results = new ArrayList<>();
		idealResults = new ArrayList<>();

		readResultSet();
	}

	public QueryEvaluation() {

	}

	// Consider removing the methos without Best Neighbor included
	private void readResultSet() throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File("./thrash/" + fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		while (line != null) {

			String rate = line.split("rate=")[1].trim();
			String label = line.split("[0-9]\\.[0-9]")[0].trim();
//			String googleDescription = line.split("googleDescription=")[1].split("score")[0].trim();
			String score = line.split("score=")[1].split("rate=")[0].trim();

			SpatialObject obj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(score));
//			System.out.println("Label: " + label);
//			System.out.println("Rate: " + rate);
//			System.out.println("Score: " + score);
//			SpatialObject idealObj = new SpatialObject(googleDescription, Double.parseDouble(rate), Double.parseDouble(rate));
			SpatialObject idealObj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(rate));

			results.add(obj);
			idealResults.add(idealObj);

			line = reader.readLine();
		}

		reader.close();
	}

	@SuppressWarnings("unused")
	private void readResultSetBN() throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File("./thrash/" + fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		while (line != null) {

			String rate = line.split("rate=")[1].trim();
			//rever esse label, parece estar lendo outras coisas
			String label = line.split("[0-9]\\.[0-9]")[0].trim();
//			String googleDescription = line.split("googleDescription=")[1].split("score")[0].trim();
			String score = line.split("score=")[1].split("rate=")[0].trim();

			SpatialObject obj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(score));
//			System.out.println("Label: " + label);
//			System.out.println("Rate: " + rate);
//			System.out.println("Score: " + score);
//			SpatialObject idealObj = new SpatialObject(googleDescription, Double.parseDouble(rate), Double.parseDouble(rate));
			SpatialObject idealObj = new SpatialObject(label, Double.parseDouble(rate), Double.parseDouble(rate));

			results.add(obj);
			idealResults.add(idealObj);

			// jump another line to ignore the BN
			line = reader.readLine();

			line = reader.readLine();
		}

		reader.close();
	}

	public double precision() {

		int k = results.size();

		int relevants = 0;

		for (int a = 0; a < results.size(); a++) {
			// if the object's rate is higher than 3, it is considered a relevant object
			if (results.get(a).getScore() > 3) {
				relevants++;
			}
		}

		return (double) relevants / k;
	}

	/* Computes P@N */
	public double precisionN(int n) {

		int relevants = 0;

		for (int a = 0; a <= n; a++) {
			// if the object's rate is higher than 3, it is considered a relevant object
			if (results.get(a).getScore() > 3) {
				relevants++;
			}
		}
		return (double) relevants / (n + 1);
	}

	public double averagePrecision() {

		double ap = 0;
		int r = 0;
		int relevants = 0;

		for (int a = 0; a < results.size(); a++) {
			if (results.get(a).getScore() > 3) {
				r = 1;
			}
			ap = ap + (precisionN(a) * r);
			r = 0;
		}

		for (int a = 0; a < results.size(); a++) {
			// if the object's rate is higher than 3, it is considered a relevant object
			if (results.get(a).getScore() > 3) {
				relevants++;
			}
		}
		return (double) ap / relevants;
	}

	public double[] cumulativeGain() {

		double[] cg = new double[results.size()];

		cg[0] = results.get(0).getScore();

		for (int a = 1; a < results.size(); a++) {
			cg[a] = results.get(a).getScore() + cg[a - 1];
		}
		return cg;
	}

	/* Uso não necessário, implementado só para testes */
	@SuppressWarnings("unchecked")
	@Deprecated
	public double[] idealCumulativeGain(ArrayList<SpatialObject> results) {

		double[] icg = new double[results.size()];

		Collections.sort(results);

		icg[0] = results.get(results.size() - 1).getScore();

		int b = 1;
		for (int a = results.size() - 2; a >= 0; a--) {
			icg[b] = results.get(a).getScore() + icg[b - 1];
			b++;
		}

		return icg;
	}

	@SuppressWarnings("unchecked")
	public double[] idealDiscountCumulativeGain(ArrayList<SpatialObject> resultsClone) {

		double[] idcg = new double[idealResults.size()];

		Collections.sort(idealResults);

		idcg[0] = idealResults.get(idealResults.size() - 1).getScore();

		int b = 1;
		for (int a = idealResults.size() - 2; a >= 0; a--) {
			idcg[b] = idcg[b - 1] + (idealResults.get(a).getScore() / (Math.log10(b + 1) / Math.log10(2)));
			b++;
		}

		return idcg;
	}

	public double[] discountCumulativeGain() {

		double[] dcg = new double[results.size()];

		dcg[0] = results.get(0).getRate();

		for (int a = 1; a < results.size(); a++) {
			dcg[a] = dcg[a - 1] + (results.get(a).getRate() / (Math.log10(a + 1) / Math.log10(2)));
		}

		return dcg;
	}

	public double[] normalizedDiscountCumulativeGain(double[] idcg, double[] dcg) {

		double[] ndcg = new double[results.size()];

		for (int a = 0; a < ndcg.length; a++) {
			ndcg[a] = dcg[a] / idcg[a];
		}
		return ndcg;
	}
	
	//Tau considering ties. Validated using scipy.stats.kendalltau. Both methods return the same values.
	public double kendallTauBCoef() {
		
		int cons = 0, disc = 0, score_ties = 0, rate_ties = 0;
		double coef;
		
		for(int a = 0; a < results.size(); a++) {
			for(int b = 0; b < results.size(); b++) {
				if(a != b) {					
					if(agreement(results.get(a), results.get(b)) == 1) {
						cons++;
					}else if(agreement(results.get(a), results.get(b)) == -1){
						disc++;
					}else if(agreement(results.get(a), results.get(b)) == 2){
						score_ties++;
					}
					else if(agreement(results.get(a), results.get(b)) == 3){
						rate_ties++;
					}
				}
			}
		}

		System.out.println("C: " + cons);
		System.out.println("D: " + disc);
		System.out.println("S: " + score_ties);
		System.out.println("R: " + rate_ties);
		coef = (cons - disc) /  Math.sqrt((cons + disc + score_ties) * (cons + disc + rate_ties));

		return Math.abs(coef);
	}
	
	//Identify if the pair of POIs is concordant or discordant. True if concordant, false otherwise
	//1 = concordant, -1 discordant, 2 tied score, 3 tied rate, 0 both tied (ignore it)
	private int agreement(SpatialObject x, SpatialObject y) {

		int agr = -1;

		if (x.getScore() > y.getScore()) {
			if (x.getRate() > y.getRate()) {
				agr = 1;
			}
		} else if (x.getScore() < y.getScore()) {
			if (x.getRate() < y.getRate()) {
				agr = 1;
			}
		} 

		if (x.getScore() == y.getScore() && x.getRate() != y.getRate()) {
			agr = 2; 		
		}

		if (x.getRate() == y.getRate() && x.getScore() != y.getScore()) {
			agr = 3;		
		} 

		if (x.getRate() == y.getRate() && x.getScore() == y.getScore()) {
			agr = 0;
		}
		
		return agr;
	}
	
	private double outputVec(String fileName, double[] dcg, double[] idcg, double[] ndcg, double precision,
			double avPrecision, double tau) throws IOException {

		double acumulator1 = 0, acumulator2 = 0;

		String file = "evaluations\\" + fileName.split(" --- ratings")[0] + " Evaluation.txt";

		Writer output = new OutputStreamWriter(new FileOutputStream(file), "ISO-8859-1");

		output.write("[DCG] = { ");
		for (int a = 0; a < results.size(); a++) {
			acumulator1 = acumulator1 + dcg[a];
			output.write(Double.toString(dcg[a]) + "  ");
		}
		output.write(" } = " + acumulator1 + "\n");

		output.write("[IDCG] = { ");
		for (int a = 0; a < results.size(); a++) {
			acumulator2 = acumulator2 + idcg[a];
			output.write(Double.toString(idcg[a]) + "  ");
		}
		output.write(" } = " + acumulator2 + "\n");

		output.write("[NDCG] =");

		output.write(" " + acumulator1 / acumulator2 + "\n");

		output.write("[Tau] = " + tau);
		
//		output.write("[Precision] = " + precision + "\n");
//		output.write("[Average Precision] = " + avPrecision);

		if (debug)
			System.out.println("Evaluation data printed at: " + file);

		output.close();

		return acumulator1 / acumulator2;
	}

	@SuppressWarnings("unused")
	private void output(String fileName, double[] dcg, double[] idcg, double[] ndcg, double precision,
			double avPrecision) throws IOException {

		Writer output = new OutputStreamWriter(
				new FileOutputStream(fileName.split(" --- ratings.txt")[0] + " Evaluation.txt"), "ISO-8859-1");

		output.write("#\tDCG \t\t\t\t IDCG \t\t\t\t NDCG\n");

		for (int a = 0; a < results.size(); a++) {
			output.write("[" + a + "] " + dcg[a] + "  " + idcg[a] + "  " + ndcg[a] + "\n");
		}

		output.write("[Precision] = " + precision + "\n");
		output.write("[Average Precision] = " + avPrecision + "\n");

		System.out.println("Evaluation data printed at: " + fileName.split(" --- ratings.txt")[0] + " Evaluation.txt");

		output.close();
	}

	public double[] execute() throws IOException {

		double[] metrics = new double[2];
		
		double[] dcg = discountCumulativeGain();
		@SuppressWarnings("unchecked")
		double[] idcg = idealDiscountCumulativeGain((ArrayList<SpatialObject>) results.clone());

		double[] ndcg = normalizedDiscountCumulativeGain(idcg, dcg);

		double precision = precision();
		double avPrecision = averagePrecision();
		
		double tau = kendallTauBCoef();		
		metrics[0] = tau;
		
		metrics[1] = outputVec(fileName, dcg, idcg, ndcg, precision, avPrecision, tau);
		
//		System.out.println(tau);		
		return metrics;
	}

	//Refatorar, chamando o construtor duas vezes
	private void evaluateQueriesGroup(String queryName, String[] queryKeyword, int k_max, String city) throws IOException {

		int inc = 5, k = 5, a = 0;
		double[][] metrics = new double[4][4];

		FileUtils.cleanDirectory(new File("./thrash"));
		FileUtils.cleanDirectory(new File("./evaluations"));
		
		for (int ind = 0; ind < queryKeyword.length; ind++) {

			System.out.println("========== KEY: " + queryKeyword[ind].toUpperCase() + " ==========\n");

			while (k <= k_max) {

				boolean arquivoCriado = false;

				String fileName = queryName + "-LD [k=" + k + ", kw=" + queryKeyword[ind] + "].txt";

				if (!arquivoCriado) {										 					
					
					Writer output = new OutputStreamWriter(
							new FileOutputStream("./thrash/" + fileName.split("\\.txt")[0] + " --- ratings.txt"),
							"ISO-8859-1");
					
				
					/*
					 * Evaluation methods: 
					 * default --> using only Google Maps rate 
					 * cosine --> considers cosine similarity score and Google Maps rate 
					 * tripAdvisor --> using an opinRank query, it searches for user's judgment related to the query. It
					 * is necessary to set the rate file manually. 
					 * personalized --> searches for the rate related to the the user preference. Each user preference is represented
					 * by a profile. The user preference must be described manually in the method.
					 */
					RatingExtractor obj = new RatingExtractor("cosine", city);				
					
					ArrayList<String> rateResults = obj.rateLODresult("evaluator/" + fileName);

					for (String x : rateResults) {

						output.write(x + "\n");
					}
					output.close();
				}

				QueryEvaluation q = new QueryEvaluation(fileName.split("\\.txt")[0] + " --- ratings.txt");

				metrics[a] = q.execute();
//				System.out.println(metrics[a]);
				a++;

				k = k + inc;
			}

			System.out.println("Tau:");
			for(double[] n : metrics) {
				System.out.println(n[0]);
			}
			
			System.out.println("NDCG:");
			for(double[] n : metrics) {
				System.out.println(n[1]);
			}
			
			
			double soma = 0;

			for (int b = 0; b < metrics.length; b++) {
				soma = soma + metrics[b][1];
				System.out.print(metrics[b][1] + " ");
			}

			System.out.println("\nAverage NDCG: " + soma / 4 + "\n\n");
			a = 0;
			k = 5;
		}
	}

	public static void main(String[] args) throws IOException {
		
		QueryEvaluation q = new QueryEvaluation();
		
		//Berlin
//		String keys[] = { "wheat", "software", "wedding", "herb", "door", "pen", "pension", "development", "resource", "eagle" };
//		String keys[] = {"amenity","natural","shop","bench","tourism","bicycle","information","waste","parking","berliner"};
				
		//Los Angeles
//		String keys[] = { "architect", "colony", "publication", "family", "movie", "photography", "green", "pension", "nail", "week" };
//		String keys[] = {"amenity","avenue","road","north","east","west","south","boulevard","place","natural"};		
		
		//Madrid
//		String keys[] = { "route", "performance", "thought", "interface", "lose", "stop", "treatment", "city", "weight", "birthday" };
//		String keys[] = {"amenity","avenida","natural","shop","plaza","parking","calle","restaurant","arroyo","camino"};
		
		//London
//		String keys[] = { "agency", "phone", "nike", "aquarium", "crash", "secretary", "field", "medicine", "father", "tennis" };
//		String keys[] = {"amenity","shop","restaurant","close","street","road","avenue","drive","lane","pub"};
		
//		New York
//		String keys[] = { "importance", "food", "perspective", "concept", "resource", "queen", "chemistry", "apartment", "department", "database" };
//		String keys[] = {"amenity","shop","street","bicycle","place","natural","tree","road","avenue","drive"};
		
		String keys[] = {"amenity"};		
//		String keys[] = { "amenity","natural","shop","bench","tourism","bicycle","information","waste","parking","berliner", "wheat", "software", "wedding", "herb", "door", "pen", "pension", "development", "resource", "eagle"};
		
//		q.evaluateQueriesGroup("Pareto", keys, 20, "Berlin");		
		q.evaluateQueriesGroup("SKPQ", keys, 20,"Berlin");	
//		q.evaluateQueriesGroup("ParetoSearch", keys, 20, "Berlin");
//		q.evaluateQueriesGroup("InfluenceSearch", keys, 20);
	}

}
