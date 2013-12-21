/*
 * mailutils.h
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

#ifndef MAILUTILS_H_
#define MAILUTILS_H_

int startMailServer();
int connMailServer(char *serverHost, int SERVER_PORT);
int readn(int sockdescriptor, char *buf, int n);
char *recvtext(int sockdescriptor);
int sendtext(int sd, char *msg);

#endif /* COMMON_H_ */
