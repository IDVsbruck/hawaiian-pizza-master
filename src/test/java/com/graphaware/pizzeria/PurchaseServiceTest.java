package com.graphaware.pizzeria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphaware.pizzeria.model.Pizza;
import com.graphaware.pizzeria.model.PizzeriaUser;
import com.graphaware.pizzeria.model.Purchase;
import com.graphaware.pizzeria.model.PurchaseStateEnum;
import com.graphaware.pizzeria.repository.PurchaseRepository;
import com.graphaware.pizzeria.security.PizzeriaUserPrincipal;
import com.graphaware.pizzeria.service.EmailService;
import com.graphaware.pizzeria.service.PizzeriaException;
import com.graphaware.pizzeria.service.PurchaseService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTest {

    private final static long CURRENT_USER_INDEX = 666L;

    @Mock
    private EmailService emailService;

    @Mock
    private PurchaseRepository purchaseRepository;

    private PurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseService(emailService, purchaseRepository);
        PizzeriaUser currentUser = new PizzeriaUser();
        currentUser.setName("Papa");
        ReflectionTestUtils.setField(currentUser, "id", CURRENT_USER_INDEX);
        currentUser.setRoles(Collections.emptyList());
        currentUser.setEmail("abc@def.com");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        PizzeriaUserPrincipal userPrincipal = new PizzeriaUserPrincipal(currentUser);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        doNothing().when(emailService).sendConfirmationEmail(any(PizzeriaUser.class));
    }

    @Test
    void should_create_draft_purchase_if_non_existing() {
        purchaseService.addPizzaToPurchase(new Pizza());
        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(purchaseCaptor.capture());
        Purchase saved = purchaseCaptor.getValue();
        assertEquals(PurchaseStateEnum.DRAFT, saved.getState());
    }

    @Test
    void should_throw_exception_if_more_purchases() {
        when(purchaseRepository.findAllByStateEqualsAndCustomer_Id(any(), any()))
                .thenReturn(Arrays.asList(new Purchase(), new Purchase()));
        assertThrows(PizzeriaException.class, () -> purchaseService.addPizzaToPurchase(new Pizza()));
    }

    @Test
    void confirm_purchase_changes_state() {
        when(purchaseRepository.findAllByStateEqualsAndCustomer_Id(any(), any()))
                .thenReturn(Collections.singletonList(new Purchase()));
        purchaseService.confirmPurchase();
        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(purchaseCaptor.capture());
        Purchase saved = purchaseCaptor.getValue();
        assertThat(saved.getState()).isEqualByComparingTo(PurchaseStateEnum.PLACED);
    }

    @Test
    void should_add_items_to_purchase() {
        Pizza one = new Pizza();
        ReflectionTestUtils.setField(one, "id", 666L);
        Pizza two = new Pizza();
        ReflectionTestUtils.setField(two, "id", 43L);
        purchaseService.addPizzaToPurchase(one);
        purchaseService.addPizzaToPurchase(two);
        Purchase toReturn = new Purchase();
        toReturn.setPizzas(Arrays.asList(one, two));
        when(purchaseRepository.findAllByStateEqualsAndCustomer_Id(any(), any()))
                .thenReturn(Collections.singletonList(new Purchase()));
        when(purchaseRepository.findFirstByStateEquals(any())).thenReturn(new Purchase());
        when(purchaseRepository.save(any())).thenReturn(toReturn);

        purchaseService.confirmPurchase();

        Purchase latest = purchaseService.pickPurchase();
        assertThat(latest.getPizzas()).containsExactlyInAnyOrder(one, two);
    }

    @Test
    void confirm_pick_changes_state() {
        when(purchaseRepository.findFirstByStateEquals(any()))
                .thenReturn(new Purchase());

        purchaseService.pickPurchase();

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(purchaseCaptor.capture());
        Purchase saved = purchaseCaptor.getValue();
        assertThat(saved.getState()).isEqualByComparingTo(PurchaseStateEnum.ONGOING);
    }

    @Test
    void confirm_close_changes_state() {
        Purchase p = new Purchase();
        p.setState(PurchaseStateEnum.ONGOING);
        when(purchaseRepository.findFirstByStateEquals(any()))
                .thenReturn(p);
        when(purchaseRepository.findById(any()))
                .thenReturn(Optional.of(p));
        purchaseService.pickPurchase();

        purchaseService.completePurchase(p.getId());

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository, times(2)).save(purchaseCaptor.capture());
        Purchase saved = purchaseCaptor.getValue();
        assertThat(saved.getState()).isEqualByComparingTo(PurchaseStateEnum.SERVED);
    }

    @Test
    void givenNoPizzasInPurchase_whenComputePurchase_thenExpectedZeroPrice() {
        //arrange
        final long PURCHASE_INDEX = 1L;
        Purchase purchase = new Purchase();
        ReflectionTestUtils.setField(purchase, "id", PURCHASE_INDEX);
        when(purchaseRepository.findFirstByStateEquals(any(PurchaseStateEnum.class))).thenReturn(purchase);
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        //act
        purchaseService.pickPurchase();
        purchaseService.completePurchase(PURCHASE_INDEX);
        //assert
        assertEquals(0, purchase.getAmount());
    }

    @Test
    void givenOnePizzasInPurchase_whenComputePurchase_thenExpectedPizzaPrice() {
        //arrange
        final long PURCHASE_INDEX = 1L;
        final double PIZZA_PRICE = 10.0;
        Purchase purchase = new Purchase();
        ReflectionTestUtils.setField(purchase, "id", PURCHASE_INDEX);
        Pizza pizza = createPizza(48L, PIZZA_PRICE, new ArrayList<>());
        purchase.setPizzas(Arrays.asList(pizza));
        when(purchaseRepository.findFirstByStateEquals(any(PurchaseStateEnum.class))).thenReturn(purchase);
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        //act
        purchaseService.addPizzaToPurchase(pizza);
        purchaseService.pickPurchase();
        purchaseService.completePurchase(PURCHASE_INDEX);
        //assert
        assertEquals(PIZZA_PRICE, purchase.getAmount());
    }

    @Test
    void givenTwoPizzasInPurchase_whenComputePurchase_thenExpectedBothPizzaPrices() {
        //arrange
        final long PURCHASE_INDEX = 1L;
        final double PIZZA_1_PRICE = 10.0;
        final double PIZZA_2_PRICE = 13.0;
        Purchase purchase = new Purchase();
        ReflectionTestUtils.setField(purchase, "id", PURCHASE_INDEX);
        Pizza pizza1 = createPizza(48L, PIZZA_1_PRICE, new ArrayList<>());
        Pizza pizza2 = createPizza(51L, PIZZA_2_PRICE, new ArrayList<>());
        purchase.setPizzas(Arrays.asList(pizza1, pizza2));
        when(purchaseRepository.findFirstByStateEquals(any(PurchaseStateEnum.class))).thenReturn(purchase);
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        //act
        purchaseService.addPizzaToPurchase(pizza1);
        purchaseService.addPizzaToPurchase(pizza2);
        purchaseService.pickPurchase();
        purchaseService.completePurchase(PURCHASE_INDEX);
        //assert
        assertEquals(PIZZA_1_PRICE + PIZZA_2_PRICE, purchase.getAmount());
    }

    @Test
    void givenThreePizzasInPurchase_whenComputePurchase_thenExpectedExpensivePizzaPrices() {
        //arrange
        final long PURCHASE_INDEX = 1L;
        final double PIZZA_1_PRICE = 10.0;
        final double PIZZA_2_PRICE = 13.0;
        final double CHEAP_PIZZA_PRICE = 5.0;
        Purchase purchase = new Purchase();
        ReflectionTestUtils.setField(purchase, "id", PURCHASE_INDEX);
        Pizza pizza1 = createPizza(48L, PIZZA_1_PRICE, new ArrayList<>());
        Pizza cheapPizza = createPizza(50L, CHEAP_PIZZA_PRICE, new ArrayList<>());
        Pizza pizza2 = createPizza(51L, PIZZA_2_PRICE, new ArrayList<>());
        purchase.setPizzas(Arrays.asList(pizza1, cheapPizza, pizza2));
        when(purchaseRepository.findFirstByStateEquals(any(PurchaseStateEnum.class))).thenReturn(purchase);
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        //act
        purchaseService.addPizzaToPurchase(pizza1);
        purchaseService.addPizzaToPurchase(cheapPizza);
        purchaseService.addPizzaToPurchase(pizza2);
        purchaseService.pickPurchase();
        purchaseService.completePurchase(PURCHASE_INDEX);
        //assert
        assertEquals(PIZZA_1_PRICE + PIZZA_2_PRICE, purchase.getAmount());
    }

    @Test
    void givenThreePizzasInPurchaseWithPineapple_whenComputePurchase_thenExpectedExpensivePizzaPricesAndDiscount() {
        //arrange
        final long PURCHASE_INDEX = 1L;
        final double PIZZA_PRICE = 10.0;
        final double PINAPPLE_PIZZA_PRICE = 13.0;
        final double CHEAP_PIZZA_PRICE = 5.0;
        Purchase purchase = new Purchase();
        ReflectionTestUtils.setField(purchase, "id", PURCHASE_INDEX);
        Pizza pizza = createPizza(48L, PIZZA_PRICE, new ArrayList<>());
        Pizza cheapPizza = createPizza(50L, CHEAP_PIZZA_PRICE, new ArrayList<>());
        Pizza pineapplePizza = createPizza(51L, PINAPPLE_PIZZA_PRICE, Arrays.asList("pineapple"));
        purchase.setPizzas(Arrays.asList(pizza, cheapPizza, pineapplePizza));
        when(purchaseRepository.findFirstByStateEquals(any(PurchaseStateEnum.class))).thenReturn(purchase);
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        //act
        purchaseService.addPizzaToPurchase(pizza);
        purchaseService.addPizzaToPurchase(cheapPizza);
        purchaseService.addPizzaToPurchase(pineapplePizza);
        purchaseService.pickPurchase();
        purchaseService.completePurchase(PURCHASE_INDEX);
        //assert
        assertEquals(PIZZA_PRICE * 0.9 + PINAPPLE_PIZZA_PRICE, purchase.getAmount());
    }

    private Pizza createPizza(final long id, final double price, final List<String> toppings) {
        Pizza pizza = new Pizza();
        ReflectionTestUtils.setField(pizza, "id", id);
        pizza.setPrice(price);
        pizza.setToppings(toppings);
        return pizza;
    }
}
