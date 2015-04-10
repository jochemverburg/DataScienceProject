package relationExtraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.Pair;

/**
 * Can analyze extracted pairs of relations using the ground truth of relations.
 * Input (both extracted relations and ground truth) has to be given as CoNNL without duplicates:
 * The first column contains the left part of the relation and the second column the right part of the relation.
 * Every line represents a different entity.
 */
public class REAnalyzer {

	public static final String COLUMN_DELIM = "\t";
	/**
	 * @require RELATION_SIZE==2
	 */
	public static final int RELATION_SIZE = 2;
	
	private List<Pair<String, String>> extractedPairs;
	private List<Pair<String, String>> groundTruthPairs;
	private double precision;
	private double recall;
	
	/**
	 * @param analyzedPath The path containing the extracted relations in described format.
	 * @param groundTruthPath The path containing the relations of the ground truth in described format.
	 * @throws IOException 
	 */
	public REAnalyzer(String analyzedPath, String groundTruthPath) throws IOException{
		extractedPairs = getRelationsFromFile(analyzedPath);
		groundTruthPairs = getRelationsFromFile(groundTruthPath);
		precision = -1.0;
		recall = -1.0;
	}
	
	/**
	 * Puts all the relations from the file into a List of Pairs.
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private static List<Pair<String,String>> getRelationsFromFile(String filePath) throws IOException{
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		BufferedReader reader = new BufferedReader(
				new FileReader( 
						new File(filePath)));
		String text = null;	
		
		while ((text = reader.readLine()) != null) {
			if(!text.isEmpty()){
				String[] splitted = text.split(COLUMN_DELIM);
				if(splitted.length>=RELATION_SIZE){
					Pair<String,String> temp = new Pair<String,String>(splitted[0], splitted[1]);
					result.add(temp);
				}
			}
		}
		   
	    reader.close();
	    
		return result;
	}
		
	public double getPrecision(){
  		if(precision<0){
			int total = extractedPairs.size();
			int TPs = 0;
			
			for(Pair<String,String> extractedPair : extractedPairs){
				if(groundTruthPairs.contains(extractedPair)){
					TPs++;
				}
			}
			
			precision = (((double) TPs)/total);
  		}
  		
  		return precision;
	}
	
	public double getRecall(){
  		if(recall<0){
			int total = groundTruthPairs.size();
			int TPs = 0;
			
			for(Pair<String,String> groundTruthPair : groundTruthPairs){
				if(extractedPairs.contains(groundTruthPair)){
					TPs++;
				}
			}
			
			recall = (((double) TPs)/total);
  		}
  		
  		return recall;
		
	}
	
	/**
	 * Calculates the F-measure for this site
	 * @param b b in the formula for F-measure, choose b=1 for F1-measure
	 * @return The F-measure
	 */
	public double getFMeasure(double b){
  		double fMeasure = 0.0;
  		double bPowered = Math.pow(b,2);
  		fMeasure = (bPowered+1)*getPrecision()*getRecall();
  		fMeasure /= (bPowered)*getPrecision()+getRecall();
  		return fMeasure;
  	}
}
