# Jour 6 - Synthèse : Installation de PetClinic

**Date :** 12 novembre 2026  
**État :** ✅ Jour 6 terminé (Backend en ligne)

## Objectif
Backend et frontend lancés, testables manuellement.

## Livrables complétés

### ✅ Infrastructure Docker
- PostgreSQL 18.4 (port 5432)
- MySQL 9.7 (port 3306)
- Commande : `docker-compose up -d`

### ✅ Backend Spring Boot
- **Compilé** : `mvn clean compile` sans erreurs
- **Lancé** : Processus en arrière-plan (PID 15964)
- **Accessible** : http://localhost:8080/api
- **Port** : 8080 en écoute

### ✅ Documentation
- `SETUP_STATUS.md` : État détaillé, debugging, commandes
- `README-PROJECT-B.md` : Guide complet PetClinic + architecture
- `docs/plan-projet-b.md` : Roadmap Jours 6-20 avec détails par jour

### ✅ Code source
- Cloné depuis GitHub : `spring-projects/spring-petclinic`
- Localisation : `D:\Projets\agent-test-web\petclinic-repo`
- Branches : Main (stable)

## Tests effectués

| Test | Résultat |
|------|----------|
| Docker conteneurs actifs | ✅ PostgreSQL + MySQL running |
| Compilation Maven | ✅ Build réussi (warnings normaux) |
| Port 8080 en écoute | ✅ Spring Boot démarre |
| Frontend Node.js | ⏳ À installer jour 7 (npm install + npm start) |

## Architecture déployée

```
┌─────────────────────────────────────────────┐
│  Jour 6 - Installation PetClinic            │
├─────────────────────────────────────────────┤
│                                             │
│  Frontend           Backend       Databases │
│  (Angular 17)       (Spring Boot) (Docker)  │
│  localhost:4200     localhost:8080          │
│  [À installer]      [✅ En ligne]           │
│  ↕                  ↕                       │
│  /app               /api          PostgreSQL│
│  (npm start)        (REST)        (5432)    │
│                                             │
│  Propriétaires ←→ /api/owners      MySQL    │
│  Animaux       ←→ /api/pets        (3306)   │
│  Visites       ←→ /api/visits              │
│  Vétérinaires  ←→ /api/vets                │
│                                             │
└─────────────────────────────────────────────┘
```

## Prochaines étapes

### Jour 7 : Playwright MCP
- Créer serveur MCP qui expose outils Playwright
- Claude pourra ouvrir http://localhost:4200 et naviguer
- Tester : "Ouvre PetClinic" → Agent voit la page d'accueil

### Jour 8 : Outils HTTP
- Ajouter GET/POST/PUT/DELETE à MCP
- Tester l'API directement (pas juste le frontend)

### Jour 9 : Jeu d'évaluation
- Créer 10 régressions intentionnelles
- Créer 5 PR saines
- Mesurer détection de l'agent

## Commandes d'accès

### Vérifier backend
```powershell
# Tester l'API
curl http://localhost:8080/api/vets

# Résultat attendu
{"vetList":[{"id":1,"firstName":"James",...}]}
```

### Logs et debugging
```powershell
# Voir les logs Spring Boot
Get-Content "$env:TEMP\petclinic.log" -Tail 50

# Vérifier containers
docker ps

# Redémarrer Docker
docker-compose down
docker-compose up -d
```

### Installer frontend (jour 7)
```powershell
cd D:\Projets\agent-test-web\petclinic-repo\frontend
npm install
npm start  # S'ouvre sur http://localhost:4200
```

## Ressources créées

| Fichier | Contenu | Localisation |
|---------|---------|--------------|
| SETUP_STATUS.md | État installation, debugging | `petclinic-repo/` |
| README-PROJECT-B.md | Guide complet PetClinic | `petclinic-repo/` |
| plan-projet-b.md | Roadmap détaillée J6-J20 | `docs/` |
| docker-compose.yml | PostgreSQL + MySQL | `petclinic-repo/` (fourni) |
| pom.xml | Build Spring Boot | `petclinic-repo/` (fourni) |

## Mesures

- **Temps d'installation** : ~30 min (clone + compile + Docker)
- **Taille des images Docker** : ~500 MB (PostgreSQL + MySQL)
- **RAM utilisée** : ~1-2 GB (Docker + Spring Boot)
- **Compilation Maven** : ~45 secondes
- **Démarrage Spring Boot** : ~60-90 secondes

## Points clés pour l'agent (jours 7+)

L'agent aura besoin de :
1. **Accès au frontend** (Playwright)
   - Ouvrir pages
   - Remplir formulaires
   - Cliquer boutons
   - Prendre screenshots

2. **Accès à l'API** (HTTP calls)
   - GET /api/owners → Récupérer liste
   - POST /api/owners → Créer propriétaire
   - PUT /api/owners/{id} → Modifier
   - DELETE /api/owners/{id} → Supprimer

3. **Analyse des changements** (Git diff)
   - Lire la PR
   - Identifier endpoints changés
   - Mapper vers écrans impactés

4. **Rapportage** (JSON)
   - Énumérer régressions trouvées
   - Gravité de chaque régression
   - Étapes de reproduction

## État du portfolio

**Projet A** (Jours 1-5, 15-22) : ✅ Score mesuré (1,33 bugs)  
**Projet B** (Jours 6-14) : ✅ Jour 6 complété, Jours 7-14 en plan

**Prochaines 24h** : Jour 7 (Playwright MCP)

---

**Signature :** Claude Code | Agent Builder  
**Date de mise à jour :** 12 nov 2026 - 18:30
