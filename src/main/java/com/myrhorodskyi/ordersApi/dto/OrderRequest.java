package com.myrhorodskyi.ordersApi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private String customerName;
    private List<OrderItemRequest> orderItems;
}
