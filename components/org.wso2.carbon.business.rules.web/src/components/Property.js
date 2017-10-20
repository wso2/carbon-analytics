import React from 'react';
// import './index.css';
// Material-UI
import TextField from 'material-ui/TextField';
import {FormControl, FormHelperText} from 'material-ui/Form';
import Input, {InputLabel} from 'material-ui/Input';
import {MenuItem} from 'material-ui/Menu';
import Select from 'material-ui/Select';

class Property extends React.Component {
    /**
     * Handles onChange action of a TextField or a Select
     * @param name
     */
    handleOnChange(event){
        this.props.onValueChange(event.target.value)
    }
    //todo: align dropdwns to left

    // Renders each Property either as a TextField or Radio Group, with default values and elements as specified
    render() {
        // If there are options specified, it is a dropdown
        if (this.props.options) {
            var options = this.props.options.map((option) => (
                <MenuItem key={option} name={option} value={option}>{option}</MenuItem>))
            return (
                <div>
                    <br/>
                    <FormControl
                        fullWidth
                        error={(this.props.errorState)?(this.props.errorState):false}
                        disabled={(this.props.disabledState)?(this.props.disabledState):false}
                    >
                        <InputLabel htmlFor={this.props.name}>{this.props.fieldName}</InputLabel>
                        <Select
                            style={{align: 'left'}}
                            value={this.props.value}
                            onChange={(e)=>this.handleOnChange(e)}
                            input={<Input id={this.props.name}/>}
                        >
                            {options}
                        </Select>
                        <FormHelperText>{this.props.description}</FormHelperText>
                    </FormControl>
                    <br/>
                </div>
            );
        } else {
            return (
                <div>
                    <TextField
                        fullWidth
                        required
                        error={(this.props.errorState)?(this.props.errorState):false}
                        disabled={(this.props.disabledState)?(this.props.disabledState):false}
                        id={this.props.name}
                        name={this.props.name}
                        label={this.props.fieldName}
                        value={this.props.value}
                        helperText={this.props.description}
                        margin="normal"
                        onChange={(e)=>this.handleOnChange(e)}
                    />
                    <br/>
                </div>
            );
        }
    }
}

export default Property;
