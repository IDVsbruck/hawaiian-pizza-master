package com.graphaware.pizzeria.controller;

import com.graphaware.pizzeria.model.Pizza;
import com.graphaware.pizzeria.model.Purchase;
import com.graphaware.pizzeria.service.PizzeriaException;
import com.graphaware.pizzeria.service.PurchaseService;

import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/purchase", produces = MediaType.APPLICATION_JSON_VALUE)
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping(path = "/addPizza")
    @CrossOrigin(methods = { RequestMethod.POST, RequestMethod.OPTIONS })
    public ResponseEntity<Purchase> addToBasked(@RequestBody @Valid Pizza pizza) {
        Purchase purchase = purchaseService.addPizzaToPurchase(pizza);
        return new ResponseEntity<>(purchase, HttpStatus.OK);
    }

    @PostMapping(path = "/submitOrder")
    @CrossOrigin(methods = { RequestMethod.POST, RequestMethod.OPTIONS })
    public ResponseEntity<Void> submitPurchase() {
        purchaseService.confirmPurchase();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/pickPurchase")
    @CrossOrigin(methods = { RequestMethod.GET, RequestMethod.OPTIONS })
    public ResponseEntity<Purchase> pickPurchase() {
        Purchase purchase = purchaseService.pickPurchase();
        return new ResponseEntity<>(purchase, HttpStatus.OK);
    }

    @PutMapping(path = "/completePurchase/{id}")
    @CrossOrigin(methods = { RequestMethod.PUT, RequestMethod.OPTIONS })
    public ResponseEntity<Void> completePurchase(@PathVariable(name = "id") Long id) {
        if (id == null) {
            throw new PizzeriaException();
        }
        purchaseService.completePurchase(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/currentPurchase")
    @CrossOrigin(methods = { RequestMethod.GET, RequestMethod.OPTIONS })
    public ResponseEntity<Purchase> getCurrentPurchase() {
        Purchase purchase = purchaseService.getCurrentPurchase();
        return new ResponseEntity<>(purchase, purchase == null ? HttpStatus.NO_CONTENT : HttpStatus.FOUND);
    }
}
