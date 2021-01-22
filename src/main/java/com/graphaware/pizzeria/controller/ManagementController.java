package com.graphaware.pizzeria.controller;

import com.graphaware.pizzeria.service.ManagementService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    @CrossOrigin(methods = { RequestMethod.GET, RequestMethod.OPTIONS })
    @GetMapping(path = "/manage/statistic/purchases", produces = MediaType.APPLICATION_JSON_VALUE)
    public long getPurchasesCount() {
        return managementService.getPurchasesCount();
    }
}
