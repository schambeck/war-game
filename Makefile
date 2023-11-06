dist:
	mvn clean compile package

run:
	java -jar shade/war.jar

dist-run: dist run
