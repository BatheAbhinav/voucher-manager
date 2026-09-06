package abhinav.projects.vouchermanager.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin/login")
    LoginResponse loginAdmin(@Valid @RequestBody LoginRequest request) {
        return authService.loginAdmin(request);
    }

    @PostMapping("/org/login")
    LoginResponse loginOrg(@Valid @RequestBody LoginRequest request) {
        return authService.loginOrg(request);
    }
}
