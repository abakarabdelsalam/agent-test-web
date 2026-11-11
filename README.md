# Agent IA de test de sites web

Agent IA qui parcourt un site comme un utilisateur, compare ce qu'il voit aux spécifications et rédige un rapport de bugs.

Stack : Java 25, Spring Boot 4.1.2, Angular 22, API Claude, Playwright.

## Fonctionnalités

- **Site de test TechShop** : boutique Angular contenant 20 bugs volontaires. Un script vérifie qu'ils se reproduisent tous dans un vrai navigateur (dernier résultat : 20/20).
- **Exploration scriptée** (`/api/explore`) : Playwright ouvre le catalogue et applique 3 vérifications écrites à la main. Testé sur TechShop : 3 bugs trouvés. Un site non autorisé est refusé (403) et une requête sans URL est refusée (400).
- **Agent avec appel d'outils** (`/api/agent`) : Claude choisit les actions et le code Java les exécute avec Playwright. L'agent dispose de 8 outils : ouvrir, lire la page, cliquer, remplir, choisir dans une liste, capture d'écran, signaler un bug et chercher une spécification. Il ne reçoit pas toutes les spécifications d'un coup : il cherche celles qui concernent ce qu'il observe, par recherche de mots communs.
  - Vérifié en **mode simulé** : un scénario enregistré pilote un vrai Chromium sur TechShop.
  - Testé en **mode réel** avec Claude Haiku 5.5 : l'agent termine un parcours seul, et les limites l'arrêtent si besoin. Un parcours complet coûte moins de 0,01 $.
  - Score mesuré sur les 20 bugs, trois essais par limite :
    - 20 actions : entre 0 et 3 bugs trouvés, 0 fausse alerte, environ 0,01 $ par essai ;
    - 40 actions : entre 4 et 5 bugs trouvés, 0 fausse alerte, environ 0,025 $ par essai.
  - Les bugs SPEC-01, SPEC-02, SPEC-04 et SPEC-06 reviennent presque à chaque essai. Chaque essai s'arrête à la limite d'actions.
- **Garde-fous** :
  - chaque parcours s'arrête à 20 actions, 3 minutes ou 0,05 $ de coût estimé, selon la première limite atteinte (réglable dans `application.properties`) ;
  - le coût est calculé à partir des jetons indiqués par l'API et figure dans la réponse ;
  - une erreur de l'API (clé invalide, crédit épuisé) arrête le parcours proprement ;
  - le navigateur n'ouvre que les hôtes autorisés, y compris après un clic sur un lien externe ;
  - tout outil inconnu est refusé ;
  - chaque bug signalé est validé avant d'entrer dans le rapport JSON ;
  - le contenu des pages est traité comme une donnée, jamais comme une instruction.
- **Mode observation** (désactivé par défaut) : fenêtre Chromium visible, pause d'une seconde entre les actions et journal en français dans la console du backend.
- **Historique** : chaque parcours est enregistré dans PostgreSQL, avec ses bugs. Chaque bug peut être validé ou rejeté par l'API.
- **Mémoire** : les bugs validés par une personne sont présentés à l'agent au parcours suivant, sur le même site, pour qu'il ne les signale pas à nouveau.
- **Vision** : pour un défaut visuel (image absente, mise en page cassée), l'agent prend une capture d'écran et la regarde, au lieu de se fier au seul texte de la page.
- **Vérificateur** : chaque bug signalé est revérifié par un second appel à Claude, à partir des observations. Seul un bug confirmé entre dans le rapport. Chaque vérification coûte un petit montant, compté dans le coût du parcours.
- **Planificateur** : avant le test, un premier appel à Claude découpe la consigne en 2 à 5 étapes, qui guident le testeur. Sans plan, l'agent travaille quand même.
- **Traces et reprises** : chaque action d'un parcours est gardée dans l'historique, dans l'ordre (`GET /api/runs/{id}`). Une erreur passagère de l'API (trop de requêtes, serveur indisponible) est reprise deux fois avant d'arrêter le parcours.
- **Écran de l'historique** : http://localhost:8080/ liste les parcours, affiche les actions de chacun et permet de valider ou rejeter les bugs. C'est une page HTML simple, sans Angular.
- **Intégration continue** : GitHub Actions lance les tests du backend et ceux de la mesure à chaque envoi. Aucune clé API n'est nécessaire.
- **Tests** : 100 tests backend, dont un qui démarre l'application complète, tous réussis lors de la dernière exécution (`mvn clean test`).

## Limites connues

- Le score reste loin de la cible de 15 bugs sur 20 : 4 à 5 bugs à 40 actions, mesurés sur quelques essais seulement.
- La vision, le vérificateur, le planificateur et la mémoire sont testés en mode simulé. Ils n'ont pas encore été validés sur plusieurs parcours réels avec l'API.
- Parfois, `signaler_bug` est appelé sans indiquer la gravité : l'action est alors gaspillée.
- L'écran de l'historique est vérifié par un test sur son contenu, pas par un test dans un navigateur.

## Prochaines étapes

- Améliorer le score : répéter chaque mesure pour connaître sa variabilité, puis chercher comment trouver davantage de bugs dans la limite d'actions.
- Corriger l'appel de `signaler_bug` sans gravité.
- Valider la vision, le vérificateur et le planificateur sur des parcours réels.

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

Le backend enregistre l'historique dans PostgreSQL. Lance d'abord la base, avec Docker Desktop ouvert :

```powershell
docker compose up -d
cd backend
mvn test
mvn spring-boot:run
```

Les tests n'ont pas besoin de PostgreSQL : ils utilisent une base H2 en mémoire.

Dans un second terminal :

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/ask -ContentType "application/json; charset=utf-8" -Body '{"prompt":"Bonjour"}'
```

## Historique et validation des bugs

Chaque parcours de l'agent est enregistré avec ses bugs, dans PostgreSQL. Une personne peut ensuite valider ou rejeter chaque bug.

```powershell
# Lister les parcours
Invoke-RestMethod -Uri http://localhost:8080/api/runs | ConvertTo-Json -Depth 4

# Voir un parcours et ses bugs (remplace 1 par l'identifiant voulu)
Invoke-RestMethod -Uri http://localhost:8080/api/runs/1 | ConvertTo-Json -Depth 6

# Valider un bug (remplace 3 par l'identifiant du bug ; statut : VALIDE, REJETE ou EN_ATTENTE)
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/bugs/3/validation -ContentType "application/json" -Body '{"statut":"VALIDE"}'
```

Un bug démarre en `EN_ATTENTE`. Tant qu'il n'est pas validé, il ne doit pas être compté comme un vrai bug.

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

## Produire la vidéo de démonstration

La vidéo est générée à partir d'un parcours écrit en YAML (`demo/journeys/techshop.yaml`). L'outil [demo-studio](https://github.com/oumar5/demo-studio) filme le site avec Playwright, puis assemble le MP4. Il est installé hors du dépôt. Sa licence ne permet pas de le republier ici.

```powershell
# Une fois : installer l'outil, hors du dépôt
git clone https://github.com/oumar5/demo-studio D:\Outils\demo-studio
cd D:\Outils\demo-studio
npm install
npm run build
npx playwright install chromium
```

Lance ensuite le site de test (port 4200) et le backend en mode simulé. Lance un parcours de l'agent pour que l'historique ne soit pas vide, puis :

```powershell
cd demo
node D:\Outils\demo-studio\dist\cli.js all --journey techshop --project demo.config.yaml
```

La vidéo est écrite dans `demo/output/` (non versionnée), avec une voix off et des sous-titres en français. Elle dure environ 45 secondes.

La voix off est créée sur la machine par [Chatterbox](https://github.com/resemble-ai/chatterbox), sans service en ligne. Il faut Python 3.11, FFmpeg et une carte graphique NVIDIA (le processeur fonctionne aussi, plus lentement). Le modèle de voix, environ 3 Go, est téléchargé une fois au premier lancement.

```powershell
# Une fois : environnement Python isolé pour la voix
python -m venv D:\Outils\chatterbox-venv
D:\Outils\chatterbox-venv\Scripts\python.exe -m pip install torch==2.6.0 torchaudio==2.6.0 --index-url https://download.pytorch.org/whl/cu124
D:\Outils\chatterbox-venv\Scripts\python.exe -m pip install "git+https://github.com/resemble-ai/chatterbox"
```

Crée ensuite `demo/.env` (non versionné) avec `CHATTERBOX_PYTHON=D:\Outils\chatterbox-venv\Scripts\python.exe`, et ajoute le dossier `bin` de FFmpeg au `PATH` du terminal avant de lancer la génération.

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
