
#include <stdio.h>
char* contact(char* cstr1,char* cstr2){
    char *result = (char *) malloc(strlen(cstr1) + strlen(cstr2));
    sprintf(result, "%s%s", cstr1,cstr2);
    return result;
}