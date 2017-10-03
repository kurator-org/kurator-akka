#include "org_kurator_akka_interpreters_RInterpreter.h"

#include <Rinternals.h>
#include <Rembedded.h>

JNIEXPORT jobject

JNICALL Java_org_kurator_akka_interpreters_RInterpreter_run(JNIEnv *env, jobject obj, jstring name, jstring func, jobject options) {
    /* Adapted from https://github.com/parkerabercrombie/call-r-from-c */

    // Intialize the embedded R environment.
    int r_argc = 2;
    char *r_argv[] = { "R", "--silent" };
    Rf_initEmbeddedR(r_argc, r_argv);

    // Hardcoded name for now
    const char* name = "test.R";
    const char* func = "hello";

    // S-expression represented by a pair (function to invoke, arguments to function)
    SEXP e;

    // PROTECT and UPROTECT macros tell the R interpreter when to garbage collect an
    // object in use by c code
    PROTECT(e = lang2(install("source"), mkString(name)));
    R_tryEval(e, R_GlobalEnv, NULL);
    UNPROTECT(1);

    // Create a hardcoded C array to use in an R vector arg
    int arr[] = { 1, 2, 3, 4, 5 };
    int length = 5;

    // Allocate an R vector and copy the C array into it.
    SEXP arg;
    PROTECT(arg = allocVector(INTSXP, alen));
    memcpy(INTEGER(arg), a, alen * sizeof(int));

    // Setup a call to the R function
    SEXP func_call;
    PROTECT(func_call = lang2(install(func), arg));

    // Execute the function
    int errorOccurred;
    SEXP ret = R_tryEval(add1_call, R_GlobalEnv, &errorOccurred);

    if (!errorOccurred)
    {
        double *val = REAL(ret);

        printf("R returned: ");
        for (int i = 0; i < LENGTH(ret); i++)
            printf("%0.1f, ", val[i]);
        printf("\n");
    }

    // Unprotect func_call and arg
    UNPROTECT(2);

    // Release R environment
    Rf_endEmbeddedR(0);
}