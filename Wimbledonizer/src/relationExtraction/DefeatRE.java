package relationExtraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.util.Pair;

/**
 * This class can extract relations from classified tweets
 * Input has to be given as CoNNL:
 * The first column contains the tokens and the second column separated by a tab contains the classification.
 * Every line represents a different entity, if it has not been classified it can also represent a token.
 * After a tweet/sentence a blank line follows.
 *
 * Output is given in tab-separated files:
 * Every line represents a defeat-relation. In the first column the winner and after the tab the loser.
 * It should not give any duplicate defeat-relations.
 * 
 * As input the output of TennisPlayerClassifier.classifyTennisPlayers() can be used.
 */
public class DefeatRE {

	public static final String RANDOM_REGEX = ".*?";
	public static final String DEFEAT_REGEX = "(beat|defeat|def\\.|wins)";
	public static final String COLUMN_DELIM = "\t";
	/**
	 * @require RELATION_SIZE==2
	 */
	public static final int RELATION_SIZE = 2;
	
	public static void printDefeatRelations(String inputPath, String outputPath, List<String> possibleSubjects, int minOccurences) throws IOException{
		Pair<List<String>, List<List<String>>> readFile = readFile(inputPath, possibleSubjects);
		List<String> tweets = readFile.first();
		List<List<String>> names = readFile.second();
		
		Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(outputPath))));
		
		List<Pair<String, String>> relations = getDefeatList(tweets, names, minOccurences);
		for(Pair<String, String> pair : relations){
			w.write(pair.first() + COLUMN_DELIM + pair.second() + "\n");
		}
		w.close();
	}
	
	public static Pair<List<String>, List<List<String>>> readFile(String inputPath, List<String> possibleSubjects) throws IOException{
		BufferedReader reader = new BufferedReader(
				new FileReader( 
						new File(inputPath)));
		
		String text = null;
		
		List<String> tweets = new ArrayList<String>();
		List<List<String>> names = new ArrayList<List<String>>();
		String tempTweet = null;
		List<String> tempList = null;
		
		while ((text = reader.readLine()) != null) {
			if(!text.isEmpty()){
				//Adds the first column to the current tweet and if the second equals one possible object-classes, also to the list of names
				String[] splitted = text.split(COLUMN_DELIM);
				if(splitted.length>=RELATION_SIZE){
					String tempEntity = splitted[0];
					if(tempTweet==null){
						tempTweet=tempEntity;
					}
					else{
						tempTweet+=" "+tempEntity;
					}
					
					String tempClass = splitted[1];
					boolean equal = false;
					for(String subject : possibleSubjects){
						if(tempClass.equals(subject)){
							equal = true;
						}
					}
					
					if(equal){
						if(tempList==null){
							tempList = new ArrayList<String>();
						}
						tempList.add(tempEntity);
					}
					
				}
			}
			else{
				//Add the last tweet and list and reset
				tweets.add(tempTweet);
				names.add(tempList);
				tempTweet = null;
				tempList = null;
			}
		}
		

		if(reader!=null){
			reader.close();
		}
		
		return new Pair<List<String>,List<List<String>>>(tweets, names);
	}
	
	public static List<Pair<String,String>> getDefeatList(List<String> tweets, List<List<String>> namesInTweets, int minOccurences){
		return getDefeatList(getDefeatMap(tweets,namesInTweets),minOccurences);
	}
	
	public static List<Pair<String,String>> getDefeatList(Map<Pair<String,String>,Integer> defeatMap, int minOccurences){
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		for(Entry<Pair<String,String>,Integer> entry : defeatMap.entrySet()){
			Pair<String,String> revertedPair = new Pair<String,String>(entry.getKey().second(),entry.getKey().first());
			if(entry.getValue()>=minOccurences && 
					((!entry.getKey().first().equals(entry.getKey().second()) && !defeatMap.keySet().contains(revertedPair)) 
							|| (defeatMap.keySet().contains(revertedPair) && entry.getValue()>=defeatMap.get(revertedPair)))){	
				result.add(entry.getKey());
			}
		}
		return result;
	}
	
	/**
	 * It doesn't work yet if the name contains a hyphen
	 * @param tweets
	 * @param namesInTweets
	 * @return
	 */
	public static Map<Pair<String,String>,Integer>  getDefeatMap(List<String> tweets, List<List<String>> namesInTweets){
		
		Map<Pair<String,String>,Integer> result = new HashMap<Pair<String,String>,Integer>();
		
		//List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		
		for(int i = 0; i<tweets.size(); i++){
			String tweet = tweets.get(i);
			List<String> names = namesInTweets.get(i);
			if(names!=null){
				String nameRegex = getNameRegex(names);
				if(names.size()>=RELATION_SIZE && nameRegex!=null){
					
					//Makes sure that it only matches with no persons in between
					String lookaheadRandom = "((?!"+nameRegex+").)*";
					String regex = nameRegex+lookaheadRandom+DEFEAT_REGEX+RANDOM_REGEX+nameRegex;
					
					//Has to be used more often so already compile
					Pattern personPattern = Pattern.compile(nameRegex);
					
					if(tweet.matches(RANDOM_REGEX+regex+RANDOM_REGEX)){
						Matcher defeatMatcher = Pattern.compile(regex).matcher(tweet);
						
						while(defeatMatcher.find()){
							Matcher personMatcher = personPattern.matcher(defeatMatcher.group());
							
							Pair<String, String> defeatPair = new Pair<String, String>();
							
							personMatcher.find();
							defeatPair.setFirst(personMatcher.group());
							personMatcher.find();
							defeatPair.setSecond(personMatcher.group());
							
							/* Used for first test
							if(!result.contains(defeatPair)){
								result.add(defeatPair);
							}*/
							if(result.containsKey(defeatPair)){
								result.put(defeatPair, result.get(defeatPair)+1);
							}
							else{
								result.put(defeatPair, 1);
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Makes a nameRegex with the list of names
	 * @param names
	 * @return
	 */
	public static String getNameRegex(List<String> names){
		String nameRegex = null;
		for(String name : names){
			if(nameRegex!=null){
				nameRegex+="|";
			}
			else{
				nameRegex="(";
			}
			nameRegex+=name;
		}
		if(nameRegex!=null){
				nameRegex+=")";
		}
		return nameRegex;
	}

}
