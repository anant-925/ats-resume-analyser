package com.anant.ats.resumeanalyser.controller;

import com.anant.ats.resumeanalyser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    /**
     * Shows the registration page.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // This is a placeholder for passing data *to* the form if needed
        model.addAttribute("user", new Object()); 
        return "register"; // This maps to 'register.html'
    }

    /**
     * Handles the new user registration.
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               RedirectAttributes redirectAttributes) {
        
        try {
            userService.registerNewUser(username, password, email);
            
            // On success, add a success message and redirect to the login page
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login";

        } catch (Exception e) {
            // On failure, add an error message and redirect back to the register page
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
@GetMapping("/login")
public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

    if (error != null) {
        model.addAttribute("errorMessage", "Invalid username or password.");
    }
    if (logout != null) {
        model.addAttribute("logoutMessage", "You have been logged out.");
    }

    // This tells Spring to render the 'login.html' template
    return "login";
}
}