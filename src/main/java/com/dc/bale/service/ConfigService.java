package com.dc.bale.service;

import com.dc.bale.database.dao.ConfigRepository;
import com.dc.bale.database.entity.Config;
import com.dc.bale.exception.ConfigException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {
    private final ConfigRepository configRepository;

    public String getMandatoryConfig(String name) {
        return configRepository.findByName(name)
                .map(Config::getValue)
                .orElseThrow(() -> new ConfigException("Unable to find config: " + name));
    }

    public Optional<String> getConfig(String name) {
        return configRepository.findByName(name)
                .map(Config::getValue);
    }
}
