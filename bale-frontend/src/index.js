import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import {BrowserRouter, Route, Switch} from "react-router-dom";
import NavBar from './NavBar'
import Videos, {VideoButtons} from './videos'
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableBody from "@material-ui/core/TableBody";
import {withStyles} from "@material-ui/core";
import Button from "@material-ui/core/Button/Button";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import Select from "@material-ui/core/Select/Select";
import MenuItem from "@material-ui/core/MenuItem/MenuItem";
import InputLabel from "@material-ui/core/InputLabel/InputLabel";
import TextField from "@material-ui/core/TextField/TextField";
import Paper from "@material-ui/core/Paper/Paper";
import {isMobile} from "react-device-detect";

class MessageWindow extends React.Component {
    constructor(props) {
        super();
        this.state = {
            text: props.text
        };
    }

    render() {
        // Render nothing if the "show" prop is false
        if (!this.props.show) {
            return null;
        }

        return (
            <Dialog
                open="true"
                onClose={this.handleClose}
                aria-labelledby="alert-dialog-title"
                aria-describedby="alert-dialog-description"
            >
                <DialogTitle id="alert-dialog-title">{this.state.text}</DialogTitle>
            </Dialog>
        );
    }
}

class AddPlayer extends React.Component {
    constructor() {
        super();
        this.state = {
            open: false,
            players: [],
            value: ''
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentWillMount() {
        fetch('http://www.bahamutslegion.com:8081/players')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    players: data,
                    value: data[0].id
                });
                console.log("state", this.state.players);
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
        super();
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
        fetch("http://www.bahamutslegion.com:8081/addPlayer?playerId=" + this.props.player)
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
    constructor() {
        super();
        this.state = {
            open: false,
            mounts: [],
            mountName: '',
            instanceName: ''
        };
        this.handleMountChange = this.handleMountChange.bind(this);
        this.handleInstanceChange = this.handleInstanceChange.bind(this);
    }

    componentWillMount() {
        fetch('http://www.bahamutslegion.com:8081/listAvailableMounts')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    mounts: data,
                    mountName: data[0].name
                });
                console.log("state", this.state.mounts);
            })
    }

    handleMountChange(event) {
        this.setState({
            mountName: event.target.value
        });
    }

    handleInstanceChange(event) {
        this.setState({
            instanceName: event.target.value
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
        return (
            <div className="row">
                <Button color="inherit" onClick={this.handleClickOpen}>Add Mount</Button>
                <Dialog
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
                        <TextField
                            autoFocus
                            margin="dense"
                            id="name"
                            label="Display Name"
                            value={this.state.instanceName}
                            onChange={this.handleInstanceChange}
                            fullWidth
                        />
                    </DialogContent>
                    <DialogActions>
                        <AddMountButton mountName={this.state.mountName} instanceName={this.state.instanceName}/>
                        <Button onClick={this.handleClose} color="primary">
                            Close
                        </Button>
                    </DialogActions>
                </Dialog>
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
        fetch("http://www.bahamutslegion.com:8081/addMount?name=" + this.props.mountName + "&instance=" + this.props.instanceName)
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
        super();
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
        fetch("http://www.bahamutslegion.com:8081/removePlayer?playerName=" + name)
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
        super();
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
        fetch("http://www.bahamutslegion.com:8081/removeMount?id=" + mount.id)
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

class MountsTable extends React.Component {
    constructor() {
        super();
        this.state = {
            updated: null,
            columns: null,
            players: [{mounts: []}]
        };
    }

    componentWillMount() {
        fetch('http://www.bahamutslegion.com:8081/listMounts')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    updated: data.lastUpdated,
                    columns: data.columns,
                    players: data.players
                });
                console.log("state", this.state.players);
            })
    }

    render() {
        return (
            <Table padding="none">
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
            </Table>
        );
    }
}

class Mounts extends React.Component {
    constructor() {
        super();
        this.state = {
            updated: null,
            columns: null,
            players: [{mounts: []}]
        };
    }

    componentWillMount() {
        fetch('http://www.bahamutslegion.com:8081/listMounts')
            .then(results => {
                return results.json();
            })
            .then(data => {
                this.setState({
                    updated: data.lastUpdated,
                    columns: data.columns,
                    players: data.players
                });
                console.log("state", this.state.players);
            })
    }

    render() {
        let table;

        if (isMobile) {
            table =
                <Paper style={{width: '100%', overflowX: 'auto'}}>
                    <MountsTable/>
                </Paper>;
        } else {
            table = <MountsTable/>;
        }

        return (
            <div>
                {table}
                <p>Last Updated: {this.state.updated}</p>
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