package com.graphaware.pizzeria.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Entity
public class Purchase {

    @Id
    @Setter(value = AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @ManyToOne
    @JoinColumn
    private PizzeriaUser worker;

    @NotNull
    @ManyToOne
    @JoinColumn
    private PizzeriaUser customer;

    @NotNull
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PurchaseStateEnum state;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "purchase_pizza", joinColumns = @JoinColumn(name = "purchase_id"),
            inverseJoinColumns = @JoinColumn(name = "pizza_id"))
    private List<Pizza> pizzas;

    @CreationTimestamp
    @Setter(value = AccessLevel.NONE)
    private Date creationDate;

    private Date checkoutDate;

    private Double amount;
}
