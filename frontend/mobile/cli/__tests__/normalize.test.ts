import {
  normalizeAddress,
  normalizeOrder,
  normalizeProduct,
  normalizeProducts,
  normalizeUser,
  resolveImageUrl,
  splitName,
  toApiAddress,
} from '../src/utils/normalize';
import {formatPrice, parseList} from '../src/utils/format';
import {shippingFor} from '../src/constants/app';
import {NEXT_ORDER_STATUSES} from '../src/constants/orderStatus';

jest.mock('../src/config/env', () => ({env: {websiteUrl: 'https://shop.example'}}));

describe('normalizeProduct (Spring API product)', () => {
  it('maps API fields to the app model', () => {
    const p = normalizeProduct({
      id: 13,
      name: 'Top',
      price: 1000.0,
      stockQuantity: 500,
      category: 'Women',
      subCategory: 'Topwear',
      bestseller: true,
      imageUrl: '/products/p_img1.png',
      images: ['/products/p_img1.png', 'https://cdn.example/x.png'],
      active: true,
    });
    expect(p).toMatchObject({
      _id: '13',
      price: 1000,
      stock: 500,
      category: 'Women',
      subCategory: 'Topwear',
      isFeatured: true,
      isActive: true,
      images: ['https://shop.example/products/p_img1.png', 'https://cdn.example/x.png'],
    });
  });

  it('falls back to imageUrl and fills defaults', () => {
    const p = normalizeProduct({id: 1, name: 'Cap', price: '9.5', imageUrl: 'https://a/b.png'});
    expect(p).toMatchObject({price: 9.5, images: ['https://a/b.png'], sizes: [], stock: 0});
  });

  it('drops null entries', () => {
    expect(normalizeProducts([null, {id: 3}])).toHaveLength(1);
    expect(normalizeProducts(undefined)).toEqual([]);
  });
});

describe('users and addresses', () => {
  it('joins the name and maps the role', () => {
    expect(
      normalizeUser({id: 7, firstName: 'Asha', lastName: 'Rao', email: 'a@x.in', role: 'ADMIN'}),
    ).toMatchObject({_id: '7', name: 'Asha Rao', role: 'admin'});
    expect(normalizeUser({id: 8, firstName: 'Ravi', role: 'CUSTOMER'})).toMatchObject({
      name: 'Ravi',
      role: 'user',
    });
  });

  it('splits a full name', () => {
    expect(splitName('  Ritik Kumar Sharma ')).toEqual({
      firstName: 'Ritik',
      lastName: 'Kumar Sharma',
    });
    expect(splitName('Ritik')).toEqual({firstName: 'Ritik', lastName: null});
  });

  it('maps zipcode <-> zipCode and label <-> type', () => {
    const a = normalizeAddress({id: 2, label: 'Work', zipcode: '500001', street: 'MG Road'});
    expect(a).toMatchObject({_id: '2', type: 'Work', zipCode: '500001', street: 'MG Road'});
    expect(
      toApiAddress({street: ' S ', city: 'C', state: '', zipCode: '500001', country: 'India'}),
    ).toEqual({street: 'S', city: 'C', state: null, zipcode: '500001', country: 'India'});
  });
});

describe('normalizeOrder (Spring API order)', () => {
  const order = normalizeOrder({
    id: 42,
    userId: 7,
    status: 'CONFIRMED',
    statusReason: 'Payment received',
    totalAmount: 2400.0,
    paymentMethod: 'CARD',
    paymentStatus: 'CASH_ON_DELIVERY',
    recipientName: 'Asha Rao',
    items: [
      {
        productId: 13,
        productName: 'Top',
        size: 'M',
        imageUrl: '/p.png',
        quantity: 2,
        unitPrice: 1000,
        subTotal: 2000,
      },
      {
        productId: 14,
        productName: 'Cap',
        size: null,
        imageUrl: null,
        quantity: 1,
        unitPrice: 400,
        subTotal: 400,
      },
    ],
    shippingAddress: {
      street: 'MG Road',
      city: 'Hyderabad',
      state: 'TS',
      country: 'India',
      zipcode: '500001',
    },
    createdAt: '2026-11-15T10:00:00',
  });

  it('lower-cases statuses and maps the payment method', () => {
    expect(order.orderStatus).toBe('confirmed');
    expect(order.paymentStatus).toBe('cash on delivery');
    expect(order.paymentMethod).toBe('card');
    expect(order.orderNumber).toBe('42');
  });

  it('maps items, totals and address', () => {
    expect(order.items[0]).toMatchObject({
      name: 'Top',
      quantity: 2,
      price: 1000,
      size: 'M',
      product: {_id: '13', images: ['https://shop.example/p.png']},
    });
    expect(order.items[1].product.images).toEqual([]);
    expect(order.subtotal).toBe(2400);
    expect(order.totalAmount).toBe(2400);
    expect(order.shippingAddress.zipCode).toBe('500001');
    expect(order.user).toEqual({_id: '7', name: 'Asha Rao'});
  });

  it('only offers the transitions the API allows', () => {
    expect(NEXT_ORDER_STATUSES.pending).toEqual(['confirmed', 'cancelled']);
    expect(NEXT_ORDER_STATUSES.shipped).toEqual(['delivered']);
    expect(NEXT_ORDER_STATUSES.delivered).toEqual([]);
  });
});

describe('helpers', () => {
  it('formats prices in rupees and parses lists', () => {
    expect(formatPrice(12.5)).toBe('₹12.50');
    expect(parseList(' S, M ,,L ')).toEqual(['S', 'M', 'L']);
  });

  it('resolves site-relative images and has free delivery', () => {
    expect(resolveImageUrl('/products/a.png')).toBe('https://shop.example/products/a.png');
    expect(resolveImageUrl('https://x/y.png')).toBe('https://x/y.png');
    expect(shippingFor(0)).toBe(0);
    expect(shippingFor(999)).toBe(0);
  });
});
