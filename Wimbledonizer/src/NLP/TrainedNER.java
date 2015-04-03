package NLP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import cmu.arktweetnlp.util.BasicFileIO;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.FeatureFactory;
import edu.stanford.nlp.util.Pair;


public class TrainedNER {

	public static final String STANDARD_TRAIN_SET = "resources/TweetsTrainset.txt";
	public static final String SMALL_TRAIN_SET = "resources/SmallTestset.txt";
	
	public static final String CONNL_EXT = "-CoNNL.txt";
	public static final String TRAIN_EXT = "-train.txt";
	public static final String TEST_EXT = "-test.txt";
	public static final String TRAINED_EXT = ".class.self-trained.ser.gz";
	public static final String CLASS_LOC = "classifiers/";
	
	public static final String COLUMN_SPLITTER = "\t";
	public static final String LINE_SPLITTER = "\n";
	public static final String NO_TAG = "O";
	
	public static void main(String[] args) throws ClassCastException, ClassNotFoundException, IOException {
		String serializedClassifier = NEREvaluation.STANDARD_SERIALIZED_CLASSIFIERS[1];
		
		//System.out.println(kFoldEvaluation(serializedClassifier, 2, 3, STANDARD_TRAIN_SET, 10));
		
		new TrainedNER(serializedClassifier, 2, 3, STANDARD_TRAIN_SET);
		/*int k = 10;
		int nrlines = 111;
		double foldSize = (double) nrlines/k;
		System.out.println(foldSize);
		for(int i = 0; i<nrlines; i++){
			System.out.println("Regel "+i+" is in fold "+(int) (i/foldSize)+"("+(i/foldSize)+")");
		}*/
	}
	
	public static String kFoldEvaluation(String classifierPath, int truthPart, int sentencePart, String path, int k) throws IOException, ClassCastException, ClassNotFoundException{
		String results = "The results for k-fold cross validation with k: "+k+LINE_SPLITTER;
		results += "Fold"+COLUMN_SPLITTER+"Precision"+COLUMN_SPLITTER +"Recall"+COLUMN_SPLITTER +"Fmeasure"+LINE_SPLITTER;
		
		TrainedNER trained;
		NEREvaluation evaluator;
		double[] precisions = new double[k];
		double[] recalls = new double[k];
		double[] fmeasures = new double[k];
		
		double sumprecision = 0.0;
		double sumrecall = 0.0;
		double sumfmeasure = 0.0;
		
		for(int fold=0; fold<k; fold++){
			writeFold(k, fold, path);
			
			trained = new TrainedNER(classifierPath, truthPart, sentencePart, newPathForFold(path, TRAIN_EXT, fold));
			evaluator = new NEREvaluation(trained.getClassifier(), newPathForFold(path, TEST_EXT, fold), truthPart, sentencePart);
			
			precisions[fold] = evaluator.getPrecision();
			recalls[fold] = evaluator.getRecall();
			fmeasures[fold] = evaluator.getFMeasure(1);
			
			sumprecision += precisions[fold];
			sumrecall += recalls[fold];
			sumfmeasure += fmeasures[fold];
			
			results += fold+COLUMN_SPLITTER+precisions[fold]+COLUMN_SPLITTER+recalls[fold]+COLUMN_SPLITTER+fmeasures[fold]+LINE_SPLITTER;
			System.out.println(results);
		}
		
		results += "Average"+COLUMN_SPLITTER+(sumprecision/k)+COLUMN_SPLITTER+(sumrecall/k)+COLUMN_SPLITTER+(sumfmeasure/k)+LINE_SPLITTER;
		
		return results;
	}
	
	
	
	public static void writeFold(int k, int fold, String path) throws IOException{
		//System.out.println("Reading file from path: "+path);
		File inputFile = new File(path);
        File trainingFile = new File(newPathForFold(path, TRAIN_EXT, fold));
        File testFile = new File(newPathForFold(path, TEST_EXT, fold));
        
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(inputFile));

        FileOutputStream trainingis = new FileOutputStream(trainingFile);
        OutputStreamWriter trainingosw = new OutputStreamWriter(trainingis);    
        Writer trainingW = new BufferedWriter(trainingosw);
        
        FileOutputStream testis = new FileOutputStream(testFile);
        OutputStreamWriter testosw = new OutputStreamWriter(testis);    
        Writer testW = new BufferedWriter(testosw);
		
		List<String> fileContents = new ArrayList<String>();
		String text = null;	
		
		while ((text = reader.readLine()) != null) {
		   	fileContents.add(text);
		}
		   
		if (reader != null) {
	       reader.close();
	    }
		
		int nrlines = fileContents.size();
		double foldSize = (double) nrlines/k;
		for(int i = 0; i<nrlines; i++){
			String line = fileContents.get(i);
			if(((int) (i/foldSize))==fold){
				testW.write(line+LINE_SPLITTER);
			}
			else{
				trainingW.write(line+LINE_SPLITTER);
			}
		}
		
		trainingW.close();
		testW.close();
	}
	
	/**
	 * 
	 * @param path
	 * @param extension Use one of the prefab-extensions
	 * @param k
	 * @return
	 */
	public static String newPathForFold(String path, String extension, int fold){
		return path.substring(0,path.lastIndexOf(".")) + "-" + fold + extension;
	}
	
	private AbstractSequenceClassifier<CoreLabel> classifier;

	String modelFilename = "/cmu/arktweetnlp/model.20120919";

	private Pair<List<String>,List<String>> trainingContent;
	private List<String> trainingContents;
	private NEREvaluation trainingEvaluation;
	private List<List<String>> trainingTokens;
	private List<String[]> trainingTags;
	private List<Double[]> trainingConfs;
	
	private String connlPath;
	
	private String truthCoNNL;	
	private Tagger tagger;
	
	//classifierPath shouldn't be necessary as soon as this is implemented with an empty classifier
	/**
	 * 
	 * @param classifierPath The path of the classifier to be trained
	 * @param truthPart
	 * @param sentencePart
	 * @param trainingPath
	 * @throws ClassCastException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public TrainedNER(String classifierPath, int truthPart, int sentencePart, String trainingPath) throws ClassCastException,
			ClassNotFoundException, IOException {

		
		trainingContent = TweetFileReader.readTweetsWithTruth(trainingPath, truthPart, sentencePart);
		trainingContents = trainingContent.first();
		
		trainingEvaluation = new NEREvaluation(classifierPath, trainingPath, truthPart, sentencePart);
		
		trainingTokens = new ArrayList<List<String>>();
		trainingTags = new ArrayList<String[]>();
		trainingConfs = new ArrayList<Double[]>();
		
		runTagger(sentencePart, trainingPath);
		
		connlPath = trainingPath.substring(0,trainingPath.lastIndexOf(".")) + CONNL_EXT;
		printCoNNLTruthToFile(connlPath);

		classifier = CRFClassifier.getClassifier(classifierPath);
		classifier.train(connlPath);
		writeClassifier(CLASS_LOC+trainingPath.substring(trainingPath.lastIndexOf("/")+1,trainingPath.lastIndexOf("."))+ TRAINED_EXT);
	}
	
	public void writeClassifier(String path){
		classifier.serializeClassifier(path);
		System.out.println("Classifier saved to: "+path);
	}
	
	public void printCoNNLTruthToFile(String path){
		String truth = makeCoNNL();
		try {
            //Whatever the file path is.
            File file = new File(path);
            FileOutputStream is = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(is);    
            Writer w = new BufferedWriter(osw);
            w.write(truth);
            w.close();
    		System.out.println("Printed CoNNL-truth to file: "+path);
        } catch (IOException e) {
            System.err.println("Problem writing to the file "+path);
        }
	}
	
	/*private void tokenizeTrainingset(){
		trainingTokenized = new ArrayList<List<String>>();
		for(String line : trainingContents){
			trainingTokenized.add(Twokenize.tokenize(line));
		}
	}*/
	
	private void runTagger(int inputField, String inputFilename) throws IOException{
		tagger = new Tagger();
		tagger.loadModel(modelFilename);
		
		LineNumberReader reader = new LineNumberReader(BasicFileIO.openFileToReadUTF8(inputFilename));

		String line;
		long currenttime = System.currentTimeMillis();
		int numtoks = 0;
		while ( (line = reader.readLine()) != null) {
			String[] parts = line.split("\t");
			String tweetData = parts[inputField-1];
						
			String text;
			text = tweetData;
			
			Sentence sentence = new Sentence();
			
			sentence.tokens = Twokenize.tokenizeRawTweetText(text);
			ModelSentence modelSentence = null;

			if (sentence.T() > 0) {
				modelSentence = new ModelSentence(sentence.T());
				tagger.featureExtractor.computeFeatures(sentence, modelSentence);
				tagger.model.greedyDecode(modelSentence, true);
				//tagger.model.viterbiDecode(mSent);
			}
			
			outputPrependedTagging(sentence, modelSentence, true);				
			
			numtoks += sentence.T();
		}
		long finishtime = System.currentTimeMillis();
		System.err.printf("Tokenized%s %d tweets (%d tokens) in %.1f seconds: %.1f tweets/sec, %.1f tokens/sec\n",
				" and tagged", 
				reader.getLineNumber(), numtoks, (finishtime-currenttime)/1000.0,
				reader.getLineNumber() / ((finishtime-currenttime)/1000.0),
				numtoks / ((finishtime-currenttime)/1000.0)
		);
		reader.close();
	}
	
	/**
	 * assume mSent's labels hold the tagging.
	 * 
	 * @param lSent
	 * @param mSent
	 * @param inputLine -- assume does NOT have trailing newline.  (default from java's readLine)
	 */
	private void outputPrependedTagging(Sentence lSent, ModelSentence mSent, 
			boolean suppressTags) {
		// mSent might be null!
		
		int T = lSent.T();
		List<String> tokens = new ArrayList<String>();
		String[] tags = new String[T];
		Double[] confs = new Double[T];
		for (int t=0; t < T; t++) {
			tokens.add(lSent.tokens.get(t));
			if (!suppressTags) {
				tags[t] = tagger.model.labelVocab.name(mSent.labels[t]);	
			}
			confs[t] = mSent.confidences[t];
		}
		trainingTokens.add(tokens);
		trainingTags.add(tags);
		trainingConfs.add(confs);
	}
	
	public static Predicate<String> contains(String string) {
	    return s -> truthContains(s, string);
	}
	
	//To check if the truth contains a token as a token
	public static boolean truthContains(String truth, String s){
		return (Twokenize.tokenize(truth)).contains(s);
	}
	
	
	//Pair.first() == truth and Pair.second() == token
	//This looks at the sequence of both truths and sentence, to give some extra security
	private List<Pair<String,String>> addTagsToTokens(int linenr){
		List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
		
		List<String> tokens = trainingTokens.get(linenr);
		List<Pair<String, String>> truths = trainingEvaluation.getGroundTruth().get(linenr);
		
		for(Pair<String, String> pair : truths){
			for(int i=0; i<tokens.size(); i++){
				String token = tokens.get(i);
				if(truthContains(pair.second(),token)){Pair<String, String> newPair = new Pair<String,String>(pair.first(), tokens.get(i));
					tokens.remove(i);
					//Add all others first and remove them from to do
					for(int j=0; j<i && j<tokens.size(); j++){
						result.add(new Pair<String,String>(NO_TAG, tokens.get(0)));
						tokens.remove(0);
					}
					result.add(newPair);
					//Back to 0 since tokens has been made smaller
					i=-1;
					if(pair.second().endsWith(token)){
						break;
					}
				}
			}
		}
		
		//Add remaining tokens
		for(int i=0; i<tokens.size(); i++){
			result.add(new Pair<String,String>(NO_TAG, tokens.get(i)));
		}
		
		return result;
	}
	
	private String makeCoNNL(){
		if(truthCoNNL==null){
			String result = "";
			
			for(int i=0; i<trainingContents.size(); i++){
				List<Pair<String,String>> taggedSentence = addTagsToTokens(i);
				
				for(Pair<String,String> taggedPair : taggedSentence){
					result += taggedPair.second();
					result += COLUMN_SPLITTER;
					result += taggedPair.first();
					result += "\n";
				}
				
				result += LINE_SPLITTER;
			}
			truthCoNNL = result;
		}
		
		return truthCoNNL;
	}
	
	public AbstractSequenceClassifier<CoreLabel> getClassifier(){
		return classifier;
	}
}
