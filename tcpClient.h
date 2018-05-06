#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>

void tcpCommunication (int descr, char **portUDP, char **ipMulti, int *in_game, int port);
int readACmd (int descr, char *str);
void writeACmd (char *str);
void treatReceip (char *cmd, char **portUDP, char **ipDiff, int *in_game, int port, int len);
int isAValidIP (char *ip);
void treatSend (char *cmd, char **portUDP);
void readFirstCommand (int descr);
void afficheMessage(char **str, int *len);
char **split(char *str, char sep, int *n);
char *trim(char *str, char sep);
