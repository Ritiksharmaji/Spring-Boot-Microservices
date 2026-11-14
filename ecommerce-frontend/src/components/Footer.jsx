import React from 'react'
import { Link } from 'react-router-dom'
import Logo from './Logo'

const Footer = () => {
  return (
    <div>
      <div className='flex flex-col sm:grid grid-cols-[3fr_1fr_1fr] gap-14 my-10 mt-40 text-sm'>

        <div>
            <div className='mb-5'><Logo size='sm' /></div>
            <p className='w-full md:w-2/3 text-gray-600'>
            oneKampus Shop — clothing for men, women and kids, delivered across India. Pay by card or cash on
            delivery, track every order, and cancel anytime before it ships.
            </p>
        </div>

        <div>
            <p className='text-xl font-medium mb-5'>COMPANY</p>
            <ul className='flex flex-col gap-1 text-gray-600'>
                <li><Link to='/' className='hover:text-black'>Home</Link></li>
                <li><Link to='/about' className='hover:text-black'>About us</Link></li>
                <li><Link to='/collection' className='hover:text-black'>Shop</Link></li>
                <li><Link to='/contact' className='hover:text-black'>Contact</Link></li>
            </ul>
        </div>

        <div>
            <p className='text-xl font-medium mb-5'>GET IN TOUCH</p>
            <ul className='flex flex-col gap-1 text-gray-600'>
                <li><a href='tel:+918125625828' className='hover:text-black'>+91 81256 25828</a></li>
                <li><a href='mailto:ritiksharma555598@gmail.com' className='hover:text-black break-all'>ritiksharma555598@gmail.com</a></li>
            </ul>
        </div>

      </div>

        <div>
            <hr />
            <p className='py-5 text-sm text-center'>© {new Date().getFullYear()} oneKampus Shop. All rights reserved.</p>
        </div>

    </div>
  )
}

export default Footer
