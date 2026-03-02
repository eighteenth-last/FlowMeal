package com.flowmeal.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowmeal.module.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {}
