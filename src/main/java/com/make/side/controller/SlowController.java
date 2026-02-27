package com.make.side.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("test")
public class SlowController {

    private static final Logger log = LoggerFactory.getLogger(SlowController.class);

    @GetMapping("/slow")
    public ResponseEntity<String> slow() throws InterruptedException {
        String thread = Thread.currentThread().getName();
        log.info("[SLOW] 스레드 점유 시작 - thread={}", thread);
        Thread.sleep(10_000000);
        log.info("[SLOW] 스레드 점유 종료 - thread={}", thread);
        return ResponseEntity.ok("done");
    }
}
