package com.lms.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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
class LearningItemsFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String bearer(String email) throws Exception {
        return "Bearer " + json.readTree(mvc.perform(post("/api/v1/identity/auth/login").header("X-Device-Id", "mentor-test-device-1").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", "Demo@12345678"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void certificateIsIssuedOnCompletion() throws Exception {
        String t = bearer("content.admin@lms.local");
        mvc.perform(get("/api/v1/learning/certificates")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/learning/certificates").header("Authorization", t)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(post("/api/v1/learning/courses/cloud-computing/enroll").header("Authorization", t)).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/learning/my-courses/cloud-computing/progress").header("Authorization", t)
                .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":60}"));
        mvc.perform(get("/api/v1/learning/certificates").header("Authorization", t)).andExpect(jsonPath("$.length()").value(0));
        for (int i = 0; i < 2; i++) { // completing twice still yields one certificate
            mvc.perform(put("/api/v1/learning/my-courses/cloud-computing/progress").header("Authorization", t)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"progressPercent\":100}")).andExpect(status().isOk());
        }
        JsonNode list = json.readTree(mvc.perform(get("/api/v1/learning/certificates").header("Authorization", t))
                .andReturn().getResponse().getContentAsString());
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("certificateNo").asText()).startsWith("UNI-");
        assertThat(list.get(0).get("courseTitle").asText()).isEqualTo("Cloud Computing");
        long id = list.get(0).get("id").asLong();
        mvc.perform(get("/api/v1/learning/certificates/" + id).header("Authorization", t)).andExpect(status().isOk());
        // another user cannot open it
        mvc.perform(get("/api/v1/learning/certificates/" + id).header("Authorization", bearer("exam.admin@lms.local")))
                .andExpect(status().isNotFound());
    }

    @Test
    void liveSessionsRegistrationAndCalendar() throws Exception {
        String t = bearer("event.admin@lms.local");
        JsonNode list = json.readTree(mvc.perform(get("/api/v1/learning/live-sessions").header("Authorization", t))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(list).hasSize(9);
        assertThat(list.get(0).get("courseTitle").asText()).isNotBlank();
        long upcoming = 0, ended = 0;
        for (JsonNode s : list) { if (s.get("ended").asBoolean()) ended = s.get("id").asLong(); else if (upcoming == 0) upcoming = s.get("id").asLong(); }
        mvc.perform(post("/api/v1/learning/live-sessions/" + upcoming + "/register").header("Authorization", t))
                .andExpect(status().isOk()).andExpect(jsonPath("$.registered").value(true));
        mvc.perform(post("/api/v1/learning/live-sessions/" + upcoming + "/register").header("Authorization", t)).andExpect(status().isOk()); // idempotent
        mvc.perform(post("/api/v1/learning/live-sessions/" + ended + "/register").header("Authorization", t))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SESSION_ENDED"));
        mvc.perform(delete("/api/v1/learning/live-sessions/" + upcoming + "/register").header("Authorization", t))
                .andExpect(status().isOk()).andExpect(jsonPath("$.registered").value(false));
        mvc.perform(post("/api/v1/learning/live-sessions/99999/register").header("Authorization", t)).andExpect(status().isNotFound());
    }

    @Test
    void askMentorAndAnswer() throws Exception {
        String learner = bearer("learner@lms.local");
        mvc.perform(post("/api/v1/learning/mentor/questions").header("Authorization", learner).header("X-Device-Id", "mentor-test-device-1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"subject\":\"\",\"question\":\"x\"}")).andExpect(status().isBadRequest());
        String res = mvc.perform(post("/api/v1/learning/mentor/questions").header("Authorization", learner).header("X-Device-Id", "mentor-test-device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseRef\":\"machine-learning\",\"subject\":\"Overfitting\",\"question\":\"How do I spot overfitting?\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("OPEN")).andReturn().getResponse().getContentAsString();
        long id = json.readTree(res).get("id").asLong();

        // learners cannot read the inbox or answer
        mvc.perform(get("/api/v1/learning/mentor/inbox").header("Authorization", learner).header("X-Device-Id", "mentor-test-device-1"))
                .andExpect(status().isForbidden());
        String mentor = bearer("instructor@lms.local");
        mvc.perform(get("/api/v1/learning/mentor/inbox").header("Authorization", mentor)).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + id + ")].subject").value("Overfitting"));
        mvc.perform(post("/api/v1/learning/mentor/questions/" + id + "/answer").header("Authorization", mentor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"answer\":\"Compare training and validation error.\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ANSWERED"));
        mvc.perform(post("/api/v1/learning/mentor/questions/" + id + "/answer").header("Authorization", mentor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"answer\":\"again\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/learning/mentor/questions").header("Authorization", learner).header("X-Device-Id", "mentor-test-device-1"))
                .andExpect(jsonPath("$[0].status").value("ANSWERED")).andExpect(jsonPath("$[0].answer").value("Compare training and validation error."));
    }
}
