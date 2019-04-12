package com.dc.bale.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MountLink {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "player_id")
    private long playerId;
    @Column(name = "mount_id")
    private long mountId;
    @Column(name = "trial_id")
    private long trialId;
}
