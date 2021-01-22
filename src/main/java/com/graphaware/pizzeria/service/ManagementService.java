package com.graphaware.pizzeria.service;

import com.graphaware.pizzeria.model.PizzeriaUser;
import com.graphaware.pizzeria.model.UserRoleEnum;
import com.graphaware.pizzeria.repository.PizzeriaUserRepository;
import com.graphaware.pizzeria.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagementService {

    private final PurchaseRepository purchaseRepository;
    private final PizzeriaUserRepository pizzeriaUserRepository;

    public long getPurchasesCount() {
        PizzeriaUser currentUser = getCurrentUser();
        if (currentUser.getRoles().stream().noneMatch(userRole -> userRole == UserRoleEnum.OWNER)) {
            throw new AccessDeniedException("Access not allowed");
        }
        return purchaseRepository.count();
    }

    private PizzeriaUser getCurrentUser() {
        //todo change current user
        return pizzeriaUserRepository.findById(66L).orElseThrow(IllegalArgumentException::new);
    }
}
