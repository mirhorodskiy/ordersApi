package com.myrhorodskyi.ordersApi.service;

import com.myrhorodskyi.ordersApi.model.entity.Goods;

import java.util.List;

public interface GoodsService {

    List<Goods> getAllGoods();

    Goods getGoodsById(Long id);

    Goods createGoods(Goods goods);

    Goods updateGoods(Long id, Goods goods);

    void deleteGoods(Long id);
}
