package com.myrhorodskyi.ordersApi.service.impl;

import com.myrhorodskyi.ordersApi.exception.SearchRuntimeException;
import com.myrhorodskyi.ordersApi.model.entity.Goods;
import com.myrhorodskyi.ordersApi.repository.GoodsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class GoodsServiceImplTest {

    private static final long goodsId = 1L;
    private static final long invalidGoodsId = 999L;

    @Mock
    private GoodsRepository goodsRepository;

    @InjectMocks
    private GoodsServiceImpl goodsService;

    @Test
    void getAllGoods_ShouldReturnListOfGoods() {
        List<Goods> expectedGoodsList = Arrays.asList(
                new Goods(1L, "Laptop", 1200.0, 5),
                new Goods(2L, "Smartphone", 800.0, 10)
        );
        when(goodsRepository.findAll()).thenReturn(expectedGoodsList);

        List<Goods> actualGoodsList = goodsService.getAllGoods();

        assertEquals(expectedGoodsList, actualGoodsList);
    }

    @Test
    void getGoodsById_WithValidId_ShouldReturnGoods() {
        Goods expectedGoods = new Goods(goodsId, "Laptop", 1200.0, 5);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(expectedGoods));

        Goods actualGoods = goodsService.getGoodsById(goodsId);

        assertEquals(expectedGoods, actualGoods);
    }

    @Test
    void getGoodsById_WithInvalidId_ShouldThrowException() {
        when(goodsRepository.findById(invalidGoodsId)).thenReturn(Optional.empty());

        assertThrows(SearchRuntimeException.class, () -> goodsService.getGoodsById(invalidGoodsId));
    }

    @Test
    void createGoods_ShouldReturnCreatedGoods() {
        Goods goodsToCreate = new Goods(null, "Tablet", 400.0, 8);
        Goods createdGoods = new Goods(3L, "Tablet", 400.0, 8);
        when(goodsRepository.save(goodsToCreate)).thenReturn(createdGoods);

        Goods actualCreatedGoods = goodsService.createGoods(goodsToCreate);

        assertEquals(createdGoods, actualCreatedGoods);
    }

    @Test
    void updateGoods_WithValidId_ShouldReturnUpdatedGoods() {
        Goods existingGoods = new Goods(goodsId, "Laptop", 1200.0, 5);
        Goods updatedGoods = new Goods(goodsId, "Updated Laptop", 1500.0, 3);

        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(existingGoods));
        when(goodsRepository.save(updatedGoods)).thenReturn(updatedGoods);

        Goods actualUpdatedGoods = goodsService.updateGoods(goodsId, updatedGoods);

        assertEquals(updatedGoods, actualUpdatedGoods);
    }

    @Test
    void updateGoods_WithInvalidId_ShouldThrowException() {
        Goods updatedGoods = new Goods(invalidGoodsId, "Invalid Laptop", 1500.0, 3);

        when(goodsRepository.findById(invalidGoodsId)).thenReturn(Optional.empty());

        assertThrows(SearchRuntimeException.class, () -> goodsService.updateGoods(invalidGoodsId, updatedGoods));
    }

    @Test
    void deleteGoods_WithValidId_ShouldDeleteGoods() {
        Goods existingGoods = new Goods(goodsId, "Laptop", 1200.0, 5);
        when(goodsRepository.findById(goodsId)).thenReturn(Optional.of(existingGoods));

        goodsService.deleteGoods(goodsId);

        verify(goodsRepository, times(1)).delete(existingGoods);
    }

    @Test
    void deleteGoods_WithInvalidId_ShouldThrowException() {
        when(goodsRepository.findById(invalidGoodsId)).thenReturn(Optional.empty());

        SearchRuntimeException exception = assertThrows(SearchRuntimeException.class, () -> goodsService.deleteGoods(invalidGoodsId));

        verify(goodsRepository, never()).delete(any(Goods.class));
        assertEquals("Goods not found with id " + invalidGoodsId, exception.getMessage());
    }

}