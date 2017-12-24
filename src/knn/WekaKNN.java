package knn;

//https://weka.wikispaces.com/Use+WEKA+in+your+Java+code
//http://weka.wikispaces.com/Use+Weka+in+your+Java+code
//hote code: http://forums.pentaho.com/showthread.php?198222-Numerical-and-nominal

//http://www.programcreek.com/java-api-examples/index.php?api=weka.classifiers.CheckClassifier

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import node.IConstants;
import node.NodePrediction;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class WekaKNN {
	
	public static void main(String[] args) {
		try {
			classifyWithOutCrossValidation(2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BufferedReader readDataFile(String filename) {

		BufferedReader inputReader = null;

		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}

		return inputReader;
	}

	/**
	 * @param model
	 * @param trainingSet
	 * @param testingSet
	 * @return
	 * @throws Exception
	 */
	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {

		Evaluation evaluation = new Evaluation(trainingSet);

		model.buildClassifier(trainingSet);
		
		//evaluation.evaluateModel(model, testingSet);
		double value = evaluation.evaluateModel(model, testingSet)[0];
		//System.out.println("aqui       "+value);
		
		

		return evaluation;
	}

	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;
		double wrong = 0;

		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);

			if (np.predicted() == np.actual()) {
				correct++;
			} else {
				wrong++;
			}
		}

		return 100 * correct / predictions.size();
	}

	public static Instances[][] crossValidationSplit(Instances data,
			int numberOfFolds) {
		Instances[][] split = new Instances[2][numberOfFolds];

		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}

		return split;
	}
	
	
	/**
	 * @param fold
	 * @throws Exception fred
	 */
	public static List<NodePrediction> classifyWithOutCrossValidation(Instances[] datasets) throws Exception {

		Instances data = datasets[0];
		Instances data2 = datasets[1];
		
		//System.out.println(data);
		//System.out.println(data2);
		
		
		data.setClassIndex(data.numAttributes() - 1);
		data2.setClassIndex(data2.numAttributes() - 1);	
	
		// creates a filter to normalize string to number
		if (data.checkForStringAttributes()) {
			StringToWordVector filter = new StringToWordVector();
			filter.setIDFTransform(true);
			filter.useStoplistTipText();
			SnowballStemmer steemer = new SnowballStemmer();
			filter.setStemmer(steemer);
			filter.setLowerCaseTokens(true);
			try {
				filter.setInputFormat(data);				
				data = Filter.useFilter(data, filter);
				//System.out.println(data.numAttributes());
				//System.out.println(data);
				data2 = Filter.useFilter(data2, filter);
				//System.out.println(data.numAttributes());
				//System.out.println(data2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		
		
		
		
		

		
		
/*System.out.println(data.classIndex());
System.out.println(data.attribute(data.classIndex()).toString());
System.out.println(data2.classIndex());
System.out.println(data2.numAttributes());
System.out.println(data2.toSummaryString());*/
		
		
		/*
		 * Classifier[] models = { new J48(), // a decision tree new PART(), new
		 * DecisionTable(),//decision table majority classifier new
		 * DecisionStump() //one-level decision tree };
		 */
		

		IBk knn = new IBk(IConstants.K);
		
		
		//Classifier knn = new IBk();
		//Classifier knn = new J48();
		
		//knn = new EuclideanDistance();

		FastVector predictions = new FastVector();
		
		Evaluation validation = null;
		
		validation = classify(knn, data, data2);
		
		List<NodePrediction> nodePredictions = new ArrayList<NodePrediction>();		
		
		try {
			predictions.appendElements(validation.predictions());
			


			for (int i = 0; i < data2.numInstances(); i++) {
				
				Prediction prediction = (Prediction)predictions.elementAt(i);
				Instance instance = data2.instance(i);
				
				//System.out.println(instance.classAttribute().value((int) prediction.predicted()));
				//System.out.println(prediction.predicted());
				
				
				
				//if (prediction.predicted()>=ILabel.semanticSimilarityThreshold) {
				if (true) {
					NodePrediction nodePrediction = new NodePrediction(
							instance.stringValue(instance.classIndex()),
							instance.classAttribute().value((int) prediction.predicted()),prediction.predicted());
					        nodePredictions.add(nodePrediction);	
					
				}else{
					NodePrediction nodePrediction = new NodePrediction(
							instance.stringValue(instance.classIndex()),IConstants.NO_LABEL,prediction.predicted());
					        nodePredictions.add(nodePrediction);	
				}
				
		        
		        
		        
		    }			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		double accuracy = calculateAccuracy(predictions);
		//System.out.println("Accuracy of " + knn.getClass().getSimpleName()+ ": " + String.format("%.2f%%", accuracy)+ "\n---------------------------------");
		
/*		System.out.println(validation.toSummaryString("\nResults\n\n", false));
		System.out.println(validation.toMatrixString());
		System.out.println(validation.toClassDetailsString());*/
		
		
		return nodePredictions;
	}		
	
	
	/**
	 * @param fold
	 * @throws Exception
	 */
	public static void classifyWithOutCrossValidation(Instances trainingData,Instances testData)
			throws Exception {

		Instances data = trainingData;
		Instances data2 = testData;

		data.setClassIndex(data.numAttributes() - 1);
		data2.setClassIndex(data2.numAttributes() - 1);

		Classifier knn = new IBk();
		FastVector predictions = new FastVector();
		Evaluation validation = classify(knn, data, data2);
		System.out.println("TRAINING DATA: \n " + data);
		System.out.println();
		System.out.println("TEST DATA: \n " + data2);

		predictions.appendElements(validation.predictions());
		double accuracy = calculateAccuracy(predictions);
		System.out.println("Accuracy of " + knn.getClass().getSimpleName()
				+ ": " + String.format("%.2f%%", accuracy)
				+ "\n---------------------------------");
	}	

	/**
	 * @param fold
	 * @throws Exception
	 */
	public static void classifyWithOutCrossValidation(int fold)
			throws Exception {

		BufferedReader datafile = readDataFile("d1.txt");
		Instances data = new Instances(datafile);
		data.setClassIndex(data.numAttributes() - 1);

		BufferedReader datafile2 = readDataFile("d2.txt");
		Instances data2 = new Instances(datafile2);
		data2.setClassIndex(data2.numAttributes() - 1);
		


		

		if (data.checkForStringAttributes()) {
			StringToWordVector filter = new StringToWordVector();
			//filter.setWordsToKeep(data.numAttributes());
			
					
			try {
				filter.setInputFormat(data);
				
				data = Filter.useFilter(data, filter);
				System.out.println(data.numAttributes());
				System.out.println(data);			
				
				//filter.setInputFormat(data2);
				
				data2 = Filter.useFilter(data2, filter);
				System.out.println(data2.numAttributes());
				System.out.println(data2);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		

		Classifier knn = new IBk();
		
		FastVector predictions = new FastVector();
		
		
		
		Evaluation validation = classify(knn, data, data2);
		//System.out.println("TRAINING DATA: \n " + data);
		//System.out.println();
		//System.out.println("TEST DATA: \n " + data2);

		predictions.appendElements(validation.predictions());
		
		
		
		for (int i = 0; i < data2.numInstances(); i++) {
		        Instance instance = data2.instance(i);
		        Prediction prediction = (Prediction)predictions.elementAt(i);
		        

		        
		        System.out.println();
		        System.out.println();
		        System.out.println("Expected:"+instance +" but predicted "+ instance.classAttribute().value((int) prediction.predicted()));
		    }
		
		
		
		double accuracy = calculateAccuracy(predictions);
		System.out.println("Accuracy of " + knn.getClass().getSimpleName()+ ": " + String.format("%.2f%%", accuracy)+ "\n---------------------------------");
		
		//System.out.println(validation.toSummaryString("\nResults\n\n", false));
		//System.out.println(validation.toMatrixString());
		//System.out.println(validation.toClassDetailsString());
		
		
		
		
		
	}

	/**
	 * @param numOfFolds
	 * @throws Exception
	 */
	public static void classifyWithCrossValidation(int numOfFolds) throws Exception {

		BufferedReader datafile = readDataFile("dataset.txt");

		Instances data = new Instances(datafile);
		data.setClassIndex(data.numAttributes() - 1);

		
		// Do 10-split cross validation
		Instances[][] split = crossValidationSplit(data, numOfFolds);

		// Separate split into training and testing arrays
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];

		// Use a set of classifiers
		/*
		 * Classifier[] models = { new J48(), // a decision tree new PART(), new
		 * DecisionTable(),//decision table majority classifier new
		 * DecisionStump() //one-level decision tre	e };
		 */

		Classifier[] models = {new 	IBk()};		
		 
		// Run for each model
		for (int j = 0; j < models.length; j++) {
 
			// Collect every group of predictions for current model in a FastVector
			FastVector predictions = new FastVector();
 
			// For each training-testing split pair, train and test the classifier
			for (int i = 0; i < trainingSplits.length; i++) {
				Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);
				predictions.appendElements(validation.predictions());
			}
 
			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracy(predictions);
 
			// Print current classifier's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
					+ String.format("%.2f%%", accuracy)
					+ "\n---------------------------------");
		}
	}

}
