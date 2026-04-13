package com.example.demo.controller.app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.demo.controller.app")
public class AppExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        ex.printStackTrace();
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        response.put("trace", sw.toString());

        return ResponseEntity.status(500).body(response);
    }
}
