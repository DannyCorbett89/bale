package com.dc.bale.database.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

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
    private boolean mountsVisible;
    private boolean minionsVisible;
    private String url;
    private String icon;

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

    public String getColumnKey() {
        return "player-" + id;
    }

    public void clearMounts() {
        mounts.clear();
    }

    public void clearMinions() {
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

    public boolean hasMount(String mountName) {
        return mounts.stream().anyMatch(mount -> mount.getName().equals(mountName));
    }

    public boolean hasMinion(String minionName) {
        return minions.stream().anyMatch(minion -> minion.getName().equals(minionName));
    }

    public long getNumVisibleMounts() {
        return mounts == null ? 0 : mounts.stream().filter(Mount::isVisible).count();
    }

    public long getNumMinions() {
        return mounts == null ? 0 : minions.size();
    }
}
