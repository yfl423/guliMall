package com.atguigu.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockLockedTo {
    private Long id; // 库存工作单id
    private Long detailId; //详情id
}
