import { Component, signal } from '@angular/core';

@Component({
  template: `
    <h1>Inscription</h1>
    <form (submit)="$event.preventDefault(); submit(email.value, password.value)" novalidate>
      <label>E-mail <input #email data-testid="register-email" type="email" /></label>
      <label>Mot de passe <input #password data-testid="password" type="password" /></label>
      <label>Confirmer le mot de passe <input data-testid="password-confirm" type="password" /></label>
      <p class="error" role="alert" data-testid="register-error">{{ error() }}</p>
      <p data-testid="register-success">{{ success() }}</p>
      <button type="submit">Créer mon compte</button>
    </form>
  `,
})
export class Register {
  protected readonly error = signal('');
  protected readonly success = signal('');

  submit(email: string, password: string): void {
    this.success.set('');
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
      this.error.set('Adresse e-mail invalide.');
    } else if (password.length < 4) {
      this.error.set('Mot de passe trop court.');
    } else {
      this.error.set('');
      this.success.set('Compte créé.');
    }
  }
}
