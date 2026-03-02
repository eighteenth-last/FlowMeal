package com.flowmeal.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowmeal.common.exception.BusinessException;
import com.flowmeal.common.result.ResultCode;
import com.flowmeal.module.product.entity.Product;
import com.flowmeal.module.product.entity.ProductCategory;
import com.flowmeal.module.product.mapper.ProductCategoryMapper;
import com.flowmeal.module.product.mapper.ProductMapper;
import com.flowmeal.module.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 餐品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;

    @Override
    @CacheEvict(value = "products", key = "#product.merchantId")
    public void addProduct(Product product) {
        product.setStatus(1);
        product.setSales(0);
        productMapper.insert(product);
    }

    @Override
    @CacheEvict(value = "products", key = "#merchantId")
    public void updateProduct(Product product, Long merchantId) {
        Product existing = getAndCheckMerchant(product.getId(), merchantId);
        productMapper.updateById(product);
    }

    @Override
    @CacheEvict(value = "products", key = "#merchantId")
    public void deleteProduct(Long productId, Long merchantId) {
        getAndCheckMerchant(productId, merchantId);
        productMapper.deleteById(productId);
    }

    @Override
    @CacheEvict(value = "products", key = "#merchantId")
    public void toggleStatus(Long productId, Long merchantId, Integer status) {
        getAndCheckMerchant(productId, merchantId);
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .set(Product::getStatus, status));
    }

    @Override
    public Page<Product> listByMerchant(Long merchantId, int page, int size) {
        return productMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .orderByAsc(Product::getSort));
    }

    @Override
    @Cacheable(value = "products", key = "#merchantId")
    public List<Product> listWithCategory(Long merchantId) {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .eq(Product::getStatus, 1)
                .orderByAsc(Product::getSort));
    }

    @Override
    public void addCategory(ProductCategory category) {
        categoryMapper.insert(category);
    }

    @Override
    public void deleteCategory(Long categoryId, Long merchantId) {
        categoryMapper.delete(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getId, categoryId)
                .eq(ProductCategory::getMerchantId, merchantId));
    }

    @Override
    public List<ProductCategory> listCategories(Long merchantId) {
        return categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getMerchantId, merchantId)
                .orderByAsc(ProductCategory::getSort));
    }

    private Product getAndCheckMerchant(Long productId, Long merchantId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (!product.getMerchantId().equals(merchantId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return product;
    }
}
