package NLP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;


public class TweetFileReader {

	/**
	   * Reads a file where only the part part is added to the String.
	   * @param path
	   * @param truth The part where the ground truth is.
	   * @param tweet The part where the actual tweet is.
	   * @return result[0] All tweets separated by new lines. 
	   * @return result[1] All ground truths corresponding to the tweets.
	 * @throws IOException 
	 * @throws Exception 
	   */
	  public static Pair<List<String>, List<String>> readTweetsWithTruth(String path, int truth, int tweet) throws IOException{
			//System.out.println("Reading file from path: "+path);
			File file = new File(path);
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(file));
			List<String> fileContents = new ArrayList<String>();
			List<String> groundTruth = new ArrayList<String>();
			String text = null;	
			
			while ((text = reader.readLine()) != null) {
			   	
			   	String[] splitted = text.split("\t");
			  	fileContents.add(splitted[tweet-1]);
			   	groundTruth.add(splitted[truth-1]);
			}
			   
			if (reader != null) {
		       reader.close();
		    }
			
			return new Pair<List<String>,List<String>>(fileContents, groundTruth);
		}
	  
	  /**
	   * Reads a file where only the part part is added to the String.
	   * @param path
	   * @param tweetPart The part where the actual tweet is. The amount of ";" before the actual tweet is tweetPart-1.
	   * @return result All tweets separated by new lines. 
	 * @throws IOException 
	   */
	  public static List<String> readTweetsWithDatestamp(String path, int tweetPart) throws IOException{
			//System.out.println("Reading file from path: "+path);
		  	final String splitter = ";";
		  	
			File file = new File(path);
			BufferedReader reader = null;
			reader = new BufferedReader(new FileReader(file));
			List<String> fileContents = new ArrayList<String>();
			String text = null;	
			
			while ((text = reader.readLine()) != null) {
			   	for(int i=1; i<tweetPart; i++){
			   		text = text.substring(text.indexOf(splitter)+1);
			   	}
			   	fileContents.add(text);
			}
			   
			if (reader != null) {
		       reader.close();
		    }
			
			return fileContents;
		}
	  
	  public static List<List<Triple<String,Integer,Integer>>> getClassifiedOffsets(List<String> fileContents, AbstractSequenceClassifier<CoreLabel> classifier){
		  	List<List<Triple<String,Integer,Integer>>> classifiedOffsets = new ArrayList<List<Triple<String,Integer,Integer>>>();
			for(String sentence : fileContents){
				List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(sentence);
				classifiedOffsets.add(list);
			}
			
			return classifiedOffsets;
	  	}
}
