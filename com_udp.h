#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <unistd.h>

typedef struct args{
    int port;
    int portUDP;
    char *ipDiff;
    int *ingame;
    int portMulti;
}args;

void *receive(void* argu);
