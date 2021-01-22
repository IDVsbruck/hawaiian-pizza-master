package com.graphaware.pizzeria.service;

import com.graphaware.pizzeria.model.Pizza;
import com.graphaware.pizzeria.repository.PizzaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PizzaService {

    private final PizzaRepository pizzaRepository;

    public void storePizza(final Pizza pizza) {
        pizzaRepository.save(pizza);
    }

    public List<Pizza> getAllPizzas() {
        return pizzaRepository.findAll();
    }

    public List<Pizza> getPizzasWithTopping(final String topping) {
        return pizzaRepository.getPizzaContainingTopping(topping);
    }
}
