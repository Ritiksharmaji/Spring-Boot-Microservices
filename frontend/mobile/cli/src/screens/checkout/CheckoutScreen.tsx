import React, {useEffect, useState} from 'react';
import {KeyboardAvoidingView, Platform, ScrollView, View} from 'react-native';
import {Text} from '@/components/ui/Typography';
import {SafeAreaView} from 'react-native-safe-area-context';
import Toast from 'react-native-toast-message';
import AppHeader from '@/components/common/AppHeader';
import FormField from '@/components/common/FormField';
import PrimaryButton from '@/components/common/PrimaryButton';
import PriceSummary from '@/components/order/PriceSummary';
import {shippingFor} from '@/constants/app';
import {useAuth} from '@/context/AuthContext';
import {useCart} from '@/context/CartContext';
import type {RootStackScreenProps} from '@/navigation/types';
import {addressApi, orderApi} from '@/services/api';
import type {PaymentMethod, ShippingAddress} from '@/types/models';
import {getErrorMessage} from '@/utils/errors';
import {splitName} from '@/utils/normalize';
import PaymentOption from './components/PaymentOption';

const EMPTY_ADDRESS: ShippingAddress = {street: '', city: '', state: '', zipCode: '', country: ''};

/** Address fields the API requires (state is optional). */
const REQUIRED: (keyof ShippingAddress)[] = ['street', 'city', 'zipCode', 'country'];

export default function CheckoutScreen({navigation}: RootStackScreenProps<'Checkout'>) {
  const {isSignedIn, user} = useAuth();
  const {cartItems, cartTotal, clearCart, flush} = useCart();

  const [recipientName, setRecipientName] = useState(user?.name ?? '');
  const [phone, setPhone] = useState(user?.phone ?? '');
  const [address, setAddress] = useState<ShippingAddress>(EMPTY_ADDRESS);
  const [hasSavedAddress, setHasSavedAddress] = useState(false);
  const [notes, setNotes] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('cash');
  const [placing, setPlacing] = useState(false);

  const shipping = shippingFor(cartTotal);
  const addressComplete =
    !!recipientName.trim() && REQUIRED.every(key => address[key].trim().length > 0);

  // Prefill with the default saved address.
  useEffect(() => {
    if (!isSignedIn) {
      return;
    }
    addressApi
      .getAll()
      .then(([saved]) => {
        if (saved) {
          const {street, city, state, zipCode, country} = saved;
          setAddress({street, city, state, zipCode, country});
          setRecipientName(
            prev => [saved.firstName, saved.lastName].filter(Boolean).join(' ') || prev,
          );
          setPhone(prev => saved.phone || prev);
          setHasSavedAddress(true);
        }
      })
      .catch(() => undefined);
  }, [isSignedIn]);

  const updateAddress = (key: keyof ShippingAddress) => (value: string) =>
    setAddress(prev => ({...prev, [key]: value}));

  const placeOrder = async () => {
    if (!isSignedIn) {
      Toast.show({
        type: 'info',
        text1: 'Login required',
        text2: 'Please sign in to place an order',
      });
      navigation.navigate('SignIn');
      return;
    }
    if (cartItems.length === 0) {
      Toast.show({type: 'error', text1: 'Cart empty', text2: 'Add items before checking out'});
      return;
    }
    if (!addressComplete) {
      Toast.show({
        type: 'error',
        text1: 'Address required',
        text2: 'Please fill in the name and address fields',
      });
      return;
    }

    setPlacing(true);
    try {
      // The order is built from the server cart: wait for pending cart updates first.
      await flush();
      const order = await orderApi.create({
        recipientName,
        recipientPhone: phone,
        shippingAddress: address,
        paymentMethod,
        notes,
      });

      if (!hasSavedAddress) {
        const {firstName, lastName} = splitName(recipientName);
        addressApi
          .create({
            ...address,
            firstName,
            lastName: lastName ?? undefined,
            phone,
            type: 'Home',
            isDefault: true,
          })
          .catch(() => undefined);
        setHasSavedAddress(true);
      }

      // The API emptied the server cart. Card payments are charged right after the order is
      // saved; the order screen shows the result (paid, or cancelled when declined).
      clearCart();
      Toast.show({
        type: 'success',
        text1: 'Order placed',
        text2:
          paymentMethod === 'card'
            ? `Order #${order.orderNumber}: card payment is being processed`
            : `Order #${order.orderNumber}: pay cash on delivery`,
      });
      navigation.replace('Orders');
    } catch (error) {
      Toast.show({type: 'error', text1: 'Order failed', text2: getErrorMessage(error)});
    } finally {
      setPlacing(false);
    }
  };

  return (
    <SafeAreaView className="flex-1 bg-surface" edges={['top', 'bottom']}>
      <AppHeader title="Checkout" showBack />

      <KeyboardAvoidingView
        className="flex-1"
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          className="flex-1"
          contentContainerClassName="px-4 pt-4"
          keyboardShouldPersistTaps="handled">
          <Text className="mb-4 text-lg font-bold text-primary">Shipping Address</Text>
          <FormField
            label="Full name"
            autoComplete="name"
            value={recipientName}
            onChangeText={setRecipientName}
          />
          <FormField
            label="Phone (optional)"
            keyboardType="phone-pad"
            autoComplete="tel"
            placeholder="e.g. +919876543210"
            value={phone}
            onChangeText={setPhone}
          />
          <FormField
            label="Street"
            autoComplete="street-address"
            value={address.street}
            onChangeText={updateAddress('street')}
          />
          <View className="flex-row gap-3">
            <FormField
              label="City"
              containerClassName="mb-4 flex-1"
              value={address.city}
              onChangeText={updateAddress('city')}
            />
            <FormField
              label="State (optional)"
              containerClassName="mb-4 flex-1"
              value={address.state}
              onChangeText={updateAddress('state')}
            />
          </View>
          <View className="flex-row gap-3">
            <FormField
              label="Zip Code"
              containerClassName="mb-4 flex-1"
              keyboardType="number-pad"
              autoComplete="postal-code"
              value={address.zipCode}
              onChangeText={updateAddress('zipCode')}
            />
            <FormField
              label="Country"
              containerClassName="mb-4 flex-1"
              value={address.country}
              onChangeText={updateAddress('country')}
            />
          </View>
          <FormField
            label="Delivery notes (optional)"
            placeholder="e.g. Leave at the front door"
            value={notes}
            onChangeText={setNotes}
          />

          <Text className="mb-4 mt-2 text-lg font-bold text-primary">Payment Method</Text>
          <PaymentOption
            icon="cash-outline"
            title="Cash on Delivery"
            subtitle="Pay when you receive the order"
            selected={paymentMethod === 'cash'}
            onPress={() => setPaymentMethod('cash')}
          />
          <PaymentOption
            icon="card-outline"
            title="Pay with Card"
            subtitle="Demo payment: approved up to ₹50,000"
            selected={paymentMethod === 'card'}
            onPress={() => setPaymentMethod('card')}
          />
        </ScrollView>

        <View className="border-t border-border bg-background p-4 shadow-lg">
          <PriceSummary subtotal={cartTotal} shipping={shipping} total={cartTotal + shipping} />
          <PrimaryButton title="Place Order" loading={placing} onPress={placeOrder} />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
