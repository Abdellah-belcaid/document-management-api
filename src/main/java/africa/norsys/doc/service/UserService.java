package africa.norsys.doc.service;

import africa.norsys.doc.dto.UserDTO;
import africa.norsys.doc.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    public User addUser(User user);

    public List<User> getAllUsers();

    UserDTO login(User userLogin) throws Exception;

    UserDTO register(User user);

    UserDTO getUserById(UUID userId);
}
