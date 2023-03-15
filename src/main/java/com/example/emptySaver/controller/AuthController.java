package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.LoginDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO){


        //do something to return jwt

        return new ResponseEntity<>("ok", null, HttpStatus.OK);
    }
}
