#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>

void tcpCommunication (int descr, int port);
int readACmd (int descr, char *str);
char *writeACmd (char *str, int *finalLength, int *portUDP);
void treatReceip (char *str, char **portMulti, char **ipDiff, int *ingame, int port, int len, int *portUDP, int* sockUDP, int* sockMulti);
void readFirstCommand (int descr);
char *char3(char *nbr);
char *getLE(char *nbr);
void afficheMessage(char **str, int *len);
char **split(char *str, char sep, int *n);
char *trim(char *str, char sep);
int extractNbDir (char *str, char* res, int len);
void getUDPport(char *str, int*portUDP, int len);
