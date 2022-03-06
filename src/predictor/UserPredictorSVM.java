package predictor;

import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class UserPredictorSVM {

	Instances dataset;
	LibSVM libsvm;

	public UserPredictorSVM(){
		 
		this.libsvm = new LibSVM();
		 
		try{
			DataSource source = new DataSource("diabetes.arff");
			this.dataset = source.getDataSet();
			//Carrega os 8 atributos do dataset, deixando o label de fora
			dataset.setClassIndex(dataset.numAttributes()-1);
		}catch(Exception e){
			System.out.println("Problema ao criar o dataset de treinamento");
		}
	}

	private void train() throws Exception{
		
		//SVM paramaters
		String[] options = new String[8];
		options[0] = "-S"; options[1] = "0";
		options[2] ="-K"; options[3] = "2";
		options[4] = "-G"; options[5] = "1.0";
		options[6] = "-C"; options[7] = "1.0";
		libsvm.setOptions(options);
		//Build it
		libsvm.buildClassifier(dataset);
		//Test it
//		Evaluation eval3 = new Evaluation(dataset);
//		eval3.evaluateModel(libsvm, dataset);

	}

	public double getPrediction(Instance description) throws Exception{
		return libsvm.classifyInstance(description);
	}

}
