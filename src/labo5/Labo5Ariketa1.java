package labo5;

import java.io.File;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.StratifiedRemoveFolds;
import weka.filters.unsupervised.attribute.ReplaceWithMissingValue;
import weka.filters.unsupervised.instance.Randomize;
import weka.filters.unsupervised.instance.RemovePercentage;

public class Labo5Ariketa1 {
	
	public static void main(String[] args) throws Exception {
		////ARGUMENTUAK: 

		// args[0] = "data.arff": jatorrizko datuen path (input)			 /home/jfu/Descargas/data_supervised.arff
		// args[1] = "train.arff" datuak gordetzeko path (output)
		// args[2] = "test_blind.arff" datuak gordetzeko path (output)

		
		////DATUAK KARGATU
		DataSource source = new DataSource(args[0]);
		Instances data = source.getDataSet();
		if(data.classIndex() == -1)
			data.setClassIndex(data.numAttributes() - 1);
		
		
		////TRAIN eta TEST multzoak lortu
		
		//TRAIN multzoa lortu (%70) --> ERA GAINBEGIRATUAN
		Instances train = null;
		Instances unekoFold = null;
		StratifiedRemoveFolds srf = new StratifiedRemoveFolds();
		srf.setNumFolds(10);
		for(int f = 1; f <= 7; f++) { //iterazio bakoitzean datuen %10 hartu (%10 * 7 = %70) 
			srf.setInputFormat(data);
			srf.setFold(f);
			srf.setInvertSelection(false);
			unekoFold = Filter.useFilter(data, srf);
			if(train == null) { //train daturik gabe dagoenean (1. iterazioan soilik)
				train = unekoFold;
			}
			else {
				for(int i = 0; i < unekoFold.numInstances(); i++) {
					train.add(unekoFold.get(i));
				}
			}
		}
		
		//TEST multzoa lortu (%30) --> ERA EZ-GAINBEGIRATUAN
		RemovePercentage rp = new RemovePercentage();
		rp.setInputFormat(data);
		rp.setInvertSelection(false);
		rp.setPercentage(70);
		Instances test = Filter.useFilter(data, rp);
		System.out.println(data.numInstances());
		System.out.println(train.numInstances());
		System.out.println(test.numInstances());
		
		//TEST multzoko instantzien klaseak '?' bihurtu
		
//ZERGATIK EZ DOA??!!??
//		ReplaceWithMissingValue misValFilter = new ReplaceWithMissingValue();
//		misValFilter.setAttributeIndices(String.valueOf(data.classIndex()));
//		misValFilter.setProbability(1);
//		misValFilter.setInputFormat(test);
//		Instances testBlind = Filter.useFilter(test, misValFilter);
		for(int i = 0; i < test.numInstances(); i++) {
			test.instance(i).setClassMissing();
			
		}
		
		////TRAIN eta TEST multzoak esportatu 
		
		ArffSaver saveTrain = new ArffSaver();
		saveTrain.setInstances(train);
		saveTrain.setFile(new File(args[1]));
		saveTrain.writeBatch();
				
		ArffSaver saveTestBlind = new ArffSaver();
		//saveTestBlind.setInstances(testBlind);
		saveTestBlind.setInstances(test);
		saveTestBlind.setFile(new File(args[2]));
		saveTestBlind.writeBatch();	
	}

}
