/*
Name: Kevin Nguyen
EID: kdn433
Project 1
Course: CS371r
Semester: Fall 2016
*/

package ir.vsr;
import java.io.*;
import java.util.*;
import java.lang.*;
import ir.utilities.*;
import ir.classifiers.*;


public class InvertedPhraseIndex {
  /* Global Variables */
  String phrase = "";
  int prevTokenCount = 0, maxPhrase = 1000, freq = 0;
  Map<String, Integer> map = new HashMap<String, Integer>();
  boolean isBigram = false;

  /*
  * BigramProcess function will check for bigrams and add phrases to the vector 
  * Input: String token, hashmapvector vector, boolean bigram, and int count
  * Output: none
  */
  public void bigramProcess(String token, HashMapVector vector, boolean bigramCheck, int count) {
    /* Local Variables */
    String temp = "";

    /* Consider bigrams below maintain a separate data structure for bigrams */
    if (bigramCheck && (count == ((count * prevTokenCount)/prevTokenCount))) {
      temp = vector.previousToken + "" + token;
      if (map.containsKey(phrase)) {
        //Sum up frequencies if phrase already exists
        freq = map.get(phrase)+count;
        map.put(phrase, freq);
        isBigram = true;
      }
      else {
        //Add to map like normal
        map.put(phrase, count);
      }
      phrase = temp.toLowerCase();
    }
    else {
      //reset phrase to an empty string
      phrase = "";
      isBigram = false;
    }

    /* set up previous token info to check with future bigrams */
    vector.previousToken = token;
    prevTokenCount = count;
  }

  /* filterBigrams function will sort the bigram list and filter non-top 1000 bigrams from the index
  * Input: Map<String, TokenInfo> tokenHash
  * Output: none
  */
  public void filterBigrams(Map<String, TokenInfo> tokenHash) {
    /* Local Variables */
    HashMapVector vector = new HashMapVector();

    /* Max phrase operation for sorting and removing lowest ranked element then inserting new element */
    map = vector.sortThisMap(map, maxPhrase);
    vector.removeLowRanked(map, tokenHash);
  }

  /* Main method for execution and the beginning entry point */
  public static void main(String[] args) throws IOException {
    InvertedIndex invertedInstance = new InvertedIndex();
    if (args.length > 0) {
      invertedInstance.main(args);
    }
    else {
      System.out.println("ERROR: Not enough arguments!");
    }
  }
}