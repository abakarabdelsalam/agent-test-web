import { Component, inject } from '@angular/core';
import { Shop, euros } from '../shop';

@Component({
  template: `
    <h1>Commande confirmée</h1>
    <div class="notice">
      <p>Merci pour votre commande.</p>
      <p>Total payé : <strong data-testid="paid-total">{{ euros(shop.lastOrderTotal()) }}</strong></p>
    </div>
  `,
})
export class Confirmation {
  protected readonly shop = inject(Shop);
  protected readonly euros = euros;
}
