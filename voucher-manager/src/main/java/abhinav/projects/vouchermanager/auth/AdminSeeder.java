package abhinav.projects.vouchermanager.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * schema.sql drops and recreates all tables on every boot (spring.sql.init.mode=always),
 * so there is no persisted admin account to log in with. Seed a dev one at startup instead
 * of requiring an out-of-band SQL script with a hand-computed bcrypt hash.
 */
@Component
class AdminSeeder implements CommandLineRunner {

    static final String SEED_EMAIL = "admin@example.com";
    static final String SEED_PASSWORD = "admin1234";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    AdminSeeder(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.findByEmail(SEED_EMAIL).isEmpty()) {
            adminRepository.save(new Admin(null, SEED_EMAIL, passwordEncoder.encode(SEED_PASSWORD)));
        }
    }
}
