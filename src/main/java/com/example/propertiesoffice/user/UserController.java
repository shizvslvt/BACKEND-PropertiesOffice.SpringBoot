package com.example.propertiesoffice.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        var response = userService.createUser(request.mail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        var response = userService.getAllUsers().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    public void DeleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }


    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.id(),
                user.mail()
        );
    }
}