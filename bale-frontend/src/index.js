import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

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
        fetch('http://localhost:8081/players')
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

    addPlayer() {
        fetch("http://localhost:8081/addPlayer?playerId=" + this.state.value)
            .then(() => {
                window.location.reload();
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
                    <h3>Add Player</h3>
                    <select onChange={this.handleChange}
                            value={this.state.value}>
                        {this.state.players.map((player) =>
                            <option key={player.id} value={player.id}>{player.name}</option>
                        )}
                    </select>
                    <div className="footer">
                        <button onClick={() => this.addPlayer()}>
                            Add
                        </button>
                        <button onClick={this.props.onClose}>
                            Close
                        </button>
                    </div>
                </div>
            </div>
        );
    }
}

class RemoveButton extends React.Component {
    constructor(props) {
        super();
        this.state = {
            disabled: false,
            text: "Remove",
            player: props.player
        };
    }

    removePlayer(name) {
        this.setState({
            disabled: true,
            text: "Removing " + name + "..."
        });
        fetch("http://localhost:8081/removePlayer?playerName=" + name)
            .then(() => {
                window.location.reload();
            });
    }

    render() {
        return (
            <button id={this.state.player.name}
                    onClick={() => this.removePlayer(this.state.player.name)}
                    disabled={this.state.disabled}>{this.state.text}</button>
        );
    }
}

class Mounts extends React.Component {
    constructor() {
        super();
        this.state = {
            updated: null,
            columns: null,
            players: [],
            isOpen: false
        };
    }

    componentWillMount() {
        fetch('http://localhost:8081/json')
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

    toggleModal = () => {
        this.setState({
            isOpen: !this.state.isOpen
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
                    <RemoveButton player={player}/>
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
                    </tbody>
                </table>
                <p>Last Updated: {this.state.updated}</p>
                <button onClick={this.toggleModal}>Add Player</button>
                <AddPlayer show={this.state.isOpen}
                           onClose={this.toggleModal}/>
            </div>
        );
    }
}

// ========================================

ReactDOM.render(<Mounts/>, document.getElementById("root"));