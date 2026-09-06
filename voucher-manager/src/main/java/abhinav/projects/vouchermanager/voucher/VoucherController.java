package abhinav.projects.vouchermanager.voucher;

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
@RequestMapping("/vouchers")
class VoucherController {

    private final VoucherService voucherService;

    VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping
    ResponseEntity<Voucher> create(@Valid @RequestBody VoucherRequest request) {
        Voucher voucher = voucherService.create(request);
        return ResponseEntity.created(URI.create("/vouchers/" + voucher.id())).body(voucher);
    }

    @GetMapping("/{id}")
    Voucher get(@PathVariable UUID id) {
        return voucherService.get(id);
    }

    @GetMapping
    List<Voucher> list(@RequestParam(required = false) UUID orgId) {
        return voucherService.list(orgId);
    }

    @PostMapping("/{code}/redeem")
    VoucherUserMapping redeem(@PathVariable String code, @Valid @RequestBody RedeemRequest request) {
        return voucherService.redeem(code, request.userId());
    }

    @PostMapping("/{id}/force-expire")
    Voucher forceExpire(@PathVariable UUID id, @Valid @RequestBody(required = false) ForceExpireRequest request) {
        String reason = request != null ? request.reason() : null;
        return voucherService.forceExpire(id, reason);
    }

    @PostMapping("/{id}/assign")
    VoucherUserMapping assign(@PathVariable UUID id, @Valid @RequestBody RedeemRequest request) {
        return voucherService.assign(id, request.userId());
    }

    @PostMapping("/{id}/revoke")
    VoucherUserMapping revoke(@PathVariable UUID id, @Valid @RequestBody RedeemRequest request) {
        return voucherService.revoke(id, request.userId());
    }

    @GetMapping("/{id}/assignments")
    List<VoucherUserMapping> assignments(@PathVariable UUID id) {
        return voucherService.listAssignments(id);
    }
}
