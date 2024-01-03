/* scan_client.c
 * gcc -o scan_client scan_client.c scan_clnt.c scan_xdr.c -lnsl -ltirpc
*/
#include "scan.h"
#include <rpc/rpc.h>
#include <stdio.h>

#define MAX_LENGTH 50

int main(int argc, char * argv[]){

    CLIENT  * cl;
    char    * server;
    char    choice;
    int     x;
    static FileName         nome_file;
    static InputDirScan     dir_input;
    OutputDirScan           * dir_output;
    FileNumbers             * file_output;


    if (argc != 2) {
        fprintf(stderr, "uso: %s host\n", argv[0]);
        exit(1);
    }

    server = argv[1];

    cl = clnt_create(server, SCANPROG, SCANVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(server);
        exit(1);
    }

    /* Corpo client */
    printf("\nScegli che operazione: \n\tD -> dir_scan \n\tF -> file_scan\n\tClrt+D -> Exit\n:");
    while(gets(&choice)){
        switch (choice) {
        case ('D'):
            printf("Dir scan\n");
            
            printf("Nome direttorio:");
            gets(dir_input.direttorio);

            printf("Grandezza massima:");
            while(scanf("%d", &(dir_input.x)) != 1){
                printf("Invalid input! Please enter an integer.\n");
                while(getchar() != '\n');
            }
            dir_output = dir_scan_1(&dir_input, cl);
            
            if (dir_output->num_files > 0){
                printf("Dir scan output for dir [%s]: %d\n", dir_input.direttorio, dir_output->num_files);
            } else {
                printf("Dir scan output for dir [%s]: %d\n", dir_input.direttorio, dir_output->num_files);
            }            
            break;

        case ('F'):
            printf("File scan\n");

            printf("Nome file: ");
            gets(nome_file.name);
            printf("\nFILENAME:%s\n", nome_file.name);

            file_output = file_scan_1(&nome_file, cl);

            if (file_output != NULL){
                printf("NULL output");
            } else if(file_output->n_caratteri > 0){
                printf("File scan output: { chars: %d, words: %d, lines: %d}",
                    file_output->n_caratteri,file_output->n_parole, file_output->n_linee);
            } else {
                printf("File output: -1");
            }
            break;

        default:
            printf("Scegli tra (D, F, Ctrl+D).");
            break;
        
        }
        printf("Scegli che operazione: \n\tD -> dir_scan \n\tF -> file_scann\n\tClrt+D -> Exit\n:");
    }

    // free(input_dir->direttorio);
    // free(input_dir);
}