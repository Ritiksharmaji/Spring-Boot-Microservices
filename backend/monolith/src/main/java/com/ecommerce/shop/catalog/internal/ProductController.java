package com.ecommerce.shop.catalog.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.ecommerce.shop.catalog.internal.ProductDtos.*;

@Tag(name = "Products", description = "Public catalog; create/update/delete need ADMIN")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
class ProductController {

    private final CatalogService catalog;
    private final ProductImageStorage images;

    @Operation(summary = "Upload a product image (ADMIN): JPEG, PNG or WebP up to 5 MB; returns its URL for `images`")
    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ImageUploadResponse> uploadImage(@RequestPart("file") MultipartFile file) {
        String path = images.store(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(url));
    }

    @Operation(summary = "List active products (paged, optional category)")
    @GetMapping
    Page<ProductResponse> list(@RequestParam(required = false) String category,
                               @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
                               Pageable pageable) {
        return catalog.list(category, pageable);
    }

    @Operation(summary = "One product")
    @GetMapping("/{id}")
    ProductResponse get(@PathVariable long id) {
        return catalog.get(id);
    }

    @Operation(summary = "Search in-stock products by name or description")
    @GetMapping("/search")
    Page<ProductResponse> search(@RequestParam @NotBlank String keyword,
                                 @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return catalog.search(keyword, pageable);
    }

    @Operation(summary = "Create a product (ADMIN)")
    @PostMapping
    ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalog.create(request));
    }

    @Operation(summary = "Update a product (ADMIN)")
    @PutMapping("/{id}")
    ProductResponse update(@PathVariable long id, @Valid @RequestBody ProductRequest request) {
        return catalog.update(id, request);
    }

    @Operation(summary = "Soft-delete a product (ADMIN)")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        catalog.delete(id);
        return ResponseEntity.noContent().build();
    }
}
