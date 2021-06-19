package com.dc.bale.hidden.controller2;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.ConfigRepository;
import com.dc.bale.database.entity.Config;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RequestMapping("/domain")
@RestController
@RequiredArgsConstructor
public class DomainNameController {
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private ConfigRepository configRepository;

    private String lastUpdated = "Never";

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String showLastChanged() {
        return "{\"LastUpdated\": \"" + lastUpdated + "\"}";
    }

    @PostConstruct
    @Scheduled(fixedRate = 3600000)
    public void hasIPAddressChanged() throws IOException {
        Config domain = configRepository.findByName("domainName");

        if (domain == null) {
            log.info("domainName has not been set, skipping IP check");
            return;
        }

        String url = "https://api.godaddy.com/v1/domains/" + domain.getValue() + "/records/A/@";
        Config key = configRepository.findByName("domainKey");
        Config secret = configRepository.findByName("domainSecret");

        if (key == null || secret == null) {
            log.info("Key/Secret empty, unable to check IP");
            return;
        }

        String ssoKey = "sso-key " + key.getValue() + ":" + secret.getValue();
        String response = httpClient.get(url, ssoKey);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Record> records = objectMapper.readValue(response, new TypeReference<List<Record>>() {
        });

        if (!records.isEmpty()) {
            Record record = records.get(0);
            String registeredIpAddress = record.getData();
            String currentIpAddress = httpClient.get("http://checkip.amazonaws.com").replace("\n", "");

            if (!currentIpAddress.equals(registeredIpAddress)) {
                log.info("Current IP [" + currentIpAddress + "] does not match website IP [" + registeredIpAddress + "], updating");
                record.setData(currentIpAddress);
                List<Record> recordList = new ArrayList<>();
                recordList.add(record);
                String json = objectMapper.writeValueAsString(recordList);

                httpClient.put(url, ContentType.APPLICATION_JSON, ssoKey, json);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                lastUpdated = sdf.format(new Date());
            }
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Record {
        private String type;
        private String name;
        private String data;
        private int ttl;
    }
}
