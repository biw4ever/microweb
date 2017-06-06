package com.yjz.microweb.http;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.yjz.microweb.MicrowebException;
import com.yjz.microweb.annotation.FilterClass;
import com.yjz.microweb.annotation.FilterDef;
import com.yjz.microweb.annotation.FilterInitParam;
import com.yjz.microweb.annotation.FilterName;
import com.yjz.microweb.annotation.FilterUrlPattern;
import com.yjz.microweb.cache.ResourceCache;
import com.yjz.microweb.cache.ResourceCacheDefault;
import com.yjz.microweb.context.MicrowebServletContext;
import com.yjz.microweb.filter.FilterMap;
import com.yjz.microweb.filter.MicrowebFilterConfig;
import com.yjz.microweb.servlet.MicrowebServletConfig;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;

/**
 * HttpServlet核心服务器
 * @ClassName HttpCoreServer
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author biw
 * @Date 2017年5月31日 下午1:19:12
 * @version 1.0.0
 */
public class HttpCoreServer
{
    private static final Logger logger = LoggerFactory.getLogger(HttpCoreServer.class);
    
    private static HttpCoreServer httpCoreServer = new HttpCoreServer();
    
    protected XmlWebApplicationContext wac;
    
    protected DispatcherServlet dispatcherServlet;
    
    protected MicrowebServletContext servletContext;
    
    protected ResourceCache CACHE = new ResourceCacheDefault();
    
    protected boolean isStaticSupport = true;
    
    private HttpCoreServer()
    {
        
    }
    
    public static HttpCoreServer instance()
    {
        return httpCoreServer;
    }
    
    public void init(String servletContextName)
    {
        initServletContext(servletContextName);
        
        initWebApplication();
        
        initFilters();
        
        initServlet();
        
    }
    
    private void initServletContext(String servletContextName)
    {
        this.servletContext = new MicrowebServletContext(servletContextName, servletContextName, servletContextName);
    }
    
    private void initWebApplication()
    {
        MicrowebServletConfig servletConfig = new MicrowebServletConfig(this.servletContext);
        this.wac = new XmlWebApplicationContext();
        this.wac.setServletContext(this.servletContext);
        this.wac.setServletConfig(servletConfig);
        this.wac.setConfigLocation("classpath*:applicationContext.xml");
        this.wac.refresh();
    }
    
    private void initServlet()
    {
        try
        {
            
            
            this.dispatcherServlet = new DispatcherServlet(this.wac);
            this.dispatcherServlet.init(this.wac.getServletConfig());
        }
        catch (ServletException e)
        {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void initFilters()
    {
        Map<String, Object> filterDefBeans = this.wac.getBeansWithAnnotation(FilterDef.class);
        for (Object obj : filterDefBeans.values())
        {
            for (Method method : obj.getClass().getDeclaredMethods())
            {
                
                FilterName filterNameAnno = method.getAnnotation(FilterName.class);
                if (filterNameAnno == null)
                {
                    logger.error("'FilterName' on annotation class " + obj.getClass().getName() + "does not exist.");
                    continue;
                }
                
                FilterClass filterClassAnno = method.getAnnotation(FilterClass.class);
                if (filterClassAnno == null)
                {
                    logger.error("'FilterClass' on annotation class " + obj.getClass().getName() + "does not exist.");
                    continue;
                }
                
                FilterInitParam[] filterInitParamAnnos = method.getAnnotationsByType(FilterInitParam.class);
                Map<String, String> initParams = new HashMap<>();
                if (filterInitParamAnnos != null && filterInitParamAnnos.length > 0)
                {
                    for (FilterInitParam filterInitParamAnno : filterInitParamAnnos)
                    {
                        initParams.put(filterInitParamAnno.paramName(), filterInitParamAnno.paramValue());
                    }
                }
                
                FilterUrlPattern filterUrlPatternAnno = method.getAnnotation(FilterUrlPattern.class);
                if (filterUrlPatternAnno == null)
                {
                    logger.error(
                        "'FilterUrlPattern' on annotation class " + obj.getClass().getName() + "does not exist.");
                    continue;
                }
                
                try
                {
                    Filter filter = (Filter)filterClassAnno.value().newInstance();
                    MicrowebFilterConfig conifg = new MicrowebFilterConfig(servletContext);
                    conifg.setFilter(filter);
                    conifg.setFilterName(filterNameAnno.value());
                    conifg.setInitParameters(initParams);   
                    filter.init(conifg);
                    servletContext.addFilter(conifg.getFilterName(), filter);
                    
                    for(String urlPattern : filterUrlPatternAnno.value())
                    {
                        FilterMap filterMap = new FilterMap();
                        filterMap.setFilterName(filterNameAnno.value());
                        filterMap.setURLPattern(urlPattern);
                        servletContext.addFilterMap(filterMap, false);
                    }   
                }
                catch (InstantiationException | IllegalAccessException | ServletException e)
                {
                    throw new MicrowebException(e);
                }    
            }
        }
    }
    
    public HttpResponse dispach(ChannelHandlerContext ctx, Object msg)
    {
        HttpActionAdapter action = HttpActionAdapter4Spring.instance();
        
        if (!(msg instanceof FullHttpRequest))
        {
            return action.doNotHttpRequest(ctx, msg);
        }
        
        FullHttpRequest request = (FullHttpRequest)msg;
        HttpMethod method = request.method();
        
        if (null == method)
        {
            return action.doNullHttpMethod(ctx, request);
        }
        
        String uri = request.uri();
        String[] temp = uri.split("\\?");
        String shortUri = getRequestURI(temp[0]);
        Map<String, String[]> parameters = getParameters(temp);
        
        if (method.equals(HttpMethod.GET))
        {
            return action.doGet(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.POST))
        {
            return action.doPost(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.OPTIONS))
        {
            return action.doOptions(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.HEAD))
        {
            return action.doHead(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.PUT))
        {
            return action.doPut(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.PATCH))
        {
            return action.doPatch(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.DELETE))
        {
            return action.doDelete(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.TRACE))
        {
            return action.doTrace(ctx, request, shortUri, parameters);
        }
        else if (method.equals(HttpMethod.CONNECT))
        {
            return action.doConnect(ctx, request, shortUri, parameters);
        }
        else
        {
            return action.doUnContainMethod(ctx, request, shortUri, parameters);
        }
    }
    
    private String getRequestURI(String fullPath)
    {
        return fullPath.replaceAll(servletContext.getContextPath(), "");
    }
    
    /**
     * 构建请求参数
     * 
     * @param temp
     * @return
     */
    private Map<String, String[]> getParameters(String[] temp)
    {
        Map<String, String[]> map = new HashMap<String, String[]>();
        if (temp.length > 1)
        {
            String suffix = temp[1];
            String[] params = suffix.split("&");
            for (String s : params)
            {
                String[] keyValues = s.split("=");
                String key = keyValues[0];
                String[] values = keyValues[1].split(",");
                map.put(key, values);
            }
        }
        return map;
    }
}
