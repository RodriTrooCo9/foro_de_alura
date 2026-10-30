package com.gep.foro_alura.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gep.foro_alura.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Service
public class TokenService {

    @Value("${api.security.secret}")
    private String apiSecret;

    public String generarToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            return JWT.create()
                    .withIssuer("alura")
                    .withSubject(usuario.getUsername())
                    .withClaim("id", usuario.getId())
                    .withExpiresAt(generarFechaVencimiento())
                    .sign(algorithm);
        }catch (JWTCreationException e){
            throw new RuntimeException("Failed to generate token");
        }

    }

    public String getSubject(String token){
        return JWT.require(Algorithm.HMAC256(apiSecret))
                .withIssuer("alura").build().verify(token).getSubject();
    }

    private Instant generarFechaVencimiento() {
        return Instant.now().plusSeconds(6 * 60 * 60);
    }

}
