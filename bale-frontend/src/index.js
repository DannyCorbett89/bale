import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

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
            <div className="backdrop">
                <div className="modal">
                    <p>{this.state.text}</p>
                </div>
            </div>
        );
    }
}

class AddPlayer extends React.Component {
    constructor() {
        super();
        this.state = {
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

    render() {
        // Render nothing if the "show" prop is false
        if (!this.props.show) {
            return null;
        }

        return (
            <div className="backdrop">
                <div className="modal">
                    <h3>Add Player</h3>
                    <select onChange={this.handleChange}
                            value={this.state.value}>
                        {this.state.players.map((player) =>
                            <option key={player.id} value={player.id}>{player.name}</option>
                        )}
                    </select>
                    <br/>
                    <br/>
                    <div className="footer">
                        <AddPlayerButton player={this.state.value}/>
                        &nbsp;
                        <button onClick={this.props.onClose}>Close</button>
                    </div>
                </div>
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
            <button id={this.props.player}
                    onClick={() => this.addPlayer(this.props.player)}
                    disabled={this.state.disabled}>{this.state.text}</button>
        );
    }
}

class AddMount extends React.Component {
    constructor() {
        super();
        this.state = {
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

    render() {
        // Render nothing if the "show" prop is false
        if (!this.props.show) {
            return null;
        }

        return (
            <div className="backdrop">
                <div className="modal">
                    <h3>Add Mount</h3>
                    <p>Mount name</p>
                    <select onChange={this.handleMountChange}
                            value={this.state.value}>
                        {this.state.mounts.map((mount) =>
                            <option key={mount.id} value={mount.name}>{mount.name}</option>
                        )}
                    </select>
                    <p>Instance name</p>
                    <input type="text" name="instance" value={this.state.instanceName}
                           onChange={this.handleInstanceChange}/>
                    <br/>
                    <br/>
                    <div className="footer">
                        <AddMountButton mountName={this.state.mountName} instanceName={this.state.instanceName}/>
                        &nbsp;
                        <button onClick={this.props.onClose}>Close</button>
                    </div>
                </div>
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
            <button id={this.props.mountName}
                    onClick={() => this.addMount(this.props.mountName)}
                    disabled={this.state.disabled}>{this.state.text}</button>
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
                <button id={this.state.player.name}
                        onClick={() => this.removePlayer(this.state.player.name)}
                        disabled={this.state.disabled}>{this.state.text}</button>
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
                <button id={this.state.mount.name}
                        onClick={() => this.removeMount(this.state.mount)}
                        disabled={this.state.disabled}>{this.state.text}</button>
                <MessageWindow show={this.state.messageWindowIsOpen} text={"Removing " + this.state.mount.name}/>
            </div>
        );
    }
}

class Mounts extends React.Component {
    constructor() {
        super();
        this.state = {
            updated: null,
            columns: null,
            players: [{mounts: []}],
            addPlayerIsOpen: false,
            addMountIsOpen: false
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

    componentDidMount() {
        document.title = "Bahamut's Legion";
    }

    togglePlayerModal = () => {
        this.setState({
            addPlayerIsOpen: !this.state.addPlayerIsOpen
        });
    };

    toggleMountModal = () => {
        this.setState({
            addMountIsOpen: !this.state.addMountIsOpen
        });
    };

    render() {
        const players = this.state.players.map((player) =>
            <tr key={player.name} className="highlight">
                <td>{player.name}</td>

                {player.mounts.map((mount) =>
                    <td key={mount.name}>{mount.instance}</td>
                )}

                <td>
                    <RemovePlayerButton player={player} action={this.toggleMountModal}/>
                </td>
            </tr>);
        return (
            <div>
                <table border="1">
                    <tbody>
                    <tr>
                        <th>Name</th>
                        <th colSpan={this.state.columns + 1}>Mounts Needed</th>
                    </tr>
                    {players}
                    <tr>
                        <td/>
                        {this.state.players[0].mounts.map((mount) =>
                            <td key={mount.name} align="center">
                                <RemoveMountButton mount={mount}/>
                            </td>
                        )}
                        <td/>
                    </tr>
                    </tbody>
                </table>
                <p>Last Updated: {this.state.updated}</p>
                <button onClick={this.togglePlayerModal}>Add Player</button>
                &nbsp;
                <button onClick={this.toggleMountModal}>Add Mount</button>
                <AddPlayer show={this.state.addPlayerIsOpen}
                           onClose={this.togglePlayerModal}/>
                <AddMount show={this.state.addMountIsOpen}
                          onClose={this.toggleMountModal}/>
            </div>
        );
    }
}

// ========================================

ReactDOM.render(<Mounts/>, document.getElementById("root"));