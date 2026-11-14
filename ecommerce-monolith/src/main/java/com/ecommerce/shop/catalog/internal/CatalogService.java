package com.ecommerce.shop.catalog.internal;

import com.ecommerce.shop.catalog.Catalog;
import com.ecommerce.shop.catalog.ProductInfo;
import com.ecommerce.shop.catalog.StockLine;
import com.ecommerce.shop.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.ecommerce.shop.catalog.internal.ProductDtos.*;

@Slf4j
@Service
@RequiredArgsConstructor
class CatalogService implements Catalog {

    private final ProductRepository products;

    // ---------------- public catalog / admin ----------------

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String category, Pageable pageable) {
        Page<Product> page = StringUtils.hasText(category)
                ? products.findByActiveTrueAndCategoryIgnoreCase(category.trim(), pageable)
                : products.findByActiveTrue(pageable);
        return page.map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(long id) {
        return products.findByIdAndActiveTrue(id).map(ProductResponse::from).orElseThrow(() -> notFound(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Pageable pageable) {
        return products.search(keyword.trim(), pageable).map(ProductResponse::from);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product p = new Product();
        apply(p, request);
        if (request.active() == null) {
            p.setActive(true);
        }
        return ProductResponse.from(products.save(p));
    }

    @Transactional
    public ProductResponse update(long id, ProductRequest request) {
        Product p = products.findById(id).orElseThrow(() -> notFound(id));
        apply(p, request);
        return ProductResponse.from(products.saveAndFlush(p));
    }

    /** Soft delete: hidden from the catalog, still referenced by past orders. */
    @Transactional
    public void delete(long id) {
        products.findById(id).orElseThrow(() -> notFound(id)).setActive(false);
    }

    // ---------------- module API ----------------

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductInfo> findActiveProduct(long productId) {
        return products.findByIdAndActiveTrue(productId)
                .map(p -> new ProductInfo(p.getId(), p.getName(), p.getPrice(), p.getStockQuantity()));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(List<StockLine> lines) {
        for (StockLine line : lines) {
            if (products.decreaseStock(line.productId(), line.quantity()) == 0) {
                throw BusinessException.conflict("Insufficient Stock",
                        "Not enough stock for product " + line.productId() + "; please review your cart");
            }
        }
    }

    @Override
    @Transactional
    public void release(List<StockLine> lines) {
        lines.forEach(l -> products.increaseStock(l.productId(), l.quantity()));
        log.info("Released stock for {} line(s)", lines.size());
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setDescription(r.description());
        p.setPrice(r.price());
        p.setStockQuantity(r.stockQuantity());
        p.setCategory(r.category());
        p.setImageUrl(r.imageUrl());
        if (r.active() != null) {
            p.setActive(r.active());
        }
    }

    private BusinessException notFound(long id) {
        return BusinessException.notFound("Product " + id + " not found");
    }
}
