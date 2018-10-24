package com.dc.bale.controller;

import com.dc.bale.component.HttpClient;
import com.dc.bale.component.JsonConverter;
import com.dc.bale.database.Config;
import com.dc.bale.database.ConfigRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequestMapping("/domain")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DomainNameController {
    @NonNull
    private HttpClient httpClient;
    @NonNull
    private ConfigRepository configRepository;
    @NonNull
    private JsonConverter jsonConverter;

    private String lastUpdated = "Never";

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String showLastChanged() {
        return "{\"LastUpdated\": \"" + lastUpdated + "\"}";
    }

    @Scheduled(fixedRate = 3600000)
    public void hasIPAddressChanged() throws IOException {
        String domain = "bahamutslegion.com";
        String url = "https://api.godaddy.com/v1/domains/" + domain + "/records/A/@";
        Config key = configRepository.findByName("domainKey");
        Config secret = configRepository.findByName("domainSecret");

        if (key == null || secret == null) {
            return;
        }

        String ssoKey = "sso-key " + key.getValue() + ":" + secret.getValue();
        Optional<String> response = httpClient.get(url, ssoKey);
        if (!response.isPresent()) {
            return;
        }
        String result = response.get();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Record> records = objectMapper.readValue(result, new TypeReference<List<Record>>() {
        });

        if (!records.isEmpty()) {
            Record record = records.get(0);
            String registeredIpAddress = record.getData();
            Optional<String> ipResponse = httpClient.get("http://checkip.amazonaws.com");
            if (!ipResponse.isPresent()) {
                return;
            }
            String ip = ipResponse.get();
            String currentIpAddress = ip.replace("\n", "");

            if (!currentIpAddress.equals(registeredIpAddress)) {
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
