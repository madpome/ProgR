#include "com_udp.h"

void * receive(void* argut){
    puts("On est dans receive udp");
    args * argu = (args *)argut;
    //A déplacer dans le fork
    int portUDP = argu->portUDP;
    int portMulti = argu->portMulti;
    char *ipMulti = argu->ipDiff;

    int * running = argu->ingame;
    printf("portUDP = %d, portmulti = %d, ip = %s\n",portUDP,portMulti,ipMulti);
    int pid = fork();
    if(pid == 0){
        //Reception de messages normaux
        int sock=socket(PF_INET,SOCK_DGRAM,0);
        struct sockaddr_in address_sock;
        address_sock.sin_family=AF_INET;
        address_sock.sin_port=htons(portUDP);
        address_sock.sin_addr.s_addr=htonl(INADDR_ANY);
        int r=bind(sock,(struct sockaddr *)&address_sock,sizeof(struct sockaddr_in));
        if(r==0){
            puts("Reception normaux");
            char tampon[256];
            while(*running != 0){
                printf("On est la\n");
                int rec=recv(sock,tampon,256,0);
                tampon[rec]='\0';
                printf("%s\n",tampon);
            }
        } else {
            printf("FAAAAAIL Bon apparemment on peut pas se connecter en UDP pour les messages normaux\n");
            perror ("Error ");
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
            puts("Reception multicast");
            char tampon[256];
            while(*running !=0 ){
                int rec=recv(sock,tampon,256,0);
                tampon[rec]='\0';
                //printf("%s\n",tampon);
            }
        }
    }
    return 0;
}
