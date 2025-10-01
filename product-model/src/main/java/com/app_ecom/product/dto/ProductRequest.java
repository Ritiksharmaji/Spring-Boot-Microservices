package com.app_ecom.product.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String name;
    private String description;
    private String price;
    private String stockQuantity;
    private String category;
    private String imageUrl;
    private Boolean active;

}