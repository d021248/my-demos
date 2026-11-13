.PHONY: compile test coverage spotbugs clean all

compile:
	mvn compile

test:
	mvn test

coverage:
	mvn test jacoco:report

spotbugs:
	mvn spotbugs:check

clean:
	mvn clean

all:
	mvn clean verify
