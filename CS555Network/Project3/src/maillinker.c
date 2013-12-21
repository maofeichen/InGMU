/*
 * maillinker.c
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 *
 */

/* From code of project 2 */
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <strings.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <time.h>
#include <errno.h>

#include <stdlib.h>
#include <unistd.h>

#include "../include/common.h"

/*-------------------------------------------------------------------*/
/**
 * From code of project 2
 * start mail server
 * return file descriptor of socket
 * return -1 if error
 */
int startMailServer() {
	int serversd; /* mail server socket descriptor */
	int SERVER_PORT = 5508;
	char *serverHost;

	char arry_serverHost[32];
	struct sockaddr_in serverAddr;
	socklen_t serverAddLen;
	struct hostent *hptr;

	if ( (serversd = socket(AF_INET, SOCK_STREAM, 0) < 0) ) { /* IPv4, TCP */
		printf("Create TCP socket error\n");
		return (-1);
	}

	bzero(&serverAddr, sizeof(serverAddr) );
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddr.sin_port = htons(SERVER_PORT );
	bind(serversd, (struct sockaddr *) &serverAddr, sizeof(serverAddr) );

	listen(serversd, 5);

	/* get full local host name */
	if( gethostname(arry_serverHost, sizeof(arry_serverHost) ) < 0)
		fprintf(stderr, "gethostname error\n");
	if( (hptr = gethostbyname(arry_serverHost) ) == NULL )
		fprintf(stderr, "gethostbyname error\n");
	serverHost = hptr->h_name;

	/* Ready to accept requests */
	printf("Welcome to Maofei Chen's Email server, running on '%s' at '%d'\n ",
			serverHost, SERVER_PORT);
	return (serversd);
}

/**
 * From code of project 2
 * connect with the mail server
 * return file descriptor of socket
 * return -1 if error
 */
int connMailServer(char *serverHost, int SERVER_PORT) {
	int clientsd; /* client socket descriptor */
	int clientPort;
	struct sockaddr_in clientAddr;

	struct sockaddr_in serverAddr;
	struct hostent *hptr;

	if( (clientsd = socket(AF_INET, SOCK_STREAM, 0 ) ) < 0 ) {
		fprintf(stderr, "Create socket error\n");
		return (-1);
	}

	if( (hptr = gethostbyname(serverHost) ) == NULL)
		fprintf(stderr, "gethostbyname error\n");

	bzero(&serverAddr, sizeof(serverAddr) );
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_port = htons(SERVER_PORT);
	memcpy(&serverAddr.sin_addr, hptr->h_addr_list[0], hptr->h_length );

	if(connect(clientsd, (struct sockaddr*)&serverAddr, sizeof(struct sockaddr_in) ) < 0 )
		fprintf(stderr, "Connect to mail server fail\n");

	socklen_t len = sizeof(clientAddr);
	if(getsockname(clientsd, (struct sockaddr*)&clientAddr, &len ) < 0 )
		fprintf(stderr, "getsockname fail\n");
	clientPort = ntohs(clientAddr.sin_port);

	printf("Connected to mail server on '%s' at '%hu' thru '%hu'\n",
			serverHost, SERVER_PORT, clientPort );

	return clientsd;
}

/**
 * From code of project 1
 */
int readn(int sockdescriptor, char *buf, int n)
{
	int     toberead;
	char *  ptr;

	toberead = n;
	ptr = buf;
	while (toberead > 0) {
		int byteread;

		byteread = read(sockdescriptor, ptr, toberead);
		if (byteread <= 0) {
			if (byteread == -1)
				perror("read");
			return(0);
		}

		toberead -= byteread;
		ptr += byteread;
	}
	return(1);
}

/**
 * From code of project 1
 */
char *recvtext(int sockdescriptor)
{
	char *msg;
	long  len;

	/* read the message length */
	if (!readn(sockdescriptor, (char *) &len, sizeof(len))) {
		return(NULL);
	}
	len = ntohl(len);

	/* allocate space for message text */
	msg = NULL;
	if (len > 0) {
		msg = (char *) malloc(len);
		if (!msg) {
			fprintf(stderr, "error : unable to malloc\n");
			return(NULL);
		}

		/* read the message text */
		if (!readn(sockdescriptor, msg, len)) {
			free(msg);
			return(NULL);
		}
	}

	/* done reading */
	return(msg);
}

/**
 * From code of project 1
 */
int sendtext(int sd, char *msg)
{
	long len;

	/* write lent */
	len = (msg ? strlen(msg) + 1 : 0);
	len = htonl(len);
	write(sd, (char *) &len, sizeof(len));

	/* write message text */
	len = ntohl(len);
	if (len > 0)
		write(sd, msg, len);
	return(1);
}
