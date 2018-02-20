import Axios from "axios";

import { MediaType} from "../Constants";
import AuthManager from "../../auth/utils/AuthManager";
import StatusDashboardAPIS from "./StatusDashboardAPIs";

export default class DistributedViewStatusDashboardAPIs{

    /**
     * This method will return the AXIOS http client.
     * @returns httpClient
     */
    static getHTTPClient(){
        let httpClient = Axios.create({
            baseURL : window.location.origin+"/"+window.contextPath.substr(1)+'/managers',
            timeout : 6000,
            headers : {"Authorization": "Bearer " + AuthManager.getUser().SDID}
        });
        httpClient.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
        httpClient.defaults.headers.put['Content-Type'] = MediaType.APPLICATION_JSON;
        return httpClient;
    }

    /**
     * This method will return the polling interval
     */
    /**
     * This method will return a list of workers real-time details.
     */
    static getManagerList() {
        return DistributedViewStatusDashboardAPIs.getHTTPClient().get();
    }
}