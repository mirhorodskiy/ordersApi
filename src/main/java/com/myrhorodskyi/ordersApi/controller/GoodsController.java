package com.myrhorodskyi.ordersApi.controller;

import com.myrhorodskyi.ordersApi.model.entity.Goods;
import com.myrhorodskyi.ordersApi.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping
    public List<Goods> getAllGoods() {
        return goodsService.getAllGoods();
    }

    @GetMapping("/{id}")
    public Goods getGoodsById(@PathVariable Long id) {
        return goodsService.getGoodsById(id);
    }

    @PostMapping
    public Goods createGoods(@RequestBody Goods goods) {
        return goodsService.createGoods(goods);
    }

    @PutMapping("/{id}")
    public Goods updateGoods(@PathVariable Long id, @RequestBody Goods goods) {
        return goodsService.updateGoods(id, goods);
    }

    @DeleteMapping("/{id}")
    public void deleteGoods(@PathVariable Long id) {
        goodsService.deleteGoods(id);
    }

}
