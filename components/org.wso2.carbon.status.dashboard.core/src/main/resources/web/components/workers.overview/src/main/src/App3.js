import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import GridListExampleComplex from './grid';


class App extends Component {

  render() {
    return (
      <MuiThemeProvider>
<GridListExampleComplex/>
  </MuiThemeProvider>

    );
  }
}

export default App;
