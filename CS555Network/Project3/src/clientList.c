/*
 * clientList.c
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../include/clientList.h"

/* create live clients list */
LiveClientList *createLiveClient(int sock, char *userName, char *clientIP){
	LiveClientList *head = malloc(sizeof(struct _liveClient) );
	head->sd = -1;
	head->userName = "";
	head->prev = NULL;
	head->next = NULL;

	LiveClientList *firstClient = malloc(sizeof(struct _liveClient) );
	firstClient->sd = sock;
	firstClient->userName = strdup(userName);
	firstClient->clientIP = strdup(clientIP);
	head->next = firstClient;
	firstClient->prev = head;
	firstClient->next = NULL;

	return head;
};

/* find a client in the list given a user name */
LiveClientList *findLiveClientByName(LiveClientList *liveclientlist, char *userName) {
	LiveClientList *currClient = liveclientlist;
	while( (strcmp(currClient->userName, userName) != 0) && currClient != NULL )
		currClient = currClient->next;

	return currClient;
};

/* find a client in the list given a sock*/
LiveClientList *findLiveClientBySock(LiveClientList *liveclientlist, int sock) {
	LiveClientList *currClient = liveclientlist;
	while( ( currClient->sd != sock) && currClient != NULL )
		currClient = currClient->next;

	return currClient;
}

/* insert a new client in the list */
void insertLiveClient(LiveClientList *liveclientlist, int sock, char *userName, char *clientIP) {
	LiveClientList *currClient = liveclientlist;
	while(currClient->next != NULL) {
		currClient = currClient->next;
		printf("The last client user name is: %s\n", currClient->userName);
	}

	LiveClientList *newClient = malloc(sizeof(struct _liveClient ) );
	if(newClient == NULL)
		fprintf(stderr, "malloc new node fail\n");
	else{
		newClient->sd = sock;
		newClient->userName = strdup(userName);
		newClient->clientIP = strdup(clientIP);

		currClient->next = newClient;
		newClient->prev = currClient;
		newClient->next = NULL;
	}
}

/**
 *  remove a live client from list
 *  return previous client
 */
LiveClientList *removeLiveClientBySock(LiveClientList *liveclientlist, int sock){
	LiveClientList *clientRemoved = findLiveClientBySock(liveclientlist, sock);
	LiveClientList *prevClient;

	if(clientRemoved == NULL)
		printf("Not found the client to be removed\n");
	else{
		if(clientRemoved->next != NULL) { /* at middle */
		/* bypass client to be removed */
		(clientRemoved->prev)->next = clientRemoved->next;
		(clientRemoved->next)->prev = clientRemoved->prev;
		} else { /* at tail */
			(clientRemoved->prev)->next = NULL;
		}

		prevClient = clientRemoved->prev;
		free(clientRemoved);
	}
	return prevClient;
}

/* traversal the live client list */
void traversalLiveClient(LiveClientList *liveclientlist){
	int count = 0;
	LiveClientList *currClient = liveclientlist;
	while(currClient != NULL ){
		printf("The %d client\n", ++count);
		printf("The sd and user name and IP are: %d %s %s\n", currClient->sd,
				currClient->userName,
				currClient->clientIP);

		currClient = currClient->next;
	}
}

