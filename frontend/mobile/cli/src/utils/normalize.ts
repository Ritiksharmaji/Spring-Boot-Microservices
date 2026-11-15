import {env} from '@/config/env';
import type {
  Address,
  Order,
  OrderStatus,
  PaymentStatus,
  Product,
  ShippingAddress,
  User,
} from '@/types/models';

/**
 * Maps the Spring Boot API's JSON (backend/monolith) to the models the screens use, and back.
 * Same idea as frontend/web/src/api/mappers.js.
 */

/** Demo images are site-relative ("/products/p_img1.png"): they are served by the web shop. */
export const resolveImageUrl = (url: string): string =>
  url.startsWith('/') ? `${env.websiteUrl}${url}` : url;

const toImages = (p: any): string[] => {
  const list: string[] =
    Array.isArray(p?.images) && p.images.length ? p.images : p?.imageUrl ? [p.imageUrl] : [];
  return list.filter(Boolean).map(resolveImageUrl);
};

export const normalizeProduct = (p: any): Product => ({
  _id: String(p?.id),
  name: p?.name ?? '',
  description: p?.description ?? '',
  price: Number(p?.price ?? 0),
  images: toImages(p),
  sizes: Array.isArray(p?.sizes) ? p.sizes : [],
  category: p?.category ?? '',
  subCategory: p?.subCategory ?? undefined,
  stock: Number(p?.stockQuantity ?? 0),
  isFeatured: !!p?.bestseller,
  isActive: p?.active ?? true,
  createdAt: p?.createdAt ?? new Date().toISOString(),
});

export const normalizeProducts = (list: unknown): Product[] =>
  Array.isArray(list) ? list.filter(Boolean).map(normalizeProduct) : [];

export const normalizeUser = (u: any): User => ({
  _id: String(u?.id),
  name: [u?.firstName, u?.lastName].filter(Boolean).join(' '),
  firstName: u?.firstName ?? '',
  lastName: u?.lastName ?? undefined,
  email: u?.email ?? '',
  phone: u?.phone ?? undefined,
  role: u?.role === 'ADMIN' ? 'admin' : 'user',
});

/** "Ritik Kumar Sharma" -> {firstName: "Ritik", lastName: "Kumar Sharma"} */
export const splitName = (name: string): {firstName: string; lastName: string | null} => {
  const [firstName = '', ...rest] = name.trim().split(/\s+/);
  return {firstName, lastName: rest.length ? rest.join(' ') : null};
};

export const normalizeAddress = (a: any): Address => ({
  _id: String(a?.id),
  type: ['Home', 'Work', 'Other'].includes(a?.label) ? a.label : 'Home',
  firstName: a?.firstName ?? '',
  lastName: a?.lastName ?? undefined,
  phone: a?.phone ?? undefined,
  street: a?.street ?? '',
  city: a?.city ?? '',
  state: a?.state ?? '',
  zipCode: a?.zipcode ?? '',
  country: a?.country ?? '',
  isDefault: !!a?.isDefault,
  createdAt: a?.createdAt ?? '',
});

/** UI address -> API address body ({zipcode}, empty optional fields as null). */
export const toApiAddress = (a: ShippingAddress) => ({
  street: a.street.trim(),
  city: a.city.trim(),
  state: a.state.trim() || null,
  zipcode: a.zipCode.trim(),
  country: a.country.trim(),
});

const ORDER_STATUSES: OrderStatus[] = ['pending', 'confirmed', 'shipped', 'delivered', 'cancelled'];

const toOrderStatus = (status: unknown): OrderStatus => {
  const value = String(status ?? '').toLowerCase() as OrderStatus;
  return ORDER_STATUSES.includes(value) ? value : 'pending';
};

const toPaymentStatus = (status: unknown): PaymentStatus =>
  String(status ?? 'PENDING')
    .toLowerCase()
    .replace(/_/g, ' ') as PaymentStatus;

export const normalizeOrder = (o: any): Order => {
  const items = Array.isArray(o?.items) ? o.items : [];
  const subtotal = items.reduce((sum: number, it: any) => sum + Number(it?.subTotal ?? 0), 0);
  const address = o?.shippingAddress ?? {};
  return {
    _id: String(o?.id),
    user: {_id: String(o?.userId ?? ''), name: o?.recipientName ?? ''},
    orderNumber: String(o?.id),
    recipientName: o?.recipientName ?? undefined,
    recipientPhone: o?.recipientPhone ?? undefined,
    statusReason: o?.statusReason ?? undefined,
    items: items.map((it: any, index: number) => ({
      _id: `${o?.id}-${index}`,
      product: {
        _id: String(it?.productId),
        name: it?.productName,
        images: it?.imageUrl ? [resolveImageUrl(it.imageUrl)] : [],
      },
      name: it?.productName ?? '',
      quantity: Number(it?.quantity ?? 0),
      price: Number(it?.unitPrice ?? 0),
      size: it?.size || undefined,
    })),
    shippingAddress: {
      street: address.street ?? '',
      city: address.city ?? '',
      state: address.state ?? '',
      zipCode: address.zipcode ?? '',
      country: address.country ?? '',
    },
    paymentMethod: o?.paymentMethod === 'CASH' ? 'cash' : 'card',
    paymentStatus: toPaymentStatus(o?.paymentStatus),
    orderStatus: toOrderStatus(o?.status),
    subtotal,
    shippingCost: 0,
    tax: 0,
    totalAmount: Number(o?.totalAmount ?? subtotal),
    notes: o?.notes ?? undefined,
    createdAt: o?.createdAt ?? '',
  };
};

export const normalizeOrders = (list: unknown): Order[] =>
  Array.isArray(list) ? list.map(normalizeOrder) : [];
