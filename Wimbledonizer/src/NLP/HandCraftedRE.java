package NLP;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;


public class HandCraftedRE extends TrainedNER {

	public static final String STANDARD_RE_SET = "resources/Wimbledon2014Tweets.txt";
	public static final String RANDOM_REGEX = ".*?";
	public static final String DEFEAT_REGEX = "(beat|defeat|def\\.|wins)";
	public static final String STANDARD_OUTPUT_FILE = "resources/assignment6deliverable.csv";
	
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, IOException {
		String serializedClassifier = NEREvaluation.STANDARD_SERIALIZED_CLASSIFIERS[1];
		HandCraftedRE re = new HandCraftedRE(serializedClassifier, 2, 3, STANDARD_TRAIN_SET, STANDARD_RE_SET);
		//re.getDefeatRelation();
		re.printRelations(STANDARD_OUTPUT_FILE);
	}
	
	private List<String> fileContents;
	
	public HandCraftedRE(String classifierPath, int truthPart, int sentencePart, String trainingPath, String tweetPath)
			throws ClassCastException, ClassNotFoundException, IOException {
		super(classifierPath, truthPart, sentencePart, trainingPath);
		fileContents = TweetFileReader.readTweetsWithDatestamp(tweetPath, 3);
	}

	
	public List<Pair<String,String>> getDefeatRelation(){
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		int scount = 0;
		
		for(List<Triple<String, Integer, Integer>> sentenceClass : TweetFileReader.getClassifiedOffsets(fileContents, super.getClassifier())){
			List<String> personNames = new ArrayList<String>();
			String wholeSentence = fileContents.get(scount);
			String personRegex = null;
			for(Triple<String, Integer, Integer> word : sentenceClass){
				if(word.first().equals("PERSON")){
					String personName = wholeSentence.substring(word.second(),word.third());
					personName = personName.replaceAll("-", "\\-");
					
					if(!(personName.contains("(")||personName.contains(")")||personName.contains("[")||personName.contains("]")||personName.contains("|"))){
						personNames.add(personName);
						if(personRegex!=null){
							personRegex+="|";
						}
						else{
							personRegex="(";
						}
						personRegex+=personName;
					}
				}
			}
			if(personRegex!=null){
				personRegex+=")";
			}
			
			//Not necessary to look for defeat relations when there's less than two persons
			if(personNames.size()>=2){
				//Makes sure that it only matches with no persons in between
				String lookaheadRandom = "((?!"+personRegex+").)*";
				String regex = personRegex+lookaheadRandom+DEFEAT_REGEX+RANDOM_REGEX+personRegex;
				
				//Has to be used more often so already compile
				Pattern personPattern = Pattern.compile(personRegex);
				
				if(wholeSentence.matches(RANDOM_REGEX+regex+RANDOM_REGEX)){
					Matcher defeatMatcher = Pattern.compile(regex).matcher(wholeSentence);
					
					while(defeatMatcher.find()){
						Matcher personMatcher = personPattern.matcher(defeatMatcher.group());
						
						Pair<String, String> defeatPair = new Pair<String, String>();
						
						personMatcher.find();
						defeatPair.setFirst(personMatcher.group());
						personMatcher.find();
						defeatPair.setSecond(personMatcher.group());
						
						if(!result.contains(defeatPair)){
							result.add(defeatPair);
						}
					}
				}
			}
			
			scount++;
		}
		return result;
	}
	
	public void printRelations(String path){
		String separator = ",";
		List<Pair<String, String>> defeats = getDefeatRelation();
		try {
            //Whatever the file path is.
            File file = new File(path);
            FileOutputStream is = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(is);    
            Writer w = new BufferedWriter(osw);
            for(Pair<String, String> defeat : defeats){
                w.write("Defeat"+separator+defeat.first()+separator+defeat.second()+"\n");
            }
            w.close();
    		System.out.println("Printed relations to file: "+path);
        } catch (IOException e) {
            System.err.println("Problem writing to the file "+path);
        }
	}
}
