package com.dc.bale.service;

import com.dc.bale.database.Player;
import com.dc.bale.database.PlayerRepository;
import com.dc.bale.exception.PlayerException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerTracker playerTracker;

    public Player addPlayer(long playerId) {
        Player player = playerRepository.findOne(playerId);
        player.setVisible(true);
        player = playerRepository.save(player);

        playerTracker.trackPlayer(player);

        return player;
    }

    public void removePlayer(String playerName) throws PlayerException {
        Player player = playerRepository.findByName(playerName);

        if (player != null) {
            player.setVisible(false);
            playerRepository.save(player);
            playerTracker.untrackPlayer(player);
        } else {
            throw new PlayerException("Player not found: " + playerName);
        }
    }

    public List<Player> listPlayers(List<Long> rankIds) {
        return playerRepository.findByVisibleFalseAndRankIdIsInOrderByName(rankIds);
    }
}
