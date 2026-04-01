package br.insper.conexoes.connections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ConnectionControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:7"));

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.0.5-alpine"))
                    .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConnectionRepository repository;

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private EventProducer eventProducer;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createConnection_shouldPersistAndReturnConnection() throws Exception {
        when(userClient.userExists("user1")).thenReturn(true);
        when(userClient.userExists("user2")).thenReturn(true);

        var request = new CreateConnectionRequest("user1", "user2");

        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromUserId").value("user1"))
                .andExpect(jsonPath("$.toUserId").value("user2"));

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void listConnections_shouldReturnConnectionsForUser() throws Exception {
        // prepare data
        repository.save(new Connection("user1", "user2"));
        repository.save(new Connection("user2", "user1"));

        mockMvc.perform(get("/connections/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromUserId").value("user1"))
                .andExpect(jsonPath("$[0].toUserId").value("user2"));
    }

    @Test
    void deleteConnection_shouldRemoveConnection() throws Exception {
        repository.save(new Connection("user1", "user2"));

        var request = new DeleteConnectionRequest("user1", "user2");
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(delete("/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void createConnection_shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(userClient.userExists("user1")).thenReturn(false);
        when(userClient.userExists("user2")).thenReturn(true);

        var request = new CreateConnectionRequest("user1", "user2");

        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createConnection_shouldReturn404WhenToUserDoesNotExist() throws Exception {
        when(userClient.userExists("user1")).thenReturn(true);
        when(userClient.userExists("user2")).thenReturn(false);

        var request = new CreateConnectionRequest("user1", "user2");

        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post("/connections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

}
