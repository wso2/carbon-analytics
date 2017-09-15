import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import AppBarExampleIcon from './AppBarExampleIcon';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import {cyan900,black} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
const muiTheme = getMuiTheme({
  palette: {
    textColor: cyan900,
    backgroundColor: cyan900,
  },
  appBar: {
      color: cyan900,
      textColor: black,
      height: 60,
    },
});
class App extends Component {

  render() {
    return (
      <MuiThemeProvider muiTheme={muiTheme}>
<AppBarExampleIcon/>
  </MuiThemeProvider>

    );
  }
}

export default App;
