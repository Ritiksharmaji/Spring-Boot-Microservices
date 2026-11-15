import type {IconName} from '@/components/icons';

export type Category = {id: string; name: string; icon: IconName};

/** Product categories of the shop (product.category in the Spring API). */
export const CATEGORIES: Category[] = [
  {id: 'men', name: 'Men', icon: 'man-outline'},
  {id: 'women', name: 'Women', icon: 'woman-outline'},
  {id: 'kids', name: 'Kids', icon: 'happy-outline'},
];

export const ALL_CATEGORY: Category = {id: 'all', name: 'All', icon: 'grid'};
