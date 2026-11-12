package edu.student.urinalysis.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import edu.student.urinalysis.repository.UrinalysisTestRepository;

@WebMvcTest(UrinalysisTestController.class)
class UrinalysisTestControllerCorsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrinalysisTestRepository repository;

    @Test
    void shouldAllowFrontendRunningOnVitePort5174() throws Exception {
        given(repository.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tests").header("Origin", "http://localhost:5174"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }
}
