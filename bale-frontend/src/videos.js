import React from 'react';
import './index.css';
import YouTube from "react-youtube";

class Videos extends React.Component {
    constructor() {
        super();
    }

    render() {
        return (
            <div>
                <YouTube videoId="X1SkQQF6YaU"/> {/* Bale Intro */}
                <p>Streams</p>
                <YouTube videoId="tLza8h0FPUc"/> {/* Maps */}
                <p>Raids</p>
                <YouTube videoId="KOl0uFWW5CI"/> {/* Benny Hill */}
                <YouTube videoId="xAitngTgHiU"/> {/* Benny Hill - Cordia edit */}
                <YouTube videoId="DvjD4JX-JZU"/> {/* T9 Fail */}
                <YouTube videoId="j2OYp7-nxY0"/> {/* T9 Win */}
                <YouTube videoId="7CeTW1dVg7A"/> {/* Return to T9 */}
                <YouTube videoId="Lt4LmETyF6k"/> {/* Cordia Helping */}
                <YouTube videoId="Aop8snbhmyk"/> {/* A6S Win */}
                <YouTube videoId="GNZAqFGNuWQ"/> {/* Brute Justice Win */}
                <p>Trials</p>
                <YouTube videoId="aNSe1nMPfoQ"/> {/* Shinryu fail */}
                <YouTube videoId="eeGBnGqIBME"/> {/* Shinryu Active Time Fail */}
                <YouTube videoId="XKRCtaudA14"/> {/* Lelouch Rescue */}
                <YouTube videoId="Ln1mr_ZMCnY"/> {/* Sophia 1% */}
                <p>Mods</p>
                <YouTube videoId="5bWA6dySbt8"/> {/* God Kefka */}
                <p>Others</p>
                <YouTube videoId="aTCD2EeBAAo"/> {/* No Carbuncle No */}
                <YouTube videoId="j5W7rilOroM"/> {/* Eureka - Jurassic Park */}
                <YouTube videoId="Eg-bXtyXvWA"/> {/* FF Type Bale */}
                <YouTube videoId="zZXjisXWBSU"/> {/* Samurai Skills */}
                <YouTube videoId="IIec34LhKa0"/> {/* Kugane tower jump */}
                <YouTube videoId="eAtbBKy2Mik"/> {/* Scullai Deaths */}
            </div>
        );
    }
}

export default Videos