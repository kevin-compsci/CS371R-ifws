package ir.classifiers;

import java.util.*;
import java.io.*;
import java.lang.*;
import ir.vsr.*;

public class TestRocchio {

	/* Main method for exeuction and to serve as entry point for the program
	* INPUT: String args[]
	* OUTPUT: NONE
	*/
	public static void main(String args[]) throws Exception {
		/* Local variables */
	    String dirName = "/u/mooney/ir-code/corpora/dmoz-science/";
	    String[] categories = {"bio", "chem", "phys"};
	    System.out.println("Loading Examples from " + dirName + "...");
	    List<Example> examples = new DirectoryExamplesConstructor(dirName, categories).getExamples();
	    System.out.println("Initializing Rocchio classifier...");
	    Rocchio rocchio;
	    boolean negFlag;

	    // setting debug flag gives very detailed output, suitable for debugging
	    if (args.length > 0 && args[0].equals("-neg"))
	      negFlag = true;
	    else
	      negFlag = false;
	  	/* get Rocchio object */
	    rocchio = new Rocchio(categories, negFlag);

	    // Perform 10-fold cross validation to generate learning curve
	    CVLearningCurve cvCurve = new CVLearningCurve(rocchio, examples);
	    cvCurve.run();
	}
}