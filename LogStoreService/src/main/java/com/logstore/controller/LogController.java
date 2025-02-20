package com.logstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logstore.service.LogService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/log")
public class LogController {
	
	@Autowired
	LogService logService;

    @PostMapping
    public Mono<ResponseEntity<String>> ingestLog(@RequestBody String logJson) {
    	logService.addLog(logJson);
        return Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).body("Log received"));
    }
}
