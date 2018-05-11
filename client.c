#include "tcpClient.h"
#include "com_udp.h"
#include <sys/mman.h>

// passe le descripteur en bloquant en non bloquant ou l'inverse;
int changeBlock (int desc) {
	int valeur = fcntl (desc, F_GETFL, 0);
	fcntl (desc, F_SETFL, valeur | O_NONBLOCK);
	return desc;
}


int main (int taille, char *args[]) {

	if (taille < 3) {
		fprintf(stderr, "Missing args. Usage : ./client address port\n");
		return -1;
	}

	int port = atoi(args[2]);
	char *address = args[1];

	struct sockaddr_in adress_sock;
	adress_sock.sin_family = AF_INET;
	adress_sock.sin_port = htons(port);
  
	inet_aton(address,&adress_sock.sin_addr);

	int descr=socket(PF_INET,SOCK_STREAM,0);
	int r=connect(descr,(struct sockaddr *)&adress_sock, sizeof(struct sockaddr_in));
	// La on a une socket TCP qui est bloquante

	//	descr = changeBlock(descr);
	// Variable partage entre les forks :
	// int *number = mmap(NULL, sizeof(int), PROT_READ | PROT_WRITE, MAP_SHARED | MAP_ANONYMOUS, -1, 0);
	// in_game = 0 si le joueur n'est pas dans une partie, 1 sinon


	if (r != -1) {
		// On a reussi a se connecter
		// On va s'occuper du TCP ici qui va lancer l'UDP si besoin
		tcpCommunication(descr, port);		
	} else {
		printf("FAIL TCP CONNECTION\n");
		perror ("ERROR ");
	}
	

	return 0;
}