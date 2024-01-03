/* 
 * scan.x
 *	Metodi remoti
 * 	- file_scan: (string) -> Response
 *	- dir_scan: (InputDirScan) -> OutputDirScan
 */

struct FileNumbers
{
	int n_caratteri;
	int n_parole;
	int n_linee;
};

struct InputDirScan
{
	char direttorio [256];
	int x;
};

struct FileName {
    char name [256];
};

struct OutputDirScan
{
	int num_files;
	FileName file_names[8];
};

program SCANPROG {
	version SCANVERS {
		FileNumbers FILE_SCAN(FileName) = 1;
		OutputDirScan DIR_SCAN(InputDirScan) = 2;
	} = 1;
} = 0x20000013;
