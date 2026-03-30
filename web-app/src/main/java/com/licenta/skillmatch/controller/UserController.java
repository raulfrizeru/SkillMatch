package com.licenta.skillmatch.controller;

import com.licenta.skillmatch.dto.RegisterDto;
import com.licenta.skillmatch.dto.UserEditDto;
import com.licenta.skillmatch.dto.UserListDto;
import com.licenta.skillmatch.entity.User;
import com.licenta.skillmatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
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
        return "user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String saveEditedUser(@PathVariable("id") Long id, @ModelAttribute("user") UserEditDto userDto) {
        userService.updateUser(id, userDto);
        return "redirect:/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id){
        userService.deleteUser(id);
        return "redirect:/users";
    }
}
