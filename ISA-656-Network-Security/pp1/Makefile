CLASSPATH = /usr/lib/java/swt.jar:.
JAVAPATH  = /usr/lib/jni:.
JAVAC     = /usr/pubsw/apps/jdk-6/bin/javac -classpath $(CLASSPATH) -g
JAVA      = /usr/pubsw/apps/jdk-6/bin/java  -classpath $(CLASSPATH) -Djava.library.path=$(JAVAPATH)

test:
	$(JAVAC) pwman/Test.java

all: test
	$(JAVAC) pwman/NetworkedMapServer.java
	$(JAVAC) pwman/Client.java

proj1.pdf: proj1.tex
	pdflatex proj1
	rm -f proj1.aux proj1.dvi proj1.ps proj1.log

run-client::
	$(JAVA) pwman.Client

run-test::
	$(JAVA) pwman.Test

run-server::
	$(JAVA) pwman.NetworkedMapServer

doc::
	mkdir doc
	javadoc -d doc pwman

clean::
	find . -name '*.class' -delete
	find . -name '*~' -delete
	find . -name '#*#' -delete
	find . -name '*.core' -delete
	find . -name 'core' -delete
	rm -fr doc test net_test proj1.pdf
