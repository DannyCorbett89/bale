import {backendUrl} from "./config";
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import {BrowserRouter, Route, Switch} from "react-router-dom";
import NavBar from './NavBar'
import Videos, {VideoButtons} from './videos'
import TableCell from "@material-ui/core/TableCell";
import {withStyles} from "@material-ui/core";
import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import Select from "@material-ui/core/Select/Select";
import MenuItem from "@material-ui/core/MenuItem/MenuItem";
import InputLabel from "@material-ui/core/InputLabel/InputLabel";
import Paper from "@material-ui/core/Paper/Paper";
import {isMobile} from "react-device-detect";
import LinearProgress from "@material-ui/core/LinearProgress/LinearProgress";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableBody from "@material-ui/core/TableBody";

class MessageWindow extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            text: props.text,
            onClick: props.onClick
        };
    }

    render() {
        // Render nothing if the "show" prop is false
        if (!this.props.show) {
            return null;
        }

        let button;

        if (this.state.onClick != null) {
            button = <DialogActions>
                <Button onClick={this.state.onClick} color="primary">
                    Close
                </Button>
            </DialogActions>;
        } else {
            button = <div/>;
        }

        return (
            <Dialog
                open="true"
                onClose={this.handleClose}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">{this.state.text}</DialogTitle>
                {button}
            </Dialog>
        );
    }
}

class AddPlayer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            open: false,
            players: [],
            value: ''
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentWillMount() {
        fetch(backendUrl + '/players')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    players: data,
                    value: data[0].id
                });
                console.log("players", this.state.players);
            })
    }

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleClickOpen = () => {
        this.setState({open: true});
    };

    handleClose = () => {
        this.setState({open: false});
    };

    render() {
        return (
            <div className="row">
                <Button color="inherit" onClick={this.handleClickOpen}>Add Player</Button>
                <Dialog
                    open={this.state.open}
                    onClose={this.handleClose}
                    aria-labelledby="form-dialog-title"
                >
                    <DialogTitle id="form-dialog-title">Add Player</DialogTitle>
                    <DialogContent>
                        <Select onChange={this.handleChange}
                                value={this.state.value}>
                            {this.state.players.map((player) =>
                                <MenuItem key={player.id} value={player.id}>{player.name}</MenuItem>
                            )}
                        </Select>
                    </DialogContent>
                    <DialogActions>
                        <AddPlayerButton player={this.state.value}/>
                        <Button onClick={this.handleClose} color="primary">
                            Close
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        );
    }
}

class AddPlayerButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            disabled: false,
            text: "Add"
        };
    }

    addPlayer() {
        this.setState({
            disabled: true,
            text: "Adding Player..."
        });
        fetch(backendUrl + "/addPlayer?playerId=" + this.props.player)
            .then(() => {
                window.location.reload();
            });
    }

    render() {
        return (
            <Button id={this.props.player}
                    color="primary"
                    onClick={() => this.addPlayer(this.props.player)}
                    disabled={this.state.disabled}>{this.state.text}</Button>
        );
    }
}

class AddMount extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            open: false,
            mounts: [],
            mountName: ''
        };
        this.handleMountChange = this.handleMountChange.bind(this);
    }

    componentWillMount() {
        fetch(backendUrl + '/listAvailableMounts')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    mounts: data,
                    mountName: data[0].name
                });
                console.log("listAvailableMounts", this.state.mounts);
            })
            .catch(error => {
                console.log("Empty response for /listAvailableMounts", error)
            });
    }

    handleMountChange(event) {
        this.setState({
            mountName: event.target.value
        });
    }

    handleChange(event) {
        this.setState({value: event.target.value});
    }

    handleClickOpen = () => {
        this.setState({open: true});
    };

    handleClose = () => {
        this.setState({open: false});
    };

    render() {
        let window;

        if (this.state.mountName === "") {
            window = <MessageWindow show={this.state.open} text={"No more mounts to add"} onClick={this.handleClose}/>;
        } else {
            window = <Dialog
                open={this.state.open}
                onClose={this.handleClose}
                aria-labelledby="form-dialog-title"
            >
                <DialogTitle id="form-dialog-title">Add Mount</DialogTitle>
                <DialogContent>
                    <InputLabel>Mount </InputLabel>
                    <Select onChange={this.handleMountChange}
                            value={this.state.mountName}>
                        {this.state.mounts.map((mount) =>
                            <MenuItem key={mount.name} value={mount.name}>{mount.name}</MenuItem>
                        )}
                    </Select>
                </DialogContent>
                <DialogActions>
                    <AddMountButton mountName={this.state.mountName}/>
                    <Button onClick={this.handleClose} color="primary">
                        Close
                    </Button>
                </DialogActions>
            </Dialog>;
        }

        return (
            <div className="row">
                <Button color="inherit" onClick={this.handleClickOpen}>Add Mount</Button>
                {window}
            </div>
        );
    }
}

class AddMountButton extends React.Component {
    constructor() {
        super();
        this.state = {
            disabled: false,
            text: "Add"
        };
    }

    addMount() {
        this.setState({
            disabled: true,
            text: "Adding Mount..."
        });
        fetch(backendUrl + "/addMount?name=" + this.props.mountName)
            .then(() => {
                window.location.reload();
            });
    }

    render() {
        return (
            <Button id={this.props.mountName}
                    color="primary"
                    onClick={() => this.addMount(this.props.mountName)}
                    disabled={this.state.disabled}>{this.state.text}</Button>
        );
    }
}

class RemovePlayerButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            disabled: false,
            text: "Remove",
            player: props.player,
            messageWindowIsOpen: false
        };
    }

    removePlayer(name) {
        this.setState({
            disabled: true,
            messageWindowIsOpen: true
        });
        fetch(backendUrl + "/removePlayer?playerName=" + name)
            .then(() => {
                window.location.reload();
            });
    }

    render() {
        return (
            <div>
                <Button id={this.state.player.name}
                        variant="outlined"
                        size="small"
                        onClick={() => this.removePlayer(this.state.player.name)}
                        disabled={this.state.disabled}>{this.state.text}</Button>
                <MessageWindow show={this.state.messageWindowIsOpen} text={"Removing " + this.state.player.name}/>
            </div>
        );
    }
}

class RemoveMountButton extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            disabled: false,
            text: "Remove",
            mount: props.mount,
            messageWindowIsOpen: false
        };
    }

    removeMount(mount) {
        this.setState({
            disabled: true,
            messageWindowIsOpen: true
        });
        fetch(backendUrl + "/removeMount?id=" + mount.id)
            .then(() => {
                window.location.reload();
            });
    }

    render() {
        return (
            <div>
                <Button id={this.state.mount.name}
                        variant="outlined"
                        size="small"
                        onClick={() => this.removeMount(this.state.mount)}
                        disabled={this.state.disabled}>{this.state.text}</Button>
                <MessageWindow show={this.state.messageWindowIsOpen} text={"Removing " + this.state.mount.name}/>
            </div>
        );
    }
}

const CustomTableCell = withStyles(theme => ({
    head: {
        textAlign: 'center'
    },
    body: {
        textAlign: 'center'
    },
}))(TableCell);

class Mounts extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            updated: null,
            columns: null,
            players: [{mounts: []}]
        };
    }


    componentWillMount() {
        fetch(backendUrl + '/listMounts')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    updated: data.lastUpdated,
                    columns: data.columns,
                    players: data.players
                });
                console.log("listMounts", this.state.players);
            })
    }

    render() {
        let table;
        let result;
        let updated;

        if (this.state.updated == null) {
            table = <div>
                <p align="center">Loading mounts...</p>
                <LinearProgress/>
            </div>
        } else if (this.state.players.length === 0) {
            table = <p align="center">No Players are being tracked</p>
        } else {
            table = <Table padding="none">
                <TableHead>
                    <TableRow>
                        <CustomTableCell>Name</CustomTableCell>
                        <CustomTableCell colSpan={this.state.columns + 1}>Mounts
                            Needed</CustomTableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {this.state.players.map((player) =>
                        <TableRow key={player.name} className="highlight">
                            <CustomTableCell>{player.name}</CustomTableCell>

                            {player.mounts.map((mount) =>
                                <CustomTableCell key={mount.id}>{mount.instance}</CustomTableCell>
                            )}

                            <CustomTableCell>
                                <RemovePlayerButton player={player}/>
                            </CustomTableCell>
                        </TableRow>)}
                    <TableRow>
                        <CustomTableCell/>
                        {this.state.players[0].mounts.map((mount) =>
                            <CustomTableCell key={mount.id} align="center">
                                <RemoveMountButton mount={mount}/>
                            </CustomTableCell>
                        )}
                        <CustomTableCell/>
                    </TableRow>
                </TableBody>
            </Table>;

            updated = <p>Last Updated: {this.state.updated}</p>;
        }

        if (isMobile) {
            result =
                <div>
                    <Paper style={{width: '100%', overflowX: 'auto'}}>
                        {table}
                    </Paper>
                    {updated}
                </div>;
        } else {
            result =
                <div>
                    {table}
                    {updated}
                </div>;
        }

        return (
            <div>
                {result}
            </div>
        );
    }
}

class MountButtons extends React.Component {
    render() {
        let content;
        const url = window.location.href;
        const page = url.substr(url.lastIndexOf("/"));

        if (page === "/") {
            content =
                <div className="rows">
                    <AddPlayer/>
                    <AddMount/>
                </div>;
        } else {
            content = <div/>;
        }

        return (
            <div>
                {content}
            </div>
        );
    }
}

class Main extends React.Component {
    render() {
        return (
            <div>
                <NavBar>
                    <MountButtons/>
                    <VideoButtons/>
                </NavBar>
                <Switch>
                    <Route exact path="/" component={Mounts}/>
                    <Route path="/videos" component={Videos}/>
                </Switch>
            </div>
        );
    }
}

// ========================================

ReactDOM.render(
    <BrowserRouter>
        <Main/>
    </BrowserRouter>, document.getElementById("root"));