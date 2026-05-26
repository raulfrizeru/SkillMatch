package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.dto.*;
import com.licenta.skillmatch.entity.Candidate;
import com.licenta.skillmatch.entity.Employer;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.repository.UserRepository;
import com.licenta.skillmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public String viewUsersPage(Model model){
        List<UserListDto> listUsers = userService.findAllUsers();

        model.addAttribute("users", listUsers);
        return "users";
    }

    @GetMapping("/users/new")
    public String showNewUserForm(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute("user", registerDto);
        return "add-user";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("user") RegisterDto registerDto) {
        userService.registerNewUser(registerDto);
        return "redirect:/users";
    }


    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        UserEditDto userDto = userService.getUserForEdit(id);
        model.addAttribute("user", userDto);
        return "edit-user";
    }


    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id){
        userService.deleteUser(id);
        return "redirect:/users";
    }

    @PostMapping("/profile/candidate/update")
    public String updateCandidateProfile(@ModelAttribute CandidateProfileDto profileDto,
                                         @RequestParam(value = "cvFile", required = false) MultipartFile cvFile,
                                         RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String oldUsername = authentication.getName();
            User user = userRepository.findByUsername(oldUsername);
            Candidate candidate = (Candidate) user;

            userService.updateCandidateProfile(candidate.getId(), profileDto);

            // Handle CV file if uploaded
            if (cvFile != null && !cvFile.isEmpty()) {
                String oldCvPath = candidate.getCvFilePath();
                String cvFilePath = uploadCvFile(candidate.getId(), cvFile, oldCvPath);
                userService.updateCandidateCvPath(candidate.getId(), cvFilePath);
            }

            // If username changed, update the security context
            if (!oldUsername.equals(profileDto.getUsername())) {
                User updatedUser = userRepository.findByUsername(profileDto.getUsername());
                if (updatedUser != null) {
                    UsernamePasswordAuthenticationToken newAuth =
                            new UsernamePasswordAuthenticationToken(updatedUser.getUsername(),
                                    updatedUser.getPassword(),
                                    authentication.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/profile/employer/update")
    public String updateEmployerProfile(@ModelAttribute EmployerProfileDto profileDto,
                                        RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String oldUsername = authentication.getName();
            User user = userRepository.findByUsername(oldUsername);
            Employer employer = (Employer) user;

            // update profile data
            userService.updateEmployerProfile(employer.getId(), profileDto);

            // uf username changed, update the security context
            if (!oldUsername.equals(profileDto.getUsername())) {
                User updatedUser = userRepository.findByUsername(profileDto.getUsername());
                if (updatedUser != null) {
                    UsernamePasswordAuthenticationToken newAuth =
                            new UsernamePasswordAuthenticationToken(updatedUser.getUsername(),
                                    updatedUser.getPassword(),
                                    authentication.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    private String uploadCvFile(Long candidateId, MultipartFile file, String oldCvPath) throws Exception {
        File uploadsDir = new File(uploadDir + File.separator + "cv");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        if (oldCvPath != null && !oldCvPath.isEmpty()) {
            try {
                File oldFile = new File(oldCvPath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            } catch (Exception e) {
                System.err.println("Could not delete old CV file: " + e.getMessage());
            }
        }
        String fileName = "user_" + candidateId + "_cv_" + UUID.randomUUID() + ".pdf";
        Path filePath = Paths.get(uploadsDir.getAbsolutePath(), fileName);

        Files.write(filePath, file.getBytes());

        return "uploads/cv/" + fileName;
    }

}
