#.c.o:
#	gcc -g -c $?
#	
#
#all:	mailserver mailclient
#
#mailserver:	./src/mailserver.o ./src/mailutils.o ./src/clientList.o
#	gcc -g -o ./bin/mailserver mailserver.o mailutils.o clientList.o
#	
#mailclient:	mailclient.o mailutils.o clientList.o
#	gcc -g -o ./bin/mailclient mailutils.o clientList.o
#	
#mailserver.o:	./src/mailutils.o ./src/clientList.o
#	gcc -g -c ./src/mailserver.c
#	
#mailclient.o:	./src/mailutils.o ./src/clientList.o
#	gcc -g -c ./src/mailclient.c
#
#mailutils.o:	./src/mailutils.c ./include/mailutils.h
#	gcc -g -c ./src/mailutils.c
#	
#clientList.o:	./src/clientList.c ./include/clientList.h
#	gcc -g -c ./src/clientList.c
		
all: 
	gcc -g -pthread ./src/mailutils.c ./include/mailutils.h \
		./src/clientList.c ./include/clientList.h \
		./src/messageList.c ./include/messageList.h \
		./src/mailserver.c -o ./bin/mailserver
	gcc -g ./src/mailutils.c ./include/mailutils.h \
		./src/clientList.c ./include/clientList.h \
		./src/messageList.c ./include/messageList.h \
		./src/mailclient.c -o ./bin/mailclient
		
#	gcc -g ./src/clientList.c ./include/clientList.h \
#		./src/testClientList.c -o ./bin/testCientList
		
#	gcc -g ./src/messageList.c ./include/messageList.h \
#		./src/testMessageList.c -o ./bin/testMessageList