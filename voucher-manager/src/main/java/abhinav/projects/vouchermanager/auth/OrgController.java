package abhinav.projects.vouchermanager.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/orgs")
class OrgController {

    private final OrgService orgService;
    private final CurrentPrincipal currentPrincipal;

    OrgController(OrgService orgService, CurrentPrincipal currentPrincipal) {
        this.orgService = orgService;
        this.currentPrincipal = currentPrincipal;
    }

    @PostMapping
    ResponseEntity<OrgSummary> create(@Valid @RequestBody CreateOrgRequest request) {
        currentPrincipal.requireAdmin();
        OrgSummary org = orgService.create(request);
        return ResponseEntity.created(URI.create("/orgs/" + org.id())).body(org);
    }

    @GetMapping
    List<OrgSummary> list() {
        currentPrincipal.requireAdmin();
        return orgService.list();
    }
}
