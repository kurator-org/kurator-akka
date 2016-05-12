package org.kurator.postproccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lowery on 4/7/16.
 */
public interface PostProcessor {
    public void postprocess(InputStream in, String format, OutputStream out) throws IOException;
}
