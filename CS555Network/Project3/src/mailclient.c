/*
 * mailclient.c
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
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <time.h>
#include <errno.h>

#include <sys/time.h>

#include "../include/mailutils.h"

#define MAXMSGLEN 1024

extern char * recvtext(int sd);
extern int sendtext(int sd, char *msg);
extern int connectMailServer(char *servhost, ushort servport);

/*--------------------------------------------------------------------*/
main(int argc, char *argv[]) {
	int clientsd;
	char *userName;
	char *serverHost = NULL;

	fd_set servOrkybrd;
	fd_set servOrkybrdTmp;

	/* check usage */
	if (argc != 4) {
		fprintf(stderr, "Arguments error\n");
		exit(1);
	}

	if(argv[2] != NULL) {
		serverHost = cvertIPToHost(argv[2]);
	}
	/* get hooked on to the server */
	clientsd = connectMailServer(serverHost, atoi(argv[3]));
	if (clientsd == -1)
		exit(1);

	/* send mail server user name */
	userName = argv[1];
	sendtext(clientsd, userName);

	FD_ZERO(&servOrkybrd);
	FD_SET(clientsd, &servOrkybrd);
	FD_SET(0, &servOrkybrd);

	/* keep talking */
	while (1) {
		servOrkybrdTmp = servOrkybrd;
		select(clientsd + 1, &servOrkybrdTmp, NULL, NULL, NULL);
		if (FD_ISSET(clientsd, &servOrkybrdTmp) /* message from server */) {
			char *msg;
			msg = recvtext(clientsd);
			if (!msg) {
				/* server killed, exit */
				fprintf(stderr, "server close connect\n");
				exit(1);
			}

			/* display the message */
			printf(">>> %s", msg);

			/* free the message */
			free(msg);
		}

		if (FD_ISSET(0, &servOrkybrdTmp) /* input from keyboard */) {
			char msg[MAXMSGLEN];

			if (!fgets(msg, MAXMSGLEN, stdin))
				exit(0);
			sendtext(clientsd, msg);
		}
	}
}
