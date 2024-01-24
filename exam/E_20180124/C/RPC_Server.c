/* Ritossa Andrea 0001020070 */

/*
 * xfactor_s.c
 */

#include "RPC_xFile.h"
#include <rpc/rpc.h>
#include <dirent.h>
// #include <stdio.h>
// #include <stdlib.h>
// #include <string.h>

// implementazione delle procedure definite nel file XDR
Output * lista_file_prefisso_1_svc(Input *input, struct svc_req *reqstp) {
    Output          * res;
    int             condition, index = 0;
    DIR             * dir;
    struct          dirent *dd1;
    
    printf("\n\nRequest for lista nomi files con prefisso [%s]:\n", input->prefisso);
    
    // Clear the contents of res
    memset(&res, 0, sizeof(res));
    
    if ((dir = opendir(input->nomeDirettorio)) != NULL) { // direttorio esiste
        while((dd1 = readdir(dir)) != NULL && index < 6) {
            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0){
                condition =  strstr(dd1->d_name, input->prefisso) == dd1->d_name;
                
                printf("[%d] file: %s | strstr: %d\n",index, dd1->d_name, condition);

                if (condition == 1){
                    // Allocate new memory and copy the string
                    res->files[index] = dd1->d_name;
                    
                    index++;
                }
                printf("D");
            }
        }
        printf("C");
        // Close the directory
        closedir(dir);
    }
    printf("B");
    res->size = index;
    printf("A");
    
    return res;
}
int * conta_occorrenza_linea_1_svc(linea *, struct svc_req * reqstp) {
    static int found = -1;

    return (&found);
}
