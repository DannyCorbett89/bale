package com.dc.bale.service;

import com.dc.bale.database.dao.ConfigRepository;
import com.dc.bale.database.entity.Config;
import com.dc.bale.exception.ConfigException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigService {
    private final ConfigRepository configRepository;

    public String getConfig(String name) {
        Config config = configRepository.findByName(name);

        if(config == null) {
            throw new ConfigException("Unable to find config: " + name);
        }

        return config.getValue();
    }
}
