package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;
// 所实现的接口有两个泛型，第一个指定注解；第二个指定校验什么类型的数据
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();
    //初始化方法(constraintAnnotation可以获取注解中的属性)
    @Override
    public void initialize(ListValue constraintAnnotation) {

        int[] vals = constraintAnnotation.vals();
        if (vals != null && vals.length >0){
            for (int val : vals) {
                set.add(val);
            }
        }
    }

    //判断是否校验成功

    /**
     *
     * @param value 需要校验的值
     * @param context 环境信息
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
