package abhinav.projects.vouchermanager.auth;

import java.util.UUID;

record AuthenticatedUser(UUID subjectId, Role role) {

    UUID orgId() {
        return role == Role.ORG ? subjectId : null;
    }
}
