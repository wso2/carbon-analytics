package org.wso2.carbon.siddhi.store.api.rest;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class ApiOriginFilter implements javax.servlet.Filter {
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }
}