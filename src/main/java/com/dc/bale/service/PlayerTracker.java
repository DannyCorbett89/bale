package com.dc.bale.service;

import com.dc.bale.database.dao.MountRepository;
import com.dc.bale.database.entity.Mount;
import com.dc.bale.database.entity.Player;
import com.dc.bale.exception.MountException;
import com.dc.bale.model.AvailableMount;
import com.dc.bale.model.MountRS;
import com.dc.bale.model.PlayerRS;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerTracker {
    private final MountRepository mountRepository;
    private final PlayerService playerService;
    private final FcLoader fcLoader;
    private final TrialService trialService;

    private String lastUpdated = "Never";

    public String getLastUpdated() {
        return lastUpdated;
    }

    public List<PlayerRS> getMounts() {
        List<Player> visiblePlayers = playerService.getVisiblePlayers();
        List<Mount> totalMounts = mountRepository.findAll();

        List<PlayerRS> players = visiblePlayers.stream().map(player -> PlayerRS.builder()
                .name(player.getName())
                .mounts(player.getMissingMounts(totalMounts).stream().map(mount -> MountRS.builder()
                        .id(mount.getId())
                        .name(mount.getName())
                        .instance(trialService.getInstance(mount))
                        .build()).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());

        for (PlayerRS player : players) {
            for (int i = player.getMounts().size() - 1; i >= 0; i--) {
                MountRS mount = player.getMounts().get(i);
                if (!anyPlayerHasMount(players, mount)) {
                    player.getMounts().remove(i);
                }
            }
        }

        Map<Long, Long> ilevels = trialService.getMountItemLevels();

        players.sort((o1, o2) -> Long.compare(o2.numMounts(), o1.numMounts()));
        players.forEach(player -> player.getMounts().sort(Comparator.comparingLong(mount -> ilevels.getOrDefault(mount.getId(), 0L))));

        return players;
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

    @PostConstruct
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
            Pattern pattern = Pattern.compile("<li class=\"entry\"><a href=\"(.+?)\".+?<p class=\"entry__name\">(.+?)</p>.+?<ul class=\"entry__freecompany__info\"><li><img src=\"(.+?)\".+?<span>(.+?)</span></li>.+?</li>.+?</li>");
            Matcher matcher = pattern.matcher(content);

            // Load all the mounts for each player from the lodestone
            while (matcher.find()) {
                String playerName = matcher.group(2).replace("&#39;", "'");
                fcPlayers.add(playerName);
            }
        }
        List<Player> oldPlayers = dbPlayers.values().stream()
                .filter(player -> !fcPlayers.contains(player.getName()))
                .collect(Collectors.toList());

        playerService.deletePlayers(oldPlayers);
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

    private boolean anyPlayerHasMount(List<PlayerRS> players, MountRS mount) {
        for (PlayerRS player : players) {
            for (MountRS mountRS : player.getMounts()) {
                if (mountRS.getId() == mount.getId() && mountRS.getName() != null) {
                    return true;
                }
            }
        }

        return false;
    }
}
