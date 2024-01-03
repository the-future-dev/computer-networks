/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "voto.h"

bool_t
xdr_InputVoto (XDR *xdrs, InputVoto *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->nome_candidato, MAX_LENGTH_NOME,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_char (xdrs, &objp->voto_espressione))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_VotoGiudice (XDR *xdrs, VotoGiudice *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->nome_giudice, MAX_LENGTH_NOME,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->voti))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_ClassificaGiudici (XDR *xdrs, ClassificaGiudici *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->classifica, NUM_GIUDICI,
		sizeof (VotoGiudice), (xdrproc_t) xdr_VotoGiudice))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_Candidato (XDR *xdrs, Candidato *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->nome_candidato, MAX_LENGTH_NOME,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_vector (xdrs, (char *)objp->nome_giudice, MAX_LENGTH_NOME,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_char (xdrs, &objp->categoria))
		 return FALSE;
	 if (!xdr_vector (xdrs, (char *)objp->nome_file, MAX_LENGTH_NOME,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_char (xdrs, &objp->fase))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->voto))
		 return FALSE;
	return TRUE;
}
