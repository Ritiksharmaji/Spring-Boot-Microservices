import React, { useContext, useEffect, useRef, useState } from 'react'
import { ShopContext } from '../context/ShopContext'
import Title from '../components/Title';
import { toast } from 'react-toastify';

const STATUS_COLORS = {
  PENDING: 'bg-yellow-500',
  CONFIRMED: 'bg-indigo-500',
  SHIPPED: 'bg-purple-500',
  DELIVERED: 'bg-green-500',
  CANCELLED: 'bg-red-500',
}

const STEPS = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED']

const PAYMENT_LABELS = {
  PENDING: 'processing',
  PAID: 'paid',
  FAILED: 'declined',
  CASH_ON_DELIVERY: 'pay on delivery',
  REFUNDED: 'refunded',
  NOT_CHARGED: 'not charged',
}

const Orders = () => {

  const { token, currency, navigate, getOrders, cancelOrder } = useContext(ShopContext);

  const [orders,setOrders] = useState([])
  const [loading,setLoading] = useState(true)
  const poll = useRef(null)

  const loadOrderData = async () => {
    try {
      setOrders(await getOrders())
    } catch (error) {
      console.log(error)
      toast.error(error.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(()=>{
    if (!token) {
      navigate('/login')
      return
    }
    loadOrderData()
  },[token])

  // While a payment is being processed, refresh every 1.5 s so the status changes on its own
  useEffect(() => {
    clearInterval(poll.current)
    if (orders.some((o) => o.status === 'PENDING')) {
      poll.current = setInterval(loadOrderData, 1500)
    }
    return () => clearInterval(poll.current)
  }, [orders])

  const onCancel = async (order) => {
    if (!window.confirm(`Cancel order #${order.orderNumber}?`)) return
    try {
      await cancelOrder(order._id)
      toast.success(order.paymentStatus === 'PAID' ? 'Order cancelled — your payment will be refunded' : 'Order cancelled')
      loadOrderData()
    } catch (error) {
      toast.error(error.message)
    }
  }

  return (
    <div className='border-t pt-16'>

        <div className='text-2xl'>
            <Title text1={'MY'} text2={'ORDERS'}/>
        </div>

        {!loading && orders.length === 0 && <p className='py-10 text-gray-500'>You have no orders yet.</p>}

        <div className='flex flex-col gap-6'>
            {
              orders.map((order) => (
                <div key={order._id} className='border text-gray-700'>
                    {/* Order header */}
                    <div className='flex flex-wrap justify-between gap-2 bg-gray-50 px-4 py-3 text-sm'>
                        <div>
                          <p className='font-medium'>Order #{order.orderNumber}</p>
                          <p className='text-gray-500'>{new Date(order.createdAt).toDateString()}</p>
                        </div>
                        <div className='text-right'>
                          <p className='font-medium'>{currency}{order.totalAmount.toFixed(2)}</p>
                          <p className='text-gray-500'>{order.paymentMethod === 'CASH' ? 'Cash on delivery' : 'Card'} · Payment {PAYMENT_LABELS[order.paymentStatus] || order.paymentStatus}</p>
                        </div>
                    </div>

                    {/* Items */}
                    {order.items.map((item) => (
                      <div key={item._id} className='flex items-start gap-6 text-sm px-4 py-3 border-t'>
                          <img onClick={() => navigate(`/product/${item.productId}`)} className='w-16 sm:w-20 cursor-pointer' src={item.image} alt="" />
                          <div>
                            <p className='sm:text-base font-medium'>{item.name}</p>
                            <div className='flex flex-wrap items-center gap-3 mt-1 text-base text-gray-700'>
                              <p>{currency}{item.price}</p>
                              <p>Quantity: {item.quantity}</p>
                              {item.size && <p>Size: {item.size}</p>}
                            </div>
                          </div>
                      </div>
                    ))}

                    {/* Status / tracking */}
                    <div className='flex flex-col md:flex-row md:items-center md:justify-between gap-3 px-4 py-3 border-t text-sm'>
                        <div>
                          <div className='flex items-center gap-2'>
                              <p className={`min-w-2 h-2 rounded-full ${STATUS_COLORS[order.status] || 'bg-gray-400'}`}></p>
                              <p className='capitalize'>{order.status.toLowerCase()}</p>
                              {order.status !== 'CANCELLED' && (
                                <div className='hidden sm:flex items-center gap-1 ml-4 text-xs text-gray-400'>
                                  {STEPS.map((step, i) => (
                                    <span key={step} className={i <= STEPS.indexOf(order.status) ? 'text-black' : ''}>{step.toLowerCase()}{i < STEPS.length - 1 ? ' →' : ''}</span>
                                  ))}
                                </div>
                              )}
                          </div>
                          {order.statusReason && <p className='text-xs text-gray-500 mt-1'>{order.statusReason}</p>}
                        </div>
                        {order.shippingAddress && (
                          <p className='text-gray-500'>Ship to: {order.recipientName ? order.recipientName + ', ' : ''}{order.shippingAddress.street}, {order.shippingAddress.city}, {order.shippingAddress.zipCode}</p>
                        )}
                        <div className='flex gap-2'>
                          {['PENDING', 'CONFIRMED'].includes(order.status) && (
                            <button onClick={() => onCancel(order)} className='border px-4 py-2 text-sm font-medium rounded-sm'>Cancel</button>
                          )}
                          <button onClick={loadOrderData} className='border px-4 py-2 text-sm font-medium rounded-sm'>Track Order</button>
                        </div>
                    </div>
                </div>
              ))
            }
        </div>
    </div>
  )
}

export default Orders
