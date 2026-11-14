import React, { useContext, useEffect, useState } from 'react'
import Title from '../components/Title'
import CartTotal from '../components/CartTotal'
import { ShopContext } from '../context/ShopContext'
import { toast } from 'react-toastify'

const EMPTY_ADDRESS = { firstName: '', lastName: '', phone: '', street: '', city: '', state: '', zipCode: '', country: 'India' }

const PlaceOrder = () => {

    const [method, setMethod] = useState('CARD');
    const { navigate, token, user, cartItems, clearCart, flushCart, getAddresses, saveAddress, placeOrder, getUserCart } = useContext(ShopContext);
    const [formData, setFormData] = useState(EMPTY_ADDRESS)
    const [notes, setNotes] = useState('')
    const [hasSavedAddress, setHasSavedAddress] = useState(false)
    const [loading, setLoading] = useState(false)

    const onChangeHandler = (event) => {
        const { name, value } = event.target
        setFormData(data => ({ ...data, [name]: value }))
    }

    // Login is required to order; prefill the default saved address (default comes first)
    useEffect(() => {
        if (!token) {
            toast.info('Please login to place an order')
            navigate('/login')
            return
        }
        getAddresses()
            .then((addresses) => {
                const saved = addresses[0]
                if (saved) {
                    const { firstName, lastName, phone, street, city, state, zipCode, country } = saved
                    setFormData({ firstName, lastName, phone, street, city, state, zipCode, country })
                    setHasSavedAddress(true)
                } else if (user) {
                    setFormData(data => ({ ...data, firstName: user.firstName || '', lastName: user.lastName || '', phone: user.phone || '' }))
                }
            })
            .catch(() => {})
    }, [token])

    const onSubmitHandler = async (event) => {
        event.preventDefault()
        if (cartItems.length === 0) {
            toast.error('Your cart is empty')
            return
        }
        setLoading(true)
        try {
            // The order is built from the server cart, so wait for pending cart updates first
            await flushCart()
            const order = await placeOrder({ address: formData, paymentMethod: method, notes })

            // First order: remember the address for next time
            if (!hasSavedAddress) {
                await saveAddress({ type: 'Home', ...formData, isDefault: true }).catch(() => {})
            }

            clearCart()
            toast.success(method === 'CASH'
                ? `Order #${order.orderNumber} placed — pay on delivery`
                : `Order #${order.orderNumber} placed — processing your payment`)
            navigate('/orders')
        } catch (error) {
            console.log(error)
            toast.error(error.message)
            if (error.status === 409) getUserCart()   // stock changed: show the real cart
        } finally {
            setLoading(false)
        }
    }

    const input = 'border border-gray-300 rounded py-1.5 px-3.5 w-full'

    return (
        <form onSubmit={onSubmitHandler} className='flex flex-col sm:flex-row justify-between gap-4 pt-5 sm:pt-14 min-h-[80vh] border-t'>
            {/* ------------- Left Side ---------------- */}
            <div className='flex flex-col gap-4 w-full sm:max-w-[480px]'>

                <div className='text-xl sm:text-2xl my-3'>
                    <Title text1={'DELIVERY'} text2={'INFORMATION'} />
                </div>
                <div className='flex gap-3'>
                    <input required onChange={onChangeHandler} name='firstName' value={formData.firstName} className={input} type="text" placeholder='First name' />
                    <input onChange={onChangeHandler} name='lastName' value={formData.lastName} className={input} type="text" placeholder='Last name' />
                </div>
                <input onChange={onChangeHandler} name='phone' value={formData.phone} className={input} type="tel" placeholder='Phone (e.g. +919876543210)' pattern='\+?[0-9]{7,15}' title='7-15 digits, optional +' />
                <input required onChange={onChangeHandler} name='street' value={formData.street} className={input} type="text" placeholder='Street' />
                <div className='flex gap-3'>
                    <input required onChange={onChangeHandler} name='city' value={formData.city} className={input} type="text" placeholder='City' />
                    <input onChange={onChangeHandler} name='state' value={formData.state} className={input} type="text" placeholder='State' />
                </div>
                <div className='flex gap-3'>
                    <input required onChange={onChangeHandler} name='zipCode' value={formData.zipCode} className={input} type="text" placeholder='Zip code' />
                    <input required onChange={onChangeHandler} name='country' value={formData.country} className={input} type="text" placeholder='Country' />
                </div>
                <textarea onChange={(e) => setNotes(e.target.value)} value={notes} maxLength={500} className={input} rows={3} placeholder='Delivery notes (optional)' />
                {hasSavedAddress && <p className='text-xs text-gray-500'>Filled in from your saved address. Manage addresses in <span onClick={() => navigate('/profile')} className='underline cursor-pointer'>My Profile</span>.</p>}
            </div>

            {/* ------------- Right Side ------------------ */}
            <div className='mt-8'>

                <div className='mt-8 min-w-80'>
                    <CartTotal />
                </div>

                <div className='mt-12'>
                    <Title text1={'PAYMENT'} text2={'METHOD'} />
                    {/* --------------- Payment Method Selection ------------- */}
                    <div className='flex gap-3 flex-col lg:flex-row'>
                        <div onClick={() => setMethod('CARD')} className='flex items-center gap-3 border p-2 px-3 cursor-pointer'>
                            <p className={`min-w-3.5 h-3.5 border rounded-full ${method === 'CARD' ? 'bg-green-400' : ''}`}></p>
                            <p className='text-gray-500 text-sm font-medium mx-4'>CARD (DEMO)</p>
                        </div>
                        <div onClick={() => setMethod('CASH')} className='flex items-center gap-3 border p-2 px-3 cursor-pointer'>
                            <p className={`min-w-3.5 h-3.5 border rounded-full ${method === 'CASH' ? 'bg-green-400' : ''}`}></p>
                            <p className='text-gray-500 text-sm font-medium mx-4'>CASH ON DELIVERY</p>
                        </div>
                    </div>
                    {method === 'CARD' && (
                        <p className='text-xs text-gray-500 mt-3 max-w-md'>
                            Demo card: no real payment. Orders up to ₹50,000 are approved; larger ones are declined
                            and the order is cancelled automatically (stock is returned).
                        </p>
                    )}

                    <div className='w-full text-end mt-8'>
                        <button disabled={loading} type='submit' className='bg-black text-white px-16 py-3 text-sm disabled:bg-gray-400'>{loading ? 'PLACING ORDER...' : 'PLACE ORDER'}</button>
                    </div>
                </div>
            </div>
        </form>
    )
}

export default PlaceOrder
