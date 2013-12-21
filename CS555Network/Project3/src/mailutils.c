/*
 * mailutils.c
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

/*-------------------------------------------------------------------*
 * Modified from project 1
 *------------------------------------------------------------------*/

#include <stdio.h>
#include <stdlib.h>
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

#include "../include/mailutils.h"

#define MAXNAMELEN 256
#define SERVER_PORT 5508

/*----------------------------------------------------------------*/

/*
 * prepare server to accept requests
 * returns file descriptor of socket
 * returns -1 on error
 */
int startMailServer() {
	int serversd; /* socket descriptor */
	char *serverHost; /* full name of this host */
	char serverHost_Arry[256];

	struct sockaddr_in serverAddr; /* server address */
	socklen_t len; /* socket address length */
	struct hostent *hptr;

	serversd = socket(AF_INET, SOCK_STREAM, 0); /* IPv4, TCP */

	bzero(&serverAddr, sizeof(serverAddr));
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
	serverAddr.sin_port = htons(SERVER_PORT);

	bind(serversd, (struct sockaddr *) &serverAddr, sizeof(serverAddr));

	/* we are ready to receive connections */
	listen(serversd, 10);

	/* figure out the full local host name (servhost) */
	if (gethostname(serverHost_Arry, sizeof(serverHost_Arry)) == -1)
		fprintf(stderr, "gethostname error\n");
	else
		// printf("gethostname successful\n");
	if ((hptr = gethostbyname(serverHost_Arry)) == NULL)
		fprintf(stderr, "gethostbyname error\n");
	serverHost = hptr->h_name;

	/* ready to accept requests */
	printf("Welcome to Maofei Chen's Email Server on '%s' at '%hu'\n", serverHost, SERVER_PORT);

	return (serversd);
}

/*
 * establishes connection with the server
 * returns file descriptor of socket
 * returns -1 on error
 */
int connectMailServer(char *serverHost, ushort serverPort) {
	int clientsd; /* socket descriptor */
	ushort clientPort; /* port assigned to this client */
	struct sockaddr_in clientHost;

	struct hostent *hptr;
	struct sockaddr_in serverAddr;

	if ((clientsd = socket(AF_INET, SOCK_STREAM, 0)) < 0) /* IPv4, TCP */
		fprintf(stderr, "socket call error\n");

	/* connect to the server on 'servhost' at 'servport' */
	if ((hptr = gethostbyname(serverHost)) == NULL)
		fprintf(stderr, "gethostbyname error\n");

	bzero(&serverAddr, sizeof(serverAddr));
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_port = htons(serverPort);
	memcpy(&serverAddr.sin_addr, hptr->h_addr_list[0], hptr->h_length);

	if (connect(clientsd, (struct sockaddr*) &serverAddr, sizeof(struct sockaddr_in)) < 0)
		fprintf(stderr, "connect to mail server fail\n");

	/* figure out the port assigned to this client */
	socklen_t len;
	len = sizeof(clientHost);
	if (getsockname(clientsd, (struct sockaddr*) &clientHost, &len) < 0)
		fprintf(stderr, "getsockname call error");
	clientPort = ntohs(clientHost.sin_port);

	/* succesful. return socket descriptor */
	printf("connected to mail server on '%s' at '%hu' thru '%hu'\n",
			serverHost,
			serverPort,
			clientPort);

	return (clientsd);
}

/**
 * convert ip v4 address to host name
 */
char *cvertIPToHost(char *IP) {

	struct hostent *hptr;
	struct in_addr serverAddr;
	char *serverHost;

	if(IP != NULL ){
		inet_pton(AF_INET, IP, &serverAddr);
		if ((hptr = gethostbyaddr(&serverAddr, sizeof(serverAddr), AF_INET)) < 0) {
			fprintf(stderr, "gethostbyaddr fails\n");
			exit(1);
		}
	}
	serverHost = hptr->h_name;

	return serverHost;
}

int readn(int sd, char *buf, int n) {
	int toberead;
	char * ptr;

	toberead = n;
	ptr = buf;
	while (toberead > 0) {
		int byteread;

		byteread = read(sd, ptr, toberead);
		if (byteread <= 0) {
			if (byteread == -1)
				perror("read");
			return (0);
		}

		toberead -= byteread;
		ptr += byteread;
	}
	return (1);
}

char *recvtext(int sd) {
	char *msg;
	long len;

	/* read the message length */
	if (!readn(sd, (char *) &len, sizeof(len))) {
		return (NULL);
	}
	len = ntohl(len);

	/* allocate space for message text */
	msg = NULL;
	if (len > 0) {
		msg = (char *) malloc(len);
		if (!msg) {
			fprintf(stderr, "error : unable to malloc\n");
			return (NULL);
		}

		/* read the message text */
		if (!readn(sd, msg, len)) {
			free(msg);
			return (NULL);
		}
	}

	/* done reading */
	return (msg);
}

int sendtext(int sd, char *msg) {
	long len;

	/* write lent */
	len = (msg ? strlen(msg) + 1 : 0);
	len = htonl(len);
	write(sd, (char *) &len, sizeof(len));

	/* write message text */
	len = ntohl(len);
	if (len > 0)
		write(sd, msg, len);
	return (1);
}


