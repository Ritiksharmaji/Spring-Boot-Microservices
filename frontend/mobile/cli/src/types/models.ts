/**
 * Domain models used by the screens. The Spring Boot API (backend/monolith) JSON is mapped to these
 * shapes in utils/normalize.ts.
 */

export type UserRole = 'user' | 'admin';

export interface User {
  _id: string;
  name: string;
  firstName: string;
  lastName?: string;
  email: string;
  phone?: string;
  role: UserRole;
}

export interface Product {
  _id: string;
  name: string;
  description: string;
  price: number;
  images: string[];
  sizes: string[];
  category: string;
  subCategory?: string;
  stock: number;
  isFeatured: boolean;
  isActive: boolean;
  createdAt: string;
}

export interface CartItem {
  productId: string;
  product: Product;
  quantity: number;
  size: string;
  price: number;
}

export interface ShippingAddress {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface Address extends ShippingAddress {
  _id: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  type: 'Home' | 'Work' | 'Other';
  isDefault: boolean;
  createdAt: string;
}

/** API order status (PENDING ...) in lower case. */
export type OrderStatus = 'pending' | 'confirmed' | 'shipped' | 'delivered' | 'cancelled';
/** API payment status in lower case, `_` replaced by a space. */
export type PaymentStatus =
  | 'pending'
  | 'paid'
  | 'failed'
  | 'cash on delivery'
  | 'refunded'
  | 'not charged';
/** CASH = cash on delivery; CARD = simulated card payment (approved up to ₹50,000). */
export type PaymentMethod = 'cash' | 'card';

export interface OrderItem {
  _id: string;
  /** Populated product (name/images), or a placeholder when the product was deleted. */
  product: Pick<Product, 'images'> & Partial<Pick<Product, '_id' | 'name'>>;
  name: string;
  quantity: number;
  price: number;
  size?: string;
}

export interface Order {
  _id: string;
  /** Customer id + delivery recipient name. */
  user: Pick<User, '_id' | 'name'> | string;
  orderNumber: string;
  /** Recipient of the delivery. */
  recipientName?: string;
  recipientPhone?: string;
  /** Why the order is in its status, e.g. "Payment failed: ...". */
  statusReason?: string;
  items: OrderItem[];
  shippingAddress: ShippingAddress;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  orderStatus: OrderStatus;
  subtotal: number;
  shippingCost: number;
  tax: number;
  totalAmount: number;
  notes?: string;
  createdAt: string;
}

export interface AdminStats {
  totalUsers: number;
  totalProducts: number;
  totalOrders: number;
  totalRevenue: number;
  recentOrders: Order[];
}
