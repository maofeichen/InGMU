/*
 * messageList.h
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

#ifndef MESSAGELIST_H_
#define MESSAGELIST_H_

/* struct of message list */
typedef struct _message {
	char *userName;
	char *IP;
	char *msg;

	struct _message *prev;
	struct _message *next;
} MessageList;

MessageList *createMsgList(char *userName, char *IP, char *msg);
MessageList *findMsgByName(MessageList *msgList, char *userName);
MessageList *findMsgByIP(MessageList *msgList, char *IP);
void insertMsg(MessageList *msgList, char *userName, char *IP, char *msg);
MessageList *removeMsg(MessageList *msgList, char *userName);
void traversalMsgList(MessageList *msgList);

#endif

