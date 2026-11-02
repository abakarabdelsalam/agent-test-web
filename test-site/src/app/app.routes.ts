import { Routes } from '@angular/router';
import { CartPage } from './pages/cart';
import { Catalog } from './pages/catalog';
import { Checkout } from './pages/checkout';
import { Confirmation } from './pages/confirmation';
import { ProductPage } from './pages/product';
import { Register } from './pages/register';
import { Contact, NotFound } from './pages/simple';

export const routes: Routes = [
  { path: '', component: Catalog, title: 'TechShop' },
  { path: 'produit/:id', component: ProductPage, title: 'Produit' },
  { path: 'panier', component: CartPage, title: 'Panier' },
  { path: 'commande', component: Checkout, title: 'Commande' },
  { path: 'confirmation', component: Confirmation, title: 'Confirmation' },
  { path: 'inscription', component: Register, title: 'Inscription' },
  { path: 'contact', component: Contact, title: 'Contact' },
  { path: '**', component: NotFound, title: 'Page introuvable' },
];
