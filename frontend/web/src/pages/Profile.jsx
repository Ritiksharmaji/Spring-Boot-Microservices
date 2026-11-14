import React, { useContext, useEffect, useState } from 'react'
import { ShopContext } from '../context/ShopContext'
import Title from '../components/Title'
import { toast } from 'react-toastify'

const EMPTY = { type: 'Home', firstName: '', lastName: '', phone: '', street: '', city: '', state: '', zipCode: '', country: 'India', isDefault: false }

// Account details + address book (GET/POST/PUT/DELETE /api/addresses through ShopContext)
const Profile = () => {

  const { token, user, navigate, logout, getAddresses, saveAddress: storeAddress, deleteAddress: removeAddress } = useContext(ShopContext)
  const [addresses, setAddresses] = useState([])
  const [form, setForm] = useState(null) // null = closed, otherwise the address being added/edited
  const [saving, setSaving] = useState(false)

  const loadAddresses = async () => {
    try {
      setAddresses(await getAddresses())
    } catch (error) {
      toast.error(error.message)
    }
  }

  useEffect(() => {
    if (!token) {
      navigate('/login')
      return
    }
    loadAddresses()
  }, [token])

  const onChange = (e) => {
    const { name, value, type, checked } = e.target
    setForm((f) => ({ ...f, [name]: type === 'checkbox' ? checked : value }))
  }

  const saveAddress = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      await storeAddress(form)
      toast.success('Address saved')
      setForm(null)
      loadAddresses()
    } catch (error) {
      toast.error(error.message)
    } finally {
      setSaving(false)
    }
  }

  const deleteAddress = async (id) => {
    if (!window.confirm('Delete this address?')) return
    try {
      await removeAddress(id)
      loadAddresses()   // the next address may have become the default
    } catch (error) {
      toast.error(error.message)
    }
  }

  const makeDefault = async (address) => {
    try {
      await storeAddress({ ...address, isDefault: true })
      loadAddresses()
    } catch (error) {
      toast.error(error.message)
    }
  }

  if (!user) return null

  return (
    <div className='border-t pt-14 mb-20'>
      <div className='text-2xl mb-6'>
        <Title text1={'MY'} text2={'PROFILE'} />
      </div>

      {/* Account */}
      <div className='border p-5 flex flex-wrap justify-between items-center gap-4'>
        <div>
          <p className='text-lg font-medium'>{user.name}</p>
          <p className='text-gray-500'>{user.email}</p>
        </div>
        <div className='flex gap-3 text-sm'>
          <button onClick={() => navigate('/orders')} className='border px-4 py-2'>My Orders</button>
          <button onClick={() => navigate('/wishlist')} className='border px-4 py-2'>Wishlist</button>
          <button onClick={logout} className='border px-4 py-2 text-red-500'>Logout</button>
        </div>
      </div>

      {/* Addresses */}
      <div className='flex justify-between items-center mt-10 mb-4'>
        <p className='text-xl'>Saved Addresses</p>
        {!form && <button onClick={() => setForm({ ...EMPTY, firstName: user.firstName || '', lastName: user.lastName || '', phone: user.phone || '', isDefault: addresses.length === 0 })} className='bg-black text-white text-sm px-5 py-2'>+ ADD ADDRESS</button>}
      </div>

      {form && (
        <form onSubmit={saveAddress} className='border p-5 mb-6 flex flex-col gap-3 sm:max-w-[520px]'>
          <p className='font-medium'>{form._id ? 'Edit address' : 'New address'}</p>
          <select name='type' value={form.type} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5'>
            <option>Home</option>
            <option>Work</option>
            <option>Other</option>
          </select>
          <div className='flex gap-3'>
            <input required name='firstName' value={form.firstName} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5 w-full' placeholder='First name' />
            <input name='lastName' value={form.lastName} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5 w-full' placeholder='Last name' />
          </div>
          <input name='phone' value={form.phone} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5' type='tel' pattern='\+?[0-9]{7,15}' title='7-15 digits, optional +' placeholder='Phone' />
          <input required name='street' value={form.street} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5' placeholder='Street' />
          <div className='flex gap-3'>
            <input required name='city' value={form.city} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5 w-full' placeholder='City' />
            <input name='state' value={form.state} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5 w-full' placeholder='State' />
          </div>
          <div className='flex gap-3'>
            <input required name='zipCode' value={form.zipCode} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5 w-full' placeholder='Zip code' />
            <input required name='country' value={form.country} onChange={onChange} className='border border-gray-300 rounded py-1.5 px-3.5 w-full' placeholder='Country' />
          </div>
          <label className='flex items-center gap-2 text-sm'>
            <input type='checkbox' name='isDefault' checked={form.isDefault} onChange={onChange} /> Use as default address
          </label>
          <div className='flex gap-3'>
            <button disabled={saving} className='bg-black text-white text-sm px-6 py-2 disabled:bg-gray-400'>{saving ? 'SAVING...' : 'SAVE'}</button>
            <button type='button' onClick={() => setForm(null)} className='border text-sm px-6 py-2'>CANCEL</button>
          </div>
        </form>
      )}

      {addresses.length === 0 && !form && <p className='text-gray-500'>No saved addresses yet. Your first order's address is saved automatically.</p>}

      <div className='grid sm:grid-cols-2 gap-4'>
        {addresses.map((a) => (
          <div key={a._id} className={`border p-4 text-sm ${a.isDefault ? 'border-black' : ''}`}>
            <div className='flex justify-between mb-2'>
              <p className='font-medium'>{a.type}{a.isDefault && <span className='ml-2 text-xs bg-black text-white px-2 py-0.5'>DEFAULT</span>}</p>
              <div className='flex gap-3 text-gray-500'>
                {!a.isDefault && <button onClick={() => makeDefault(a)} className='hover:text-black'>Set default</button>}
                <button onClick={() => setForm(a)} className='hover:text-black'>Edit</button>
                <button onClick={() => deleteAddress(a._id)} className='hover:text-red-500'>Delete</button>
              </div>
            </div>
            <p className='text-gray-600'>{[a.firstName, a.lastName].filter(Boolean).join(' ')}{a.phone ? ' · ' + a.phone : ''}</p>
            <p className='text-gray-600'>{a.street}</p>
            <p className='text-gray-600'>{a.city}, {a.state} {a.zipCode}</p>
            <p className='text-gray-600'>{a.country}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Profile
