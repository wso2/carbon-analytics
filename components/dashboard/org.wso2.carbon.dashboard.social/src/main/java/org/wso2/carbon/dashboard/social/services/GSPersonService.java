/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.dashboard.social.services;


import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.core.model.*;
import org.apache.shindig.social.opensocial.model.*;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.wso2.carbon.dashboard.social.common.utils.SocialUtils;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.people.PersonManager;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.Future;


//@Singleton
public class GSPersonService implements PersonService {
    private PersonManager personManager = new PersonManagerImpl();
    /*  @Inject
    public GSPersonService(){
      
    }*/

    public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId,
                                                       CollectionOptions collectionOptions,
                                                       Set<String> fields,
                                                       SecurityToken securityToken)
            throws ProtocolException {
        org.wso2.carbon.registry.social.api.people.userprofile.Person[] resultPersonArray;
        List<Person> resultList = new ArrayList<Person>();
        String[] userIdArray = new String[userIds.size()];
        int index = 0;
        for (UserId id : userIds) {
            if (id != null) {
                userIdArray[index++] = id.getUserId(securityToken);
            }
        }
        String groupIdString = groupId.getType().name();
        FilterOptions filterOptions = SocialUtils.convertCollectionOptionsToFilterOptions(collectionOptions);
        String[] userFieldsArray;
        if (fields != null) {
            userFieldsArray = new String[fields.size()];
            List<String> userFieldsList = new ArrayList<String>(fields);
            userFieldsArray = userFieldsList.toArray(userFieldsArray);
        } else {
            userFieldsArray = null;
        }
        try {
            resultPersonArray = personManager.getPeople(userIdArray, groupIdString, filterOptions, userFieldsArray);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        if (resultPersonArray != null) {
            for (org.wso2.carbon.registry.social.api.people.userprofile.Person person : resultPersonArray) {
                if (person != null) {
                    resultList.add(convertToShindigPersonModel(person));
                }
            }

        }
        if (GroupId.Type.self == groupId.getType() && resultList.isEmpty()) {
            throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Person not found");
        }

        if (collectionOptions.getSortBy().equals(Person.Field.NAME.toString())) {
            Collections.sort(resultList, NAME_COMPARATOR);
        }

        if (collectionOptions.getSortOrder() == SortOrder.descending) {
            Collections.reverse(resultList);
        }
        int totalSize = resultList.size();
        int last = collectionOptions.getFirst() + collectionOptions.getMax();
        resultList = resultList.subList(collectionOptions.getFirst(), Math.min(last, totalSize));

        return ImmediateFuture.newInstance(new RestfulCollection<Person>(
                resultList, collectionOptions.getFirst(), totalSize, collectionOptions.getMax()));


    }


    public Future<Person> getPerson(UserId userId, Set<String> fields, SecurityToken securityToken)
            throws ProtocolException {

        String userIdString = userId.getUserId(securityToken);
        if (userIdString.equals("null")) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No userId specified");
        }
        Person resultPersonObj = null;
        org.wso2.carbon.registry.social.api.people.userprofile.Person person;
        String[] userFieldsArray;
        if (fields != null) {
            userFieldsArray = new String[fields.size()];
            List<String> userFieldsList = new ArrayList<String>(fields);
            userFieldsArray = userFieldsList.toArray(userFieldsArray);
        } else {
            userFieldsArray = null;
        }
        try {
            person = personManager.getPerson(userIdString, userFieldsArray);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        if (person != null) {
            resultPersonObj = convertToShindigPersonModel(person);
        }
        return ImmediateFuture.newInstance(resultPersonObj);
    }

    private static final Comparator<Person> NAME_COMPARATOR = new Comparator<Person>() {
        public int compare(Person person, Person person1) {
            String name = person.getName().getFormatted();
            String name1 = person1.getName().getFormatted();
            return name.compareTo(name1);
        }
    };

    private Person convertToShindigPersonModel(org.wso2.carbon.registry.social.api.people.userprofile.Person personObj) {
        Person resultObj = new PersonImpl();
        if (personObj.getAboutMe() != null) {
            resultObj.setAboutMe(personObj.getAboutMe());
        }
        if (personObj.getAccounts() != null) {
            List<Account> accounts = new ArrayList<Account>();
            for (org.wso2.carbon.registry.social.api.people.userprofile.model.Account account : personObj.getAccounts()) {
                if (account != null) {
                    Account accountObj = new AccountImpl();
                    if (account.getDomain() != null) {
                        accountObj.setDomain(account.getDomain());
                    }
                    if (account.getUserId() != null) {
                        accountObj.setUserId(account.getUserId());
                    }
                    if (account.getUsername() != null) {
                        accountObj.setUsername(account.getUsername());
                    }
                    accounts.add(accountObj);
                }
            }
            resultObj.setAccounts(accounts);
        }
        if (personObj.getActivities() != null) {
            resultObj.setActivities(personObj.getActivities());
        }
        if (personObj.getAddresses() != null) {
            List<Address> addressList = new ArrayList<Address>();
            for (org.wso2.carbon.registry.social.api.people.userprofile.model.Address address : personObj.getAddresses()) {
                if (address != null) {
                    addressList.add(convertToShindigAddressModel(address));
                }
            }
            resultObj.setAddresses(addressList);
        }
        if (personObj.getAge() != null) {
            resultObj.setAge(personObj.getAge());
        }
        if (personObj.getAppData() != null) {
            resultObj.setAppData(personObj.getAppData());
        }
        if (personObj.getBirthday() != null) {
            resultObj.setBirthday(personObj.getBirthday());
        }
        if (personObj.getBooks() != null) {
            resultObj.setBooks(personObj.getBooks());
        }
        if (personObj.getCars() != null) {
            resultObj.setCars(personObj.getCars());
        }
        if (personObj.getChildren() != null) {
            resultObj.setChildren(personObj.getChildren());
        }
        if (personObj.getCurrentLocation() != null) {
            resultObj.setCurrentLocation(convertToShindigAddressModel(personObj.getCurrentLocation()));
        }
        if (personObj.getName() != null) {
            Name nameObj = new NameImpl();
            org.wso2.carbon.registry.social.api.people.userprofile.model.Name name = personObj.getName();
            if (name.getGivenName() != null) {
                nameObj.setGivenName(name.getGivenName());
            }
            if (name.getFamilyName() != null) {
                nameObj.setFamilyName(name.getFamilyName());
            }
            if (name.getAdditionalName() != null) {
                nameObj.setAdditionalName(name.getAdditionalName());
            }
            if (name.getFormatted() != null) {
                nameObj.setFormatted(name.getFormatted());
            }
            if (name.getHonorificPrefix() != null) {
                nameObj.setHonorificPrefix(name.getHonorificPrefix());
            }
            if (name.getHonorificSuffix() != null) {
                nameObj.setHonorificSuffix(name.getHonorificSuffix());
            }
            resultObj.setName(nameObj);
        }
        if (personObj.getDisplayName() != null) {
            resultObj.setDisplayName(personObj.getDisplayName());
        }

        if (personObj.getEmails() != null) {
            List<ListField> emailsList = new ArrayList<ListField>();
            for (org.wso2.carbon.registry.social.api.people.userprofile.model.ListField entry : personObj.getEmails()) {
                if (entry != null) {
                    emailsList.add(convertToShindigListFieldModel(entry));
                }
            }
            resultObj.setEmails(emailsList);
        }
        if (personObj.getIms() != null) {
            List<ListField> imsList = new ArrayList<ListField>();
            for (org.wso2.carbon.registry.social.api.people.userprofile.model.ListField entry : personObj.getIms()) {
                if (entry != null) {
                    imsList.add(convertToShindigListFieldModel(entry));
                }
            }
            resultObj.setIms(imsList);
        }
        if (personObj.getEthnicity() != null) {
            resultObj.setEthnicity(personObj.getEthnicity());
        }
        if (personObj.getFashion() != null) {
            resultObj.setFashion(personObj.getFashion());
        }
        if (personObj.getFood() != null) {
            resultObj.setFood(personObj.getFood());
        }
        if (personObj.getGender() != null) {
            if ("female".equals(personObj.getGender().name())) {
                resultObj.setGender(Person.Gender.female);
            } else if ("male".equals(personObj.getGender().name())) {
                resultObj.setGender(Person.Gender.male);
            }
        }
        if (personObj.getOrganizations() != null) {
            List<Organization> orgsList = new ArrayList<Organization>();
            for (org.wso2.carbon.registry.social.api.people.userprofile.model.Organization org : personObj.getOrganizations()) {
                if (org != null) {
                    orgsList.add(convertToShindigOrganizationModel(org));
                }
            }
            resultObj.setOrganizations(orgsList);
        }

        if (personObj.getHappiestWhen() != null) {
            resultObj.setHappiestWhen(personObj.getHappiestWhen());
        }
        if (personObj.getHeroes() != null) {
            resultObj.setHeroes(personObj.getHeroes());
        }
        if (personObj.getHumor() != null) {
            resultObj.setHumor(personObj.getHumor());
        }
        if (personObj.getId() != null) {
            resultObj.setId(personObj.getId());
        }
        if (personObj.getInterests() != null) {
            resultObj.setInterests(personObj.getInterests());
        }
        if (personObj.getJobInterests() != null) {
            resultObj.setJobInterests(personObj.getJobInterests());
        }
        if (personObj.getLanguagesSpoken() != null) {
            resultObj.setLanguagesSpoken(personObj.getLanguagesSpoken());
        }
        if (personObj.getLivingArrangement() != null) {
            resultObj.setLivingArrangement(personObj.getLivingArrangement());
        }
        if (personObj.getMovies() != null) {
            resultObj.setMovies(personObj.getMovies());
        }
        if (personObj.getMusic() != null) {
            resultObj.setMusic(personObj.getMusic());
        }
        if (personObj.getNickname() != null) {
            resultObj.setNickname(personObj.getNickname());
        }
        if (personObj.getPets() != null) {
            resultObj.setPets(personObj.getPets());
        }
        if (personObj.getPhoneNumbers() != null) {
            List<ListField> phoneNoList = new ArrayList<ListField>();
            for (org.wso2.carbon.registry.social.api.people.userprofile.model.ListField entry : personObj.getPhoneNumbers()) {
                if (entry != null) {
                    phoneNoList.add(convertToShindigListFieldModel(entry));
                }
            }
            resultObj.setIms(phoneNoList);
        }
        if (personObj.getPoliticalViews() != null) {
            resultObj.setPoliticalViews(personObj.getPoliticalViews());
        }
        if (personObj.getUpdated() != null) {
            resultObj.setUpdated(personObj.getUpdated());
        }
        if (personObj.getThumbnailUrl() != null) {
            resultObj.setThumbnailUrl(personObj.getThumbnailUrl());
        }
        if (personObj.getStatus() != null) {
            resultObj.setStatus(personObj.getStatus());
        }
        if (personObj.getReligion() != null) {
            resultObj.setReligion(personObj.getReligion());
        }
        if (personObj.getRelationshipStatus() != null) {
            resultObj.setRelationshipStatus(personObj.getRelationshipStatus());
        }
        if (personObj.getQuotes() != null) {
            resultObj.setQuotes(personObj.getQuotes());
        }
        if (personObj.getProfileUrl() != null) {
            resultObj.setProfileUrl(personObj.getProfileUrl());
        }
        if (personObj.getPreferredUsername() != null) {
            resultObj.setPreferredUsername(personObj.getPreferredUsername());
        }
        //TODO: other fields
        return resultObj;
    }

    private Address convertToShindigAddressModel(org.wso2.carbon.registry.social.api.people.userprofile.model.Address address) {
        Address resultObj = new AddressImpl();
        if (address.getCountry() != null) {
            resultObj.setCountry(address.getCountry());
        }
        if (address.getFormatted() != null) {
            resultObj.setFormatted(address.getFormatted());
        }
        if (address.getLatitude() != null) {
            resultObj.setLatitude(address.getLatitude());
        }
        if (address.getLocality() != null) {
            resultObj.setLocality(address.getLocality());
        }
        if (address.getLongitude() != null) {
            resultObj.setLongitude(address.getLongitude());
        }
        if (address.getPostalCode() != null) {
            resultObj.setPostalCode(address.getPostalCode());
        }
        if (address.getRegion() != null) {
            resultObj.setRegion(address.getRegion());
        }
        if (address.getStreetAddress() != null) {
            resultObj.setStreetAddress(address.getStreetAddress());
        }
        if (address.getType() != null) {
            resultObj.setType(address.getType());
        }
        resultObj.setPrimary(address.getPrimary());
        return resultObj;
    }

    private Organization convertToShindigOrganizationModel(org.wso2.carbon.registry.social.api.people.userprofile.model.Organization org) {
        Organization orgObj = new OrganizationImpl();
        if (org.getName() != null) {
            orgObj.setName(org.getName());
        }
        if (org.getDescription() != null) {
            orgObj.setDescription(org.getDescription());
        }
        if (org.getField() != null) {
            orgObj.setField(org.getField());
        }
        if (org.getTitle() != null) {
            orgObj.setTitle(org.getTitle());
        }
        orgObj.setPrimary(org.getPrimary());
        if (org.getAddress() != null) {
            orgObj.setAddress(convertToShindigAddressModel(org.getAddress()));
        }
        if (org.getEndDate() != null) {
            orgObj.setEndDate(org.getEndDate());
        }
        if (org.getSalary() != null) {
            orgObj.setSalary(org.getSalary());
        }
        if (org.getStartDate() != null) {
            orgObj.setStartDate(org.getStartDate());
        }
        if (org.getSubField() != null) {
            orgObj.setSubField(org.getSubField());
        }
        if (org.getType() != null) {
            orgObj.setType(org.getType());
        }
        if (org.getWebpage() != null) {
            orgObj.setWebpage(org.getWebpage());
        }
        return orgObj;
    }

    private ListField convertToShindigListFieldModel(org.wso2.carbon.registry.social.api.people.userprofile.model.ListField field) {
        ListField resultObj = new ListFieldImpl();
        if (field.getType() != null) {
            resultObj.setType(field.getType());
        }
        if (field.getValue() != null) {
            resultObj.setValue(field.getValue());
        }
        resultObj.setPrimary(field.getPrimary());
        return resultObj;
    }
}
