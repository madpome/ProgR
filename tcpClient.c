#include "tcpClient.h"
#include "com_udp.h"
void tcpCommunication (int descr, char **portUDP, char **ipMulti, int *in_game, int port) {
	int pid = fork();
	if (pid == 0) {
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
	  int length = 0;
		while (1) {
		  // creation de str ici (flo)
		  char *str = calloc (10000,sizeof(char));
		  memset(str, '\0', 10000*sizeof(char));
		  
		  str = writeACmd( str, &length);
		  if (strlen(str)>length)
		    length = strlen(str);
		  
		  write(descr, str, length);
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
		printf("%d ", cpy[8]+ cpy[9] * 256); //m
		printf("%d ", cpy[11] + cpy[12] * 256); //h
		printf("%d ", cpy[14] + cpy[15] * 256); //w
		printf("%d", cpy[17] + cpy[18] * 256); //f
		for (int i = 19; i<*length; i++) {
			printf("%c", cpy[i]);
		}
		printf("\n");
	} else if (strcmp(token, "UNREGOK") == 0) {
		printf("UNREGOK %d***\n", cpy[8] + cpy[9] * 256);
	} else if (strcmp(token, "GAMES") == 0) {
		printf("GAMES %d***\n", cpy[6] + cpy[7] * 256);
	} else if (strcmp(token, "REGOK") == 0) {
		printf("REGOK %d***\n", cpy[6] + cpy[7] * 256);
	} else if (strcmp(token, "SIZE!") == 0) {
		printf("SIZE! %d %d %d\n", cpy[6] + cpy[7] * 256, cpy[9] + cpy[10] * 256, cpy[12] + cpy[13] * 256);
	} else if (strcmp(token, "LIST!") == 0) {
		printf("LIST! %d %d***\n", cpy[6] + cpy[7] * 256, cpy[9] + cpy[10] * 256);
	} else if (strcmp(token, "GAME") == 0) {
		printf("GAME %d %d\n", cpy[5] + cpy[6] * 256, cpy[8] + cpy[9] * 256);
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

char* writeACmd (char *str, int *finalLength) {
	int nbAtx = 0;
	int ite = 0;
	char c = '\0';

	char *type = calloc(10,'\0');
	int typeFound = 0;
	int onlyReturn = 1;
	int length = 0;
	
	while (nbAtx != 3) {
	  c = getchar();
	  if (!(onlyReturn && c == '\n')){
	    onlyReturn = 0;
	    if (!typeFound && c == ' '){
	      strcpy(type,str);
	      typeFound = 1;
	    }else if (c == '*') {
	      nbAtx++;
	    } else {
	      nbAtx = 0;
	    }
	    length++;
	    str[ite++] = c;
	  }
	}
	
	//printf("type: %s\n",type);

	if (strcmp(type,"NEW") == 0){
	  *finalLength = length;
	}else if (strcmp(type,"REG") == 0){
	  char ** splitted = split(str,' ',&length);
	  if (length != 4){
	    printf("L163");
	  }
	  
	  char *nbr = malloc (strlen(splitted[3])-3);
	  strcpy(nbr,splitted[3]);
	  nbr = getLE(nbr);
	  
	  str = calloc(100,sizeof(char));
	  strcat(str,"REG ");
	  strcat(str,splitted[1]);
	  strcat(str," ");
	  strcat(str,splitted[2]);
	  strcat(str," ");
	  strcat(str,nbr);
	  int p = strlen(str) -1 ;
	  str[p++] = ' ';
	  str[p++] = nbr[0];
	  str[p++] = nbr[1];
	  str[p++] = '*';
	  str[p++] = '*';
	  str[p++] = '*';
	  *finalLength = p;
	}else if (strcmp(type,"SIZE?") == 0 || strcmp(type,"LIST?") == 0){
	  char **splitted = split(str,' ',&length);
	  
	  char *nbr = malloc(strlen(splitted[1])-3);
	  strncpy(nbr,splitted[1],strlen(splitted[1])-3);
	  nbr = getLE(nbr);
	  
	  str = calloc(100,sizeof(char));
	  strcat(str,splitted[0]);
	  strcat(str," ");
	  int p = 6;
	  str[p++] = nbr[0];
	  str[p++] = nbr[1];
	  str[p++] = '*';
	  str[p++] = '*';
	  str[p++] = '*';
	   *finalLength = p;
	}else if (strcmp(type,"SEND") == 0 || strcmp(type,"ALL") == 0){
	  *finalLength = length;
	}else if (strcmp(type,"DOWN") == 0 || strcmp(type,"UP") == 0 || strcmp(type,"RIGHT") == 0 || strcmp(type,"LEFT") == 0){
	  char **splitted = split(str,' ',&length);
	  
	  char *nbr = malloc(strlen(splitted[1])-3);
	  strncpy(nbr,splitted[1],strlen(splitted[1])-3);
	  nbr = char3(nbr);
	  
	  str = calloc(100,sizeof(char));
	  strcat(str,splitted[0]);
	  strcat(str," ");
	  strcat(str,nbr);
	  strcat(str,"***");
	}
	return str;
}

char * doubleString(char **s){
  int n = strlen(*s);
  *s = realloc(*s,2*n);
  (*s)[2*n-1]='\0';
  return *s;
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
				tab[j]=doubleString(&tab[j]);
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
	char *caca = calloc (len, sizeof(char));
	printf("Treat Receip\n&");
	for (int i = 0; i<len; i++) {
		printf("%c", caca[i]);
		caca[i] = str[i];
	}
	printf("&\n");
	int flag1 = 0;
	char *tmpudp = calloc (5, sizeof(char));
	char *tmpip = calloc (16, sizeof(char));

	if (len == 6) {
		if (strcmp(str, "BYE***") == 0) {
			(*ingame) = 0;
			(*portUDP) = "0";
			(*ipDiff) = "0";
		}
	} else if (len == 43) {
		char welc[7] = {'W','E','L','C','O','M','E'};
		int flag = 1;
		for (int i = 0; i<7; i++) {
			if (str[i] != welc[i]) {
				flag = 0;
				break;
			}
		}
		if (flag) {
			for (int i = 20; i<35; i++) {
				tmpip[i-20] = str[i];
			}
			for (int i = 36; i<40; i++) {
				tmpudp[i-36] = str[i];
			}
			flag1 = 1;

		}
	}
	//WELCOME aa aa aa aa aaaaaaaaaaaaaaa port***


	
	free(caca);
	if (flag1 > 0) {
		for (int i = 0; tmpip[i] != '#' && i < 15; i++) {
			(*ipDiff)[i] = tmpip[i];
		}
		for (int i = 0; i<4; i++) {
			(*portUDP)[i] = tmpudp[i];
		}



		*portUDP = tmpudp;
		*ingame = 1;
		args *argument = malloc(sizeof(args));
		argument->ipDiff = *ipDiff;
		argument->portUDP = atoi(*portUDP);
		argument->port = port;
		argument->ingame = ingame;
		pthread_t t;
		pthread_create(&t,NULL,receive,argument);

	}

}

int isAValidIP (char *ip) {
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

char* char3(char *nbr){
  char *tmp = malloc(3*sizeof(char));
  if (strlen(nbr) == 1){
    tmp[2] = nbr[0];
    tmp[0] = '0';
    tmp[1] = '0';
  }

  if (strlen(nbr) == 2){
    tmp[2] = nbr[1];
    tmp[1] = nbr[0];
    tmp[0] = '0';
  }
  if (strlen(nbr) == 3){
    return nbr;
  }
  return tmp;
}

char* getLE(char *nbr){
	int nb = atoi(nbr);
	printf("%d\n",nb);
	char *tmp = malloc( 2 * sizeof(char));
	tmp[0] = (char)(nb%256);
	tmp[1] = (char)(nb/256);
	printf("%s\n",tmp);
	return tmp;
}
