package com.dockerforjavadevelopers.hello;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
        return "Hello World\n";
    }

    @RequestMapping("/debug/headers")
    public ResponseEntity<Map<String, String>> debugHeaders(@RequestHeader Map<String, String> headers) {
        return ResponseEntity.ok(headers);
    }
    
}
