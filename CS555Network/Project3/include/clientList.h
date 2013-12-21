/*
 * clientList.h
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

#ifndef CLIENTLIST_H_
#define CLIENTLIST_H_

/* structure of live client info */
typedef struct _liveClient {
	int sd; /* socket descriptor */
	char *userName; /* user name */
	char *clientIP; /* user IP */

	struct _liveClient * prev; /* previous live client */
	struct _liveClient * next; /* next live client */
} LiveClientList;

LiveClientList *createLiveClient(int sock, char *userNmae, char *clientIP);
LiveClientList *findLiveClientByName(LiveClientList *liveclientlist, char *userName);
LiveClientList *findLiveClientBySock(LiveClientList *liveclientlist, int sock);
void insertLiveClient(LiveClientList *liveclientlist, int sock, char *userName, char *clientIP);
LiveClientList *removeLiveClientBySock(LiveClientList *liveclientlist, int sock);
void traversalLiveClient(LiveClientList *liveclientlist);

#endif /* CLIENTLIST_H_ */
