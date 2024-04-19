package africa.norsys.doc.service;

import africa.norsys.doc.entity.User;

import java.util.List;

public interface UserService {
    public User addUser(User user);
    public List<User> getAllUsers();

}
