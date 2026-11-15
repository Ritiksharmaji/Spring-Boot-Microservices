import type {Page, ProductInput} from '@/types/api';
import type {Product} from '@/types/models';
import {normalizeProduct, normalizeProducts} from '@/utils/normalize';
import {apiClient} from './client';

/** Form values + image URLs -> API product body (JSON). */
const toBody = (input: ProductInput, images: string[]) => ({
  name: input.name,
  description: input.description,
  price: Number(input.price),
  stockQuantity: Number(input.stock),
  category: input.category,
  subCategory: input.subCategory || null,
  sizes: input.sizes,
  bestseller: input.isFeatured,
  images,
});

/** Photos picked on the phone (file://, ph://, content://) still have to be uploaded. */
const isLocalFile = (uri: string) => !/^(https?:\/\/|\/)/.test(uri);

/** Uploads local photos (one at a time) and returns all images as server URLs, order kept. */
const uploadLocalImages = async (images: string[]): Promise<string[]> => {
  const urls: string[] = [];
  for (const image of images) {
    urls.push(isLocalFile(image) ? await productApi.uploadImage(image) : image);
  }
  return urls;
};

export const productApi = {
  /** POST /api/products/images (admin, multipart `file`): stores a JPEG/PNG/WebP, returns its URL. */
  async uploadImage(uri: string): Promise<string> {
    const form = new FormData();
    form.append('file', {uri, name: 'product.jpg', type: 'image/jpeg'} as unknown as Blob);
    const {data} = await apiClient.post<{url: string}>('/api/products/images', form, {
      headers: {'Content-Type': 'multipart/form-data'},
      // send the FormData as is: React Native's networking writes the multipart body + boundary
      transformRequest: body => body,
      timeout: 60_000,
    });
    return data.url;
  },

  /** GET /api/products — the whole catalogue (filtered and paged in the app). */
  async getAll(): Promise<Product[]> {
    const {data} = await apiClient.get<Page<unknown>>('/api/products', {
      params: {size: 500, sort: 'id,desc'},
    });
    return normalizeProducts(data.content);
  },

  /** GET /api/products/:id */
  async getById(id: string): Promise<Product> {
    const {data} = await apiClient.get(`/api/products/${id}`);
    return normalizeProduct(data);
  },

  /** POST /api/products (admin). Local photos are uploaded first. */
  async create(input: ProductInput, images: string[]): Promise<Product> {
    const urls = await uploadLocalImages(images);
    const {data} = await apiClient.post('/api/products', toBody(input, urls));
    return normalizeProduct(data);
  },

  /** PUT /api/products/:id (admin) */
  async update(id: string, input: ProductInput, images: string[]): Promise<Product> {
    const urls = await uploadLocalImages(images);
    const {data} = await apiClient.put(`/api/products/${id}`, toBody(input, urls));
    return normalizeProduct(data);
  },

  /** DELETE /api/products/:id (admin) — hides the product from the shop. */
  async remove(id: string): Promise<void> {
    await apiClient.delete(`/api/products/${id}`);
  },
};
