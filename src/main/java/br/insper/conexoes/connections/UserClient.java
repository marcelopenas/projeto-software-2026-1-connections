package br.insper.conexoes.connections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    @Value("${USER_URL:http://localhost:5001}")
    private String userUrl;


    public boolean userExists(String id) {
        try {
            System.out.println(userUrl + "/users/" + id);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<UserResponse> user =
                    restTemplate.getForEntity(userUrl + "/users/"
                            + id, UserResponse.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("User not found");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error inesperado");
            return false;
        }
    }

}
