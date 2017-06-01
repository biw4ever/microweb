package com.yjz.microweb.filter;

import java.io.CharConversionException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;

import org.glassfish.grizzly.http.util.URLDecoder;

public class FilterMap implements Serializable
{
    
    private static final EnumSet<DispatcherType> DEFAULT_DISPATCHER = EnumSet.of(DispatcherType.REQUEST);
    
    /**
     * The name of the filter with which this filter mapping is associated
     */
    private String filterName = null;
    
    /**
     * The servlet name for which this filter mapping applies
     */
    private String servletName = null;
    
    /**
     * The URL pattern for which this filter mapping applies
     */
    private String urlPattern = null;
    
    /**
     * The dispatcher types of this filter mapping
     */
    private Set<DispatcherType> dispatcherTypes;
    
    // ------------------------------------------------------------- Properties
    
    public String getFilterName()
    {
        return (this.filterName);
    }
    
    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }
    
    public String getServletName()
    {
        return (this.servletName);
    }
    
    public void setServletName(String servletName)
    {
        this.servletName = servletName;
    }
    
    public String getURLPattern()
    {
        return (this.urlPattern);
    }
    
    public void setURLPattern(String urlPattern)
    {
        
        try
        {
            this.urlPattern = URLDecoder.decode(urlPattern);
        }
        catch (CharConversionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public Set<DispatcherType> getDispatcherTypes()
    {
        // Per the SRV.6.2.5 absence of any dispatcher elements is
        // equivelant to a REQUEST value
        return (dispatcherTypes == null || dispatcherTypes.isEmpty()) ? DEFAULT_DISPATCHER : dispatcherTypes;
    }
    
    public void setDispatcherTypes(Set<DispatcherType> dispatcherTypes)
    {
        this.dispatcherTypes = dispatcherTypes;
    }
    
    // --------------------------------------------------------- Public Methods
    
    /**
     * Render a String representation of this object.
     */
    public String toString()
    {
        
        StringBuilder sb = new StringBuilder("FilterMap[");
        sb.append("filterName=");
        sb.append(this.filterName);
        if (servletName != null)
        {
            sb.append(", servletName=");
            sb.append(servletName);
        }
        if (urlPattern != null)
        {
            sb.append(", urlPattern=");
            sb.append(urlPattern);
        }
        sb.append("]");
        return (sb.toString());
    }
}
