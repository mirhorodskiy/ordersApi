package com.myrhorodskyi.ordersApi.service.impl;

import com.myrhorodskyi.ordersApi.exception.SearchRuntimeException;
import com.myrhorodskyi.ordersApi.model.entity.Goods;
import com.myrhorodskyi.ordersApi.repository.GoodsRepository;
import com.myrhorodskyi.ordersApi.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoodsServiceImpl implements GoodsService {

    private final GoodsRepository goodsRepository;

    @Autowired
    public GoodsServiceImpl(GoodsRepository goodsRepository) {
        this.goodsRepository = goodsRepository;
    }

    @Override
    public List<Goods> getAllGoods() {
        return goodsRepository.findAll();
    }

    @Override
    public Goods getGoodsById(Long id) {
        return goodsRepository.findById(id).orElseThrow(() -> new SearchRuntimeException("Goods not found with id " + id));
    }

    @Override
    public Goods createGoods(Goods goods) {
        return goodsRepository.save(goods);
    }

    @Override
    public Goods updateGoods(Long id, Goods goods) {
        Optional<Goods> existingGoodsOptional = goodsRepository.findById(id);
        if (existingGoodsOptional.isPresent()) {
            Goods existingGoods = existingGoodsOptional.get();
            existingGoods.setName(goods.getName());
            existingGoods.setPrice(goods.getPrice());
            existingGoods.setQuantity(goods.getQuantity());
            return goodsRepository.save(existingGoods);
        } else {
            throw new SearchRuntimeException("Goods not found with id " + id);
        }
    }

    @Override
    public void deleteGoods(Long id) {
        var goods = goodsRepository.findById(id).orElseThrow(() -> new SearchRuntimeException("Goods not found with id " + id));
        goodsRepository.delete(goods);
    }
}
