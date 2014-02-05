package org.wso2.carbon.dashboard.common.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The bean represent a tab in the dashboard
 */
public class Tab {
    private String tabId;
    private String tabName;
    private String gadgetLayout;
    private Gadget[] gadgets;

    public Gadget[] getGadgets() {
        return gadgets;
    }

    public void setGadgets(Gadget[] gadgets) {
        this.gadgets = gadgets;
    }

    public String getGadgetLayout() {
        return gadgetLayout;
    }

    public void setGadgetLayout(String gadgetLayout) {
        this.gadgetLayout = gadgetLayout;
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("tabId", tabId);
        json.put("tabName", tabName);
        json.put("gadgetLayout", gadgetLayout);

        JSONArray jsonItems = new JSONArray();
        if (gadgets != null) {
            for (Gadget gadget : gadgets) {
                jsonItems.put(gadget.toJSONObject());
            }
        }
        json.put("gadgets", jsonItems);

        return json;
    }
}