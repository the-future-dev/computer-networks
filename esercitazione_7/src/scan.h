/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _SCAN_H_RPCGEN
#define _SCAN_H_RPCGEN

#include <rpc/rpc.h>


#ifdef __cplusplus
extern "C" {
#endif


struct FileNumbers {
	int n_caratteri;
	int n_parole;
	int n_linee;
};
typedef struct FileNumbers FileNumbers;

struct InputDirScan {
	char direttorio[256];
	int x;
};
typedef struct InputDirScan InputDirScan;

struct FileName {
	char name[256];
};
typedef struct FileName FileName;

struct OutputDirScan {
	int num_files;
	FileName file_names[8];
};
typedef struct OutputDirScan OutputDirScan;

#define SCANPROG 0x20000013
#define SCANVERS 1

#if defined(__STDC__) || defined(__cplusplus)
#define FILE_SCAN 1
extern  FileNumbers * file_scan_1(FileName *, CLIENT *);
extern  FileNumbers * file_scan_1_svc(FileName *, struct svc_req *);
#define DIR_SCAN 2
extern  OutputDirScan * dir_scan_1(InputDirScan *, CLIENT *);
extern  OutputDirScan * dir_scan_1_svc(InputDirScan *, struct svc_req *);
extern int scanprog_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define FILE_SCAN 1
extern  FileNumbers * file_scan_1();
extern  FileNumbers * file_scan_1_svc();
#define DIR_SCAN 2
extern  OutputDirScan * dir_scan_1();
extern  OutputDirScan * dir_scan_1_svc();
extern int scanprog_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_FileNumbers (XDR *, FileNumbers*);
extern  bool_t xdr_InputDirScan (XDR *, InputDirScan*);
extern  bool_t xdr_FileName (XDR *, FileName*);
extern  bool_t xdr_OutputDirScan (XDR *, OutputDirScan*);

#else /* K&R C */
extern bool_t xdr_FileNumbers ();
extern bool_t xdr_InputDirScan ();
extern bool_t xdr_FileName ();
extern bool_t xdr_OutputDirScan ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_SCAN_H_RPCGEN */
