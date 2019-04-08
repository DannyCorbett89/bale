package com.dc.bale.database;

import com.dc.bale.service.LodestoneDataLoader;
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
public class Minion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    @Column(name = "lodestone_id")
    private String lodestoneId;

    public String getUrl() {
        return LodestoneDataLoader.ITEM_URL + "/" + lodestoneId;
    }

    public String getDisplayName() {
//        return name != null ? "<a href=\"" + getUrl() + "\" target=\"_blank\">" + name + "</a>" : "";
        return name != null ? name : "";
    }
}
