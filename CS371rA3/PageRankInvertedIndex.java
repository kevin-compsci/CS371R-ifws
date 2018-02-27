package ir.vsr;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import ir.utilities.*;
import ir.classifiers.*;

/**
 * The PageRankInvertedIndex class will keep track of the page ranks by an inverted index, data structure
 * This class was made by Kevin Nguyen (kdn433) for project 3 of CS371r, UT Austin
 */

public class PageRankInvertedIndex extends InvertedIndex {
  /*
   * The maximum number of retrieved documents for a query to present to the user
   * at a time
   */
	public static final int MAX_RETRIEVALS = 10;

  /*
   * A HashMap where tokens are indexed. Each indexed token maps
   * to a TokenInfo.
   */
  public Map<String, TokenInfo> tokenHash = null;

  /*
   * A list of all indexed documents.  Elements are DocumentReference's.
   */
  public List<DocumentReference> docRefs = null;

  /*
   * The directory from which the indexed documents come.
   */
  public static File dirFile = null;

  /*
   * The type of Documents (text, HTML). See docType in DocumentIterator.
   */
  public short docType = DocumentIterator.TYPE_TEXT;

  /*
   * Whether tokens should be stemmed with Porter stemmer
   */
	public boolean stem = false;

  /*
   * Whether relevance feedback using the Ide_regular algorithm is used
   */
	public boolean feedback = false;

  /*
   * weight = some weight specified by the user
   */
	public static int weight = 0;

  /*
   * dirName is the name of the directory
   */
  public static String dirName = "";

  /*
   * HashMap to store the values read from the page rank file
   */
  public static HashMap<String, String> pageRanking = new HashMap<String, String>();

	/*
	 * Constructor for the class that will send the needed information to the super class
   * INPUT: File dirName, short docType, boolean stem, boolean feedback
   * OUTPUT: NONE
	 */
	public PageRankInvertedIndex(File dirName, short docType, boolean stem, boolean feedback) {
    super(dirName, docType, stem, feedback);
	}

  /* -OVERRIDE-
   * retrieve Retrieval[] function will Perform ranked retrieval on this input query Document vector. In addition the scores will be recomputed with the page rank and weight accordingly.
   * INPUT: HashMapVector vector
   * OUTPUT: Retrieval[]
   */
  public Retrieval[] retrieve(HashMapVector vector) {
    // Create a hashtable to store the retrieved documents.  Keys
    // are docRefs and values are DoubleValues which indicate the
    // partial score accumulated for this document so far.
    // As each token in the query is processed, each document
    // it indexes is added to this hashtable and its retrieval
    // score (similarity to the query) is appropriately updated.
    Map<DocumentReference, DoubleValue> retrievalHash =
        new HashMap<DocumentReference, DoubleValue>();
    // Initialize a variable to store the length of the query vector
    double queryLength = 0.0;
    // Iterate through each token in the query input Document
    for (Map.Entry<String, Weight> entry : vector.entrySet()) {
      String token = entry.getKey();
      double count = entry.getValue().getValue();
      // Determine the score added to the similarity of each document
      // indexed under this token and update the length of the
      // query vector with the square of the weight for this token.
      queryLength = queryLength + incorporateToken(token, count, retrievalHash);
    }
    // Finalize the length of the query vector by taking the square-root of the
    // final sum of squares of its token weights.
    queryLength = Math.sqrt(queryLength);
    // Make an array to store the final ranked Retrievals.
    Retrieval[] retrievals = new Retrieval[retrievalHash.size()];
    // Iterate through each of the retrieved documents stored in
    // the final retrievalHash.
    int retrievalCount = 0;
    for (Map.Entry<DocumentReference, DoubleValue> entry : retrievalHash.entrySet()) {
      DocumentReference docRef = entry.getKey(); //get the document
      double score = entry.getValue().value; //obtain the score
      score = computeFinalScore(score, Double.parseDouble(pageRanking.get(docRef.file.getName())));  //get new score with page ranking and weight
      retrievals[retrievalCount++] = getRetrieval(queryLength, docRef, score); //store the object data into an array
    }
    // Sort the retrievals to produce a final ranked list using the
    // Comparator for retrievals that produces a best to worst ordering.
    Arrays.sort(retrievals);
    return retrievals;
  }

	/* readRanksFromFile void function will look into a text file and read the page ranks accordingly
 	 * INPUT: NONE
	 * OUTPUT: NONE
	 */
	public static void readRanksFromFile() throws IOException {
 		/* Local variables */
   	String fileName = "./" + dirName + "/page_ranks.txt", line = "";
    String[] tokens = null;
   	Scanner file = new Scanner(new File(fileName));
    /* Loop through each line in the text file */
    while (file.hasNext()) {
      /* get line and get the token from each line --> name of file and page rank value */
   		line = file.nextLine();
    	tokens = line.split("\\s");
   		pageRanking.put(tokens[0], tokens[1]);
   	}
    file.close();
	}

  /* computeFinalScore will return the final score of the cosineSimilarity + pageRank*weight
   * INPUT: Double cosineSimilarity, Double pageRank
   * OUTPUT: Double value
   */
  public Double computeFinalScore(Double cosineSimilarity, Double pageRank) {
    return cosineSimilarity + (pageRank * weight);
  }

  /* -OVERRIDE-
   * main function for entry point of the PageRankInvertedIndex.java it will parse arguments like normal
   * INPUT: String[] args
   * OUTPUT: NONE
   */
  public static void main(String[] args) {
    // Parse the arguments into a directory name and optional flag
    dirName = args[args.length - 1];
    short docType = DocumentIterator.TYPE_TEXT;
    boolean stem = false, feedback = false;
    for (int i = 0; i < args.length - 1; i++) {      
      String flag = args[i];
      if (flag.equals("-html"))
        // Create HTMLFileDocuments to filter HTML tags
        docType = DocumentIterator.TYPE_HTML;
      else if (flag.equals("-stem"))
        // Stem tokens with Porter stemmer
        stem = true;
      else if (flag.equals("-feedback"))
        // Use relevance feedback
        feedback = true;
      else if (flag.equals("-weight")) {
        // set the weight for page ranking
        weight = Integer.parseInt(args[++i]);
      }
	    else {
        throw new IllegalArgumentException("Unknown flag: "+ flag);
	    }
    }
    /* Try reading page ranks from file and store them for reference */
    try {
      readRanksFromFile();
    }
    catch (IOException e) {
      System.out.println("IOException error!");
    }
    /* Index the given page by invoking the super class Inverted Index */
    PageRankInvertedIndex invertedIndex = new PageRankInvertedIndex(new File(dirName), docType, stem, feedback);
    InvertedIndex index = invertedIndex;
    index.processQueries();
	}
}