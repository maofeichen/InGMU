/*--------------------------------------------------------------------*/
/* conference server */

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

extern char *  recvtext(int sd);
extern int     sendtext(int sd, char *msg);

extern int     startserver();
/*--------------------------------------------------------------------*/

/*--------------------------------------------------------------------*/
/* main routine */
main(int argc, char *argv[])
{
	int    servsock;    /* server socket descriptor */

	fd_set livesdset;   /* set of live client sockets */
	int    livesdmax;   /* largest live client socket descriptor */

	fd_set livesdset_cp; 		  /* reset fd_set each select */
 
	/* check usage */
	if (argc != 1) {
		fprintf(stderr, "usage : %s\n", argv[0]);
		exit(1);
	}

	/* get ready to receive requests */
	servsock = startserver();
	if (servsock == -1) {
		exit(1);
	}
  
	/*
	  FILL HERE:
	  init the set of live clients
	*/
	FD_ZERO(&livesdset);
	FD_SET(servsock, &livesdset); /* Add server sd to livesdset */
	livesdmax = servsock;

	/* receive requests and process them */
	while (1) {
		int    frsock;      /* loop variable */
		/*
		  FILL HERE
		  wait using select() for
        messages from existing clients and
		  connect requests from new clients
		*/
		livesdset_cp = livesdset;
		if (select(livesdmax + 1, &livesdset_cp, NULL, NULL, NULL) < 0)
			printf ("select error!\n");

		/* look for messages from live clients */
		for (frsock=3; frsock <= livesdmax; frsock++) {
			/* skip the listen socket */
			/* this case is covered separately */
			if (frsock == servsock) continue;

			if ( FD_ISSET(frsock, &livesdset_cp) /* FILL HERE: message from client 'frsock'? */ ) {
				char *  clientaddr;  /* host name of the client */
				ushort  clientport;  /* port number of the client */

				struct sockaddr_in sa_clientaddr; /* host address */
				socklen_t len = sizeof(sa_clientaddr);						 /* socket address length */			
				struct hostent *hptr;
				/*
				  FILL HERE:
				  figure out client's host name and port
				  using getpeername() and gethostbyaddr()
				*/
				if(getpeername(frsock, (struct sockaddr*)&sa_clientaddr, &len) < 0){
					printf ("getpeername error\n");
					/* return(-1); */
				}

				if( (hptr = gethostbyaddr((const char *)&sa_clientaddr.sin_addr, 
												  sizeof(sa_clientaddr.sin_addr), AF_INET)) == NULL)
					printf ("gethostbyaddr error\n");
				clientaddr = hptr->h_name;
				clientport = ntohs(sa_clientaddr.sin_port);

				/* read the message */
				char *msg = recvtext(frsock);
				if (!msg) {
					/* disconnect from client */
					printf("admin: disconnect from '%s(%hu)'\n",
							 clientaddr, clientport);
					/*
					  FILL HERE:
					  remove this guy from the set of live clients
					*/
					FD_CLR(frsock, &livesdset);
					if(livesdmax == frsock){ /* the largest sd is removed, track the second largest */
						int i, tempMax = 0;
						for(i = 0; i < livesdmax; i++){
							if(FD_ISSET(i, &livesdset)){
								if(i > tempMax)
									tempMax = i;
							}
						}
						livesdmax = tempMax;
					}
					/* close the socket */
					close(frsock);
				} else {
					/*
					  FILL HERE
					  send the message to all live clients
					  except the one that sent the message
					*/
					int i;										 /* loop variable */
					for(i = 3; i <= livesdmax; i++){ /* Loop livemax */
						if(i != frsock && i != servsock) /* servsock is in livesdset, but cannot be it */
							if(FD_ISSET(i, &livesdset))
								sendtext(i, msg);
					}
					/* display the message */
					printf("%s(%hu): %s", clientaddr, clientport, msg);

					/* free the message */
					free(msg);
				}
			}
		}

		/* look for connect requests */
		if ( FD_ISSET(servsock, &livesdset_cp) /* FILL HERE: connect request from a new client? */ ) {

			/*
			  FILL HERE:
			  accept a new connection request
			*/
			struct sockaddr_in sa_clientaddr; /* host address */
			socklen_t len = sizeof(sa_clientaddr);						 /* socket address length */			
			struct hostent *hptr;

			int csd = accept(servsock, (struct sockaddr*)&sa_clientaddr, &len);
			/* if accept is fine? */
			if (csd != -1) {
				char *  clientaddr;  /* host name of the client */
				ushort  clientport;  /* port number of the client */

				printf("The accept IP is:%s\n", inet_ntoa(sa_clientaddr.sin_addr));
				printf ("The accept port number is:%d\n", ntohs(sa_clientaddr.sin_port));
				/*
				  FILL HERE:
				  figure out client's host name and port
				  using gethostbyaddr() and without using getpeername().
				*/
				if( (hptr = gethostbyaddr((const char*)&sa_clientaddr.sin_addr, 
												  sizeof(struct in_addr), AF_INET)) == NULL)
					printf ("in connect phase, gethostbyaddr error\n");
				clientaddr = hptr->h_name; /* already pointer */
				clientport = ntohs(sa_clientaddr.sin_port);

				printf("admin: connect from '%s' at '%hu'\n",
						 clientaddr, clientport);
				/*
				  FILL HERE:
				  add this guy to set of live clients
				*/
				FD_SET(csd, &livesdset);
				if(livesdmax < csd) /* livesdmax always the largest sd in livesdset */
					livesdmax = csd;
			} else {
				perror("accept");
				exit(0);
			}
		}
	}
}
/*--------------------------------------------------------------------*/
