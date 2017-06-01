package com.yjz.microweb.filter;

import java.io.IOException;
import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yjz.microweb.context.MicrowebServletContext;

public class MicrowebFilterChain implements FilterChain, FilterChainInvoker
{
    
    private static Logger LOGGER = LoggerFactory.getLogger(MicrowebFilterChain.class);
    
    /**
     * The servlet instance to be executed by this chain.
     */
    private final Servlet servlet;
    
    private final MicrowebServletContext ctx;
    
    private final Object lock = new Object();
    
    private int n;
    
    private MicrowebFilterRegistration[] filters = new MicrowebFilterRegistration[0];
    
    /**
     * The int which is used to maintain the current position in the filter chain.
     */
    private int pos;
    
    public MicrowebFilterChain(final Servlet servlet, final MicrowebServletContext ctx)
    {
        
        this.servlet = servlet;
        this.ctx = ctx;
    }
    
    // ---------------------------------------------------- FilterChain Methods
    
    public void invokeFilterChain(ServletRequest request, ServletResponse response)
        throws IOException, ServletException
    {
        
        ServletRequestEvent event = new ServletRequestEvent(ctx, request);
        try
        {
            requestInitialized(event);
            pos = 0;
            doFilter(request, response);
        }
        finally
        {
            requestDestroyed(event);
        }
        
    }
    
    /**
     * Invoke the next filter in this chain, passing the specified request and response. If there are no more filters in
     * this chain, invoke the <code>service()</code> method of the servlet itself.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception java.io.IOException if an input/output error occurs
     * @exception javax.servlet.ServletException if a servlet exception occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException
    {
        
        // Call the next filter if there is one
        if (pos < n)
        {
            
            MicrowebFilterRegistration registration = filters[pos++];
            
            try
            {
                Filter filter = registration.filter;
                filter.doFilter(request, response, this);
            }
            catch (Exception e)
            {
                throw new ServletException(e);
            }
            
            return;
        }
        
        try
        {
            if (servlet != null)
            {
                servlet.service(request, response);
            }
            
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
        
    }
    
    // ------------------------------------------------------- Protected Methods
    
    protected void addFilter(final MicrowebFilterRegistration filterRegistration)
    {
        synchronized (lock)
        {
            if (n == filters.length)
            {
                MicrowebFilterRegistration[] newFilters = new MicrowebFilterRegistration[n + 4];
                System.arraycopy(filters, 0, newFilters, 0, n);
                filters = newFilters;
            }
            
            filters[n++] = filterRegistration;
        }
    }
    
    // --------------------------------------------------------- Private Methods
    
    private void requestDestroyed(ServletRequestEvent event)
    {
        // TODO don't create the event unless necessary
        final EventListener[] listeners = ctx.getEventListeners();
        for (int i = 0, len = listeners.length; i < len; i++)
        {
            if (listeners[i] instanceof ServletRequestListener)
            {
                try
                {
                    ((ServletRequestListener)listeners[i]).requestDestroyed(event);
                }
                catch (Throwable t)
                {
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }
    }
    
    private void requestInitialized(ServletRequestEvent event)
    {
        final EventListener[] listeners = ctx.getEventListeners();
        for (int i = 0, len = listeners.length; i < len; i++)
        {
            if (listeners[i] instanceof ServletRequestListener)
            {
                try
                {
                    ((ServletRequestListener)listeners[i]).requestInitialized(event);
                }
                catch (Throwable t)
                {
                    LOGGER.error(t.getMessage(), t);
                }
            }
        }
    }
}
