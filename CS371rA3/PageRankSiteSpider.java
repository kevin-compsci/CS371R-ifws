package ir.webutils;
import java.util.*;
import java.io.*;
import java.net.*;
import ir.utilities.*;

/**
 * The PageRankSiteSpider class will only manage the page rank within the restricted site and will not spider * other pages into consideration nor rank them
 * This class was made by Kevin Nguyen (kdn433) for project 3 of CS371r, UT Austin
 */

public class PageRankSiteSpider extends PageRankSpider {
	/* -OVERRIDE-
	 * getNewLinks function will iterate over the possible links on a page and return them as a list
	 * INPUT: HTMLPAGE page
	 * OUTPUT: List<Link> object
	 */
	public List<Link> getNewLinks(HTMLPage page) {
		/* Local variables */
	    List<Link> links = new LinkExtractor(page).extractLinks();
	    URL url = page.getLink().getURL();
	    ListIterator<Link> iterator = links.listIterator();
	    /* Iterate and check each link for filter out any irrelevant links */
	    while (iterator.hasNext()) {
	    	Link link = iterator.next();
	      	if (!url.getHost().equals(link.getURL().getHost()))
	        	iterator.remove();
	    }
	    return links; //return the new set of filtered links
	}

	/* main void method that will kickstart the program and be the main entry point of the project
	 * INPUT: String[] args
	 * OUTPUT: none
     */
	public static void main(String args[]) {
		/* call the constructors to initiate the rest of the program */
		PageRankSiteSpider siteSpider = new PageRankSiteSpider();
		PageRankSpider rankSpider = siteSpider;
    	rankSpider.go(args); //begin spidering
 	}
}