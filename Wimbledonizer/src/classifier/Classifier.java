package classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.Sentence;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

/**
 * Extends the subclassifier by also making the methods for first classifying a file
 * @author Jochem
 *
 */
public class Classifier extends SubClassifier {
	
	/**
	 * Combines the steps of both the initial classification and the subclassification, using the Stanford CRF Classifier. Puts the the temporary classification in a file named the same as outputPath with -temp add the end.
	 * @param classifierPath
	 * @param tweetPart
	 * @param delim
	 * @param inputPath
	 * @param outputPath
	 * @param analyzers
	 * @param classAnnotation
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static void subClassifyClass(String classifierPath, int tweetPart, String delim, String inputPath, String outputPath, List<EntityResolutionInterface> analyzers, String classAnnotation) throws ClassCastException, ClassNotFoundException, IOException{
		//Defines a new path for the initialclassification by adding -temp add the end of the filename
		String initialClassificationPath = outputPath.substring(0, outputPath.lastIndexOf(".")) + "-temp" + outputPath.substring(outputPath.lastIndexOf("."),outputPath.length());
		printInitialClassification(classifierPath, tweetPart, delim, inputPath, initialClassificationPath);
		subClassifyClass(initialClassificationPath, outputPath, analyzers, classAnnotation);
	}
	
	/**
	 * Prints the classification of the NLP CRF Classifier which is entered to a file in the right format for the subclassifier.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void printInitialClassification(String classifierPath, int tweetPart, String delim, String inputPath, String outputPath) throws ClassCastException, ClassNotFoundException, IOException{
		printInitialClassification(CRFClassifier.getClassifier(classifierPath), tweetPart, delim, inputPath, outputPath);
	}
	
	/**
	 * Prints the classification of the NLP CRF Classifier which is entered to a file in the right format for the subclassifier.
	 * @throws IOException 
	 */
	public static void printInitialClassification(AbstractSequenceClassifier<CoreLabel> classifier, int tweetPart, String delim, String inputPath, String outputPath) throws IOException{
		List<String> sentences = TweetFileReader.readTweets(inputPath, delim, tweetPart);
				
		Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(outputPath))));
		
		Tagger tagger = new Tagger();
		String modelFilename = "/cmu/arktweetnlp/model.20120919";
		tagger.loadModel(modelFilename);
		
		for(int i = 0; i<sentences.size(); i++){
			String line = sentences.get(i);
			List<Pair<String, String>> tags = classifySentence(line, classifier);
			Sentence sentence = new Sentence();
			sentence.tokens = Twokenize.tokenizeRawTweetText(line);
			List<Pair<String,String>> tokensAndTags = addTagsToTokens(tags, sentence.tokens);
			for(Pair<String, String> pair : tokensAndTags){
				w.write(pair.first() + COLUMN_DELIM + pair.second()+"\n");
			}
			w.write(SENTENCE_SEPARATOR);
		}
		w.close();
	}
	
	/**
	 * Edits the list of tokens
	 * @param tags
	 * @param tokens
	 * @return A pair of an entity or token with the tag, pair.first() contains the entity/token and pair.second() contains the tag
	 */
	private static List<Pair<String,String>> addTagsToTokens(List<Pair<String,String>> tags, List<String> tokens){
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		
		for(Pair<String, String> pair : tags){
			for(int i=0; i<tokens.size(); i++){
				String token = tokens.get(i);
				if(containsToken(pair.first(),token)){
					tokens.remove(i);
					//Add all others first and remove them from to do
					for(int j=0; j<i && j<tokens.size(); j++){
						result.add(new Pair<String,String>(tokens.get(0),NO_TAG));
						tokens.remove(0);
					}
					
					//Back to 0 since tokens has been made smaller
					i=-1;
					if(pair.first().endsWith(token)){
						break;
					}
				}
			}
			result.add(pair);
		}
		
		//Add remaining tokens
		for(int i=0; i<tokens.size(); i++){
			result.add(new Pair<String,String>(tokens.get(i), NO_TAG));
		}
		
		return result;
	}
	
	/**
	 * To check if an entity contains a token
	 * @param entity
	 * @param token
	 * @return
	 */
	public static boolean containsToken(String entity, String token){
		return (Twokenize.tokenize(entity)).contains(token);
	}
	
	/**
	 * @param sentence
	 * @param classifier
	 * @return A pair of the tags, with pair.first() the entity and pair.second() the tag
	 */
	private static List<Pair<String,String>> classifySentence(String sentence, AbstractSequenceClassifier<CoreLabel> classifier){
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(sentence);
		for(Triple<String, Integer, Integer> tag : list){
			result.add(new Pair<String,String>(sentence.substring(tag.second(),tag.third()), tag.first()));
		}
		return result;
	}
	
	/**
	 * Tokenizes a file which is delimited with delim and where the tweet is in the last part (denoted by tweetPart)
	 * @param tweetPart
	 * @param delim
	 * @param inputFilename
	 * @return A pair, with pair.first() all lines and pair.second() a list of tokens for every line.
	 * @throws IOException
	 */
	private static Pair<List<String>,List<List<String>>> tokenizeFile(int tweetPart, String delim, String inputFilename) throws IOException{
		List<String> lines = TweetFileReader.readTweets(inputFilename, delim, tweetPart);
		return new Pair<List<String>,List<List<String>>>(lines,tokenizeFile(lines));
	}
	
	private static List<List<String>> tokenizeFile(List<String> lines) throws IOException{
		List<List<String>> result = new ArrayList<List<String>>();
		
		Tagger tagger = new Tagger();
		String modelFilename = "/cmu/arktweetnlp/model.20120919";
		tagger.loadModel(modelFilename);
		for(String line : lines){			
			Sentence sentence = new Sentence();
			sentence.tokens = Twokenize.tokenizeRawTweetText(line);
			result.add(sentence.tokens);
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException{
		System.out.println(tokenizeFile(3,";","resources/Wimbledon2014TweetsSmallTestset.txt"));
	}
}
