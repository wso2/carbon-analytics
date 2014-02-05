package org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.processors;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.common.ServerData;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 */
public class GadgetUploadProcessor extends AbstractFileUploadExecutor {
    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws CarbonException, IOException {
        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        HttpSession session = request.getSession();

        String errorRedirect = null;
        try {
            GadgetRepoServiceClient client =
                    new GadgetRepoServiceClient(cookie, serverURL, configurationContext, request.getLocale());

            String gName = null;
            Map<String, ArrayList<String>> formFieldsMap = getFormFieldsMap();

            if (formFieldsMap.get("gadgetName") != null)
                gName = formFieldsMap.get("gadgetName").get(0);

            String gUrl = null;
            if (formFieldsMap.get("gadgetUrl") != null)
                gUrl = formFieldsMap.get("gadgetUrl").get(0);

            String gDesc = null;
            if (formFieldsMap.get("gadgetDesc") != null)
                gDesc = formFieldsMap.get("gadgetDesc").get(0);

            String formMode = null;
            if (formFieldsMap.get("mode") != null) {
                formMode = formFieldsMap.get("mode").get(0);
            }

            String gadgetPath = null;
            if (formFieldsMap.get("gadgetPath") != null) {
                gadgetPath = formFieldsMap.get("gadgetPath").get(0);
            }

            String redirect = null;
            if (formFieldsMap.get("redirect") != null) {
                redirect = formFieldsMap.get("redirect").get(0);
            }

            if (formFieldsMap.get("errorRedirect") != null) {
                errorRedirect = formFieldsMap.get("errorRedirect").get(0);
            }

            String symlinkLocation = null;
            if (formFieldsMap.get("symlinkLocation") != null) {
                symlinkLocation = formFieldsMap.get("symlinkLocation").get(0);
            }
            IServerAdmin adminClient =
                    (IServerAdmin) CarbonUIUtil.
                            getServerProxy(new ServerAdminClient(configurationContext,
                                    serverURL, cookie, session), IServerAdmin.class, session);
            ServerData data = adminClient.getServerData();
            String chroot = "";
            if (data.getRegistryType() != null && data.getRegistryType().equals("remote") &&
                    data.getRemoteRegistryChroot() != null &&
                    !data.getRemoteRegistryChroot().equals(RegistryConstants.PATH_SEPARATOR)) {
                chroot = data.getRemoteRegistryChroot();
                if (!chroot.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                    chroot = RegistryConstants.PATH_SEPARATOR + chroot;
                }
                if (chroot.endsWith(RegistryConstants.PATH_SEPARATOR)) {
                    chroot = chroot.substring(0, chroot.length() - RegistryConstants.PATH_SEPARATOR.length());
                }
            }
            if (symlinkLocation != null) {
                symlinkLocation = chroot + symlinkLocation;
            }

            FileItemData gadgetScreenData = null;
            Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();

            if (fileItemsMap.get("gadgetScreen") != null) {
                gadgetScreenData = fileItemsMap.get("gadgetScreen").get(0);
            }

            DataHandler gadgetThumb = null;
            if (gadgetScreenData != null) {
                gadgetThumb = gadgetScreenData.getDataHandler();
            }

            String cType = null;
            if (gadgetThumb != null) {
                cType = gadgetThumb.getContentType();
            }

            FileItemData gadgetArchive = null;
            if (fileItemsMap.get("gadget") != null) {
                gadgetArchive = fileItemsMap.get("gadget").get(0);
            } else if (gUrl == null || "".equals(gUrl)) {
                String msg = "Failed add resource. Gadget URL is empty";
                log.error(msg);

                if (errorRedirect == null) {
                    CarbonUIMessage.sendCarbonUIMessage(
                            msg, CarbonUIMessage.ERROR, request, response,
                            getContextRoot(request) + "/" +  webContext + "/admin/error.jsp");
                } else {
                    CarbonUIMessage.sendCarbonUIMessage(
                            msg, CarbonUIMessage.ERROR, request, response,
                            getContextRoot(request) + "/" + webContext + "/" + errorRedirect +
                            (errorRedirect.indexOf("?") == -1 ? "?" : "&") +
                            "msg=" + URLEncoder.encode(msg, "UTF-8"));
                }
                return false;

            }

            DataHandler gadgetFile = null;
            if (gadgetArchive != null) {
                gadgetFile = gadgetArchive.getDataHandler();
                if ("application/zip".equals(gadgetFile.getContentType())) {
                     /* This is a zip file. So upload gadget using zip file (Replaces new media type now). In this case
                     there is no gadget thumbnail. So use the cType var to store media type for gadget */
                    cType = DashboardConstants.GADGET_MEDIA_TYPE;
                }
            }

            if ("add".equals(formMode)) {
                Boolean created = client.addGadgetEntryToRepo(gName, gUrl, gDesc, gadgetThumb, cType, gadgetFile);
                if(!created){
                    String msg = "Can not add Gadget " + gName + ". Please refer error log for more details.";
                    log.info(msg);
                    CarbonUIMessage.sendCarbonUIMessage(
                            msg, CarbonUIMessage.ERROR, request, response,
                            getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
                    return false;
                } else {
                    String msg = "Gadget " + gName + " added successfully.";
                    log.debug(msg);
                }
            } else if ("mod".equals(formMode)) {
                Boolean created = client.modifyGadgetEntry(gadgetPath, gName, gUrl, gDesc, gadgetThumb, cType, gadgetFile);
                if(!created){
                    String msg = "Can not modify Gadget" + gName + ". Please refer error log for more details.";
                    log.info(msg);
                    CarbonUIMessage.sendCarbonUIMessage(
                            msg, CarbonUIMessage.ERROR, request, response,
                            getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
                    return false;
                } else {
                    String msg = "Gadget " + gName + " modified successfully.";
                    log.debug(msg);
                }
            }

            response.setContentType("text/html; charset=utf-8");
            String msg = "Successfully uploaded content.";

            CarbonUIMessage.sendCarbonUIMessage(
                    msg, CarbonUIMessage.INFO, request, response,
                    getContextRoot(request) + "/" + webContext + "/" + redirect + "?ordinal=0&mode=" +
                    formMode + "&region=region1&item=gadgetrepo_menu&name=governance");
            return true;

        } catch (Exception e) {
            String msg = "File upload failed. " + e.getMessage();
            if (e.getMessage() == null) {
                msg = "File upload failed. Unexpected error";
            }
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(
                    msg, CarbonUIMessage.ERROR, request, response,
                    getContextRoot(request) + "/" + webContext + "/admin/error.jsp");
            return false;
        }
    }

    private static String calcualtePath(String parentPath, String resourceName) {
        String resourcePath;
        if (!parentPath.startsWith(RegistryConstants.PATH_SEPARATOR)) {
            parentPath = RegistryConstants.PATH_SEPARATOR + parentPath;
        }
        if (parentPath.endsWith(RegistryConstants.PATH_SEPARATOR)) {
            resourcePath = parentPath + resourceName;
        } else {
            resourcePath = parentPath + RegistryConstants.PATH_SEPARATOR + resourceName;
        }
        return resourcePath;
    }
}
