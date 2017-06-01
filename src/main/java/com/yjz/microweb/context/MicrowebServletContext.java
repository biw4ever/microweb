package com.yjz.microweb.context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.SingleThreadModel;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpUpgradeHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yjz.microweb.filter.FilterChainFactory;
import com.yjz.microweb.filter.FilterMap;
import com.yjz.microweb.filter.MicrowebFilterConfig;
import com.yjz.microweb.filter.MicrowebFilterRegistration;
import com.yjz.microweb.http.MicrowebRequestDispatcher;
import com.yjz.microweb.servlet.MicrowebServletConfig;
import com.yjz.microweb.servlet.MicrowebServletRegistration;
import com.yjz.microweb.session.MicrowebSessionCookieConfig;
import com.yjz.microweb.util.ClassLoaderUtil;
import com.yjz.microweb.util.Enumerator;
import com.yjz.microweb.util.MimeType;

public class MicrowebServletContext implements ServletContext
{
    private static Logger logger = LoggerFactory.getLogger(MicrowebServletContext.class);
    
    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES =
        EnumSet.of(SessionTrackingMode.COOKIE);
    
    /* Servlet major/minor versions */
    private static final int MAJOR_VERSION = 3;
    
    private static final int MINOR_VERSION = 1;
    
    /* Logical name, path spec, and filesystem path of this application */
    private final String displayName;
    
    private final String contextPath;
    
    private final String basePath;
    
    /* Servlet context initialization parameters */
    private final Map<String, String> contextInitParams = new LinkedHashMap<String, String>(8, 1.0f);
    
    /**
     * The security roles for this application
     */
    private final List<String> securityRoles = new ArrayList<String>();
    
    /* Registrations */
    protected final Map<String, MicrowebServletRegistration> servletRegistrations =
        new HashMap<String, MicrowebServletRegistration>(8, 1.0f);
    
    protected final Map<String, MicrowebFilterRegistration> filterRegistrations =
        new LinkedHashMap<String, MicrowebFilterRegistration>(4, 1.0f);
    
    protected final Map<String, MicrowebFilterRegistration> unmodifiableFilterRegistrations =
        Collections.unmodifiableMap(filterRegistrations);
    
    /* Listeners */
    private final Set<EventListener> eventListenerInstances = new LinkedHashSet<EventListener>(4, 1.0f); // TODO - wire
                                                                                                         // this in
    
    private EventListener[] eventListeners = new EventListener[0];
    
    /* Application start/stop state */
    public boolean deployed = false;
    
    /* Factory for creating FilterChainImpl instances */
    final private FilterChainFactory filterChainFactory;
    
    /* Servlet context attributes */
    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
    
    /* Server name; used in the Server entity header */
    private volatile String serverInfo = "SlimHttpServer";
    
    private ClassLoader webappClassLoader;
    
    /**
     * Session cookie config
     */
    private SessionCookieConfig sessionCookieConfig;
    
    private Set<SessionTrackingMode> sessionTrackingModes;
    
    /**
     * The list of filter mappings for this application, in the order they were defined in the deployment descriptor.
     */
    private final List<FilterMap> filterMaps = new ArrayList<FilterMap>();
    
    // ------------------------------------------------------------ Constructors
    
    protected MicrowebServletContext()
    {
        displayName = "";
        contextPath = "";
        basePath = "";
        filterChainFactory = new FilterChainFactory(this);
    }
    
    /**
     * <p>
     * Creates a simple <code>WebappContext</code> with the root being "/".
     * </p>
     *
     * @param displayName
     */
    public MicrowebServletContext(final String displayName)
    {
        this(displayName, "");
    }
    
    public MicrowebServletContext(final String displayName, final String contextPath)
    {
        this(displayName, contextPath, ".");
    }
    
    public MicrowebServletContext(final String displayName, final String contextPath, final String basePath)
    {
        
        if (displayName == null || displayName.length() == 0)
        {
            throw new IllegalArgumentException("'displayName' cannot be null or zero-length");
        }
        if (contextPath == null)
        {
            throw new IllegalArgumentException("'contextPath' cannot be null");
        }
        if (contextPath.length() > 0)
        {
            if (contextPath.charAt(0) != '/')
            {
                throw new IllegalArgumentException("'contextPath' must start with a forward slash");
            }
            if (!contextPath.equals("/"))
            {
                if (contextPath.charAt(contextPath.length() - 1) == '/')
                {
                    throw new IllegalArgumentException("'contextPath' must not end with a forward slash");
                }
            }
        }
        this.displayName = displayName;
        this.contextPath = contextPath;
        try
        {
            this.basePath = new File(basePath).getCanonicalPath();
        }
        catch (IOException ioe)
        {
            throw new IllegalArgumentException("Unable to resolve path: " + basePath);
        }
        filterChainFactory = new FilterChainFactory(this);
        
    }
    
    // ---------------------------------------------------------- Public Methods
    
    /**
     *
     * @param name
     * @param value
     */
    public void addContextInitParameter(final String name, final String value)
    {
        if (!deployed)
        {
            contextInitParams.put(name, value);
        }
        
    }
    
    /**
     *
     * @param name
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void removeContextInitParameter(final String name)
    {
        
        if (!deployed)
        {
            contextInitParams.remove(name);
        }
        
    }
    
    /**
     *
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void clearContextInitParameters()
    {
        
        if (!deployed)
        {
            contextInitParams.clear();
        }
        
    }
    
    // --------------------------------------------- Methods from ServletContext
    
    /**
     * Adds the filter with the given name and class type to this servlet context.
     *
     * <p>
     * The registered filter may be further configured via the returned {@link FilterRegistration} object.
     *
     * <p>
     * If this WebappContext already contains a preliminary FilterRegistration for a filter with the given
     * <tt>filterName</tt>, it will be completed (by assigning the name of the given <tt>filterClass</tt> to it) and
     * returned.
     *
     * @param filterName the name of the filter
     * @param filterClass the class object from which the filter will be instantiated
     *
     * @return a FilterRegistration object that may be used to further configure the registered filter, or <tt>null</tt>
     *         if this WebappContext already contains a complete FilterRegistration for a filter with the given
     *         <tt>filterName</tt>
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     */
    @Override
    public MicrowebFilterRegistration addFilter(final String filterName, final Class<? extends Filter> filterClass)
    {
        
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        if (filterName == null)
        {
            throw new IllegalArgumentException("'filterName' cannot be null");
        }
        if (filterClass == null)
        {
            throw new IllegalArgumentException("'filterClass' cannot be null");
        }
        
        MicrowebFilterRegistration registration = filterRegistrations.get(filterName);
        if (registration == null)
        {
            registration = new MicrowebFilterRegistration(this, filterName, filterClass);
            filterRegistrations.put(filterName, registration);
        }
        else
        {
            if (registration.getFilterClass() != filterClass)
            {
                registration.setFilter(null);
                registration.setFilterClass(filterClass);
                registration.setClassName(filterClass.getName());
            }
        }
        return registration;
    }
    
    /**
     * Registers the given filter instance with this WebappContext under the given <tt>filterName</tt>.
     *
     * <p>
     * The registered filter may be further configured via the returned {@link FilterRegistration} object.
     *
     * <p>
     * If this WebappContext already contains a preliminary FilterRegistration for a filter with the given
     * <tt>filterName</tt>, it will be completed (by assigning the class name of the given filter instance to it) and
     * returned.
     *
     * @param filterName the name of the filter
     * @param filter the filter instance to register
     *
     * @return a FilterRegistration object that may be used to further configure the given filter, or <tt>null</tt> if
     *         this WebappContext already contains a complete FilterRegistration for a filter with the given
     *         <tt>filterName</tt> or if the same filter instance has already been registered with this or another
     *         WebappContext in the same container
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     *
     * @since Servlet 3.0
     */
    @Override
    public MicrowebFilterRegistration addFilter(final String filterName, final Filter filter)
    {
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        if (filterName == null)
        {
            throw new IllegalArgumentException("'filterName' cannot be null");
        }
        if (filter == null)
        {
            throw new IllegalArgumentException("'filter' cannot be null");
        }
        
        MicrowebFilterRegistration registration = filterRegistrations.get(filterName);
        if (registration == null)
        {
            registration = new MicrowebFilterRegistration(this, filterName, filter);
            filterRegistrations.put(filterName, registration);
        }
        else
        {
            if (registration.getFilter() != filter)
            {
                registration.setFilter(filter);
                registration.setFilterClass(filter.getClass());
                registration.setClassName(filter.getClass().getName());
            }
        }
        return registration;
    }
    
    /**
     * Adds the filter with the given name and class name to this servlet context.
     *
     * <p>
     * The registered filter may be further configured via the returned {@link FilterRegistration} object.
     *
     * <p>
     * The specified <tt>className</tt> will be loaded using the classloader associated with the application represented
     * by this WebappContext.
     *
     * <p>
     * If this WebappContext already contains a preliminary FilterRegistration for a filter with the given
     * <tt>filterName</tt>, it will be completed (by assigning the given <tt>className</tt> to it) and returned.
     *
     * @param filterName the name of the filter
     * @param className the fully qualified class name of the filter
     *
     * @return a FilterRegistration object that may be used to further configure the registered filter, or <tt>null</tt>
     *         if this WebappContext already contains a complete FilterRegistration for a filter with the given
     *         <tt>filterName</tt>
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     */
    @Override
    public MicrowebFilterRegistration addFilter(final String filterName, final String className)
    {
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        if (filterName == null)
        {
            throw new IllegalArgumentException("'filterName' cannot be null");
        }
        if (className == null)
        {
            throw new IllegalArgumentException("'className' cannot be null");
        }
        
        MicrowebFilterRegistration registration = filterRegistrations.get(filterName);
        if (registration == null)
        {
            registration = new MicrowebFilterRegistration(this, filterName, className);
            filterRegistrations.put(filterName, registration);
        }
        else
        {
            if (!registration.getClassName().equals(className))
            {
                registration.setClassName(className);
                registration.setFilterClass(null);
                registration.setFilter(null);
            }
        }
        return registration;
    }
    
    /**
     * Adds the servlet with the given name and class type to this servlet context.
     *
     * <p>
     * The registered servlet may be further configured via the returned {@link ServletRegistration} object.
     *
     * <p>
     * If this WebappContext already contains a preliminary ServletRegistration for a servlet with the given
     * <tt>servletName</tt>, it will be completed (by assigning the name of the given <tt>servletClass</tt> to it) and
     * returned.
     *
     *
     * @param servletName the name of the servlet
     * @param servletClass the class object from which the servlet will be instantiated
     *
     * @return a ServletRegistration object that may be used to further configure the registered servlet, or
     *         <tt>null</tt> if this WebappContext already contains a complete ServletRegistration for the given
     *         <tt>servletName</tt>
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     *
     */
    @Override
    public MicrowebServletRegistration addServlet(final String servletName, final Class<? extends Servlet> servletClass)
    {
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        if (servletName == null)
        {
            throw new IllegalArgumentException("'servletName' cannot be null");
        }
        if (servletClass == null)
        {
            throw new IllegalArgumentException("'servletClass' cannot be null");
        }
        
        MicrowebServletRegistration registration = servletRegistrations.get(servletName);
        if (registration == null)
        {
            registration = new MicrowebServletRegistration(this, servletName, servletClass);
            servletRegistrations.put(servletName, registration);
        }
        else
        {
            if (registration.getServletClass() != servletClass)
            {
                registration.setServlet(null);
                registration.setServletClass(servletClass);
                registration.setClassName(servletClass.getName());
            }
        }
        return registration;
    }
    
    /**
     * Registers the given servlet instance with this WebappContext under the given <tt>servletName</tt>.
     *
     * <p>
     * The registered servlet may be further configured via the returned {@link ServletRegistration} object.
     *
     * <p>
     * If this WebappContext already contains a preliminary ServletRegistration for a servlet with the given
     * <tt>servletName</tt>, it will be completed (by assigning the class name of the given servlet instance to it) and
     * returned.
     *
     * @param servletName the name of the servlet
     * @param servlet the servlet instance to register
     *
     * @return a ServletRegistration object that may be used to further configure the given servlet, or <tt>null</tt> if
     *         this WebappContext already contains a complete ServletRegistration for a servlet with the given
     *         <tt>servletName</tt> or if the same servlet instance has already been registered with this or another
     *         WebappContext in the same container
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     *
     * @throws IllegalArgumentException if the given servlet instance implements {@link javax.servlet.SingleThreadModel}
     */
    @SuppressWarnings({"deprecation"})
    @Override
    public MicrowebServletRegistration addServlet(final String servletName, final Servlet servlet)
    {
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        if (servletName == null)
        {
            throw new IllegalArgumentException("'servletName' cannot be null");
        }
        if (servlet == null)
        {
            throw new IllegalArgumentException("'servlet' cannot be null");
        }
        if (servlet instanceof SingleThreadModel)
        {
            throw new IllegalArgumentException("SingleThreadModel Servlet instances are not allowed.");
        }
        
        MicrowebServletRegistration registration = servletRegistrations.get(servletName);
        if (registration == null)
        {
            registration = new MicrowebServletRegistration(this, servletName, servlet);
            servletRegistrations.put(servletName, registration);
        }
        else
        {
            if (registration.getServlet() != servlet)
            {
                registration.setServlet(servlet);
                registration.setServletClass(servlet.getClass());
                registration.setClassName(servlet.getClass().getName());
            }
        }
        return registration;
    }
    
    /**
     * Adds the servlet with the given name and class name to this servlet context.
     *
     * <p>
     * The registered servlet may be further configured via the returned {@link ServletRegistration} object.
     *
     * <p>
     * The specified <tt>className</tt> will be loaded using the classloader associated with the application represented
     * by this WebappContext.
     *
     * <p>
     * If this WebappContext already contains a preliminary ServletRegistration for a servlet with the given
     * <tt>servletName</tt>, it will be completed (by assigning the given <tt>className</tt> to it) and returned.
     *
     *
     * @param servletName the name of the servlet
     * @param className the fully qualified class name of the servlet
     *
     * @return a ServletRegistration object that may be used to further configure the registered servlet, or
     *         <tt>null</tt> if this WebappContext already contains a complete ServletRegistration for a servlet with
     *         the given <tt>servletName</tt>
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     */
    @Override
    public MicrowebServletRegistration addServlet(final String servletName, final String className)
    {
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        if (servletName == null)
        {
            throw new IllegalArgumentException("'servletName' cannot be null");
        }
        if (className == null)
        {
            throw new IllegalArgumentException("'className' cannot be null");
        }
        
        MicrowebServletRegistration registration = servletRegistrations.get(servletName);
        if (registration == null)
        {
            registration = new MicrowebServletRegistration(this, servletName, className);
            servletRegistrations.put(servletName, registration);
        }
        else
        {
            if (!registration.getClassName().equals(className))
            {
                registration.setServlet(null);
                registration.setServletClass(null);
                registration.setClassName(className);
            }
        }
        return registration;
    }
    
    /**
     * Gets the FilterRegistration corresponding to the filter with the given <tt>filterName</tt>.
     *
     * @return the (complete or preliminary) FilterRegistration for the filter with the given <tt>filterName</tt>, or
     *         null if no FilterRegistration exists under that name
     */
    @Override
    public FilterRegistration getFilterRegistration(final String name)
    {
        if (name == null)
        {
            return null;
        }
        return filterRegistrations.get(name);
    }
    
    /**
     * Gets a (possibly empty) Map of the FilterRegistration objects (keyed by filter name) corresponding to all filters
     * registered with this WebappContext.
     *
     * <p>
     * The returned Map includes the FilterRegistration objects corresponding to all declared and annotated filters, as
     * well as the FilterRegistration objects corresponding to all filters that have been added via one of the
     * <tt>addFilter</tt> methods.
     *
     * <p>
     * Any changes to the returned Map must not affect this WebappContext.
     *
     * @return Map of the (complete and preliminary) FilterRegistration objects corresponding to all filters currently
     *         registered with this WebappContext
     */
    @Override
    public Map<String, ? extends MicrowebFilterRegistration> getFilterRegistrations()
    {
        return unmodifiableFilterRegistrations;
    }
    
    /**
     * Gets the ServletRegistration corresponding to the servlet with the given <tt>servletName</tt>.
     *
     * @return the (complete or preliminary) ServletRegistration for the servlet with the given <tt>servletName</tt>, or
     *         null if no ServletRegistration exists under that name
     */
    @Override
    public ServletRegistration getServletRegistration(final String name)
    {
        if (name == null)
        {
            return null;
        }
        return servletRegistrations.get(name);
    }
    
    /**
     * Gets a (possibly empty) Map of the ServletRegistration objects (keyed by servlet name) corresponding to all
     * servlets registered with this WebappContext.
     *
     * <p>
     * The returned Map includes the ServletRegistration objects corresponding to all declared and annotated servlets,
     * as well as the ServletRegistration objects corresponding to all servlets that have been added via one of the
     * <tt>addServlet</tt> methods.
     *
     * <p>
     * If permitted, any changes to the returned Map must not affect this WebappContext.
     *
     * @return Map of the (complete and preliminary) ServletRegistration objects corresponding to all servlets currently
     *         registered with this WebappContext
     *
     * @since Servlet 3.0
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations()
    {
        return Collections.unmodifiableMap(servletRegistrations);
    }
    
    /**
     * Adds the given listener class to this WebappContext.
     *
     * <p>
     * The given listener must be an instance of one or more of the following interfaces:
     * <ul>
     * <li>{@link ServletContextAttributeListener}</tt>
     * <li>{@link javax.servlet.ServletRequestListener}</tt>
     * <li>{@link javax.servlet.ServletRequestAttributeListener}</tt>
     * <li>{@link javax.servlet.http.HttpSessionListener}</tt>
     * <li>{@link javax.servlet.http.HttpSessionAttributeListener}</tt>
     * </ul>
     *
     * <p>
     * If the given listener is an instance of a listener interface whose invocation order corresponds to the
     * declaration order (i.e., if it is an instance of {@link javax.servlet.ServletRequestListener},
     * {@link ServletContextListener}, or {@link javax.servlet.http.HttpSessionListener}), then the listener will be
     * added to the end of the ordered list of listeners of that interface.
     *
     * @throws IllegalArgumentException if the given listener is not an instance of any of the above interfaces
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     */
    @Override
    public void addListener(final Class<? extends EventListener> listenerClass)
    {
        if (deployed)
        {
            throw new IllegalStateException("WebappContext has already been deployed");
        }
        
        if (listenerClass == null)
        {
            throw new IllegalArgumentException("'listener' cannot be null");
        }
        
        try
        {
            addListener(createEventListenerInstance(listenerClass));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Adds the listener with the given class name to this WebappContext.
     *
     * <p>
     * The class with the given name will be loaded using the classloader associated with the application represented by
     * this WebappContext, and must implement one or more of the following interfaces:
     * <ul>
     * <li>{@link ServletContextAttributeListener}</tt>
     * <li>{@link javax.servlet.ServletRequestListener}</tt>
     * <li>{@link javax.servlet.ServletRequestAttributeListener}</tt>
     * <li>{@link javax.servlet.http.HttpSessionListener}</tt>
     * <li>{@link javax.servlet.http.HttpSessionAttributeListener}</tt>
     * </ul>
     *
     * <p>
     * As part of this method call, the container must load the class with the specified class name to ensure that it
     * implements one of the required interfaces.
     *
     * <p>
     * If the class with the given name implements a listener interface whose invocation order corresponds to the
     * declaration order (i.e., if it implements {@link javax.servlet.ServletRequestListener},
     * {@link ServletContextListener}, or {@link javax.servlet.http.HttpSessionListener}), then the new listener will be
     * added to the end of the ordered list of listeners of that interface.
     *
     * @param className the fully qualified class name of the listener
     *
     * @throws IllegalArgumentException if the class with the given name does not implement any of the above interfaces
     *
     * @throws IllegalStateException if this WebappContext has already been initialized
     */
    @Override
    public void addListener(String className)
    {
        if (deployed)
        {
            throw new IllegalStateException("WebappContext has already been deployed");
        }
        
        if (className == null)
        {
            throw new IllegalArgumentException("'className' cannot be null");
        }
        
        try
        {
            addListener(createEventListenerInstance(className));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends EventListener> void addListener(T eventListener)
    {
        if (deployed)
        {
            throw new IllegalStateException("WebappContext has already been deployed");
        }
        
        eventListenerInstances.add(eventListener);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz)
        throws ServletException
    {
        try
        {
            return (T)createServletInstance(clazz);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Filter> T createFilter(Class<T> clazz)
        throws ServletException
    {
        try
        {
            return (T)createFilterInstance(clazz);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends EventListener> T createListener(Class<T> clazz)
        throws ServletException
    {
        try
        {
            return (T)createEventListenerInstance(clazz);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    
    @Override
    public void declareRoles(String... roleNames)
    {
        if (deployed)
        {
            throw new IllegalStateException("WebappContext has already been deployed");
        }
        
        securityRoles.addAll(Arrays.asList(roleNames));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getContextPath()
    {
        return contextPath;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ServletContext getContext(String uri)
    {
        // Validate the format of the specified argument
        if (uri == null || !uri.startsWith("/"))
        {
            return null;
        }
        
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectiveMajorVersion()
    {
        return MAJOR_VERSION;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getEffectiveMinorVersion()
    {
        return MINOR_VERSION;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getMimeType(String file)
    {
        if (file == null)
        {
            return (null);
        }
        int period = file.lastIndexOf(".");
        if (period < 0)
        {
            return (null);
        }
        String extension = file.substring(period + 1);
        if (extension.length() < 1)
        {
            return (null);
        }
        return MimeType.get(extension);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getResourcePaths(String path)
    {
        // Validate the path argument
        if (path == null)
        {
            return null;
        }
        if (!path.startsWith("/"))
        {
            throw new IllegalArgumentException(path);
        }
        
        path = normalize(path);
        if (path == null)
        {
            return (null);
        }
        
        File[] files = new File(basePath, path).listFiles();
        
        Set<String> set = Collections.emptySet();
        if (files != null)
        {
            set = new HashSet<String>(files.length);
            for (File f : files)
            {
                try
                {
                    String canonicalPath = f.getCanonicalPath();
                    
                    // add a trailing "/" if a folder
                    if (f.isDirectory())
                    {
                        canonicalPath = canonicalPath + "/";
                    }
                    
                    canonicalPath = canonicalPath.substring(canonicalPath.indexOf(basePath) + basePath.length());
                    set.add(canonicalPath.replace("\\", "/"));
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
        return set;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(String path)
        throws MalformedURLException
    {
        if (path == null || !path.startsWith("/"))
        {
            throw new MalformedURLException(path);
        }
        
        path = normalize(path);
        if (path == null)
        {
            return (null);
        }
        
        // Help the UrlClassLoader, which is not able to load resources
        // that contains '//'
        if (path.length() > 1)
        {
            path = path.substring(1);
        }
        
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getResourceAsStream(String path)
    {
        String pathLocal = normalize(path);
        if (pathLocal == null)
        {
            return (null);
        }
        
        // Help the UrlClassLoader, which is not able to load resources
        // that contains '//'
        if (pathLocal.length() > 1)
        {
            pathLocal = pathLocal.substring(1);
        }
        
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathLocal);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        // Validate the path argument
        if (path == null)
        {
            return null;
        }
        
        if (!path.startsWith("/") && !path.isEmpty())
        {
            throw new IllegalArgumentException("Path " + path + " does not start with ''/'' and is not empty");
        }
        
        // // Get query string
        // String queryString = null;
        // int pos = path.indexOf('?');
        // if (pos >= 0)
        // {
        // queryString = path.substring(pos + 1);
        // path = path.substring(0, pos);
        // }
        // path = normalize(path);
        // if (path == null)
        // return null;
        
        return new MicrowebRequestDispatcher(path);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestDispatcher getNamedDispatcher(String name)
    {
        return new MicrowebRequestDispatcher(name);
    }
    
    /**
     * {@inheritDoc}
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public Servlet getServlet(String name)
        throws ServletException
    {
        return null;
    }
    
    /**
     * {@inheritDoc}
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets()
    {
        return new Enumerator<Servlet>(Collections.<Servlet> emptyList());
    }
    
    /**
     *
     * {@inheritDoc}
     * 
     * @deprecated
     */
    @Override
    @Deprecated
    public Enumeration<String> getServletNames()
    {
        return new Enumerator<String>(Collections.<String> emptyList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String message)
    {
        logger.debug(message);
    }
    
    /**
     * {@inheritDoc}
     *
     * @deprecated
     */
    @Override
    @Deprecated
    public void log(Exception e, String message)
    {
        log(message, e);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void log(String message, Throwable throwable)
    {
        logger.error(message, throwable);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealPath(String path)
    {
        if (path == null)
        {
            return null;
        }
        
        return new File(basePath, path).getAbsolutePath();
    }
    
    @Override
    public String getVirtualServerName()
    {
        return "server";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerInfo()
    {
        return serverInfo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getInitParameter(String name)
    {
        return contextInitParams.get(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return new Enumerator<String>(contextInitParams.keySet());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setInitParameter(String name, String value)
    {
        if (!deployed)
        {
            contextInitParams.put(name, value);
            return true;
        }
        
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<String> getAttributeNames()
    {
        return new Enumerator<String>(attributes.keySet());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, Object value)
    {
        // Name cannot be null
        if (name == null)
        {
            throw new IllegalArgumentException("Cannot be null");
        }
        
        // Null value is the same as removeAttribute()
        if (value == null)
        {
            removeAttribute(name);
            return;
        }
        
        Object oldValue = attributes.put(name, value);
        
        ServletContextAttributeEvent event = null;
        for (int i = 0, len = eventListeners.length; i < len; i++)
        {
            if (!(eventListeners[i] instanceof ServletContextAttributeListener))
            {
                continue;
            }
            ServletContextAttributeListener listener = (ServletContextAttributeListener)eventListeners[i];
            try
            {
                if (event == null)
                {
                    if (oldValue != null)
                    {
                        event = new ServletContextAttributeEvent(this, name, oldValue);
                    }
                    else
                    {
                        event = new ServletContextAttributeEvent(this, name, value);
                    }
                    
                }
                if (oldValue != null)
                {
                    listener.attributeReplaced(event);
                }
                else
                {
                    listener.attributeAdded(event);
                }
            }
            catch (Throwable t)
            {
                
                logger.error(t.getMessage(), t);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String name)
    {
        Object value = attributes.remove(name);
        if (value == null)
        {
            return;
        }
        
        ServletContextAttributeEvent event = null;
        for (int i = 0, len = eventListeners.length; i < len; i++)
        {
            if (!(eventListeners[i] instanceof ServletContextAttributeListener))
            {
                continue;
            }
            ServletContextAttributeListener listener = (ServletContextAttributeListener)eventListeners[i];
            try
            {
                if (event == null)
                {
                    event = new ServletContextAttributeEvent(this, name, value);
                    
                }
                listener.attributeRemoved(event);
            }
            catch (Throwable t)
            {
                
                logger.warn(t.getMessage(), t);
                
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletContextName()
    {
        return displayName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SessionCookieConfig getSessionCookieConfig()
    {
        if (sessionCookieConfig == null)
        {
            sessionCookieConfig = new MicrowebSessionCookieConfig(this);
        }
        return sessionCookieConfig;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {
        if (sessionTrackingModes.contains(SessionTrackingMode.SSL))
        {
            throw new IllegalArgumentException("SSL tracking mode is not supported");
        }
        
        if (deployed)
        {
            throw new IllegalArgumentException("WebappContext has already been deployed");
        }
        
        this.sessionTrackingModes = Collections.unmodifiableSet(sessionTrackingModes);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        return DEFAULT_SESSION_TRACKING_MODES;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        return (sessionTrackingModes != null ? sessionTrackingModes : DEFAULT_SESSION_TRACKING_MODES);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader()
    {
        return null;
    }
    
    // ------------------------------------------------------- Protected Methods
    
    /**
     * Return a context-relative path, beginning with a "/", that represents the canonical version of the specified path
     * after ".." and "." elements are resolved out. If the specified path attempts to go outside the boundaries of the
     * current context (i.e. too many ".." path elements are present), return <code>null</code> instead.
     *
     * @param path Path to be normalized
     */
    protected String normalize(String path)
    {
        
        if (path == null)
        {
            return null;
        }
        
        String normalized = path;
        
        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
        {
            normalized = normalized.replace('\\', '/');
        }
        
        // Resolve occurrences of "/../" in the normalized path
        while (true)
        {
            int index = normalized.indexOf("/../");
            if (index < 0)
            {
                break;
            }
            if (index == 0)
            {
                return (null); // Trying to go outside our context
            }
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }
        
        // Return the normalized path that we have completed
        return (normalized);
        
    }
    
    /**
     *
     * @return
     */
    protected String getBasePath()
    {
        return basePath;
    }
    
    /**
     *
     * @return
     */
    public EventListener[] getEventListeners()
    {
        return eventListeners;
    }
    
    /**
     * Add a filter mapping to this Context.
     *
     * @param filterMap The filter mapping to be added
     *
     * @param isMatchAfter true if the given filter mapping should be matched against requests after any declared filter
     *            mappings of this servlet context, and false if it is supposed to be matched before any declared filter
     *            mappings of this servlet context
     *
     * @exception IllegalArgumentException if the specified filter name does not match an existing filter definition, or
     *                the filter mapping is malformed
     *
     */
    public void addFilterMap(FilterMap filterMap, boolean isMatchAfter)
    {
        
        // Validate the proposed filter mapping
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getURLPattern();
        if (null == filterRegistrations.get(filterName))
        {
            throw new IllegalArgumentException("Filter mapping specifies an unknown filter name: " + filterName);
        }
        if ((servletName == null) && (urlPattern == null))
        {
            throw new IllegalArgumentException(
                "Filter mapping must specify either a <url-pattern> or a <servlet-name>");
        }
        if ((servletName != null) && (urlPattern != null))
        {
            throw new IllegalArgumentException(
                "Filter mapping must specify either a <url-pattern> or a <servlet-name>");
        }
        // Because filter-pattern is new in 2.3, no need to adjust
        // for 2.2 backwards compatibility
        if ((urlPattern != null) && !validateURLPattern(urlPattern))
        {
            throw new IllegalArgumentException("Invalid <url-pattern> {0} in filter mapping: " + urlPattern);
        }
        
        // Add this filter mapping to our registered set
        if (isMatchAfter)
        {
            filterMaps.add(filterMap);
        }
        else
        {
            filterMaps.add(0, filterMap);
        }
        
        // if (notifyContainerListeners) {
        // fireContainerEvent("addFilterMap", filterMap);
        // }
    }
    
    public List<FilterMap> getFilterMaps()
    {
        return filterMaps;
    }
    
    /**
     * Removes any filter mappings from this Context.
     */
    protected void removeFilterMaps()
    {
        // Inform interested listeners
        // if (notifyContainerListeners) {
        // Iterator<FilterMap> i = filterMaps.iterator();
        // while (i.hasNext()) {
        // fireContainerEvent("removeFilterMap", i.next());
        // }
        // }
        filterMaps.clear();
    }
    
    /**
     * Gets the current servlet name mappings of the Filter with the given name.
     */
    public Collection<String> getServletNameFilterMappings(String filterName)
    {
        HashSet<String> mappings = new HashSet<String>();
        synchronized (filterMaps)
        {
            for (FilterMap fm : filterMaps)
            {
                if (filterName.equals(fm.getFilterName()) && fm.getServletName() != null)
                {
                    mappings.add(fm.getServletName());
                }
            }
        }
        return mappings;
    }
    
    /**
     * Gets the current URL pattern mappings of the Filter with the given name.
     */
    public Collection<String> getUrlPatternFilterMappings(String filterName)
    {
        HashSet<String> mappings = new HashSet<String>();
        synchronized (filterMaps)
        {
            for (FilterMap fm : filterMaps)
            {
                if (filterName.equals(fm.getFilterName()) && fm.getURLPattern() != null)
                {
                    mappings.add(fm.getURLPattern());
                }
            }
        }
        return mappings;
    }
    
    protected FilterChainFactory getFilterChainFactory()
    {
        return filterChainFactory;
    }
    
    @SuppressWarnings("UnusedDeclaration")
    protected void unregisterFilter(final Filter f)
    {
        synchronized (filterRegistrations)
        {
            for (Iterator<MicrowebFilterRegistration> i = filterRegistrations.values().iterator(); i.hasNext();)
            {
                final MicrowebFilterRegistration registration = i.next();
                if (registration.getFilter() == f)
                {
                    for (Iterator<FilterMap> fmi = filterMaps.iterator(); fmi.hasNext();)
                    {
                        FilterMap fm = fmi.next();
                        if (fm.getFilterName().equals(registration.getName()))
                        {
                            fmi.remove();
                        }
                    }
                    f.destroy();
                    i.remove();
                }
            }
        }
    }
    
    protected void unregisterAllFilters()
    {
        destroyFilters();
    }
    
    // --------------------------------------------------------- Private Methods
    
    protected void destroyFilters()
    {
        for (final MicrowebFilterRegistration registration : filterRegistrations.values())
        {
            registration.getFilter().destroy();
        }
        
        removeFilterMaps();
    }
    
    /**
     *
     */
    private void initializeListeners()
        throws ServletException
    {
        if (!eventListenerInstances.isEmpty())
        {
            eventListeners = eventListenerInstances.toArray(new EventListener[eventListenerInstances.size()]);
        }
    }
    
    /**
     *
     */
    @SuppressWarnings({"unchecked"})
    private void initFilters()
    {
        if (!filterRegistrations.isEmpty())
        {
            for (final MicrowebFilterRegistration registration : filterRegistrations.values())
            {
                try
                {
                    Filter f = registration.getFilter();
                    if (f == null)
                    {
                        f = createFilterInstance(registration);
                    }
                    
                    final MicrowebFilterConfig filterConfig = createFilterConfig(registration);
                    registration.setFilter(f);
                    f.init(filterConfig);
                    
                    logger.info("[{0}] Filter [{1}] registered for url pattern(s) [{2}] and servlet name(s) [{3}]",
                        new Object[] {displayName, registration.getClassName(), registration.getUrlPatternMappings(),
                            registration.getServletNameMappings()});
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    // --------------------------------------------------------- Private Methods
    
    /**
     *
     * @param registration
     * @return
     */
    private MicrowebFilterConfig createFilterConfig(final MicrowebFilterRegistration registration)
    {
        final MicrowebFilterConfig fConfig = new MicrowebFilterConfig(this);
        fConfig.setFilterName(registration.getName());
        if (!registration.getInitParameters().isEmpty())
        {
            fConfig.setInitParameters(registration.getInitParameters());
        }
        return fConfig;
    }
    
    /**
     *
     * @param registration
     * @return
     */
    private MicrowebServletConfig createServletConfig(final ServletRegistration registration)
    {
        final MicrowebServletConfig sConfig = new MicrowebServletConfig(this);
        sConfig.setServletName(registration.getName());
        if (!registration.getInitParameters().isEmpty())
        {
            sConfig.setInitParameters(registration.getInitParameters());
        }
        return sConfig;
        
    }
    
    /**
     *
     */
    private void contextInitialized()
    {
        ServletContextEvent event = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(webappClassLoader);
        for (int i = 0, len = eventListeners.length; i < len; i++)
        {
            if (!(eventListeners[i] instanceof ServletContextListener))
            {
                continue;
            }
            ServletContextListener listener = (ServletContextListener)eventListeners[i];
            if (event == null)
            {
                event = new ServletContextEvent(this);
            }
            try
            {
                listener.contextInitialized(event);
            }
            catch (Throwable t)
            {
                
                logger.warn(t.getMessage(), t);
                
            }
        }
        Thread.currentThread().setContextClassLoader(loader);
    }
    
    /**
     *
     */
    private void contextDestroyed()
    {
        ServletContextEvent event = null;
        for (int i = 0, len = eventListeners.length; i < len; i++)
        {
            if (!(eventListeners[i] instanceof ServletContextListener))
            {
                continue;
            }
            ServletContextListener listener = (ServletContextListener)eventListeners[i];
            if (event == null)
            {
                event = new ServletContextEvent(this);
            }
            try
            {
                listener.contextDestroyed(event);
            }
            catch (Throwable t)
            {
                
                logger.warn(t.getMessage(), t);
                
            }
        }
    }
    
    /**
     * Instantiates the given Servlet class.
     *
     * @return the new Servlet instance
     */
    protected Servlet createServletInstance(final MicrowebServletRegistration registration)
        throws Exception
    {
        String servletClassName = registration.getClassName();
        Class<? extends Servlet> servletClass = registration.getServletClass();
        if (servletClassName != null)
        {
            return (Servlet)ClassLoaderUtil.load(servletClassName);
        }
        else
        {
            return createServletInstance(servletClass);
        }
    }
    
    /**
     * Instantiates the given Servlet class.
     *
     * @return the new Servlet instance
     */
    protected Servlet createServletInstance(Class<? extends Servlet> servletClass)
        throws Exception
    {
        return servletClass.newInstance();
    }
    
    /**
     * Instantiates the given Filter class.
     *
     * @return the new Filter instance
     */
    protected Filter createFilterInstance(final MicrowebFilterRegistration registration)
        throws Exception
    {
        String filterClassName = registration.getClassName();
        Class<? extends Filter> filterClass = registration.getFilterClass();
        if (filterClassName != null)
        {
            return (Filter)ClassLoaderUtil.load(filterClassName);
        }
        else
        {
            return createFilterInstance(filterClass);
        }
    }
    
    /**
     * Instantiates the given Filter class.
     *
     * @return the new Filter instance
     */
    protected Filter createFilterInstance(Class<? extends Filter> filterClass)
        throws Exception
    {
        return filterClass.newInstance();
    }
    
    /**
     * Instantiates the given EventListener class.
     *
     * @return the new EventListener instance
     */
    protected EventListener createEventListenerInstance(Class<? extends EventListener> eventListenerClass)
        throws Exception
    {
        return eventListenerClass.newInstance();
    }
    
    /**
     * Instantiates the given EventListener class.
     *
     * @return the new EventListener instance
     */
    protected EventListener createEventListenerInstance(String eventListenerClassname)
        throws Exception
    {
        return (EventListener)ClassLoaderUtil.load(eventListenerClassname);
    }
    
    /**
     * Instantiates the given HttpUpgradeHandler class.
     *
     * @param clazz
     * @param <T>
     * @return a new T instance
     * @throws Exception
     */
    public <T extends HttpUpgradeHandler> T createHttpUpgradeHandlerInstance(Class<T> clazz)
        throws Exception
    {
        return clazz.newInstance();
    }
    
    /**
     * Validate the syntax of a proposed <code>&lt;url-pattern&gt;</code> for conformance with specification
     * requirements.
     *
     * @param urlPattern URL pattern to be validated
     */
    protected boolean validateURLPattern(String urlPattern)
    {
        if (urlPattern == null)
        {
            return false;
        }
        if (urlPattern.isEmpty())
        {
            return true;
        }
        if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0)
        {
            logger.warn("The URL pattern ''{0}'' contains a CR or LF and so can never be matched", urlPattern);
            return false;
        }
        if (urlPattern.startsWith("*."))
        {
            if (urlPattern.indexOf('/') < 0)
            {
                checkUnusualURLPattern(urlPattern);
                return true;
            }
            else
                return false;
        }
        if ((urlPattern.startsWith("/")) && (!urlPattern.contains("*.")))
        {
            checkUnusualURLPattern(urlPattern);
            return true;
        }
        else
            return false;
        
    }
    
    /**
     * Check for unusual but valid <code>&lt;url-pattern&gt;</code>s. See Bugzilla 34805, 43079 &amp; 43080
     */
    private void checkUnusualURLPattern(String urlPattern)
    {
        
        if (urlPattern.endsWith("*") && (urlPattern.length() < 2 || urlPattern.charAt(urlPattern.length() - 2) != '/'))
        {
            logger.info("Suspicious url pattern: \"{0}" + "\"" + " in context - see"
                + " section SRV.11.2 of the Servlet specification", urlPattern);
        }
    }
}
