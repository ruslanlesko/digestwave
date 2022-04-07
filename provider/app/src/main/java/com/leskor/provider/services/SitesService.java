package com.leskor.provider.services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Service
public class SitesService {
    private final Properties siteCodes;

    public SitesService() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("sites.properties");
        if (in == null) throw new RuntimeException("File with site codes is not found");
        Properties props = new Properties();
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load site codes", e);
        }
        this.siteCodes = props;
    }

    public String siteForCode(String code) {
        return siteCodes.getProperty(code, "");
    }
}
