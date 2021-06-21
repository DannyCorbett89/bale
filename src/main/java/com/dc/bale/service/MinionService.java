package com.dc.bale.service;

import com.dc.bale.database.dao.MinionRepository;
import com.dc.bale.database.dao.PlayerRepository;
import com.dc.bale.database.entity.Minion;
import com.dc.bale.database.entity.Player;
import com.dc.bale.model.MinionRS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinionService {
    private final MinionRepository minionRepository;
    private final PlayerRepository playerRepository;

    public synchronized void addMinion(String name) {
        Minion minion = minionRepository.findByName(name);

        if (minion == null) {
            minionRepository.save(Minion.builder()
                    .name(name)
                    .build());
        }
    }

    public List<MinionRS> getMinions() {
        List<Player> visiblePlayers = playerRepository.findByVisibleTrue();
        List<Minion> totalMinions = minionRepository.findAll();
        return totalMinions.stream()
                .filter(minion -> anyPlayerNeedsMinion(minion, visiblePlayers))
                .map(minion -> getPlayersNeedingMinion(visiblePlayers, minion))
                .sorted(Comparator.comparing(MinionRS::getName))
                .collect(Collectors.toList());
    }

    private boolean anyPlayerNeedsMinion(Minion minion, List<Player> players) {
        return players.stream().anyMatch(player -> !player.hasMinion(minion.getName()));
    }

    private MinionRS getPlayersNeedingMinion(List<Player> players, Minion minion) {
        return MinionRS.builder()
                .id(minion.getId())
                .name(minion.getDisplayName())
                .lodestoneId(minion.getLodestoneId())
                .players(players.stream()
                        .filter(player1 -> playerDoesNotHaveMinion(minion, player1))
                        .collect(Collectors.toMap(player -> "player-" + player.getId(), player -> "X")))
                .build();
    }

    private boolean playerDoesNotHaveMinion(Minion minion, Player player1) {
        return player1.getMinions().stream()
                .noneMatch(playerMinion -> playerMinion.getId() == minion.getId());
    }

    synchronized Optional<Minion> getAndUpdateMinionForHash(Player player, String hash, Supplier<String> lookupMinionNameFromHash) {
        Minion minion = minionRepository.findByHash(hash);
        if (minion != null) {
            return Optional.of(minion);
        } else {
            String name = lookupMinionNameFromHash.get();

            if (name != null) {
                Minion minionByName = minionRepository.findByName(name);

                if (minionByName != null) {
                    minionByName.setHash(hash);
                } else {
                    minionByName = Minion.builder()
                            .name(name)
                            .hash(hash)
                            .build();
                }
                minionRepository.save(minionByName);

                return Optional.of(minionByName);
            } else {
                log.error("Unable to load minion name for hash {}, player {}", hash, player.getName());
                return Optional.empty();
            }
        }
    }
}
