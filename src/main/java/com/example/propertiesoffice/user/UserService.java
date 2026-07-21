package com.example.propertiesoffice.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponse createUser(String mail) {
        var entity = new UserEntity(null, mail);
        var saved = repository.save(entity);
        return new UserResponse(saved.getId(), saved.getMail());
    }

    public List<User> getAllUsers() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }


    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getMail()
        );
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("User not found, id = " + id);
        }
        repository.deleteById(id);
    }
}