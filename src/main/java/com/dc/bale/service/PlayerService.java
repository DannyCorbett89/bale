package com.dc.bale.service;

import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.PlayerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;

    public List<Player> addPlayersForMounts(List<Long> ids) {
        List<Player> players = playerRepository.findByIdIsIn(ids);
        players.forEach(player -> player.setMountsVisible(true));
        return playerRepository.save(players);
    }

    public List<Player> addPlayersForMinions(List<Long> ids) {
        List<Player> players = playerRepository.findByIdIsIn(ids);
        players.forEach(player -> player.setMinionsVisible(true));
        return playerRepository.save(players);
    }

    public Player removePlayerForMounts(Long id, String name) throws PlayerException {
        return removePlayer(id, name, player -> player.setMountsVisible(false));
    }

    public Player removePlayerForMinions(Long id, String name) throws PlayerException {
        return removePlayer(id, name, player -> player.setMinionsVisible(false));
    }

    private Player removePlayer(Long id, String name, Consumer<Player> action) throws PlayerException {
        Player player;
        String identifier = "with id " + id;

        if (id != null) {
            player = playerRepository.findOne(id);
        } else {
            identifier += " or name " + name;
            player = playerRepository.findByName(name);
        }

        if (player != null) {
            action.accept(player);
            return playerRepository.save(player);
        } else {
            throw new PlayerException("Player not found " + identifier);
        }
    }

    public List<Player> listPlayersForMounts() {
        return playerRepository.findByMountsVisibleFalseOrderByName();
    }

    public List<Player> listPlayersForMinions() {
        return playerRepository.findByMinionsVisibleFalseOrderByName();
    }

    public List<Player> listVisiblePlayersForMounts() {
        return playerRepository.findByMountsVisibleTrue().stream()
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());
    }

    public List<Player> listVisiblePlayersForMinions() {
        return playerRepository.findByMinionsVisibleTrue().stream()
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());
    }

    public void setPlayersVisibleForMounts(List<Player> players) {
        Map<Long, Boolean> visibleMap = players.stream().collect(Collectors.toMap(Player::getId, Player::isMountsVisible));
        List<Player> playersFromDatabase = playerRepository.findAll(visibleMap.keySet());
        List<Player> updatedPlayers = playersFromDatabase.stream()
                .peek(player -> player.setMountsVisible(visibleMap.get(player.getId())))
                .collect(Collectors.toList());
        playerRepository.save(updatedPlayers);
    }

    public void setPlayersVisibleForMinions(List<Player> players) {
        Map<Long, Boolean> visibleMap = players.stream().collect(Collectors.toMap(Player::getId, Player::isMinionsVisible));
        List<Player> playersFromDatabase = playerRepository.findAll(visibleMap.keySet());
        List<Player> updatedPlayers = playersFromDatabase.stream()
                .peek(player -> player.setMinionsVisible(visibleMap.get(player.getId())))
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

    List<Player> getPlayersVisibleForMounts() {
        return playerRepository.findByMountsVisibleTrue();
    }

    List<Player> getPlayersVisibleForMinions() {
        return playerRepository.findByMinionsVisibleTrue();
    }
}
