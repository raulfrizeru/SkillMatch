package com.licenta.skillmatch.service;

import com.licenta.skillmatch.dto.RegisterDto;
import com.licenta.skillmatch.dto.UserEditDto;
import com.licenta.skillmatch.dto.UserListDto;
import com.licenta.skillmatch.dto.CandidateProfileDto;
import com.licenta.skillmatch.dto.EmployerProfileDto;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.Employer;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.entity.UserGroup;
import com.licenta.skillmatch.repository.UserGroupRepository;
import com.licenta.skillmatch.repository.UserRepository;
import com.licenta.skillmatch.repository.CandidateRepository;
import com.licenta.skillmatch.repository.EmployerRepository;
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
    private CandidateRepository candidateRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CandidateProfileDto getCandidateProfileByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user instanceof Candidate) {
            Candidate c = (Candidate) user;
            return CandidateProfileDto.builder()
                    .id(c.getId())
                    .username(c.getUsername())
                    .email(c.getEmail())
                    .firstName(c.getFirstName())
                    .lastName(c.getLastName())
                    .cvFilePath(c.getCvFilePath())
                    .build();
        }
        return null;
    }

    public EmployerProfileDto getEmployerProfileByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user instanceof Employer) {
            Employer e = (Employer) user;
            return EmployerProfileDto.builder()
                    .id(e.getId())
                    .username(e.getUsername())
                    .email(e.getEmail())
                    .companyName(e.getCompanyName())
                    .description(e.getDescription())
                    .build();
        }
        return null;
    }

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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Profile methods for Candidate
    public CandidateProfileDto getCandidateProfile(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId));

        return CandidateProfileDto.builder()
                .id(candidate.getId())
                .username(candidate.getUsername())
                .email(candidate.getEmail())
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .cvFilePath(candidate.getCvFilePath())
                .build();
    }

    public void updateCandidateProfile(Long candidateId, CandidateProfileDto dto) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId));

        candidate.setUsername(dto.getUsername());
        candidate.setEmail(dto.getEmail());
        candidate.setFirstName(dto.getFirstName());
        candidate.setLastName(dto.getLastName());

        candidateRepository.save(candidate);
    }

    public void updateCandidateCvPath(Long candidateId, String cvFilePath) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with id: " + candidateId));

        candidate.setCvFilePath(cvFilePath);
        candidateRepository.save(candidate);
    }

    public EmployerProfileDto getEmployerProfile(Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with id: " + employerId));

        return EmployerProfileDto.builder()
                .id(employer.getId())
                .username(employer.getUsername())
                .email(employer.getEmail())
                .companyName(employer.getCompanyName())
                .description(employer.getDescription())
                .build();
    }

    public void updateEmployerProfile(Long employerId, EmployerProfileDto dto) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found with id: " + employerId));

        employer.setUsername(dto.getUsername());
        employer.setEmail(dto.getEmail());
        employer.setCompanyName(dto.getCompanyName());
        employer.setDescription(dto.getDescription());

        employerRepository.save(employer);
    }
}
