/* Ritossa Andrea 0001020070 */

#include <dirent.h>
#include <unistd.h>

#define LINE_LENGTH 256

int main(int argc, char ** argv){
    DIR           *dir1, *dir2, *dir3;
    struct dirent *dd1, *dd2;
    char          newDir[LINE_LENGTH];
    char risp;

    int conn_sd = STDOUT_FILENO;

    if (argc != 2){
        printf("Usage: dirs nomeDirectory");
    }
    char * dir = argv[1];
    
    if ((dir1 = opendir(dir)) != NULL) { // direttorio presente
        risp = 'S';
        printf("Invio risposta affermativa al client\n");
        write(conn_sd, &risp, sizeof(char));

        while ((dd1 = readdir(dir1)) != NULL)
        {
            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0)
            {
                // build new path
                sprintf(newDir, "%s/%s", dir, dd1->d_name);

                if ((dir2 = opendir(newDir)) != NULL)
                { // dir sec livello
                    while ((dd2 = readdir(dir2)) != NULL)
                    {
                        if (strcmp(dd2->d_name, ".") != 0 && strcmp(dd2->d_name, "..") != 0)
                        {
                            // build new path
                            strcat(newDir, "/");
                            strcat(newDir, dd2->d_name);

                            // se non è un direttorio, è un file da considerare 
                            if ((dir3 = opendir(newDir)) == NULL)
                            { // file of sec livellov
                                /*
                                 * Check if it's a text file.
                                */
                                char *ext = ext = strstr(dd2->d_name, ".txt");
                                if (ext != NULL && strcmp(ext, ".txt")==0)
                                {
                                    printf("Sending name: %s\n", dd2->d_name);
                                    if (write(conn_sd, dd2->d_name,
                                            (strlen(dd2->d_name) + 1)) < 0)
                                    {
                                        perror("Error in sending the file name\n");
                                        continue;
                                    }
                                }

                                printf("Invio nome: %s\n", dd2->d_name);
                                if (write(conn_sd, dd2->d_name,
                                        (strlen(dd2->d_name) + 1)) < 0)
                                {
                                    perror("Errore nell'invio del nome file\n");
                                    continue;
                                }
                            } // if file 2 livello
                        }     // if not . and .. 2 livello
                    }         // while in 2* livello
                    printf("Fine invio\n");
                } // if dir 2 livello
            }     // if opendir 2 livello
        }         // while frst livello
        risp = '\1';
        write(conn_sd, &risp, sizeof(char));

}