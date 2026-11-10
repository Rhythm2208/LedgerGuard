package com.rhythm.ledgerguard.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key; private final long expiry;
 public JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiry-seconds:3600}") long expiry) {
  if(secret.getBytes(StandardCharsets.UTF_8).length<32) throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
  key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiry=expiry;
 }
 public String issue(String subject,String tenant) {
  Instant now=Instant.now();
  return Jwts.builder().subject(subject).claim("tenant",tenant).issuedAt(Date.from(now))
   .expiration(Date.from(now.plusSeconds(expiry))).signWith(key).compact();
 }
 public TenantPrincipal verify(String token) {
  var c=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  String tenant=c.get("tenant",String.class);
  if(tenant==null||tenant.isBlank()) throw new IllegalArgumentException("Missing tenant claim");
  return new TenantPrincipal(tenant,c.getSubject());
 }
}
