package com.yjz.microweb.http;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import com.yjz.microweb.util.Assert;

public class MicrowebServletInputStream extends ServletInputStream
{
    
    private final InputStream sourceStream;
    
    private volatile boolean closed = false;
    
    private volatile boolean eof = false;
    
    private volatile Boolean ready = Boolean.TRUE;
    
    private volatile ReadListener readListener = null;
    
    /**
     * @Description 
     * @author biw
     * @param sourceStream
     */
    public MicrowebServletInputStream(InputStream sourceStream)
    {
        Assert.notNull(sourceStream, "Source InputStream must not be null");
        this.sourceStream = sourceStream;
    }
    
    /**
     * Return the underlying source stream (never {@code null}).
     */
    public final InputStream getSourceStream()
    {
        return this.sourceStream;
    }
    
    @Override
    public int read()
        throws IOException
    {
        return this.sourceStream.read();
    }
    
    @Override
    public final int read(byte[] b, int off, int len)
        throws IOException
    {
        preReadChecks();
        
        try
        {
            int result = sourceStream.read(b, off, len);
            if (result == -1)
            {
                eof = true;
            }
            return result;
        }
        catch (IOException ioe)
        {
            close();
            throw ioe;
        }
    }
    
    @Override
    public void close()
        throws IOException
    {
        closed = true;
        super.close();
        this.sourceStream.close();
    }
    
    @Override
    public boolean isFinished()
    {
        Assert.notNull(readListener, "readListener must not be null");
        return eof;
    }
    
    @Override
    public boolean isReady()
    {
        Assert.notNull(readListener, "readListener must not be null");
        
        if (eof || closed)
        {
            return false;
        }
        
        if (ready != null)
        {
            return ready.booleanValue();
        }
        
        return false;
    }
    
    @Override
    public void setReadListener(ReadListener readListener)
    {
        this.readListener = readListener;
    }
    
    private void preReadChecks()
    {
        if (readListener != null && (ready == null || !ready.booleanValue()))
        {
            throw new IllegalStateException();
        }
        if (closed)
        {
            throw new IllegalStateException();
        }
        ready = null;
    }
    
}
