package com.gep.foro_alura.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gep.foro_alura.domain.usuario.*;
import com.gep.foro_alura.domain.respuesta.*;
import com.gep.foro_alura.domain.respuesta.validaciones.actualizar.SolucionDuplicada;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.*;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class SecurityRegressionTest {
    private TokenService service() {
        var service = new TokenService();
        ReflectionTestUtils.setField(service, "apiSecret", "regression-test-secret");
        return service;
    }
    @Test void tokenRoundTripAndSixHourExpiry() {
        var service = service();
        var user = new Usuario(new CrearUsuarioDTO("ana", "password", "Ana", "Perez", "ana@example.com"), "hash");
        String token = service.generarToken(user);
        assertEquals("ana", service.getSubject(token));
        long seconds = JWT.decode(token).getExpiresAtAsInstant().getEpochSecond() - Instant.now().getEpochSecond();
        assertTrue(seconds >= 21595 && seconds <= 21600);
    }
    @Test void rejectsMalformedAndExpiredTokens() {
        var service = service();
        assertThrows(JWTVerificationException.class, () -> service.getSubject("invalid"));
        String expired = JWT.create().withIssuer("alura").withSubject("ana")
            .withExpiresAt(Instant.now().minusSeconds(60)).sign(Algorithm.HMAC256("regression-test-secret"));
        assertThrows(JWTVerificationException.class, () -> service.getSubject(expired));
    }
    @Test void invalidTokenReturns401WithoutCallingEndpoint() throws Exception {
        var filter = new SecurityFilter();
        ReflectionTestUtils.setField(filter, "tokenService", service());
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> fail("Endpoint must not execute"));
        assertEquals(401, response.getStatus());
    }
    @Test void partialReplyUpdateDoesNotRequireSolution() {
        assertDoesNotThrow(() -> new SolucionDuplicada().validate(new ActualizarRespuestaDTO("edit", null, null), 1L));
    }
}
