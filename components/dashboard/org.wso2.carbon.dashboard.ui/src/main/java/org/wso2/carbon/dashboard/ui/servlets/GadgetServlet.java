package org.wso2.carbon.dashboard.ui.servlets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.ui.processors.GadgetContentProcessor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * When the gadget is request as an http url, this servlet get invoked and the gadget xml is served
 * This is a frontend registered servlet, which also handled security based on logged in user.
 * URL example :
 *      When in Gadget Server
 *      http://<host:port>/registry/resource/_system/config/repository/gadget-server/gadgets/foo.xml
 *
 *      When in product mode
 *      http://<host:port>/registry/resource/_system/config/repository/dashboards/gadgets/foo.xml
 */
public class GadgetServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(GadgetServlet.class);

    private ServletConfig servletConfig;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.servletConfig = servletConfig;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            GadgetContentProcessor.getContent(request, response, servletConfig);
        } catch (Exception e) {

            String msg = "Failed to get resource content. " + e.getMessage();
            log.error(msg, e);
            response.setStatus(500);
        }
    }
}
