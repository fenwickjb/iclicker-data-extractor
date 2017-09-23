
default:
	@echo "Available targets: demo extract clean mkjar runjar"

demo: JDomParserDemo.class
	java -cp .:jdom.jar JDomParserDemo

JDomParserDemo.class:
	javac -cp .:jdom.jar JDomParserDemo.java

SessionDataExtractor.class: SessionDataExtractor.java
	javac -cp .:jdom.jar SessionDataExtractor.java

extract: SessionDataExtractor.class
	java -cp .:jdom.jar SessionDataExtractor inputs/L1709190946.xml

manifest.txt:
	echo Main-Class: SessionDataExtractor > manifest.txt
	echo Class-Path: jdom.jar >> manifest.txt

mkjar: SessionDataExtractor.class jdom.jar manifest.txt
	jar cfm SessionDataExtractor.jar manifest.txt SessionDataExtractor.class 

runjar: SessionDataExtractor.jar jdom.jar
	java -jar SessionDataExtractor.jar inputs/L1709190946.xml

clean:
	rm -f *.class
	rm -f *.CSV
	rm -f *~
	rm SessionDataExtractor.jar
