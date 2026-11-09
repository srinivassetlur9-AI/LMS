package com.lms.learning;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = com.lms.identity.IdentityApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LearningFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void enrolTrackProgressAndComplete() throws Exception {
        mvc.perform(get("/api/v1/learning/my-courses")).andExpect(status().isUnauthorized());

        String token = json.readTree(mvc.perform(post("/api/v1/identity/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "org.admin@lms.local", "password", "Demo@12345678"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("accessToken").asText();
        String bearer = "Bearer " + token;

        mvc.perform(get("/api/v1/learning/my-courses").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/learning/courses").header("Authorization", bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6)).andExpect(jsonPath("$[0].enrolled").value(false));

        mvc.perform(post("/api/v1/learning/courses/python-basics/enroll").header("Authorization", bearer))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.progressPercent").value(0)).andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mvc.perform(post("/api/v1/learning/courses/python-basics/enroll").header("Authorization", bearer)) // idempotent
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/learning/courses/nope/enroll").header("Authorization", bearer)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/learning/my-courses").header("Authorization", bearer)).andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put("/api/v1/learning/my-courses/python-basics/progress").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":40}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.progressPercent").value(40));
        mvc.perform(put("/api/v1/learning/my-courses/python-basics/progress").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":10}")) // never goes backwards
                .andExpect(jsonPath("$.progressPercent").value(40));
        mvc.perform(put("/api/v1/learning/my-courses/python-basics/progress").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":101}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/learning/my-courses/python-basics/progress").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":100}"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(put("/api/v1/learning/my-courses/machine-learning/progress").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":5}"))
                .andExpect(status().isNotFound());
    }
}
