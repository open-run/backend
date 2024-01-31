package io.openur.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {

  @GetMapping("/v1/test")
  public String test(){return "test";}
}
