import React, {useState} from 'react';
import {Alert, FlatList, RefreshControl, TouchableOpacity, View} from 'react-native';
import {Text} from '@/components/ui/Typography';
import Toast from 'react-native-toast-message';
import EmptyState from '@/components/common/EmptyState';
import LoadingView from '@/components/common/LoadingView';
import {Ionicons} from '@/components/icons';
import StatusBadge from '@/components/order/StatusBadge';
import {getOrderStatusStyle, NEXT_ORDER_STATUSES} from '@/constants/orderStatus';
import {useAsync} from '@/hooks/useAsync';
import {orderApi} from '@/services/api';
import {colors} from '@/theme';
import type {Order, OrderStatus} from '@/types/models';
import {getErrorMessage} from '@/utils/errors';
import {formatDate, formatPrice} from '@/utils/format';
import OrderStatusSheet from './components/OrderStatusSheet';

function InfoBlock({label, children}: {label: string; children: React.ReactNode}) {
  return (
    <View className="mb-3 rounded-lg bg-surface p-3">
      <Text className="mb-1 text-xs font-bold text-secondary">{label}</Text>
      {children}
    </View>
  );
}

function AdminOrderCard({order, onEditStatus}: {order: Order; onEditStatus: () => void}) {
  const customer = typeof order.user === 'object' ? order.user : undefined;
  const address = order.shippingAddress;
  const statusStyle = getOrderStatusStyle(order.orderStatus);
  // Delivered and cancelled orders cannot change any more.
  const isFinal = NEXT_ORDER_STATUSES[order.orderStatus].length === 0;

  return (
    <View className="mb-4 rounded-xl border border-border bg-background p-4 shadow-sm">
      <View className="mb-2 flex-row justify-between">
        <Text className="text-sm font-medium text-gray-400">Order #{order.orderNumber}</Text>
        <Text className="text-xs text-secondary">{formatDate(order.createdAt)}</Text>
      </View>

      <InfoBlock label="CUSTOMER">
        <Text className="font-medium text-primary">{customer?.name ?? 'Unknown user'}</Text>
        {!!order.recipientPhone && (
          <Text className="text-xs text-secondary">{order.recipientPhone}</Text>
        )}
      </InfoBlock>

      <InfoBlock label="SHIPPING ADDRESS">
        <Text className="text-xs text-primary">
          {[address?.street, address?.city, address?.state, address?.zipCode, address?.country]
            .filter(Boolean)
            .join(', ')}
        </Text>
      </InfoBlock>

      <Text className="mb-2 text-xs font-bold text-secondary">ITEMS</Text>
      {order.items.map(item => (
        <View key={item._id} className="mb-1 flex-row justify-between">
          <Text className="flex-1 text-xs text-secondary">
            {item.quantity}× {item.product.name ?? item.name}
            {!!item.size && <Text className="text-gray-400"> ({item.size})</Text>}
          </Text>
          <Text className="text-xs font-bold text-secondary">{formatPrice(item.price)}</Text>
        </View>
      ))}

      <View className="mt-2 flex-row items-center justify-between border-t border-border pt-3">
        <Text className="text-lg font-bold text-primary">{formatPrice(order.totalAmount)}</Text>
        <TouchableOpacity
          onPress={onEditStatus}
          disabled={isFinal}
          className={`flex-row items-center rounded-full px-4 py-2 ${statusStyle.container}`}
          accessibilityLabel={
            isFinal ? `Status ${order.orderStatus}` : `Status ${order.orderStatus}, change`
          }>
          <Text
            className={`text-xs font-bold uppercase tracking-wide ${statusStyle.text} ${
              isFinal ? '' : 'mr-2'
            }`}>
            {order.orderStatus}
          </Text>
          {!isFinal && <Ionicons name="pencil" size={12} color={colors.secondary} />}
        </TouchableOpacity>
      </View>
      <View className="mt-2 flex-row items-center">
        <Text className="mr-2 text-xs text-secondary">
          {order.paymentMethod === 'cash' ? 'Cash on delivery' : 'Card'} ·
        </Text>
        <StatusBadge status={order.paymentStatus} kind="payment" />
      </View>
      {!!order.statusReason && (
        <Text className="mt-1 text-xs text-secondary">{order.statusReason}</Text>
      )}
    </View>
  );
}

export default function AdminOrdersScreen() {
  const {data: orders, setData: setOrders, loading, refreshing, reload} = useAsync(orderApi.getAll);
  const [selected, setSelected] = useState<Order | null>(null);
  const [updating, setUpdating] = useState(false);

  const applyStatus = async (order: Order, status: OrderStatus) => {
    setUpdating(true);
    try {
      const updated = await orderApi.updateStatus(order._id, status);
      setOrders(prev => (prev ?? []).map(o => (o._id === updated._id ? updated : o)));
      const refund = status === 'cancelled' && updated.paymentStatus === 'paid';
      Toast.show({
        type: 'success',
        text1: 'Order updated',
        text2: refund ? 'Cancelled, the card payment is being refunded' : `Marked as ${status}`,
      });
      if (status === 'cancelled' || status === 'delivered') {
        // Refunds and cash collection are recorded by the payment module just after the change.
        setTimeout(reload, 1500);
      }
    } catch (error) {
      Toast.show({type: 'error', text1: 'Update failed', text2: getErrorMessage(error)});
    } finally {
      setUpdating(false);
      setSelected(null);
    }
  };

  const updateStatus = (status: OrderStatus) => {
    const order = selected;
    if (!order) {
      return;
    }
    if (status !== 'cancelled') {
      applyStatus(order, status);
      return;
    }
    // Cancelling returns the stock and refunds a card payment: ask first.
    Alert.alert(
      `Cancel order #${order.orderNumber}?`,
      order.paymentStatus === 'paid'
        ? "The customer's card payment will be refunded. This cannot be undone."
        : 'The order will be cancelled. This cannot be undone.',
      [
        {text: 'Keep order', style: 'cancel'},
        {text: 'Cancel order', style: 'destructive', onPress: () => applyStatus(order, status)},
      ],
    );
  };

  if (loading) {
    return <LoadingView className="bg-surface" />;
  }

  return (
    <View className="flex-1 bg-surface">
      <FlatList
        data={orders ?? []}
        keyExtractor={item => item._id}
        contentContainerClassName="flex-grow p-4"
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={reload} />}
        renderItem={({item}) => (
          <AdminOrderCard order={item} onEditStatus={() => setSelected(item)} />
        )}
        ListEmptyComponent={<EmptyState icon="receipt-outline" title="No orders found" />}
      />
      <OrderStatusSheet
        visible={!!selected}
        current={selected?.orderStatus}
        updating={updating}
        onSelect={updateStatus}
        onClose={() => setSelected(null)}
      />
    </View>
  );
}
