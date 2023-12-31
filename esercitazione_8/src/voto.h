/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _VOTO_H_RPCGEN
#define _VOTO_H_RPCGEN

#include <rpc/rpc.h>


#ifdef __cplusplus
extern "C" {
#endif

#define NUM_GIUDICI 4
#define MAX_LENGTH_NOME 10
#define MAX_CANDIDATI 10

struct InputVoto {
	char nome_candidato[MAX_LENGTH_NOME];
	char voto_espressione;
};
typedef struct InputVoto InputVoto;

struct VotoGiudice {
	char nome_giudice[MAX_LENGTH_NOME];
	int voti;
};
typedef struct VotoGiudice VotoGiudice;

struct ClassificaGiudici {
	VotoGiudice classifica[NUM_GIUDICI];
};
typedef struct ClassificaGiudici ClassificaGiudici;

struct Candidato {
	char nome_candidato[MAX_LENGTH_NOME];
	char nome_giudice[MAX_LENGTH_NOME];
	char categoria;
	char nome_file[MAX_LENGTH_NOME];
	char fase;
	int voto;
};
typedef struct Candidato Candidato;

#define VOTO 0x20000013
#define VOTOVERS 1

#if defined(__STDC__) || defined(__cplusplus)
#define VISUALIZZA_CLASSIFICA 1
extern  ClassificaGiudici * visualizza_classifica_1(void *, CLIENT *);
extern  ClassificaGiudici * visualizza_classifica_1_svc(void *, struct svc_req *);
#define ESPRIMI_VOTO 2
extern  int * esprimi_voto_1(InputVoto *, CLIENT *);
extern  int * esprimi_voto_1_svc(InputVoto *, struct svc_req *);
extern int voto_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define VISUALIZZA_CLASSIFICA 1
extern  ClassificaGiudici * visualizza_classifica_1();
extern  ClassificaGiudici * visualizza_classifica_1_svc();
#define ESPRIMI_VOTO 2
extern  int * esprimi_voto_1();
extern  int * esprimi_voto_1_svc();
extern int voto_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_InputVoto (XDR *, InputVoto*);
extern  bool_t xdr_VotoGiudice (XDR *, VotoGiudice*);
extern  bool_t xdr_ClassificaGiudici (XDR *, ClassificaGiudici*);
extern  bool_t xdr_Candidato (XDR *, Candidato*);

#else /* K&R C */
extern bool_t xdr_InputVoto ();
extern bool_t xdr_VotoGiudice ();
extern bool_t xdr_ClassificaGiudici ();
extern bool_t xdr_Candidato ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_VOTO_H_RPCGEN */
