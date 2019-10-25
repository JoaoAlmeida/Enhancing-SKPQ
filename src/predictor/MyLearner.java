package predictor;
/**
 * A Java class that implements a simple text learner, based on WEKA.
 * To be used with MyFilteredClassifier.java.
 * WEKA is available at: http://www.cs.waikato.ac.nz/ml/weka/
 * Copyright (C) 2013 Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 *
 * This program is free software: you can redistribute it and/or modify
 * it for any purpose.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.classifiers.Evaluation;
import java.util.Random;

import libsvm.svm;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
//import weka.classifiers.pmml.consumer.SupportVectorMachineModel;
import weka.core.converters.ArffLoader.ArffReader;
import java.io.*;

/**
 * This class implements a simple text learner in Java using WEKA.
 * It loads a text dataset written in ARFF format, evaluates a classifier on it,
 * and saves the learnt model for further use.
 * @author Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 * @see MyFilteredClassifier
 */
public class MyLearner {

	/**
	 * Object that stores training data.
	 */
	Instances trainData;
	/**
	 * Object that stores the filter
	 */
	StringToWordVector filter;
	/**
	 * Object that stores the classifier
	 */
	FilteredClassifier classifier;
	String userProfile;
	
	public MyLearner(String userProfile){
		this.userProfile = userProfile;	
		loadDataset("profiles/"+userProfile+".arff");
		learn();
	}
		
	/**
	 * This method loads a dataset in ARFF format. If the file does not exist, or
	 * it has a wrong format, the attribute trainData is null.
	 * @param fileName The name of the file that stores the dataset.
	 */
	public void loadDataset(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			ArffReader arff = new ArffReader(reader);
			trainData = arff.getData();
			System.out.println("===== Loaded dataset: " + fileName + " =====");
			reader.close();
		}
		catch (IOException e) {
			System.out.println("Problem found when reading: " + fileName + "Error: " + e);
		}
	}
	
	/**
	 * This method evaluates the classifier. As recommended by WEKA documentation,
	 * the classifier is defined but not trained yet. Evaluation of previously
	 * trained classifiers can lead to unexpected results.
	 * 
	 * The classifiers evaluation reported in "Personalizing the Top-k Spatial Keyword Preference Query with Textual Classifiers" employs this method.
	 * @throws Exception 
	 */
	public String evaluate() throws Exception {
		try {
			trainData.setClassIndex(0);
			filter = new StringToWordVector();
			filter.setAttributeIndices("last");
			classifier = new FilteredClassifier();
			classifier.setFilter(filter);
//			classifier.setClassifier(new NaiveBayes());
			//classifier.setClassifier(new LibSVM());
//			KNN
			classifier.setClassifier(new IBk());
//			Random Forest
//			classifier.setClassifier(new RandomForest());
//			Decision Tree
//			classifier.setClassifier(new J48());
//			Logistic Regression
//			classifier.setClassifier(new Logistic());
			
			filter.setInputFormat(trainData);
			Instances otp = Filter.useFilter(trainData, filter);

			
			Evaluation eval = new Evaluation(trainData);
			eval.crossValidateModel(classifier, trainData, 5, new Random(1));			
			String summary = eval.toSummaryString(true);
			System.out.println(summary);
			String details = eval.toClassDetailsString();
			System.out.println(details);
			//double accuracy = eval.pctCorrect();
			//System.out.println("Accuracy = " + accuracy);
			
//			double fmeasure = eval.fMeasure(classIndex);
//			System.out.println("F-measure = " + fmeasure);
			System.out.println("===== Evaluating on filtered (training) dataset done =====");
			return summary + "\n\n" + details;
		}
		catch (Exception e) {
			System.out.println("Problem found when evaluating. Error: " + e);
			return null;
		}
	}
	
	/**
	 * This method trains the classifier on the loaded dataset.
	 */
	public void learn() {
		try {
			String[] options = {"-C"};
			
			trainData.setClassIndex(0);
			filter = new StringToWordVector(5000);
			filter.setAttributeIndices("last");
			filter.setOptions(options);
			classifier = new FilteredClassifier();
			classifier.setFilter(filter);
			classifier.setClassifier(new NaiveBayes());
			classifier.buildClassifier(trainData);
			// Uncomment to see the classifier
			// System.out.println(classifier);
			System.out.println("===== Training on filtered (training) dataset done =====");
		}
		catch (Exception e) {
			System.out.println("Problem found when training");
		}
	}
	
	/**
	 * This method saves the trained model into a file. This is done by
	 * simple serialization of the classifier object.
	 * @param fileName The name of the file that will store the trained model.
	 */
	public void saveModel(String fileName) {
		try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(classifier);
            out.close();
 			System.out.println("===== Saved model: " + fileName + " =====");
        } 
		catch (IOException e) {
			System.out.println("Problem found when writing: " + fileName);
		}
	}
	
	public double classifyInstance2(int index) throws Exception{
	
		loadDataset("bFrente.arff");
		learn();
		
		 Instances unlabeled = new Instances(
                 new BufferedReader(
                   new FileReader("unlabeled.arff")));

		// set class attribute
		unlabeled.setClassIndex(0);
		
		// create copy
		Instances labeled = new Instances(unlabeled);
		
		// label instances		
		double clsLabel = classifier.classifyInstance(unlabeled.instance(index));
		
		labeled.instance(0).setClassValue(clsLabel);
		
		// save labeled data
		BufferedWriter writer = new BufferedWriter(
		                   new FileWriter("labeled.arff"));
		
		writer.write(labeled.toString());
		writer.newLine();
		writer.flush();
		writer.close();
		
		return clsLabel;
	}	
	
	public double classifyHotel(String hotelProfile) throws Exception{
//		System.out.println("Hotel name: " + hotelProfile);
		double count = 0;
		Instances unlabeled = null;
		
		try{
		 unlabeled = new Instances(
                 new BufferedReader(
                   new FileReader("profiles/hotels/"+hotelProfile+".arff")));
		}catch(Exception IOException){
			 System.out.println("Hotel does not have reviews yet! " + IOException + "\n");
			 return 0;
		 }
		// set class attribute
		unlabeled.setClassIndex(0);
		
		// create copy
		Instances labeled = new Instances(unlabeled);
		
		// label instances		
		for(int i = 0; i < unlabeled.numInstances(); i++){
			double clsLabel = classifier.classifyInstance(unlabeled.instance(i));
//			System.out.println(clsLabel);
			//pode remover a linha abaixo. Não preciso armazenar essa classificação
			labeled.instance(i).setClassValue(clsLabel);
//			System.out.println(clsLabel);
			if(clsLabel == 0){
				count--;
			}else{
				count++;
			}			
		}					
		
		// save labeled data
//		BufferedWriter writer = new BufferedWriter(
//		                   new FileWriter("labeledOrchid.arff"));
//		
//		writer.write(labeled.toString());
//		writer.newLine();
//		writer.flush();
//		writer.close();
		
		return count/unlabeled.numInstances();
	}	
	
	public double classifyInstance(String description) throws Exception{
		
		FastVector atts;
		Instances data;
		
		FastVector classes = new FastVector();

		classes.addElement(1);
		classes.addElement(5);
				
		double[] vals;

		atts = new FastVector();
	
//		atts.addElement(new Attribute("class"));
		atts.addElement(new Attribute("description", (FastVector) null));

		data = new Instances("Classifying Instance", atts, 0);
		data.setClassIndex(0);
		
		
		vals = new double[data.numAttributes()];		

		vals[0] = data.attribute(0).addStringValue(description);
//		vals[0] = 1;

		data.add(new Instance(0, vals));			
						
		System.out.println(data.toString());
		return classifier.classifyInstance(data.instance(0));
	}	
	
	/**
	 * Main method. It is an example of the usage of this class.
	 * @param args Command-line arguments: fileData and fileModel.
	 * @throws Exception 
	 */
	public static void main (String[] args) throws Exception {
		
		MyLearner learner;
//		if (args.length < 1)
//			System.out.println("Usage: java MyLearner <fileData> <fileModel>");
//		else {
		//room value location service clean
		
		String[] experiment = {"room", "value", "location", "service", "clean"};
		
		learner = new MyLearner(experiment[4]);
		
//			learner.loadDataset("profiles/room.arff");

			// Evaluation must be done before training
			// More info in: http://weka.wikispaces.com/Use+WEKA+in+your+Java+code
			learner.evaluate();
			//learn já é feito no construtor
//			learner.learn();
			//System.out.println("Resultado -> " + learner.classifyHotel("Al Bustan Beach Hotel"));
//			learner.saveModel("output.txt");
//		}
	}
}	