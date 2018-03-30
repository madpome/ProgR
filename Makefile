SHELL := /bin/bash
SOURCES = 
OBJS = $(SOURCES:%.c=%.o)
CC = gcc
OPTS = -g -Wall
all : $(SOURCES:%.c:%)
% : %.o
	@echo "linking $(@:%.o=%) ..."
	@$(CC) $(OPTS) -o $@ $<

%.o : %.c
	@echo "building $(@:%=%) ..."
	@$(CC) $(OPTS) -c $<


cleanO :
	rm -f $(OBJS)
clean:
	rm -f $(SOURCES:%.c:%) $(OBJS)
