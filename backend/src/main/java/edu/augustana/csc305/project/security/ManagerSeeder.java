package edu.augustana.csc305.project.security;

import edu.augustana.csc305.project.model.User;
import edu.augustana.csc305.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ManagerSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String managerName;
    private final String managerEmail;
    private final String managerPassword;

    public ManagerSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${project.manager.name:Manager}") String managerName,
            @Value("${project.manager.email:}") String managerEmail,
            @Value("${project.manager.password:}") String managerPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.managerName = managerName;
        this.managerEmail = managerEmail;
        this.managerPassword = managerPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (managerEmail.isBlank() || managerPassword.isBlank()) {
            return;
        }

        String email = managerEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        String[] nameParts = managerName.trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        User manager = new User(
                null,
                firstName,
                lastName,
                email,
                passwordEncoder.encode(managerPassword),
                List.of("MANAGER")
        );

        userRepository.storeUser(manager);
    }
}