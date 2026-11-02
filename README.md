# Agent IA de test de sites web

Agent IA qui parcourt un site comme un utilisateur, compare ce qu'il voit aux spécifications et rédige un rapport de bugs.

Stack : Java 25, Spring Boot 4.1.2, Angular 22, API Claude, Playwright.

**État actuel : jour 2.** Le backend ouvre le site avec Playwright, applique 3 vérifications écrites à la main et produit un rapport JSON validé. L'IA qui décide seule arrive à partir du jour 3.

## Structure

| Dossier | Contenu |
|---|---|
| `backend/` | API Spring Boot et client Claude |
| `test-site/` | Boutique Angular de démonstration, avec 20 bugs volontaires |
| `specs/` | Comportement attendu du site, consultable par l'agent |
| `evaluation/` | Liste des 20 bugs et script qui vérifie qu'ils se reproduisent |

`evaluation/bugs.json` est le corrigé : l'agent ne doit jamais y avoir accès pendant un test.

## Prérequis

- Java 25
- Maven 3.9 ou plus récent
- Node.js 24.15 ou plus récent

## Lancer le site de test

```powershell
cd test-site
npm install
npm start
```

Le site s'ouvre sur http://localhost:4200.

## Vérifier les 20 bugs

```powershell
cd test-site
npm run build
cd ..\evaluation
npm install
npx playwright install chromium
npm run verify
```

Résultat attendu : `20/20 bugs reproduits`.

## Lancer le backend sans frais

Par défaut, le backend répond avec un texte simulé.

```powershell
cd backend
mvn test
mvn spring-boot:run
```

Dans un second terminal :

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/ask -ContentType "application/json; charset=utf-8" -Body '{"prompt":"Bonjour"}'
```

## Explorer le catalogue (jour 2)

Lance le site de test sur le port 4200 et le backend, puis :

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/explore -ContentType "application/json; charset=utf-8" -Body '{"url":"http://localhost:4200"}' | ConvertTo-Json -Depth 5
```

Seuls `localhost` et `127.0.0.1` sont autorisés.

## Appeler vraiment Claude

Ce mode consomme du crédit API.

```powershell
$env:CLAUDE_MODE = "real"
$env:ANTHROPIC_API_KEY = "ta-cle"
mvn spring-boot:run
```

Ne mets jamais ta clé dans un fichier suivi par Git.
