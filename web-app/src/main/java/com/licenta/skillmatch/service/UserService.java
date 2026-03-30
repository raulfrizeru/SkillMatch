package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.RegisterDto;
import com.licenta.skillmatch.dto.UserEditDto;
import com.licenta.skillmatch.dto.UserListDto;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<UserListDto> findAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            UserListDto dto = new UserListDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            if (user.getUserGroup() != null) {
                dto.setRole(user.getUserGroup().getGroupName());
            } else {
                dto.setRole("NOT ASSIGNED");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public UserEditDto getUserForEdit(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        UserEditDto dto = new UserEditDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        return dto;
    }

    public void updateUser(Long id, UserEditDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());
        userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void registerNewUser(RegisterDto registerDto) {
        // Aici pe viitor vei pune un IF: dacă e Candidat, creezi Candidate.
        // Dacă e Angajator, creezi Employer. Deocamdată facem un User de bază.
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(registerDto.getPassword()); // Atenție, pe viitor o vom cripta!

        userRepository.save(user);
    }

}
