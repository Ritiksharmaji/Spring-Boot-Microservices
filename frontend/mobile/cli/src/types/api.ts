/** Spring Data page: `GET /api/products`, `/api/orders`, `/api/users` ... */
export interface Page<T> {
  content: T[];
  page: {size: number; number: number; totalElements: number; totalPages: number};
}

export interface AuthResponse {
  token: string;
  user: import('./models').User;
}

/** Fields of the admin product form (sent as JSON with image URLs). */
export interface ProductInput {
  name: string;
  description: string;
  price: string;
  stock: string;
  category: string;
  subCategory?: string;
  sizes: string[];
  isFeatured: boolean;
}
