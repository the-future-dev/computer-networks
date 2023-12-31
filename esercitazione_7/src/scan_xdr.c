/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "scan.h"

bool_t
xdr_FileNumbers (XDR *xdrs, FileNumbers *objp)
{
	register int32_t *buf;

	 if (!xdr_int (xdrs, &objp->n_caratteri))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->n_parole))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->n_linee))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_InputDirScan (XDR *xdrs, InputDirScan *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->direttorio, 256,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	 if (!xdr_int (xdrs, &objp->x))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_FileName (XDR *xdrs, FileName *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_vector (xdrs, (char *)objp->name, 256,
		sizeof (char), (xdrproc_t) xdr_char))
		 return FALSE;
	return TRUE;
}

bool_t
xdr_OutputDirScan (XDR *xdrs, OutputDirScan *objp)
{
	register int32_t *buf;

	int i;
	 if (!xdr_int (xdrs, &objp->num_files))
		 return FALSE;
	 if (!xdr_vector (xdrs, (char *)objp->file_names, 8,
		sizeof (FileName), (xdrproc_t) xdr_FileName))
		 return FALSE;
	return TRUE;
}
