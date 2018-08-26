import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import Drawer from "@material-ui/core/Drawer/Drawer";
import List from "@material-ui/core/List/List";
import ListItem from "@material-ui/core/ListItem/ListItem";
import {Link} from "react-router-dom";

const styles = {
    root: {
        flexGrow: 1,
    },
    flex: {
        flexGrow: 1,
    },
    menuButton: {
        marginLeft: -12,
        marginRight: 20,
    },
};

class NavBar extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            drawerOpen: false
        };
    }

    toggleDrawer = () => {
        this.setState({drawerOpen: !this.state.drawerOpen});
    };

    render() {
        const {classes} = this.props;

        return (
            <div className={classes.root}>
                <AppBar position="fixed">
                    <Toolbar>
                        <IconButton className={classes.menuButton} color="inherit" aria-label="Menu"
                                    onClick={this.toggleDrawer}>
                            <MenuIcon/>
                        </IconButton>
                        <Typography variant="title" color="inherit" className={classes.flex}>
                            {/* TODO: Dynamic text label which displays current page */} {/* Bahamut's Legion */}
                            {this.props.children}
                        </Typography>
                        <Drawer open={this.state.drawerOpen} onClose={this.toggleDrawer}>
                            <div
                                tabIndex={0}
                                role="button"
                                onClick={this.toggleDrawer}
                            >
                                <div className={classes.list}>
                                    <List component="nav">
                                        <ListItem button>
                                            <Link to="/">Mounts</Link>
                                        </ListItem>
                                        <ListItem button>
                                            <Link to="/videos">Videos</Link>
                                        </ListItem>
                                    </List>
                                </div>
                            </div>
                        </Drawer>
                    </Toolbar>
                </AppBar>
                <div style={{paddingTop: 64}}/>
            </div>
        );
    }
}

NavBar.propTypes = {
    classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(NavBar);