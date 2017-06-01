package com.yjz.microweb.filter;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

import org.glassfish.grizzly.servlet.WebappContext;

import com.yjz.microweb.context.MicrowebServletContext;

public class MicrowebFilterRegistration extends Registration implements FilterRegistration.Dynamic
{
    
    protected Class<? extends Filter> filterClass;
    
    protected Filter filter;
    
    public Class<? extends Filter> getFilterClass()
    {
        return filterClass;
    }
    
    public void setFilterClass(Class<? extends Filter> filterClass)
    {
        this.filterClass = filterClass;
    }
    
    public Filter getFilter()
    {
        return filter;
    }
    
    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
    
    public boolean isAsyncSupported()
    {
        return isAsyncSupported;
    }
    
    protected boolean isAsyncSupported;
    
    // ------------------------------------------------------------ Constructors
    
    /**
     * Creates a new FilterRegistration associated with the specified {@link WebappContext}.
     *
     * @param ctx the owning {@link WebappContext}.
     * @param name the name of the Filter.
     * @param filterClassName the fully qualified class name of the {@link Filter} implementation.
     */
    public MicrowebFilterRegistration(final MicrowebServletContext ctx, final String name, final String filterClassName)
    {
        
        super(ctx, name, filterClassName);
        initParameters = new HashMap<String, String>(4, 1.0f);
        
    }
    
    /**
     * Creates a new FilterRegistration associated with the specified {@link WebappContext}.
     *
     * @param ctx the owning {@link WebappContext}.
     * @param name name the name of the Filter.
     * @param filter the class of the {@link Filter} implementation
     */
    public MicrowebFilterRegistration(final MicrowebServletContext ctx, final String name,
        final Class<? extends Filter> filter)
    {
        this(ctx, name, filter.getName());
        this.filterClass = filter;
        
    }
    
    /**
     * Creates a new FilterRegistration associated with the specified {@link WebappContext}.
     *
     * @param ctx the owning {@link WebappContext}.
     * @param name name the name of the Filter.
     * @param filter the {@link Filter} instance.
     */
    public MicrowebFilterRegistration(final MicrowebServletContext ctx, final String name, final Filter filter)
    {
        this(ctx, name, filter.getClass());
        this.filter = filter;
    }
    
    // ---------------------------------------------------------- Public Methods
    
    /**
     * Adds a filter mapping with the given servlet names and dispatcher types for the Filter represented by this
     * FilterRegistration.
     *
     * <p>
     * Filter mappings are matched in the order in which they were added.
     *
     * <p>
     * If this method is called multiple times, each successive call adds to the effects of the former.
     *
     * @param dispatcherTypes the dispatcher types of the filter mapping, or null if the default
     *            <tt>DispatcherType.REQUEST</tt> is to be used
     * @param servletNames the servlet names of the filter mapping
     *
     * @throws IllegalArgumentException if <tt>servletNames</tt> is null or empty
     * @throws IllegalStateException if the ServletContext from which this FilterRegistration was obtained has already
     *             been initialized
     */
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, String... servletNames)
    {
        addMappingForServletNames(dispatcherTypes, true, servletNames);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... servletNames)
    {
        if (ctx.deployed)
        {
            throw new IllegalStateException("WebappContext has already been deployed");
        }
        
        if ((servletNames == null) || (servletNames.length == 0))
        {
            throw new IllegalArgumentException("'servletNames' is null or zero-length");
        }
        
        for (String servletName : servletNames)
        {
            FilterMap fmap = new FilterMap();
            fmap.setFilterName(getName());
            fmap.setServletName(servletName);
            fmap.setDispatcherTypes(dispatcherTypes);
            
            ctx.addFilterMap(fmap, isMatchAfter);
        }
    }
    
    /**
     * Gets the currently available servlet name mappings of the Filter represented by this
     * <code>FilterRegistration</code>.
     *
     * <p>
     * If permitted, any changes to the returned <code>Collection</code> must not affect this
     * <code>FilterRegistration</code>.
     *
     * @return a (possibly empty) <code>Collection</code> of the currently available servlet name mappings of the Filter
     *         represented by this <code>FilterRegistration</code>
     */
    @Override
    public Collection<String> getServletNameMappings()
    {
        return ctx.getServletNameFilterMappings(getName());
    }
    
    /**
     * Adds a filter mapping with the given url patterns and dispatcher types for the Filter represented by this
     * FilterRegistration.
     *
     * <p>
     * Filter mappings are matched in the order in which they were added.
     *
     * <p>
     * If this method is called multiple times, each successive call adds to the effects of the former.
     *
     * @param dispatcherTypes the dispatcher types of the filter mapping, or null if the default
     *            <tt>DispatcherType.REQUEST</tt> is to be used
     * @param urlPatterns the url patterns of the filter mapping
     *
     * @throws IllegalArgumentException if <tt>urlPatterns</tt> is null or empty
     * @throws IllegalStateException if the ServletContext from which this FilterRegistration was obtained has already
     *             been initialized
     */
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, String... urlPatterns)
    {
        addMappingForUrlPatterns(dispatcherTypes, true, urlPatterns);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... urlPatterns)
    {
        // if (ctx.deployed) {
        // throw new IllegalStateException("WebappContext has already been deployed");
        // }
        
        if ((urlPatterns == null) || (urlPatterns.length == 0))
        {
            throw new IllegalArgumentException("'urlPatterns' is null or zero-length");
        }
        
        for (String urlPattern : urlPatterns)
        {
            FilterMap fmap = new FilterMap();
            fmap.setFilterName(getName());
            fmap.setURLPattern(urlPattern);
            fmap.setDispatcherTypes(dispatcherTypes);
            
            ctx.addFilterMap(fmap, isMatchAfter);
        }
    }
    
    /**
     * Gets the currently available URL pattern mappings of the Filter represented by this
     * <code>FilterRegistration</code>.
     *
     * <p>
     * If permitted, any changes to the returned <code>Collection</code> must not affect this
     * <code>FilterRegistration</code>.
     *
     * @return a (possibly empty) <code>Collection</code> of the currently available URL pattern mappings of the Filter
     *         represented by this <code>FilterRegistration</code>
     */
    @Override
    public Collection<String> getUrlPatternMappings()
    {
        return ctx.getUrlPatternFilterMappings(getName());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAsyncSupported(boolean isAsyncSupported)
    {
        this.isAsyncSupported = isAsyncSupported;
    }
}
