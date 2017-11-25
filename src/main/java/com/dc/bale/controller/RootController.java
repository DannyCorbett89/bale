package com.dc.bale.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// TODO: Re-enable when there is some content to go there
//@RequestMapping("/")
//@RestController
public class RootController {
    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.TEXT_HTML_VALUE})
    public String getMainWebPage() {
        return "<html><head><style>" +
                "p {font-family: helvetica; font-size: 14px;}" +
                "</style></head><body>" +
                "<p>Not implemented</p></body></html>";
    }
}