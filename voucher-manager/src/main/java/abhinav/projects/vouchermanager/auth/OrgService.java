package abhinav.projects.vouchermanager.auth;

import abhinav.projects.vouchermanager.error.ConflictException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class OrgService {

    private final OrgRepository orgRepository;
    private final PasswordEncoder passwordEncoder;

    OrgService(OrgRepository orgRepository, PasswordEncoder passwordEncoder) {
        this.orgRepository = orgRepository;
        this.passwordEncoder = passwordEncoder;
    }

    OrgSummary create(CreateOrgRequest request) {
        Org org = new Org(null, request.name(), request.email(), passwordEncoder.encode(request.password()));
        try {
            Org saved = orgRepository.save(org);
            return new OrgSummary(saved.id(), saved.name(), saved.email());
        } catch (DuplicateKeyException e) {
            throw new ConflictException("Org already exists with email: " + request.email());
        }
    }

    List<OrgSummary> list() {
        return orgRepository.findAll().stream()
                .map(org -> new OrgSummary(org.id(), org.name(), org.email()))
                .toList();
    }
}
