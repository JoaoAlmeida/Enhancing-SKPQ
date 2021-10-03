package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.tika.mime.ProbabilisticMimeDetectionSelector;

import skpq.SpatialObject;

public class QueryEvaluation {

	private String fileName;
	ArrayList<SpatialObject> results;
	ArrayList<SpatialObject> idealResults;
	private boolean debug = false;
	private static double alpha;
	
	//conn armazena a conexao com o SGBD
    Connection conn = null;

	public QueryEvaluation(String fileName) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.fileName = fileName;

		results = new ArrayList<>();
		idealResults = new ArrayList<>();

		readResultSet();
		
		connect();
	}

	public QueryEvaluation() {

	}
	
	  private void connect() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
	        try {
//	            System.out.println("----------- Conectando ao SGBD -----------");
	            
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            conn = DriverManager.getConnection("jdbc:mysql://localhost/acm_recsys20?user=root&password=572069ce&serverTimezone=UTC&useSSL=FALSE");          

//	            System.out.println("----------- Conectado! -----------");
	            
	        } catch (SQLException ex) {
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	        }
	    }
	

	  public void storeSKPQResult(String query, int k, int numKey, String key, double radius, String city, String experimentName, double tau, double ndcg, String matchMethod, String neighborhood) throws SQLException {
		  
		  PreparedStatement result;
		  
		  if(tau != tau) {			  
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`,"
				  		+ "`ndcg`, `textSimilarity_methodName`, `neighborhood_name`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"',"
				  				+ "'"+ndcg+"', '"+matchMethod+"', '"+neighborhood+"');");
		  }else {
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `tau`,"
				  		+ "`ndcg`, `textSimilarity_methodName`, `neighborhood_name`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+tau+"', "
				  				+ "'"+ndcg+"', '"+matchMethod+"', '"+neighborhood+"');"); 
		  }		  
	        
	      result.execute();
	  }
	  
	  public void storeHdResult(String query, int k, int numKey, String key, double radius, String city, String experimentName, double alpha, double tau, double ndcg) throws SQLException {
		  
		  PreparedStatement result;

		  if(tau != tau) {			  
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `gamma`,"
				  		+ "`ndcg`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+alpha+"', "
				  				+ "'"+ndcg+"');");
		  }else {
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `gamma`,`tau`,"
				  		+ "`ndcg`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+alpha+"', '"+tau+"', "
				  				+ "'"+ndcg+"');"); 
		  }		  

	      result.execute();
	  }

	  public void storeParetoSearch(String query, int k, int numKey, String key, double radius, String city, String experimentName, double alpha, double tau, double ndcg, String matchMethod) throws SQLException {
		  
		  PreparedStatement result;

		  if(tau != tau) {			  
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `alpha`,"
				  		+ "`ndcg`, `textSimilarity_methodName`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+alpha+"', "
				  				+ "'"+ndcg+"', '"+matchMethod+"');");
		  }else {
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `alpha`,`tau`,"
				  		+ "`ndcg`, `textSimilarity_methodName`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+alpha+"', '"+tau+"', "
				  				+ "'"+ndcg+"', '"+matchMethod+"');"); 
		  }		  

	      result.execute();
	  }
	  
	  public void storePRRresult(String query, int k, int numKey, String key, double radius, String city, String experimentName, double alpha, double tau, double ndcg, String matchMethod) throws SQLException {
		  
		  PreparedStatement result;

//		  System.out.println(query + " " + k + " " + numKey + " " + key + " " +  radius+ " " +  city+ " " +  experimentName+ " " +  alpha+ " " +  tau+ " " +  ndcg+ " " +  matchMethod);
		  if(tau != tau) {			  
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `alpha`,"
				  		+ "`ndcg`, `textSimilarity_methodName`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+alpha+"', "
				  				+ "'"+ndcg+"', '"+matchMethod+"');");
		  }else {
			  result = conn.prepareStatement("INSERT INTO `acm_recsys20`.`"+query+"` (`k`, `numKey`, `keyword`, `radius`, `city`, `experimentName`, `alpha`,`tau`,"
				  		+ "`ndcg`, `textSimilarity_methodName`) VALUES ('"+k+"', '"+numKey+"', '"+key+"', '"+radius+"', '"+city+"', '"+experimentName+"', '"+alpha+"', '"+tau+"', "
				  				+ "'"+ndcg+"', '"+matchMethod+"');"); 
		  }		  

	      result.execute();
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
			if (results.get(a).getRate() > 3) {
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
			if (results.get(a).getRate() > 3) {
				r = 1;
				relevants++;
			}
			ap = ap + (precisionN(a) * r);
			r = 0;
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

//		System.out.println("C: " + cons);
//		System.out.println("D: " + disc);
//		System.out.println("S: " + score_ties);
//		System.out.println("R: " + rate_ties);
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
	
	//Identify if the pair of POIs is concordant or discordant. True if concordant, false otherwise
		//1 = concordant, -1 discordant, 2 tied score, 3 tied rate, 0 both tied (ignore it). Considers that score don't tie. Favours the method with one measure in the function score such as textual score (SKPQ)> Fica enviesado.
//	Comom o score da skpq so usa score textual, ela se beneficia pois vai haver mais empates no score, que gerarao cons ao inves de ties. Fazendo o valor do coeficiente subir consideravelmente a favor dela injustamente.
		@Deprecated
		private int agreementScore(SpatialObject x, SpatialObject y) {

			int agr = -1;

			if (x.getScore() > y.getScore()) {
				if (x.getRate() > y.getRate()) {
					agr = 1;
				}
			} else if(x.getScore() == y.getScore()) {
				if (x.getRate() > y.getRate() && x.getId() > y.getId()) {
					agr = 1;
				}
			}						
			
			if (x.getScore() < y.getScore()) {
				if (x.getRate() < y.getRate()) {
					agr = 1;
				}
			} else if(x.getScore() == y.getScore()) {
				if (x.getRate() < y.getRate() && x.getId() < y.getId()) {
					agr = 1;
				}
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
	private void evaluateQueriesGroup(String queryName, String[] queryKeyword, int k_max, String city, int numKey, double radius, String type) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {

		connect();
		
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
				if(queryName.equals("SKPQ")) {
					storeSKPQResult(queryName.toLowerCase(),k, numKey, queryKeyword[ind], radius, city, "recsys20", metrics[a][0], metrics[a][1], "default", type);
				}else if(queryName.equals("Pareto")) {
					storeSKPQResult("skpq",k, numKey, queryKeyword[ind], radius, city, "recsys20", metrics[a][0], metrics[a][1], "default", type);
				}else if(queryName.equals("InfluenceSearch")) {
					storeSKPQResult("skpq",k, numKey, queryKeyword[ind], radius, city, "recsys20", metrics[a][0], metrics[a][1], "default", type);
				}else if(queryName.equals("ParetoSearch")) {
					storeParetoSearch(queryName.toLowerCase(),k, numKey, queryKeyword[ind], radius, city, "recsys20", alpha,metrics[a][0], metrics[a][1], "default");
				}else {
					System.err.print("Query not implemented in database yet!");
				}

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

//	validated using the calculator: https://www.socscistatistics.com/tests/ttestdependent/default2.aspx
	public void pairedT_Test(int numberRanks, String baselineRank, String targetRank, int[] rankSize, 
			int[] numKey, String[] datasetNames, double radius, double[] alpha, String experimentName) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		connect();

		for(int d = 0; d < datasetNames.length; d++) {
			
			System.out.println(experimentName+": " + datasetNames[d] + "\n");

			for (int a=0; a < alpha.length; a++) {
			for(int k = 0; k < rankSize.length; k++) {									

				for(int n = 0; n < numKey.length; n++) {					

					String targetQuery = " ";
					String baselineQuery = " ";

					/* Verify if the baseline algorithm is correct */
					if(baselineRank.equals("skpq")) {
						baselineQuery = "SELECT keyword,ndcg,tau FROM `acm_recsys20`.`"+baselineRank+"` WHERE numKey="+ numKey[n] 
								+ " and k="+rankSize[k]+" and city ='"+datasetNames[d]+"' and radius="+radius+" and neighborhood_name ='range' and experimentName ='recsys20' order by keyword;";
					} else if (baselineRank.equals("inf")) {
						baselineQuery = "SELECT keyword,ndcg,tau FROM `acm_recsys20`.`"+"skpq"+"` WHERE numKey="+ numKey[n] 
								+ " and k="+rankSize[k]+" and city ='"+datasetNames[d]+"'and radius="+radius+" and neighborhood_name ='inf' and experimentName ='recsys20' order by keyword;";
					} else if (baselineRank.equals("hd")) {
						//gamma not included
						baselineQuery = "SELECT keyword,ndcg,tau FROM `acm_recsys20`.`"+"hdskpq"+"` WHERE numKey="+ numKey[n] 
								+ " and k="+rankSize[k]+" and city ='"+datasetNames[d]+"'and radius="+radius+" and experimentName ='eswa21' order by keyword;";
					} else {
						System.out.println("Inexistent baseline rank: " + baselineRank);
						System.exit(0);
					}

					/* Verify if the target algorithm is correct */
					if(targetRank.equals("paretosearch")) {
						targetQuery = "SELECT keyword,ndcg,tau FROM `acm_recsys20`.`"+targetRank+"` where numKey="+numKey[n]+" and k="+rankSize[k]+
								" and city='"+datasetNames[d]+"' and textSimilarity_methodName='default' and radius="+radius+" and alpha="+alpha[a]
								+ " and experimentName ='"+experimentName+"' order by keyword;";
					}else if(targetRank.equals("skpq")) {
						targetQuery = "SELECT keyword,ndcg,tau FROM `acm_recsys20`.`"+targetRank+"` where numKey="+numKey[n]+" and k="+rankSize[k]+
						" and city='"+datasetNames[d]+"' and radius="+radius+" and neighborhood_name='paretorank' and experimentName ='recsys20' order by keyword;";
			} 
					else if(targetRank.equals("prr")) {						
						if(numKey.length == 1) {
							experimentName="recsys20";
						}else {
							experimentName="jis2020";
						}
						targetQuery = "SELECT keyword,ndcg,tau FROM `acm_recsys20`.`"+targetRank+"` where numKey="+numKey[n]+" and k="+rankSize[k]+
								" and city='"+datasetNames[d]+"' and textSimilarity_methodName='default' and radius="+radius+" and alpha="+alpha[a]
								+ " and experimentName ='"+experimentName+"' order by keyword;";
					}else {
						System.out.println("Inexistent target rank: " + targetRank);
						System.exit(0);
					}

//					System.out.println("Baseline: " + baselineQuery);
//					System.out.println("Target: " + targetQuery);
//					System.out.println();
					
					PreparedStatement sqlBaseline = conn.prepareStatement(baselineQuery);
					PreparedStatement sqlTarget = conn.prepareStatement(targetQuery);		

					double[] ndcgB = new double[numberRanks];
					double[] ndcgTarget = new double[numberRanks];

					double[] tauB = new double[numberRanks];
					double[] tauTarget = new double[numberRanks];

					ResultSet baselineResult = sqlBaseline.executeQuery();
					ResultSet targetResult = sqlTarget.executeQuery();

					for(int i = 0; baselineResult.next();i++) {
						ndcgB[i] = baselineResult.getDouble("ndcg");

						targetResult.next();
						ndcgTarget[i] = targetResult.getDouble("ndcg");

						tauB[i] = baselineResult.getDouble("tau");							
						tauTarget[i] = targetResult.getDouble("tau");
					}

					TTest t = new TTest();

					double p_value_ndcg = t.pairedTTest(ndcgB, ndcgTarget);						

					double p_value_tau = t.pairedTTest(tauB, tauTarget);

					if(numKey.length>1) {
						System.out.println("NDCG --> #keys="+numKey[n]+ " --  p="+p_value_ndcg);

						System.out.println("TAU --> #keys="+numKey[n]+ " --  p="+p_value_tau);
						System.out.println();
					}
					else {
						System.out.println("NDCG --> k="+rankSize[k]+ " --  p="+p_value_ndcg);

						System.out.println("TAU --> k="+rankSize[k]+ " --  p="+p_value_tau);
						System.out.println();
					}
					sqlBaseline.close();
					sqlTarget.close();
				}
			}
		}
		}		
	}
	
	public void averageImprovement(String baseline, String proposal, int[] rankSize, int[] numKey) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
	
		connect();

		String[] datasetNames = {"Berlin", "London", "LosAngeles", "NewYork"};
		double radius = 0.01;
		double alpha = 0.5;
				
		double ndcgSum = 0;
		double tauSum = 0;
		
		for(int d = 0; d < datasetNames.length; d++) {								
			for(int k = 0; k < rankSize.length; k++) {									
				for(int n = 0; n < numKey.length; n++) {					

					String targetQuery = " ";
					String baselineQuery = " ";

					/* Verify if the baseline algorithm is correct */
					if(baseline.equals("skpq")) {
						String experimentName="recsys20";
						baselineQuery = "SELECT ROUND(avg(tau),2), ROUND(avg(ndcg),2) FROM `acm_recsys20`.`"+baseline+"` WHERE numKey="+ numKey[n] 
								+ " and k="+rankSize[k]+" and city ='"+datasetNames[d]+"' and radius="+radius+" and neighborhood_name ='range' and experimentName ='"+experimentName+"' order by keyword;";
					} else if (baseline.equals("inf")) {
						String experimentName="recsys20";
						baselineQuery = "SELECT ROUND(avg(tau),2), ROUND(avg(ndcg),2) FROM `acm_recsys20`.`"+"skpq"+"` WHERE numKey="+ numKey[n] 
								+ " and k="+rankSize[k]+" and city ='"+datasetNames[d]+"'and radius="+radius+" and neighborhood_name ='inf' and experimentName ='"+experimentName+"' order by keyword;";
					} else if (baseline.equals("hd")) {
						String experimentName="eswa21";
						baselineQuery = "SELECT ROUND(avg(tau),2), ROUND(avg(ndcg),2) FROM `acm_recsys20`.`"+"hdskpq"+"` WHERE numKey="+ numKey[n] 
								+ " and k="+rankSize[k]+" and city ='"+datasetNames[d]+"'and radius="+radius+" and experimentName ='"+experimentName+"' order by keyword;";
					} else {
						System.out.println("Inexistent baseline rank: " + baseline);
						System.exit(0);
					}

					/* Verify if the target algorithm is correct */
					if(proposal.equals("paretosearch")) {
						String experimentName="jis2020";
						targetQuery = "SELECT ROUND(avg(tau),2), ROUND(avg(ndcg),2) FROM `acm_recsys20`.`"+proposal+"` where numKey="+numKey[n]+" and k="+rankSize[k]+
								" and city='"+datasetNames[d]+"' and textSimilarity_methodName='default' and radius="+radius+" and alpha="+alpha
								+ " and experimentName ='"+experimentName+"' order by keyword;";											
					}else if(proposal.equals("skpq")) {
						targetQuery = "SELECT ROUND(avg(tau),2), ROUND(avg(ndcg),2) FROM `acm_recsys20`.`"+proposal+"` where numKey="+numKey[n]+" and k="+rankSize[k]+
								" and city='"+datasetNames[d]+"' and radius="+radius+" and neighborhood_name='paretorank' and experimentName ='recsys20' order by keyword;";
					}else if(proposal.equals("prr")) {
						
						String experimentName=" ";
						
						if(numKey.length == 1) {
							experimentName="recsys20";
						}else {
							experimentName="jis2020";
						}
						
						targetQuery = "SELECT ROUND(avg(tau),2), ROUND(avg(ndcg),2) FROM `acm_recsys20`.`"+proposal+"` where numKey="+numKey[n]+" and k="+rankSize[k]+
								" and city='"+datasetNames[d]+"' and textSimilarity_methodName='default' and radius="+radius+" and alpha="+alpha
								+ " and experimentName ='"+experimentName+"' order by keyword;";
					}else {
						System.out.println("Inexistent target rank: " + proposal);
						System.exit(0);
					}
					
//					System.out.println("Baseline: " + baselineQuery);
//					System.out.println("Target: " + targetQuery);
//					System.out.println();
					
					PreparedStatement sqlBaseline = conn.prepareStatement(baselineQuery);
					PreparedStatement sqlTarget = conn.prepareStatement(targetQuery);		

					ResultSet baselineResult = sqlBaseline.executeQuery();
					ResultSet targetResult = sqlTarget.executeQuery();				
					
					for(int i = 0; baselineResult.next();i++) {
						
						//0 --> NDCG || 1 --> Tau
						double[] base = new double[2];
						double[] pro = new double[2];
						
						targetResult.next();
						pro[0] = targetResult.getDouble("ROUND(avg(ndcg),2)");
						pro[1] = targetResult.getDouble("ROUND(avg(tau),2)");
						
//						System.out.println(pro[0]);
//						System.out.println("1 --- " + pro[1]);
						
						base[0] = baselineResult.getDouble("ROUND(avg(ndcg),2)");
						base[1] = baselineResult.getDouble("ROUND(avg(tau),2)");
						
//						System.out.println("\n B: " + pro[0]);
//						System.out.println("1 --- " + pro[1] + "\n");
						
						ndcgSum += ((pro[0] - base[0])/base[0])*100;					
						tauSum += ((pro[1] - base[1])/base[1])*100;						
					}
					
					sqlBaseline.close();
					sqlTarget.close();
				}
			}
		
		}
		
		System.out.println("Tau: " + tauSum/16);
		System.out.println("NDCG: " + ndcgSum/16);
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
		
		QueryEvaluation q = new QueryEvaluation();
		
		String[] datasetNames = {"Berlin", "London", "LosAngeles", "NewYork"};
		double radius = 0.01;
		
		double[] alpha = {0.5};
//		double[] alpha = {0.05,0.25,0.5,0.75,0.95};
		
//		int[] num_keys = {1};
		int[] num_keys = {2,3,4,5};
		
		int[] k = {10};
//		int[] k = {5,10,15,20};
		
		/*Baseline: skpq or inf or hd | Targets: skpq (Pareto re-order | a=0.5), or paretosearch (PSM), or PRR (numKey>1 and a=0.05)*/
//		q.pairedT_Test(20, "hd", "skpq", k, num_keys, datasetNames, radius, alpha,"jis2020");
		
		/* prr variando key é 'skpq', variando k é 'prr' (testado comparando com inf) */
		q.averageImprovement("inf", "skpq", k, num_keys);
		
//		int numKey = 5;
//		double radius = 0.01;		
		
		//Berlin
//		String keys[] = { "wheat", "software", "wedding", "herb", "door", "pen", "pension", "development", "resource", "eagle" };
//		String keys[] = {"amenity","natural","shop","bench","tourism","bicycle","information","waste","parking","berliner"};
//		String keys[] = { "amenity","natural","shop","bench","tourism","bicycle","information","waste","parking","berliner", "district", "software", "wedding", "herb", "door", "pen", "pension", "development", "resource", "eagle"};
//		String keys[] = {"amenity eagle","natural resource","shop development","bench pension","tourism pen","bicycle door","information herb","waste wedding","parking software","berliner district","district amenity","software natural","wedding shop","herb bench","door tourism","pen bicycle","pension information","development waste","resource parking","eagle berliner"};
//		String keys[] = {"amenity eagle berliner","natural resource parking","shop development waste","bench pension information","tourism pen bicycle","bicycle door tourism","information herb bench","waste wedding shop","parking software natural","berliner district amenity","district amenity eagle","software natural district","wedding shop software","herb bench wedding","door tourism herb","pen bicycle door","pension information pen","development waste pension","resource parking development","eagle berliner resource"};			
//		String keys[] = {"amenity eagle berliner district","natural resource parking software","shop development waste wedding","bench pension information herb","tourism pen bicycle door","bicycle door tourism pen","information herb bench pension","waste wedding shop development","parking software natural resource","berliner district amenity eagle","district amenity eagle berliner","software natural district parking","wedding shop software waste","herb bench wedding information","door tourism herb bicycle","pen bicycle door tourism","pension information pen bench","development waste pension shop","resource parking development natural","eagle berliner resource amenity"};
//		String keys[] = {"amenity eagle berliner district natural","natural resource parking software amenity","shop development waste wedding bench","bench pension information herb shop","tourism pen bicycle door information","bicycle door tourism pen waste","information herb bench pension tourism","waste wedding shop development parking","parking software natural resource bicycle","berliner district amenity eagle software","district amenity eagle berliner resource","software natural district parking berliner","wedding shop software waste eagle","herb bench wedding information development","door tourism herb bicycle pension","pen bicycle door tourism district","pension information pen bench wedding","development waste pension shop herb","resource parking development natural door","eagle berliner resource amenity pen"};
		
		//Los Angeles
//		String keys[] = { "architect", "colony", "publication", "family", "movie", "photography", "green", "pension", "nail", "week" };
//		String keys[] = {"amenity","avenue","road","north","east","west","south","boulevard","place","natural"};		
//		String keys[] = {"amenity","avenue","road","north","east","west","south","boulevard","place","natural","architect", "colony", "publication", "family", "movie", "photography", "green", "pension", "nail", "week" };
//		String keys[] = {"amenity avenue","road north","east west","south boulevard","place natural","architect colony","publication family","movie photography","green pension","nail week","amenity road","avenue north","east south","boulevard natural","architect publication","family photography","green nail","pension week","nail photography","week movie"};
//		String keys[] = {"amenity avenue road","road north boulevard","east west north","south boulevard natural","place natural amenity","architect colony avenue","publication family east","movie photography south","green pension architect","nail week colony","amenity road publication","avenue north family","east south movie","boulevard natural photography","architect publication green","family photography pension","green nail amenity","pension week nail","nail photography week","week movie amenity"};
//		String keys[] = {"amenity avenue road north","road north boulevard east","east west north amenity","south boulevard natural west","place natural amenity avenue","architect colony avenue road","publication family east south","movie photography south boulevard","green pension architect place","nail week colony natural","amenity road publication architect","avenue north family colony","east south movie publication","boulevard natural photography family","architect publication green movie","family photography pension green","green nail amenity photography","pension week nail amenity","nail photography week pension","week movie amenity nail"};
//		String keys[] = {"amenity avenue road north east","road north boulevard east west","east west north amenity south","south boulevard natural west amenity","place natural amenity avenue boulevard","architect colony avenue road place","publication family east south natural","movie photography south boulevard architect","green pension architect place colony","nail week colony natural publication","amenity road publication architect family","avenue north family colony movie","east south movie publication photography","boulevard natural photography family green","architect publication green movie pension","family photography pension green nail","green nail amenity photography week","pension week nail amenity avenue","nail photography week pension road","week movie amenity nail north"};
		
		//Madrid
//		String keys[] = { "route", "performance", "thought", "interface", "lose", "stop", "treatment", "city", "weight", "birthday" };
//		String keys[] = {"amenity","avenida","natural","shop","plaza","parking","calle","restaurant","arroyo","camino"};
		
		//London
//		String keys[] = { "agency", "phone", "nike", "aquarium", "crash", "secretary", "field", "medicine", "father", "tennis" };
//		String keys[] = {"amenity","shop","restaurant","close","street","road","avenue","drive","lane","pub"};
//		String keys[] = { "agency", "phone", "nike", "aquarium", "crash", "secretary", "field", "medicine", "father", "tennis","amenity","shop","restaurant","close","street","road","avenue","drive","lane","pub"};
//		String keys[] = {"amenity shop","shop restaurant","restaurant amenity","close street","street road","road close","avenue drive","drive lane","lane avenue","pub agency","agency phone","phone pub","nike aquarium","aquarium crash","crash nike","secretary field","field medicine","medicine secretary","father tennis","tennis amenity"};
//		String keys[] = {"amenity shop restaurant","shop restaurant close","restaurant amenity street","close street road","street road amenity","road close street","avenue drive road","drive lane avenue","lane avenue pub","pub agency drive","agency phone amenity","phone pub agency","nike aquarium phone","aquarium crash nike","crash nike secretary","secretary field medicine","field medicine crash","medicine secretary father","father tennis agency","tennis amenity phone"};
//		String keys[] = {"amenity shop restaurant agency","shop restaurant close phone","restaurant amenity street nike","close street road aquarium","street road amenity crash","road close street secretary","avenue drive road field","drive lane avenue medicine","lane avenue pub father","pub agency drive tennis","agency phone amenity pub","phone pub agency lane","nike aquarium phone drive","aquarium crash nike avenue","crash nike secretary road","secretary field medicine street","field medicine crash close","medicine secretary father restaurant","father tennis agency shop","tennis amenity phone shop"};
//		String keys[] = {"amenity shop restaurant agency pub","shop restaurant close phone lane","restaurant amenity street nike drive","close street road aquarium avenue","street road amenity crash road","road close street secretary restaurant","avenue drive road field street","drive lane avenue medicine close","lane avenue pub father shop","pub agency drive tennis amenity","agency phone amenity pub tennis","phone pub agency lane father","nike aquarium phone drive medicine","aquarium crash nike avenue field","crash nike secretary road shop","secretary field medicine street agency","field medicine crash close secretary","medicine secretary father restaurant crash","father tennis agency shop aquarium","tennis amenity phone shop nike"};
		
//		New York
//		String keys[] = { "importance", "food", "perspective", "concept", "resource", "queen", "chemistry", "apartment", "department", "database" };
//		String keys[] = {"amenity","shop","street","bicycle","place","natural","tree","road","avenue","drive"};
//		String keys[] = {"amenity","shop","street","bicycle","place","natural","tree","road","avenue","drive","importance", "food", "perspective", "concept", "resource", "queen", "chemistry", "apartment", "department", "database" };
//		String keys[] = {"amenity shop","street bicycle","place natural","tree road","avenue drive","importance food","perspective concept","resource queen","chemistry apartment","department database","amenity street","shop bicycle","place tree","natural road","avenue importance","drive food","perspective resource","concept queen","chemistry department","apartment database"};
//		String keys[] = {"amenity shop street","street bicycle amenity","place natural shop","tree road bicycle","avenue drive place","importance food natural","perspective concept tree","resource queen road","chemistry apartment avenue","department database drive","amenity street importance","shop bicycle food","place tree perspective","natural road concept","avenue importance resource","drive food queen","perspective resource chemistry","concept queen apartment","chemistry department database","apartment database department"};
//		String keys[] = {"amenity shop street bicycle","street bicycle amenity place","place natural shop amenity","tree road bicycle natural","avenue drive place tree","importance food natural road","perspective concept tree avenue","resource queen road drive","chemistry apartment avenue shop","department database drive street","amenity street importance food","shop bicycle food importance","place tree perspective concept","natural road concept perspective","avenue importance resource queen","drive food queen resource","perspective resource chemistry apartment","concept queen apartment chemistry","chemistry department database amenity","apartment database department food"};
//		String keys[] = {"amenity shop street bicycle place","street bicycle amenity place natural","place natural shop amenity tree","tree road bicycle natural road","avenue drive place tree avenue","importance food natural road drive","perspective concept tree avenue importance","resource queen road drive food","chemistry apartment avenue shop perspective","department database drive street concept","amenity street importance food resource","shop bicycle food importance queen","place tree perspective concept chemistry","natural road concept perspective apartment","avenue importance resource queen department","drive food queen resource database","perspective resource chemistry apartment amenity","concept queen apartment chemistry shop","chemistry department database amenity street","apartment database department food place"};
		
//		String keys[] = {"district"};		
//		String keys[] = { "agency", "phone", "nike", "aquarium", "crash", "secretary", "field", "medicine", "father", "tennis","amenity","shop","restaurant","close","street","road","avenue","drive","lane","pub"};
//		
		
//		String city = "LosAngeles";
//		
//		alpha=0.5;
//		q.evaluateQueriesGroup("ParetoSearch", keys, 20, city, numKey,radius,"");
//		q.evaluateQueriesGroup("InfluenceSearch", keys, 20, city, numKey,radius,"inf");
//		q.evaluateQueriesGroup("Pareto", keys, 20, city, numKey,radius,"paretoRank");		
//		q.evaluateQueriesGroup("SKPQ", keys, 20,city, numKey,radius,"range");		
	}

}
