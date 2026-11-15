import type {CartItem} from '@/types/models';
import {resolveImageUrl} from '@/utils/normalize';
import {apiClient} from './client';

/** API cart line -> CartItem. The line carries name/image/price, enough for the cart screen. */
const toCartItems = (cart: any): CartItem[] =>
  (Array.isArray(cart?.items) ? cart.items : []).map((it: any) => {
    const price = Number(it.unitPrice ?? 0);
    return {
      productId: String(it.productId),
      quantity: Number(it.quantity ?? 0),
      size: it.size ?? '',
      price,
      product: {
        _id: String(it.productId),
        name: it.productName ?? '',
        description: '',
        price,
        images: it.imageUrl ? [resolveImageUrl(it.imageUrl)] : [],
        sizes: [],
        category: '',
        stock: 0,
        isFeatured: false,
        isActive: true,
        createdAt: '',
      },
    };
  });

/** `?size=` for lines with a size; the API matches an empty size as "no size". */
const sizeParams = (size: string) => (size ? {params: {size}} : undefined);

export const cartApi = {
  /** GET /api/cart */
  async get(): Promise<CartItem[]> {
    const {data} = await apiClient.get('/api/cart');
    return toCartItems(data);
  },

  /** POST /api/cart/items — adds to the quantity of an existing line. */
  async add(productId: string, quantity: number, size: string): Promise<CartItem[]> {
    const {data} = await apiClient.post('/api/cart/items', {
      productId: Number(productId),
      quantity,
      size: size || null,
    });
    return toCartItems(data);
  },

  /** PUT /api/cart/items/:productId?size= */
  async updateQuantity(productId: string, quantity: number, size: string): Promise<CartItem[]> {
    const {data} = await apiClient.put(
      `/api/cart/items/${productId}`,
      {quantity},
      sizeParams(size),
    );
    return toCartItems(data);
  },

  /** DELETE /api/cart/items/:productId?size= (204, the app already removed the line). */
  async removeItem(productId: string, size: string): Promise<void> {
    await apiClient.delete(`/api/cart/items/${productId}`, sizeParams(size));
  },

  /** DELETE /api/cart */
  async clear(): Promise<void> {
    await apiClient.delete('/api/cart');
  },
};
