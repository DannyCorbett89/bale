package com.dc.bale.service;

import com.dc.bale.database.Player;
import com.dc.bale.database.PlayerRepository;
import com.dc.bale.exception.PlayerException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerService {
    @NonNull
    private PlayerRepository playerRepository;
    @NonNull
    private MountTracker mountTracker;

    public void addPlayer(long playerId) {
        Player player = playerRepository.findOne(playerId);
        player.setTracking(true);
        player = playerRepository.save(player);

        mountTracker.trackPlayer(player);
    }

    public void removePlayer(String playerName) throws PlayerException {
        Player player = playerRepository.findByName(playerName);

        if (player != null) {
            player.setTracking(false);
            playerRepository.save(player);

            mountTracker.untrackPlayer(player);
        } else {
            throw new PlayerException("Player not found: " + playerName);
        }
    }

    public List<Player> listPlayers() {
        return playerRepository.findByTrackingFalseOrderByName();
    }
}