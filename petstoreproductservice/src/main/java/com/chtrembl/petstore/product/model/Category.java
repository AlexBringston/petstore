package com.chtrembl.petstore.product.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "category")
public class Category {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    public Category name(String name) {
        this.name = name;
        return this;
    }
}