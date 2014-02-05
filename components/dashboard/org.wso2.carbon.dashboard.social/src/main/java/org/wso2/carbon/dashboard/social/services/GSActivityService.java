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
import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.core.model.MediaItemImpl;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.wso2.carbon.dashboard.social.common.utils.SocialUtils;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.activity.ActivityManager;
import org.wso2.carbon.registry.social.api.utils.FilterOptions;
import org.wso2.carbon.registry.social.impl.activity.ActivityManagerImpl;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;


//@Singleton

public class GSActivityService implements ActivityService {

    private ActivityManager manager = new ActivityManagerImpl();
    private final String MEDIA_ITEM_TYPE_AUDIO = "audio";
    private final String MEDIA_ITEM_TYPE_IMAGE = "image";
    private final String MEDIA_ITEM_TYPE_VIDEO = "video";

    /* @Inject
    public GSActivityService(){
        
    }*/


    public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds, GroupId groupId,
                                                             String appId, Set<String> fields,
                                                             CollectionOptions collectionOptions,
                                                             SecurityToken securityToken)
            throws ProtocolException {
        List<Activity> activitiesList = new ArrayList<Activity>();
        org.wso2.carbon.registry.social.api.activity.Activity[] activitiesArray;
        FilterOptions options = SocialUtils.convertCollectionOptionsToFilterOptions(collectionOptions);
        String[] userIdArray = new String[userIds.size()];
        int index = 0;
        for (UserId id : userIds) {
            if (id != null) {
                if (id.getUserId(securityToken).equals("null")) {
                    throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No userId specified");
                }
                userIdArray[index++] = id.getUserId(securityToken);
            }
        }

        String groupIdString = groupId.getType().name();
        try {
            activitiesArray = manager.getActivities(userIdArray, groupIdString, appId, fields, options);
        }
        catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        if (activitiesArray != null) {
            for (org.wso2.carbon.registry.social.api.activity.Activity item : activitiesArray) {
                if (item != null) {
                    activitiesList.add(convertToShindigActivity(item));
                }
            }
        }
        return ImmediateFuture.newInstance(new RestfulCollection<Activity>(activitiesList));
    }

    public Future<RestfulCollection<Activity>> getActivities(UserId userId, GroupId groupId,
                                                             String appId, Set<String> fields,
                                                             CollectionOptions collectionOptions,
                                                             Set<String> activityIds,
                                                             SecurityToken securityToken)
            throws ProtocolException {
        FilterOptions filterOptions = SocialUtils.convertCollectionOptionsToFilterOptions(collectionOptions);
        String groupIdString = groupId.getType().name();
        String userIdString = userId.getUserId(securityToken);
        if (userIdString.equals("null")) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No userId specified");
        }
        String[] activityIdsArray = new String[activityIds.size()];
        List<String> list = new ArrayList<String>(activityIds);
        activityIdsArray = list.toArray(activityIdsArray);
        org.wso2.carbon.registry.social.api.activity.Activity[] activitiesArray;
        List<Activity> activitiesList = new ArrayList<Activity>();
        try {
            activitiesArray = manager.getActivities(userIdString, groupIdString, appId, fields, filterOptions,
                    activityIdsArray);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        if (activitiesArray != null) {
            for (org.wso2.carbon.registry.social.api.activity.Activity item : activitiesArray) {
                if (item != null) {
                    activitiesList.add(convertToShindigActivity(item));
                }
            }
        }
        return ImmediateFuture.newInstance(new RestfulCollection<Activity>(activitiesList));
    }

    public Future<Activity> getActivity(UserId userId, GroupId groupId, String appId,
                                        Set<String> fields, String activityId, SecurityToken securityToken)
            throws ProtocolException {
        String userIdString = userId.getUserId(securityToken);
        if (userIdString.equals("null")) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No userId specified");
        }
        String groupIdString = groupId.getType().name();
        org.wso2.carbon.registry.social.api.activity.Activity activityObj;
        try {
            activityObj = manager.getActivity(userIdString, groupIdString, appId, fields, activityId);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return ImmediateFuture.newInstance(convertToShindigActivity(activityObj));

    }

    public Future<Void> deleteActivities(UserId userId, GroupId groupId, String appId,
                                         Set<String> activityIds, SecurityToken securityToken)
            throws ProtocolException {
        String userIdString = userId.getUserId(securityToken);
        String groupIdString = groupId.getType().name();
        try {
            manager.deleteActivities(userIdString, groupIdString, appId, activityIds);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        return ImmediateFuture.newInstance(null);
    }

    public Future<Void> createActivity(UserId userId, GroupId groupId, String appId,
                                       Set<String> fields, Activity activity,
                                       SecurityToken securityToken) throws ProtocolException {

        String userIdString = userId.getUserId(securityToken);
        String groupIdString = groupId.getType().name();

        try {
            manager.createActivity(userIdString, groupIdString, appId, fields, convertToSocialActivity(activity));
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return ImmediateFuture.newInstance(null);
    }


    /**
     * Converts {@link org.wso2.carbon.registry.social.api.activity.Activity} to {@link org.apache.shindig.social.opensocial.model.Activity}
     *
     * @param activityObj org.wso2.carbon.social.activity.Activity object
     * @return org.apache.shindig.social.opensocial.model.Activity object
     */

    private Activity convertToShindigActivity(org.wso2.carbon.registry.social.api.activity.Activity activityObj) {
        Activity resultObj = new ActivityImpl();
        if (activityObj.getAppId() != null) {
            resultObj.setAppId(activityObj.getAppId());
        }
        if (activityObj.getBody() != null) {
            resultObj.setBody(activityObj.getBody());
        }
        if (activityObj.getBodyId() != null) {
            resultObj.setBodyId(activityObj.getBodyId());
        }
        if (activityObj.getExternalId() != null) {
            resultObj.setExternalId(activityObj.getExternalId());
        }
        if (activityObj.getId() != null) {
            resultObj.setId(activityObj.getId());
        }
        if (activityObj.getPostedTime() != null) {
            resultObj.setPostedTime(activityObj.getPostedTime());
        }
        if (activityObj.getPriority() != null) {
            resultObj.setPriority(activityObj.getPriority());
        }
        if (activityObj.getStreamFaviconUrl() != null) {
            resultObj.setStreamFaviconUrl(activityObj.getStreamFaviconUrl());
        }
        if (activityObj.getStreamSourceUrl() != null) {
            resultObj.setStreamSourceUrl(activityObj.getStreamSourceUrl());
        }
        if (activityObj.getStreamTitle() != null) {
            resultObj.setStreamTitle(activityObj.getStreamTitle());
        }
        if (activityObj.getStreamUrl() != null) {
            resultObj.setStreamUrl(activityObj.getStreamUrl());
        }
        if (activityObj.getTemplateParams() != null) {
            resultObj.setTemplateParams(activityObj.getTemplateParams());
        }
        if (activityObj.getTitle() != null) {
            resultObj.setTitle(activityObj.getTitle());
        }
        if (activityObj.getTitleId() != null) {
            resultObj.setTitleId(activityObj.getTitleId());
        }
        if (activityObj.getUpdated() != null) {
            resultObj.setUpdated(activityObj.getUpdated());
        }
        if (activityObj.getUrl() != null) {
            resultObj.setUrl(activityObj.getUrl());
        }
        if (activityObj.getUserId() != null) {
            resultObj.setUserId(activityObj.getUserId());
        }
        List<MediaItem> mediaItemList = new ArrayList<MediaItem>();

        if (activityObj.getMediaItems() != null) {
            for (org.wso2.carbon.registry.social.api.activity.MediaItem item : activityObj.getMediaItems()) {
                MediaItem itemObj = new MediaItemImpl();
                if (item != null) {
                    if (item.getMimeType() != null) {
                        itemObj.setMimeType(item.getMimeType());
                    }
                    if (item.getThumbnailUrl() != null) {
                        itemObj.setThumbnailUrl(item.getThumbnailUrl());
                    }
                    if (item.getType() != null) {
                        org.wso2.carbon.registry.social.api.activity.MediaItem.Type itemType = item.getType();
                        if (itemType.name().equals(MEDIA_ITEM_TYPE_AUDIO)) {
                            itemObj.setType(MediaItem.Type.AUDIO);
                        }
                        if (itemType.name().equals(MEDIA_ITEM_TYPE_IMAGE)) {
                            itemObj.setType(MediaItem.Type.IMAGE);
                        }
                        if (itemType.name().equals(MEDIA_ITEM_TYPE_VIDEO)) {
                            itemObj.setType(MediaItem.Type.VIDEO);
                        }

                    }
                    if (item.getUrl() != null) {
                        itemObj.setUrl(item.getUrl());
                    }
                    mediaItemList.add(itemObj);
                }

            }
            resultObj.setMediaItems(mediaItemList);

        }

        return resultObj;
    }

    /**
     * Converts {@link org.apache.shindig.social.opensocial.model.Activity} to {@link org.wso2.carbon.registry.social.api.activity.Activity}
     *
     * @param activityObj org.apache.shindig.social.opensocial.model.Activity object
     * @return org.wso2.carbon.registry.social.api.activity.Activity object
     */


    private org.wso2.carbon.registry.social.api.activity.Activity convertToSocialActivity(Activity activityObj) {
        org.wso2.carbon.registry.social.api.activity.Activity resultObj =
                new org.wso2.carbon.registry.social.impl.activity.ActivityImpl();
        if (activityObj.getAppId() != null) {
            resultObj.setAppId(activityObj.getAppId());
        }
        if (activityObj.getBody() != null) {
            resultObj.setBody(activityObj.getBody());
        }
        if (activityObj.getBodyId() != null) {
            resultObj.setBodyId(activityObj.getBodyId());
        }
        if (activityObj.getExternalId() != null) {
            resultObj.setExternalId(activityObj.getExternalId());
        }
        if (activityObj.getId() != null) {
            resultObj.setId(activityObj.getId());
        }
        if (activityObj.getPostedTime() != null) {
            resultObj.setPostedTime(activityObj.getPostedTime());
        }
        if (activityObj.getPriority() != null) {
            resultObj.setPriority(activityObj.getPriority());
        }
        if (activityObj.getStreamFaviconUrl() != null) {
            resultObj.setStreamFaviconUrl(activityObj.getStreamFaviconUrl());
        }
        if (activityObj.getStreamSourceUrl() != null) {
            resultObj.setStreamSourceUrl(activityObj.getStreamSourceUrl());
        }
        if (activityObj.getStreamTitle() != null) {
            resultObj.setStreamTitle(activityObj.getStreamTitle());
        }
        if (activityObj.getStreamUrl() != null) {
            resultObj.setStreamUrl(activityObj.getStreamUrl());
        }
        if (activityObj.getTemplateParams() != null) {
            resultObj.setTemplateParams(activityObj.getTemplateParams());
        }
        if (activityObj.getTitle() != null) {
            resultObj.setTitle(activityObj.getTitle());
        }
        if (activityObj.getTitleId() != null) {
            resultObj.setTitleId(activityObj.getTitleId());
        }
        if (activityObj.getUpdated() != null) {
            resultObj.setUpdated(activityObj.getUpdated());
        }
        if (activityObj.getUrl() != null) {
            resultObj.setUrl(activityObj.getUrl());
        }
        if (activityObj.getUserId() != null) {
            resultObj.setUserId(activityObj.getUserId());
        }
        List<org.wso2.carbon.registry.social.api.activity.MediaItem> mediaItemList =
                new ArrayList<org.wso2.carbon.registry.social.api.activity.MediaItem>();

        if (activityObj.getMediaItems() != null) {
            for (MediaItem item : activityObj.getMediaItems()) {
                org.wso2.carbon.registry.social.api.activity.MediaItem itemObj =
                        new org.wso2.carbon.registry.social.impl.activity.MediaItemImpl();
                if (item != null) {
                    if (item.getMimeType() != null) {
                        itemObj.setMimeType(item.getMimeType());
                    }
                    if (item.getThumbnailUrl() != null) {
                        itemObj.setThumbnailUrl(item.getThumbnailUrl());
                    }
                    if (item.getType() != null) {
                        MediaItem.Type itemType = item.getType();
                        if (itemType.name().equals(MEDIA_ITEM_TYPE_AUDIO)) {
                            itemObj.setType(org.wso2.carbon.registry.social.api.activity.MediaItem.Type.AUDIO);
                        }
                        if (itemType.name().equals(MEDIA_ITEM_TYPE_IMAGE)) {
                            itemObj.setType(org.wso2.carbon.registry.social.api.activity.MediaItem.Type.IMAGE);
                        }
                        if (itemType.name().equals(MEDIA_ITEM_TYPE_VIDEO)) {
                            itemObj.setType(org.wso2.carbon.registry.social.api.activity.MediaItem.Type.VIDEO);
                        }

                    }
                    if (item.getUrl() != null) {
                        itemObj.setUrl(item.getUrl());
                    }
                    mediaItemList.add(itemObj);
                }

            }
            resultObj.setMediaItems(mediaItemList);
        }

        return resultObj;
    }
}
