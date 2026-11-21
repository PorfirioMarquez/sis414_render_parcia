package com.sisi414.parcial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Sisi414ParcialApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaMeses() throws Exception {
        mockMvc.perform(get("/api/meses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    void oficinaIncluyeNombreDeUnidadYResponsablesNoMuestranCiCompleto() throws Exception {
        mockMvc.perform(get("/api/oficinas/0/siste/1/responsables"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"ci\""))));
    }

    @Test
    void usuariosNoExponenContrasena() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("psw"))))
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void recursoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/meses/99"))
                .andExpect(status().isNotFound());
    }
}
