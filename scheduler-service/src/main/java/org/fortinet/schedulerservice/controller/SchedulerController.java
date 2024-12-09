package org.fortinet.schedulerservice.controller;


import lombok.RequiredArgsConstructor;
import org.fortinet.schedulerservice.service.SchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping
    public ResponseEntity trigger(){
        schedulerService.scheduleJobs();
        return ResponseEntity.ok().build();
    }
}
