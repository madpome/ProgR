#include "tcpClient.h"

void tcpCommunication (int descr, char **portUDP, char **ipMulti, int *in_game) {
	int pid = fork();
	if (pid == 0) {
		printf("On est en reception TCP\n");
		// On s'occupe de la reception ici
		readFirstCommand (descr);
		printf("On a lu la premiere commande recu\n");
		char *cmd = calloc (1000, sizeof(char));
		while (1) {
			memset(cmd, '\0', 1000);
			readACmd(descr, cmd);
			treatReceip (cmd, portUDP, ipMulti, in_game);
			printf("%s\n", cmd);
		}
		free(cmd);
	} else {
		// On s'occupe de l'envois
		printf("On est en envois TCP\n");
		char *str = calloc (10000,sizeof(char));
		while (1) {
			memset(str, '\0', 10000*sizeof(char));
			writeACmd(str);
			write(descr, str, strlen(str));
		}
	}
}

void readACmd (int descr, char *str) {
	int nbAtx = 0;
	int ite = 0;
	char c = '\0';
	while (nbAtx != 3) {
		read(descr, &c, sizeof(char));
		if (c == '*') {
			nbAtx++;
		} else {
			nbAtx = 0;
		}
		str[ite++] = c;
	}
}

void writeACmd (char *str) {
	int nbAtx = 0;
	int ite = 0;
	char c = '\0';
	while (nbAtx != 3) {
		c = getchar();
		if (c == '*') {
			nbAtx++;
		} else {
			nbAtx = 0;
		}
		str[ite++] = c;
	}
}

void treatReceip (char *str, char **portUDP, char **ipDiff, int *ingame) {
	const char s[2] = " ";
	char *token;

	token = strtok(str, s);
	int step = 0; 
	int type = -1; // 1 = WELCOME | 2 = BYE***
	int flag1 = 0;
	int flag2 = 0;
	char tmpudp[4];
	char *tmpip;
	while(token != NULL) {
		if (step == 0) {
			if (strcmp(token, "BYE***") == 0) {
				type = 2;
				(*ingame) = 0;
				(*portUDP) = "0";
				(*ipDiff) = "0";
				break;
			} else if (strcmp(token, "WELCOME") == 0) {
				type = 1;
			}
		} else if (step == 5) {
			if (isAValidIP (token) > 0) {
				flag1++;
				if (type == 1) {
					tmpip = token;
				}
				
			} else {
				flag1 = 0;
			}
		} else if (step == 6) {
			if (type == 1) {
				if (strlen(token) < 7) {
					flag2 = 0;
				} else {
					for (int i = 0; i<4; i++) {
						tmpudp[i] = token[i];
					}
					flag2++;
				}
			}
		}


    	token = strtok(NULL, s);
    	step++;
	}
	if (flag1 > 0 && flag2 > 0) {
		*ipDiff = strtok(tmpip, "#");;
		*portUDP = tmpudp;
		*ingame = 1;
	}
  
}

int isAValidIP (char *ip) {
	if (strlen(ip) != 15) {
		return -1;
	}

	return 1;
}

void treatSend (char *cmd, char **portUDP) {
	const char sep[2] = " ";

	char *tok = strtok(cmd, sep);
	int etap = 0;
	int type = -1;
	//Recup port udp
	char *port = calloc(4, sizeof(char));
	while (tok != NULL) {
		if (etap == 0) {
			if (strcmp(tok, "REG") == 0) {
				type = 8;
			} else if (strcmp(tok, "NEW") == 0) {
				type = 9;
			}
		} else {
			if (etap == 2 && (type == 8 || type == 9)) {
				if (strlen(tok) != 5) {
					break;
				}
				for (int i = 0; i<4; i++) {
					port[i] = tok[i];
				}
			}
		}

		tok = strtok (NULL, " ");
		etap++;
	}
	*portUDP = port;

}

void readFirstCommand (int descr) {
	char *rcp = calloc (10000, sizeof(char));
	readACmd(descr, rcp);
	printf("%s\n", rcp);
	char *tok = strtok (rcp, "#");
	tok = strtok(NULL, " ");
	tok = strtok(NULL, " ");
	int n = tok[0] + (int)tok[1] * 256;
	for (int i = 0; i<n; i++) {
		memset(rcp, '\0', 10000);
		readACmd(descr, rcp);
		printf("%s\n", rcp);
	}
}