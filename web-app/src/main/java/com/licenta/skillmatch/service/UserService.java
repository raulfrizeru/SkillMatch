package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.RegisterDto;
import com.licenta.skillmatch.dto.UserEditDto;
import com.licenta.skillmatch.dto.UserListDto;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.Employer;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.entity.UserGroup;
import com.licenta.skillmatch.repository.UserGroupRepository;
import com.licenta.skillmatch.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match!");
        }

        User user;
        if ("CANDIDATE".equalsIgnoreCase(registerDto.getRole())) {
            Candidate candidate = new Candidate();
            candidate.setFirstName(registerDto.getFirstName());
            candidate.setLastName(registerDto.getLastName());
            user = candidate;
        } else if ("EMPLOYER".equalsIgnoreCase(registerDto.getRole())) {
            Employer employer = new Employer();
            employer.setCompanyName(registerDto.getCompanyName());
            employer.setDescription("");
            user = employer;
        } else {
            throw new IllegalArgumentException("Invalid role selected");
        }

        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        UserGroup group = userGroupRepository.findByGroupName(registerDto.getRole().toUpperCase());
        if (group == null) {
            group = new UserGroup();
            group.setGroupName(registerDto.getRole().toUpperCase());
            userGroupRepository.save(group);
        }
        user.setUserGroup(group);

        userRepository.save(user);
    }

}
