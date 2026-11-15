/** Currency symbol shown in prices. */
export const CURRENCY = '₹';

/** Delivery is free: the API's order total is the sum of the items. */
export const DELIVERY_FEE = 0;

/** Products shown per "page" in the shop list (client-side paging). */
export const SHOP_PAGE_SIZE = 10;

/** Maximum images per product (API: ProductRequest.images @Size(max = 8)). */
export const MAX_PRODUCT_IMAGES = 8;

/** Flat delivery fee for a non-empty cart (matches the backend order calculation). */
export const shippingFor = (subtotal: number): number => (subtotal > 0 ? DELIVERY_FEE : 0);
