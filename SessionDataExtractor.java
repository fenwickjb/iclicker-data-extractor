
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Scanner;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class SessionDataExtractor {
    private static final String defaultVoteMap = "A=Yes:B=ErrorB:C=No:D=ErrorD:E=Abstain";

    private Document document;
    private String filename;
    private String idFilename;
    private String outFilename;
    private PrintStream outFile;
    private ArrayList<String> voteMap;
    private HashMap<String,String> clickerMap;

    public static void usage() {
	System.err.println();
	System.err.println("usage: java SessionDataExtractor [options] session-file");
	System.err.println("options available:");
	System.err.println("  -votemap A-E_mapping_string");

	System.err.println("notes: RemoteId.csv file is automatically looked for.");
	System.err.println("sample usage:");
	System.err.println("  java SessionDataExtractor -votemap A=True:B=Unused:C=C:D=D:E=False L170909153200.xml");
	System.err.println();
    }

    public SessionDataExtractor(String[] args) {
	String votemapString = defaultVoteMap;

	if (args.length == 1) {
	    filename = args[0];
	}
	else if ("-votemap".equals(args[0])) {
	    votemapString = args[1];
	    filename = args[2];
	}
	else {
	    usage();
	    System.exit(1);
	}

	outFilename = buildOutput(filename);
	voteMap = buildVoteMap(votemapString);
	clickerMap = buildClickerMap(filename);
    }

    private ArrayList<String> buildVoteMap(String mapping) {
	boolean formatError = false;
	String[] starts = {"A=","B=","C=","D=","E="};

	String[] map = mapping.split(":");
	for (int i=0; i < 5; i++) {
	    if (map[i] != null && map[i].startsWith(starts[i])) {
		map[i] = map[i].substring(2);
	    }
	    else formatError = true;
	}

	if (formatError) {
	    System.err.println("-votemap string had a format error using default");
	    return buildVoteMap(defaultVoteMap);
	}
	else {
	    return new ArrayList<String>(Arrays.asList(map));
	}
    }

    private void openDocument() {
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

    private String printVoteMap() {
	String map = "";
	map += "A="; map += voteMap.get(0);
	map += ":B="; map += voteMap.get(1);
	map += ":C="; map += voteMap.get(2);
	map += ":D="; map += voteMap.get(3);
	map += ":E="; map += voteMap.get(4);
	return map;
    }
    private void notifyUser(PrintStream stream) {
	if (stream != null) {
	    stream.println();
	    stream.println("Preparing to read i>Clicker session data...");
	    stream.println("  Input file: " + filename);
	    if (clickerMap.size() == 0) 
		stream.println("  Did not find RemoteIDs file");
	    else
		stream.println("  Found RemoteIDs file");
	    if (outFile == System.out) 
		stream.println("  Unable to open output file, dumping to screen.");
	    else
		stream.println("  Output file: " + outFilename);
	    stream.println("  Clicker response map: " + printVoteMap());
	    stream.println();
	}
    }

    public static void main(String[] args) {

	SessionDataExtractor extractor = new SessionDataExtractor(args);
	extractor.notifyUser(System.out);

	extractor.openDocument();

	//	extractor.printStudents();
	extractor.printAsCSV();
    }

    private Scanner openScanner(String filename) {
	Scanner scanner;
	try {
	    scanner = new Scanner(new File(filename));
	}
	catch (FileNotFoundException e) {
	    scanner = null;
	}
	catch (Exception e) {
	    scanner = null;
	}
	return scanner;
    }

    private String buildOutput(String dataFilename) {
	int chIndex = dataFilename.lastIndexOf('/');
	String of = dataFilename.substring(chIndex+1);
	chIndex = of.lastIndexOf('.');
	of = of.substring(0,chIndex);
	of += ".CSV";

	try {
	    outFile = new PrintStream(new File(of));
	}
	catch (Exception e) {
	    outFile = System.out;
	}
	
	return of;
    }

    private HashMap<String,String> buildClickerMap(String dataFilename) {
	String path = "";
	String filename = "RemoteID.csv";
	Scanner userIDs;
	userIDs = openScanner(filename);
	if (userIDs == null) {
	    int lastSlash = dataFilename.lastIndexOf('/');
	    if (lastSlash != -1) {
		path = dataFilename.substring(0,lastSlash+1);
		userIDs = openScanner(path+filename);
	    }
	}

	HashMap<String,String> map =  new HashMap<String,String>();

	// fill map
	if (userIDs != null) {
	    while (userIDs.hasNextLine()) {
		String entryLine = userIDs.nextLine();

		String[] iduser = entryLine.split(",");
		String[] idParts = iduser[0].split("\"");
		String[] userParts = iduser[1].split("\"");

		map.put(idParts[1], userParts[1]);
	    }
	}

	return map;
    }


    private String convertToDate(String filename) {
	/* iClicker filename format:  LYYMMDDHHMM.xml
	   L is an L or an x (usually L)
	   YY is year, e.g., 17 for 2017
	   MM is month, e.g., 09 for September
	   DD id day; HH is hour (is 24 hour clock?), MM is minute

	   Output format: MM/DD/YYYY
	*/
	int lastSlash = filename.lastIndexOf('/');
	filename = filename.substring(lastSlash+2);
	String dateStr = filename.substring(2,4); 
	dateStr += "/";
	dateStr += filename.substring(4,6);
	dateStr += "/20";
	dateStr += filename.substring(0,2);
	return dateStr;
    }
    
    // Debugging routine
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

    private void printAsCSV() {

	/* date session time pollname senator clickerid vote */
	outFile.println("Date,Session,Time,Poll,Senator,ClickerID,Vote");

	String date = convertToDate(filename);

	Element sessionElement = document.getRootElement();

	List<Element> pollingList = sessionElement.getChildren();
	for (int temp = 0; temp < pollingList.size(); temp++) {    
	    Element poll = pollingList.get(temp);

		List<Element> voteList = poll.getChildren();
		for (int vIndex = 0; vIndex < voteList.size(); vIndex++) {
		    Element vote = voteList.get(vIndex);

		    /* date session time pollname senator clickerid vote */
		    outFile.print(date);
		    outFile.print("," + sessionElement.getAttribute("ssnn").getValue());
		    outFile.print("," + poll.getAttribute("strt").getValue());
		    outFile.print("," + poll.getAttribute("qn").getValue());
		    String clickerID = vote.getAttribute("id").getValue();
		    outFile.print("," + getClickerUser(clickerID));
		    outFile.print("," + clickerID);
		    String mappedVote = mapVote(vote.getAttribute("ans").getValue());
		    outFile.println("," + mappedVote);
		}
	    }
    }
    private String mapVote(String response) {
	String mappedResponse;
	try {
	    mappedResponse = voteMap.get(response.charAt(0)-'A');
	}
	catch (IndexOutOfBoundsException e) {
	    mappedResponse = "NoVoteRecorded";
	}
	catch (Exception e) {
	    mappedResponse = response;
	}
	return mappedResponse;
    }

    private String getClickerUser(String clickerID) {
	String user;
	user = clickerMap.get(clickerID);
	if (user == null) 
	    user = new String("UnknownUser");

	return user;
    }
}
