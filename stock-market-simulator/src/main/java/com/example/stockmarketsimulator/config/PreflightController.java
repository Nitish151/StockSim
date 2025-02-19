package com.example.stockmarketsimulator.config;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PreflightController {

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void handlePreflight() {
        // This method is intentionally left empty to handle preflight requests
    }
}
