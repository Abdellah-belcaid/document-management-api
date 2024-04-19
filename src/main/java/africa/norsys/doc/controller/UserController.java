package africa.norsys.doc.controller;

import africa.norsys.doc.dto.UserDTO;
import africa.norsys.doc.entity.User;
import africa.norsys.doc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        try {
            User addedUser = userService.addUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();

    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@RequestBody User userLoginDTO) {
        try {
            UserDTO user = userService.login(userLoginDTO);
            return ResponseEntity.ok(user);
        } catch (DisabledException e) {

            String errorMessage = "Account is disabled. Please contact support for assistance.";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
        } catch (Exception e) {

            String errorMessage = "An error occurred during login ";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage + " : " + e.getMessage());

        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.register(user));
        } catch (Exception e) {
            String errorMessage = "An error occurred while registering.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage + " : " + e.getMessage());
        }
    }


    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        try {
            UserDTO userDTO = userService.getUserById(userId);
            if (userDTO != null) {
                return ResponseEntity.ok(userDTO);
            } else {

                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {

            String errorMessage = "An error occurred while retrieving the user.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
