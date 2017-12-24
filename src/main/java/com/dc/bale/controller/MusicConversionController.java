package com.dc.bale.controller;

import lombok.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RequestMapping("/music")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MusicConversionController {

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.TEXT_HTML_VALUE})
    public String conversionForm() throws IOException {
        String rawHtml = loadHtml();
        return rawHtml.replace("${RESULTS}", "")
                .replace("${ORIGINAL}", "");
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = {MediaType.TEXT_HTML_VALUE})
    public String convert(FormData body) throws IOException {
        String rawHtml = loadHtml();
        return rawHtml.replace("${ORIGINAL}", body.getMusicNotes())
                .replace("${RESULTS}", "<pre>" + body.getConvertedMusicNotes() + "</pre>");
    }

    private String loadHtml() throws IOException {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("static/resources/music.html");
        return IOUtils.toString(resource);
    }
}
