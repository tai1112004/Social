// End-to-end helpers for checking the mobile app against a local backend.
// Usage: node tools/e2e/e2e-check.mjs <setup|watch|send|burst|seen-all|password> [...args]
// Test accounts are written to tools/e2e/.e2e-accounts.local.json (local dev data only).
import { randomBytes } from 'node:crypto';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { StompClient, delay } from '../performance/lib/stomp-utils.mjs';

const BASE_URL = process.env.BASE_URL || 'http://localhost:8081';
const WS_URL = BASE_URL.replace(/^http/, 'ws') + '/ws';
const ACCOUNTS_FILE = join(dirname(fileURLToPath(import.meta.url)), '.e2e-accounts.local.json');

async function api(method, path, body, token) {
  const response = await fetch(BASE_URL + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const text = await response.text();
  if (!response.ok) throw new Error(`${method} ${path} -> ${response.status} ${text}`);
  return text ? JSON.parse(text) : null;
}

const loadAccounts = () => JSON.parse(readFileSync(ACCOUNTS_FILE, 'utf8'));

async function login(account) {
  const auth = await api('POST', '/auth/login', {
    email: account.email,
    password: account.password,
    deviceId: account.deviceId,
    deviceName: 'E2E script',
    deviceType: 'SCRIPT',
    os: 'Node',
    // Trusted so repeated script logins are not challenged; the emulator device stays untrusted.
    trustDevice: true
  });
  if (auth.loginStatus !== 'SUCCESS') throw new Error(`${account.label} login status ${auth.loginStatus}`);
  return auth.token;
}

async function connect(account) {
  const client = new StompClient({ token: await login(account), wsUrl: WS_URL, name: account.label });
  await client.connect();
  return client;
}

async function setup() {
  if (existsSync(ACCOUNTS_FILE)) throw new Error(`${ACCOUNTS_FILE} already exists; delete it to create new accounts`);
  const run = Date.now().toString(36);
  const accounts = {};
  for (const label of ['A', 'B', 'C']) {
    const account = {
      label,
      userName: `E2E ${label} ${run}`,
      email: `e2e-${label.toLowerCase()}-${run}@example.test`,
      password: randomBytes(12).toString('base64url'),
      deviceId: `e2e-script-${label.toLowerCase()}-${run}`
    };
    const registered = await api('POST', '/auth/register', {
      userName: account.userName, email: account.email, password: account.password
    });
    account.id = registered.userId;
    // First login records the script device, so a later login from the emulator is a "new device".
    await login(account);
    accounts[label] = account;
  }

  const tokenB = await login(accounts.B);
  const group = await api('POST', '/user/conversations/group', {
    name: `E2E nhom ${run}`, memberIds: [accounts.A.id, accounts.C.id]
  }, tokenB);
  const groupId = group.conversationId ?? group.id;

  writeFileSync(ACCOUNTS_FILE, JSON.stringify({ run, groupId, accounts }, null, 2));
  console.log(JSON.stringify({
    run, groupId,
    users: Object.values(accounts).map(({ label, id, email }) => ({ label, id, email }))
  }, null, 2));
}

// Prints every frame B and C receive on the given queues for `seconds`.
async function watch(seconds = '60', ...queues) {
  const { accounts } = loadAccounts();
  const destinations = queues.length ? queues : ['messages', 'messages.seen'];
  const clients = await Promise.all([accounts.B, accounts.C].map(connect));
  for (const client of clients) {
    for (const queue of destinations) {
      client.subscribe(`/user/queue/${queue}`, (payload) => {
        const summary = typeof payload === 'object'
          ? { ...payload, content: payload?.content ? `${payload.content.slice(0, 40)}... (${payload.content.length} chars)` : payload?.content }
          : payload;
        console.log(`[${new Date().toISOString()}] ${client.name} <- ${queue} ${JSON.stringify(summary)}`);
      });
    }
  }
  console.log(`watching ${destinations.join(', ')} as B and C for ${seconds}s`);
  await delay(Number(seconds) * 1000);
  await Promise.all(clients.map((client) => client.close()));
}

// Sends a group message as B or C, optionally `length` characters long.
async function send(label = 'B', length = '0', text = '') {
  const { accounts, groupId } = loadAccounts();
  const client = await connect(accounts[label]);
  let content = text || `Tin tu ${label} luc ${new Date().toLocaleTimeString()}`;
  if (Number(length) > 0) content = (content + ' ').repeat(Math.ceil(Number(length) / (content.length + 1))).slice(0, Number(length));
  const acknowledged = new Promise((resolve, reject) => {
    client.subscribe('/user/queue/messages.ack', resolve);
    client.subscribe('/user/queue/errors', (error) => reject(new Error(JSON.stringify(error))));
    setTimeout(() => reject(new Error('no ack within 10s')), 10000);
  });
  client.send('/app/chat.send', { conversationId: groupId, clientMessageId: `e2e-${randomBytes(6).toString('hex')}`, content, uploadIds: [] });
  const ack = await acknowledged;
  console.log(`${label} sent message ${ack.id} (${ack.content.length} chars)`);
  await client.close();
}

// B and C both mark the group as seen, which should produce receipts for A's messages.
async function seenAll() {
  const { accounts, groupId } = loadAccounts();
  for (const account of [accounts.B, accounts.C]) {
    const response = await api('POST', `/user/conversations/${groupId}/seen`, undefined, await login(account));
    console.log(`${account.label} marked seen: ${JSON.stringify(response)}`);
  }
}

// Sends `count` chat messages at once over one STOMP connection; resolves with every ack and error.
async function sendMany(account, conversationId, count) {
  const client = await connect(account);
  const results = [];
  const finished = new Promise((resolve) => {
    const record = (result) => { results.push(result); if (results.length === count) resolve(); };
    client.subscribe('/user/queue/messages.ack', (ack) => record({ ok: true, ack }));
    client.subscribe('/user/queue/errors', (error) => record({ ok: false, error }));
    setTimeout(resolve, 15000);
  });
  await delay(300); // let the subscriptions register before the burst
  for (let i = 0; i < count; i++) {
    client.send('/app/chat.send', {
      conversationId, clientMessageId: `burst-${randomBytes(6).toString('hex')}`, content: `burst ${account.label} ${i}`, uploadIds: []
    });
  }
  await finished;
  await client.close();
  return results;
}

// Checks that the conversation's latest messages have distinct sequence numbers and include every acked id.
async function checkSequences(label, conversationId, ackedIds, token) {
  const page = await api('GET', `/user/conversations/${conversationId}/messages?limit=100`, undefined, token);
  const counts = new Map();
  for (const message of page.items) counts.set(message.sequenceNumber, (counts.get(message.sequenceNumber) ?? 0) + 1);
  const duplicates = [...counts].filter(([, count]) => count > 1).map(([sequence]) => sequence);
  const present = new Set(page.items.map((message) => message.id));
  const missing = ackedIds.filter((id) => !present.has(id));
  console.log(`${label}: ${ackedIds.length} acked, duplicate sequences ${JSON.stringify(duplicates)}, missing from history ${JSON.stringify(missing)}`);
}

// Races messages from several senders (and story replies against normal messages) and checks the sequence numbers.
async function burst(perUser = '5') {
  const { accounts, groupId } = loadAccounts();
  const count = Number(perUser);
  const tokens = {};
  for (const account of Object.values(accounts)) tokens[account.label] = await login(account);
  const errorsOf = (results) => results.filter((result) => !result.ok).map((result) => result.error);

  // 1. A, B and C write to the group at the same time.
  const group = (await Promise.all(['A', 'B', 'C'].map((label) => sendMany(accounts[label], groupId, count)))).flat();
  console.log(`group: ${group.length} results, errors ${JSON.stringify(errorsOf(group))}`);
  await checkSequences('group', groupId, group.filter((r) => r.ok).map((r) => r.ack.id), tokens.B);

  // 2. In the B-C chat, B's normal messages race B's replies to C's story.
  await delay(1500); // stay under the 5 messages/second rate limit
  const story = await api('POST', '/user/stories', { type: 'TEXT', privacy: 'PUBLIC', text: `E2E burst ${Date.now()}` }, tokens.C);
  const direct = await api('POST', '/user/conversations/direct', { recipientId: accounts.C.id }, tokens.B);
  const reply = (token) => api('POST', `/user/stories/${story.id}/reply`, {
    clientMessageId: `reply-${randomBytes(6).toString('hex')}`, content: 'tra loi story'
  }, token);
  const [messages, replies] = await Promise.all([
    sendMany(accounts.B, direct.id, count),
    Promise.allSettled(Array.from({ length: count }, () => reply(tokens.B)))
  ]);
  const failedReplies = replies.filter((r) => r.status === 'rejected').map((r) => r.reason.message);
  console.log(`direct B-C: message errors ${JSON.stringify(errorsOf(messages))}, story reply errors ${JSON.stringify(failedReplies)}`);
  await checkSequences('direct B-C', direct.id, [
    ...messages.filter((r) => r.ok).map((r) => r.ack.id),
    ...replies.filter((r) => r.status === 'fulfilled').map((r) => r.value.id)
  ], tokens.B);

  // 3. A replies to the story several times at once; every reply must land in the same A-C chat.
  const fromA = await Promise.allSettled(Array.from({ length: count }, () => reply(tokens.A)));
  const chats = new Set(fromA.filter((r) => r.status === 'fulfilled').map((r) => r.value.conversationId));
  console.log(`A story replies: ${fromA.filter((r) => r.status === 'fulfilled').length}/${count} ok, chats used ${JSON.stringify([...chats])}, errors ${JSON.stringify(fromA.filter((r) => r.status === 'rejected').map((r) => r.reason.message))}`);

  await api('DELETE', `/user/stories/${story.id}`, undefined, tokens.C);
}

// Prints A's email or password for typing into the emulator, without echoing other secrets.
function password(label = 'A', field = 'password') {
  process.stdout.write(loadAccounts().accounts[label][field]);
}

const [command, ...args] = process.argv.slice(2);
const commands = { setup, watch, send, burst, 'seen-all': seenAll, password };
if (!commands[command]) {
  console.error(`Unknown command. Use one of: ${Object.keys(commands).join(', ')}`);
  process.exit(1);
}
await commands[command](...args);
