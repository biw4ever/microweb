package com.yjz.microweb.transport;

public class HttpServerConf
{
    
    protected boolean sslEnabled = false;
    
    protected int port = sslEnabled ? 443 : 8088;
    
    protected int corePoolSize = 100;
    
    protected int maximumPoolSize = 1000;
    
    protected int poolQueueSize = 100;
    
    public boolean isSslEnabled()
    {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled)
    {
        this.sslEnabled = sslEnabled;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public int getCorePoolSize()
    {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize)
    {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize)
    {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getPoolQueueSize()
    {
        return poolQueueSize;
    }

    public void setPoolQueueSize(int poolQueueSize)
    {
        this.poolQueueSize = poolQueueSize;
    }

    
  
}
