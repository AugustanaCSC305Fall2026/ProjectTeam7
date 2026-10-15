package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.dto.UserDto;
import edu.augustana.csc305.project.dto.RegisterRequest;
import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.UserRepository;
import edu.augustana.csc305.project.security.LoginRequest;
import edu.augustana.csc305.project.security.JwtService;
import edu.augustana.csc305.project.security.LoginResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

/**
 * The controller for handling authentication requests. Defines our /auth routes.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Login requests contain the email address and password of the user
     * logging in. This endpoint checks the hashed password against the
     * password stored for this user's email.
     *
     * Fails if no user exists with the given email or the password hashes
     * don't match. Otherwise, creates and returns a fresh JWT authentication
     * token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid email or password");
            }

            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new LoginResponse(token, jwtService.getExpirationSeconds(),
                    toUserDto(user)));
        } catch (BadCredentialsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_CREDENTIALS");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String firstName = clean(request.firstName());
        String lastName = clean(request.lastName());
        String email = request.email() == null ? "" : request.email().trim().toLowerCase(Locale.ROOT);
        String password = request.password();

        if (firstName.isBlank() || lastName.isBlank() || !email.contains("@")
                || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body("INVALID_REGISTRATION_DATA");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("EMAIL_ALREADY_REGISTERED");
        }

        User user = new User(null, firstName, lastName, email,
                passwordEncoder.encode(password), List.of("USER"));
        userRepository.storeUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserDto(user));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // JWT logout is client-side token disposal for the MVP.
    }

    private UserDto toUserDto(User user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        if (firstName == null && lastName == null) {
            String[] nameParts = user.getName() == null ? new String[]{"", ""}
                    : user.getName().trim().split("\\s+", 2);
            firstName = nameParts.length > 0 ? nameParts[0] : "";
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }
        return new UserDto(user.getId().toHexString(), firstName, lastName, user.getRoles());
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
