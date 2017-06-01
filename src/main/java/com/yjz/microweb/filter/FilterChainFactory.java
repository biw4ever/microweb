package com.yjz.microweb.filter;

import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;

import com.yjz.microweb.context.MicrowebServletContext;
import com.yjz.microweb.http.FullHttpRequestWrapper;
import com.yjz.microweb.util.Globals;

public class FilterChainFactory
{
    private final MicrowebServletContext ctx;
    
    // ------------------------------------------------------------ Constructors
    
    public FilterChainFactory(final MicrowebServletContext ctx)
    {
        
        this.ctx = ctx;
        // this.registrations = registrations;
        
    }
    
    // ---------------------------------------------------------- Public Methods
    
    /**
     * Construct and return a FilterChain implementation that will wrap the execution of the specified servlet instance.
     * If we should not execute a filter chain at all, return <code>null</code>.
     *
     * @param request The servlet request we are processing
     * @param servlet The servlet instance to be wrapped
     */
    public MicrowebFilterChain createFilterChain(final ServletRequest request, final Servlet servlet,
        final DispatcherType dispatcherType)
    {
        
        return buildFilterChain(servlet, getRequestPath(request), dispatcherType);
        
    }
    
    // -------------------------------------------------------- Private Methods
    private MicrowebFilterChain buildFilterChain(final Servlet servlet, final String requestPath,
        final DispatcherType dispatcherType)
    {
        // If there is no servlet to execute, return null
        if (servlet == null)
        {
            return (null);
        }
        
        // Create and initialize a filter chain object
        MicrowebFilterChain filterChain = new MicrowebFilterChain(servlet, ctx);
        
        final Map<String, ? extends MicrowebFilterRegistration> registrations = ctx.getFilterRegistrations();
        
        // If there are no filter mappings, we are done
        if (registrations.isEmpty())
        {
            return filterChain;
        }
        
        final List<FilterMap> filterMaps = ctx.getFilterMaps();
        
        // Add the relevant path-mapped filters to this filter chain
        for (final FilterMap filterMap : filterMaps)
        {
            if (!filterMap.getDispatcherTypes().contains(dispatcherType))
            {
                continue;
            }
            
            if (!matchFiltersURL(filterMap, requestPath))
            {
                continue;
            }
            
            filterChain.addFilter(registrations.get(filterMap.getFilterName()));
        }
        
        // Add filters that match on servlet name second
        String servletName = servlet.getServletConfig().getServletName();
        for (final FilterMap filterMap : filterMaps)
        {
            if (!filterMap.getDispatcherTypes().contains(dispatcherType))
            {
                continue;
            }
            
            if (!matchFiltersServlet(filterMap, servletName))
            {
                continue;
            }
            
            filterChain.addFilter(registrations.get(filterMap.getFilterName()));
        }
        
        // Return the completed filter chain
        return filterChain;
    }
    
    private String getRequestPath(ServletRequest request)
    {
        // get the dispatcher type
        String requestPath = null;
        Object attribute = request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);
        if (attribute != null)
        {
            requestPath = attribute.toString();
        }
        return requestPath;
    }
    
    private String getRequestPath(FullHttpRequestWrapper request)
    {
        // get the dispatcher type
        String requestPath = null;
        Object attribute = request.getAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR);
        if (attribute != null)
        {
            requestPath = attribute.toString();
        }
        return requestPath;
    }
    
    /**
     * Return <code>true</code> if the context-relative request path matches the requirements of the specified filter
     * mapping; otherwise, return <code>null</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param requestPath Context-relative request path of this request
     */
    /*
     * SJSWS 6324431 private boolean matchFiltersURL(FilterMap filterMap, String requestPath) {
     */
    // START SJSWS 6324431
    private boolean matchFiltersURL(FilterMap filterMap, String requestPath)
    {
        // END SJSWS 6324431
        
        if (requestPath == null)
        {
            return false;
        }
        
        // Match on context relative request path
        String testPath = filterMap.getURLPattern();
        if (testPath == null)
        {
            return false;
        }
        
        // Case 1 - Exact Match
        if (testPath.equals(requestPath))
        {
            return true;
        }
        
        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*"))
        {
            return true;
        }
        if (testPath.endsWith("/*"))
        {
            if (testPath.regionMatches(0, requestPath, 0, testPath.length() - 2))
            {
                if (requestPath.length() == (testPath.length() - 2))
                {
                    return true;
                }
                else if ('/' == requestPath.charAt(testPath.length() - 2))
                {
                    return true;
                }
            }
            return false;
        }
        
        // Case 3 - Extension Match
        if (testPath.startsWith("*."))
        {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) && (period != requestPath.length() - 1)
                && ((requestPath.length() - period) == (testPath.length() - 1)))
            {
                return (testPath.regionMatches(2, requestPath, period + 1, testPath.length() - 2));
            }
        }
        
        // Case 4 - "Default" Match
        return false; // NOTE - Not relevant for selecting filters
        
    }
    
    /**
     * Return <code>true</code> if the specified servlet name matches the requirements of the specified filter mapping;
     * otherwise return <code>false</code>.
     *
     * @param filterMap Filter mapping being checked
     * @param servletName Servlet name being checked
     */
    private boolean matchFiltersServlet(FilterMap filterMap, String servletName)
    {
        
        if (servletName == null)
        {
            return false;
        }
        else
        {
            if (servletName.equals(filterMap.getServletName()) || "*".equals(filterMap.getServletName()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
}
