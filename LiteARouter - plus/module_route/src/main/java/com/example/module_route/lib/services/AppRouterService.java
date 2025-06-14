package com.example.module_route.lib.services;

import android.content.Intent;

import com.example.module_route.lib.CartItem;
import com.example.module_route.lib.annotations.Body;
import com.example.module_route.lib.annotations.GET;
import com.example.module_route.lib.annotations.POST;
import com.example.module_route.lib.annotations.Query;

import java.util.List;

// AppRouterService.java
public interface AppRouterService {
    @GET("/home/detail")
    Intent navigateToHomeDetail(
            @Query("id") String productId,
            @Query("tab") String tabName
    );

    @POST("/cart/checkout")
    Intent navigateToCartCheckout(@Body List<CartItem> items);
}