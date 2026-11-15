import type {Address, ShippingAddress} from '@/types/models';
import {normalizeAddress, toApiAddress} from '@/utils/normalize';
import {apiClient} from './client';

export interface AddressInput extends ShippingAddress {
  firstName: string;
  lastName?: string;
  phone?: string;
  type?: Address['type'];
  isDefault?: boolean;
}

export const addressApi = {
  /** GET /api/addresses — default address first. */
  async getAll(): Promise<Address[]> {
    const {data} = await apiClient.get<unknown[]>('/api/addresses');
    return (Array.isArray(data) ? data : []).map(normalizeAddress);
  },

  /** POST /api/addresses */
  async create(address: AddressInput): Promise<Address> {
    const {data} = await apiClient.post('/api/addresses', {
      label: address.type ?? 'Home',
      firstName: address.firstName,
      lastName: address.lastName || null,
      phone: address.phone?.replace(/[\s-]/g, '') || null,
      ...toApiAddress(address),
      isDefault: !!address.isDefault,
    });
    return normalizeAddress(data);
  },
};
