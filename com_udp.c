#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <unistd.h>
void receive(int port, int portMulti, char *ipMulti){
    //A déplacer dans le fork
    int pid = fork();
    if(pid == 0){
        //Reception de messages normaux
        int sock=socket(PF_INET,SOCK_DGRAM,0);
        sock=socket(PF_INET,SOCK_DGRAM,0);
        struct sockaddr_in address_sock;
        address_sock.sin_family=AF_INET;
        address_sock.sin_port=htons(port);
        address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
        int r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
        if(r==0){
            char tampon[256];
            while(1){
                int rec=recv(sock,tampon,256,0);
                tampon[rec]='\0';
                printf("UDP NORMAL :%s",tampon);
            }
        }
    }else{
        //Reception du multicast
        int sock=socket(PF_INET,SOCK_DGRAM,0);
        int ok=1;
        int r=setsockopt(sock,SOL_SOCKET,SO_REUSEPORT,&ok,sizeof(ok));
        struct sockaddr_in address_sock;
        address_sock.sin_family=AF_INET;
        address_sock.sin_port=htons(portMulti);
        address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
        r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
        struct ip_mreq mreq;
        mreq.imr_multiaddr.s_addr=inet_addr(ipMulti);
        mreq.imr_interface.s_addr=htonl(INADDR_ANY);
        r=setsockopt(sock,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq));
        if(r == 0){
            char tampon[100];
            while(1){
                int rec=recv(sock,tampon,256,0);
                tampon[rec]='\0';
                printf("MULTICAST :%s",tampon);
            }
        }
    }
}
