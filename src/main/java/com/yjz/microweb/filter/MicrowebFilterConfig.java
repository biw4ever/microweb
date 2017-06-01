package com.yjz.microweb.filter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.glassfish.grizzly.http.server.util.Enumerator;

import com.yjz.microweb.context.MicrowebServletContext;

public class MicrowebFilterConfig implements FilterConfig
{
    
    /**
     * The Context with which we are associated.
     */
    private MicrowebServletContext servletContext = null;
    
    /**
     * The application Filter we are configured for.
     */
    private Filter filter = null;
    
    /**
     * Filter's initParameters.
     */
    private Map<String, String> initParameters = null;
    
    /**
     * Filter name
     */
    private String filterName;
    
    // ------------------------------------------------------------------ //
    public MicrowebFilterConfig(MicrowebServletContext servletContext)
    {
        this.servletContext = servletContext;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitParameter(String name)
    {
        if (initParameters == null)
        {
            return null;
        }
        return ((String)initParameters.get(name));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilterName()
    {
        return filterName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getInitParameterNames()
    {
        Map map = initParameters;
        if (map == null)
        {
            return (new Enumerator<String>(new ArrayList<String>()));
        }
        else
        {
            return (new Enumerator<String>(map.keySet()));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ServletContext getServletContext()
    {
        return servletContext;
    }
    
    /**
     * Return the application Filter we are configured for.
     */
    public Filter getFilter()
    {
        return filter;
    }
    
    /**
     * Release the Filter instance associated with this FilterConfig, if there is one.
     */
    protected void recycle()
    {
        if (this.filter != null)
        {
            filter.destroy();
        }
        this.filter = null;
    }
    
    /**
     * Set the {@link Filter} associated with this object.
     * 
     * @param filter
     */
    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
    
    /**
     * Set the {@link Filter}'s name associated with this object.
     * 
     * @param filterName the name of this {@link Filter}.
     */
    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }
    
    /**
     * Set the init parameters associated with this associated {@link Filter}.
     * 
     * @param initParameters the configuration parameters for this {@link Filter}
     */
    public void setInitParameters(Map<String, String> initParameters)
    {
        if (initParameters != null && !initParameters.isEmpty())
        {
            this.initParameters = initParameters;
        }
    }
}
