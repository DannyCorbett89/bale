package com.dc.bale.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Trial {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String boss;
    @Column(name = "lodestone_id")
    private String lodestoneId;
    private boolean loaded;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "mount_link",
            joinColumns = @JoinColumn(name = "trial_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "mount_id", referencedColumnName = "id"))
    private List<Mount> mounts;

    public boolean hasAllValues() {
        return name != null && boss != null && lodestoneId != null;
    }
}
