
default:
	@echo "Available targets: demo extract clean mkjar runjar mkguijar rungui"
	@echo "Try: clean mkjar mkguijar rungui"

demo: JDomParserDemo.class
	java -cp .:jdom.jar JDomParserDemo

JDomParserDemo.class:
	javac -cp .:jdom.jar JDomParserDemo.java

SessionDataExtractor.class: SessionDataExtractor.java
	javac -cp .:jdom.jar SessionDataExtractor.java

GUI.class: GUI.java
	javac GUI.java

extract: SessionDataExtractor.class
	java -cp .:jdom.jar SessionDataExtractor inputs/L1709190946.xml

manifest.txt:
	echo Main-Class: SessionDataExtractor > manifest.txt
	echo Class-Path: jdom.jar >> manifest.txt

gui_manifest.txt:
	echo Main-Class: GUI > gui_manifest.txt

mkjar: SessionDataExtractor.class jdom.jar manifest.txt SessionDataExtractor.java GUI.java
	jar cfm SessionDataExtractor.jar manifest.txt SessionDataExtractor.class SessionDataExtractor.java jdom.jar GUI.java

mkguijar: GUI.class gui_manifest.txt GUI.java 
	jar cfm JaysFacSenVoteExtractor.jar gui_manifest.txt GUI.java GUI*.class

runjar: SessionDataExtractor.jar 
	java -jar SessionDataExtractor.jar inputs/L1709190946.xml

rungui: SessionDataExtractor.jar GUI.class
	java -jar JaysFacSenVoteExtractor.jar

clean:
	rm -f *.class
	rm -f *.CSV
	rm -f *~
	rm SessionDataExtractor.jar
	rm JaysFacSenVoteExtractor.jar
