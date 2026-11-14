import React from 'react'

/** oneKampus Store mark (shopping bag) — same artwork as onekampus.com's brand/onekampus-store-icon.svg */
export const StoreIcon = ({ className = 'w-9 h-9' }) => (
  <svg viewBox="0 0 1024 1024" className={className} aria-hidden="true">
    <rect width="1024" height="1024" rx="228" fill="#161826" />
    <g transform="translate(512 512) scale(0.857) translate(-512 -512)" fill="none" strokeWidth="65" strokeLinecap="round" strokeLinejoin="round">
      <path d="M296 400H728L760 792H264Z" stroke="#e9e9ed" />
      <path d="M416 544Q512 640 608 544" stroke="#ffc5cd" />
      <path d="M400 400V352A112 112 0 0 1 624 352V400" stroke="#e46c83" />
    </g>
  </svg>
)

/** Logo: store mark + "oneKampus Shop" wordmark */
const Logo = ({ size = 'md' }) => {
  const icon = size === 'sm' ? 'w-8 h-8' : 'w-8 h-8 sm:w-9 sm:h-9'
  const text = size === 'sm' ? 'text-lg' : 'text-[17px] sm:text-2xl'
  return (
    <span className='inline-flex items-center gap-2 select-none' aria-label='oneKampus Shop'>
      <StoreIcon className={icon} />
      <span className={`${text} font-semibold tracking-tight text-[#161826] leading-none whitespace-nowrap`}>
        oneKampus<span className='text-[#e46c83] font-medium'> Shop</span>
      </span>
    </span>
  )
}

export default Logo
