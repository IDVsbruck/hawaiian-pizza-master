package com.graphaware.pizzeria.controller;

import com.graphaware.pizzeria.model.Pizza;
import com.graphaware.pizzeria.service.PizzaService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/pizza", produces = MediaType.APPLICATION_JSON_VALUE)
public class PizzaController {

    private final PizzaService pizzaService;

    @PostMapping
    @CrossOrigin(methods = { RequestMethod.POST, RequestMethod.OPTIONS })
    public void createPizza(@RequestBody @Valid Pizza pizza){
        pizzaService.storePizza(pizza);
    }

    @GetMapping
    @CrossOrigin(methods = { RequestMethod.GET, RequestMethod.OPTIONS })
    public List<Pizza> getPizzas(@RequestParam(value = "topping", required = false) String topping){
        if (topping == null) {
            return pizzaService.getAllPizzas();
        }
        return pizzaService.getPizzasWithTopping(topping);
    }
}
