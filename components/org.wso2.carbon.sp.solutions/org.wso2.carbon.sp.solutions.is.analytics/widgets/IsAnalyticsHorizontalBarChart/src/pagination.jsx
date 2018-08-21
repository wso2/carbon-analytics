import React from 'react';
import PropTypes from 'prop-types';
import Button from '@material-ui/core/Button';
import NavigationFirstPage from 'material-ui/svg-icons/navigation/first-page';
import NavigationLastPage from 'material-ui/svg-icons/navigation/last-page';

const ButtonStyle = {
    minWidth: 16,
};

const calculateRange = arg => {
    const { total, current, display } = arg;
    let end = total;
    let start = 1;
    if (display < end) {
        // rounded to the nearest integer smaller
        let beforeNumber = Math.round(display / 2 - 0.5);
        const afterNumber = beforeNumber;
        if (display % 2 === 0) {
            beforeNumber -= 1;
        }

        if (current <= beforeNumber + 1) {
            end = display;
        } else if (current >= (total - afterNumber)) {
            start = total - display + 1;
        } else {
            start = current - beforeNumber;
            end = current + afterNumber;
        }
    }

    return { end, start };
};

const getStateFromProps = props => {
    let { total, current, display } = props;
    total = total > 0 ? total : 1;
    current = current > 0 ? current : 1;
    display = display > 0 ? display : 1;
    current = current < total ? current : total;
    display = display < total ? display : total;
    return { current, display, total };
};

const Page = ({ value, isActive, onClick, styleButton, stylePrimary }) => {
    return !styleButton ? (<Button
        style = { ButtonStyle }
        label = { value.toString() }
        primary = { isActive }
        onClick = { onClick }
    />) : (<div
        style = { isActive ? stylePrimary : styleButton }
        label = { value.toString() }
        onClick = { onClick }
    >
        {value}
    </div>)

};
Page.propTypes = {
    value: PropTypes.number,
    isActive: PropTypes.bool,
    onClick: PropTypes.func,
    styleButton: PropTypes.object,
    stylePrimary: PropTypes.object,
};

const FirstPageLink = ({ onClick, styleFirstPageLink }) => {
    return !styleFirstPageLink ? (<Button
        style = { ButtonStyle }
        icon = { <NavigationFirstPage /> }
        onClick = { onClick }
    />) : (<div
        style = { styleFirstPageLink }
        onClick = { onClick }
    />);
};

FirstPageLink.propTypes = {
    onClick: PropTypes.func,
    styleFirstPageLink: PropTypes.object,
};

const LastPageLink = ({ onClick, styleLastPageLink }) => {
    return !styleLastPageLink ? (<Button
        style = { ButtonStyle }
        icon = { <NavigationLastPage /> }
        onClick = { onClick }
    />) : (<div
        style = { styleLastPageLink }
        onClick = { onClick }
    />);
};

LastPageLink.propTypes = {
    onClick: PropTypes.func,
    styleLastPageLink: PropTypes.object,
};

class Pagination extends React.Component {

    constructor(props) {
        super(props);
        const tem = getStateFromProps(props);
        this.setCurrent = this.setCurrent.bind(this);

        this.state = {
            ...tem,
            ...calculateRange(tem),
        };
    }

    componentWillReceiveProps(nextProps) {
        const tem = getStateFromProps(nextProps);
        this.setState({
            ...tem,
            ...calculateRange(tem),
        });
    }

    setCurrent(current) {
        const tem = { ...this.state, current };
        this.props.onChange(current);
        this.setState({
            ...tem,
            ...calculateRange(tem),
        });
    }

    render() {
        const array = [];
        for (let i = this.state.start; i <= this.state.end; i += 1) {
            array.push(i);
        }

        return (
            <div style={this.props.styleRoot}>
                <FirstPageLink
                    onClick = { () => this.setCurrent(1) }
                    styleFirstPageLink = { this.props.styleFirstPageLink }
                />
                {
                    array.map((page, k) => (
                        <Page
                            key = { k }
                            value = { page }
                            isActive = { this.state.current === page }
                            onClick = { () => this.setCurrent(page) }
                            styleButton = { this.props.styleButton }
                            stylePrimary = { this.props.stylePrimary }
                        />
                    ))
                }
                <LastPageLink
                    onClick = { () => this.setCurrent(this.state.total) }
                    styleLastPageLink = { this.props.styleLastPageLink }
                />
            </div>
        );
    }
}

Pagination.propTypes = {

    // eslint-disable-next-line react/no-unused-prop-types
    total: PropTypes.number,

    // eslint-disable-next-line react/no-unused-prop-types
    current: PropTypes.number,

    // eslint-disable-next-line react/no-unused-prop-types
    display: PropTypes.number,
    onChange: PropTypes.func,

    styleRoot: PropTypes.object,
    styleFirstPageLink: PropTypes.object,
    styleLastPageLink: PropTypes.object,
    styleButton: PropTypes.object,
    stylePrimary: PropTypes.object,
};

Pagination.defaultProps = {
    styleRoot: null,
    styleFirstPageLink: null,
    styleLastPageLink: null,
    styleButton: null,
    stylePrimary: null,
};

Pagination.displayName = 'Pagination';
export default Pagination;