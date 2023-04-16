package com.example.emptySaver.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class TestController {
    @GetMapping("/helloTest")
    public String helloTest(Model model){
        log.info("Called HelloTest");
        model.addAttribute("greet","hello from Controller~");
        return "test/hello";
    }
}
