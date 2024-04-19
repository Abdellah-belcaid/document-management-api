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
            // Customize the response for disabled accounts
            String errorMessage = "Account is disabled. Please contact support for assistance.";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
        } catch (Exception e) {
            // Return INTERNAL SERVER ERROR (500) with a generic error message
            String errorMessage = "An error occurred during login ";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage + " : " + e.getMessage());

        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.register(user)); // Return OK (200) with the registered user object
        } catch (Exception e) {
            String errorMessage = "An error occurred while registering.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage + " : " + e.getMessage());
        }
    }
}
