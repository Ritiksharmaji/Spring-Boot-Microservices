import React from 'react'
import { Link } from 'react-router-dom'

const NotFound = () => (
  <div className='border-t pt-24 pb-32 text-center'>
    <p className='text-5xl font-medium'>404</p>
    <p className='mt-4 text-gray-500'>This page does not exist.</p>
    <Link to='/' className='inline-block mt-8 bg-black text-white px-8 py-3 text-sm'>BACK TO SHOP</Link>
  </div>
)

export default NotFound
