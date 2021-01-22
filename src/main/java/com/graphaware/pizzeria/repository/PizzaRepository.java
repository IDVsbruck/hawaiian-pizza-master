package com.graphaware.pizzeria.repository;

import com.graphaware.pizzeria.model.Pizza;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PizzaRepository extends CrudRepository<Pizza, Long> {

    List<Pizza> findAll();
    List<Pizza> getPizzaContainingTopping(@Param(value = "topping") String topping);
}
