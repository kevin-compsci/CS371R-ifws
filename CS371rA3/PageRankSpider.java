package ir.webutils;
import java.util.*;
import java.io.*;
import java.net.*;
import ir.utilities.*;

/**
 * The PageRankSpider class will manage the page ranks of every known page from the spidering
 * This class was made by Kevin Nguyen (kdn433) for project 3 of CS371r, UT Austin
 */

public class PageRankSpider extends Spider {
	/* Global Variables */
	/* the variables are represented directly from the CS371r slides for page ranking 
   * currentPageName = the current page being referenced
	 * s = set of total pages
	 * nq = total number of out-links from a page q
	 * rp = the page rank for the page
	 * rq = the fraction of authority for a page q
	 * rpp = (R prime p) is the summation of Rq / Nq
	 * c = the normalizing constant factor
	 * Ep = the rank Source that replenishes the rank of a page, p
	 * alpha = constant that is between 0 and 1. (i.e "0 < alpha < 1")
	 * iterations = the total number of needed iterations specified by the user for page ranking
	 */
  public static String currentPageName = "", dirName = "";
	public static Double s = 0.0, rp = 0.0, rq = 0.0, rpp = 0.0, c = 0.0, ep = 0.0, alpha = 0.15;
	public static int iterations = 50, nq = 0;

  /* Hashmaps for page ranking storage and reference */
  HashMap<String, Double> pageRanking = new HashMap<String, Double>();
  HashMap<String, Double> tempRanking = new HashMap<String, Double>();
  HashMap<String, String> pageNameURL = new HashMap<String, String>();

	/* -OVERRIDE-
	 * go void function is a method from Spider.java that will initiate the process for the program by parsing arguments and starting the crawl
   * INPUT: String[] args
   * OUTPUT: NONE
	 */
  public void go(String[] args) {
  	processArgs(args);
   	doCrawl();
  }

  /* -OVERRIDE-
   * processArgs is a method from Spider.java that will process the command line arguments
   * INPUT: String[] args
   * OUTPUT: NONE
   */
  public void processArgs(String[] args) {
    /* local variable */
    int i = 0;
    /* loop through all argument tokens from command line */
    while (i < args.length) {
      /* conditionals based on the parameters */
      if (args[i].charAt(0) == '-') {
        if (args[i].equals("-safe"))
          this.handleSafeCommandLineOption();
        else if (args[i].equals("-d")) {
          dirName = args[++i];
          this.handleDCommandLineOption(args[i]);
        }
        else if (args[i].equals("-c"))
          this.handleCCommandLineOption(args[++i]);
        else if (args[i].equals("-u"))
          this.handleUCommandLineOption(args[++i]);
        else if (args[i].equals("-slow"))
          this.handleSlowCommandLineOption();
      }
      ++i;
    }
  }

  /* -OVERRIDE-
   * doCrawl method is a method from Spider.java that will handle the crawling process as needed
   * INPUT: NONE
   * OUTPUT: NONE
   */
  public void doCrawl() {
    /* Local Variable */
    Graph pageGraph = new Graph();
    HTMLPage currentPage = null;
    /* Check if there are any links to visit */
    if (this.linksToVisit.size() == 0) {
      System.err.println("Exiting: No pages to visit.");
      System.exit(0);
    }
    this.visited = new HashSet<Link>();
    /* Iterate over links that could still need to be visited and indexed */
    while (this.linksToVisit.size() > 0 && this.count < this.maxCount) {
      // Pause if in slow mode
      if (this.slow) {
        synchronized (this) {
          try {
            wait(1000);
          }
          catch (InterruptedException e) {
          }
        }
      }
      // Take the top link off the queue
      Link link = this.linksToVisit.remove(0);
      link.cleanURL(); // Standardize and clean the URL for the link
      System.out.println("Trying: " + link);
      // Skip if already visited this page
      if (!this.visited.add(link)) {
        System.out.println("Already visited");
        continue;
      }
      // check if page is HTML or not
      if (!this.linkToHTMLPage(link)) {
        System.out.println("Not HTML Page");
        continue;
      }
      currentPage = null;
      /* Use the page retriever to get the page otherwise catch exception */
      try {
        currentPage = retriever.getHTMLPage(link);
      }
      catch (PathDisallowedException e) {
        System.out.println(e);
        continue;
      }
      /* Check for empty page */
      if (currentPage.empty()) {
        System.out.println("No Page Found");
        continue;
      }
      /* check if index is alowed */
      if (currentPage.indexAllowed()) {
        this.count++;
        System.out.println("Indexing" + "(" + this.count + "): " + link);
        this.indexPage(currentPage, pageGraph, link);
        s++; //increment the S, the total set of pages
      }
      /* Get new links if count is not yet at the max allowed number of links */
      if (this.count < this.maxCount) {
        List<Link> newLinks = getNewLinks(currentPage);
        // System.out.println("Adding the following links" + newLinks);
        // Add new links to end of queue
        this.linksToVisit.addAll(newLinks);
        getNewLinkEdges(newLinks, pageGraph, link);
      }
    }
    /* print out the graph */
    System.out.println("\nGRAPH STRUCTURE: ");
    pageGraph.print();
    /* get list of pages and set initial rankings for the pages */
    setInitialRanking();
    /* gets the page rankings --> could be costly */
    getPageRankings(pageGraph);
    /* print the final page ranking output to a .txt file */
    try {
      printRanksToFile();
    }
    catch (IOException e) {
    }
    /* Print ranks to screen */
    printRanking();
  }

  /* printRanking void function will take the page rankings and display them on screen
   * INPUT: NONE
   * OUTPUT NONE
   */
  public void printRanking() {
    System.out.println("\nPAGE RANK: ");
    /* Loop through the pageNameURL hashmap and display the key (link) and the page ranking value */
    for (String key : pageNameURL.keySet()) {
      System.out.println("PR(" + key + "): " + pageRanking.get(key));
    }
  }
 
  /* printRanksToFile void function will take the list of page ranks that represents each page and output them to a file 
   * INPUT: NONE
   * OUTPUT: NONE
   */
  public void printRanksToFile() throws IOException {
  	/* Local variables */
  	int count = 0;
  	String line = "";
    PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dirName+"/page_ranks.txt")));
  	/* Loop to append all page rank data to a txt file */
    for (String key : pageRanking.keySet()) {
      line = pageNameURL.get(key) + ".html " + Double.toString(pageRanking.get(key));
      out.println(line);
    }
    /* close file when finished */
    out.close();
  }

  /* getNewLinkEdges void function will take the list of newlinks and try to form and edge from the current node (page)
   * INPUT: List<Link> newLinks, Graph pageGraph, Link currentLink
   * OUTPUT: NONE
   */
  public void getNewLinkEdges(List<Link> newLinks, Graph pageGraph, Link currentLink) {
    /* Local Variables */
    int linkCount = 0;
    HTMLPage tempPage = null;
    Link linkTemp = null;
    /* Loop to iterate through the list of new links*/
    while (linkCount < newLinks.size()) {
      linkTemp = newLinks.get(linkCount); 
      /* try and get the page from list of links */
      try {
        tempPage = retriever.getHTMLPage(linkTemp);
      }
      catch (PathDisallowedException e) {
        linkCount++;
        continue;
      }
      /* test if index is allowed if not then skip this temp page reference */
      if (tempPage.indexAllowed()) {
        /* Add edge between nodes, create one if node doesn't exist and retry --> handled by Graph.java */
        pageGraph.addEdge(currentLink.toString(), linkTemp.toString());
      }
      linkCount++; //increment loop
    }
  }

  /* -OVERRIDE-
   * indexPage void function will index into a given page and place into the graph if it's not present
   * INPUT: HTMLPage page, Graph pageGraph
   * OUTPUT: NONE
   */
  protected void indexPage(HTMLPage page, Graph pageGraph, Link link) {
    /* get the String for the page name */
    currentPageName = "P" + MoreString.padWithZeros(this.count, (int) Math.floor(MoreMath.log(this.maxCount, 10)) + 1);
    /* use the page name and put it into the graph if it doesn't exist */
    pageGraph.getNode(link.toString()); pageNameURL.put(link.toString(), currentPageName);
    /* write the page name to some file */
    page.write(saveDir, currentPageName);
  }

  /* setInitialRanking void function will iterate over the graph and set the initial
   * INPUT: Graph pageGraph and Node[] pages
   * OUTPUT: none
   */
  public void setInitialRanking() {
    /* Loop to append all page ranks to a txt file */
    for (String key : pageNameURL.keySet()) {
      /* Hashmaps will have string link as key and page ranking as value */
      pageRanking.put(key, (1.0/s));
      tempRanking.put(key, (1.0/s));
    }
  }

  /* getPageRankings void function will compute the page ranks for all pages and store them into a hashmap
   * INPUT: Graph pageGraph
   * OUTPUT: none
   */
  public void getPageRankings(Graph pageGraph) {
    /* Local variables */
    Double c = 0.0, total = 0.0;
    int count = 0, edgeCount = 0; //control loop iterations
    Node currentPage = null, q = null; //Node is the page that we're looking at
    List<Node> edgesIn = null, edgesOut = null; //lists of the nodes that points to and from the current node 
    ep = alpha/s; //constant factor for page ranking E(p)
    /* iterate page ranking algorithm up the the number of iterations allowed */
    while (count < iterations) {
      /* loop through each page in the set S (or the list) of pages */
      for (String key : pageNameURL.keySet()) {
        /* get the current page node from graph based on the ith key from pageNameURL */
        currentPage = pageGraph.getExistingNode(key);
        edgesIn = currentPage.edgesIn;
        /* loop through each page with incoming links to the current page, computes the summation of R(q)/Nq */
        while (edgeCount < edgesIn.size()) {
          /* compute the summation for page ranking */
          q = edgesIn.get(edgeCount);
          nq = q.edgesOut.size(); //get number of outlinks for node q
          rp = pageRanking.get(q.name);
          rq = rq + (rp/nq); //add up the summation of Rq/Nq
          /* increment loop */
          edgeCount++;
        }
        /* compute the R prime of p (Rpp) and store the value temporarily in a map and increment the value for total */
        rpp = ((1.0-alpha)*rq)+ep;
        tempRanking.put(currentPage.name, rpp);
        total += rpp;
        /* increment & reset loops */
        edgeCount = 0;
        rq = 0.0;
      }
      c = 1.0/total;  //normalization constant
      /* loop through the pages and put new ranking values into the pageRanking map for use in the next iteration */
      for (String key : tempRanking.keySet()) {
        pageRanking.put(key, c*tempRanking.get(key));
      }
      total = 0.0; //reset total for next iteration
      /* increment & reset loops */
      count++;
    }
  }

	/* main method of program execution
	 * INPUT: String args
	 * OUTPUT: none
   */
	public static void main(String args[]) {
    /* Get super class constructor and process args */
		PageRankSpider rankSpider = new PageRankSpider();
    Spider mySpider = rankSpider;
    mySpider.go(args);
  }
}