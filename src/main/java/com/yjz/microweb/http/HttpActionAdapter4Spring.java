package com.yjz.microweb.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import com.yjz.microweb.cache.ResourceCache;
import com.yjz.microweb.context.MicrowebServletContext;
import com.yjz.microweb.filter.FilterChainFactory;
import com.yjz.microweb.filter.MicrowebFilterChain;
import com.yjz.microweb.util.FileUtil;
import com.yjz.microweb.util.MimeType;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class HttpActionAdapter4Spring implements HttpActionAdapter
{
    
    private static final Logger logger = LoggerFactory.getLogger(HttpActionAdapter4Spring.class);
    
    // private static final InterceptorHandler<FullHttpRequestWrapper, FullHttpResponseWrapper> interceptors = new
    // InterceptorHandler<>();
    
    private static HttpActionAdapter4Spring httpActionAdaptor = new HttpActionAdapter4Spring();
    
    private boolean isStaticSupport;
    
    private MicrowebServletContext servletContext;
    
    private DispatcherServlet dispatcherServlet;
    
    private ResourceCache CACHE;
    
    // FilterChain是非线程安全的，需为每个线程保存索引，因此采用ThreadLocal建立副本。这里采用Interceptor会更加适合。
    // private static final ThreadLocal<FilterChain<FullHttpRequestWrapper, FullHttpResponseWrapper>> chain = new
    // ThreadLocal<>();
    
    private HttpActionAdapter4Spring()
    {
        this.isStaticSupport = HttpCoreServer.instance().isStaticSupport;
        this.servletContext = HttpCoreServer.instance().servletContext;
        this.dispatcherServlet = HttpCoreServer.instance().dispatcherServlet;
        this.CACHE = HttpCoreServer.instance().CACHE;
    }
    
    public static HttpActionAdapter4Spring instance()
    {
        return httpActionAdaptor;
    }
    
    public HttpResponse doService(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        HttpServletRequest requestWrapper = new FullHttpRequestWrapper(servletContext, request, requestURI, parameters);
        FullHttpResponseWrapper responseWrapper = new FullHttpResponseWrapper(ctx, resp);   
        ((FullHttpRequestWrapper) requestWrapper).setResponse(responseWrapper);
        
        if(request.headers() != null)
        {
        	String contentTye = request.headers().get("Content-Type");
        	if(contentTye !=  null && contentTye.toUpperCase().contains("MULTIPART/FORM-DATA"))
        	{
        	    CommonsMultipartResolver cmr = new CommonsMultipartResolver();
        	    cmr.setDefaultEncoding("UTF-8");
        		requestWrapper = cmr.resolveMultipart(requestWrapper);
        	}
        }
        
        try
        {
            MicrowebFilterChain filterChain = new FilterChainFactory(servletContext).createFilterChain(requestWrapper,
                dispatcherServlet,  DispatcherType.REQUEST);
            
            if (filterChain != null)
            {
                filterChain.invokeFilterChain(requestWrapper, responseWrapper);
            }
            else
            {
                dispatcherServlet.service(requestWrapper, responseWrapper);
            }
        }
        catch (ServletException | IOException e)
        {
            logger.error(e.getMessage(), e);
        }
    
        return responseWrapper.getResponse();
    }
    
    private void appendContentType4Resource(FullHttpResponseWrapper responseWrapper, String requestURI)
    {
        int dotIdx = requestURI.lastIndexOf(".");
        String extension = requestURI.substring(dotIdx+1);
        if(MimeType.contains(extension))
        {
            String contentType = MimeType.get(extension);
            responseWrapper.addHeader("Content-Type", contentType);
        }
    }
    
    @Override
    public HttpResponse doGet(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        return doService(ctx, request, requestURI, parameters);
    }
    
    @Override
    public HttpResponse doPost(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        return doService(ctx, request, requestURI, parameters);
    }
    
    @Override
    public HttpResponse doNotHttpRequest(ChannelHandlerContext ctx, Object msg)
    {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
    }
    
    @Override
    public HttpResponse doNullHttpMethod(ChannelHandlerContext ctx, FullHttpRequest request)
    {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
    }
    
    @Override
    public HttpResponse doUnContainMethod(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
    }
    
    @Override
    public HttpResponse doTrace(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public HttpResponse doDelete(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public HttpResponse doPatch(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public HttpResponse doPut(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public HttpResponse doHead(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public HttpResponse doOptions(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public HttpResponse doConnect(ChannelHandlerContext ctx, FullHttpRequest request, String requestURI,
        Map<String, String[]> parameters)
    {
        
        return null;
    }
    
    /**
     * <b>返回静态资源</b></br>
     * </br>
     * 待修改：新建http协议管理类，根据http协议完善请求和响应</br>
     * 
     * @param resp
     * @param requestURI
     * @param parameters
     * @return
     */
    private HttpResponse getStaticResource(FullHttpRequest request, FullHttpResponse resp, String requestURI,
        Map<String, String[]> parameters)
    {
        HttpHeaders headers = resp.headers();
        if (null == requestURI || requestURI.isEmpty() || "/".equals(requestURI))
        {
            requestURI = "/index.html";
            headers.add(HttpHeaderNames.CONTENT_LOCATION, request.headers().get("Host") + "/index.html");
        }
        
        byte[] bytes = CACHE.getCache(requestURI);
        if (null == bytes)
        {
            InputStream is = this.getClass().getResourceAsStream("/views/public" + requestURI);
            bytes = FileUtil.inputStreamToBytes(is);
            CACHE.putCache(requestURI, bytes);
        }
        
        if (bytes != null && bytes.length > 0)
        {
            resp = resp.replace(Unpooled.wrappedBuffer(bytes));
            
            // 需要根据实际的文件类型设置参数
            headers.add(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            headers.add(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
            // headers.add(HttpHeaderNames.EXPIRES, "Thu, 01 Oct 2030 15:33:19 GMT");
            // headers.add(HttpHeaderNames.LAST_MODIFIED, "Wed, 22 Jun 2017 06:40:43 GMT");
            
            resp = resp.replace(Unpooled.wrappedBuffer(bytes));
            // headers.add(HttpHeaderNames.CONTENT_MD5, "dddddfsadfarewqerrew");
        }
        else
        {
            resp.setStatus(HttpResponseStatus.NOT_FOUND);
            headers.add("Content-Length", 0);
        }
        return resp;
    }
    
}
