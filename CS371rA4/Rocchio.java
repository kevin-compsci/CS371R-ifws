package ir.classifiers;

import java.util.*;
import java.io.*;
import java.lang.*;
import ir.vsr.*;

public class Rocchio extends Classifier {
	/* Global Variables */
	String name = "Rocchio"; //name of the classifier and directory for indexing
	HashMap<String, HashMapVector> prototypeVectors = new HashMap<String, HashMapVector>(); //mapping of category --> prototype Vectors
	boolean negFlag = false, isEmpty = false; //if true then subtract vectors so they're not considered; isEmpty to see if the set of training data is empty

	/* Constructor for Rocchio */
	public Rocchio(String[] categoryList, boolean neg) {
		this.categories = categoryList;
		negFlag = neg;
		setInitialPrototypeVectors();
	}

	/* 
	* getName String function will return the name of the classifier
	* INPUT: NONE
	* OUTPUT: String name
	*/
	public String getName() {
		return name;
	} 

	/*
	* setInitialPrototypeVectors void function will set the initial prototype vectors to zero
	* INPUT: NONE
	* OUTPUT NONE
	*/
	public void setInitialPrototypeVectors() {
		/* local variable */
		int count = 0;
		HashMapVector pVector;
		/* loop to set the prototype vectors to 0 per category */
		while (count < this.categories.length) {
			pVector = (new TextStringDocument(this.categories[count], false)).hashMapVector();
			prototypeVectors.put(this.categories[count], pVector);
			count++;
		}
	}

	/*
	* train void function will call either normal or modified rocchio to train data
	* INPUT: List<Example> trainExamples
	* OUTPUT: NONE
	*/
	public void train(List<Example> trainExamples) {
		if (negFlag) {
			modifiedTrainRocchio(trainExamples);
		}
		else {
			normalTrainRocchio(trainExamples);
		}
	}

	/*
	* test boolean function will call either normal or modified rocchio to test data
	* INPUT: List<Example> testExample
	* OUTPUT: NONE
	*/
	public boolean test(Example testExample) {
		return normalTestRocchio(testExample);
	}

	/*
	* normalTrainRocchio void function will perform the regular rocchio computation for training and test data
	* INPUT: List<Example> examples
	* OUTPUT: NONE
	*/
	public void normalTrainRocchio(List<Example> examples) {
		/* Local Variables */
		Double maxFreq = 0.0, length = 0.0; //max token of a document and vector length
		int exampleCount = 0; //iterative counter
		String categoryName = ""; //categoryName is the name of the category
		HashMapVector exampleVector, pVector; //vectors to reference
		Example x; //instance of the example
		/* check if list is empty */
		if (examples.isEmpty()) {
			isEmpty = true;
		}
		else {
			/* Loop through each example and try to categorize with prototype vectors */
			while (exampleCount < examples.size()) {
				x = examples.get(exampleCount); //get example object as the instance
				categoryName = this.categories[x.getCategory()]; //get name of the category
				pVector = prototypeVectors.get(categoryName); //get current prototype vector
				exampleVector = x.getHashMapVector(); //get vector of the example doc x
				maxFreq = exampleVector.maxWeight(); //get max frequency of a token in doc x for normalization
				length = exampleVector.length(); //get the vector length of doc x
				/* normalize document vector by max frequency found in that doc and add to mapping for future reference */
				length = length / maxFreq;
				/* add lenth to prototype vector */
				pVector.addScaled(exampleVector, length);
				prototypeVectors.put(categoryName, pVector);
				/* increment loop */
				exampleCount++;
			}
		}
	}

	/*
	* modifiedTrainRocchio void function will perform the modified rocchio computation for training and test data, but some vectors may be subtracted
	* INPUT: List<Example> examples
	* OUTPUT: NONE
	*/
	public void modifiedTrainRocchio(List<Example> examples) {
		/* Local Variables */
		Double maxFreq = 0.0, length = 0.0; //value for max token and vector length
		int exampleCount = 0; //iterative counter
		String categoryName = ""; //categoryName is the name of the category
		HashMapVector exampleVector, pVector; //vectors to reference
		Example x; //instance of the example
		/* check if list is empty */
		if (examples.isEmpty()) {
			isEmpty = true;
		}
		else {
			/* Loop through each example and try to categorize with prototype vectors */
			while (exampleCount < examples.size()) {
				x = examples.get(exampleCount); //get example object as the instance
				categoryName = this.categories[x.getCategory()]; //get name of the category
				pVector = prototypeVectors.get(categoryName); //get current prototype vector
				exampleVector = x.getHashMapVector(); //get vector of the example doc x
				maxFreq = exampleVector.maxWeight(); //get max frequency of a token in doc x for normalization
				length = exampleVector.length(); //get the vector length of doc x
				/* normalize document vector by max frequency found in that doc and add to mapping for future reference */
				length = length / maxFreq;
				/* add lenth to prototype vector */
				pVector.addScaled(exampleVector, length);
				prototypeVectors.put(categoryName, pVector);
				/* Loop through to Subtract docs from all other categories */
				for (Map.Entry<String, HashMapVector> entry : prototypeVectors.entrySet()) {
					/* if key matches current category, then continue */
					if (entry.getKey().equals(categoryName)) {
						continue;
					}
					entry.getValue().subtract(exampleVector); //copy?
					prototypeVectors.put(entry.getKey(), entry.getValue());
				}
				/* increment loop */
				exampleCount++;
			}
		}
	}

	/*
	* normalTestRocchio void function will perform the test data for the Rocchio algorithm
	* INPUT: Example example
	* OUTPUT: TRUE or FALSE
	*/
	public boolean normalTestRocchio(Example example) {
		/* Local Variables */
		Double cosineSimilarity = 0.0, m = -2.0, docLength = 0.0; //cosine similarity value and m is the initial maximum cosine similiarity
		HashMapVector pVector, exampleVector = example.getHashMapVector(); //vectors to reference
		String categoryName = ""; //string that tells which category the test data is closest to
		/* Get random category since set was empty */
		if (isEmpty) {
			isEmpty = false;
			Random rand = new Random();
			return (rand.nextInt(3) == example.getCategory()); //return randomly chosen category class
		}
		/* loop through the prototype vectors */
		for (Map.Entry<String, HashMapVector> entry : prototypeVectors.entrySet()) {
			pVector = entry.getValue(); //get current prototype vector
			cosineSimilarity = exampleVector.cosineTo(pVector, pVector.length()); //get cosine similarity
			if (cosineSimilarity > m) {
				m = cosineSimilarity; //set the initial/old cosine similarity to the new value
				categoryName = entry.getKey(); //assign r to be the category which has the closest prototype vector
			}
		}
		return (Arrays.asList(this.categories).indexOf(categoryName) == example.getCategory()); //compare and return
	}
}