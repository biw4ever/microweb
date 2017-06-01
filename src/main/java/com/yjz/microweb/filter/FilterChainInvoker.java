package com.yjz.microweb.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface FilterChainInvoker
{
    void invokeFilterChain(ServletRequest request, ServletResponse response)
        throws IOException, ServletException;
}
