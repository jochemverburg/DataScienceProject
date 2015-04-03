package Reasoner;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.PrintUtil;

public class Executer {

	public static void main(String[] args) {
		Executer newEx = new Executer("file:resources/ontologies.xml","file:resources/results.nt");
		
		Resource nadal = newEx.getResource("http://dbpedia.org/resource/Murray%E2%80%93Nadal_rivalry");
		//newEx.printStatements(nadal, null, null);
		//newEx.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
		//newEx.getProperty("file:///C:/Users/Jochem/Dropbox/UT/Master/DataScience/2.Semantics/Workspace/SW-Assignment2/data/2.6-b-gedcom.xml#parentIn")
		//newEx.printStatements(beatrix, newEx.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
		//testEx.printStatements(nForce, null, null);
		
		ValidityReport validity = newEx.getInfModel().validate();
		if (validity.isValid()) {
		    System.out.println("OK");
		} else {
		    System.out.println("Conflicts");
		    for (Iterator i = validity.getReports(); i.hasNext(); ) {
		        ValidityReport.Report report = (ValidityReport.Report)i.next();
		        System.out.println(" - " + report);
		    }
		}
	}
	
	private InfModel infmodel;
	
	public Executer(String schemaFileNameorURI, String dataFileNameorURI){
		Model schema = FileManager.get().loadModel(schemaFileNameorURI);
		Model data = FileManager.get().loadModel(dataFileNameorURI);
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		infmodel = ModelFactory.createInfModel(reasoner, data);
	}
	
	public InfModel getInfModel(){
		return infmodel;
	}
	
	public Property getProperty(String uri){
		return getInfModel().getProperty(uri);
	}
	
	public Resource getResource(String uri){
		return getInfModel().getResource(uri);
	}
	
	public void printStatements(Resource s, Property p, Resource o) {
	    for (StmtIterator i = getInfModel().listStatements(s,p,o); i.hasNext(); ) {
	        Statement stmt = i.nextStatement();
	        System.out.println(" - " + PrintUtil.print(stmt));
	    }
	}
	
}
