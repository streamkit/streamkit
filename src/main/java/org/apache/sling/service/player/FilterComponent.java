package org.apache.sling.service.player;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @scr.component immediate="true" metatype="no" enabled="no"
 * @scr.property name="service.description" value="Request Filter"
 * @scr.property name="filter.scope" value="request" private="true"
 * @scr.property name="filter.order" value="-2147483648" type="Integer" private="true"
 * @scr.service
 */
public class FilterComponent implements Filter {
    private static final String RESOURCE_URI_PATH = "/admin/";
    private static final String SC_FORBIDDEN_MSG = "Resource not supported";

    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("INIT Filter");
    }

    public void destroy() {
        System.out.println("Destroy filter");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String uri = httpServletRequest.getRequestURI();

        if (checkURIPath(uri)) {
            handleResourceNotImplemented(httpServletRequest, httpServletResponse);
            return;
        }

// default implementation
        chain.doFilter(request, response);
    }



    private void handleResourceNotImplemented(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, SC_FORBIDDEN_MSG);
    }

    private boolean checkURIPath(String requestURI) {
        boolean flag = false;

        if (requestURI != null) {
            if (requestURI.contains(RESOURCE_URI_PATH)) {
                flag = true;
            }
        }

        return flag;
    }
}
