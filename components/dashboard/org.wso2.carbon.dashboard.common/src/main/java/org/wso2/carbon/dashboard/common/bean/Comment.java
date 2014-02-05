package org.wso2.carbon.dashboard.common.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * The bean represent a comment (meta information) for a gadget
 */
public class Comment {
    private String commentPath;
    private String commentText;
    private String authorUserName;
    private Date createTime;

    public String getCommentPath() {
        return commentPath;
    }

    public void setCommentPath(String commentPath) {
        this.commentPath = commentPath;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getAuthorUserName() {
        return authorUserName;
    }

    public void setAuthorUserName(String authorUserName) {
        this.authorUserName = authorUserName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("commentPath", commentPath);
        json.put("commentText", commentText);
        json.put("authorUserName", authorUserName);
        json.put("createTime", createTime);
        return json;
    }

}