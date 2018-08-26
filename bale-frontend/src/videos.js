import React from 'react';
import './index.css';
import YouTube from "react-youtube";

export class VideoButtons extends React.Component {
    render() {
        let content;
        const url = window.location.href;
        const page = url.substr(url.lastIndexOf("/"));

        if (page === "/videos") {
            content =
                <div className="rows">
                    {/*<Button color="inherit">Add Video</Button>*/}
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

class Videos extends React.Component {
    constructor() {
        super();
    }

    componentDidMount() {
        document.title = "Bahamut's Legion";
    }

    render() {
        const opts = {
            height: '195',
            width: '320'
        };
        return (
            <div>
                <p>BaLe</p>
                <YouTube opts={opts} videoId="X1SkQQF6YaU"/> {/* Bale Intro */}
                <YouTube opts={opts} videoId="7CeTW1dVg7A"/> {/* Return to T9 */}
                <p>Streams</p>
                <YouTube opts={opts} videoId="tLza8h0FPUc"/> {/* Maps */}
                <p>Raids</p>
                <YouTube opts={opts} videoId="KOl0uFWW5CI"/> {/* Benny Hill */}
                <YouTube opts={opts} videoId="xAitngTgHiU"/> {/* Benny Hill - Cordia edit */}
                <YouTube opts={opts} videoId="DvjD4JX-JZU"/> {/* T9 Fail */}
                <YouTube opts={opts} videoId="j2OYp7-nxY0"/> {/* T9 Win */}
                <YouTube opts={opts} videoId="Lt4LmETyF6k"/> {/* Cordia Helping */}
                <YouTube opts={opts} videoId="Aop8snbhmyk"/> {/* A6S Win */}
                <YouTube opts={opts} videoId="GNZAqFGNuWQ"/> {/* Brute Justice Win */}
                <p>Trials</p>
                <YouTube opts={opts} videoId="aNSe1nMPfoQ"/> {/* Shinryu fail */}
                <YouTube opts={opts} videoId="eeGBnGqIBME"/> {/* Shinryu Active Time Fail */}
                <YouTube opts={opts} videoId="XKRCtaudA14"/> {/* Lelouch Rescue */}
                <YouTube opts={opts} videoId="Ln1mr_ZMCnY"/> {/* Sophia 1% */}
                <p>Others</p>
                <YouTube opts={opts} videoId="aTCD2EeBAAo"/> {/* No Carbuncle No */}
                <YouTube opts={opts} videoId="j5W7rilOroM"/> {/* Eureka - Jurassic Park */}
                <YouTube opts={opts} videoId="Eg-bXtyXvWA"/> {/* FF Type Bale */}
                <YouTube opts={opts} videoId="zZXjisXWBSU"/> {/* Samurai Skills */}
                <YouTube opts={opts} videoId="IIec34LhKa0"/> {/* Kugane tower jump */}
                <YouTube opts={opts} videoId="eAtbBKy2Mik"/> {/* Scullai Deaths */}
            </div>
        );
    }
}

export default Videos