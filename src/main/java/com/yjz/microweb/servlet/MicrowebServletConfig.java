package com.yjz.microweb.servlet;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.glassfish.grizzly.http.server.util.Enumerator;
import org.glassfish.grizzly.utils.DataStructures;

import com.yjz.microweb.context.MicrowebServletContext;

public class MicrowebServletConfig implements ServletConfig
{
    
    protected String name;
    
    protected final ConcurrentMap<String, String> initParameters =
        DataStructures.<String, String> getConcurrentMap(16, 0.75f, 64);
    
    protected final MicrowebServletContext servletContextImpl;
    
    public MicrowebServletConfig(MicrowebServletContext servletContextImpl)
    {
        this.servletContextImpl = servletContextImpl;
    }
    
    public MicrowebServletConfig(String name, MicrowebServletContext servletContextImpl)
    {
        this.name = name;
        this.servletContextImpl = servletContextImpl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletName()
    {
        return name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ServletContext getServletContext()
    {
        return servletContextImpl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitParameter(String name)
    {
        return initParameters.get(name);
    }
    
    public void setInitParameters(Map<String, String> parameters)
    {
        if (parameters != null && !parameters.isEmpty())
        {
            this.initParameters.clear();
            this.initParameters.putAll(parameters);
        }
    }
    
    /**
     * Set the name of this servlet.
     *
     * @param name The new name of this servlet
     */
    public void setServletName(String name)
    {
        this.name = name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getInitParameterNames()
    {
        return (new Enumerator(initParameters.keySet()));
    }
}
