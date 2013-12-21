/*
 * messageList.c
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../include/messageList.h"

MessageList *createMsgList(char *userName, char *IP, char *msg) {
	MessageList *head = malloc(sizeof(struct _message) );
	head->userName = "";
	head->IP = "";
	head->msg = "";
	head->prev = NULL;
	head->next = NULL;

	MessageList *firstMsg = malloc(sizeof(struct _message) );
	firstMsg->userName = strdup(userName);
	firstMsg->IP = strdup(IP);
	firstMsg->msg = strdup(msg);
	head->next = firstMsg;
	firstMsg->prev = head;
	firstMsg->next = NULL;

	return head;
}

MessageList *findMsgByName(MessageList *msgList, char *userName) {
	MessageList *currMsg = msgList;
	while ( (strcmp(currMsg->userName, userName) != 0) && currMsg != NULL)
		currMsg = currMsg->next;

	return currMsg;
}

MessageList *findMsgByIP(MessageList *msgList, char *IP) {
	return NULL;
}

void insertMsg(MessageList *msgList, char *userName, char *IP, char *msg) {
	MessageList *currMsg = msgList;
	while ( currMsg->next != NULL)
		currMsg = currMsg->next;

	MessageList *newMsg = malloc(sizeof(struct _message ) );
	if (newMsg == NULL)
		printf("malloc new message fali\n");
	else {
		newMsg->userName = strdup(userName);
		newMsg->IP = strdup(IP);
		newMsg->msg = strdup(msg);

		currMsg->next = newMsg;
		newMsg->prev = currMsg;
		newMsg->next = NULL;
	}

}

MessageList *removeMsg(MessageList *msgList, char *userName) {
	MessageList *msgRemoved = findMsgByName(msgList, userName);
	MessageList *prevMsg;

	if(msgRemoved == NULL) {
		printf("Not found the message to be removed\n");
	}
	else {
		if(msgRemoved->next == NULL) /* at tail */
			(msgRemoved->prev)->next = NULL;
		else {
			/* bypass the msg to be removed */
			(msgRemoved->prev)->next = msgRemoved->next;
			(msgRemoved->next)->prev = msgRemoved->prev;
		}
		prevMsg = msgRemoved->prev;
		free(msgRemoved);
	}
	return prevMsg;
}

void traversalMsgList(MessageList *msgList) {
	int count = 0;
	MessageList *currMsg = msgList;
	while(currMsg != NULL) {
		printf("This %d th message: \n", ++count);
		printf("The user name, IP, email message is: %s %s %s\n",
				currMsg->userName,
				currMsg->IP,
				currMsg->msg);
		currMsg = currMsg->next;
	}
}

