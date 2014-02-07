/*
 * Copyright 2007 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package net.stevechaloner.intellijad.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import com.intellij.openapi.diagnostic.Logger;
import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;
import org.jetbrains.annotations.NotNull;

/**
 * Pumps the input stream of a process, ensuring it doesn't block.
 *
 * @author Steve Chaloner
 */
public class StreamPumper implements Runnable {
    
    private final Logger LOG = Logger.getInstance(getClass());
    
    /**
     * End of stream flag.
     */
    private final AtomicBoolean pump = new AtomicBoolean(true);

    /**
     * The target output stream.
     */
    @NotNull
    private final OutputStream out;

    /**
     * The source input stream.
     */
    @NotNull
    private final InputStream in;

    /**
     * The context the pumper is working in.
     */
    @NotNull
    private final DecompilationContext context;
    
    @NotNull
    private final String name;
    
    /**
     * Initialises a new instance of this class.
     *
     * @param context the context the pumper is working in
     * @param in the input stream
     * @param out the output stream
     */
    public StreamPumper(@NotNull DecompilationContext context,
                        @NotNull String name,
                        @NotNull InputStream in,
                        @NotNull OutputStream out) {
        this.context = context;
        this.name = name;
        this.in = in;
        this.out = out;
    }

    /**
     * While the end of the stream hasn't been reached, pump
     * the content of the input stream into the output stream.
     */
    public void run() {
        final boolean debug = LOG.isDebugEnabled();
        
        if (debug) {
            LOG.debug("["+name+"] started");
        }
        
        try {
            byte[] buffer = new byte[512];
            while (!Thread.currentThread().isInterrupted() && pump.get()) {
                int pumped = pump(buffer);
                if (debug) {
                    LOG.debug("["+name+"] pumped "+pumped+" bytes");
                }
                if (pumped <= 0) {
                    Thread.sleep(5);
                }
            }
        } catch (InterruptedException e) {
            context.getConsoleContext().addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                                   "error",
                                                   e.getMessage());
        } catch (IOException e) {
            context.getConsoleContext().addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                                   "error",
                                                   e.getMessage());
        } finally {
            if (debug) {
                if (Thread.currentThread().isInterrupted()) {
                    LOG.info("["+name+"] interrupted");                    
                } else {
                    LOG.debug("["+name+"] finished");
                }
            }
        }
    }

    /**
     * Pumps chunk of the stream if content is available.
     *
     * @throws IOException if there is an error accessing one of the streams.
     */
    private int pump(byte[] buffer) throws IOException {
        
        int bytesRead = in.read(buffer, 0, buffer.length);

        if (bytesRead > 0) {
            out.write(buffer, 0, bytesRead);            
        }
        return bytesRead;
    }

    /**
     * Stops the pump.
     */
    public void stopPumping() {
        pump.compareAndSet(true, false);
    }
}
