/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.util;

import java.io.IOException;
import java.io.OutputStream;

import com.intellij.openapi.diagnostic.Logger;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;

/**
 * <p></p>
 * <br/>
 * <p>Created on 06.02.14</p>
 *
 * @author Lukasz Zielinski
 */
public class ProcessGobbler {
    private final Logger LOG = Logger.getInstance(getClass());
    
    private final Process process;

    public ProcessGobbler(Process process) {
        this.process = process;
    }
    
    public int waitFor(DecompilationContext context, OutputStream output, OutputStream err) 
        throws IOException, InterruptedException {
        
        final boolean debug = LOG.isDebugEnabled();
        
        StreamPumper outputPumper = new StreamPumper(context,
                                                     "output",
                                                     process.getInputStream(),
                                                     output);
        Thread outputThread = new Thread(outputPumper);
        outputThread.start();
        StreamPumper errPumper = new StreamPumper(context,
                                                  "error",
                                                  process.getErrorStream(),
                                                  err);
        Thread errThread = new Thread(errPumper);
        errThread.start();
        
        //magic code indicating InterruptedException
        int exitCode = 9000;
        try {
            if (debug) {
                LOG.debug("Waiting for process finish");
            }
            
            exitCode = process.waitFor();

            if (debug) {
                LOG.debug("Process finished, exit code: "+exitCode);
            }
        } finally {
            //always stop pumping
            outputPumper.stopPumping();
            errPumper.stopPumping();
        }
        return exitCode;
    }
}
