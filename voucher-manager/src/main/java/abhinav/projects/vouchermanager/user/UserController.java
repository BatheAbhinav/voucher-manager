package abhinav.projects.vouchermanager.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    ResponseEntity<User> create(@Valid @RequestBody UserRequest request) {
        User user = userService.create(request);
        return ResponseEntity.created(URI.create("/users/" + user.id())).body(user);
    }

    @GetMapping("/{id}")
    User get(@PathVariable UUID id) {
        return userService.get(id);
    }

    @GetMapping
    List<User> list(@RequestParam(required = false) UUID orgId) {
        return userService.list(orgId);
    }
}
