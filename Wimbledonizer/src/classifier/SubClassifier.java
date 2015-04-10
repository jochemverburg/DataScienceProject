package classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
/**
 * This class can add classifications to Persons using an already classified file.
 * Input has to be given as CoNNL:
 * The first column contains the tokens and the second column separated by a tab contains the original classification.
 * Every line represents a different entity, if it has not been classified it can also represent a token.
 * After a tweet/sentence a blank line follows.
 *
 * Output is given the same way
 */
public class SubClassifier {
	
	public static final String COLUMN_DELIM = "\t";
	public static final String SENTENCE_SEPARATOR = "\n";
	public static final String PERSON_ANNOTATION = "Person";
	public static final String CLASS_DELIM = "\t";
	
	public static final String EMPTY_STRING = "";
	
	public static final int TOKEN_COLUMN = 0;
	public static final int CLASS_COLUMN = 1;
	
	/**
	 * Uses the input to find entities classified as the classAnnotation to classify these further and puts this in the file at outputPath. Uses classes to find the concerned classes in the SemanticWeb and the analyzer to discover and analyze whether it's part of this class.
	 * @param inputPath The path where the input-file is originated.
	 * @param outputPath The path where the output-file is originated.
	 * @param analyzers The objects which can analyze the entities and compare them to all entities of the class to check if it's part of that class. If an entity matches multiple classes, the last one in the list is matched. 
	 * @throws IOException
	 */
	public static void subClassifyClass(String inputPath, String outputPath, List<EntityResolutionInterface> analyzers, String classAnnotation) throws IOException{
		BufferedReader reader = new BufferedReader(
				new FileReader( 
						new File(inputPath)));
        Writer w = new BufferedWriter(
        		new OutputStreamWriter(
        				new FileOutputStream(
        						new File(outputPath))));
		String text = null;	
		
		while ((text = reader.readLine()) != null) {
			if(!text.isEmpty()){
				String[] splitted = text.split(COLUMN_DELIM);
			   	if(splitted[CLASS_COLUMN].equals(classAnnotation)){
			   		String entity = splitted[TOKEN_COLUMN];
			   		String newClass = null;
			   		String newEntity = null;
			   		for(EntityResolutionInterface analyzer : analyzers){
			   			String mainEntry = analyzer.isOfClass(entity);
			   			if(mainEntry!=null){
			   				newClass = analyzer.getClassName();
			   				newEntity = mainEntry;
			   			}
			   		}
			   		if(newEntity == null){
			   			newClass = classAnnotation;
			   			newEntity = entity;
			   		}
			   		w.write(newEntity+COLUMN_DELIM+newClass);
			   	}
			   	else{
			   		w.write(text);
			   	}
			}
			w.write(SENTENCE_SEPARATOR);
		}
		   
	    reader.close();
        w.close();
	}
	
	/**
	 * Can classify persons, uses PERSON_ANNOTATION in subClassifyClass()
	 * @param inputPath
	 * @param outputPath
	 * @param analyzers
	 * @throws IOException
	 */
	public static void classifyPerson(String inputPath, String outputPath, List<EntityResolutionInterface> analyzers) throws IOException{
		subClassifyClass(inputPath, outputPath, analyzers, PERSON_ANNOTATION);
	}
	
	
}
