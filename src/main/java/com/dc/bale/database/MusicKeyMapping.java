package com.dc.bale.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicKeyMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String c;
    private String c_sharp;
    private String d;
    private String e_flat;
    private String e;
    private String f;
    private String f_sharp;
    private String g;
    private String g_sharp;
    private String a;
    private String b_flat;
    private String b;
    private String octave_up;
    private String octave_down;
}
