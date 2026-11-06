const { test } = require('node:test');
const assert = require('node:assert/strict');
const { score, unreachableMessage } = require('./evaluate.cjs');

const BUGS = [
    { id: 'BUG-01', spec: 'SPEC-01' },
    { id: 'BUG-02', spec: 'SPEC-02' },
    { id: 'BUG-03', spec: 'SPEC-03' },
];

test('compte un bug trouvé quand la spécification correspond', () => {
    const result = score(BUGS, { bugs: [{ spec: 'SPEC-01' }] });

    assert.equal(result.found, 1);
    assert.deepEqual(result.missed, ['BUG-02', 'BUG-03']);
    assert.equal(result.falseAlerts, 0);
});

test('compte comme fausse alerte une spécification absente du corrigé', () => {
    const result = score(BUGS, { bugs: [{ spec: 'SPEC-99' }] });

    assert.equal(result.found, 0);
    assert.equal(result.falseAlerts, 1);
});

test('compte comme fausse alerte un bug signalé deux fois', () => {
    const result = score(BUGS, { bugs: [{ spec: 'SPEC-01' }, { spec: 'SPEC-01' }] });

    assert.equal(result.found, 1);
    assert.equal(result.falseAlerts, 1);
});

test('explique comment relancer le backend quand il ne répond pas', () => {
    const message = unreachableMessage('http://localhost:8080/api/agent');

    assert.match(message, /mvn spring-boot:run/);
    assert.match(message, /localhost:8080/);
});

test('ne trouve rien sans rapport', () => {
    const result = score(BUGS, { bugs: [] });

    assert.equal(result.found, 0);
    assert.equal(result.falseAlerts, 0);
    assert.deepEqual(result.missed, ['BUG-01', 'BUG-02', 'BUG-03']);
});
