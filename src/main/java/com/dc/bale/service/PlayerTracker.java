package com.dc.bale.service;

import com.dc.bale.database.dao.MountRepository;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.Column;
import com.dc.bale.model.MountRS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerTracker {
    private static final int WIDTH_MODIFIER = 9;

    private final MountRepository mountRepository;
    private final PlayerService playerService;
    private final FcLoader fcLoader;
    private final TrialService trialService;
    private final ConfigService configService;

    private String lastUpdated = "Never";

    public String getLastUpdated() {
        return lastUpdated;
    }

    public List<MountRS> getMounts() {
        List<Player> visiblePlayers = playerService.getVisiblePlayers();
        List<Mount> totalMounts = mountRepository.findAllByVisible(true);
        Map<Long, Long> ilevels = trialService.getMountItemLevels();

        return totalMounts.stream()
                .filter(mount -> anyPlayerNeedsMount(mount, visiblePlayers))
                .map(mount -> getPlayersNeedingMount(visiblePlayers, mount))
                .sorted((mount1, mount2) ->
                        Long.compare(ilevels.getOrDefault(mount2.getId(), 0L),
                                ilevels.getOrDefault(mount1.getId(), 0L)))
                .collect(Collectors.toList());
    }

    private boolean anyPlayerNeedsMount(Mount mount, List<Player> players) {
        return players.stream().anyMatch(player -> !player.hasMount(mount.getName()));
    }

    private MountRS getPlayersNeedingMount(List<Player> players, Mount mount) {
        return MountRS.builder()
                .id(mount.getId())
                .name(trialService.getInstance(mount))
                .players(players.stream()
                        .filter(player -> playerDoesNotHaveMount(mount, player))
                        .collect(Collectors.toMap(player -> "player-" + player.getId(), player -> "X")))
                .build();
    }

    private boolean playerDoesNotHaveMount(Mount mount, Player player) {
        return player.getMounts().stream()
                .noneMatch(playerMount -> playerMount.getId() == mount.getId());
    }

    public void addMount(String name) throws MountException {
        Mount mount = mountRepository.findByName(name);

        if (mount == null) {
            throw new MountException("Unknown mount: " + name);
        } else {
            mount.setVisible(true);
            mountRepository.save(mount);
        }
    }

    public Mount removeMount(long id) throws MountException {
        Mount mount = mountRepository.findOne(id);

        if (mount == null) {
            throw new MountException("Unknown mount: " + id);
        } else {
            mount.setVisible(false);
            mountRepository.save(mount);
        }

        return mount;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void loadMounts() {
        fcLoader.loadPlayerData();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lastUpdated = sdf.format(new Date());
    }

    @PostConstruct
    @Scheduled(cron = "0 30 * * * *")
    public void cleanOldPlayers() {
        Map<String, Player> dbPlayers = playerService.getPlayerMap();
        Set<String> fcPlayers = new HashSet<>();

        String content = fcLoader.getFCPageContent();

        if (content == null || content.isEmpty()) {
            return;
        }

        int numPages = fcLoader.getNumPages(content);

        for (int x = 1; x <= numPages; x++) {
            // First page is already loaded, don't load it again
            if (x > 1) {
                content = fcLoader.getFCPageContent(x);
            }
            String playerRegex = configService.getConfig("regex_old_players");
            Pattern pattern = Pattern.compile(playerRegex);
            Matcher matcher = pattern.matcher(content);

            // Load all the mounts for each player from the lodestone
            while (matcher.find()) {
                String playerName = matcher.group(2).replace("&#39;", "'");
                fcPlayers.add(playerName);
            }
        }

        if(!fcPlayers.isEmpty()) {
            List<Player> oldPlayers = dbPlayers.values().stream()
                    .filter(player -> !fcPlayers.contains(player.getName()))
                    .collect(Collectors.toList());

            if (!oldPlayers.isEmpty()) {
                log.info("FC players: {}", fcPlayers.toString());
            }

            playerService.deletePlayers(oldPlayers);
        }
    }

    public List<AvailableMount> getAvailableMounts() {
        return mountRepository.findAllByVisible(false).stream()
                .map(mount -> AvailableMount.builder()
                        .id(mount.getId())
                        .name(mount.getName())
                        .build())
                .sorted(Comparator.comparing(AvailableMount::getName))
                .collect(Collectors.toList());
    }

    public List<Column> getColumns(int firstColumnWidth, String firstColumnName) {
        List<Player> visiblePlayers = playerService.getVisiblePlayers();
        Map<String, Long> numMounts = visiblePlayers.stream().collect(Collectors.toMap(Player::getColumnKey, Player::getNumVisibleMounts));
        List<Column> columns = new ArrayList<>();
        columns.add(Column.builder()
                .key("name")
                .name(firstColumnName)
                .frozen(true)
                .filterable(true)
                .width(firstColumnWidth)
                .build());
        columns.addAll(visiblePlayers.stream()
                .map(this::getColumn)
                .sorted(Comparator.comparingLong(column -> numMounts.getOrDefault(column.getKey(), 0L)))
                .collect(Collectors.toList()));
        return columns;
    }

    private Column getColumn(Player player) {
        return Column.builder()
                .key(player.getColumnKey())
                .name(player.getName())
                .width(player.getName().length() * WIDTH_MODIFIER)
                .build();
    }
}
