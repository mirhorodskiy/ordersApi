package com.myrhorodskyi.ordersApi.dto;

import lombok.Data;

@Data
public class OrderItemRequest {

    private Long goodsId;
    private int quantity;
}
