#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int main(int argc, char ** argv){

}
//Verifie que c'est un ALL? ou un SEND? correcte
int check_msg(char *s){
    if(strlen(s)<4){
        return -1;
    }
    int n = strlen(s);
    char *p = malloc(5*sizeof(char));
    memcpy(p,s,4*sizeof(char));
    p[4] = '\0';
    if((s[n-1] == '*' && s[n-2] == '*' && s[n-3] == '*')){
        return -1;
    }
    if(strcmp(p,"SEND?") != 0){
        p[3] = '\0';
        if(strcmp(p, "ALL?") != 0){
            return -1;
        }else{
            if(s[4] == ' '){
                return 1;
            }
            return -1;
        }
    }else{
        if(n<11){
            return -1;
        }
        if(s[5] == ' '){
            int cmp = 1;
            for(int i = 7 ;s[i]; i++){
                if(s[i]==' '){
                    cmp++;
                }
            }
            if(cmp == 2){
                return 1;
            }
        }
        return -1;
    }
}
