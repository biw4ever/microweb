package com.yjz.microweb;

public class MicrowebException extends RuntimeException
{
    public MicrowebException(Throwable t)
    {
        super(t);
    }
    
    public MicrowebException(String message)
    {
        super(message);
    }
}
