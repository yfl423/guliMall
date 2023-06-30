package com.atguigu.gulimall.product.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog2Vo {
    private String catalog1Id;
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo{
        /**
         * "catalog2Id":"62",
         * "id":"624",
         * "name":"高跟鞋
         */
        private String catalog2Id;
        private String id;
        private String name;
    }
}
