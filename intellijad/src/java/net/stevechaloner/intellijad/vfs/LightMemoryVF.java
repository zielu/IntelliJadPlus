/* 
 * @(#) $Id:  $
 */
package net.stevechaloner.intellijad.vfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * <p></p>
 * <br/>
 * <p>Created on 12.02.13</p>
 *
 * @author Lukasz Zielinski
 */
public class LightMemoryVF implements MemoryVF {
    private final VirtualFile virtualFile;

    public LightMemoryVF(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    @NotNull
    @Override
    public String getContent() {
        try {
            return new String(virtualFile.contentsToByteArray(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VirtualFile asVirtualFile() {
        return virtualFile;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public MemoryVF addChild(MemoryVF file) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setWritable(boolean writable) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setContent(@NotNull String content) {
        try {
            OutputStream out = virtualFile.getOutputStream(this);
            BufferedReader reader = new BufferedReader(new StringReader(content));
            String line;
            byte[] newLine = "\n".getBytes("UTF-8");
            while ((line = reader.readLine()) != null) {
                out.write(line.getBytes("UTF-8"));
                out.write(newLine);
            }
            reader.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
