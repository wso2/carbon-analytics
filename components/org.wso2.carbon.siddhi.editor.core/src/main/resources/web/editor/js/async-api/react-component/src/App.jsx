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

// function App() {
//     let asyncYaml =
//         "asyncapi: 2.0.0\n" +
//         "info:\n" +
//         "  title: Account Service\n" +
//         "  version: 1.0.0\n" +
//         "  description: This service is in charge of processing user signups\n" +
//         "channels:\n" +
//         "  user/signedup:\n" +
//         "    subscribe:\n" +
//         "      message:\n" +
//         "        $ref: '#/components/messages/UserSignedUp'\n" +
//         "components:\n" +
//         "  messages:\n" +
//         "    UserSignedUp:\n" +
//         "      payload:\n" +
//         "        type: object\n" +
//         "        properties:\n" +
//         "          displayName:\n" +
//         "            type: string\n" +
//         "            description: Name of the user\n" +
//         "          email:\n" +
//         "            type: string\n" +
//         "            format: email\n" +
//         "            description: Email of the user";
//
//     return (
//         <div className="App">
//             <WebComponent schema={asyncYaml}/>
//         </div>
//     );
// }
//
export default App;
