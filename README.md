# Agent IA de test de sites web

Agent IA qui parcourt un site comme un utilisateur, compare ce qu'il voit aux spécifications et rédige un rapport de bugs.

Stack : Java 25, Spring Boot 4.1.2, Angular 22, API Claude, Playwright.

## Fonctionnalités

- **Site de test TechShop** : boutique Angular contenant 20 bugs volontaires. Un script vérifie qu'ils se reproduisent tous dans un vrai navigateur (dernier résultat : 20/20).
- **Exploration scriptée** (`/api/explore`) : Playwright ouvre le catalogue et applique 3 vérifications écrites à la main. Testé sur TechShop : 3 bugs trouvés. Un site non autorisé est refusé (403) et une requête sans URL est refusée (400).
- **Agent avec appel d'outils** (`/api/agent`) : Claude choisit les actions et le code Java les exécute avec Playwright. L'agent dispose de 6 outils (ouvrir, lire la page, cliquer, remplir, capture d'écran, signaler un bug), limités à 5 actions par parcours.
  - Vérifié en **mode simulé** : un scénario enregistré pilote un vrai Chromium sur TechShop.
  - **Le mode réel, avec l'API Claude, n'a pas encore été testé.**
- **Garde-fous** :
  - le navigateur n'ouvre que les hôtes autorisés, y compris après un clic sur un lien externe ;
  - tout outil inconnu est refusé ;
  - chaque bug signalé est validé avant d'entrer dans le rapport JSON ;
  - le contenu des pages est traité comme une donnée, jamais comme une instruction.
- **Tests** : 46 tests backend, tous réussis lors de la dernière exécution (`mvn clean test`).

## Prochaines étapes

- Premier essai de l'agent en mode réel, sur un seul parcours, avec Claude Haiku.
- Limites de durée et de budget pour un agent autonome.
- Évaluation : mesure du nombre de bugs trouvés sur les 20, avec une seule commande.
- Accès de l'agent aux spécifications pendant le test.

## Structure

| Dossier | Contenu |
|---|---|
| `backend/` | API Spring Boot, client Claude, outils Playwright et boucle d'agent |
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

Par défaut, le backend utilise des réponses simulées et n'appelle pas l'API Claude.

```powershell
cd backend
mvn test
mvn spring-boot:run
```

Dans un second terminal :

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/ask -ContentType "application/json; charset=utf-8" -Body '{"prompt":"Bonjour"}'
```

## Explorer le catalogue

Lance le site de test sur le port 4200 et le backend, puis :

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/explore -ContentType "application/json; charset=utf-8" -Body '{"url":"http://localhost:4200"}' | ConvertTo-Json -Depth 5
```

Seuls `localhost` et `127.0.0.1` sont autorisés.

## Lancer l'agent

Lance le site de test sur le port 4200 et le backend, puis :

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/agent -ContentType "application/json; charset=utf-8" -Body '{"url":"http://localhost:4200","instruction":"Cherche casque dans le catalogue"}' | ConvertTo-Json -Depth 6
```

La réponse contient :
- les actions exécutées et leur résultat ;
- la raison de l'arrêt dans `endedBy` : `end_turn` (Claude a terminé), `action_limit` (5 actions atteintes), `max_tokens` ou `refusal` ;
- le rapport de bugs validé.

En mode simulé, le scénario enregistré est toujours le même : il ouvre http://localhost:4200 et cherche « casque », quelle que soit la consigne.

## Appeler vraiment Claude

Ce mode consomme du crédit API. Définis la clé **seulement dans le terminal qui lance le backend**. Elle disparaît quand ce terminal est fermé.

```powershell
cd backend
$env:CLAUDE_MODE = "real"
$env:ANTHROPIC_API_KEY = "ta-cle"
mvn spring-boot:run
```

Ne mets jamais ta clé dans un fichier suivi par Git, ni dans les variables d'environnement de Windows.
