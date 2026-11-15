import React from 'react'
import Title from '../components/Title'

const CONTACT_EMAIL = 'ritiksharma555598@gmail.com'

const Section = ({ heading, children }) => (
  <section className='mb-8'>
    <h2 className='text-lg font-medium text-gray-800 mb-2'>{heading}</h2>
    <div className='flex flex-col gap-2'>{children}</div>
  </section>
)

// Linked from the footer and from the mobile app (Profile → Privacy Policy).
const Privacy = () => {
  return (
    <div className='border-t pt-8 max-w-3xl text-gray-600 text-sm leading-6'>
      <div className='text-2xl mb-2'>
        <Title text1={'PRIVACY'} text2={'POLICY'} />
      </div>
      <p className='mb-8 text-xs text-gray-400'>Last updated: 15 November 2026</p>

      <Section heading='Who we are'>
        <p>oneKampus Shop (shop.onekampus.com and the oneKampus Shop mobile app) is an online clothing store run by oneKampus. This page explains what we collect when you use the shop and what we do with it.</p>
      </Section>

      <Section heading='What we collect'>
        <ul className='list-disc pl-5'>
          <li><b>Account:</b> your name, email address and password. The password is stored only as a one-way hash.</li>
          <li><b>Profile and delivery:</b> phone number and the delivery addresses you save or enter at checkout.</li>
          <li><b>Shopping:</b> your cart, wishlist and orders (products, sizes, amounts, status and notes).</li>
          <li><b>Payments:</b> the payment method you choose and the payment status. Card payments are a demo: no card number is asked for or stored.</li>
        </ul>
        <p>We do not use advertising or analytics trackers, and we do not sell or share your data with third parties.</p>
      </Section>

      <Section heading='How we use it'>
        <p>Only to run the shop: to sign you in, keep your cart and wishlist, deliver your orders, and email you about your orders (received, confirmed, shipped, delivered or cancelled).</p>
      </Section>

      <Section heading='Where it is stored'>
        <p>On our server in a PostgreSQL database. The connection between your browser or app and the shop is encrypted (HTTPS). The mobile app keeps your sign-in token in the device's secure keychain / keystore.</p>
      </Section>

      <Section heading='Deleting your account'>
        <p>In the mobile app open <b>Profile → Delete Account</b> and confirm with your password. You can also email <a className='underline' href={`mailto:${CONTACT_EMAIL}?subject=Delete%20my%20oneKampus%20Shop%20account`}>{CONTACT_EMAIL}</a> from the address you registered with.</p>
        <p>Deleting removes your name, email, phone, saved addresses, cart and wishlist. Past orders are kept, because they are needed for accounting and for delivering orders that are still on their way: their items, amounts and the delivery details entered for them (recipient and address) stay, while your account name and email are removed from them.</p>
      </Section>

      <Section heading='Contact'>
        <p>Questions about your data: <a className='underline' href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a> or <a className='underline' href='tel:+918125625828'>+91 81256 25828</a>.</p>
      </Section>
    </div>
  )
}

export default Privacy
