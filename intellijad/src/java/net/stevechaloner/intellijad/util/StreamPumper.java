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

import net.stevechaloner.intellijad.console.ConsoleEntryType;
import net.stevechaloner.intellijad.decompilers.DecompilationContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Pumps the input stream of a process, ensuring it doesn't block.
 *
 * @author Steve Chaloner
 */
public class StreamPumper implements Runnable
{
    /**
     * End of stream flag.
     */
    private boolean pump = true;

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

    /**
     * Initialises a new instance of this class.
     *
     * @param context the context the pumper is working in
     * @param in the input stream
     * @param out the output stream
     */
    public StreamPumper(@NotNull DecompilationContext context,
                        @NotNull InputStream in,
                        @NotNull OutputStream out)
    {
        this.context = context;
        this.in = in;
        this.out = out;
    }

    /**
     * While the end of the stream hasn't been reached, pump
     * the content of the input stream into the output stream.
     */
    public void run()
    {
        try
        {
            while (pump)
            {
                pump();
                Thread.sleep(5);
            }
        }
        catch (InterruptedException e)
        {
            context.getConsoleContext().addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                                   "error",
                                                   e.getMessage());
        }
        catch (IOException e)
        {
            context.getConsoleContext().addMessage(ConsoleEntryType.DECOMPILATION_OPERATION,
                                                   "error",
                                                   e.getMessage());
        }
    }

    /**
     * Repeatedly pumps the stream if content is available.
     *
     * @throws IOException if there is an error accessing one of the streams.
     */
    private void pump()
            throws IOException
    {
        byte[] buffer = new byte[512];
        int bytesRead = in.read(buffer,
                                0,
                                buffer.length);

        if (bytesRead > 0)
        {
            out.write(buffer,
                      0,
                      bytesRead);
            pump();
        }
    }

    /**
     * Stops the pump.
     */
    public void stopPumping()
    {
        pump = false;
    }
}
