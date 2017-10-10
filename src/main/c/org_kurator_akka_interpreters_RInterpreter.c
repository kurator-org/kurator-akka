#include <stdio.h>
#include <string.h>
#include <signal.h>

#include <Rinternals.h>
#include <Rembedded.h>

#define R_INTERFACE_PTRS 1
#define CSTACK_DEFNS 1
#include <Rinterface.h>

#include "org_kurator_akka_interpreters_RInterpreter.h"

JNIEXPORT jobject

JNICALL Java_org_kurator_akka_interpreters_RInterpreter_run(JNIEnv *env, jobject obj, jstring jname, jstring jfunc, jobject options) {
    /* Adapted from https://github.com/parkerabercrombie/call-r-from-c */

    // Initialize the embedded R environment.
    char *r_argv[] = { "R", "--no-save" };
    int r_argc = sizeof(r_argv)/sizeof(r_argv[0]);

    Rf_initialize_R(r_argc, r_argv);

    /* disable stack checking, because threads will thow it off */
    R_CStackLimit = (uintptr_t) -1;
    setup_Rmainloop();

    //Rf_mainloop(); /* does not return */

    // Hardcoded name for now
    const char* name = "/home/lowery/IdeaProjects/kurator-akka/src/main/c/test.R";
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
    PROTECT(arg = allocVector(INTSXP, length));
    memcpy(INTEGER(arg), arr, length * sizeof(int));

    // Setup a call to the R function
    SEXP func_call;
    PROTECT(func_call = lang2(install(func), arg));

    // Execute the function
    int errorOccurred;
    SEXP ret = R_tryEval(func_call, R_GlobalEnv, &errorOccurred);

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
    printf("testing");

    // Release R environment
    kill(getpid(), SIGINT);

    jobject jMap
    return 0;
}