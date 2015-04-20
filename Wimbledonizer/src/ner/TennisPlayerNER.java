package ner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.Pair;
import entityResolution.TennisPlayerAnalyzer;

public class TennisPlayerNER extends NER {
	
	/**
	 * Can classify tennis players using an initial Stanford CRF Classifier, an input file with tweets/sentences and an input of possible subclasses
	 * @param initialClassifierPath
	 * @param tweetPart
	 * @param delim
	 * @param inputPath
	 * @param outputPath
	 * @param playersAndClasses For each pair, pair.first() contains the path where all participant names can be found and pair.second() contains the name to be given to the class (e.g. WimbledonParticipant)
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void classifyTennisPlayers(String initialClassifierPath, String inputPath, int tweetPart, String delim, String outputPath, List<Pair<String,String>> playersAndClasses) throws IOException, ClassCastException, ClassNotFoundException{
		List<EntityResolutionInterface> analyzers = new ArrayList<EntityResolutionInterface>();
		for(Pair<String,String> playerClassPair : playersAndClasses){
			EntityResolutionInterface analyzer = new TennisPlayerAnalyzer(playerClassPair.first(), playerClassPair.second());
			analyzers.add(analyzer);
		}
		subClassifyClass(initialClassifierPath, tweetPart, delim, inputPath, outputPath, analyzers, SubNER.PERSON_ANNOTATION);
	}
	
	public static void classifyTennisPlayers(String initialClassifierPath, String inputPath, int tweetPart, String delim, String outputPath, String playersPath, String className) throws IOException, ClassCastException, ClassNotFoundException{
		List<Pair<String,String>> playersAndClasses = new ArrayList<Pair<String,String>>();
		playersAndClasses.add(new Pair<String, String>(playersPath, className));
		classifyTennisPlayers(initialClassifierPath, inputPath, tweetPart, delim, outputPath, playersAndClasses);
	}
}
