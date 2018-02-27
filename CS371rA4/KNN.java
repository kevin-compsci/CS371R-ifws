package ir.classifiers;

import java.util.*;
import java.io.*;
import java.lang.*;
import ir.vsr.*;

public class KNN extends Classifier {
	/* Global Variables */
	int k = 5; //values for neighbor limit
	String name = "KNN"; //name of the classifier
	InvertedIndex xIndex; //inverted index for set of document X
	HashMap<DocumentReference, Integer> docRef = new HashMap<DocumentReference, Integer>();
	boolean isEmpty = false;

	/* Constructor for the class, KNN */
	public KNN(String[] categoryList, int kValue) {
		this.categories = categoryList;
		k = kValue;
	}

	/* 
	* getName String function will return the name of the classifier
	* INPUT: NONE
	* OUTPUT: String name
	*/
	public String getName() {
		return name;
	} 

	/** -FROM Example.java-
   	* Finds the class ID from the name of the document file.
   	* Assumes file name contains the category name as a substring
   	* INPUT: String name
   	* OUTPUT: Integer value
   	*/
  	public int findClassID(String name) {
    	for (int count = 0; count < this.categories.length; count++) {
     		if (name.indexOf(this.categories[count]) != -1)
        	return count;
    	}
    	return -1;
  	}	

	/*
	* train void function will get the training data for KNN
	* INPUT: List<Example> examples
	* OUTPUT: NONE
	*/
	public void train(List<Example> examples) {
		/* Local variables */
		int count = 0, catID = 0; //iterative count and category ID
		DocumentReference doc; //document reference variable
		xIndex = new InvertedIndex(examples); //use inverted index to set the TF-IDF vectors for each doc x in the set of examples
		/* Check for empty set */
		if (examples.isEmpty()) {
			isEmpty = true;
		}
		else {
			/* iterate through loop to put docs and their corresponding category into a hashmap for future reference */
			while (count < xIndex.docRefs.size()) {
				doc = xIndex.docRefs.get(count); //get doc reference
				catID = findClassID(xIndex.docRefs.get(count).file.getName()); //get id of category 
				docRef.put(doc, catID); //put doc reference and its category into map
				count++; //increment loop
			}
		}
	}

	/*
	* test boolean function will get the test data on KNN
	* INTPUT: Example example
	* OUTPUT: TRUE or FALSE
	*/
	public boolean test(Example example) {
		/* Local Variables */
		int count = 0, maxValue = 0, categoryIndex = -1, bio = 0, chem = 0, phys = 0; //loop and category incrementors
		HashMapVector docYVector = example.getHashMapVector(); //get hashmap vector for instance test example doc y
		Retrieval[] retrievalList = xIndex.retrieve(docYVector); //get the retrieval list
		int[] resultList = {0, 0, 0}; //data structure to hold information about which category is more frequent
		/* Get random category since the set was empty */
		if (isEmpty) {
			isEmpty = false;
			Random rand = new Random();
			return (rand.nextInt(3) == example.getCategory()); //return a randomly chosen category class
		}
		/* Loop to collect the top k documents */
		while (count < Math.min(retrievalList.length, 0 + k)) {
			categoryIndex = docRef.get(retrievalList[count].docRef);
			/* if file names match exactly then just return the index of it */
			if (retrievalList[count].docRef.file.getName().equals(example.name)) {
				return (docRef.get(retrievalList[count].docRef) == example.getCategory());
			}
			/* Increment the corresponding index from the array for the category */
			if (categoryIndex == 0) {
				bio++;
				resultList[0] = bio;
			}
			else if (categoryIndex == 1) {
				chem++;
				resultList[1] = chem;
			}
			else {
				phys++;
				resultList[2] = phys;
			}
			count++; //increment counter
		}
		count = 0; //reset counter
		/* Loop to find the max within the array and return the index of it */
		while (count < resultList.length) {
			if (resultList[count] > maxValue) {
				maxValue = resultList[count]; //update the max value
				categoryIndex = count; //the index is renewed to the current index that has the maximum value
			}
			count++; //increment counter
		}
		return (categoryIndex == example.getCategory()); //return highest value index category
	}
}