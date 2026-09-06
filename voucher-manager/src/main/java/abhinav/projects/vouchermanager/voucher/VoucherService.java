package abhinav.projects.vouchermanager.voucher;

import abhinav.projects.vouchermanager.auth.CurrentPrincipal;
import abhinav.projects.vouchermanager.error.ConflictException;
import abhinav.projects.vouchermanager.error.NotFoundException;
import abhinav.projects.vouchermanager.error.UnprocessableEntityException;
import abhinav.projects.vouchermanager.user.User;
import abhinav.projects.vouchermanager.user.UserRepository;
import abhinav.projects.vouchermanager.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUserMappingRepository mappingRepository;
    private final VoucherHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final CurrentPrincipal currentPrincipal;

    VoucherService(VoucherRepository voucherRepository,
                   VoucherUserMappingRepository mappingRepository,
                   VoucherHistoryRepository historyRepository,
                   UserRepository userRepository,
                   CurrentPrincipal currentPrincipal) {
        this.voucherRepository = voucherRepository;
        this.mappingRepository = mappingRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.currentPrincipal = currentPrincipal;
    }

    @Transactional
    Voucher create(VoucherRequest request) {
        UUID orgId = currentPrincipal.resolveOrgId(request.orgId());
        VoucherStatus status = request.status() != null ? request.status() : VoucherStatus.DRAFT;
        VoucherScope scope = request.scope() != null ? request.scope() : VoucherScope.FREE;
        Map<String, Object> attributes = request.attributes() != null ? request.attributes() : Map.of();
        Voucher voucher = new Voucher(null, orgId, request.code(), request.title(), status, scope,
                request.startsAt(), request.expiresAt(), request.maxRedemptions(), attributes);
        Voucher saved = voucherRepository.save(voucher);
        recordHistory(saved.id(), null, VoucherHistoryEventType.CREATED, Map.of(
                "code", saved.code(),
                "status", saved.status().name()
        ));

        if (saved.scope() == VoucherScope.USER_SPECIFIC && request.assignedUserIds() != null) {
            for (UUID userId : request.assignedUserIds()) {
                assign(saved.id(), userId);
            }
        }

        return saved;
    }

    Voucher get(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found: " + id));
        currentPrincipal.requireAccess(voucher.orgId());
        return voucher;
    }

    List<Voucher> list(UUID orgId) {
        UUID scopedOrgId = currentPrincipal.resolveOrgIdOrNull(orgId);
        return scopedOrgId != null ? voucherRepository.findByOrgId(scopedOrgId) : voucherRepository.findAll();
    }

    @Transactional
    VoucherUserMapping redeem(String code, UUID userId) {
        Voucher voucher = voucherRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new NotFoundException("Voucher not found: " + code));

        if (voucher.status() != VoucherStatus.ACTIVE) {
            throw new ConflictException("Voucher is not active: " + voucher.status());
        }

        Instant now = Instant.now();
        if (voucher.startsAt() != null && now.isBefore(voucher.startsAt())) {
            throw new ConflictException("Voucher redemption window has not started yet");
        }
        if (voucher.expiresAt() != null && now.isAfter(voucher.expiresAt())) {
            throw new ConflictException("Voucher has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (user.status() != UserStatus.ACTIVE) {
            throw new UnprocessableEntityException("User is not active: " + user.status());
        }

        VoucherUserMapping existing = mappingRepository.findByVoucherIdAndUserId(voucher.id(), userId)
                .orElse(null);

        if (voucher.scope() == VoucherScope.USER_SPECIFIC
                && (existing == null || existing.status() == MappingStatus.REVOKED)) {
            throw new ConflictException("Voucher is not assigned to this user");
        }
        if (existing != null && existing.status() == MappingStatus.REDEEMED) {
            throw new ConflictException("Voucher already redeemed by this user");
        }

        if (voucher.maxRedemptions() != null) {
            long redeemedCount = mappingRepository.countByVoucherIdAndStatus(voucher.id(), MappingStatus.REDEEMED);
            if (redeemedCount >= voucher.maxRedemptions()) {
                throw new ConflictException("Voucher redemptions exhausted");
            }
        }

        VoucherUserMapping mapping = new VoucherUserMapping(
                existing != null ? existing.id() : null,
                voucher.id(),
                userId,
                MappingStatus.REDEEMED,
                existing != null ? existing.assignedAt() : now,
                now
        );
        VoucherUserMapping savedMapping = mappingRepository.save(mapping);

        recordHistory(voucher.id(), userId, VoucherHistoryEventType.REDEEMED, Map.of(
                "mappingId", savedMapping.id().toString()
        ));

        if (voucher.maxRedemptions() != null) {
            long redeemedCount = mappingRepository.countByVoucherIdAndStatus(voucher.id(), MappingStatus.REDEEMED);
            if (redeemedCount >= voucher.maxRedemptions()) {
                Voucher exhausted = new Voucher(voucher.id(), voucher.orgId(), voucher.code(), voucher.title(),
                        VoucherStatus.EXHAUSTED, voucher.scope(), voucher.startsAt(), voucher.expiresAt(),
                        voucher.maxRedemptions(), voucher.attributes());
                voucherRepository.save(exhausted);
                recordHistory(voucher.id(), null, VoucherHistoryEventType.EXHAUSTED, Map.of(
                        "maxRedemptions", voucher.maxRedemptions()
                ));
            }
        }

        return savedMapping;
    }

    @Transactional
    Voucher forceExpire(UUID voucherId, String reason) {
        Voucher voucher = get(voucherId);

        if (voucher.status() == VoucherStatus.EXPIRED) {
            return voucher;
        }

        Voucher expired = new Voucher(voucher.id(), voucher.orgId(), voucher.code(), voucher.title(),
                VoucherStatus.EXPIRED, voucher.scope(), voucher.startsAt(), voucher.expiresAt(),
                voucher.maxRedemptions(), voucher.attributes());
        Voucher saved = voucherRepository.save(expired);

        recordHistory(voucher.id(), null, VoucherHistoryEventType.EXPIRED, Map.of(
                "reason", reason != null ? reason : "forced"
        ));

        List<VoucherUserMapping> assigned = mappingRepository.findByVoucherIdAndStatus(voucher.id(), MappingStatus.ASSIGNED);
        for (VoucherUserMapping mapping : assigned) {
            VoucherUserMapping expiredMapping = new VoucherUserMapping(mapping.id(), mapping.voucherId(),
                    mapping.userId(), MappingStatus.EXPIRED, mapping.assignedAt(), mapping.redeemedAt());
            mappingRepository.save(expiredMapping);
            recordHistory(voucher.id(), mapping.userId(), VoucherHistoryEventType.EXPIRED, Map.of(
                    "mappingId", mapping.id().toString(),
                    "reason", "voucher_force_expired"
            ));
        }

        return saved;
    }

    @Transactional
    VoucherUserMapping assign(UUID voucherId, UUID userId) {
        Voucher voucher = get(voucherId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        if (!user.orgId().equals(voucher.orgId())) {
            throw new UnprocessableEntityException("User does not belong to the voucher's organization");
        }

        VoucherUserMapping existing = mappingRepository.findByVoucherIdAndUserId(voucherId, userId).orElse(null);
        if (existing != null && existing.status() == MappingStatus.REDEEMED) {
            throw new ConflictException("Voucher already redeemed by this user");
        }

        Instant now = Instant.now();
        VoucherUserMapping mapping = new VoucherUserMapping(
                existing != null ? existing.id() : null,
                voucherId,
                userId,
                MappingStatus.ASSIGNED,
                existing != null ? existing.assignedAt() : now,
                existing != null ? existing.redeemedAt() : null
        );
        VoucherUserMapping saved = mappingRepository.save(mapping);
        recordHistory(voucherId, userId, VoucherHistoryEventType.ASSIGNED, Map.of());
        return saved;
    }

    @Transactional
    VoucherUserMapping revoke(UUID voucherId, UUID userId) {
        Voucher voucher = get(voucherId);

        VoucherUserMapping existing = mappingRepository.findByVoucherIdAndUserId(voucherId, userId)
                .orElseThrow(() -> new NotFoundException("No assignment found for this user"));
        if (existing.status() == MappingStatus.REDEEMED) {
            throw new ConflictException("Cannot revoke a mapping that has already been redeemed");
        }

        VoucherUserMapping revoked = new VoucherUserMapping(existing.id(), voucher.id(), userId,
                MappingStatus.REVOKED, existing.assignedAt(), existing.redeemedAt());
        VoucherUserMapping saved = mappingRepository.save(revoked);
        recordHistory(voucher.id(), userId, VoucherHistoryEventType.REVOKED, Map.of());
        return saved;
    }

    List<VoucherUserMapping> listAssignments(UUID voucherId) {
        get(voucherId);
        return mappingRepository.findByVoucherId(voucherId);
    }

    private void recordHistory(UUID voucherId, UUID userId, VoucherHistoryEventType eventType, Map<String, Object> eventData) {
        historyRepository.save(new VoucherHistory(null, voucherId, userId, eventType, eventData, Instant.now()));
    }
}
