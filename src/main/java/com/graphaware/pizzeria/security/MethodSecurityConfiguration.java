package com.graphaware.pizzeria.security;

import com.graphaware.pizzeria.model.UserRoleEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        MethodSecurityExpressionHandler expressionHandler = super.createExpressionHandler();
        ((DefaultMethodSecurityExpressionHandler) expressionHandler).setRoleHierarchy(getRoleHierarchy());
        return expressionHandler;
    }

    private RoleHierarchy getRoleHierarchy() {
        StringWriter roleHierarchyDescriptionBuffer = new StringWriter();
        PrintWriter roleHierarchyDescriptionWriter = new PrintWriter(roleHierarchyDescriptionBuffer);

        roleHierarchyDescriptionWriter.println(UserRoleEnum.OWNER.name() + " > " + "VIEW_STATISTICS");
        roleHierarchyDescriptionWriter.println(UserRoleEnum.PIZZA_MAKER.name() + " > " + "PICK_PURCHASE");
        roleHierarchyDescriptionWriter.println(UserRoleEnum.PIZZA_MAKER.name() + " > " + "COMPLETE_PURCHASE");
        roleHierarchyDescriptionWriter.println(UserRoleEnum.CUSTOMER.name() + " > " + "ADD_PIZZA");
        roleHierarchyDescriptionWriter.println(UserRoleEnum.CUSTOMER.name() + " > " + "VIEW_PURCHASE");
        roleHierarchyDescriptionWriter.println(UserRoleEnum.CUSTOMER.name() + " > " + "CONFIRM_PURCHASE");

        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(roleHierarchyDescriptionBuffer.toString());

        return roleHierarchy;
    }
}
