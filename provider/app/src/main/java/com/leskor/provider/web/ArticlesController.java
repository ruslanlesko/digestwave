package com.leskor.provider.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticlesController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
