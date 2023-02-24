package com.myomi.order.dto;

import com.myomi.order.entity.Order;
import com.myomi.product.entity.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderDetailDto {
    private Order order;
    private Product product;
    private int prodCnt;

    @Builder
    public OrderDetailDto (Order order,Product product, int prodCnt) {
        this.order = order;
        this.product = product;
        this.prodCnt = prodCnt;
    }


//    public OrderDetail createOrderDetail(OrderDetail detail) {
//        return OrderDetail.builder()
//                .order(detail.getOrder())
//                .product(detail.getProduct())
//                .prodCnt(detail.getProdCnt())
//                .build();
//    }
}