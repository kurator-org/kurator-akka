#include <stdio.h>
#include <dlfcn.h>

#include <Python.h>
#include "org_kurator_akka_interpreters_PythonInterpreter.h"

JNIEXPORT jobject

JNICALL Java_org_kurator_akka_interpreters_PythonInterpreter_eval(JNIEnv *env, jobject obj, jstring code, jstring func, jobject options) {

    // Python variables
    PyObject *pArgs, *pValue, *pModule, *pFunc, *pGlobal, *pLocal, *pDict;

    // JNI variables
    jboolean iscopy;
    const *jCode, *jFunc;

    // Load python2.7 dynamic library, symbols defined will be made
    // available to subsequently loaded shared objects via the
    // RTLD_GLOBAL flag
    dlopen("libpython2.7.so", RTLD_LAZY | RTLD_GLOBAL);

    // Initialize the python interpreter
    Py_Initialize();

    // Redirect stdout to file
    // TODO: interface with Java and pass python std as jstring?
    PyObject *sys = PyImport_ImportModule("sys");
    PyObject *out = PyFile_FromString("python_out", "w+");
    PyObject_SetAttrString(sys, "stdout", out);

    FILE *output = PyFile_AsFile(out);

    // Create a new module object for the inline code
    pModule = PyImport_AddModule("__main__");

    // Get the dictionary object from the module
    pGlobal = PyModule_GetDict(pModule);
    pLocal = pGlobal;

    // Get inline code and name of the python function from java strings
    jCode = (*env)->GetStringUTFChars(env, code, &iscopy);
    jFunc = (*env)->GetStringUTFChars(env, func, &iscopy);

    // Define functions in new module by running inline code
    pValue = PyRun_String(jCode, Py_file_input, pGlobal, pLocal);

    if (pValue == NULL) {
        if (PyErr_Occurred()) {
            PyErr_Print();
        }
        return 1;
    }

    Py_DECREF(pValue);

    // Get function from the new module
    pFunc = PyObject_GetAttrString(pModule, jFunc);

    if (pFunc && PyCallable_Check(pFunc)) {
        // Create the empty optdict python argument
        pArgs = PyTuple_New(1);

        // Get the options as a python dict
        pDict = request_dict(env, options);

        // Add pDict to input args and call the function
        PyTuple_SetItem(pArgs, 0, pDict);
        pDict = PyObject_CallObject(pFunc, pArgs);

        Py_DECREF(pArgs);

        // Process python return value
        if (pDict != NULL) {
            // If the function didn't return a dict, the Java
            // return type should be NULL
            if (!PyDict_Check(pDict)) {
                return 0;
            }

            // Create the response Java Map
            jobject jMap = response_map(env, pDict);

            // Return map as response
            return jMap;
        } else {
            Py_DECREF(pFunc);
            Py_DECREF(pModule);
            PyErr_Print();
            fprintf(stderr, "Call failed\n");
            return 1;
        }
    } else {
        if (PyErr_Occurred())
        PyErr_Print();
        fprintf(stderr, "Cannot find function \"%s\"\n", func);
    }

    Py_XDECREF(pFunc);
    Py_DECREF(pModule);

    Py_Finalize();

    // Write stdout to file
    fclose(output);
}

JNIEXPORT jobject

JNICALL Java_org_kurator_akka_interpreters_PythonInterpreter_run(JNIEnv *env, jobject obj, jstring name, jstring func, jobject options) {

    // Python variables
    PyObject *pName, *pModule, *pDict, *pFunc;
    PyObject *pArgs, *pList, *pKey, *pValue;
    int i;

    // JNI variables
    jboolean iscopy;
    const *jName, *jFunc;

    // Load python2.7 dynamic library, symbols defined will be made
    // available to subsequently loaded shared objects via the
    // RTLD_GLOBAL flag
    dlopen("libpython2.7.so", RTLD_LAZY | RTLD_GLOBAL);

    // initialize the Java Map interface and methods
    jclass c_Map = (*env)->FindClass(env, "java/util/HashMap");

    jmethodID m_Init = (*env)->GetMethodID(env, c_Map, "<init>", "()V");
    jmethodID m_KeySet = (*env)->GetMethodID(env, c_Map, "keySet", "()Ljava/util/Set;");
    jmethodID m_Get = (*env)->GetMethodID(env, c_Map, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
    jmethodID m_Put = (*env)->GetMethodID(env, c_Map, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    // initialize the Java Set interface and methods
    jclass c_Set = (*env)->FindClass(env, "java/util/Set");

    jmethodID m_ToArray = (*env)->GetMethodID(env, c_Set, "toArray", "()[Ljava/lang/Object;");

    // Get names of the python module and function from java strings
    jName = (*env)->GetStringUTFChars(env, name, &iscopy);
    jFunc = (*env)->GetStringUTFChars(env, func, &iscopy);

    //printf("name: %s, func: %s\n", jName, jFunc);

    // Initialize the python interpreter and import the module
    Py_Initialize();
    pName = PyString_FromString(jName);
    pModule = PyImport_Import(pName);
    Py_DECREF(pName);

    // If the module loaded successfully get the python function and
    // process input parameters from java map
    if (pModule != NULL) {
        pFunc = PyObject_GetAttrString(pModule, jFunc);

        if (pFunc && PyCallable_Check(pFunc)) {

            // Create the empty optdict python argument
            pArgs = PyTuple_New(1);

            // Get the options as a python dict
            pDict = request_dict(env, options);

            // Add pDict to input args and call the function
            PyTuple_SetItem(pArgs, 0, pDict);
            pDict = PyObject_CallObject(pFunc, pArgs);

            Py_DECREF(pArgs);

            // Process python return value
            if (pDict != NULL) {

                // If the function didn't return a dict, the Java
                // return type should be NULL
                if (!PyDict_Check(pDict)) {
                    return 0;
                }

                // Create the response Java Map
                jobject jMap = response_map(env, pDict);

                // Return map as response
                return jMap;

            } else {
                  Py_DECREF(pFunc);
                  Py_DECREF(pModule);
                  PyErr_Print();
                  fprintf(stderr, "Call failed\n");
                  return 1;
            }
        } else {
            if (PyErr_Occurred())
                PyErr_Print();
            fprintf(stderr, "Cannot find function \"%s\"\n", func);
        }
        Py_XDECREF(pFunc);
        Py_DECREF(pModule);
    } else {
        PyErr_Print();
        fprintf(stderr, "Failed to load \"%s\"\n", name);
        return 1;
    }


    Py_Finalize();
}
