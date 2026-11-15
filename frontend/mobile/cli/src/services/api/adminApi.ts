import type {Page} from '@/types/api';
import type {AdminStats} from '@/types/models';
import {orderApi} from './orderApi';
import {productApi} from './productApi';
import {apiClient} from './client';

export const adminApi = {
  /** Dashboard numbers, put together from the admin list endpoints. */
  async getStats(): Promise<AdminStats> {
    const [users, products, orders] = await Promise.all([
      apiClient.get<Page<unknown>>('/api/users', {params: {size: 1}}),
      productApi.getAll(),
      orderApi.getAll(),
    ]);
    // Over the latest 100 orders. Revenue = money taken: paid orders (card, or cash once delivered).
    const totalRevenue = orders
      .filter(o => o.paymentStatus === 'paid')
      .reduce((sum, o) => sum + o.totalAmount, 0);
    return {
      totalUsers: users.data.page.totalElements,
      totalProducts: products.length,
      totalOrders: orders.length,
      totalRevenue,
      recentOrders: orders.slice(0, 5),
    };
  },
};
