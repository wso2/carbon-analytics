package org.wso2.carbon.event.stream.manager.core.internal.build;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

@Deprecated
public class Axis2ConfigurationContextObserverImpl
        extends AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        System.out.println("--------------- A new Axis2 Configuration context is created for : " +
                           tenantDomain);
        log.info("Loading Event stream specific to tenant when the tenant logged in");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            //EventBuilderServiceBuilder.loadConfigurationsFromRegistry();
        } catch (Exception e) {
            log.error("Unable to load Event Streams ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
