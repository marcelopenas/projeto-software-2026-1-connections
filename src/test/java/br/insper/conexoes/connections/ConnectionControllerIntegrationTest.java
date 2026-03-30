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

    //listConnections_shouldReturnConnectionsForUser
    //deleteConnection_shouldRemoveConnection
    //createConnection_shouldReturn404WhenUserDoesNotExist

}
