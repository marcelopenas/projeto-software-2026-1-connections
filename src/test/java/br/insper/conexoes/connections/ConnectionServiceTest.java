package br.insper.conexoes.connections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceTest {

    @Mock
    private ConnectionRepository repository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ConnectionService connectionService;

    @Test
    void create_shouldSaveConnectionWhenBothUsersExist() {
        // mocks
        String fromUserId = "user1";
        String toUserId = "user2";

        when(userClient.userExists(fromUserId)).thenReturn(true);
        when(userClient.userExists(toUserId)).thenReturn(true);

        Connection savedConnection = new Connection(fromUserId, toUserId);
        when(repository.save(any(Connection.class))).thenReturn(savedConnection);

        // ação
        Connection result = connectionService.create(fromUserId, toUserId);

        // asserções
        assertNotNull(result);
        assertEquals(fromUserId, result.getFromUserId());
        assertEquals(toUserId, result.getToUserId());

        verify(userClient).userExists(fromUserId);
        verify(userClient).userExists(toUserId);
        verify(eventProducer).send(any(Event.class));
        verify(repository).save(any(Connection.class));
    }

    // createShouldThrowNotFoundWhenFromUserDoesNotExist
    // createShouldThrowNotFoundWhenToUserDoesNotExist
    // listByUserShouldReturnConnectionsAndSendEvent
    // deleteShouldSendEventAndRemoveConnection

}
