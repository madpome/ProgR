#include "tcpClient.h"
#include "com_udp.h"

void tcpCommunication (int descr, char **portUDP, char **ipMulti, int *in_game, int port) {
	int pid = fork();
	if (pid == 0) {
		printf("On est en reception TCP\n");
		// On s'occupe de la reception ici
		char *cmd = calloc (1000, sizeof(char));
		while (1) {
			memset(cmd, '\0', 1000);
			int len = readACmd(descr, cmd);
			afficheMessage(&cmd, &len);
			treatReceip (cmd, portUDP, ipMulti, in_game, port, len);
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

/*
	Liste des messages envoyes par le serveur :
	Prob :
	GLIST! s***
	WELCOME m h w f ip port***
	UNREGOK m***
	GAMES n***
	REGOK m***
	SIZE! m h w***
	LIST! m s***
	GAME m s***

	Pas de prob :
	GPLAYER id x y p***
	POS id x y***
	MOV x y***
	MOF x y p***
	BYE***
	ALL!***
	NOSEND***
	DUNNO***
	REGNO***

*/
void afficheCmd (char *str, int len) {
	for (int i = 0; i<len; i++) {
		printf("%c", str[i]);
	}
}

void afficheMessage (char **string, int *length) {
	int len = *length;
	char *cpy = calloc (len, sizeof(char));
	if ((*string)[0] == '\n') {
		for (int i = 0; i<len-1; i++) {
			cpy[i] = (*string)[i+1];
			(*string)[i] = (*string)[i+1];
		}
		len--;
		(*length)--;
	} else {
		for (int i = 0; i<*length; i++) {
			cpy[i] = (*string)[i];
		}
	}
	char *token; 
	token = strtok(cpy, " ");
	if (strcmp(token, "BYE***") == 0 ||
		strcmp(token, "ALL!***") == 0 ||
		strcmp(token, "NOSEND***") == 0 ||
		strcmp(token, "DUNNO***") == 0 ||
		strcmp(token, "REGNO***") == 0) {
		printf("%s\n", token);
	} else if (strcmp(token, "MOV") == 0 || strcmp (token, "MOF") == 0 ||
				strcmp(token, "POS") == 0 ||  strcmp(token, "GPLAYER") == 0) {
		afficheCmd(cpy, len);
	} else if (strcmp(token, "GLIST!") == 0) {
		printf("GLIST! ");
		printf("%d", cpy[7] * 256 + cpy[8]);
		printf("***\n");
	} else if (strcmp(token, "WELCOME") == 0) {
		printf("WELCOME ");
		printf("%d ", cpy[8] * 256  + cpy[9]); //m
		printf("%d ", cpy[11] * 256 + cpy[12]); //h
		printf("%d ", cpy[14] * 256 + cpy[15]); //w
		printf("%d ", cpy[17] * 256 + cpy[18]); //f
		for (int i = 19; i<*length; i++) {
			printf("%c", cpy[i]);
		}
		printf("\n");
	} else if (strcmp(token, "UNREGOK") == 0) {
		printf("UNREGOK %d***\n", cpy[8] * 256 + cpy[9]);
	} else if (strcmp(token, "GAMES") == 0) {
		printf("GAMES %d***\n", cpy[6] * 256 + cpy[7]);
	} else if (strcmp(token, "REGOK") == 0) {
		printf("REGOK %d***\n", cpy[6] * 256 + cpy[7]);
	} else if (strcmp(token, "SIZE!") == 0) {
		printf("SIZE! %d %d %d\n", cpy[6] * 256 + cpy[7], cpy[9] * 256 + cpy[10], cpy[12] * 256 + cpy[13]);
	} else if (strcmp(token, "LIST!") == 0) {
		printf("LIST! %d %d***\n", cpy[6] * 256 + cpy[7], cpy[9] * 256 + cpy[10]);
	} else if (strcmp(token, "GAME") == 0) {
		printf("GAME %d %d\n", cpy[5] * 256 + cpy[6], cpy[8] * 256 + cpy[9]);
	}
}

int readACmd (int descr, char *str) {
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

	return ite;
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
char * doubleString(char *s){
	int n=  strlen(s);
	s = realloc(s,2*n);
	s[2*n-1]='\0';
	return s;
}
char * trim(char *s, char sep){
	int deb = 0;
	int fin =0;
	while(s[deb]==sep){
		deb++;
	}
	while(s[strlen(s)-1-fin]==sep){
		fin++;
	}
	s[strlen(s)-fin] = '\0';
	memmove(s,s+deb,strlen(s)-deb+1);
	return s;
}
char **split(char *s, char sep, int * taille){
	s = trim(s,sep);
	*taille = 1;
	int j = 0;
	int c = 0;
	int len = 10;
	char **tab = malloc(200*sizeof(char *));
	tab[0] = malloc(10);
	int inarow = 0;
	for(int i = 0 ;s[i]; i++){
		if(s[i]!=sep){
			if(c == len-1){
				tab[j]=doubleString(tab[j]);
				len = 2*len;
			}
			inarow = 0;
			tab[j][c]=s[i];
			c++;
		}else{
			if((c != 0 || j!=0) && inarow == 0){
				inarow = 1;
				tab[j][c]='\0';
				j++;
				c=0;
				tab[j]=malloc(10);
				len = 10;
			}
		}
	}
	tab[j][c]='\0';
	*taille = j+1;

	return tab;
}

void treatReceip (char *str, char **portUDP, char **ipDiff, int *ingame, int port, int len) {
	const char s[2] = " ";
	char *token;
	char *caca = calloc (len, sizeof(char));

	token = strtok(caca, s);
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
		} else if (step == 4) {
			if (isAValidIP (token) > 0) {
				flag1++;
				if (type == 1) {
					tmpip = token;
				}

			} else {
				flag1 = 0;
			}
		} else if (step == 5) {
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
	free(caca);
	if (flag1 > 0 && flag2 > 0) {
		*ipDiff = strtok(tmpip, "#");;
		*portUDP = tmpudp;
		*ingame = 1;
		receive(port, atoi(*portUDP), *ipDiff, ingame);

	}

}

int isAValidIP (char *ip) {
	return 1;
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
	char *tok = strtok (rcp, "*");
	tok = strtok(NULL, " ");
	int n = tok[0] + tok[1] * 256;
	printf("n = %d\n", n);
	for (int i = 0; i<n; i++) {
		memset(rcp, '\0', 10000);
		readACmd(descr, rcp);
		printf("%s\n", rcp);
	}
	free(rcp);
}
