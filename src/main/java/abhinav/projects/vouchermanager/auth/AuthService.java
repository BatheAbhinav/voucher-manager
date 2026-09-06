package abhinav.projects.vouchermanager.auth;

import abhinav.projects.vouchermanager.error.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class AuthService {

    private final AdminRepository adminRepository;
    private final OrgRepository orgRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    AuthService(AdminRepository adminRepository, OrgRepository orgRepository,
                PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.orgRepository = orgRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    LoginResponse loginAdmin(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), admin.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.issue(admin.id(), Role.ADMIN);
        return new LoginResponse(token, Role.ADMIN, admin.id(), admin.email());
    }

    LoginResponse loginOrg(LoginRequest request) {
        Org org = orgRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), org.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.issue(org.id(), Role.ORG);
        return new LoginResponse(token, Role.ORG, org.id(), org.name());
    }
}
