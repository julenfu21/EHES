package Labo3_4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.ChebyshevDistance;
import weka.core.EuclideanDistance;
import weka.core.FilteredDistance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.MinkowskiDistance;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.neighboursearch.LinearNNSearch;
import weka.estimators.KKConditionalEstimator;

public class Labo3ParamEkorketa {

	public static void main(String[] args) throws Exception {
        
        //DATUAK KARGATU
        DataSource source = new DataSource(args[0]);
        Instances data = source.getDataSet();
        if(data.classIndex() == - 1)
            data.setClassIndex(data.numAttributes() - 1);
        
        //Parametro optimoak gordetzeko zerrenda eta beharrezko hasieraketak
        ArrayList<String> paramOptimoak = new ArrayList<String>();
        int iterazioKop = 10;
        int max = data.numInstances() / 4;
        int urratsa = max / iterazioKop;
        double fMeasureMax = 0.0;
        
        ArrayList<LinearNNSearch> distFuntzioak = new ArrayList<LinearNNSearch>();
        LinearNNSearch chebyshevDistance = new LinearNNSearch();
        chebyshevDistance.setDistanceFunction(new ChebyshevDistance());
        distFuntzioak.add(chebyshevDistance);
        LinearNNSearch euclideanDistance = new LinearNNSearch();
        euclideanDistance.setDistanceFunction(new EuclideanDistance());
        distFuntzioak.add(euclideanDistance);
        LinearNNSearch filteredDistance = new LinearNNSearch();
        filteredDistance.setDistanceFunction(new FilteredDistance());
        distFuntzioak.add(filteredDistance);
        LinearNNSearch manhattanDistance = new LinearNNSearch();
        manhattanDistance.setDistanceFunction(new ManhattanDistance());
        distFuntzioak.add(manhattanDistance);
        LinearNNSearch minkowskiDistance = new LinearNNSearch();
        minkowskiDistance.setDistanceFunction(new MinkowskiDistance());
        distFuntzioak.add(minkowskiDistance);
        
//        Tag w1 = new Tag(IBk.WEIGHT_NONE, "WEIGHT_NONE");       //No distance weighting
//        Tag w2 = new Tag(IBk.WEIGHT_INVERSE, "WEIGHT_INVERSE");    //Weight by 1/distance
//        Tag w3 = new Tag(IBk.WEIGHT_SIMILARITY, "WEIGHT_SIMILARITY"); //Weight by 1-distance
        Tag[] distanceWeighting = IBk.TAGS_WEIGHTING;
        
        //EBALUATZERAKOAN KLASE MINORITARIOA EZ DA LORTZEN --> PRECISION, RECALL = 0 --> FMEASURE = 0 -->  Weighted f-measure erabiliko dugu
//        //Klase minoritarioaren id-a lortu (KASU HONETAN --> B (ID = 1))
//        int[] klaseMaiztasunak = data.attributeStats(data.classIndex()).nominalCounts;
//        int klaseMinId = 0;
//        int unekoMaiztasunMin = klaseMaiztasunak[0];
//        for(int i = 1; i < klaseMaiztasunak.length; i++) {
//        	if(klaseMaiztasunak[i] < unekoMaiztasunMin) {
//        		unekoMaiztasunMin = klaseMaiztasunak[i];
//        		klaseMinId = i;
//        	}        		
//        }
        
        //Auzokide kopurua = k (KNN)
        int kOpt = 0;
        //Metrika = d (nearestNeighbourSearchAlgorithm --> distanceFunction)
        LinearNNSearch dOpt = null;
        //Distantziaren ponderazio faktorea = w (distanceWeighting)
        SelectedTag wOpt = null;
        

        //10-FCV KNN-ren PARAM. EKORKETA EGITEKO
        //**IR HASTA NUMINSTANCES = ZEROR [NO ES ÓPTIMO // KONTZEPTUALKI EZ ZUZENA]
        for(int k = 1; k <= max; k = k + urratsa) {
        	Iterator<LinearNNSearch> itr = distFuntzioak.iterator();
        	while(itr.hasNext()) {
        		LinearNNSearch unekoDistFuntzioa = itr.next();
        		for(int w = 0; w < distanceWeighting.length; w++) {
	        		//bukle para métrica (BUSCAR VALORES POSIBLES)
		        		//bucle para distanceWeighting (BUSCAR VALORES POSIBLES)
		        			//klase minoritarioa lortu eta f-measure aztertu
		        			//if x > currentOPTFMeasure --> balioOptimoak eguneratu
		            //SAILKATZAILEA/ENTRENAMENDUA
		            IBk iBk = new IBk();
		            iBk.setKNN(k);
		            iBk.setNearestNeighbourSearchAlgorithm(unekoDistFuntzioa);
		            SelectedTag unekoDistanceWeighting = new SelectedTag(distanceWeighting[w].getID(), distanceWeighting);
		            iBk.setDistanceWeighting(unekoDistanceWeighting);
		            iBk.buildClassifier(data);
		            
		            //EBALUAZIOA
		            Evaluation ev = new Evaluation(data);
		            ev.crossValidateModel(iBk, data, 5, new Random(3));
		            //DATUAK ESKURATU
		        	System.out.println(ev.toSummaryString());
		        	System.out.println(ev.toClassDetailsString());
		        	System.out.println(ev.toMatrixString());
		        	
		        	//System.out.println("KLASE MINORITARIOAREN ('B') F-MEASURE: " + ev.fMeasure(2));
		        	//if x > currentOPTFMeasure --> balioOptimoak eguneratu
		        	//System.out.println(ev.fMeasure(klaseMinId));
		        	System.out.println("f-measure AVG: " + ev.weightedFMeasure());
		        	System.out.println("k: " + k);
		        	System.out.println("Distantzia funtzioa: " + unekoDistFuntzioa.getDistanceFunction().getClass().toString());
		        	System.out.println("Distance weighting: " + unekoDistanceWeighting.getSelectedTag().getReadable());
		        	System.out.println("Distance weighting V2: " + iBk.getDistanceWeighting().getSelectedTag().getReadable());
		            if(ev.weightedFMeasure() > fMeasureMax) {
		            	fMeasureMax = ev.weightedFMeasure();
		            	kOpt = k;
		            	dOpt = unekoDistFuntzioa;
		            	wOpt = unekoDistanceWeighting;
		            } 
	        	}
	        	System.out.println("\n--------------------------------------------------------------\n");
        	}
        }
        
        System.out.println("f-measure AVG (max): " + fMeasureMax);
        System.out.println("k OPTIMOA: " + kOpt);
        System.out.println("d OPTIMOA: " + dOpt.getDistanceFunction().getClass().toString());
        System.out.println("w OPTIMOA: " + wOpt.getSelectedTag().getReadable());
        
        //SAILKATZAILEARI PARAMETRO OPTIMOAK ESLEITU
        IBk iBkOPT = new IBk();
        iBkOPT.setKNN(kOpt);
        iBkOPT.setNearestNeighbourSearchAlgorithm(dOpt);
        iBkOPT.setDistanceWeighting(wOpt);
        //EBALUAZIOA
        Evaluation evOPT = new Evaluation(data);
        evOPT.crossValidateModel(iBkOPT, data, 5, new Random(3));
        //DATUAK ESKURATU
    	System.out.println(evOPT.toSummaryString());
    	System.out.println(evOPT.toClassDetailsString());
    	System.out.println(evOPT.toMatrixString());
    }

}
