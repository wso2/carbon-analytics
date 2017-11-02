import React from "react";
import {Link} from "react-router-dom";

export default class LandingPage extends React.Component {

    componentWillMount() {
        window.location.href = "/sp-status-dashboard/overview";
    }

}