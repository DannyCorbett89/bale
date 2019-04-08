package com.dc.bale.database;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private boolean tracking;
    private String url;

    @OneToOne
    private FcRank rank;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "mount_link",
            joinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "mount_id", referencedColumnName = "id"))
    private Set<Mount> mounts;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "minion_link",
            joinColumns = @JoinColumn(name = "player_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "minion_id", referencedColumnName = "id"))
    private Set<Minion> minions;

    public Set<Minion> getMissingMinions(List<Minion> totalMinions) {
        return totalMinions.stream()
                .map(minion -> !minions.contains(minion) ? minion : Minion.builder().id(minion.getId()).build())
                .collect(Collectors.toSet());
    }

    public boolean hasMount(String mountName) {
        return mounts.stream().anyMatch(mount -> mount.getName().equals(mountName));
    }

    public boolean hasMinion(String minionName) {
        return minions.stream().anyMatch(minion -> minion.getName().equals(minionName));
    }

    public boolean addMount(Mount mount) {
        return mount != null && mounts.add(mount);
    }

    public boolean addMinion(Minion minion) {
        return minion != null && minions.add(minion);
    }
}
