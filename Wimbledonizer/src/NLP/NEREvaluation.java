package NLP;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NEREvaluation {
	public static final String[] STANDARD_SERIALIZED_CLASSIFIERS = new String[]{"classifiers/english.all.3class.distsim.crf.ser.gz","classifiers/english.conll.4class.distsim.crf.ser.gz","classifiers/english.all.7class.distsim.crf.ser.gz"};
	public static final Map<String,String> GROUND_TRUTH_MAP;
	public static final String EXTRA_CLASS = "MISSED";
	
	static {
	    Map<String,String> tmpMap = new HashMap<String,String>();
	    tmpMap.put("PER", "PERSON");
	    tmpMap.put("ORG", "ORGANIZATION");
	    tmpMap.put("LOC", "LOCATION");
	    tmpMap.put("MISC", "MISC");	    
	    GROUND_TRUTH_MAP = Collections.unmodifiableMap(tmpMap);
	}
	
	
  public static void main(String[] args) throws Exception {

    String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";

    if (args.length > 0) {
      serializedClassifier = args[0];
    }
    serializedClassifier = STANDARD_SERIALIZED_CLASSIFIERS[0];

    AbstractSequenceClassifier<CoreLabel> classifier = null;
    		classifier = CRFClassifier.getClassifier(serializedClassifier);
    
    //System.out.println(classifier.classIndex);

    /* For either a file to annotate or for the hardcoded text example, this
       demo file shows several ways to process the input, for teaching purposes.
    */

    if (args.length > 1) {

      /* For the file, it shows (1) how to run NER on a String, (2) how
         to get the entities in the String with character offsets, and
         (3) how to run NER on a whole file (without loading it into a String).
      */
    	//System.out.println(new NEREvaluation(serializedClassifier, args[1], 2, 3).generateConfusionMatrix());
    	NEREvaluation test = new NEREvaluation(serializedClassifier, args[1], 2, 3);
    	/*for(List<List<CoreLabel>> sentenceList : test.getClassifiedList()){
	    	for(List<CoreLabel> part : sentenceList){
				for(CoreLabel token : part){
					System.out.print(token.originalText()+"/"+token.getString(CoreAnnotations.AnswerAnnotation.class)+" ");
				}
	    	}
	    	System.out.println();
    	}*/
    	System.out.println(test.getRecall());
    	System.out.println(test.getPrecision());
    	System.out.println(test.getConfusionMatrix());
    	System.out.println(test.getFMeasure(1));
    	//test.printHighProbabilities(0.1);
     // String fileContents = readFile(args[1],2,3)[0];
    		  //IOUtils.slurpFile(args[1]);
      /*System.out.print(fileContents);
      System.out.println();
      List<List<CoreLabel>> out = classifier.classify(fileContents);
      */
      //This part writes the annotation behind every word (O is when not classified)
      /*for (List<CoreLabel> sentence : out) {
        for (CoreLabel word : sentence) {
          System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
        }
        System.out.println();
      }*/
	  
      
      //This part writes the annotation behind every word as well but using classifyFile
      /*System.out.println("---");
      out = classifier.classifyFile(args[1]);
      for (List<CoreLabel> sentence : out) {
        for (CoreLabel word : sentence) {
          System.out.print(word.word() + '/' + word.get(CoreAnnotations.AnswerAnnotation.class) + ' ');
        }
        System.out.println();
      }*/
      
      //This part defines which part of the sentence has a certain class
      /*System.out.println("---");
      List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);
      for (Triple<String, Integer, Integer> item : list) {
        System.out.println(item.first() + ": " + fileContents.substring(item.second(), item.third()));
      }
      */
      
      //This part gives for each of the sentences the k-best classifications of the whole sentence.
      //System.out.println("---");
      //System.out.println("Ten best entity labelings");
    /* DocumentReaderAndWriter<CoreLabel> readerAndWriter = classifier.makePlainTextReaderAndWriter();
      //classifier.classifyAndWriteAnswersKBest(args[1], 10, readerAndWriter);
      
      //This part gives for each of the tokens/words the probability for each class
      System.out.println("---");
      System.out.println("Per-token marginalized probabilities");
      classifier.printProbs(args[1], readerAndWriter);
	*/
      // -- This code prints out the first order (token pair) clique probabilities.
      // -- But that output is a bit overwhelming, so we leave it commented out by default.
      // System.out.println("---");
      // System.out.println("First Order Clique Probabilities");
      // ((CRFClassifier) classifier).printFirstOrderProbs(args[1], readerAndWriter);

    } else {

      /* For the hard-coded String, it shows how to run it on a single
         sentence, and how to do this and produce several formats, including
         slash tags and an inline XML output format. It also shows the full
         contents of the {@code CoreLabel}s that are constructed by the
         classifier. And it shows getting out the probabilities of different
         assignments and an n-best list of classifications with probabilities.
      */

      String[] example = {"Good afternoon Rajat Raina, how are you today?\n Test new sentence",
                          "I go to school at Stanford University, which is located in California." };
      for (String str : example) {
        System.out.println(classifier.classifyToString(str));
      }
      System.out.println("---");

      for (String str : example) {
        // This one puts in spaces and newlines between tokens, so just print not println.
        System.out.print(classifier.classifyToString(str, "slashTags", false));
      }
      System.out.println("---");

      for (String str : example) {
        // This one is best for dealing with the output as a TSV (tab-separated column) file.
        // The first column gives entities, the second their classes, and the third the remaining text in a document
        System.out.print(classifier.classifyToString(str, "tabbedEntities", false));
      }
      System.out.println("---");

      for (String str : example) {
        System.out.println(classifier.classifyWithInlineXML(str));
      }
      System.out.println("---");

      for (String str : example) {
        System.out.println(classifier.classifyToString(str, "xml", true));
      }
      System.out.println("---");

      for (String str : example) {
        System.out.print(classifier.classifyToString(str, "tsv", false));
      }
      System.out.println("---");

      // This gets out entities with character offsets
      int j = 0;
      for (String str : example) {
        j++;
        List<Triple<String,Integer,Integer>> triples = classifier.classifyToCharacterOffsets(str);
        for (Triple<String,Integer,Integer> trip : triples) {
          System.out.printf("%s over character offsets [%d, %d) in sentence %d.%n",
                  trip.first(), trip.second(), trip.third, j);
        }
      }
      System.out.println("---");

      // This prints out all the details of what is stored for each token
      int i=0;
      for (String str : example) {
        for (List<CoreLabel> lcl : classifier.classify(str)) {
          for (CoreLabel cl : lcl) {
            System.out.print(i++ + ": ");
            System.out.println(cl.toShorterString());
          }
        }
      }

      System.out.println("---");

    }
  }
  
   	private AbstractSequenceClassifier<CoreLabel> classifier;
  	private List<String> fileContents;
  	private List<String> groundTruth;
  	
  	private List<List<Pair<String, String>>> groundTruths;
  	private List<List<Triple<String,Integer,Integer>>> classifiedOffsets;
  	private int[][] confusionMatrix;
  	private String confusionMatrixString;
  	private double recall;
  	private double precision;
  	
  	/**
  	 * 
  	 * @param classifierPath The path of the serialized classifier
  	 * @param evaluationFilePath The path of the file to be used for evaluation
  	 * @param truthPart The part where the ground truth is.
  	 * @param sentencePart The part where the actual sentence is.
  	 * @throws IOException 
  	 * @throws ClassNotFoundException 
  	 * @throws ClassCastException 
  	 */
  	public NEREvaluation(String classifierPath, String evaluationFilePath, int truthPart, int sentencePart) throws ClassCastException, ClassNotFoundException, IOException{
  		this(CRFClassifier.getClassifier(classifierPath), evaluationFilePath, truthPart, sentencePart);
  	}
  	
  	/**
  	 * 
  	 * @param classifierPath The path of the serialized classifier
  	 * @param evaluationFilePath The path of the file to be used for evaluation
  	 * @param truthPart The part where the ground truth is.
  	 * @param sentencePart The part where the actual sentence is.
  	 * @throws IOException 
  	 * @throws ClassNotFoundException 
  	 * @throws ClassCastException 
  	 */
  	public NEREvaluation(AbstractSequenceClassifier<CoreLabel> classifier, String evaluationFilePath, int truthPart, int sentencePart) throws IOException{
  		this.classifier = classifier;
  		Pair<List<String>,List<String>> content = TweetFileReader.readTweetsWithTruth(evaluationFilePath, truthPart, sentencePart);
  		setFileContents(content.first());
  		groundTruth = content.second();
  		groundTruths = null;
  		classifiedOffsets = null;
  		confusionMatrix = null;
  		confusionMatrixString = null;
  		recall = -1.0;
  		precision = -1.0;
  	}
  	
  	/**
  	 * Makes a list with on every place a list of all the pairs of ground truths for the sentence, only if the ground truth is in the classes
  	 * @return
  	 */
  	public List<List<Pair<String, String>>> getGroundTruth(){
  		if(groundTruths==null){
	  		groundTruths = new ArrayList<List<Pair<String,String>>>();
	  		for(String sentence : groundTruth){
	  			List<Pair<String,String>> sentenceList = new ArrayList<Pair<String,String>>();
	  			
	  			for(String pair : sentence.split(";")){
	  				String splitted[] = pair.split("/");
		  			String first = GROUND_TRUTH_MAP.get(splitted[0]);
		  			if(first==null){
		  				first = splitted[0];
		  			}
		  			String second = null;
		  			try{
		  				second = splitted[1];
		  				if(first!=null && getClassifier().classIndex.contains(first))
		  					sentenceList.add(new Pair<String,String>(first, second));
		  			}
		  			catch(ArrayIndexOutOfBoundsException e){
		  			}
	  			}
	  			
	  			groundTruths.add(sentenceList);
	  		}
  		}
  		return groundTruths;
  	}
  	
  	public List<List<Triple<String,Integer,Integer>>> getClassifiedOffsets(){
  		if(classifiedOffsets==null){
  			/* For this file it goes wrong when only done once, but if the last sentence is removed it always goes wrong:
  			 * 1163	PER/Sarah Palin;PER/Michelle Obama;	@FakeUsername : Sarah Palin Slams Michelle Obama in Racially Charged Passage From New Book http://FakeURL via @FakeUsername ''
  			 * 1164	PER/Michelle Bachman;	@FakeUsername : RT @FakeUsername : Why do republicans like Michelle Bachman lie so much #FakeHashtag  #FakeHashtag  #FakeHashtag / / She learned frm the best . ''
  			 * 1165	PER/Cheryl Cole;PER/Cowell;PER/Wagner;	@FakeUsername : Cheryl Cole and Cowell bullying Wagner, very unedifying #FakeHashtag ''
  			 * 1166	PER/Michele Bachmann;	@FakeUsername : Just watched Rep Michele Bachmann on Newsnight . God help the world if she ever becomes President . Both thick and dangerous . ''
  			 * 1167	PER/Gary Mckinnon;	@FakeUsername : RT @FakeUsername : end the war of Terror against Gary Mckinnon - #FakeHashtag  @FakeUsername ''
  			 * 1168	LOC/US;PER/Mr Clegg;	@FakeUsername : @FakeUsername This does not help my wife & I facing Illegal US extradition Mr Clegg, we have (cont) http://FakeURL
  			 * 1169	PER/Brian_Howes;PER/Brian Howes;LOC/U.S;	@FakeUsername : Brian_Howes Brian Howes  #FakeHashtag and clear human rights violations in the U.S http://FakeURL @FakeUsername ? ''
  			 * 1170		@FakeUsername : Regular Exercise May Ward Off Dozens of Health Problems http://FakeURL
  			 * 1171	ORG/GOP;PER/Bob Inglis;	@FakeUsername : GOP Rep. Bob Inglis slams his party on climate change denial . http://FakeURL*/
  			classifiedOffsets = TweetFileReader.getClassifiedOffsets(fileContents, getClassifier());
  		}
  		return classifiedOffsets;
  	}
  	
  	
  	
  	/**
  	 * First part is true statements, second part is assigned
  	 * result[a][classifier.classIndex.size()] stands for an entity which was not found 
  	 * result[0][1] correspond to the amount of times to classifier.classIndex.get(1) is assigned to something of classifier.classIndex.get(0)
  	 * result[0] can be ignored as well as result[x][0]
  	 * @return
  	 */
  	public int[][] generateConfusionMatrix(){
  		//classifier.get
  		if(confusionMatrix==null){
	  		
  			//Initalize with an extra at the end because
	  		confusionMatrix = new int[getClassifier().classIndex.size()][getClassifier().classIndex.size()+1];
	  		
	  		//scount counts in which sentence has to be looked
	  		int scount = 0;
	  		for(List<Pair<String,String>> sentence : getGroundTruth()){
	  			//For each pair
	  			for(Pair<String,String> pair : sentence){

	  	  			//System.out.println(pair.second()+"/"+pair.first());
	  				//Find the corresponding part
	  				
	  				boolean found = false;
	  				int classIndex = getClassifier().classIndex.indexOf(pair.first());
	  				
	  				List<Triple<String,Integer,Integer>> sentenceList =  getClassifiedOffsets().get(scount);
	  				for(Triple<String, Integer, Integer> entity : sentenceList){
	  	  					String text = getFileContents().get(scount).substring(entity.second(),entity.third());
	  	  					if(pair.second().equals(text) && !found){
	  	  						found = true;
	  	  						String discoveredClass = entity.first();
	  	  						confusionMatrix[classIndex][getClassifier().classIndex.indexOf(discoveredClass)]++;
	  	  	  				//System.out.println("Equals: "+text+"/"+discoveredClass);
	  	  					}
		  			}
		  			if(!found){
						//System.out.println("Not found "+pair.second()+"/"+pair.first());
		  				confusionMatrix[classIndex][getClassifier().classIndex.size()]++;
		  			}
	  			}
	  			scount++;
	  		}
	  			  		
	  		confusionMatrix = shiftArray(confusionMatrix);
  		}
  		
  		return confusionMatrix;
  	}
  	
  	public static int[][] shiftArray(int[][] array){
  		int[][] result = new int[array.length-1][array[0].length-1];
  		for(int i=0; i<result.length; i++){
  			for(int j=0; j<result[i].length; j++){
  				result[i][j] = array[i+1][j+1];
  			}
  		}
  		
  		return result;
  	}
  	
  	public String getConfusionMatrix(){
  		if(confusionMatrixString==null){
  			String result = "";
	  		String[] sentence = new String[getClassifier().classIndex.size()];
	  		sentence[0] = "";
	  		for(int i=1; i<getClassifier().classIndex.size(); i++){
	  			sentence[0] += "\t"+getClassifier().classIndex.get(i);
	  			sentence[i] = getClassifier().classIndex.get(i);
	  		}
	  		sentence[0] += "\t"+EXTRA_CLASS;
	  		
	  		
	  		for(int i=0; i<generateConfusionMatrix().length; i++){
	  			for(int j=0; j<generateConfusionMatrix()[i].length; j++){
	  				sentence[i+1] += "\t"+generateConfusionMatrix()[i][j];
	  			}
	  		}
	  		
	  		for(int i=0; i<getClassifier().classIndex.size(); i++){
	  			result += sentence[i]+"\n";
	  		}
	  		confusionMatrixString = result;
  		}
  		return confusionMatrixString;
  	}
  	
  	public double getRecall(){
  		if(recall<0){
	  		int TP = 0;
	  		int FN = 0;
	  		for(int i=0; i<generateConfusionMatrix().length; i++){
	  			TP += generateConfusionMatrix()[i][i];
	  			for(int j=0; j<generateConfusionMatrix()[i].length; j++){
	  				if(i!=j){
	  				FN += generateConfusionMatrix()[i][j];
	  				}
	  			}
	  		}
	  		recall = (((double) TP)/((TP+FN)));
  		}
  		//System.out.println("TP: "+TP);
  		return recall;
  	}
  	
  	public double getPrecision(){
  		if(precision<0){
	  		int[] TP = new int[generateConfusionMatrix().length];
	  		int[] FP = new int[generateConfusionMatrix().length];
	  		for(int i=0; i<getClassifiedOffsets().size(); i++){
	  			List<Triple<String, Integer, Integer>> sentenceList =  getClassifiedOffsets().get(i);
	  			
				for(Triple<String, Integer, Integer> entity : sentenceList){
	  				String discoveredClass = entity.first();
					String entityText = getFileContents().get(i).substring(entity.second(),entity.third());
	  				//System.out.println(entityText+"/"+discoveredClass);
					
					int classNr = getClassifier().classIndex.indexOf(discoveredClass);
					if(classNr>0){
						boolean same = false;
							//Try to look for a pair with the same class, then it's a false positive
						for(Pair<String,String> pair : getGroundTruth().get(i)){
							if(!same && pair.second().equals(entityText) && classNr==getClassifier().classIndex.indexOf(pair.first())){
								TP[classNr-1]++;
								same = true;
								//System.out.println("Equals: "+pair.second()+"/"+pair.first);
							}
						}
							//If the pair was not found, then it was a false positive
						if(!same){
							FP[classNr-1]++;
						}
					}
	  				
	  			}
	  		}
	  		
	  		int TPs = 0;
	  		int FPs = 0;
	  		
	  		for(int i = 0; i<generateConfusionMatrix().length; i++){
	  			TPs += TP[i];
	  			FPs += FP[i];
	  		}
	  		
	  		//System.out.println("TP: "+TPs);
	  		precision = (((double) TPs)/((TPs+FPs)));
  		}
  		return precision;			
  	}
  	
  	//For F1 use b=1
  	public double getFMeasure(double b){
  		double fMeasure = 0.0;
  		double bPowered = Math.pow(b,2);
  		fMeasure = (bPowered+1)*getPrecision()*getRecall();
  		fMeasure /= (bPowered)*getPrecision()+getRecall();
  		return fMeasure;
  	}
  	
  	protected AbstractSequenceClassifier<CoreLabel> getClassifier(){
  		return classifier;
  	}

	public List<String> getFileContents() {
		return fileContents;
	}

	public void setFileContents(List<String> fileContents) {
		this.fileContents = fileContents;
	}

}
