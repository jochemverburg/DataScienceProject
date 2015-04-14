package relationExtraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import classifier.TennisPlayerClassifier;
import edu.stanford.nlp.util.Pair;
import entityResolution.TennisPlayerAnalyzer;

/**
 * Can analyze extracted pairs of relations using the ground truth of relations.
 * Input (both extracted relations and ground truth) has to be given as CoNNL without duplicates:
 * The first column contains the left part of the relation and the second column the right part of the relation.
 * Every line represents a different entity.
 * 
 * As input the output of DefeatRE.printDefeatRelations() can be used
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
		this(getRelationsFromFile(analyzedPath),getRelationsFromFile(groundTruthPath));
	}
	
	/**
	 * @param analyzedPath The path containing the extracted relations in described format.
	 * @param groundTruthPath The path containing the relations of the ground truth in described format.
	 * @throws IOException 
	 */
	public REAnalyzer(List<Pair<String, String>> analyzed, String groundTruthPath) throws IOException{
		this(analyzed, getRelationsFromFile(groundTruthPath));
	}
	
	/**
	 * @param analyzedPath The path containing the extracted relations in described format.
	 * @param groundTruthPath The path containing the relations of the ground truth in described format.
	 * @throws IOException 
	 */
	public REAnalyzer(String analyzedPath, List<Pair<String, String>> groundTruth) throws IOException{
		this(getRelationsFromFile(analyzedPath),groundTruth);
	}
	
	/**
	 * @param analyzedPath The path containing the extracted relations in described format.
	 * @param groundTruthPath The path containing the relations of the ground truth in described format.
	 * @throws IOException 
	 */
	public REAnalyzer(List<Pair<String, String>> analyzed, List<Pair<String, String>> groundTruth){
		extractedPairs = analyzed;
		groundTruthPairs = groundTruth;
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
	
	/**
	 * 
	 * @return The amount of relations that was extracted
	 */
	public int getAmountOfRelations(){
		return extractedPairs.size();
	}
	
	/**
	 * Turns comma-separated files with defeat-relations of names into tab-separated files with URIs.	
	 * @param inputPath
	 * @param outputPath
	 * @throws IOException
	 */
	public static void printDefeatURIs(String inputPath, String outputPath) throws IOException{
    	BufferedReader reader = new BufferedReader(
    			new FileReader(
    					new File(inputPath)));
    	List<Pair<String,String>> defeatPairs = new ArrayList<Pair<String,String>>();
    	Set<String> nameSet = new HashSet<String>();
    	String line;
    	while((line = reader.readLine()) !=null ){
    		String[] splitted = line.split(",");
    		String winner = splitted[0];
    		String loser = splitted[1];
    		nameSet.add(winner);
    		nameSet.add(loser);
    		defeatPairs.add(new Pair<String,String>(winner, loser));
    	}

    	if (reader != null) {
 	       reader.close();
 	    }
    	
    	List<String> nameList = new ArrayList<String>();
    	nameList.addAll(nameSet);
    	Map<String, String> uriMapping = TennisPlayerAnalyzer.makeNameURIMapping(nameList);
    	
    	Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(outputPath))));
    	
		for(Pair<String, String> defeat : defeatPairs){
			w.write(uriMapping.get(defeat.first())+COLUMN_DELIM+uriMapping.get(defeat.second())+"\n");
		}
        w.close();
    }
	
	public static final String FIRST_TEST_INPUT = "resources/firstTestUsed/input/";
	public static final String FIRST_TEST_TEMP = "resources/firstTestUsed/temp/";
	public static final String FIRST_TEST_OUTPUT = "resources/firstTestUsed/";
	
	public static REAnalyzer analyzerForWimbledon(int minOccurences) throws IOException, ClassCastException, ClassNotFoundException{
		String groundTruthDefeats = FIRST_TEST_TEMP+"defeatGroundTruth.txt";
		//printDefeatURIs(FIRST_TEST_INPUT+"defeated.csv", groundTruthDefeats);
		
		String analyzerClassName = "WimbledonParticipant";
		
		String classifiedPath = FIRST_TEST_TEMP+"classifiedTweets.txt";
		/*String participantsURIPath = FIRST_TEST_TEMP+"participantsURIs.txt";
		//TennisPlayerAnalyzer.printParticipantURIsToPath(FIRST_TEST_INPUT+"participants.txt", 2,  participantsURIPath);
		TennisPlayerClassifier.classifyTennisPlayers("classifiers/english.conll.4class.distsim.crf.ser.gz", FIRST_TEST_INPUT+"Wimbledon2014Tweets.txt", 3, ";", classifiedPath, participantsURIPath, "WimbledonParticipant");
		*/
		String realDefeats = FIRST_TEST_OUTPUT+"defeats.txt";
		List<String> possibleSubjects = new ArrayList<String>();
		possibleSubjects.add(analyzerClassName);
		DefeatRE.printDefeatRelations(classifiedPath, realDefeats, possibleSubjects, minOccurences);
		return new REAnalyzer(realDefeats, groundTruthDefeats);
	}
	
	public static REAnalyzer analyzerForWimbledon() throws IOException, ClassCastException, ClassNotFoundException{
		//Used for firstTest
		return analyzerForWimbledon(1);
	}
	
	public static void analyzeREOccurences(String outputPath, int max) throws IOException{
		String analyzerClassName = "WimbledonParticipant";
		String classifiedPath = FIRST_TEST_TEMP+"classifiedTweets.txt";
		List<String> possibleSubjects = new ArrayList<String>();
		possibleSubjects.add(analyzerClassName);
		List<Pair<String,String>> groundTruth = getRelationsFromFile(FIRST_TEST_TEMP+"defeatGroundTruth.txt");
		
		Pair<List<String>, List<List<String>>> readFile = DefeatRE.readFile(classifiedPath, possibleSubjects);
		Map<Pair<String,String>,Integer> defeatMap = DefeatRE.getDefeatMap(readFile.first(), readFile.second());
		
		Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(outputPath))));
		String Comma = ",";
		w.write("Minimum occurences"+Comma+"Recall"+Comma+"Precision"+Comma+"F1-measure"+Comma+"Nr Relations"+"\n");
		
		//22526 gives: [(http://dbpedia.org/resource/Novak_Djokovic,http://dbpedia.org/resource/Roger_Federer)]
		for(int i = 0; i<=max; i++){
			REAnalyzer analyzer = new REAnalyzer(DefeatRE.getDefeatList(defeatMap, i), groundTruth);
			double recall = analyzer.getRecall();
			double precision = analyzer.getPrecision();
			double f1measure = analyzer.getFMeasure(1);
			int relations = analyzer.getAmountOfRelations();
			w.write(i+Comma+recall+Comma+precision+Comma+f1measure+Comma+relations+"\n");
			System.out.println(i+Comma+recall+Comma+precision+Comma+f1measure+Comma+relations);
		}
		//System.out.println(DefeatRE.getDefeatList(defeatMap, 22526).toString());
	}
	
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, IOException{
		analyzeREOccurences(FIRST_TEST_OUTPUT+"resultsWithoutDoubles.csv",25000);
		/*int amount = 1000;
		double[] recalls = new double[amount+1];
		double[] precisions = new double[amount+1];
		double[] f1measures = new double[amount+1];
		Pair<Integer, Double> maxRecall = new Pair<Integer, Double>(-1, -1.0);
		Pair<Integer, Double> maxPrecision = new Pair<Integer, Double>(-1, -1.0);
		Pair<Integer, Double> maxF1Measure = new Pair<Integer, Double>(-1, -1.0);
		
		Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(FIRST_TEST_OUTPUT+"results.txt"))));
		
		String Comma = ",";
		w.write("Minimum occurences"+Comma+"Recall"+Comma+"Precision"+Comma+"F1-measure"+Comma+"Nr Relations"+"\n");
		
		for(int i = 0; i<=amount; i++){
			REAnalyzer analyzer = analyzerForWimbledon(i);
			double recall = analyzer.getRecall();
			double precision = analyzer.getPrecision();
			double f1measure = analyzer.getFMeasure(1);
			int relations = analyzer.getAmountOfRelations();
			System.out.println("Recall "+i+": "+recall[i]);
			System.out.println("Precision "+i+": "+precision[i]);
			System.out.println("F1 Measure "+i+": "+f1measure[i]);
			System.out.println("Relations "+i+": "+relations);
			w.write("Recall "+i+": "+recall[i]+"\n");
			w.write("Precision "+i+": "+precision[i]+"\n");
			w.write("F1 Measure "+i+": "+f1measure[i]+"\n");
			w.write("Relations "+i+": "+relations+"\n");
			w.write(i+Comma+recall+Comma+precision+Comma+f1measure+Comma+relations+"\n");
			System.out.println(i+Comma+recall+Comma+precision+Comma+f1measure+Comma+relations);
			
			if(recall[i]>maxRecall.second()){
				maxRecall = new Pair<Integer, Double>(i, recall[i]);
			}
			if(precision[i]>maxPrecision.second()){
				maxPrecision = new Pair<Integer, Double>(i, precision[i]);
			}
			if(f1measure[i]>maxF1Measure.second()){
				maxF1Measure = new Pair<Integer, Double>(i, f1measure[i]);
			}
		}
		
		
		System.out.println("\n"+"Recall: "+maxRecall.toString());
		System.out.println("Precision: "+maxPrecision.toString());
		System.out.println("F1 Measure: "+maxF1Measure.toString());

		w.write("\n"+"Recall: "+maxRecall.toString()+"\n");
		w.write("Precision: "+maxPrecision.toString()+"\n");
		w.write("F1 Measure: "+maxF1Measure.toString()+"\n");
		
		w.close();*/
	}
}
