// Lance l'agent sur TechShop et compare ses bugs au corrigé. Une seule commande : npm run evaluate.
const fs = require('node:fs');
const path = require('node:path');

const AGENT_URL = process.env.AGENT_URL || 'http://localhost:8080/api/agent';
const SITE_URL = 'http://localhost:4200';
// Consigne neutre : elle ne donne ni le nombre de bugs ni leur emplacement.
const INSTRUCTION = 'Vérifie le site TechShop par rapport aux spécifications : parcours le catalogue, '
    + 'les fiches produit et le panier, puis signale chaque écart.';

// Un bug du corrigé est trouvé si l'agent a signalé la même spécification.
// Une alerte est fausse si elle ne correspond à aucun bug du corrigé, ou si elle répète un bug déjà trouvé.
function score(bugs, report) {
    const expected = new Set(bugs.map((bug) => bug.spec));
    const reported = report.bugs.map((bug) => bug.spec);
    const found = new Set(reported.filter((spec) => expected.has(spec)));
    const falseAlerts = reported.length - found.size;
    return {
        found: found.size,
        total: bugs.length,
        falseAlerts,
        missed: bugs.filter((bug) => !found.has(bug.spec)).map((bug) => bug.id),
    };
}

// « fetch failed » ne dit pas quoi faire : on explique que le backend ne répond pas.
function unreachableMessage(url) {
    return `Le backend ne répond pas sur ${url}. Lance-le avec « mvn spring-boot:run » dans le dossier backend, puis relance cette commande.`;
}

async function run() {
    const bugs = JSON.parse(fs.readFileSync(path.join(__dirname, 'bugs.json'), 'utf8'));
    let response;
    try {
        response = await fetch(AGENT_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=utf-8' },
            body: JSON.stringify({ url: SITE_URL, instruction: INSTRUCTION }),
        });
    } catch {
        throw new Error(unreachableMessage(AGENT_URL));
    }
    if (!response.ok) {
        throw new Error(`Le backend a répondu ${response.status}. Vérifie l'URL et les logs du backend.`);
    }
    const run = await response.json();
    const result = score(bugs, run.report);
    console.log(`Arrêt : ${run.endedBy} | actions : ${run.actions.length} | coût : ${run.costUsd.toFixed(4)} $`);
    if (run.costUsd === 0) {
        console.log('⚠ Coût nul : le backend tourne en mode simulé. Ce n\'est pas un score réel.');
    }
    console.log('Bugs signalés par l\'agent :');
    run.report.bugs.forEach((bug) => console.log(`  - ${bug.spec} : ${bug.title}`));
    console.log(`Bugs trouvés : ${result.found}/${result.total}`);
    console.log(`Fausses alertes : ${result.falseAlerts}`);
    console.log(`Non trouvés : ${result.missed.join(', ') || 'aucun'}`);
    fs.writeFileSync(path.join(__dirname, 'resultats.json'), JSON.stringify({ result, run }, null, 2), 'utf8');
}

if (require.main === module) {
    run().catch((error) => {
        console.error(error.message);
        process.exitCode = 1;
    });
}

module.exports = { score, unreachableMessage };
