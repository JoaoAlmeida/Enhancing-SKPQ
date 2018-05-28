package weka;



import java.io.File;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TestingSVM {

	public TestingSVM(){
		
	}
	
	public static void main(String[] args) throws Exception{
		
		
		DataSource source = new DataSource("WEKADataset.arff");
		Instances dataset = source.getDataSet();
		dataset.setClassIndex(dataset.numAttributes()-1);
		
		StringToWordVector filter = new StringToWordVector();    
		filter.setWordsToKeep(1000000);
		
		filter.setIDFTransform(true);
		
		filter.setTFTransform(true);
		filter.setLowerCaseTokens(true);
		filter.setOutputWordCounts(true);
		
		filter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL,StringToWordVector.TAGS_FILTER));
		
		NGramTokenizer t = new NGramTokenizer();
	    t.setNGramMaxSize(3);
	    t.setNGramMinSize(2);    
	    filter.setTokenizer(t);     

	    filter.setStopwords(new File("stopwords/english-stop-words.txt"));
	    	    
	        Stemmer s = new /*Iterated*/LovinsStemmer();
	        filter.setStemmer(s);
	    
	    
	    filter.setInputFormat(dataset);
	    
	    filter.batchFinished();
	    dataset = Filter.useFilter(dataset, filter);
	    		
		
		LibSVM libsvm = new LibSVM();
		String[] options = new String[8];
		options[0] = "-S"; options[1] = "0";
		options[2] ="-K"; options[3] = "2";
		options[4] = "-G"; options[5] = "1.0";
		options[6] = "-C"; options[7] = "1.0";
	    libsvm.setOptions(options);
	    libsvm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_EPSILON_SVR, LibSVM.TAGS_SVMTYPE));
		libsvm.buildClassifier(dataset);
		
		Evaluation eval3 = new Evaluation(dataset);
		eval3.evaluateModel(libsvm, dataset);
		System.out.println(eval3.toSummaryString());
	
		double[]vals = new double[dataset.numAttributes()];
		
//		vals[0] = dataset.attribute(0).addStringValue("Hotel TourismThing Node Al Rayaan Hotel");
////		DenseInstance teste = new DenseInstance(8, vals);
////		
//		System.out.println("INst:" + libsvm.classifyInstance(new Instance(1.0, vals)));
		System.out.println("INst:" + libsvm.classifyInstance(dataset.instance(89)));
	}
}
