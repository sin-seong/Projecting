package com.sin.sb1101.controller;

import com.sin.sb1101.dto.Sign;
import com.sin.sb1101.repository.SignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpSession;
import java.util.List;

@Controller

public class UserController {

    @Autowired
    private SignRepository signRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/userProfile")
     public String userProfile(Model model, HttpSession session) {
        Sign sign = (Sign) session.getAttribute("user");
        if(sign != null && sign.getRole().equals("admin")) {
            List<Sign> signs = signRepository.findAll();
            model.addAttribute("signs", signs);
            return "view/userProfile";
        }
        return "redirect:/login";
    }
    @RequestMapping(value = "/deleteUser/{id}", method = RequestMethod.GET)
    public String deleteUser(Model model, @PathVariable Long id) {
        signRepository.deleteById(id);
        return "redirect:/userProfile";
    }
    @GetMapping("editUser/{id}")
    public String editUser(Model model, @PathVariable Long id) {
        Sign signed = signRepository.findById(id).orElse(null);
        if(signed != null){
            model.addAttribute("sign", signed);
            return "view/editUser";
        }
        return "redirect:/userProfile";
    }
    @PostMapping("/updateUser/{id}")
    public String updateUser(Model model, @PathVariable Long id, Sign updatedUser){
        Sign existingUser = signRepository.findById(id).orElse(null);
        if(existingUser != null){
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setDepartment(updatedUser.getDepartment());
            existingUser.setLevel(updatedUser.getLevel());
            existingUser.setRole(updatedUser.getRole());

            if(updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()){
                String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
                existingUser.setPassword(encodedPassword);
            }
            signRepository.save(existingUser);
        }
        return "redirect:/userProfile";
    }
    public boolean checkPassword(String inputPassword, String storedPassword) {
        return passwordEncoder.matches(inputPassword, storedPassword);
    }
    @GetMapping("/profillChange")
    public String profilChange(Model model, HttpSession session) {
        Sign sign = (Sign) session.getAttribute("user");
        if(sign != null && sign.getRole().equals("admin")) {
            List<Sign> signs = signRepository.findAll();
            model.addAttribute("signs", signs);
            return "view/profilChange";
        }
    return "redirect:/view/userInfo";
}
    }
