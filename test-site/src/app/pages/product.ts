import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DETAIL_PRICES, PRODUCTS, Shop, euros } from '../shop';

@Component({
  imports: [RouterLink],
  template: `
    @if (product; as p) {
      <h1>{{ p.name }}</h1>
      <img [src]="p.image" [alt]="p.name" />
      <p>{{ p.description }}</p>
      <p data-testid="detail-price">{{ euros(price) }}</p>
      <button type="button" [disabled]="p.stock === 0" (click)="shop.add(p)">Ajouter au panier</button>
    } @else {
      <p>Produit introuvable.</p>
    }
    <p><a routerLink="/">Retour au catalogue</a></p>
  `,
})
export class ProductPage {
  protected readonly shop = inject(Shop);
  protected readonly euros = euros;
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  protected readonly product = PRODUCTS.find((p) => p.id === this.id);
  protected readonly price = DETAIL_PRICES[this.id] ?? this.product?.price ?? 0;
}
