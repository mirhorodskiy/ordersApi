package com.myrhorodskyi.ordersApi.repository;

import com.myrhorodskyi.ordersApi.model.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsRepository extends JpaRepository<Goods, Long> {
}
