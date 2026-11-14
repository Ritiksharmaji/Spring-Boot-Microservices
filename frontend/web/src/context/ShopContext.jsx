import { createContext, useEffect, useRef, useState } from "react";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";
import { api, backendUrl, setAuthToken } from '../api/client'
import { toAddress, toAddressBody, toCartLines, toOrder, toProduct, toUser } from '../api/mappers'

export const ShopContext = createContext();

const GUEST_CART_KEY = 'guest_cart'
const GUEST_WISHLIST_KEY = 'guest_wishlist'

const readLocal = (key, fallback) => {
    try { return JSON.parse(localStorage.getItem(key)) ?? fallback } catch { return fallback }
}

const sameLine = (it, productId, size) => it.productId === productId && it.size === size
const sizeParam = (size) => (size ? { params: { size } } : undefined)

/**
 * App state + all calls to the Spring Boot API (backend/monolith).
 * Guests keep cart and wishlist in localStorage; they are merged into the account at login.
 */
const ShopContextProvider = (props) => {

    const currency = '₹';
    const delivery_fee = 0;   // free delivery
    const [search, setSearch] = useState('');
    const [showSearch, setShowSearch] = useState(false);
    const [products, setProducts] = useState([]);
    const [productsLoaded, setProductsLoaded] = useState(false);
    const [token, setTokenState] = useState(localStorage.getItem('token') || '')
    const [user, setUser] = useState(readLocal('user', null))
    const [cartItems, setCartItems] = useState(token ? [] : readLocal(GUEST_CART_KEY, []));
    const [wishlist, setWishlist] = useState([]); // product ids
    const navigate = useNavigate();

    setAuthToken(token)

    // ---------- auth ----------

    const saveSession = (newToken, newUser) => {
        localStorage.setItem('token', newToken)
        localStorage.setItem('user', JSON.stringify(newUser))
        setAuthToken(newToken)
        setUser(newUser)
        setTokenState(newToken)
    }

    // POST /api/auth/login | /api/auth/register -> { accessToken, user }
    const login = async (email, password) => {
        const { data } = await api.post('/api/auth/login', { email: email.trim().toLowerCase(), password })
        saveSession(data.accessToken, toUser(data.user))
    }

    const register = async (name, email, password) => {
        const [firstName, ...rest] = name.trim().split(/\s+/)
        const { data } = await api.post('/api/auth/register', {
            firstName, lastName: rest.join(' ') || null, email: email.trim().toLowerCase(), password,
        })
        saveSession(data.accessToken, toUser(data.user))
    }

    const logout = (redirect = true) => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setAuthToken(null)
        setTokenState('')
        setUser(null)
        setCartItems([])
        setWishlist([])
        if (redirect) navigate('/login')
    }

    // ---------- products ----------

    // GET /api/products is paged: one large page holds the whole catalogue (newest first)
    const getProductsData = async () => {
        try {
            const { data } = await api.get('/api/products', { params: { size: 500, sort: 'id,desc' } })
            setProducts(data.content.map(toProduct))
        } catch (error) {
            console.log(error)
            toast.error('Could not load products: ' + error.message)
        } finally {
            setProductsLoaded(true)
        }
    }

    const getProduct = async (id) => toProduct((await api.get(`/api/products/${id}`)).data)

    // ---------- cart ----------

    // Server updates run one at a time so the last response reflects every change
    const queue = useRef(Promise.resolve())
    const enqueue = (fn) => {
        queue.current = queue.current.then(fn).catch(async (error) => {
            toast.error(error.message)
            await getUserCart()
        })
    }
    const flushCart = () => queue.current

    const getUserCart = async () => {
        try {
            const { data } = await api.get('/api/cart')
            setCartItems(toCartLines(data))
        } catch (error) {
            console.log(error)
            if (error.status === 401) logout()
        }
    }

    const addToCart = async (productId, size) => {
        const product = products.find((p) => p._id === productId)
        if (product?.sizes?.length && !size) {
            toast.error('Select Product Size');
            return;
        }
        const lineSize = size || ''

        setCartItems((prev) => {
            const existing = prev.find((it) => sameLine(it, productId, lineSize))
            if (existing) return prev.map((it) => it === existing ? { ...it, quantity: it.quantity + 1 } : it)
            return [...prev, { productId, size: lineSize, quantity: 1, price: product?.price ?? 0 }]
        })
        toast.success('Added to cart')

        if (token) {
            enqueue(async () => {
                const { data } = await api.post('/api/cart/items', { productId, quantity: 1, size: lineSize || null })
                setCartItems(toCartLines(data))
            })
        }
    }

    // quantity 0 removes the line
    const updateQuantity = async (productId, size, quantity) => {
        if (quantity <= 0) {
            setCartItems((prev) => prev.filter((it) => !sameLine(it, productId, size)))
            if (token) {
                enqueue(async () => {
                    await api.delete(`/api/cart/items/${productId}`, sizeParam(size))
                    await getUserCart()
                })
            }
            return
        }

        setCartItems((prev) => prev.map((it) => sameLine(it, productId, size) ? { ...it, quantity } : it))
        if (token) {
            enqueue(async () => {
                const { data } = await api.put(`/api/cart/items/${productId}`, { quantity }, sizeParam(size))
                setCartItems(toCartLines(data))
            })
        }
    }

    const clearCart = () => {
        setCartItems([])
        if (token) enqueue(async () => { await api.delete('/api/cart') })
    }

    const getCartCount = () => cartItems.reduce((sum, it) => sum + it.quantity, 0)

    const getCartAmount = () => cartItems.reduce((sum, it) => sum + it.price * it.quantity, 0)

    // ---------- wishlist ----------

    const getWishlist = async () => {
        try {
            const { data } = await api.get('/api/wishlist')
            setWishlist(data.productIds)
        } catch (error) {
            console.log(error)
        }
    }

    const isInWishlist = (productId) => wishlist.includes(productId)

    const toggleWishlist = async (productId) => {
        const previous = wishlist
        const remove = previous.includes(productId)
        setWishlist(remove ? previous.filter((id) => id !== productId) : [...previous, productId])
        if (!token) return
        try {
            const { data } = remove ? await api.delete(`/api/wishlist/${productId}`) : await api.put(`/api/wishlist/${productId}`)
            setWishlist(data.productIds)
        } catch (error) {
            setWishlist(previous)
            toast.error(error.message)
        }
    }

    // ---------- addresses, orders, payments ----------

    const getAddresses = async () => (await api.get('/api/addresses')).data.map(toAddress)
    const saveAddress = async (form) => form._id
        ? api.put(`/api/addresses/${form._id}`, toAddressBody(form))
        : api.post('/api/addresses', toAddressBody(form))
    const deleteAddress = async (id) => api.delete(`/api/addresses/${id}`)

    // POST /api/orders: the order is built from the server cart (stock reserved in the same transaction)
    const placeOrder = async ({ address, paymentMethod, notes }) => {
        const { data } = await api.post('/api/orders', {
            paymentMethod,
            notes: notes || null,
            shippingAddress: {
                firstName: address.firstName, lastName: address.lastName || null, phone: address.phone || null,
                street: address.street, city: address.city, state: address.state || null,
                zipcode: address.zipCode, country: address.country,
            },
        })
        return toOrder(data)
    }

    const getOrders = async () => (await api.get('/api/orders', { params: { size: 50 } })).data.content.map(toOrder)
    const cancelOrder = async (id) => toOrder((await api.post(`/api/orders/${id}/cancel`)).data)

    // ---------- effects ----------

    useEffect(() => {
        getProductsData()
        if (!token) setWishlist(readLocal(GUEST_WISHLIST_KEY, []))
    }, [])

    // Guest cart/wishlist live in localStorage
    useEffect(() => {
        if (!token) localStorage.setItem(GUEST_CART_KEY, JSON.stringify(cartItems))
    }, [cartItems, token])

    useEffect(() => {
        if (!token) localStorage.setItem(GUEST_WISHLIST_KEY, JSON.stringify(wishlist))
    }, [wishlist, token])

    // On login (or page load while logged in): refresh profile, move guest items into the account, load cart + wishlist
    useEffect(() => {
        if (!token) return
        const sync = async () => {
            try {
                const { data } = await api.get('/api/users/me')
                const me = toUser(data)
                setUser(me)
                localStorage.setItem('user', JSON.stringify(me))
            } catch (error) {
                if (error.status === 401) {
                    toast.info('Your session expired, please log in again')
                    return logout()
                }
            }

            const guestCart = readLocal(GUEST_CART_KEY, [])
            for (const it of guestCart) {
                await api.post('/api/cart/items', { productId: it.productId, quantity: it.quantity, size: it.size || null }).catch(() => {})
            }
            localStorage.removeItem(GUEST_CART_KEY)

            const guestWishlist = readLocal(GUEST_WISHLIST_KEY, [])
            for (const productId of guestWishlist) {
                await api.put(`/api/wishlist/${productId}`).catch(() => {})
            }
            localStorage.removeItem(GUEST_WISHLIST_KEY)

            await Promise.all([getUserCart(), getWishlist()])
        }
        sync()
    }, [token])

    const value = {
        api, backendUrl, navigate,
        products, productsLoaded, getProduct, currency, delivery_fee,
        search, setSearch, showSearch, setShowSearch,
        token, user, login, register, logout,
        cartItems, addToCart, updateQuantity, clearCart, flushCart, getUserCart,
        getCartCount, getCartAmount,
        wishlist, isInWishlist, toggleWishlist,
        getAddresses, saveAddress, deleteAddress,
        placeOrder, getOrders, cancelOrder,
    }

    return (
        <ShopContext.Provider value={value}>
            {props.children}
        </ShopContext.Provider>
    )

}

export default ShopContextProvider;
