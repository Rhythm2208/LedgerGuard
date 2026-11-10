package com.rhythm.ledgerguard.security;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final JwtService jwt; private final String user,pass,tenant;
 public AuthController(JwtService jwt,@Value("${app.demo.username}")String user,@Value("${app.demo.password}")String pass,@Value("${app.demo.tenant}")String tenant){this.jwt=jwt;this.user=user;this.pass=pass;this.tenant=tenant;}
 public record Login(String username,String password){}
 @PostMapping("/token") public Map<String,String> token(@RequestBody Login r){
  if(!user.equals(r.username())||!pass.equals(r.password()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
  return Map.of("accessToken",jwt.issue(user,tenant),"tokenType","Bearer");
 }
}
