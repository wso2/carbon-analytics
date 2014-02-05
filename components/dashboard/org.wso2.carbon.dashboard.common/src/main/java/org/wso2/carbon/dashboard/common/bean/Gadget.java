package org.wso2.carbon.dashboard.common.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The bean represent a gadget (meta information) in the dashboard
 */
public class Gadget {
    private String gadgetId;
    private String gadgetName;
    private String gadgetPrefs;
    private String gadgetUrl;
    private String gadgetDesc;
    private String gadgetScreenBase64;
    private String gadgetPath;
    private Float rating;
    private String userCount;
    private String defaultGadget;
    private String unsignedUserGadget;
    private String thumbUrl;
    private Comment[] comments;

    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getDefaultGadget() {
        return defaultGadget;
    }

    public void setDefaultGadget(String defaultGadget) {
        this.defaultGadget = defaultGadget;
    }

    public String getGadgetDesc() {
        return gadgetDesc;
    }

    public void setGadgetDesc(String gadgetDesc) {
        this.gadgetDesc = gadgetDesc;
    }

    public String getGadgetName() {
        return gadgetName;
    }

    public void setGadgetName(String gadgetName) {
        this.gadgetName = gadgetName;
    }

    public String getGadgetPath() {
        return gadgetPath;
    }

    public void setGadgetPath(String gadgetPath) {
        this.gadgetPath = gadgetPath;
    }

    @Deprecated
    public String getGadgetScreenBase64() {
        return gadgetScreenBase64;
    }

    @Deprecated
    public void setGadgetScreenBase64(String gadgetScreenBase64) {
        this.gadgetScreenBase64 = gadgetScreenBase64;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getUnsignedUserGadget() {
        return unsignedUserGadget;
    }

    public void setUnsignedUserGadget(String unsignedUserGadget) {
        this.unsignedUserGadget = unsignedUserGadget;
    }

    public String getUserCount() {
        return userCount;
    }

    public void setUserCount(String userCount) {
        this.userCount = userCount;
    }

    public String getGadgetId() {
        return gadgetId;
    }

    public void setGadgetId(String gadgetId) {
        this.gadgetId = gadgetId;
    }

    public String getGadgetPrefs() {
        return gadgetPrefs;
    }

    public void setGadgetPrefs(String gadgetPrefs) {
        this.gadgetPrefs = gadgetPrefs;
    }

    public String getGadgetUrl() {
        return gadgetUrl;
    }

    public void setGadgetUrl(String gadgetUrl) {
        this.gadgetUrl = gadgetUrl;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("gadgetId", gadgetId);
        json.put("gadgetName", gadgetUrl);
        json.put("gadgetPrefs", gadgetPrefs);
        json.put("gadgetUrl", gadgetPrefs);
        json.put("gadgetDesc", gadgetPrefs);
        json.put("gadgetScreenBase64", gadgetPrefs);
        json.put("gadgetPath", gadgetPrefs);
        json.put("rating", gadgetPrefs);
        json.put("userCount", gadgetPrefs);
        json.put("defaultGadget", gadgetPrefs);
        json.put("unsignedUserGadget", gadgetPrefs);
        json.put("thumbUrl", gadgetPrefs);
        json.put("gadgetPrefs", gadgetPrefs);

        JSONArray jsonItems = new JSONArray();
        if (comments != null) {
            for (Comment comment : comments) {
                jsonItems.put(comment.toJSONObject());
            }
        }
        json.put("comments", jsonItems);

        return json;
    }
}
