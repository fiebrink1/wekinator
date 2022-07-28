/*
 * Used to "copy" stdout and stderr to both console/terminal (if applicable) and to log file
 */
package wekimini.util;

import java.io.PrintStream;
import java.util.logging.Level;

/**
 * Adapted from
 * http://stackoverflow.com/questions/1356706/copy-stdout-to-file-without-stopping-it-showing-onscreen
 *
 */
public class TeeStream extends PrintStream {

    PrintStream myOut;

    public TeeStream(PrintStream out1, PrintStream out2) {
        super(out1);
        this.myOut = out2;
    }

    @Override
    public void write(byte buf[], int off, int len) {
        try {
            super.write(buf, off, len);
            myOut.write(buf, off, len);
        } catch (Exception e) {
            //TODO: REMOVE THIS
            System.out.println("ERROR: " +e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void flush() {
        super.flush();
        myOut.flush();
    }
}
