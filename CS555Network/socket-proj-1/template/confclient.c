/*--------------------------------------------------------------------*/
/* conference client */

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

#include <sys/time.h>

#define MAXMSGLEN  1024

extern char *  recvtext(int sd);
extern int     sendtext(int sd, char *msg);

extern int     hooktoserver(char *servhost, ushort servport);
/*--------------------------------------------------------------------*/

/*--------------------------------------------------------------------*/
main(int argc, char *argv[])
{
	int  sock;

	fd_set servOrkybrd;
	fd_set servOrkybrd_cp;
	/* check usage */
	if (argc != 3) {
		fprintf(stderr, "usage : %s <servhost> <servport>\n", argv[0]);
		exit(1);
	}

	/* get hooked on to the server */
	sock = hooktoserver(argv[1], atoi(argv[2]));
	if (sock == -1)
		exit(1);
  
	FD_ZERO(&servOrkybrd);
	FD_SET(sock, &servOrkybrd);
	FD_SET(0, &servOrkybrd);

	/* keep talking */
	while (1) {
    
		/*
		  FILL HERE 
		  use select() to watch simulataneously for
		  inputs from user and messages from server
		*/
		servOrkybrd_cp = servOrkybrd;
		select(sock + 1, &servOrkybrd_cp, NULL, NULL, NULL);
		if (FD_ISSET(sock, &servOrkybrd_cp) /* FILL HERE: message from server? */) {
			char *msg;
			msg = recvtext(sock);
			if (!msg) {
				/* server killed, exit */
				fprintf(stderr, "error: server died\n");
				exit(1);
			}

			/* display the message */
			printf(">>> %s", msg);

			/* free the message */
			free(msg);
		}

		if (FD_ISSET(0, &servOrkybrd_cp) /* FILL HERE: input from keyboard? */) {
			char      msg[MAXMSGLEN];

			if (!fgets(msg, MAXMSGLEN, stdin))
				exit(0);
			sendtext(sock, msg);
		}
	}
}
/*--------------------------------------------------------------------*/
