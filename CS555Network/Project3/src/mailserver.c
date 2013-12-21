/*
 * mailserver.c
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

/*-------------------------------------------------------------------*
 * Modified from project 1
 *------------------------------------------------------------------*/
#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <time.h>
#include <errno.h>
#include <signal.h>

#include <stdlib.h>
#include <unistd.h>

#include <pthread.h>

#include "../include/mailutils.h"
#include "../include/clientList.h"
#include "../include/messageList.h"

/*--------------------------------------------------------------------*/
#define DELAY_IN_SEC 15

extern char * recvtext(int sd);
extern int sendtext(int sd, char *msg);
extern int startMailServer();

void sendEmail();
void onAlarm(int signum);
void closeConnect(int sock, fd_set livesdset, int livesdmax);
void listClient(int sock);

void *threadSendEmail(void *arg);
/*--------------------------------------------------------------------*/

int numLiveClient = 0; /* number of live client */
LiveClientList *liveClientList = NULL; /* keep a list of live clients*/

int numMessage = 0; /* number of store messages */
MessageList *messageList = NULL; /* keep a list of message */

main(int argc, char *argv[]) {
	int serversd; /* server socket descriptor */
	char *clientIP; /* ip for each client connect */

	fd_set livesdset; /* set of live client sockets */
	int livesdmax; /* largest live client socket descriptor */
	fd_set livesdsetTmp; /* reset fd_set each select */

	/* check usage */
	if (argc != 1) {
		fprintf(stderr, "usage : %s\n", argv[0]);
		exit(1);
	}

	/* get ready to receive requests */
	serversd = startMailServer();
	if (serversd == -1) {
		exit(1);
	}

	FD_ZERO(&livesdset);
	FD_SET(serversd, &livesdset); /* Add server sd to livesdset */
	livesdmax = serversd;

	/* schedule task via alarm() */
/*	struct sigaction act;
	sigset_t block_mask;
	act.sa_handler = &onAlarm;
	sigemptyset(block_mask);
	sigaddset(&block_mask, )
	act.sa_mask = 0;
	act.sa_flags = SA_RESTART;
	sigaction(SIGALRM, &act, NULL);

	alarm(DELAY_IN_SEC);  // init alarm */

	pthread_t tid;
	int ret = pthread_create(&tid, NULL, &threadSendEmail, NULL);
	if( ret != 0) {
		fprintf(stderr, "create pthread error\n");
		exit(1);
	}

	/* receive requests and process them */
	while (1) {
		int sock; /* loop variable */

		livesdsetTmp = livesdset;
		if (select(livesdmax + 1, &livesdsetTmp, NULL, NULL, NULL) < 0)
			fprintf(stderr, "select error!\n");

		/* look for messages from live clients */
		for (sock = 3; sock <= livesdmax; sock++) {
			/* skip the listen socket */
			/* this case is covered separately */
			if (sock == serversd)
				continue;

			if (FD_ISSET(sock, &livesdsetTmp) /* message from client 'sock'? */) {
				char * clientHost; /* host name of the client */
				ushort clientPort; /* port number of the client */

				struct sockaddr_in sa_clientAddr; /* host address */
				socklen_t len = sizeof(sa_clientAddr); /* socket address length */
				struct hostent *hptr;

				/* figure out client's host name and port */
				if (getpeername(sock, (struct sockaddr*) &sa_clientAddr, &len) < 0) {
					fprintf(stderr, "getpeername error\n");
				}

				if ((hptr = gethostbyaddr((const char *) &sa_clientAddr.sin_addr,
						sizeof(sa_clientAddr.sin_addr),
						AF_INET)) == NULL)
					fprintf(stderr, "gethostbyaddr error\n");

				clientHost = hptr->h_name;
				clientPort = ntohs(sa_clientAddr.sin_port);

				/* read the message */
				char *msg = recvtext(sock);
				if (!msg) {
					/* disconnect from client */
					printf("mail server disconnect from '%s(%hu)'\n", clientHost, clientPort);

					/* remove this guy from the set of live clients */
					FD_CLR(sock, &livesdset);
					if (livesdmax == sock) { /* the largest sd is removed, track the second largest */
						int i, tempMax = 0;
						for (i = 0; i < livesdmax; i++) {
							if (FD_ISSET(i, &livesdset)) {
								if (i > tempMax)
									tempMax = i;
							}
						}
						livesdmax = tempMax;
					}

					numLiveClient--; /* decrease 1 */
					removeLiveClientBySock(liveClientList, sock);
					// traversalLiveClient(liveClientList);
					/* close the socket */
					close(sock);
				}
				else { /* valid msg */
					/* close cmd? */
					if(strcmp(msg, "close\n") == 0 ) {
						/* disconnect from client */
						printf("mail server disconnect from '%s(%hu)'\n", clientHost, clientPort);

						/* remove this guy from the set of live clients */
						FD_CLR(sock, &livesdset);
						if (livesdmax == sock) { /* the largest sd is removed, track the second largest */
							int i, tempMax = 0;
							for (i = 0; i < livesdmax; i++) {
								if (FD_ISSET(i, &livesdset)) {
									if (i > tempMax)
										tempMax = i;
								}
							}
							livesdmax = tempMax;
						}

						numLiveClient--; /* decrease 1 */
						removeLiveClientBySock(liveClientList, sock);
						// traversalLiveClient(liveClientList);
						/* close the socket */
						close(sock);
					}

					/* list client? */
					else if(strcmp(msg, "list\n") == 0 || strcmp(msg, "LIST\n") == 0)
						listClient(sock);

					/* msg contains @? */
					else if (strchr(msg, '@') == NULL) { /* user name message */
						char *userName = msg;
						if (numLiveClient == 1) { /* first live client */
							liveClientList = createLiveClient(sock, userName, clientIP);
						} else {
							insertLiveClient(liveClientList, sock, userName, clientIP);
						}
						clientIP = NULL; /* reset client IP for next new client */
						// traversalLiveClient(liveClientList);
					} else { /* email message */
						char *userName;
						char *IP;
						char emailMsg[256] = "";
						char *token;

						/* parse user name */
						token = strtok(msg, "@ ");
						userName = token;

						/* parse IP */
						token = strtok(NULL, "@ ");
						IP = token;

						/* parse email msg */
						token = strtok(NULL, "@ ");
						while (token != NULL) {
							strcat(emailMsg, token);
							strcat(emailMsg, " ");
							token = strtok(NULL, "@ ");
						}

						if (numMessage == 0) {
							messageList = createMsgList(userName, IP, emailMsg);
						} else {
							insertMsg(messageList, userName, IP, emailMsg);
						}
						// traversalMsgList(messageList);
						numMessage++;
					}

					/* free the message */
					free(msg);
				}
			}
		}

		/* look for connect requests */
		if (FD_ISSET(serversd, &livesdsetTmp) /* connect request from a new client */) {

			struct sockaddr_in sa_clientAddr; /* host address */
			socklen_t len = sizeof(sa_clientAddr); /* socket address length */
			struct hostent *hptr;

			char *welcome = "Weclcome to Maofei Chen's Email Server, running on port 5508\n";

			int csd = accept(serversd, (struct sockaddr*) &sa_clientAddr, &len);
			/* if accept is fine? */
			if (csd != -1) {
				char * clientHost; /* host name of the client */
				ushort clientPort; /* port number of the client */

				sendtext(csd, welcome);

				clientIP = inet_ntoa(sa_clientAddr.sin_addr);
				printf("The client IP is %s\n", clientIP );
				printf("The accept IP is:%s\n", inet_ntoa(sa_clientAddr.sin_addr));
				printf("The accept port number is:%d\n", ntohs(sa_clientAddr.sin_port));

				/* figure out client's host name and port */
				if ((hptr = gethostbyaddr((const char*) &sa_clientAddr.sin_addr,
						sizeof(struct in_addr),
						AF_INET)) == NULL)
					fprintf(stderr, "in connect phase, gethostbyaddr error\n");

				clientHost = hptr->h_name;
				clientPort = ntohs(sa_clientAddr.sin_port);

				printf("mail server connect from '%s' at '%hu'\n", clientHost, clientPort);

				/* add this guy to set of live clients */
				FD_SET(csd, &livesdset);
				if (livesdmax < csd) /* livesdmax always the largest sd in livesdset */
					livesdmax = csd;

				numLiveClient++; /* increase by 1*/
			} else {
				perror("accept");
				exit(0);
			}
		}
	}
}

/**
 * scan all msg, if user is connected, send msg
 */
void sendEmail() {
	if(messageList != NULL) {
		MessageList *currMsg = messageList->next;

		while (currMsg != NULL) {
			LiveClientList *currClient = liveClientList->next;
			while (currClient != NULL) {
				if (strcmp(currMsg->userName, currClient->userName) == 0
						&& strcmp(currMsg->IP, currClient->clientIP) == 0) { /* a match */
					// printf("The receiver is a live client\n");
					sendtext(currClient->sd, currMsg->msg);
					/* remove send msg, back to its previous */
					currMsg = removeMsg(messageList, currMsg->userName);
				}
				currClient = currClient->next;
			}
			currMsg = currMsg->next;
		}
	}
}

/**
 * schedule task for a time interval
 */
void onAlarm(int signum) {
	/* scan all msg, if user is live client, send it */
	sendEmail();
	alarm(DELAY_IN_SEC);
}


void *threadSendEmail(void *arg) {
	while(1) {
		sleep(DELAY_IN_SEC);
		sendEmail();
	}
	return 0;
}

void closeConnect(int sock, fd_set livesdset, int livesdmax) {

}

/**
 * list current live clients
 */
void listClient(int sock ) {
	char buf[512];
	LiveClientList *currClient = liveClientList->next;
	sendtext(sock, "The current live clients are:\n");
	while (currClient != NULL) {
		sprintf(buf, "user name: %s; IP: %s\n", currClient->userName, currClient->clientIP);
		sendtext(sock, buf);
		currClient = currClient->next;
	}
}
