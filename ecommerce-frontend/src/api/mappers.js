// Maps the Spring Boot API's JSON to the shapes the UI components use (and back).

// API product -> UI product (components use _id, images, stock, isFeatured)
export const toProduct = (p) => ({
  _id: p.id,
  name: p.name,
  description: p.description || '',
  price: Number(p.price),
  images: p.images?.length ? p.images : p.imageUrl ? [p.imageUrl] : [],
  sizes: p.sizes || [],
  stock: p.stockQuantity ?? 0,
  category: p.category || '',
  subCategory: p.subCategory || '',
  isFeatured: !!p.bestseller,
  createdAt: p.createdAt,
})

// API cart -> UI cart lines { productId, size, quantity, price }
export const toCartLines = (cart) => (cart?.items ?? []).map((it) => ({
  productId: it.productId,
  size: it.size || '',
  quantity: it.quantity,
  price: Number(it.unitPrice),
}))

export const toUser = (u) => u && ({
  ...u,
  _id: u.id,
  name: [u.firstName, u.lastName].filter(Boolean).join(' '),
})

// API address <-> UI address form ({ _id, type, zipCode, ... })
export const toAddress = (a) => ({
  _id: a.id,
  type: a.label || 'Home',
  firstName: a.firstName || '',
  lastName: a.lastName || '',
  phone: a.phone || '',
  street: a.street,
  city: a.city,
  state: a.state || '',
  zipCode: a.zipcode,
  country: a.country,
  isDefault: !!a.isDefault,
})

export const toAddressBody = (f) => ({
  label: f.type || 'Home',
  firstName: f.firstName,
  lastName: f.lastName || null,
  phone: f.phone || null,
  street: f.street,
  city: f.city,
  state: f.state || null,
  zipcode: f.zipCode,
  country: f.country,
  isDefault: !!f.isDefault,
})

// API order -> UI order
export const toOrder = (o) => ({
  _id: o.id,
  orderNumber: o.id,
  status: o.status,                       // PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
  statusReason: o.statusReason,
  totalAmount: Number(o.totalAmount),
  paymentMethod: o.paymentMethod,         // CARD | CASH
  paymentStatus: o.paymentStatus,         // PENDING | PAID | FAILED | CASH_ON_DELIVERY | REFUNDED | NOT_CHARGED
  items: o.items.map((i, index) => ({
    _id: `${o.id}-${index}`,
    productId: i.productId,
    name: i.productName,
    size: i.size,
    image: i.imageUrl,
    price: Number(i.unitPrice),
    quantity: i.quantity,
  })),
  recipientName: o.recipientName,
  shippingAddress: o.shippingAddress ? { ...o.shippingAddress, zipCode: o.shippingAddress.zipcode } : null,
  notes: o.notes,
  createdAt: o.createdAt,
})
