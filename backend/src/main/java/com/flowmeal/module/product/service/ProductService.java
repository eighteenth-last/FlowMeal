package com.flowmeal.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.module.product.entity.Product;
import com.flowmeal.module.product.entity.ProductCategory;

import java.util.List;

/**
 * 餐品服务接口
 */
public interface ProductService {

    /** 添加餐品 */
    void addProduct(Product product);

    /** 修改餐品 */
    void updateProduct(Product product, Long merchantId);

    /** 删除餐品 */
    void deleteProduct(Long productId, Long merchantId);

    /** 上/下架 */
    void toggleStatus(Long productId, Long merchantId, Integer status);

    /** 商家端 - 餐品列表（分页） */
    Page<Product> listByMerchant(Long merchantId, int page, int size);

    /** 用户端 - 餐品列表（按分类） */
    List<Product> listWithCategory(Long merchantId);

    /** 分类管理 */
    void addCategory(ProductCategory category);
    void deleteCategory(Long categoryId, Long merchantId);
    List<ProductCategory> listCategories(Long merchantId);
}
