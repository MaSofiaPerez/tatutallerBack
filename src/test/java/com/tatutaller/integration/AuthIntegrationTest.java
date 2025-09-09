package com.tatutaller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAuthDebugTest {

    @Autowired private org.springframework.test.web.servlet.MockMvc mockMvc;
    @Autowired private ObjectMapper mapper;

    private ObjectNode registerPayload(String name, String email, String password) {
        ObjectNode n = mapper.createObjectNode();
        n.put("name", name);              // @NotBlank, <= 100
        n.put("email", email);            // @Email
        n.put("password", password);      // @Size(min=6)
        n.put("phone", "099123456");      // opcional
        n.put("address", "Some Street");  // opcional
        return n;
    }

    private ObjectNode loginPayload(String email, String password) {
        ObjectNode n = mapper.createObjectNode();
        n.put("email", email);
        n.put("password", password);
        return n;
    }

    @Test
    @DisplayName("Diagnóstico: probar rutas de /auth y volcar respuesta 4xx")
    void debugAuthEndpoints() throws Exception {
        String email = "debug.user@test.com";
        String pass  = "secret123"; // >=6 como pide tu DTO

        List<String> regEndpoints = List.of(
                "/api/auth/register",
                "/auth/register",
                "/api/v1/auth/register"
        );
        List<String> loginEndpoints = List.of(
                "/api/auth/login",
                "/auth/login",
                "/api/v1/auth/login"
        );

        boolean any200 = false;
        String usedRegister = null;
        String usedLogin = null;

        // PROBAR REGISTRO EN MÚLTIPLES RUTAS
        for (int i = 0; i < regEndpoints.size(); i++) {
            String ep = regEndpoints.get(i);
            MvcResult res = mockMvc.perform(
                    post(ep)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(registerPayload("Usuario Debug", email, pass)))
            ).andReturn();

            int status = res.getResponse().getStatus();
            String body = res.getResponse().getContentAsString(StandardCharsets.UTF_8);
            System.out.println("[DEBUG][REGISTER][" + ep + "] status=" + status + " body=" + body);

            if (status == 200) {
                any200 = true;
                usedRegister = ep;
                usedLogin = loginEndpoints.get(i);
                break;
            }
        }

        assertThat(any200)
                .as("Ninguna ruta de registro devolvió 200. Revisá el log [DEBUG][REGISTER] para ver el motivo exacto del 400 (binding errors, mensaje, captcha, etc.).")
                .isTrue();

        // SI REGISTRO OK EN ALGUNA RUTA, PROBAR LOGIN EN LA RUTA CORRESPONDIENTE
        MvcResult loginRes = mockMvc.perform(
                post(usedLogin)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(loginPayload(email, pass)))
        ).andReturn();
        int loginStatus = loginRes.getResponse().getStatus();
        String loginBody = loginRes.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println("[DEBUG][LOGIN][" + usedLogin + "] status=" + loginStatus + " body=" + loginBody);

        assertThat(loginStatus).isEqualTo(200);
        assertThat(loginBody).contains("token");
    }
}
