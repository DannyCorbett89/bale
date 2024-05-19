package com.dc.bale.hidden.controller2;

import com.dc.bale.component.HttpClient;
import com.dc.bale.service.ConfigService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import java.util.Optional;

@Slf4j
@RequestMapping("/domain")
@RestController
@RequiredArgsConstructor
public class DomainNameController {
    private final HttpClient httpClient;
    private final ConfigService configService;

    private String lastUpdated = "Never";

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String showLastChanged() {
        return "{\"LastUpdated\": \"" + lastUpdated + "\"}";
    }

    @PostConstruct
    @Scheduled(fixedRate = 3600000)
    public void hasIPAddressChanged() throws IOException {
        Optional<String> domain = configService.getConfig("domainName");

        if (domain.isEmpty()) {
            log.info("domainName has not been set, skipping IP check");
            return;
        }

        updateARecord(domain.get());
        updateARecord("dc-minecraft.com");
    }

    private void updateARecord(String domain) throws IOException {
        String url = "https://api.godaddy.com/v1/domains/" + domain + "/records/A/@";
        Optional<String> key = configService.getConfig("domainKey");
        Optional<String> secret = configService.getConfig("domainSecret");

        if (key.isEmpty() || secret.isEmpty()) {
            log.info("Key/Secret empty, unable to check IP");
            return;
        }

        String ssoKey = "sso-key " + key.get() + ":" + secret.get();
        String response = httpClient.get(url, ssoKey);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Record> records = objectMapper.readValue(response, new TypeReference<List<Record>>() {
            });

            if (!records.isEmpty()) {
                Record record = records.get(0);
                String registeredIpAddress = record.getData();
                String currentIpAddress = httpClient.get("http://checkip.amazonaws.com").replace("\n", "");

                if (!currentIpAddress.equals(registeredIpAddress)) {
                    log.info("Current IP [" + currentIpAddress + "] does not match website IP [" + registeredIpAddress + "] for " + domain + " domain, updating");
                    record.setData(currentIpAddress);
                    List<Record> recordList = new ArrayList<>();
                    recordList.add(record);
                    String json = objectMapper.writeValueAsString(recordList);

                    httpClient.put(url, ContentType.APPLICATION_JSON, ssoKey, json);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    lastUpdated = sdf.format(new Date());
                }
            }
        } catch (JsonMappingException e) {
            log.error("Unable to parse response from {}: {}", url, response);
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
