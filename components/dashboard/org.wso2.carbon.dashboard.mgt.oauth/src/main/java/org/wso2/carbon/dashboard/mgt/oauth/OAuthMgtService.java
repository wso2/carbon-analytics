package org.wso2.carbon.dashboard.mgt.oauth;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.gadgets.GadgetException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.common.OAuthUtils;
import org.wso2.carbon.dashboard.common.oauth.RegistryBasedOAuthStore;
import org.wso2.carbon.dashboard.mgt.oauth.bean.ConsumerEntry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * This admin service manages the OAuth Consumer keys in Dashboard
 */
public class OAuthMgtService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(OAuthMgtService.class);

    private static final int NUMBER_OF_ENTRIES_PER_PAGE = 5;

    public ConsumerEntry[] getConsumerEntries() throws AxisFault {
        Collection oauthKeyCollection;
        Registry reg = getConfigSystemRegistry();
        ConsumerEntry[] entries;
        try {
            if (reg.resourceExists(DashboardConstants.OAUTH_KEY_STORE)) {
                oauthKeyCollection = (Collection) reg.get(DashboardConstants.OAUTH_KEY_STORE);
                entries = new ConsumerEntry[oauthKeyCollection.getChildCount()];
                for (int i = 0; i < oauthKeyCollection.getChildCount(); i++) {
                    Resource res = (Resource) reg.get(oauthKeyCollection.getChildren()[i]);

                    ConsumerEntry entry = new ConsumerEntry();
                    entry.setConsumerKey(res.getProperty(DashboardConstants.CONSUMER_KEY_KEY));
                    entry.setConsumerSecret(res.getProperty(DashboardConstants.CONSUMER_SECRET_KEY));
                    entry.setKeyType(res.getProperty(DashboardConstants.KEY_TYPE_KEY));
                    entry.setService(res.getProperty(DashboardConstants.CONSUMER_SERVICE));
                    entries[i] = entry;

                }
            } else {
                return new ConsumerEntry[0];
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
        return entries;
    }

    public ConsumerEntry[] getConsumerPagedEntries(int pageNum) throws AxisFault {
        Collection oauthKeyCollection;
        Registry reg = getConfigSystemRegistry();
        ConsumerEntry[] entries;
        int startEntry = pageNum * NUMBER_OF_ENTRIES_PER_PAGE;
        try {
            if (reg.resourceExists(DashboardConstants.OAUTH_KEY_STORE)) {

                // Get all keys to a collection
                oauthKeyCollection = (Collection) reg.get(DashboardConstants.OAUTH_KEY_STORE);
                int childCount = oauthKeyCollection.getChildCount();

                // Set ConsumerEntry array size
                if (childCount <= NUMBER_OF_ENTRIES_PER_PAGE) {
                    entries = new ConsumerEntry[childCount];
                } else {
                    if (childCount < (startEntry + NUMBER_OF_ENTRIES_PER_PAGE)) {
                        entries = new ConsumerEntry[childCount - startEntry];

                    } else {
                        entries = new ConsumerEntry[NUMBER_OF_ENTRIES_PER_PAGE];
                    }
                }

                // Set entries for selected page
                for (int i = 0; i < childCount && i < NUMBER_OF_ENTRIES_PER_PAGE &&
                        (i + startEntry) < childCount; i++) {

                    Resource res = (Resource) reg.get(oauthKeyCollection.getChildren()[i + startEntry]);
                    ConsumerEntry entry = new ConsumerEntry();
                    entry.setConsumerKey(res.getProperty(DashboardConstants.CONSUMER_KEY_KEY));
                    entry.setConsumerSecret(res.getProperty(DashboardConstants.CONSUMER_SECRET_KEY));
                    entry.setKeyType(res.getProperty(DashboardConstants.KEY_TYPE_KEY));
                    entry.setService(res.getProperty(DashboardConstants.CONSUMER_SERVICE));
                    entries[i] = entry;

                }
            } else {
                return new ConsumerEntry[0];
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
        return entries;
    }

    public boolean addConsumerEntry(ConsumerEntry entry, String mode) throws AxisFault {
        Registry reg = getConfigSystemRegistry();
        if (entry == null) {
            throw new AxisFault("OAuth Entry is null", new IllegalArgumentException());
        }

        try {
            if (reg.resourceExists(DashboardConstants.OAUTH_KEY_STORE + "/" + entry.getService()) && "new".equals(mode)) {
                throw new AxisFault("Consumer already exists", new IllegalArgumentException());
            } else {
                Resource res = reg.newResource();
                if ("update".equals(mode)) {
                    res = reg.get(DashboardConstants.OAUTH_KEY_STORE + "/" + entry.getService());
                }
                res.setProperty(DashboardConstants.CONSUMER_KEY_KEY, entry.getConsumerKey());
                res.setProperty(DashboardConstants.CONSUMER_SECRET_KEY, entry.getConsumerSecret());
                res.setProperty(DashboardConstants.KEY_TYPE_KEY, entry.getKeyType());
                res.setProperty(DashboardConstants.CONSUMER_SERVICE, entry.getService());
                reg.put(DashboardConstants.OAUTH_KEY_STORE + "/" + entry.getService(), res);
            }

            int tenantId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
            RegistryBasedOAuthStore store = (RegistryBasedOAuthStore) OAuthUtils.
                    getOauthStoreProvider().getStore(tenantId);
            try {
                store.initFromConfigString(reg, (Collection) reg.get(DashboardConstants.OAUTH_KEY_STORE));
                log.debug("RegistryBasedOAuthStore initialized FromConfigString for tenant ID = " + tenantId);
            } catch (GadgetException e) {
                log.error(e.getMessage(), e);
                throw new AxisFault(e.getMessage(), e);
            }

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }

        return true;
    }

    public ConsumerEntry getConsumerEntry(String consumerServiceName) throws AxisFault {
        ConsumerEntry entry = new ConsumerEntry();

        Registry reg = getConfigSystemRegistry();
        try {
            if (reg.resourceExists(DashboardConstants.OAUTH_KEY_STORE + "/" + consumerServiceName)) {
                Resource res = reg.get(DashboardConstants.OAUTH_KEY_STORE + "/" + consumerServiceName);
                entry.setConsumerKey(res.getProperty(DashboardConstants.CONSUMER_KEY_KEY));
                entry.setConsumerSecret(res.getProperty(DashboardConstants.CONSUMER_SECRET_KEY));
                entry.setKeyType(res.getProperty(DashboardConstants.KEY_TYPE_KEY));
                entry.setService(res.getProperty(DashboardConstants.CONSUMER_SERVICE));
                return entry;
            } else {
                return null;
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public boolean deleteConsumerEntry(String consumerServiceName) throws AxisFault {
        ConsumerEntry entry = new ConsumerEntry();

        Registry reg = getConfigSystemRegistry();
        try {
            if (reg.resourceExists(DashboardConstants.OAUTH_KEY_STORE + "/" + consumerServiceName)) {
                reg.delete(DashboardConstants.OAUTH_KEY_STORE + "/" + consumerServiceName);
                return true;
            } else {
                throw new AxisFault("No such consumer to remove", new IllegalArgumentException());
            }
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public int getNumberOfPages() {
        int intVal = getNumberOfEntries() / NUMBER_OF_ENTRIES_PER_PAGE;
        int rem = getNumberOfEntries() % NUMBER_OF_ENTRIES_PER_PAGE;

        if (intVal > 0 && rem > 0) {
            return intVal + 1;
        } else if (intVal == 0 && rem > 0) {
            return 1;
        } else if (intVal > 0 && rem == 0) {
            return intVal;
        }

        return 1;
    }

    private int getNumberOfEntries() {
        Collection oauthKeyCollection;
        Registry reg = getConfigSystemRegistry();
        ConsumerEntry[] entries;
        try {
            if (reg.resourceExists(DashboardConstants.OAUTH_KEY_STORE)) {
                oauthKeyCollection = (Collection) reg.get(DashboardConstants.OAUTH_KEY_STORE);
                return oauthKeyCollection.getChildCount();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

}
