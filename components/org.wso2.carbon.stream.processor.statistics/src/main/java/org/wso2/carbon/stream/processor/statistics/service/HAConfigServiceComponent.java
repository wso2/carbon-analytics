package org.wso2.carbon.stream.processor.statistics.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.stream.processor.core.ha.HAInfo;
import org.wso2.carbon.stream.processor.statistics.internal.StreamProcessorStatisticDataHolder;


/**
 * Service component which is used to get the HA details of worker node.
 */
@Component(
        name = "org.wso2.carbon.stream.processor.statistics.service.HAConfigServiceComponent",
        service = HAConfigServiceComponent.class,
        immediate = true
)
public class HAConfigServiceComponent {

    public HAConfigServiceComponent() {
    }

    /**
     * Get the HAInfo service.
     * This is the bind method that gets called for HAInfo service registration that satisfy the policy.
     *
     * @param haInfo the HAInfo service that is registered as a service.
     */
    // TODO: 11/1/17 check mandatory
    @Reference(
            //org.wso2.carbon.stream.processor.core.ha.HAInfo
            name = "org.wso2.carbon.stream.processor.core.ha.HAInfo",
            service = HAInfo.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterHAInfoProvider"
    )
    protected void registerHAInfoProvider(HAInfo haInfo){
        StreamProcessorStatisticDataHolder.getInstance().setHaInfo(haInfo);
    }

    protected void unregisterHAInfoProvider(HAInfo haInfo){
        StreamProcessorStatisticDataHolder.getInstance().setHaInfo(null);
    }
}
