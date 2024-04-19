package africa.norsys.doc.service;

import africa.norsys.doc.dto.UserDTO;
import africa.norsys.doc.entity.User;

public interface UserService {
    public User addUser(User user);

    UserDTO login(User userLogin) throws Exception;

    UserDTO register(User user);
}
