/* CS555 Project 2
 * Maofei Chen
 * G00709508
 */

/*--------------------------------------------------------------------*/
/* chat server */

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

#include "common.h"

#include <stdlib.h>
#include <unistd.h>
/*--------------------------------------------------------------------*/

/*--------------------------------------------------------------------*/

/* info about a client */
typedef struct _member {
  /* member name */
  char * name;

  /* member socket */
  int sock;

  /* member of group */
  int grid;

  /* next member */
  struct _member * next;

  /* prev member */
  struct _member * prev;

} Member;

/* info about a group */
typedef struct _group {
  /* group name */
  char * name;

  /* maximum capacity */
  int capa;

  /* current occupancy */
  int occu;

  /* member list */
  struct _member * mems;

} Group;

/* list of all groups */
Group * group;
int ngroups; /* number of groups? */
/*--------------------------------------------------------------------*/


/*--------------------------------------------------------------------*/

/* find the group with given name */
int findgroup(char *name) {
  int grid;

  for (grid = 0; grid < ngroups; grid++) {
    if (strcmp(group[grid].name, name) == 0)
      return (grid);
  }
  return (-1);
}

/* find the member with given name */
Member *findmemberbyname(char *name) {
  int grid;

  /* go thru each group */
  for (grid = 0; grid < ngroups; grid++) {
    Member *memb;

    /* go thru all members */
    for (memb = group[grid].mems; memb; memb = memb->next) {
      if (strcmp(memb->name, name) == 0)
	return (memb);
    }
  }
  return (NULL);
}

/* find the member with given sock */
Member *findmemberbysock(int sock) {
  int grid;

  /* go thru each group */
  for (grid = 0; grid < ngroups; grid++) {
    Member *memb;

    /* go thru all members */
    for (memb = group[grid].mems; memb; memb = memb->next) { /* don't understand */
      if (memb->sock == sock)
	return (memb);
    }
  }
  return (NULL);
}
/*--------------------------------------------------------------------*/


/*--------------------------------------------------------------------*/

/* clean up before exit */
void cleanup() {
  char linkname[MAXNAMELEN];

  /* unlink the link */
  sprintf(linkname, "%s/%s", getenv("HOME"), PORTLINK);
  unlink(linkname);

  exit(0);
}

/* main routine */
main(int argc, char *argv[]) {
  int servsock; /* server socket descriptor */
  int maxsd; /* largest client socket descriptor */

  fd_set livesdset; /* set of live client sockets */
  fd_set livesdset_cp; /* reset fd_set each select */

  /* check usage */
  if (argc != 2) {
    fprintf(stderr, "usage : %s <groups-file>\n", argv[0]);
    exit(1);
  }

  /* init groups */
  if (!initgroups(argv[1])) {
    exit(1);
  }

  /* setup signal handlers to clean up */
  signal(SIGTERM, cleanup);
  signal(SIGINT, cleanup);

  /* get ready to receive requests */
  servsock = startserver();
  if (servsock == -1) {
    exit(1);
  }

  /* init maxsd */
  maxsd = servsock;

  /* init fd set */
  FD_ZERO(&livesdset);
  FD_SET(servsock, &livesdset); /* Add server sd to livesdset */

  /* receive requests and process them */
  while (1) {
    int sock; /* loop variable */

    /*
      FILL HERE
      wait using select() for
      packets on existing clients and
      connect requests from new clients
    */
    livesdset_cp = livesdset;
    if (select(maxsd + 1, &livesdset_cp, NULL, NULL, NULL) < 0)
      printf("select error!\n");

    /* look for requests from existing clients */
    for (sock = 3; sock <= maxsd; sock++) {
      /* skip the server socket */
      if (sock == servsock) continue;

      if (FD_ISSET(sock, &livesdset_cp) /* FILL HERE: message from client 'sock'? */) {
	Packet *pkt;

	/* read the message */
	pkt = recvpkt(sock);
	printf ("receive packet from client: %d\n", sock );
	if (!pkt) {
	  printf ("not packet? \n");
	  /* client socket disconnected */
	  char *clientname; /* host name of the client */

	  struct sockaddr_in sa_clientaddr; /* host address */
	  socklen_t len = sizeof (sa_clientaddr); /* socket address length */
	  struct hostent *hptr;

	  /*
	    FILL HERE:
	    figure out client's host name
	    using getpeername() and gethostbyaddr()
	  */
	  if (getpeername(sock, (struct sockaddr*) &sa_clientaddr, &len) < 0) {
	    printf("getpeername error\n");
	    /* return(-1); */
	  }

	  if ((hptr = gethostbyaddr((const char *) &sa_clientaddr.sin_addr,
				    sizeof (sa_clientaddr.sin_addr), AF_INET)) == NULL)
	    printf("gethostbyaddr error\n");
	  clientname = hptr->h_name;
	  /* clientport = ntohs(sa_clientaddr.sin_port); */

	  printf("admin: disconnect from '%s' at '%d'\n",
		 clientname, sock);

	  /* revoke its membership */
	  leavegroup(sock);

	  /* close the socket */
	  close(sock);

	  /*
	    FILL HERE:
	    stop looking for packets from this 'sock' further
	  */
	  FD_CLR(sock, &livesdset);
	  if (maxsd == sock) { /* the largest sd is removed, track the second largest */
	    int i, tempMax = 0;
	    for (i = 0; i < maxsd; i++) {
	      if (FD_ISSET(i, &livesdset)) {
		if (i > tempMax)
		  tempMax = i;
	      }
	    }
	    maxsd = tempMax;

	  } 
	} else {
	  char *gname, *mname;
	  printf ("send list group msg to client, when client init\n");

	  /* take action based on messge type */
	  switch (pkt->type) {
	  case LIST_GROUPS:
	    listgroups(sock);
	    break;
	  case JOIN_GROUP:
	    gname = pkt->text;
	    mname = gname + strlen(gname) + 1;
	    joingroup(sock, gname, mname);
	    break;
	  case LEAVE_GROUP:
	    leavegroup(sock);
	    break;
	  case USER_TEXT:
	    relaymsg(sock, pkt->text);
	    break;
	  }

	  /* free the message */
	  freepkt(pkt);
	}
				
      }
    }
    if (FD_ISSET(servsock, &livesdset_cp) /* FILL HERE: connect request from a new client? */) {
      int csd; /* new client socket descriptor */

      /*
	FILL HERE:
	accept a new connection request
      */
      struct sockaddr_in sa_clientaddr; /* host address */
      socklen_t len = sizeof (sa_clientaddr); /* socket address length */
      struct hostent *hptr;
      csd = accept(servsock, (struct sockaddr*) &sa_clientaddr, &len);

      /* if accept is fine? */
      if (csd != -1) {
	char *clientname;

	/*
	  FILL HERE:
	  figure out client's host name using gethostbyaddr()
	*/
	if ((hptr = gethostbyaddr((const char*) &sa_clientaddr.sin_addr,
				  sizeof (struct in_addr), AF_INET)) == NULL)
	  printf("in connect phase, gethostbyaddr error\n");
	clientname = hptr->h_name; /* already pointer */
	/* clientport = ntohs(sa_clientaddr.sin_port); */

	/* dispaly the client's hostname and the socket */
	printf("admin: connect from '%s' at '%d'\n",
	       clientname, csd);

	/*
	  FILL HERE
	  look for packets from 'csd' now on
	*/
	FD_SET(csd, &livesdset);

	/* keep track of the max */
	if (csd > maxsd)
	  maxsd = csd;
      } else {
	perror("accept");
	exit(0);
      }
    }
		
  }
}
/*--------------------------------------------------------------------*/


/*--------------------------------------------------------------------*/

/* init group list */
int initgroups(char *groupsfile) {
  FILE * fp;
  char name[MAXNAMELEN];
  int capa;
  int grid;

  /* open the file */
  fp = fopen(groupsfile, "r");
  if (!fp) {
    fprintf(stderr, "error : unable to open file '%s'\n", groupsfile);
    return (0);
  }

  /* get the number of groups */
  fscanf(fp, "%d", &ngroups);

  /* allocate space for all groups */
  group = (Group *) calloc(ngroups, sizeof (Group));
  if (!group) {
    fprintf(stderr, "error : unable to calloc\n");
    return (0);
  }

  /* read info on all groups */
  for (grid = 0; grid < ngroups; grid++) {
    /* get name and capa */
    if (fscanf(fp, "%s %d", name, &capa) != 2) {
      fprintf(stderr, "error : no info on group %d\n", grid + 1);
      return (0);
    }

    /* fill in details */
    group[grid].name = strdup(name);
    group[grid].capa = capa;
    group[grid].occu = 0;
    group[grid].mems = NULL;
  }
  return (1);
}

/* list groups and their occupancies */
int listgroups(int sock) {
  int grid;
  char pktbufr[MAXPKTLEN];
  char * bufrptr;
  long bufrlen;

  /* each piece of info is separated by null char */
  bufrptr = pktbufr;
  for (grid = 0; grid < ngroups; grid++) {
    /* out group name */
    sprintf(bufrptr, "%s", group[grid].name);
    bufrptr += strlen(bufrptr) + 1;

    /* out group capacity */
    sprintf(bufrptr, "%d", group[grid].capa);
    bufrptr += strlen(bufrptr) + 1;

    /* out group occupancy */
    sprintf(bufrptr, "%d", group[grid].occu);
    bufrptr += strlen(bufrptr) + 1;
  }
  bufrlen = bufrptr - pktbufr;

  /* send the message */
  sendpkt(sock, LIST_GROUPS, bufrlen, pktbufr);
  return (1);
}

/* join the group as a member */
int joingroup(int sock, char *gname, char *mname) {
  int grid;
  Member * memb;

  /* get hold of the group */
  grid = findgroup(gname);
  if (grid == -1) {
    char *errmsg = "no such group";
    sendpkt(sock, JOIN_REJECTED, strlen(errmsg), errmsg);
    return (0);
  }

  /* check if member name already exists */
  memb = findmemberbyname(mname);
  if (memb) {
    char *errmsg = "member name already exists";
    sendpkt(sock, JOIN_REJECTED, strlen(errmsg), errmsg);
    return (0);
  }

  /* check if room is full */
  if (group[grid].capa == group[grid].occu) {
    char *errmsg = "room is full";
    sendpkt(sock, JOIN_REJECTED, strlen(errmsg), errmsg);
    return (0);
  }

  /* make this a member of the group */
  memb = (Member *) calloc(1, sizeof (Member));
  if (!memb) {
    fprintf(stderr, "error : unable to calloc\n");
    cleanup();
  }
  memb->name = strdup(mname);
  memb->sock = sock;
  memb->grid = grid;
  memb->prev = NULL;
  memb->next = group[grid].mems;
  if (group[grid].mems) {
    group[grid].mems->prev = memb;
  }
  group[grid].mems = memb;
  printf("admin: '%s' joined '%s'\n", mname, gname);

  /* one more member in this group */
  group[grid].occu++;

  /* send acceptance message */
  sendpkt(sock, JOIN_ACCEPTED, 0, NULL);
  return (1);
}

/* leave the group */
int leavegroup(int sock) {
  Member * memb;

  /* get hold of the member */
  memb = findmemberbysock(sock);
  if (!memb) {
    return (0);
  }

  /* exclude from the group */
  if (memb->next) {
    memb->next->prev = memb->prev;
  }
  /* remove from ... */
  if (group[memb->grid].mems == memb) {
    /* the head of list */
    group[memb->grid].mems = memb->next;
  } else {
    /* the middle of list */
    memb->prev->next = memb->next;
  }
  printf("admin: '%s' left '%s'\n",
	 memb->name, group[memb->grid].name);

  /* one more left the group */
  group[memb->grid].occu--;

  /* free up member */
  free(memb->name);
  free(memb);
  return (1);
}

/* leave the group */
int relaymsg(int sock, char *text) {
  Member * memb;
  Member * sender;
  char pktbufr[MAXPKTLEN];
  char * bufrptr;
  long bufrlen;

  /* get hold of the member */
  sender = findmemberbysock(sock);
  if (!sender) {
    fprintf(stderr, "strange: no member at %d\n", sock);
    return (0);
  }

  /* prepend the member name to text */
  bufrptr = pktbufr;
  strcpy(bufrptr, sender->name);
  bufrptr += strlen(bufrptr) + 1;
  strcpy(bufrptr, text);
  bufrptr += strlen(bufrptr) + 1;
  bufrlen = bufrptr - pktbufr;

  /* send msg to all members of the group */
  for (memb = group[sender->grid].mems;
       memb; memb = memb->next) {
    /* skip the sender */
    if (memb->sock == sock) continue;

    /* send the pkt */
    sendpkt(memb->sock, USER_TEXT, bufrlen, pktbufr);
  }
  printf("%s: %s", sender->name, text);
  return (1);
}
/*--------------------------------------------------------------------*/
