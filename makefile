
default:
	@echo "Available targets: demo extract clean"

demo: JDomParserDemo.class
	java -cp .:jdom.jar JDomParserDemo

JDomParserDemo.class:
	javac -cp .:jdom.jar JDomParserDemo.java

SessionDataExtractor.class:
	javac -cp .:jdom.jar SessionDataExtractor.java

extract: SessionDataExtractor.class
	java -cp .:jdom.jar SessionDataExtractor inputs/L1709190946.xml

clean:
	rm *.class

