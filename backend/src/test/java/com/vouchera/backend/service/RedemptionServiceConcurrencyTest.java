package com.vouchera.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.vouchera.backend.dto.auth.CurrentUserInfo;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.entity.Redemption;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.entity.VoucherType;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.CampaignStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.repository.RedemptionRepository;
import com.vouchera.backend.repository.UserRepository;
import com.vouchera.backend.repository.VoucherTypeRepository;

@ExtendWith(MockitoExtension.class)
class RedemptionServiceConcurrencyTest {

    @Mock
    private RedemptionRepository redemptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoucherTypeRepository voucherTypeRepository;

    @InjectMocks
    private RedemptionService redemptionService;

    @Test
    void redeemCouponThrowsConflictWhenConcurrentDuplicateInsertDetected() {
        UUID userId = UUID.randomUUID();
        UUID voucherTypeId = UUID.randomUUID();
        CurrentUserInfo currentUser = new CurrentUserInfo(userId, "user@example.com", Role.CUSTOMER, AccountStatus.ACTIVE,
                null);

        User user = new User("user@example.com", "password123", Role.CUSTOMER, null);
        user.setId(userId);

        VoucherType voucherType = buildActiveVoucherType(voucherTypeId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherTypeRepository.findById(voucherTypeId)).thenReturn(Optional.of(voucherType));
        when(redemptionRepository.existsByUserIdAndVoucherTypeId(userId, voucherTypeId)).thenReturn(false);
        when(voucherTypeRepository.decrementQuotaIfAvailable(voucherTypeId)).thenReturn(1);
        when(redemptionRepository.save(any(Redemption.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate redemption"));

        assertThrows(IllegalStateException.class, () -> redemptionService.redeemCoupon(currentUser, voucherTypeId));

        verify(voucherTypeRepository).decrementQuotaIfAvailable(voucherTypeId);
        verify(redemptionRepository).save(any(Redemption.class));
    }

    @Test
    void redeemCouponDoesNotCreateRedemptionWhenQuotaIsExhausted() {
        UUID userId = UUID.randomUUID();
        UUID voucherTypeId = UUID.randomUUID();
        CurrentUserInfo currentUser = new CurrentUserInfo(userId, "user@example.com", Role.CUSTOMER, AccountStatus.ACTIVE,
                null);

        User user = new User("user@example.com", "password123", Role.CUSTOMER, null);
        user.setId(userId);

        VoucherType voucherType = buildActiveVoucherType(voucherTypeId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherTypeRepository.findById(voucherTypeId)).thenReturn(Optional.of(voucherType));
        when(redemptionRepository.existsByUserIdAndVoucherTypeId(userId, voucherTypeId)).thenReturn(false);
        when(voucherTypeRepository.decrementQuotaIfAvailable(voucherTypeId)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> redemptionService.redeemCoupon(currentUser, voucherTypeId));

        verify(redemptionRepository, never()).save(any(Redemption.class));
    }

    @Test
    void redeemCouponSucceedsWhenQuotaAvailableAndNoDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID voucherTypeId = UUID.randomUUID();
        CurrentUserInfo currentUser = new CurrentUserInfo(userId, "user@example.com", Role.CUSTOMER, AccountStatus.ACTIVE,
                null);

        User user = new User("user@example.com", "password123", Role.CUSTOMER, null);
        user.setId(userId);

        VoucherType voucherType = buildActiveVoucherType(voucherTypeId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherTypeRepository.findById(voucherTypeId)).thenReturn(Optional.of(voucherType));
        when(redemptionRepository.existsByUserIdAndVoucherTypeId(userId, voucherTypeId)).thenReturn(false);
        when(voucherTypeRepository.decrementQuotaIfAvailable(voucherTypeId)).thenReturn(1);
        when(redemptionRepository.save(any(Redemption.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> redemptionService.redeemCoupon(currentUser, voucherTypeId));
    }

    private VoucherType buildActiveVoucherType(UUID voucherTypeId) {
        Company company = new Company("Acme");
        Campaign campaign = new Campaign(
                "Campaign",
                "Description",
                company,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1),
                CampaignStatus.ACTIVE);
        VoucherType voucherType = new VoucherType(campaign, 10, 10);
        voucherType.setId(voucherTypeId);
        return voucherType;
    }
}
