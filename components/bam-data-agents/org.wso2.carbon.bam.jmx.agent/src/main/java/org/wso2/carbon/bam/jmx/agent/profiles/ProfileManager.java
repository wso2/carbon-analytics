/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.bam.jmx.agent.profiles;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.jmx.agent.exceptions.JmxProfileException;
import org.wso2.carbon.bam.jmx.agent.exceptions.ProfileAlreadyExistsException;
import org.wso2.carbon.bam.jmx.agent.exceptions.ProfileDoesNotExistException;
import org.wso2.carbon.bam.jmx.agent.tasks.internal.JmxTaskServiceComponent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileManager extends RegistryAbstractAdmin {

    private static final Log log = LogFactory.getLog(ProfileManager.class);

    static final String PROFILE_SAVE_REG_LOCATION =  "repository/components/org.wso2.carbon.publish.jmx.agent/";
    
    private Registry registry;

    public ProfileManager() {
        RegistryService registryService = JmxTaskServiceComponent.getRegistryService();
        TenantRegistryLoader tenantRegistryLoader = JmxTaskServiceComponent.getTenantRegistryLoader();

        //get the tenant's registry
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        tenantRegistryLoader.loadTenantRegistry(tenantId);
        try {
            registry = registryService.getGovernanceSystemRegistry(tenantId);
        } catch (RegistryException e) {
            log.error("Error obtaining the registry " + e.getMessage(), e);
        }
    }

    /**
     * Encrypts the sensitive data in the Profile.
     * Currently encrypts only the passwords of the JMX
     * server and Data publisher.
     *
     * @param profile The profile which has the data to be encrypted
     * @return The profile with the encrypted data
     */
    private Profile encryptData(Profile profile) throws CryptoException {

        //encrypt the JMX server password
        String password = profile.getPass();

        //encrypt the password
        String cipherT =
                CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(password.getBytes());
        profile.setPass(cipherT);

        //encrypt the data publisher password
        String dpPassword = profile.getDpPassword();

        //encrypt the password
        String dpCipherT =
                CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(dpPassword.getBytes());
        profile.setDpPassword(dpCipherT);

        return profile;

    }

    /**
     * Decrypts the sensitive data in the Profile.
     *
     * @param profile The profile which has the data to be decrypted
     * @return The profile with the decrypted data
     */
    private Profile decryptData(Profile profile) throws CryptoException {

        String cipherT = profile.getPass();

        //decrypt the jmx server password
        byte[] decodedBArr = Base64.decodeBase64(cipherT.getBytes());
        byte[] passwordBArr = CryptoUtil.getDefaultCryptoUtil().decrypt(decodedBArr);
        String password = new String(passwordBArr);

        profile.setPass(password);

        //decrypt the data publisher password
        String dpCipherT = profile.getDpPassword();

        byte[] decodedDPBArr = Base64.decodeBase64(dpCipherT.getBytes());
        byte[] passwordDPBArr = CryptoUtil.getDefaultCryptoUtil().decrypt(decodedDPBArr);
        String dpPassword = new String(passwordDPBArr);

        profile.setDpPassword(dpPassword);

        return profile;
    }

    /**
     * Used to add a new JMX monitoring profile.
     *
     * @param profile The profile that needs to be added
     * @return Returns whether adding the profile was successful or not
     * @throws ProfileAlreadyExistsException
     */
    public boolean addProfile(Profile profile)
            throws ProfileAlreadyExistsException, JmxProfileException {

        String path = PROFILE_SAVE_REG_LOCATION + profile.getName();
        boolean resourceExist;

        try {
            //check whether the profile already exists
            resourceExist = registry.resourceExists(path);
        } catch (RegistryException e) {
            log.error("Unable to access to registry", e);
            throw new JmxProfileException("Unable to access to registry", e);
        }
        // if resource already exist
        if (resourceExist) {
            String error = "The profile " + profile.getName() + " already exists.";
            log.error(error);
            throw new ProfileAlreadyExistsException(error);

        } else {
            //encrypt data
            try {
                profile = encryptData(profile);
            } catch (CryptoException e) {
                log.error("Unable to encrypt profile", e);
                throw new JmxProfileException("Unable to encrypt profile", e);
            }

            JAXBContext jaxbContext;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                jaxbContext = JAXBContext.newInstance(Profile.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.marshal(profile, byteArrayOutputStream);
            } catch (JAXBException e) {
                log.error("JAXB marshalling exception :" + e.getMessage(), e);
                throw new JmxProfileException("JAXB marshalling exception :" + e.getMessage(), e);
            }

            try {
                Resource res = registry.newResource();
                res.setContent(byteArrayOutputStream.toString());
                //save the profile
                registry.put(path, res);
            } catch (RegistryException e) {
                log.error("Unable to save in registry", e);
                throw new JmxProfileException("Unable to save in registry", e);
            }

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                // Just log the exception. Do nothing.
                log.warn("Unable to close byte stream ...", e);
            }

            return true;
        }
    }

    public Profile getProfile(String profileName)
            throws ProfileDoesNotExistException, JmxProfileException {

        String path = PROFILE_SAVE_REG_LOCATION + profileName;

        //if the profile does not exist
        try {
            if (!registry.resourceExists(path)) {
                String error = "The profile " + profileName + " does not exist.";
                log.error(error);
                throw new ProfileDoesNotExistException(error);
            }
        } catch (RegistryException e) {
            log.error("Unable to access to registry", e);
            throw new JmxProfileException("Unable to access to registry", e);
        }

        ByteArrayInputStream byteArrayInputStream;
        try {
            //if the profile exists
            Resource res = registry.get(path);
            byteArrayInputStream = new ByteArrayInputStream((byte[]) res.getContent());
        } catch (RegistryException e) {
            log.error("Unable to get profile :" + e.getMessage(), e);
            throw new JmxProfileException("Unable to get profile :" + e.getMessage(), e);
        }

        Profile profile;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Profile.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            profile = (Profile) jaxbUnmarshaller.unmarshal(byteArrayInputStream);
        } catch (JAXBException e) {
            log.error("JAXB unmarshalling exception :" + e.getMessage(), e);
            throw new JmxProfileException("JAXB unmarshalling exception :" + e.getMessage(), e);
        }

        try {
            profile = decryptData(profile);
        } catch (CryptoException e) {
            log.error("Unable to decrypt profile", e);
            throw new JmxProfileException("Unable to decrypt profile", e);
        }

        try {
            byteArrayInputStream.close();
        } catch (IOException e) {
            // Just log the exception. Do nothing.
            log.warn("Unable to close byte stream ...", e);
        }

        //return profile
        return profile;
    }

    public boolean updateProfile(Profile profile)
            throws ProfileDoesNotExistException, JmxProfileException {

        String path = PROFILE_SAVE_REG_LOCATION + profile.getName();

        try {
            //check whether the profile already exists
            if (!registry.resourceExists(path)) {
                String error =
                        "Cannot Update: The profile " + profile.getName() + " does not exist.";
                log.error(error);
                throw new ProfileDoesNotExistException(error);
            }
        } catch (RegistryException e) {
            log.error("Unable to access to registry", e);
            throw new JmxProfileException("Unable to access to registry", e);
        }

        try {
            //encrypt data
            profile = encryptData(profile);
        } catch (CryptoException e) {
            log.error("Unable to encrypt profile", e);
            throw new JmxProfileException("Unable to encrypt profile", e);
        }

        JAXBContext jaxbContext;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            jaxbContext = JAXBContext.newInstance(Profile.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(profile, byteArrayOutputStream);
        } catch (JAXBException e) {
            log.error("JAXB unmarshalling exception :" + e.getMessage(), e);
            throw new JmxProfileException("JAXB unmarshalling exception :" + e.getMessage(), e);
        }

        //replace the profile if it exists
        try {
            Resource res = registry.newResource();
            res.setContent(byteArrayOutputStream.toString());
            //delete the existing profile
            registry.delete(path);
            //save the new profile
            registry.put(path, res);
        } catch (RegistryException e) {
            log.error("Unable to save in registry", e);
            throw new JmxProfileException("Unable to save in registry", e);
        }

        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            // Just log the exception. Do nothing.
            log.warn("Unable to close byte stream ...", e);
        }

        return true;
    }

    public boolean deleteProfile(String profileName)
            throws ProfileDoesNotExistException, JmxProfileException {

        String path = PROFILE_SAVE_REG_LOCATION + profileName;

        try {
            //check whether the profile already exists
            if (!registry.resourceExists(path)) {
                String error = "Cannot Delete: The profile " + profileName + " does not exist.";
                log.error(error);
                throw new ProfileDoesNotExistException(error);
            } else {
                //if the profile exists
                registry.delete(path);
                return true;
            }
        } catch (RegistryException e) {
            log.error("Unable to delete profile", e);
            throw new JmxProfileException("Unable to to delete profile", e);
        }
    }

    /**
     * Returns an array of all the profiles regardless of their state
     *
     * @return an array of all the profiles regardless of their state
     */
    public Profile[] getAllProfiles() throws JmxProfileException {

        Profile[] profiles = null;

        try {
            if (registry.resourceExists(PROFILE_SAVE_REG_LOCATION)) {

                Resource folder = registry.get(PROFILE_SAVE_REG_LOCATION);
                String[] content = (String[]) folder.getContent();

                //initiate the profiles array
                profiles = new Profile[content.length];
                int counter = 0;
                Profile profile;
                ByteArrayInputStream byteArrayInputStream;
                JAXBContext jaxbContext = JAXBContext.newInstance(Profile.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                //iterate through the profiles
                for (String path : content) {
                    Resource res = registry.get(path);
                    try {
                        byteArrayInputStream = new ByteArrayInputStream((byte[]) res.getContent());
                        profile = (Profile) jaxbUnmarshaller.unmarshal(byteArrayInputStream);
                    } catch (JAXBException e) {
                        log.error("JAXB unmarshalling exception :" + e.getMessage(), e);
                        throw new JmxProfileException("JAXB unmarshalling exception :" + e.getMessage(), e);
                    }

                    try {
                        byteArrayInputStream.close();
                    } catch (IOException e) {
                        // Just log the exception. Do nothing.
                        log.warn("Unable to close byte stream ...", e);
                    }

                    //decrypt data
                    try {
                        profile = decryptData(profile);
                    } catch (CryptoException e) {
                        log.error("Unable to decrypt profile", e);
                        throw new JmxProfileException("Unable to decrypt profile", e);
                    }
                    profiles[counter++] = profile;
                }
            }
        } catch (RegistryException e) {
            log.error("Unable to access to registry", e);
            throw new JmxProfileException("Unable to access to registry", e);
        } catch (JAXBException e) {
            log.error("JAXB unmarshalling exception :" + e.getMessage(), e);
            throw new JmxProfileException("JAXB unmarshalling exception :" + e.getMessage(), e);
        }

        return profiles;
    }

    /**
     * Creates the profile for the toolbox
     *
     * @return - Returns the created profile
     */
    public Profile createToolboxProfile() throws ProfileAlreadyExistsException,
                                                 JmxProfileException {

        Profile tbProfile = new Profile();

        //set basic information
        tbProfile.setName("toolbox");
        tbProfile.setVersion(1);


        //set the data publisher info
        tbProfile.setDpReceiverConnectionType("tcp://");
        int receiverPort = 7611 + getPortOffset();
        tbProfile.setDpReceiverAddress("127.0.0.1:" + receiverPort);
        tbProfile.setDpSecureUrlConnectionType("ssl://");
        int secureReceiverPort = 7711 + getPortOffset();
        tbProfile.setDpSecureAddress("127.0.0.1:" + secureReceiverPort);

        tbProfile.setDpUserName("admin");
        tbProfile.setDpPassword("admin");

        tbProfile.setCronExpression("0/2 * * ? * *");

        //set the JMX server information
        int rmiServerPort = 11111 + getPortOffset();
        int rmiRegistryPort = 9999 + getPortOffset();
        tbProfile.setUrl("service:jmx:rmi://localhost:" + rmiServerPort + "/jndi/rmi://localhost:" + rmiRegistryPort + "/jmxrmi");
        tbProfile.setUserName("admin");
        tbProfile.setPass("admin");

        MBean[] mBeans = new MBean[4];

        /* Memory MBean*/
        MBean memoryMBean = new MBean();
        memoryMBean.setMBeanName("java.lang:type=Memory");

        MBeanAttribute[] memoryMBeanAttributes = new MBeanAttribute[2];

        /* Heap memory usage*/
        MBeanAttribute heapMemoryAttribute = new MBeanAttribute();
        heapMemoryAttribute.setAttributeName("HeapMemoryUsage");

        MBeanAttributeProperty[] heapMemoryMBeanAttributeProperties = new MBeanAttributeProperty[4];

        MBeanAttributeProperty heapInit = new MBeanAttributeProperty();
        heapInit.setPropertyName("init");
        heapInit.setAliasName("heap_mem_init");
        heapMemoryMBeanAttributeProperties[0] = heapInit;

        MBeanAttributeProperty heapMax = new MBeanAttributeProperty();
        heapMax.setPropertyName("max");
        heapMax.setAliasName("heap_mem_max");
        heapMemoryMBeanAttributeProperties[1] = heapMax;

        MBeanAttributeProperty heapUsed = new MBeanAttributeProperty();
        heapUsed.setPropertyName("used");
        heapUsed.setAliasName("heap_mem_used");
        heapMemoryMBeanAttributeProperties[2] = heapUsed;

        MBeanAttributeProperty heapCommitted = new MBeanAttributeProperty();
        heapCommitted.setPropertyName("committed");
        heapCommitted.setAliasName("heap_mem_committed");
        heapMemoryMBeanAttributeProperties[3] = heapCommitted;

        heapMemoryAttribute.setProperties(heapMemoryMBeanAttributeProperties);
        memoryMBeanAttributes[0] = heapMemoryAttribute;

        /* Non Heap memory usage*/
        MBeanAttribute nonHeapMemoryAttribute = new MBeanAttribute();
        nonHeapMemoryAttribute.setAttributeName("NonHeapMemoryUsage");

        MBeanAttributeProperty[] nonHeapMBeanAttributeProperties = new MBeanAttributeProperty[4];

        MBeanAttributeProperty nonHeapInit = new MBeanAttributeProperty();
        nonHeapInit.setPropertyName("init");
        nonHeapInit.setAliasName("non_heap_mem_init");
        nonHeapMBeanAttributeProperties[0] = nonHeapInit;

        MBeanAttributeProperty nonHeapMax = new MBeanAttributeProperty();
        nonHeapMax.setPropertyName("max");
        nonHeapMax.setAliasName("non_heap_mem_max");
        nonHeapMBeanAttributeProperties[1] = nonHeapMax;

        MBeanAttributeProperty nonHeapUsed = new MBeanAttributeProperty();
        nonHeapUsed.setPropertyName("used");
        nonHeapUsed.setAliasName("non_heap_mem_used");
        nonHeapMBeanAttributeProperties[2] = nonHeapUsed;

        MBeanAttributeProperty nonHeapCommitted = new MBeanAttributeProperty();
        nonHeapCommitted.setPropertyName("committed");
        nonHeapCommitted.setAliasName("non_heap_mem_committed");
        nonHeapMBeanAttributeProperties[3] = nonHeapCommitted;

        nonHeapMemoryAttribute.setProperties(nonHeapMBeanAttributeProperties);
        memoryMBeanAttributes[1] = nonHeapMemoryAttribute;

        memoryMBean.setAttributes(memoryMBeanAttributes);

        mBeans[0] = memoryMBean;

        /* Operating system MBean */
        MBean opsMBean = new MBean();
        opsMBean.setMBeanName("java.lang:type=OperatingSystem");

        MBeanAttribute cpuTimeAttribute = new MBeanAttribute();
        cpuTimeAttribute.setAttributeName("ProcessCpuTime");
        cpuTimeAttribute.setAliasName("processCpuTime");

        MBeanAttribute[] opsMBeanAttributes = new MBeanAttribute[1];
        opsMBeanAttributes[0] = cpuTimeAttribute;

        opsMBean.setAttributes(opsMBeanAttributes);

        mBeans[1] = opsMBean;

        /* Class loading MBean */
        MBean classLoadingMBean = new MBean();
        classLoadingMBean.setMBeanName("java.lang:type=ClassLoading");

        MBeanAttribute classCountAttribute = new MBeanAttribute();
        classCountAttribute.setAttributeName("LoadedClassCount");
        classCountAttribute.setAliasName("loadedClassCount");

        MBeanAttribute[] classLoadingAttributes = new MBeanAttribute[1];
        classLoadingAttributes[0] = classCountAttribute;

        classLoadingMBean.setAttributes(classLoadingAttributes);

        mBeans[2] = classLoadingMBean;

        /* Threading MBean*/
        MBean threadingMBean = new MBean();
        threadingMBean.setMBeanName("java.lang:type=Threading");

        MBeanAttribute threadCountAttribute = new MBeanAttribute();
        threadCountAttribute.setAttributeName("ThreadCount");
        threadCountAttribute.setAliasName("threadCount");

        MBeanAttribute peakCountAttribute = new MBeanAttribute();
        peakCountAttribute.setAttributeName("PeakThreadCount");
        peakCountAttribute.setAliasName("peakThreadCount");

        MBeanAttribute[] threadingAttributes = new MBeanAttribute[2];
        threadingAttributes[0] = threadCountAttribute;
        threadingAttributes[1] = peakCountAttribute;

        threadingMBean.setAttributes(threadingAttributes);

        mBeans[3] = threadingMBean;

        tbProfile.setSelectedMBeans(mBeans);

        //keep the profile deactivated at the beginning
        tbProfile.setActive(false);

        this.addProfile(tbProfile);

        return tbProfile;

    }

    public static int getPortOffset() {
        return CarbonUtils.getPortFromServerConfig("Ports.Offset") + 1;
    }
}
