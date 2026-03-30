package br.insper.conexoes.connections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ConnectionService {

    @Autowired
    private ConnectionRepository repository;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private UserClient userClient;


    public Connection create(String fromUserId, String toUserId) {

        if (!userClient.userExists(fromUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!userClient.userExists(toUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        eventProducer.send(
                new Event("CREATE_USER",
                        fromUserId + " connected " + toUserId,
                        "CONNECTIONS_API"));

        Connection connection = new Connection(fromUserId, toUserId);
        return repository.save(connection);
    }

    public List<Connection> listByUser(String userId) {
        eventProducer.send(new Event("LIST_USER",
                "List all users",
                "CONNECTIONS_API"));

        return repository.findByFromUserId(userId);
    }

    public void delete(String fromUserId, String toUserId) {
        eventProducer.send(new Event("DELETE_CONNECTION",
                fromUserId + " disconnected " + toUserId,
                "CONNECTIONS_API"));

        repository.deleteByFromUserIdAndToUserId(fromUserId, toUserId);
    }
}
