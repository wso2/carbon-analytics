
/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dashboard.social.common.utils;

import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.wso2.carbon.registry.social.api.utils.FilterOperation;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.api.utils.SortOrder;
import org.wso2.carbon.registry.social.impl.utils.FilterOptionsImpl;

public class SocialUtils {
    private SocialUtils(){
        
    }
    public static String USER_DASHBOARD_REGISTRY_ROOT = "/users/";
     /* Listing Users */
    public static final String USER_LIST_FILTER_STRING="*";
    public static final int USER_LIST_SIZE=-1;
    /* Profile Image */
    public static final String DEFAULT_PROFILE="default";
    public static final String USER_ROOT="/users/";
    public static final String PROFILE_IMAGE_NAME="profileimage.jpg";
    public static final String  PROFILE_IMAGE_PATH="/gadget-server/profiles/default/profileimage";
    public static final int DEFAULT_PROFILE_FIELDS_COUNT=2; // Default no. of fields of a profile. FIRST_NAME & LAST_NAME. These fields have no privacy-setting option
    public static final String USER_PROFILE_DASHBOARD_REGISTRY_ROOT = "/gadget-server/profiles/";
    public static final String USER_PROFILE_FIELD_VISIBILITY = "visibility";
    public static final String VISIBILITY_NONE = "none";
    public static final String VISIBILITY_EVERYONE = "every-one";
    public static final String VISIBILITY_ONLYFRIENDS = "only-to-friends";
    public static final String RELATIONSHIP_STATUS_FRIEND = "friend";
    public static final String RELATIONSHIP_STATUS_SELF = "self";

    public static final String USER_PROFILE_IMAGE = "/profileimage/profileimage.jpg";
    public static final String USER_PROFILE_REGISTRY_ROOT = "/registry/resourceContent?path=/_system/config/users/";

    
    public static boolean isViewable(String relationshipStatus, String privacyOption) {
        boolean result = false;
        if(RELATIONSHIP_STATUS_SELF.equals(relationshipStatus)){
            return true;
        }
        String visibility = getVisibility(relationshipStatus);
        if (privacyOption.equals(VISIBILITY_NONE)) {
            result = false;
        } else if (privacyOption.equals(VISIBILITY_EVERYONE)) {
            result = true;
        } else if (privacyOption.equals(visibility)) {
            result = true;
        } else if (visibility.equals(VISIBILITY_ONLYFRIENDS) &&
                   privacyOption.equals(VISIBILITY_EVERYONE)) {
            result = true;
        }

        return result;
    }

    private static String getVisibility(String relationshipStatus) {
        String visibility = "";
         if (relationshipStatus.equals(RELATIONSHIP_STATUS_FRIEND)) {
            visibility =VISIBILITY_ONLYFRIENDS;
        } else {
            visibility = VISIBILITY_NONE;
        }
        return visibility;
    }

    /**
        * Converts {@link org.apache.shindig.social.opensocial.spi.CollectionOptions} to {@link org.wso2.carbon.registry.social.api.utils.FilterOptions}
        *
        * @param collectionOptions CollectionOptions object
        * @return FilterOptions object
        */

       public static FilterOptions convertCollectionOptionsToFilterOptions
               (CollectionOptions collectionOptions) {
           FilterOptions filterOptions = new FilterOptionsImpl();
           FilterOptions options = new FilterOptionsImpl();
           if (collectionOptions != null) {
               if (collectionOptions.getFilter() != null) {
                   options.setFilter(collectionOptions.getFilter());
               }
               if (collectionOptions.getFilterValue() != null) {
                   options.setFilterValue(collectionOptions.getFilterValue());
               }
               if (collectionOptions.getFilterOperation() != null &&
                       collectionOptions.getFilterOperation().name() != null) {
                   options.setFilterOperation(
                           FilterOperation.valueOf(collectionOptions.getFilterOperation().name()));
               }
               options.setFirst(collectionOptions.getFirst());
               options.setMax(collectionOptions.getMax());
               if (collectionOptions.getSortBy() != null) {
                   options.setSortBy(collectionOptions.getSortBy());
               }
               if (collectionOptions.getUpdatedSince() != null) {
                   options.setUpdatedSince(collectionOptions.getUpdatedSince());
               }
               if (collectionOptions.getSortOrder() != null &&
                       collectionOptions.getSortOrder().name() != null) {
                   options.setSortOrder(SortOrder.valueOf(collectionOptions.getSortOrder().name()));
               }

               return filterOptions;
           } else {
               return null;
           }
       }
}
