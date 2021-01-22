package com.graphaware.pizzeria.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;
import lombok.Setter;

@Data
@Entity
public class PizzeriaUser {

    @Id
    @Setter(value = AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private String name;

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String email;

    @NotNull
    @Column(length = 10485760)
    @Convert(converter = RoleConverter.class)
    private List<UserRoleEnum> roles;
}
