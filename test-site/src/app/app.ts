import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { Shop } from './shop';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterOutlet],
  template: `
    <header>
      <a routerLink="/" class="logo">TechShop</a>
      <nav aria-label="Navigation principale">
        <a routerLink="/">Catalogue</a>
        <a routerLink="/inscription">Inscription</a>
        <a routerLink="/panier">Panier (<span data-testid="cart-badge">{{ shop.badge() }}</span>)</a>
      </nav>
    </header>
    <main><router-outlet /></main>
    <footer>
      <span>TechShop, boutique de démonstration</span>
      <a routerLink="/contacts" data-testid="footer-contact">Contact</a>
    </footer>
  `,
})
export class App {
  protected readonly shop = inject(Shop);
}
