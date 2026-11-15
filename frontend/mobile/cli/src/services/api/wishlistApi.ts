import type {Product} from '@/types/models';
import {apiClient} from './client';
import {productApi} from './productApi';

/** The API stores product ids; the app shows full products from the catalogue. */
const toProducts = async (productIds: unknown): Promise<Product[]> => {
  const ids = new Set((Array.isArray(productIds) ? productIds : []).map(String));
  if (ids.size === 0) {
    return [];
  }
  return (await productApi.getAll()).filter(p => ids.has(p._id));
};

export const wishlistApi = {
  /** GET /api/wishlist -> {productIds} */
  async get(): Promise<Product[]> {
    const {data} = await apiClient.get('/api/wishlist');
    return toProducts(data?.productIds);
  },

  /** PUT or DELETE /api/wishlist/:productId; returns the updated wishlist. */
  async toggle(productId: string): Promise<Product[]> {
    const {data: current} = await apiClient.get('/api/wishlist');
    const ids: string[] = (current?.productIds ?? []).map(String);
    const {data} = ids.includes(productId)
      ? await apiClient.delete(`/api/wishlist/${productId}`)
      : await apiClient.put(`/api/wishlist/${productId}`);
    return toProducts(data?.productIds);
  },
};
