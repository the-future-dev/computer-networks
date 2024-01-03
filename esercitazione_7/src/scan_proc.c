/* scan proc.c 
 *  + implementazione delle procedure remote: "file_scan" e "dir_scan".
 *  +include "scan.h"
*/

#include "scan.h"
#include <dirent.h>
#include <fcntl.h>
#include <rpc/rpc.h>
#include <sys/stat.h>
#include <stdio.h>
#include <string.h>

#define MAX_FILES 8

FileNumbers * file_scan_1_svc(FileName * input, struct svc_req *rp){

    int fd_file = open(input->name, O_RDONLY);
    char ch, last_char = ' ';
    static FileNumbers risposta;
    risposta.n_caratteri = 0, risposta.n_linee = 0, risposta.n_parole = 0;

    if (fd_file < 0){
        printf("Could not open file %s", (input->name));
        risposta.n_caratteri = -1, risposta.n_linee = -1, risposta.n_parole = -1;
        return &risposta; /* ? */
    }   
    
    while (read(fd_file, &ch, 1) > 0) {
        risposta.n_caratteri++;

        if((ch=='\n'))
            risposta.n_linee++;

        if((ch == ' ' || ch == '\n' || ch == '\t')
            && (last_char != ' '&& last_char != '\n' && last_char != '\t'))
            risposta.n_parole++;
        
        last_char = ch;
    }
    
    close(fd_file);
    return &risposta;
}

OutputDirScan * dir_scan_1_svc (InputDirScan * input, struct svc_req *rp){
    DIR             *d;
    struct dirent   *dir;
    struct stat     stat;
    int             count = 0, fd_file;
    char            fullpath[256];
    static OutputDirScan output;

    printf("Direttorio: %s\n", input->direttorio);
    if ((dir = opendir(input->direttorio)) == NULL){
        output.num_files = -1;
        return (&output);
    }

    // MAX 8 files to be returned.
    output.num_files = 0;
    while((dir = readdir(d)) != NULL && output.num_files < 8){
        snprintf(fullpath, sizeof(fullpath), "%s/%s", input->direttorio, dir->d_name);

        fd_file = open(fullpath, O_RDONLY);
        if (fd_file < 0){
            printf("FILE: %s\n", fullpath);
            perror("OpenFile:");
            output.num_files = -1;
            return(&output);
        }

        fstat(fd_file, &stat);

        // Check if it is a regular file and if the size is greater than the threshold
        if (S_ISREG(stat.st_mode) && stat.st_size >= input->x){
            strcpy(output.file_names[output.num_files].name, dir->d_name);
            output.num_files++;
        }

        memset(fullpath, 0, 256);
    }

    return (&output);

}