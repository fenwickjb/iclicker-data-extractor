
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
    private static final String defaultVoteMap = "A=Yes:B=B?:C=No:D=D?:E=Abstain";

    private Document document;
    private String osName;
    private char dirSlashChar;
    private String filename;
    private String idFilename;
    private String outFilename;
    private PrintStream outFile;
    private String outVotelogFilename;
    private PrintStream outVotelogFile;
    private ArrayList<String> voteMap;
    private String[] qIgnoreList;
    private HashMap<String,String> clickerMap;
    private HashMap<String,ArrayList<String>> senatorMap;

    public static void usage() {
	System.err.println();
	System.err.println("usage: java SessionDataExtractor [options] session-file");
	System.err.println("options available:");
	System.err.println("  -votemap A-E_mapping_string");
	System.err.println("  -qignore number-list");

	System.err.println("notes: RemoteId.csv file is automatically looked for.");
	System.err.println("sample usage:");
	System.err.println("  java SessionDataExtractor -votemap A=True:B=Unused:C=C:D=D:E=False -qignore 1,3 L170909153200.xml");
	System.err.println();
    }

    public SessionDataExtractor(String[] args) {
	osName = System.getProperty("os.name");
	if (osName.startsWith("Mac")) {
	    dirSlashChar = '/';
	}
	else {
	    dirSlashChar = '\\';
	}

	String votemapString = defaultVoteMap;
	qIgnoreList = new String[0];

	if (args.length == 1) {
	    filename = args[0];
	}
	else if (args.length == 3) {
	    if ("-votemap".equals(args[0])) {
		votemapString = args[1];
	    }
	    else if ("-qignore".equals(args[0])) {
		qIgnoreList = args[1].split(",");
	    }
	    else {
		usage();
		System.exit(1);
	    }
	    filename = args[2];
	}
	else if (args.length == 5) {
	    if ("-votemap".equals(args[0])) {
		votemapString = args[1];
		if ("-qignore".equals(args[2])) {
		    qIgnoreList = args[3].split(",");
		}
		else {
		    usage();
		    System.exit(1);
		}
	    }
	    else if ("-qignore".equals(args[0])) {
		qIgnoreList = args[1].split(",");
		if ("-votemap".equals(args[2])) {
		    votemapString = args[3];
		}
		else {
		    usage();
		    System.exit(1);
		}
	    }
	    else {
		usage();
		System.exit(1);
	    }
	    filename = args[4];
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
	    stream.println("It looks like we are running " 
			   + osName + " machine.");

	    stream.println("  Input file: " + filename);
	    if (clickerMap.size() == 0) 
		stream.println("  Did not find RemoteIDs file, using clickerIDs.");
	    else
		stream.println("  Found RemoteIDs file.");

	    stream.println("  Clicker response map: " + printVoteMap());
	    stream.println("  Ignoring questions: " + printQIgnoreList());

	    if (outFile == System.out) 
		stream.println("  Unable to open output file, dumping to screen.");
	    else
		stream.println("  Output file: " + outFilename);

	    stream.println("Extraction complete.");
	}
    }
    public String printQIgnoreList() {
	String list = "";
	if (qIgnoreList.length > 0) {
	    list += qIgnoreList[0];
	    int idx=1;
	    for ( ; idx < qIgnoreList.length; idx++) {
		list += ",";
		list += qIgnoreList[idx];
	    }
	}
	return list;
    }

    public static void main(String[] args) {

	SessionDataExtractor extractor = new SessionDataExtractor(args);
	extractor.notifyUser(System.out);

	extractor.openDocument();

	//	extractor.printStudents();
	extractor.printSummaryCSV();
	extractor.printVotelogCSV();
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
	int chIndex = dataFilename.lastIndexOf(dirSlashChar);
	String baseFilename = dataFilename.substring(chIndex+1);
	chIndex = baseFilename.lastIndexOf('.');
	if (chIndex == -1) {
	    System.err.println("base:"+baseFilename);
	}
	baseFilename = baseFilename.substring(0,chIndex);
	String of = baseFilename + "_Summary.CSV";

	try {
	    outFile = new PrintStream(new File(of));
	}
	catch (Exception e) {
	    outFile = System.out;
	}
	
	outVotelogFilename = baseFilename + "_Votelog.CSV";
	try {
	    outVotelogFile = new PrintStream(new File(outVotelogFilename));
	}
	catch (Exception e) {
	    outVotelogFile = System.out;
	}

	return of;
    }

    private HashMap<String,String> buildClickerMap(String dataFilename) {
	String path = "";
	String filename = "RemoteID.csv";
	Scanner userIDs;
	userIDs = openScanner(filename);
	if (userIDs == null) {
	    int lastSlash = dataFilename.lastIndexOf(dirSlashChar);
	    if (lastSlash != -1) {
		path = dataFilename.substring(0,lastSlash+1);
		userIDs = openScanner(path+filename);
	    }
	}
	HashMap<String,String> map =  new HashMap<String,String>();
	senatorMap = new HashMap<String,ArrayList<String>>();

	// fill map
	if (userIDs != null) {
	    try {
		while (userIDs.hasNextLine()) {
		    // "clickerID","senatorName"
		    String entryLine = userIDs.nextLine();
		    System.err.println("entryLine:"+entryLine);
		    if (entryLine.startsWith("\"#Clicker") ||
			entryLine.startsWith("#Clicker"))
			continue; // skip header

		    // Non-header data now
		    String senator, clickerTag;
		    if (entryLine.startsWith("\"#")) {
			// System.out.println("Quoted entries");
			// Have double quotes...
			//	System.err.println("userID line:("+entryLine+")");

			String[] iduser = entryLine.split(",");
			//		System.err.println("iduser[0]:("+iduser[0]+")");
			//		System.err.println("iduser[1]:("+iduser[1]+")");
		
			String[] idParts = iduser[0].split("\"");
			clickerTag = idParts[1];
			//		System.err.println("idparts[0]:("+idParts[0]+")");
			//		System.err.println("idparts[1]:("+idParts[1]+")");
			String[] userParts = iduser[1].split("\"");
			//		System.err.println("userparts[0]:("+userParts[0]+")");
			//		System.err.println("userparts[1]:("+userParts[1]+")");
			senator = "\"" + userParts[1].replace("-", ", ") + "\"";
		    }
		    else {
			//System.out.println("Unquoted entries");
			// No double quotes (sometimes Excel strips the quotes??..)
			String[] iduser = entryLine.split(",");
			clickerTag = iduser[0];
			senator = iduser[1].replace("-", ", ");
		    }
		    map.put(clickerTag, senator);
		    senatorMap.put(senator, new ArrayList<String>());
		}
	    }
	    catch (Exception e) {
		System.err.println(">>>> Error parsing the RemoteID.csv file.");
		System.err.println(">>>> Ensure format of each line in file is like:");
		System.err.println(">>>> \"#123ABC4D\",\"Lastname-Firstname (UNIT)\"");
		System.err.println(">>>> Use a 'text editor' to double check, not Excel.");
		System.exit(1);
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
	int lastSlash = filename.lastIndexOf(dirSlashChar);
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

    private void printSummaryCSV() {

	/* ignore date session time pollname senator clickerid vote */
	outFile.println("Ignore,Date,Session,Time,Poll,Senator,ClickerID,Vote");

	String date = convertToDate(filename);

	Element sessionElement = document.getRootElement();

	List<Element> pollingList = sessionElement.getChildren();
	for (int temp = 0; temp < pollingList.size(); temp++) {    
	    Element poll = pollingList.get(temp);

	    List<Element> voteList = poll.getChildren();
	    for (int vIndex = 0; vIndex < voteList.size(); vIndex++) {
		Element vote = voteList.get(vIndex);

		/* date session time pollname senator clickerid vote */
		String qIdx = poll.getAttribute("idx").getValue();
		boolean ignored=false;
		for (int i=0; i<qIgnoreList.length; i++) {
		    if (qIdx.equals(qIgnoreList[i])) {
			outFile.print("*");
			ignored=true;
		    }
		}
		if (! ignored) outFile.print(" ");
		outFile.print("," + date);
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
	outFile.close();
    }

    private void printVotelogCSV() {
	Element sessionElement = document.getRootElement();
	List<Element> pollingList = sessionElement.getChildren();
	int numPolls = pollingList.size();

	for (int pollNum = 0; pollNum < pollingList.size(); pollNum++) {    
	    Element poll = pollingList.get(pollNum);
	    List<Element> voteList = poll.getChildren();
	    for (int vIndex = 0; vIndex < voteList.size(); vIndex++) {
		Element vote = voteList.get(vIndex);

		String clickerID = vote.getAttribute("id").getValue();
		String senator = getClickerUser(clickerID);
		String mappedVote = mapVote(vote.getAttribute("ans").getValue());
		ArrayList<String> votes = senatorMap.get(senator);
		if (votes != null) 
		    votes.add(mappedVote);
		else System.err.println("ERROR: Senator "+senator+" votelist null.");
	    }
	}

	/* senator vote1 vote2 vote3 ... voteN */
	outVotelogFile.print("Senator");
	int pIdx=1;
	int numNotIgnored = numPolls - qIgnoreList.length;
	for ( ; pIdx <= numNotIgnored; pIdx++) {
	    outVotelogFile.print(", "+pIdx);
	}
	outVotelogFile.println();

	String[] senatorKeys = senatorMap.keySet().toArray(new String[1]);
	Arrays.sort(senatorKeys);
	for (int senIdx=0; senIdx < senatorKeys.length; senIdx++) {
	    /* senator vote1 vote2 vote3 ... voteN */
	    String senator = senatorKeys[senIdx];
	    outVotelogFile.print(senator);
	    ArrayList<String> votes = senatorMap.get(senator);
	    if (votes.size() > 0) {
		int vIdx=1;
		String voteStr="";
		for ( ; vIdx < votes.size(); vIdx++) {
		    boolean ignored=false;
		    for (int i=0; i<qIgnoreList.length; i++) {
			if (vIdx == Integer.parseInt(qIgnoreList[i])) {
			    ignored=true;
			    break;
			}
		    }
		    if (! ignored) {
			voteStr = votes.get(vIdx-1);
			if ("NoVoteRecorded".equals(voteStr))
			    voteStr = " ";
			outVotelogFile.print(", "+voteStr);
		    }
		}
		boolean ignored=false;
		for (int i=0; i<qIgnoreList.length; i++) {
		    if (vIdx == Integer.parseInt(qIgnoreList[i])) {
			ignored=true;
			break;
		    }
		}
		if (! ignored) {
		    voteStr = votes.get(vIdx-1);
		    if ("NoVoteRecorded".equals(voteStr))
			voteStr = " ";
		    outVotelogFile.println(", "+voteStr);
		}
		else
		    outVotelogFile.println();
		    //		voteStr = votes.get(vIdx-1);
		    //		outVotelogFile.println(","+voteStr);
	    }
	    else {
		// senator didn't vote at all....
		outVotelogFile.println(",U,N,E,X,C,U,S,E,D");
	    }
	}
	outVotelogFile.close();
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
