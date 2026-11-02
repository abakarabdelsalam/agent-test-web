# Spécifications de TechShop

Ce document décrit le comportement attendu de la boutique. L'agent peut le consulter. Tout écart constaté sur le site est un bug.

## Catalogue

- SPEC-01 : la recherche par nom ignore les majuscules et les minuscules.
- SPEC-02 : « Prix croissant » affiche le produit le moins cher en premier, et « Prix décroissant » le plus cher en premier.
- SPEC-03 : le filtre de catégorie n'affiche que les produits de cette catégorie.
- SPEC-04 : un produit en rupture de stock ne peut pas être ajouté au panier. Son bouton est désactivé partout.
- SPEC-05 : le prix d'un produit est le même dans le catalogue et sur sa fiche.
- SPEC-06 : l'image de chaque produit s'affiche.

## Navigation

- SPEC-07 : le lien « Contact » du pied de page ouvre la page Contact.

## Panier

- SPEC-08 : la quantité d'une ligne ne descend jamais sous 1.
- SPEC-09 : le sous-total est la somme de chaque prix multiplié par sa quantité.
- SPEC-10 : « Retirer » supprime uniquement le produit de la ligne concernée.
- SPEC-11 : le code BIENVENUE10 donne 10 % de remise sur le sous-total.
- SPEC-12 : un code promo inconnu affiche le message « Code invalide ».
- SPEC-13 : la livraison coûte 4,90 €. Elle est offerte quand le sous-total atteint 50 € ou plus.
- SPEC-14 : le compteur du panier dans l'en-tête indique toujours le nombre total d'articles du panier.

## Commande

- SPEC-15 : l'e-mail doit avoir un format valide, par exemple nom@domaine.fr.
- SPEC-16 : le code postal contient exactement 5 chiffres.
- SPEC-17 : le total payé sur la confirmation est égal au total du panier, livraison comprise.
- SPEC-18 : le panier est vide après une commande validée.

## Inscription

- SPEC-19 : le mot de passe contient au moins 8 caractères.
- SPEC-20 : les deux mots de passe doivent être identiques.
