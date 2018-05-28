package weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Generates a little ARFF file with different attribute types.
 *
 * @author FracPete
 * @author João Paulo
 */
public class WEKADatasetBuilder {

	Instances data;

	public WEKADatasetBuilder() {
		create();
	}

	private void create() {

		FastVector atts = new FastVector();

		//Attributes
		atts.addElement(new Attribute("description", (FastVector) null));
		atts.addElement(new Attribute("rate"));

		data = new Instances("WEKADataRelation", atts, 0);
	}

	public void createInstance(String description, double rate) {

		double[] values = new double[data.numAttributes()];

		values[0] = data.attribute(0).addStringValue(description);
		values[1] = rate;
		// Instance parameters --> Instances(weight, attributes)
		data.add(new Instance(1.0, values));
	}

	/*
	 * This description file contains DBPedia and LinkedGeodata descriptions of
	 * the same objects
	 */
	public void importDescriptions(String filePath) throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(filePath)), "ISO-8859-1")));		

		String line = reader.readLine();

		while (line != null) {

			if (line.contains("Object")) {

				String[] lineVec = line.split("spatial#Feature");

				//ratings2 pq havia um hotel que o nome era apenas "Hotel", entao modifiquei
				BufferedReader label = new BufferedReader(
						(new InputStreamReader(new FileInputStream(new File("ratings2.txt")), "ISO-8859-1")));
				
				String labelStr = label.readLine();
				double rate = 0;
				boolean contain = false;
				
//				System.out.println(lineVec[1]);
				//Precisa pegar o label de forma mais adequada, contains é fraco
				while (labelStr != null) {					
					if (lineVec[1].trim().contains(labelStr.split("\\[")[0].trim())) {												
						contain = true;
						String[] labelStrVec = labelStr.split(" ");
						if(labelStrVec[labelStrVec.length - 1].equals("Empty")){
							rate = 0;
						}else{
							rate = Double.valueOf(labelStrVec[labelStrVec.length - 1]);
							if(rate > 0){
								createInstance(lineVec[1].trim(), rate);
							}
							
						}						
//						createInstance(lineVec[1].trim(), rate);						
					}														

					labelStr = label.readLine();
				}
				
				if(!contain){
					
//					createInstance(lineVec[1].trim(), 0);
				}
				label.close();
			}
			line = reader.readLine();			
		}
		reader.close();		
	}

	public void saveARFFFile() throws IOException {

		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File("WEKADataset.arff"));
		saver.writeBatch();
	}

	public void loadArrfFile(String filePath) throws Exception {

		if (data.numInstances() == 0) {
			DataSource source = new DataSource(filePath);
			data = source.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);
		} else {
			System.out.println("There is another data loaded!");
		}
	}

	public static void main(String[] args) throws Exception {

		WEKADatasetBuilder w = new WEKADatasetBuilder();

		w.importDescriptions("descriptions.txt");
		w.saveARFFFile();

		// FastVector atts;
		// FastVector attsRel;
		// FastVector attVals;
		// FastVector attValsRel;
		// Instances data;
		// Instances dataRel;
		// double[] vals;
		// double[] valsRel;
		// int i;
		//
		// // 1. set up attributes
		// //Atributos são colocados em FastVector
		// atts = new FastVector();
		// // - numeric
		// atts.addElement(new Attribute("att1"));
		// // - nominal
		// attVals = new FastVector();
		// for (i = 0; i < 5; i++)
		// attVals.addElement("val" + (i+1));
		// atts.addElement(new Attribute("att2", attVals));
		// // - string
		// atts.addElement(new Attribute("att3", (FastVector) null));
		// // - date
		// atts.addElement(new Attribute("att4", "yyyy-MM-dd"));
		// // - relational
		// attsRel = new FastVector();
		// // -- numeric
		// attsRel.addElement(new Attribute("att5.1"));
		// // -- nominal
		// attValsRel = new FastVector();
		// for (i = 0; i < 5; i++)
		// attValsRel.addElement("val5." + (i+1));
		// attsRel.addElement(new Attribute("att5.2", attValsRel));
		// dataRel = new Instances("att5", attsRel, 0);
		// atts.addElement(new Attribute("att5", dataRel, 0));
		//
		// // 2. create Instances object
		// data = new Instances("MyRelation", atts, 0);
		//
		// // 3. fill with data
		// // first instance
		// vals = new double[data.numAttributes()];
		// System.out.println("Num. de atributos " + data.numAttributes());
		// // - numeric
		// vals[0] = Math.PI;
		// // - nominal
		// vals[1] = attVals.indexOf("val3");
		// // - string
		// vals[2] = data.attribute(2).addStringValue("This is a string!");
		// // - date
		// vals[3] = data.attribute(3).parseDate("2001-11-09");
		// // - relational
		// dataRel = new Instances(data.attribute(4).relation(), 0);
		// // -- first instance
		// valsRel = new double[2];
		// valsRel[0] = Math.PI + 1;
		// valsRel[1] = attValsRel.indexOf("val5.3");
		// dataRel.add(new Instance(1.0, valsRel));
		// // -- second instance
		// valsRel = new double[2];
		// valsRel[0] = Math.PI + 2;
		// valsRel[1] = attValsRel.indexOf("val5.2");
		// dataRel.add(new Instance(1.0, valsRel));
		// vals[4] = data.attribute(4).addRelation(dataRel);
		// // add
		// data.add(new Instance(1.0, vals));
		//
		// // second instance
		// vals = new double[data.numAttributes()]; // important: needs NEW
		// array!
		// // - numeric
		// vals[0] = Math.E;
		// // - nominal
		// vals[1] = attVals.indexOf("val1");
		// // - string
		// vals[2] = data.attribute(2).addStringValue("And another one!");
		// // - date
		// vals[3] = data.attribute(3).parseDate("2000-12-01");
		// // - relational
		// dataRel = new Instances(data.attribute(4).relation(), 0);
		// // -- first instance
		// valsRel = new double[2];
		// valsRel[0] = Math.E + 1;
		// valsRel[1] = attValsRel.indexOf("val5.4");
		// dataRel.add(new Instance(1.0, valsRel));
		// // -- second instance
		// valsRel = new double[2];
		// valsRel[0] = Math.E + 2;
		// valsRel[1] = attValsRel.indexOf("val5.1");
		// dataRel.add(new Instance(1.0, valsRel));
		// vals[4] = data.attribute(4).addRelation(dataRel);
		// // add
		// data.add(new Instance(1.0, vals));
		//
		// // 4. output data
		// System.out.println(data);
	}
}