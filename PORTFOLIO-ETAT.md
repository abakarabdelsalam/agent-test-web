# 📊 Portfolio - État global

**Date :** 12 novembre 2026 - 18:45  
**Étape :** Passage Projet A → Projet B  
**Prochain jalon :** Mesure de variance et PR inédites (Projet B), test Jenkins

---

## Projet A : ✅ Terminé (Score mesuré)

### État
- **Durée :** 22 jours (achevé)
- **Status :** Fonctionnel, mesures prises
- **Code :** 111 tests, zéro dépendance API

### Résultats
```
Score final :          1,33 bugs/parcours (cible : 15)
Faux positifs :        0
Précision :            100%
Rappel :               8.8%
Coût moyen :           0,04 $ / parcours
Temps moyen :          ~60 sec / parcours
```

### Livrables
- ✅ Backend Spring Boot (Java 25, 8 outils)
- ✅ Frontend TechShop (Angular 22, 20 bugs)
- ✅ Historique PostgreSQL
- ✅ Boucle d'agent autonome
- ✅ Vérificateur secondaire (jour 22 : parsing tolérant)
- ✅ Interface HTML5 (thème clair/sombre)
- ✅ README avec commandes
- ✅ Cahier de charges (avec CV)
- ✅ Mesures en mode réel (3 essais)

### Artefacts
- Cahier de charges A : https://claude.ai/artifact/R8MBjgFEQGNifwAAaphBw7
- Architecture : https://claude.ai/artifact/XN2b5ZiACS2ZzBBy1QrmYa
- Squelette : https://claude.ai/artifact/6shCBEunfXeBhwmw4mk8t2

---

## Projet B : ✅ Jours 6-14 réalisés, mesure en cours

### État
- **Phase :** Agent, corpus, mesures et Jenkins terminés
- **Status :** Agent de non-régression opérationnel en mode réel
- **Localisation :** dépôt séparé `D:\Projets\petclinic-agent-b` (`mcp-server/`, `agent-b/`, `evaluation-b/`), PetClinic cloné dans `agent-test-web` (`petclinic-rest/` port 9966, `petclinic-angular/` port 4300)
- **Dépôts :** `petclinic-*` sont des clones ignorés par git ; `petclinic-repo/` (Thymeleaf) n'est plus utilisé

### Jour 6 - Livrables
```
✅ Docker Compose lancé (PostgreSQL + MySQL)
✅ Spring Boot compilé et en écoute (port 8080)
✅ Code source cloné (spring-projects/spring-petclinic)
✅ Documentation d'installation
✅ Plan détaillé Jours 6-20
⏳ Frontend Angular (à installer J7)
```

### Roadmap
| Jour | Objectif | État |
|------|----------|------|
| 7 | Serveur MCP Playwright | ✅ 9 outils, démo réelle |
| 8 | Outils HTTP | ✅ `appeler_api` |
| 9 | Jeu d'évaluation | ✅ 10 régressions, 5 PR saines |
| 10 | Analyse du diff | ✅ `lire_diff`, `analyser_diff` |
| 11 | Flux complet | ✅ Agent et vérificateur, mesuré |
| 12 | Jenkins | ✅ CI verte, vraie PR GitHub testée et commentée par l'agent |
| 13 | Vérificateur et faux positifs | ✅ Verdict par outil, retry réseau |
| 14 | Mesure + portfolio | ⚠️ Mesuré, voir ci-dessous |

### Résultats mesurés (Haiku 4.5, mode réel)
- Corpus de réglage : 7/10 régressions, 0/5 fausses alertes (optimiste : prompts ajustés dessus).
- Corpus de contrôle : 2/4 régressions sur deux runs, 0/2 fausses alertes.
- Corpus vierge (jamais vu avant le run) : 3/3 régressions relues à la main, 0/2 fausses alertes. Un seul essai.
- Coût moyen : environ 0,07 $ par PR.

### Cibles de performance
- Détecter : 7/10 régressions
- Faux positifs : ≤ 1/5 PR saines
- Durée : 2-5 min par test
- Coût : 0,02-0,05 $/test

### Artefacts
- Cahier de charges B : https://claude.ai/artifact/AjfRevfvYhc3fZ8FwD36nm
- **Combined A+B** : https://claude.ai/artifact/WTHG2HqNWaMgpvT62rJef9 ← Menu principal

---

## Architecture globale

```
┌─────────────────────────────────────────────────────────────┐
│  Portfolio : Agent IA - Deux projets                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Projet A (J1-5, 15-22)    │    Projet B (J6-14)          │
│  ═══════════════════════   │    ════════════════           │
│  Test de sites web         │    Non-régression API        │
│  • TechShop (Angular)      │    • PetClinic               │
│  • 20 bugs intentionnels   │    • Spring Boot             │
│  • Claude Haiku 4.5        │    • Docker                  │
│  • Score : 1,33 bugs       │    • Jenkins (jour 12)       │
│  • 111 tests               │    • Cible : 7/10 régr.      │
│  ✅ DONE                   │    🚀 Day 6 Complete         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Fichiers clés du portfolio

### Cahiers de charges
- `cahier-charges-combined.html` (A+B avec menu) ← **Lien principal**
- `cahier-charges-a.html` (Projet A seul)
- `cahier-charges-b.html` (Projet B seul)

### Documentation technique
- `backend/` (Java, Spring Boot, 111 tests)
- `test-site/` (Angular, TechShop, 20 bugs)
- `evaluation/` (Script de mesure)
- `petclinic-repo/` (PetClinic clone)
- `docs/plan-22-jours.md` (Chronologie)
- `docs/plan-projet-b.md` (Roadmap J6-20)

### Synthèses
- `README.md` (Overview global)
- `PORTFOLIO-ETAT.md` (Ce fichier)

---

## Prochaines 24h

### Jour 7 : Playwright MCP (J7 = 13 nov)

**Objectif :** Connecter Claude à PetClinic via serveur MCP

**Étapes :**
1. Créer `server-mcp.py` (Playwright MCP)
2. Exposer outils : ouvrir, lire_page, cliquer, remplir
3. Tester : Claude ouvre PetClinic et voit la page

**Code minimal :**
```python
from mcp.server import Server
server = Server("petclinic-agent")

@server.tool()
def ouvrir(url: str) -> str:
    # Playwright navigation
    pass
```

**Livrables :**
- Serveur MCP opérationnel
- Test : Claude navigue sur PetClinic
- Documentation MCP

---

## Commits récents

```
722d1f7  Jour 6 : Installation de PetClinic
fe1fc95  Mettre à jour le modèle Claude de Haiku 5.5 à Haiku 4.5
c41a241  Mesurer le score final avec le vérificateur corrigé
f075b9f  Lire le verdict du vérificateur malgré la mise en forme
```

---

## Ressources

### Cahier de charges interactif
👉 **https://claude.ai/artifact/WTHG2HqNWaMgpvT62rJef9**

Menu latéral pour naviguer :
- Vue d'ensemble (A & B)
- Projet A (Intro, Architecture, Résultats, CV)
- Projet B (Intro, Jour 6, Architecture, Roadmap)

### Commandes clés

```powershell
# Projet A : Mesurer le score
cd backend
mvn test  # 111 tests
cd ..\evaluation
npm run evaluate

# Projet B : Lancer PetClinic
cd petclinic-rest;    mvn spring-boot:run              # API : http://localhost:9966/petclinic/api/
cd petclinic-angular; npx ng serve --port 4300         # Front : http://localhost:4300

# Projet B : Tester une PR (mode simulé, gratuit)
cd evaluation-b; node evaluate.js R04
```

---

## Points forts du portfolio

✨ **Projet A**
- Approche mesurée (3 essais en mode réel)
- Score public (1,33 bugs) = honnêteté
- 111 tests = fiabilité
- Vérificateur 2e appel = qualité
- Zéro fausse alerte = précision

✨ **Projet B**
- Stack moderne (Docker, Jenkins, Spring Boot 3)
- Problème réel (détecter régressions)
- Approche holistique (API + Frontend + Diff)
- Roadmap claire (14 jours, 9 étapes)

✨ **Global**
- Code open (pas de clé API hardcodée)
- Documentation exhaustive
- Deux cas d'usage différents
- Progression linéaire visible

---

## Défis et lessons learned

### Projet A
- **Défi :** Score 91% en dessous de la cible
- **Cause :** Agent cherche bugs visuels d'abord, pas bugs métier
- **Solution :** Planificateur + mémoire (pour J15+)
- **Insight :** Rappel vs Précision : facile d'être précis, difficile d'être exhaustif

### Projet B (en construction)
- **Défi :** Isolation des données (chaque test part d'un état connu)
- **Défi :** Faux positifs (délai réseau, état temporaire)
- **Solution :** Retry + timeout + vérificateur secondaire
- **Défi :** Analyse du diff (mapping endpoint ↔ écran testé)

---

## CV : Lignes pour insérer

### Projekt A
> Conçu et développé un agent IA autonome en Java 25 / Spring Boot qui exécute une boucle d'agent itérative via l'API Claude, gère 8 outils de navigation (Playwright), applique des garde-fous (limites en actions, durée, budget), valide les bugs par un vérificateur IA. 111 tests automatisés, historique PostgreSQL, score mesuré (1,33 bugs/parcours). Défi identifié : score 91% en dessous de la cible, à investiguer.

### Projekt B
> Conçu un agent de non-régression qui teste si une PR sur l'API Spring Boot casse le front Angular. Serveur MCP Playwright, analyse de diff, vérificateur indépendant par outil. Intégré à Jenkins : l'agent teste une PR GitHub et publie son rapport en commentaire. Mesuré en mode réel : 3 régressions sur 3 détectées sur un corpus vierge, 0 fausse alerte sur 2 PR saines, environ 0,07 $ par PR. Sur un autre corpus de contrôle : 2 régressions sur 4.

---

## Contacts & Attributions

**Auteur :** Abdelsalam (abdeldjaach@gmail.com)  
**Mentorat :** Claude Code (Anthropic)  
**Modèle :** Claude Haiku 4.5  
**Date début :** 2 novembre 2026  
**Date milestone :** 12 novembre 2026 (J6 Projet B)  

---

**🎯 Prochaine étape :** mesure de variance et PR inédites (Projet B), test Jenkins  
**📅 Échéance portfolio :** 22 novembre 2026 (Jour 22)
