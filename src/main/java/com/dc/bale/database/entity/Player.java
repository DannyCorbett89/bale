package com.dc.bale.database.entity;


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
    private boolean visible;
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

    public Set<Mount> getMissingMounts(List<Mount> totalMounts) {
        return totalMounts.stream()
                .filter(Mount::isVisible)
                .map(mount -> !mounts.contains(mount) ? mount : Mount.builder().id(mount.getId()).build())
                .collect(Collectors.toSet());
    }

    public void clear() {
        mounts.clear();
        minions.clear();
    }

    public void addMount(Mount mount) {
        if (mount != null && !hasMount(mount.getName())) {
            mounts.add(mount);
        }
    }

    public void addMinion(Minion minion) {
        if (minion != null && !hasMinion(minion.getName())) {
            minions.add(minion);
        }
    }

    private boolean hasMount(String mountName) {
        return mounts.stream().anyMatch(mount -> mount.getName().equals(mountName));
    }

    public boolean hasMinion(String minionName) {
        return minions.stream().anyMatch(minion -> minion.getName().equals(minionName));
    }
}
