/*--------------------------------------------------------------------*/
/* functions to connect clients and server */

#include <stdio.h>
#include <fcntl.h>
#include <string.h>
#include <strings.h>
#include <sys/types.h> 
#include <sys/socket.h> 
#include <netinet/in.h> 
#include <arpa/inet.h> 
#include <netdb.h>
#include <time.h> 
#include <errno.h>

#define MAXNAMELEN 256
/*--------------------------------------------------------------------*/


/*----------------------------------------------------------------*/
/* prepare server to accept requests
   returns file descriptor of socket
   returns -1 on error
*/
int startserver()
{
	int     sd;        /* socket descriptor */

	char *servhost;  /* full name of this host */
	char servhost_ary [32];
	ushort  servport;  /* port assigned to this server */

	struct sockaddr_in serveraddr; /* server address */
	socklen_t len;						 /* socket address length */
	struct hostent *hptr;
	/*
	  FILL HERE
	  create a TCP socket using socket()
	*/
	sd = socket(AF_INET, SOCK_STREAM, 0);  /* IPv4, TCP */

	/*
	  FILL HERE
	  bind the socket to some port using bind()
	  let the system choose a port
	*/
	bzero(&serveraddr, sizeof(serveraddr));
	serveraddr.sin_family = AF_INET;
	serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);
	serveraddr.sin_port = htons(0);

	bind(sd, (struct sockaddr *)&serveraddr, sizeof(serveraddr));

	/* we are ready to receive connections */
	listen(sd, 5);

	/*
	  FILL HERE
	  figure out the full local host name (servhost)
	  use gethostname() and gethostbyname()
	  full host name is zeus.ite.gmu.edu instead of just zeus
	*/
	if(gethostname(servhost_ary, sizeof(servhost_ary)) < 0)
		printf ("gethostname error\n");
	if((hptr = gethostbyname(servhost_ary)) == NULL)
		printf ("gethostbyname error\n");
	servhost = hptr->h_name;
	/*
	  FILL HERE
	  figure out the port assigned to this server (servport)
	  use getsockname()
	*/
	len = sizeof(serveraddr);
	if(getsockname(sd, (struct sockaddr *)&serveraddr, &len) < 0)
		printf("getsockname error \n");
	else
		servport = ntohs(serveraddr.sin_port);
	/* ready to accept requests */
	printf("admin: started server on '%s' at '%hu'\n",
			 servhost, servport);
	return(sd);
}
/*----------------------------------------------------------------*/

/*----------------------------------------------------------------*/
/*
  establishes connection with the server
  returns file descriptor of socket
  returns -1 on error
*/
int hooktoserver(char *servhost, ushort servport)
{
	int     sd;          /* socket descriptor */

	ushort  clientport;  /* port assigned to this client */

	struct hostent *hptr;
	/*
	  FILL HERE
	  create a TCP socket using socket()
	*/
	if( (sd = socket(AF_INET, SOCK_STREAM, 0)) < 0)  /* IPv4, TCP */
		printf ("socket call error\n");

	/*
	  FILL HERE
	  connect to the server on 'servhost' at 'servport'
	  use gethostbyname() and connect()
	*/
	if((hptr = gethostbyname(servhost)) == NULL)
		printf ("gethostbyname error\n");

	struct sockaddr_in servaddr;
	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(servport);
	memcpy(&servaddr.sin_addr, hptr->h_addr_list[0], hptr->h_length);

	if( connect(sd, (struct sockaddr*)&servaddr, sizeof(struct sockaddr_in)) < 0)
		printf ("connet fail\n");
	/*
	  FILL HERE
	  figure out the port assigned to this client
	  use getsockname()
	*/
	struct sockaddr_in clientaddr;
	socklen_t len;
	len = sizeof(clientaddr);
	if(getsockname(sd, (struct sockaddr*)&clientaddr, &len) < 0)
		printf("getsockname call error");
	clientport = ntohs(clientaddr.sin_port);

	/* succesful. return socket descriptor */
	printf("admin: connected to server on '%s' at '%hu' thru '%hu'\n",
			 servhost, servport, clientport);
	return(sd);
}
/*----------------------------------------------------------------*/


/*----------------------------------------------------------------*/
int readn(int sd, char *buf, int n)
{
	int     toberead;
	char *  ptr;

	toberead = n;
	ptr = buf;
	while (toberead > 0) {
		int byteread;

		byteread = read(sd, ptr, toberead);
		if (byteread <= 0) {
			if (byteread == -1)
				perror("read");
			return(0);
		}
    
		toberead -= byteread;
		ptr += byteread;
	}
	return(1);
}

char *recvtext(int sd)
{
	char *msg;
	long  len;
  
	/* read the message length */
	if (!readn(sd, (char *) &len, sizeof(len))) {
		return(NULL);
	}
	len = ntohl(len);

	/* allocate space for message text */
	msg = NULL;
	if (len > 0) {
		msg = (char *) malloc(len);
		if (!msg) {
			fprintf(stderr, "error : unable to malloc\n");
			return(NULL);
		}

		/* read the message text */
		if (!readn(sd, msg, len)) {
			free(msg);
			return(NULL);
		}
	}
  
	/* done reading */
	return(msg);
}

int sendtext(int sd, char *msg)
{
	long len;

	/* write lent */
	len = (msg ? strlen(msg) + 1 : 0);
	len = htonl(len);
	write(sd, (char *) &len, sizeof(len));

	/* write message text */
	len = ntohl(len);
	if (len > 0)
		write(sd, msg, len);
	return(1);
}
/*----------------------------------------------------------------*/
