import { Injectable, computed, signal } from '@angular/core';

export interface Product {
  id: number;
  name: string;
  category: 'Audio' | 'Accessoires' | 'Écrans';
  price: number;
  stock: number;
  image: string;
  description: string;
}

export interface CartLine {
  product: Product;
  quantity: number;
}

export const PRODUCTS: Product[] = [
  { id: 1, name: 'Casque Bluetooth', category: 'Audio', price: 59.9, stock: 12, image: 'images/casque.svg', description: 'Casque sans fil avec 30 heures d\'autonomie.' },
  { id: 2, name: 'Enceinte Portable', category: 'Audio', price: 39.9, stock: 8, image: 'images/enceinte.svg', description: 'Enceinte compacte résistante aux éclaboussures.' },
  { id: 3, name: 'Clavier Mécanique', category: 'Accessoires', price: 79.9, stock: 5, image: 'images/clavier.svg', description: 'Clavier AZERTY rétroéclairé.' },
  { id: 4, name: 'Souris Sans Fil', category: 'Accessoires', price: 24.9, stock: 20, image: 'images/souris.svg', description: 'Souris ergonomique silencieuse.' },
  { id: 5, name: 'Webcam Full HD', category: 'Accessoires', price: 49.9, stock: 0, image: 'images/webcam.svg', description: 'Webcam 1080p avec micro intégré.' },
  { id: 6, name: 'Écran 27 pouces', category: 'Écrans', price: 229, stock: 3, image: 'images/ecran-27.png', description: 'Écran QHD 165 Hz.' },
  { id: 7, name: 'Écran 24 pouces', category: 'Écrans', price: 149, stock: 6, image: 'images/ecran-24.svg', description: 'Écran Full HD IPS.' },
  { id: 8, name: 'Câble Audio Jack', category: 'Accessoires', price: 9.9, stock: 30, image: 'images/cable.svg', description: 'Câble jack 3,5 mm de 2 mètres.' },
];

export const DETAIL_PRICES: Record<number, number> = { 3: 89.9 };

@Injectable({ providedIn: 'root' })
export class Shop {
  readonly lines = signal<CartLine[]>([]);
  readonly badge = signal(0);
  readonly discountRate = signal(0);
  readonly promoMessage = signal('');
  readonly lastOrderTotal = signal(0);

  readonly subtotal = computed(() => this.lines().reduce((sum, line) => sum + line.product.price, 0));
  readonly discount = computed(() => this.subtotal() * this.discountRate());
  readonly shipping = computed(() => (this.lines().length === 0 || this.subtotal() > 100 ? 0 : 4.9));
  readonly total = computed(() => this.subtotal() - this.discount() + this.shipping());

  add(product: Product): void {
    this.badge.update((count) => count + 1);
    const existing = this.lines().find((line) => line.product.id === product.id);
    if (existing) {
      this.setQuantity(product.id, existing.quantity + 1);
    } else {
      this.lines.update((lines) => [...lines, { product, quantity: 1 }]);
    }
  }

  setQuantity(id: number, quantity: number): void {
    this.lines.update((lines) => lines.map((line) => (line.product.id === id ? { ...line, quantity } : line)));
  }

  remove(_id: number): void {
    this.lines.update((lines) => lines.slice(1));
  }

  applyPromo(code: string): void {
    if (code.trim().toUpperCase() === 'BIENVENUE10') {
      this.discountRate.set(0.2);
      this.promoMessage.set('Code appliqué');
    }
  }

  placeOrder(): void {
    this.lastOrderTotal.set(this.subtotal() - this.discount());
  }
}

export function euros(value: number): string {
  return value.toFixed(2).replace('.', ',') + ' €';
}
