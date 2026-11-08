# Agent IA de test de sites web

Agent IA qui parcourt un site comme un utilisateur, compare ce qu'il voit aux spécifications et rédige un rapport de bugs.

Stack : Java 25, Spring Boot 4.1.2, Angular 22, API Claude, Playwright.

## Fonctionnalités

- **Site de test TechShop** : boutique Angular contenant 20 bugs volontaires. Un script vérifie qu'ils se reproduisent tous dans un vrai navigateur (dernier résultat : 20/20).
- **Exploration scriptée** (`/api/explore`) : Playwright ouvre le catalogue et applique 3 vérifications écrites à la main. Testé sur TechShop : 3 bugs trouvés. Un site non autorisé est refusé (403) et une requête sans URL est refusée (400).
- **Agent avec appel d'outils** (`/api/agent`) : Claude choisit les actions et le code Java les exécute avec Playwright. L'agent dispose de 7 outils : ouvrir, lire la page, cliquer, remplir, choisir dans une liste, capture d'écran et signaler un bug. Les spécifications du site lui sont fournies dans sa consigne.
  - Vérifié en **mode simulé** : un scénario enregistré pilote un vrai Chromium sur TechShop.
  - Testé en **mode réel** avec Claude Haiku 5.5 : l'agent termine un parcours seul, et les limites l'arrêtent si besoin. Un parcours complet coûte moins de 0,01 $.
  - Premier score mesuré sur les 20 bugs : **3 bugs trouvés, 0 fausse alerte**, avec 20 actions (un seul essai, pour environ 0,011 $). Les autres bugs n'ont pas été atteints avant la limite.
- **Garde-fous** :
  - chaque parcours s'arrête à 20 actions, 3 minutes ou 0,05 $ de coût estimé, selon la première limite atteinte (réglable dans `application.properties`) ;
  - le coût est calculé à partir des jetons indiqués par l'API et figure dans la réponse ;
  - une erreur de l'API (clé invalide, crédit épuisé) arrête le parcours proprement ;
  - le navigateur n'ouvre que les hôtes autorisés, y compris après un clic sur un lien externe ;
  - tout outil inconnu est refusé ;
  - chaque bug signalé est validé avant d'entrer dans le rapport JSON ;
  - le contenu des pages est traité comme une donnée, jamais comme une instruction.
- **Mode observation** (désactivé par défaut) : fenêtre Chromium visible, pause d'une seconde entre les actions et journal en français dans la console du backend.
- **Tests** : 74 tests backend, dont un qui démarre l'application complète, tous réussis lors de la dernière exécution (`mvn clean test`).

## Prochaines étapes

- Améliorer le score : le premier essai a trouvé 3 bugs sur 20, sans fausse alerte, avec la limite de 20 actions. Reste à savoir si une limite plus haute en trouve davantage.

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

Par défaut, le backend utilise des réponses simulées et n'appelle pas l'API Claude. Au démarrage, la console indique le mode actif : `Mode Claude : simulé, aucun appel à l'API.` ou `Mode Claude : réel (claude-haiku-5-5), chaque appel consomme du crédit API.`

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
- la raison de l'arrêt dans `endedBy` :
  - `end_turn` : Claude a terminé ;
  - `action_limit`, `time_limit` ou `budget_limit` : une limite a été atteinte ;
  - `api_error` : l'API a renvoyé une erreur ;
  - `max_tokens` ou `refusal` : la réponse a été coupée ou refusée ;
- le coût estimé en dollars dans `costUsd` (0 en mode simulé) ;
- le rapport de bugs validé.

En mode simulé, le scénario enregistré est toujours le même : il ouvre http://localhost:4200 et cherche « casque », quelle que soit la consigne.

## Mesurer le score

Lance le site de test et le backend, puis, dans le dossier `evaluation` :

```powershell
cd evaluation
npm install
npm run evaluate
```

La commande lance l'agent sur TechShop, compare ses bugs au corrigé et affiche le nombre de bugs trouvés. Le détail est enregistré dans `evaluation/resultats.json`, qui n'est pas versionné. Si le backend tourne en mode simulé, la commande le signale : le score n'est alors pas significatif.

## Observer l'agent

Pour regarder l'agent travailler, lance le backend avec `AGENT_HEADED=true` :

```powershell
cd backend
$env:CLAUDE_MODE = "fake"
$env:AGENT_HEADED = "true"
mvn spring-boot:run
```

Puis lance l'agent comme ci-dessus.
- Chromium s'ouvre dans une vraie fenêtre et attend une seconde avant chaque action.
- La console du backend affiche chaque étape : numéro de l'action, outil choisi, paramètres, résultat résumé, raison de l'arrêt et coût estimé.

Exemple :

```text
Action 3/20 : remplir {selecteur=[data-testid="search"], texte=casque} -> Champ rempli.
Arrêt après 4 action(s) : le modèle a terminé (coût estimé : 0,0000 $)
```

Le journal n'affiche jamais la clé API ni les en-têtes d'authentification. Il ne contient que les actions de l'agent.

## Appeler vraiment Claude

Ce mode consomme du crédit API. Définis la clé **seulement dans le terminal qui lance le backend**. Elle disparaît quand ce terminal est fermé.

```powershell
cd backend
$env:CLAUDE_MODE = "real"
$env:ANTHROPIC_API_KEY = "ta-cle"
mvn spring-boot:run
```

Pour un premier essai plus court, ajoute `$env:AGENT_LIMITS_MAX_ACTIONS = "5"` avant `mvn spring-boot:run`. Cette variable remplace la limite de 20 actions.

Ne mets jamais ta clé dans un fichier suivi par Git, ni dans les variables d'environnement de Windows.
