package com.dc.bale.service;

import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.PlayerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerService {
    private final PlayerRepository playerRepository;

    public Player addPlayer(long playerId) {
        Player player = playerRepository.findOne(playerId);
        player.setVisible(true);
        return playerRepository.save(player);
    }

    public Player removePlayer(Long id, String name) throws PlayerException {
        Player player;
        String identifier = "with id " + id;

        if (id != null) {
            player = playerRepository.findOne(id);
        } else {
            identifier += " or name " + name;
            player = playerRepository.findByName(name);
        }

        if (player != null) {
            player.setVisible(false);
            return playerRepository.save(player);
        } else {
            throw new PlayerException("Player not found " + identifier);
        }
    }

    public List<Player> listPlayers(List<Long> rankIds) {
        return playerRepository.findByVisibleFalseAndRankIdIsInOrderByName(rankIds);
    }

    public void setPlayersVisible(List<Player> players) {
        Map<Long, Boolean> visibleMap = players.stream().collect(Collectors.toMap(Player::getId, Player::isVisible));
        List<Player> playersFromDatabase = playerRepository.findAll(visibleMap.keySet());
        List<Player> updatedPlayers = playersFromDatabase.stream()
                .peek(player -> player.setVisible(visibleMap.get(player.getId())))
                .collect(Collectors.toList());
        playerRepository.save(updatedPlayers);
    }

    Map<String, Player> getPlayerMap() {
        return playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getName, player -> player));
    }

    void deletePlayers(List<Player> players) {
        log.info("Deleting players: " + players.stream().map(Player::getName).collect(Collectors.joining()));
        playerRepository.delete(players);
    }

    List<Player> getVisiblePlayers() {
        return playerRepository.findByVisibleTrue();
    }
}
