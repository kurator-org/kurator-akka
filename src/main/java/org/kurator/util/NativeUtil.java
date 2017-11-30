package org.kurator.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.kurator.akka.interpreters.PythonInterpreter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NativeUtil {
    public static void loadLibrary(String libname) {
        try {
            String bit = System.getProperty("sun.arch.data.model");

            if (SystemUtils.IS_OS_MAC && bit.equals("64")) {
                loadDarwin64(libname);
            } else if (SystemUtils.IS_OS_LINUX  && bit.equals("64")) {
                loadLinux64(libname);
            } else {
                throw new RuntimeException("Operating system is unsupported by kuratorlib");
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to load kurator native libraries.", e);
        }
    }

    private static void loadLinux64(String libname) throws IOException {
        File lib = File.createTempFile("lib" + libname, ".so");

        InputStream src = NativeUtil.class.getResourceAsStream("/native/lib" + libname + ".so");
        OutputStream dest = new FileOutputStream(lib);

        IOUtils.copy(src, dest);
        System.out.println("Loading native lib: " + lib.getAbsolutePath());
        try {
            System.load(lib.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadDarwin64(String libname) throws IOException {
        File lib = File.createTempFile("native", "lib" + libname + ".dylib");

        InputStream src = NativeUtil.class.getResourceAsStream("/native/lib" + libname + ".dylib");
        OutputStream dest = new FileOutputStream(lib);

        IOUtils.copy(src, dest);

        System.load(lib.getAbsolutePath());
    }
}
