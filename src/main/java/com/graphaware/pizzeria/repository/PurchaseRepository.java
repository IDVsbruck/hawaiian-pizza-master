package com.graphaware.pizzeria.repository;

import com.graphaware.pizzeria.model.Purchase;
import com.graphaware.pizzeria.model.PurchaseStateEnum;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends CrudRepository<Purchase, Long> {

    Purchase findFirstByStateEquals(PurchaseStateEnum state);
    List<Purchase> findAllByStateEqualsAndCustomer_Id(PurchaseStateEnum state, Long customerId);
}
