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
						<AddButton player={this.state.value}/>
						&nbsp;
                        <button onClick={this.props.onClose}>Close</button>
                    </div>
                </div>
            </div>
        );
    }
}

class AddButton extends React.Component {
    constructor(props) {
        super();
        this.state = {
            disabled: false,
            text: "Add"
        };
    }

    addPlayer(name) {
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
        fetch("http://www.bahamutslegion.com:8081/removePlayer?playerName=" + name)
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
        fetch('http://www.bahamutslegion.com:8081/json')
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