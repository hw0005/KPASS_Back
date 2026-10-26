package com.example.kpass.domain.test;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.test.service.TestBO;

import lombok.RequiredArgsConstructor;

@RequestMapping("/api/test")
@RestController
@RequiredArgsConstructor
public class TestController {
	
	private final TestBO testBO;
	
    private final ServletWebServerApplicationContext webServerContext; // 실행 중인 실제 포트 추출용

    @GetMapping
    public Map<String, Object> runTest(
            @RequestParam(name = "message", defaultValue = "KPASS_START") String message) {
        
        String result = testBO.executeTest(message);
        int actualPort = webServerContext.getWebServer().getPort();

        return Map.of(
            "port", actualPort,
            "queryDslData", result,
            "status", "SUCCESS"
        );
    }
	
	
	
	
}
