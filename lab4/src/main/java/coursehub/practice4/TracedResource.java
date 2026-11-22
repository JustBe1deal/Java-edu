package coursehub.practice4;

import java.io.*;
import java.util.*;
public final class TracedResource implements AutoCloseable {
    private final List<String> log; private final boolean failClose;
    public TracedResource(List<String> log, boolean failClose) { this.log = log; this.failClose = failClose; log.add("open"); }
    public void use(boolean fail) throws IOException { log.add("use"); if (fail) throw new IOException("use failed"); }
    @Override public void close() throws IOException { log.add("close"); if (failClose) throw new IOException("close failed"); }
}
