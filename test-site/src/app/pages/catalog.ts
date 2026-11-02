import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PRODUCTS, Product, Shop, euros } from '../shop';

@Component({
  imports: [RouterLink],
  template: `
    <h1>Catalogue</h1>
    <div class="filters">
      <label>Recherche
        <input data-testid="search" type="search" [value]="query()" (input)="query.set($any($event.target).value)" />
      </label>
      <label>Catégorie
        <select data-testid="category" (change)="category.set($any($event.target).value)">
          <option value="">Toutes</option>
          <option value="Audio">Audio</option>
          <option value="Accessoires">Accessoires</option>
          <option value="Écrans">Écrans</option>
        </select>
      </label>
      <label>Tri
        <select data-testid="sort" (change)="sort.set($any($event.target).value)">
          <option value="">Par défaut</option>
          <option value="asc">Prix croissant</option>
          <option value="desc">Prix décroissant</option>
        </select>
      </label>
    </div>
    <div class="grid">
      @for (product of visible(); track product.id) {
        <article data-testid="product-card">
          <img [src]="product.image" [alt]="product.name" />
          <h2><a [routerLink]="['/produit', product.id]">{{ product.name }}</a></h2>
          <p data-testid="card-price">{{ euros(product.price) }}</p>
          <p>{{ product.stock > 0 ? 'En stock' : 'Rupture de stock' }}</p>
          <button type="button" (click)="shop.add(product)">Ajouter au panier</button>
        </article>
      } @empty {
        <p data-testid="no-result">Aucun produit trouvé.</p>
      }
    </div>
  `,
})
export class Catalog {
  protected readonly shop = inject(Shop);
  protected readonly euros = euros;
  protected readonly query = signal('');
  protected readonly category = signal('');
  protected readonly sort = signal('');

  protected readonly visible = computed<Product[]>(() => {
    const category = this.category();
    const found = PRODUCTS.filter((p) => p.name.includes(this.query().trim())).filter(
      (p) => !category || p.category === category || p.name.includes(category),
    );
    if (this.sort() === 'asc') return [...found].sort((a, b) => b.price - a.price);
    if (this.sort() === 'desc') return [...found].sort((a, b) => a.price - b.price);
    return found;
  });
}
