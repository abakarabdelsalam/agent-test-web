import { Component } from '@angular/core';

@Component({
  template: `<h1>Contact</h1><p class="notice">Écrivez-nous à contact&#64;techshop.example.</p>`,
})
export class Contact {}

@Component({
  template: `<h1>Page introuvable</h1><p class="notice">Cette page n'existe pas.</p>`,
})
export class NotFound {}
