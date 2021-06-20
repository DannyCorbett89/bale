package com.dc.bale.service;

import com.dc.bale.component.HttpClient;
import com.dc.bale.database.dao.MinionRepository;
import com.dc.bale.database.entity.Minion;
import com.dc.bale.exception.UnableToParseNumPagesException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dc.bale.Constants.MINIONS_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class LodestoneDataLoader {

    private final HttpClient httpClient;
    private final MinionRepository minionRepository;

    @PostConstruct
    private void loadAllMinions() {
        loadMinions(false);
    }

    @Scheduled(cron = "0 20 * * * *")
    private void loadLatestMinions() {
        loadMinions(true);
    }


    private int getNumPagesDB(String content, String url) {
        String urlPrefix = url.contains("&") ? url.substring(0, url.indexOf("&")) : url;
        String searchTerm = urlPrefix + "&page=";
        String formattedSearchTerm = searchTerm.replace("&", "&amp;");
        int total = StringUtils.countMatches(content, formattedSearchTerm);

        if (total <= 2) {
            return 1;
        }

        Pattern pattern = Pattern.compile("<li class=\"next_all\"><a href=\"" + formattedSearchTerm.replace("?", "\\?") + "([0-9]+).+?</li>");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String numPages = matcher.group(1);
            return Integer.parseInt(numPages);
        }

        throw new UnableToParseNumPagesException();
    }

    private String getFullUrl(String url, boolean latestPatch) {
        if (latestPatch) {
            url += "&patch=latest";
        }

        return url;
    }

    private void loadMinions(boolean latestPatch) {
        // TODO: Refactor, extract
        log.info("Loading minions...");
        String minionsUrl = getFullUrl(MINIONS_URL, latestPatch);
        String content = httpClient.get(minionsUrl);
        Set<String> minionNames = minionRepository.findAll().stream()
                .map(Minion::getName)
                .collect(Collectors.toSet());
        int numPages = getNumPagesDB(content, minionsUrl);

        for (int x = 1; x <= numPages; x++) {
            if (x > 1) {
                content = httpClient.get(minionsUrl + "&page=" + x)
                        .replace("\n", "")
                        .replace("\r", "");
            }

            Pattern pattern = Pattern.compile("</span>.+?<a href=\"/lodestone/playguide/db/item/([a-z0-9]+)/(?:\\?patch=latest)?\" class=\"db_popup db-table__txt--detail_link\">(.+?)</a>");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String lodestoneId = matcher.group(1);
                String name = matcher.group(2)
                        .replace("<i>", "")
                        .replace("</i>", "");
                if (!minionNames.contains(name)) {
                    minionRepository.save(Minion.builder()
                            .name(name)
                            .lodestoneId(lodestoneId)
                            .build());
                }
            }
        }
        log.info("Finished loading minions");
    }
}
