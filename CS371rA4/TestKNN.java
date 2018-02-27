package ir.classifiers;

import java.util.*;
import java.io.*;
import java.lang.*;
import ir.vsr.*;

public class TestKNN {
	
	public static void main(String args[]) throws Exception {
	  	/* Local variables */
	    String dirName = "/u/mooney/ir-code/corpora/dmoz-science/";
	    String[] categories = {"bio", "chem", "phys"};
	    System.out.println("Loading Examples from " + dirName + "...");
	    List<Example> examples = new DirectoryExamplesConstructor(dirName, categories).getExamples();
	    System.out.println("Initializing K-Nearest Neighbors classifier...");
	    KNN knn;
	    int k = 5;

	    // setting debug flag gives very detailed output, suitable for debugging
	    if (args.length > 0 && args[0].equals("-K"))
	      k = Integer.parseInt(args[1]);

	  	/* get K-Nearest Neightbor class object */
	    knn = new KNN(categories, k);

	    // Perform 10-fold cross validation to generate learning curve
	    CVLearningCurve cvCurve = new CVLearningCurve(knn, examples);
	    cvCurve.run();
	}
}