package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.component.JsonMapper;
import com.dc.bale.model.ffxivcollect.MountsRS;
import com.dc.bale.util.SourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FFXIVCollectAPI {
    private static final String BASE_URL = "https://ffxivcollect.com/api";
    private static final String MOUNTS = BASE_URL + "/mounts?sources_type_id_eq=";

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;

    public MountsRS getMounts(SourceType type) {
        String mountsResponse = httpClient.get(MOUNTS + type.getTypeId());
        return jsonMapper.toObject(mountsResponse, MountsRS.class);
    }
}
