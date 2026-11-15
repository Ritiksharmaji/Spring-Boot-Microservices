import React from 'react';
import {ActivityIndicator, Modal, Pressable, TouchableOpacity, View} from 'react-native';
import {Text} from '@/components/ui/Typography';
import {Ionicons} from '@/components/icons';
import {NEXT_ORDER_STATUSES, ORDER_STATUSES} from '@/constants/orderStatus';
import {colors} from '@/theme';
import type {OrderStatus} from '@/types/models';

interface OrderStatusSheetProps {
  visible: boolean;
  current?: OrderStatus;
  updating: boolean;
  onSelect: (status: OrderStatus) => void;
  onClose: () => void;
}

export default function OrderStatusSheet({
  visible,
  current,
  updating,
  onSelect,
  onClose,
}: OrderStatusSheetProps) {
  return (
    <Modal visible={visible} animationType="fade" transparent onRequestClose={onClose}>
      <Pressable className="flex-1 justify-end bg-black/50" onPress={onClose}>
        <Pressable className="rounded-t-2xl bg-background p-4">
          <View className="mb-4 flex-row items-center justify-between border-b border-border pb-4">
            <Text className="text-lg font-bold text-primary">Update Order Status</Text>
            <TouchableOpacity onPress={onClose} accessibilityLabel="Close">
              <Ionicons name="close" size={24} color={colors.secondary} />
            </TouchableOpacity>
          </View>

          {updating ? (
            <View className="py-8">
              <ActivityIndicator size="large" color={colors.primary} />
              <Text className="mt-2 text-center text-secondary">Updating status...</Text>
            </View>
          ) : (
            ORDER_STATUSES.map(status => {
              const selected = status === current;
              // Only the next steps of the order flow can be chosen (the API rejects others).
              const allowed = !!current && NEXT_ORDER_STATUSES[current].includes(status);
              return (
                <TouchableOpacity
                  key={status}
                  disabled={!allowed}
                  accessibilityState={{disabled: !allowed, selected}}
                  onPress={() => onSelect(status)}
                  className={`mb-2 flex-row items-center justify-between rounded-xl p-4 ${
                    selected ? 'bg-primary/10' : 'bg-surface'
                  } ${!allowed && !selected ? 'opacity-40' : ''}`}>
                  <Text
                    className={`capitalize ${
                      selected ? 'font-bold text-primary' : 'font-medium text-secondary'
                    }`}>
                    {status}
                  </Text>
                  {selected && (
                    <Ionicons name="checkmark-circle" size={20} color={colors.primary} />
                  )}
                </TouchableOpacity>
              );
            })
          )}
        </Pressable>
      </Pressable>
    </Modal>
  );
}
