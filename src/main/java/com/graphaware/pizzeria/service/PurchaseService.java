package com.graphaware.pizzeria.service;

import com.graphaware.pizzeria.model.Pizza;
import com.graphaware.pizzeria.model.PizzeriaUser;
import com.graphaware.pizzeria.model.Purchase;
import com.graphaware.pizzeria.model.PurchaseStateEnum;
import com.graphaware.pizzeria.repository.PurchaseRepository;
import com.graphaware.pizzeria.security.PizzeriaUserPrincipal;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final AtomicReference<Map<Long, Purchase>> ongoingPurchases = new AtomicReference<>(new HashMap<>());

    private final EmailService emailService;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    @PreAuthorize("hasAuthority('ADD_PIZZA')")
    public Purchase addPizzaToPurchase(final Pizza pizza) {
        PizzeriaUser currentUser = getCurrentUser();

        List<Purchase> purchases
                = purchaseRepository.findAllByStateEqualsAndCustomer_Id(PurchaseStateEnum.DRAFT, currentUser.getId());
        if (purchases.size() > 1) {
            throw new PizzeriaException();
        }
        Purchase purchase;
        if (purchases.isEmpty()) {
            purchase = new Purchase();
            purchase.setCustomer(currentUser);
            purchase.setState(PurchaseStateEnum.DRAFT);
        } else {
            purchase = purchases.get(0);
        }
        if (purchase.getPizzas() == null) {
            purchase.setPizzas(new LinkedList<>());
        }
        purchase.getPizzas().add(pizza);
        return purchaseRepository.save(purchase);
    }

    @PreAuthorize("hasAuthority('CONFIRM_PURCHASE')")
    public void confirmPurchase() {
        PizzeriaUser currentUser = getCurrentUser();
        List<Purchase> purchases
                = purchaseRepository.findAllByStateEqualsAndCustomer_Id(PurchaseStateEnum.DRAFT, currentUser.getId());
        if (purchases.size() != 1) {
            throw new PizzeriaException();
        }
        Purchase purchase = purchases.get(0);
        purchase.setState(PurchaseStateEnum.PLACED);
        purchaseRepository.save(purchase);
    }

    @PreAuthorize("hasAuthority('PICK_PURCHASE')")
    public synchronized Purchase pickPurchase() {
        PizzeriaUser currentUser = getCurrentUser();
        Purchase purchase = purchaseRepository.findFirstByStateEquals(PurchaseStateEnum.PLACED);
        purchase.setWorker(currentUser);
        purchase.setState(PurchaseStateEnum.ONGOING);
        //can work only on a single order!
        if (ongoingPurchases.get().containsKey(currentUser.getId())) {
            throw new PizzeriaException();
        }
        ongoingPurchases.get().put(currentUser.getId(), purchase);
        return purchaseRepository.save(purchase);
    }

    @PreAuthorize("hasRole('PIZZA_MAKER')")
    public synchronized void completePurchase(final long id) {
        PizzeriaUser currentUser = getCurrentUser();

        Purchase purchase = purchaseRepository.findById(id).orElseThrow(PizzeriaException::new);

        if (!purchase.getState().equals(PurchaseStateEnum.ONGOING)) {
            throw new PizzeriaException();
        }
        Purchase ongoingUserPurchase = ongoingPurchases.get().get(currentUser.getId());
        if (ongoingUserPurchase == null || ongoingUserPurchase.getId() != purchase.getId()) {
            throw new PizzeriaException();
        }
        purchase.setCheckoutDate(new Date());
        purchase.setState(PurchaseStateEnum.SERVED);
        purchase.setAmount(computeAmount(purchase.getPizzas()));
        purchaseRepository.save(purchase);
        ongoingPurchases.get().remove(currentUser.getId());

        emailService.sendConfirmationEmail(currentUser);
    }

    @PreAuthorize("hasRole('PIZZA_MAKER')")
    public Purchase getCurrentPurchase() {
        return ongoingPurchases.get().get(getCurrentUser().getId());
    }

    private Double computeAmount(final List<Pizza> pizzas) {
        if (pizzas == null) {
            return 0.0;
        }
        int numberPizzas = pizzas.size();
        // buy a pineapple pizza, get 10% off the others
        boolean applyPineappleDiscount = pizzas.stream()
                .filter(pizza -> pizza.getToppings() != null)
                .map(Pizza::getToppings)
                .flatMap(Collection::stream)
                .anyMatch(topping -> topping != null && topping.contains("pineapple"));
        return pizzas.stream()
                .sorted(Comparator.comparing(Pizza::getPrice).reversed())
                .limit(numberPizzas - numberPizzas / 3)
                .mapToDouble(pizza -> pizza.getPrice()
                        * (pizza.getToppings().contains("pineapple") || !applyPineappleDiscount ? 1.0 : 0.9))
                .sum();
    }

    private PizzeriaUser getCurrentUser() {
        return ((PizzeriaUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }
}
