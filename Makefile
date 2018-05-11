SHELL := /bin/bash
SOURCES = Game.java Ghost.java KruskalMaze.java Messagerie.java Occupe_Connection.java Occupe_Joueur.java Player.java Serveur.java TypeMessage.java ServeurDisplay.java
OBJS = $(SOURCES:%.c=%.o)
CC = gcc
OPTS = -g -Wall -pthread
all : Serveur Client

Serveur : $(SOURCES)
	javac $(SOURCES)

Client : client.c tcpClient.c com_udp.c com_udp.h tcpClient.h
	$(CC) $(OPTS) $^ -o Client

clean :
	rm *.o
