package abhinav.projects.vouchermanager.auth;

import abhinav.projects.vouchermanager.error.ForbiddenException;
import abhinav.projects.vouchermanager.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class CurrentPrincipal {

    private final AuthenticatedUser user;

    CurrentPrincipal(HttpServletRequest request) {
        this.user = (AuthenticatedUser) request.getAttribute(AuthenticationFilter.ATTRIBUTE);
    }

    public boolean isAdmin() {
        return user != null && user.role() == Role.ADMIN;
    }

    public void requireAdmin() {
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
        if (user.role() != Role.ADMIN) {
            throw new ForbiddenException("Admin access required");
        }
    }

    /** The org id this request is scoped to: the caller's own org, or an explicit org id supplied by an admin. */
    public UUID resolveOrgId(UUID requestedOrgId) {
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
        if (user.role() == Role.ORG) {
            return user.orgId();
        }
        if (requestedOrgId == null) {
            throw new ForbiddenException("orgId is required for admin requests");
        }
        return requestedOrgId;
    }

    /** Like resolveOrgId, but admins may omit orgId to mean "no filter, all orgs". */
    public UUID resolveOrgIdOrNull(UUID requestedOrgId) {
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return user.role() == Role.ORG ? user.orgId() : requestedOrgId;
    }

    /** Rejects access unless the caller is an admin or owns the given org's resource. */
    public void requireAccess(UUID resourceOrgId) {
        if (isAdmin()) {
            return;
        }
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
        if (!resourceOrgId.equals(user.orgId())) {
            throw new ForbiddenException("Not authorized for this organization's resource");
        }
    }
}
