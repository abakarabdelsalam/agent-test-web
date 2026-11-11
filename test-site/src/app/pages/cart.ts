import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Shop, euros } from '../shop';

@Component({
  imports: [RouterLink],
  template: `
    <h1>Panier</h1>
    @if (shop.lines().length === 0) {
      <p class="notice" data-testid="cart-empty">Votre panier est vide.</p>
    } @else {
      <div class="panel">
        <table>
          <thead><tr><th>Produit</th><th>Quantité</th><th>Prix</th><th>Action</th></tr></thead>
          <tbody>
            @for (line of shop.lines(); track line.product.id) {
              <tr data-testid="cart-line">
                <td data-testid="line-name">{{ line.product.name }}</td>
                <td>
                  <span class="quantity">
                    <button type="button" [attr.aria-label]="'Diminuer ' + line.product.name" (click)="shop.setQuantity(line.product.id, line.quantity - 1)">-</button>
                    <span data-testid="line-quantity">{{ line.quantity }}</span>
                    <button type="button" [attr.aria-label]="'Augmenter ' + line.product.name" (click)="shop.setQuantity(line.product.id, line.quantity + 1)">+</button>
                  </span>
                </td>
                <td data-testid="line-total">{{ euros(line.product.price * line.quantity) }}</td>
                <td><button type="button" class="remove" (click)="shop.remove(line.product.id)">Retirer {{ line.product.name }}</button></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <div class="cart-bottom">
        <div class="promo">
          <label>Code promo <input data-testid="promo-input" #code /></label>
          <button type="button" (click)="shop.applyPromo(code.value)">Appliquer</button>
          <p data-testid="promo-message">{{ shop.promoMessage() }}</p>
        </div>
        <div class="summary">
          <dl>
            <dt>Sous-total</dt><dd data-testid="subtotal">{{ euros(shop.subtotal()) }}</dd>
            <dt>Remise</dt><dd data-testid="discount">{{ euros(shop.discount()) }}</dd>
            <dt>Livraison</dt><dd data-testid="shipping">{{ euros(shop.shipping()) }}</dd>
            <dt>Total</dt><dd data-testid="total">{{ euros(shop.total()) }}</dd>
          </dl>
          <a routerLink="/commande" class="button">Commander</a>
        </div>
      </div>
    }
  `,
})
export class CartPage {
  protected readonly shop = inject(Shop);
  protected readonly euros = euros;
}
