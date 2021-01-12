import WebComponent from '@asyncapi/react-component';
import "@asyncapi/react-component/lib/styles/fiori.css";
import * as React from "react";

function App(props) {
    const {asyncYaml} = props;
    return (
        <div className="App">
            <WebComponent schema={asyncYaml}/>
        </div>
    );
}

export default App;
