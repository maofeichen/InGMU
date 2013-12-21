/*
 * common.h
 *
 * CS555 Project 3
 * Maofei Chen
 * G00709508
 */

#ifndef COMMON_H_
#define COMMON_H_

/*int SERVER_PORT = 5508;*/
extern int startMailServer();
extern int connMailServer(char *serverHost, int SERVER_PORT);
extern int readn(int sockdescriptor, char *buf, int n);
extern char *recvtext(int sockdescriptor);
extern int sendtext(int sd, char *msg);

#endif /* COMMON_H_ */
