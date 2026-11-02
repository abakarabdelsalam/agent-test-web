// Vérifie que chaque bug de bugs.json se reproduit réellement dans le navigateur.
// Usage : node evaluation/verify-bugs.cjs (après "npm run build" dans test-site)
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const { chromium } = require('playwright');

const ROOT = path.join(__dirname, '..', 'test-site', 'dist', 'test-site', 'browser');
const BUGS = require('./bugs.json');
const TYPES = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.svg': 'image/svg+xml', '.ico': 'image/x-icon' };

const server = http.createServer((req, res) => {
  const url = decodeURIComponent(req.url.split('?')[0]);
  let file = path.join(ROOT, url);
  if (!file.startsWith(ROOT)) { res.writeHead(403).end(); return; }
  if (!path.extname(url)) file = path.join(ROOT, 'index.html');
  if (!fs.existsSync(file)) { res.writeHead(404).end(); return; }
  res.writeHead(200, { 'Content-Type': TYPES[path.extname(file)] || 'application/octet-stream' });
  fs.createReadStream(file).pipe(res);
});

const settle = (page) => page.waitForTimeout(200);
const id = (name) => `[data-testid="${name}"]`;

async function open(page, base) { await page.goto(base); await page.waitForSelector('h1'); }
async function names(page) { return page.locator(`${id('product-card')} h2`).allTextContents(); }
async function add(page, name) {
  await page.locator(id('product-card'), { hasText: name }).getByRole('button', { name: 'Ajouter au panier' }).click();
}
async function cart(page) { await page.getByRole('link', { name: /Panier/ }).click(); await page.waitForSelector('h1:has-text("Panier")'); }
async function fillCheckout(page, email = 'nom@domaine.fr', postal = '69001') {
  await page.getByRole('link', { name: 'Commander' }).click();
  await page.fill(id('name'), 'Test');
  await page.fill(id('email'), email);
  await page.fill(id('address'), '1 rue Test');
  await page.fill(id('postal'), postal);
  await page.fill(id('city'), 'Lyon');
  await page.getByRole('button', { name: 'Valider la commande' }).click();
}
async function register(page, base, password, confirm) {
  await page.goto(`${base}/inscription`);
  await page.fill(id('register-email'), 'nom@domaine.fr');
  await page.fill(id('password'), password);
  await page.fill(id('password-confirm'), confirm);
  await page.getByRole('button', { name: 'Créer mon compte' }).click();
  await settle(page);
  return (await page.locator(id('register-success')).textContent()).trim();
}

const CHECKS = {
  'recherche-casse': async (p, b) => { await open(p, b); await p.fill(id('search'), 'casque'); await p.waitForTimeout(100); return (await names(p)).length === 0; },
  'tri-inverse': async (p, b) => { await open(p, b); await p.selectOption(id('sort'), 'asc'); return (await names(p))[0].includes('Écran 27'); },
  'filtre-categorie': async (p, b) => { await open(p, b); await p.selectOption(id('category'), 'Audio'); return (await names(p)).some((n) => n.includes('Câble')); },
  'rupture-ajoutable': async (p, b) => { await open(p, b); await add(p, 'Webcam'); await cart(p);
    // Le titre du panier s'affiche avant ses lignes : sans cette attente, la liste est lue vide.
    await p.locator(id('line-name')).first().waitFor({ timeout: 2000 }).catch(() => {});
    return (await p.locator(id('line-name')).allTextContents()).some((n) => n.includes('Webcam')); },
  'prix-different': async (p, b) => {
    await open(p, b);
    const card = await p.locator(id('product-card'), { hasText: 'Clavier' }).locator(id('card-price')).textContent();
    await p.getByRole('link', { name: 'Clavier Mécanique' }).click();
    return card !== (await p.locator(id('detail-price')).textContent());
  },
  'image-cassee': async (p, b) => {
    await open(p, b); await p.waitForLoadState('networkidle');
    const widths = await p.locator(`${id('product-card')} img`).evaluateAll((all) => all.map((i) => [i.alt, i.naturalWidth]));
    const broken = widths.filter(([, w]) => w === 0).map(([a]) => a);
    return broken.length === 1 && broken[0].includes('Écran 27');
  },
  'lien-contact': async (p, b) => { await open(p, b); await p.locator(id('footer-contact')).click(); return p.locator('h1:has-text("Page introuvable")').isVisible(); },
  'quantite-negative': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await cart(p);
    await p.getByRole('button', { name: 'Diminuer Souris' }).click();
    await p.getByRole('button', { name: 'Diminuer Souris' }).click();
    await settle(p);
    return (await p.locator(id('line-quantity')).textContent()).trim() === '-1';
  },
  'sous-total-quantite': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await add(p, 'Souris'); await cart(p);
    return (await p.locator(id('subtotal')).textContent()).includes('24,90');
  },
  'mauvaise-suppression': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await add(p, 'Casque'); await cart(p);
    await p.getByRole('button', { name: 'Retirer Casque' }).click();
    return (await p.locator(id('line-name')).allTextContents()).join().includes('Casque');
  },
  'promo-20': async (p, b) => {
    await open(p, b); await add(p, 'Écran 24'); await cart(p);
    await p.fill(id('promo-input'), 'BIENVENUE10'); await p.getByRole('button', { name: 'Appliquer' }).click();
    await settle(p);
    return (await p.locator(id('discount')).textContent()).includes('29,80');
  },
  'promo-sans-erreur': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await cart(p);
    await p.fill(id('promo-input'), 'FAUX'); await p.getByRole('button', { name: 'Appliquer' }).click();
    return (await p.locator(id('promo-message')).textContent()).trim() === '';
  },
  'seuil-livraison': async (p, b) => { await open(p, b); await add(p, 'Casque'); await cart(p); return (await p.locator(id('shipping')).textContent()).includes('4,90'); },
  'compteur-faux': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await cart(p);
    await p.getByRole('button', { name: 'Retirer Souris' }).click();
    await settle(p);
    return (await p.locator(id('cart-badge')).textContent()).trim() === '1' && (await p.locator(id('cart-empty')).isVisible());
  },
  'email-commande': async (p, b) => { await open(p, b); await add(p, 'Souris'); await cart(p); await fillCheckout(p, 'pasunemail'); await p.waitForSelector('h1'); return p.locator('h1:has-text("Commande confirmée")').isVisible(); },
  'code-postal': async (p, b) => { await open(p, b); await add(p, 'Souris'); await cart(p); await fillCheckout(p, 'nom@domaine.fr', '6900A'); await p.waitForSelector('h1'); return p.locator('h1:has-text("Commande confirmée")').isVisible(); },
  'total-sans-livraison': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await cart(p);
    const total = await p.locator(id('total')).textContent();
    await fillCheckout(p);
    await p.waitForSelector(id('paid-total'));
    return total.includes('29,80') && (await p.locator(id('paid-total')).textContent()).includes('24,90');
  },
  'panier-non-vide': async (p, b) => {
    await open(p, b); await add(p, 'Souris'); await cart(p); await fillCheckout(p);
    await p.waitForSelector(id('paid-total'));
    await cart(p);
    await settle(p);
    return (await p.locator(id('cart-line')).count()) === 1;
  },
  'mot-de-passe-court': async (p, b) => (await register(p, b, 'abcd', 'abcd')) === 'Compte créé.',
  'confirmation-mdp': async (p, b) => (await register(p, b, 'motdepasse1', 'motdepasse2')) === 'Compte créé.',
};

(async () => {
  await new Promise((done) => server.listen(0, '127.0.0.1', done));
  const base = `http://127.0.0.1:${server.address().port}`;
  const browser = await chromium.launch();
  let found = 0;
  for (const bug of BUGS) {
    const context = await browser.newContext();
    const page = await context.newPage();
    let result;
    try { result = (await CHECKS[bug.slug](page, base)) === true; }
    catch (error) { result = `ERREUR ${error.message.split('\n')[0]}`; }
    if (result === true) found += 1;
    console.log(`${result === true ? 'OK ' : 'NON'} ${bug.id} ${bug.slug}${result === true ? '' : ` -> ${result}`}`);
    await context.close();
  }
  await browser.close();
  server.close();
  console.log(`\n${found}/${BUGS.length} bugs reproduits`);
  process.exit(found === BUGS.length ? 0 : 1);
})();
