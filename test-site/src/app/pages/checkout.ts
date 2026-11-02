import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Shop } from '../shop';

@Component({
  template: `
    <h1>Commande</h1>
    <form (submit)="$event.preventDefault(); submit(name.value, email.value, address.value, postal.value, city.value)" novalidate>
      <label>Nom <input #name data-testid="name" /></label>
      <label>E-mail <input #email data-testid="email" /></label>
      <label>Adresse <input #address data-testid="address" /></label>
      <label>Code postal <input #postal data-testid="postal" /></label>
      <label>Ville <input #city data-testid="city" /></label>
      <p class="error" role="alert" data-testid="checkout-error">{{ error() }}</p>
      <button type="submit">Valider la commande</button>
    </form>
  `,
})
export class Checkout {
  private readonly shop = inject(Shop);
  private readonly router = inject(Router);
  protected readonly error = signal('');

  submit(name: string, email: string, address: string, postal: string, city: string): void {
    if (![name, email, address, postal, city].every((value) => value.trim())) {
      this.error.set('Tous les champs sont obligatoires.');
      return;
    }
    this.shop.placeOrder();
    this.router.navigate(['/confirmation']);
  }
}
