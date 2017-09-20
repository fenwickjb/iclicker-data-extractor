import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class SessionDataExtractor {

    private Document document;

    public static void usage() {
	System.err.println("usage: java SessionDataExtractor session-file");
	System.err.println("notes: RemoteId.csv file is automatically looked for.");
    }

    private void openDocument(String filename) {
	try {
	    File inputFile = new File(filename);
	    SAXBuilder saxBuilder = new SAXBuilder();
	    document = saxBuilder.build(inputFile);
	} catch(JDOMException e) {
	    e.printStackTrace();
	} catch(IOException ioe) {
	    ioe.printStackTrace();
	}
    }
    public static void main(String[] args) {
	if (args.length != 1) {
	    usage();
	    System.exit(1);
	}

	SessionDataExtractor extractor = new SessionDataExtractor();
	extractor.openDocument(args[0]);

	extractor.printStudents();
    }

    private void printStudents() {

	    Element sessionElement = document.getRootElement();
	    System.out.println("Root element :" + sessionElement.getName());
	    System.out.println("Session name :" + sessionElement.getAttribute("ssnn").getValue());

	    List<Element> pollingList = sessionElement.getChildren();
	    System.out.println("----------------------------");

	    for (int temp = 0; temp < pollingList.size(); temp++) {    
		Element poll = pollingList.get(temp);
		System.out.println("\nCurrent Element :" + poll.getName());
		System.out.println("Question name :" + poll.getAttribute("qn").getValue());

		List<Element> voteList = poll.getChildren();
		for (int vIndex = 0; vIndex < voteList.size(); vIndex++) {
		    Element vote = voteList.get(vIndex);
		    System.out.print("Clicker id #: " + vote.getAttribute("id").getValue());
		    System.out.println(" voted " + vote.getAttribute("ans").getValue());
		}
	    }
    }
}
