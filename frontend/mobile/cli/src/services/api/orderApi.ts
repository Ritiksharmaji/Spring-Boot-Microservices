import type {Page} from '@/types/api';
import type {Order, OrderStatus, PaymentMethod, ShippingAddress} from '@/types/models';
import {normalizeOrder, normalizeOrders, splitName, toApiAddress} from '@/utils/normalize';
import {apiClient} from './client';

export interface CreateOrderInput {
  /** Recipient name and phone for the delivery. */
  recipientName: string;
  recipientPhone?: string;
  shippingAddress: ShippingAddress;
  paymentMethod: PaymentMethod;
  notes?: string;
}

export const orderApi = {
  /** GET /api/orders — the signed-in user's orders, newest first. */
  async getMine(): Promise<Order[]> {
    const {data} = await apiClient.get<Page<unknown>>('/api/orders', {
      params: {size: 100, sort: 'id,desc'},
    });
    return normalizeOrders(data.content);
  },

  /** GET /api/orders/:id */
  async getById(id: string): Promise<Order> {
    const {data} = await apiClient.get(`/api/orders/${id}`);
    return normalizeOrder(data);
  },

  /**
   * POST /api/orders — built from the server cart, which the API then empties. Card payments are
   * simulated after the order is saved (approved up to ₹50,000), so the order comes back PENDING.
   */
  async create(input: CreateOrderInput): Promise<Order> {
    const {firstName, lastName} = splitName(input.recipientName);
    const {data} = await apiClient.post('/api/orders', {
      shippingAddress: {
        firstName,
        lastName,
        phone: input.recipientPhone?.replace(/[\s-]/g, '') || null,
        ...toApiAddress(input.shippingAddress),
      },
      paymentMethod: input.paymentMethod === 'cash' ? 'CASH' : 'CARD',
      notes: input.notes?.trim() || null,
    });
    return normalizeOrder(data);
  },

  /** POST /api/orders/:id/cancel — while PENDING or CONFIRMED (stock returned, card refunded). */
  async cancel(id: string): Promise<Order> {
    const {data} = await apiClient.post(`/api/orders/${id}/cancel`);
    return normalizeOrder(data);
  },

  /** GET /api/orders/admin (admin) — all orders, newest first. */
  async getAll(): Promise<Order[]> {
    const {data} = await apiClient.get<Page<unknown>>('/api/orders/admin', {
      params: {size: 100, sort: 'id,desc'},
    });
    return normalizeOrders(data.content);
  },

  /** PATCH /api/orders/:id/status (admin). PENDING → CONFIRMED → SHIPPED → DELIVERED, or CANCELLED. */
  async updateStatus(id: string, status: OrderStatus): Promise<Order> {
    const {data} = await apiClient.patch(`/api/orders/${id}/status`, {
      status: status.toUpperCase(),
    });
    return normalizeOrder(data);
  },
};
