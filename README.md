
# Prism Mock + Contract Testing POC (Java + Spring Boot)

A production-style **mock server & contract testing** proof‑of‑concept you can demo end‑to‑end:

- **Mock servers** for two OpenAPI specs (Accounts, Petstore) using **Prism**
- **Java 17** Spring Boot consumer with **WebClient**
- **Contract tests** using Atlassian's **swagger-request-validator** + **RestAssured**
- **Windows-friendly workflows** (no WSH popups), robust waits, and CI-ready structure

> Built to show an engineering manager how consumer‑driven testing and API-first development look in practice.

---

## 1) Architecture (What’s happening)

```text
        +------------------+
        |  Spring Boot app |    WebClient calls
        | (consumer tests) +-----------> http://127.0.0.1:4010  (Accounts Mock)
        +------------------+             http://127.0.0.1:4020  (Petstore Mock)
                 |
                 | RestAssured HTTP calls in integration tests
                 v
        +--------------------------+
        | Contract Validator       |
        | (swagger-request-... )   |
        |   • Validates responses  |
        |     against OpenAPI      |
        +--------------------------+
```

**Flow**

1. Prism starts from your OpenAPI files and serves **mock responses**.
2. Tests hit the mock endpoints and **assert on HTTP** (status/body).
3. The contract validator checks that responses **conform** to the OpenAPI schema and examples.
4. If Prism or the consumer deviates, the test fails → fast feedback.

---

## 2) Repo layout

```
prism-poc/
├─ api/                  # OpenAPI specs (used by Prism + tests)
│  ├─ accounts.yaml
│  └─ petstore.yaml
├─ mock/                 # Mock-server runner (Prism)
│  └─ package.json       # npm scripts / dev deps
└─ app/                  # Spring Boot consumer + contract tests
   ├─ pom.xml
   ├─ src/main/java/com/example/demo/...
   └─ src/test/java/tests/...
```

Key test files:

- `tests/ContractsIT.java` → calls **Accounts** mock via WebClient and asserts structure
- `tests/PetstoreIT.java` → uses **RestAssured** to call Petstore and validates with **OpenAPI**

---

## 3) Prerequisites

- **Java 17** (or higher). Verify: `java -version`
- **Maven 3.9+**. Verify: `mvn -v`
- **Node.js 18+**. Verify: `node -v && npm -v`

> On Windows, allow Node.js through Defender Firewall when prompted.

---

## 4) Run the mocks (Prism)

**Option A (recommended on Windows):** start each mock with `npx` (no custom .js, no WSH popups).

```bash
# Terminal 1 (Accounts on 4010)
cd mock
npx -y @stoplight/prism-cli mock ../api/accounts.yaml -p 4010 --errors --cors --dynamic --log=info
```

```bash
# Terminal 2 (Petstore on 4020)
cd mock
npx -y @stoplight/prism-cli mock ../api/petstore.yaml -p 4020 --errors --cors --dynamic --log=info
```

Verify in a browser:

- `http://127.0.0.1:4010/accounts`
- `http://127.0.0.1:4020/pets`

> Logs use Unicode icons (`√`, `×`, `►`). They’re English with icons. For plain text, add `--log=debug`.

**What Prism does**

- Reads OpenAPI and **validates your request** shape.
- **Negotiates** a response: chooses examples/content type and returns realistic samples.
- Can be forced to return errors using `Prefer` headers in tests.

---

## 5) Run contract tests

Keep both Prism terminals running, then in a new terminal:

```bash
cd app
mvn -Dskip.mocks=true -U clean verify
```

- `ContractsIT` hits `http://127.0.0.1:4010/accounts`
- `PetstoreIT` hits `http://127.0.0.1:4020/pets`
- The **validator** checks each response against OpenAPI (`api/*.yaml`).

**What the validator checks**

- Response status matches defined responses (`200`, `400`, `404`, …)
- Body schema (types, required fields) conforms to OpenAPI
- Content type matches the operation’s `content` definition

---

## 6) Demo script (step-by-step)

**Goal:** Explain each layer and show green tests.

1. **Open API specs** (`api/accounts.yaml`, `api/petstore.yaml`)
   - Show realistic examples (e.g., “Chequing Account”, “Bella the DOG”).
   - Point out schemas and responses.

2. **Start mocks** (two terminals with `npx prism ...`)
   - Show the ports and test URLs in the browser.
   - Explain the Unicode icons in logs (success/error/start).

3. **Show the Spring client** (`app/src/main/java/.../AccountsClient.java`)
   - Explain base URL and simple WebClient usage.

4. **Explain contract tests**
   - `ContractsIT` (WebClient + StepVerifier).
   - `PetstoreIT` (RestAssured + validator).
   - Mention validator = **swagger-request-validator-core**.

5. **Run tests**
   ```bash
   cd app && mvn -Dskip.mocks=true -U clean verify
   ```
   - Show green build. Emphasize contract enforcement.

6. **(Optional) Break it on purpose** (demo value)
   - Edit `accounts.yaml` to remove `name` or change its type to `integer`.
   - Re-run tests → **fail** → shows safety net of contracts.
   - Revert the change.

7. **Wrap-up**
   - API-first dev
   - Consumer and provider safety
   - Reproducible mocks for frontend & QA
   - CI friendly

_Time guide_: 8–12 minutes total.

---

## 7) IntelliJ quickstart

- **Open** the project: `File → Open → prism-poc/app/pom.xml`
- Ensure **Project SDK = 17**
- Run tests via Maven panel or `mvn verify`

To run mocks from IntelliJ Terminal, use the `npx` commands above.

---

## 8) How the “realistic names” work

We updated OpenAPI examples to realistic values:

- **Accounts**: Chequing, Savings, Business, USD, Investment (TFSA)
- **Pets**: Bella (DOG), Max (CAT), Charlie (BIRD)…

Prism returns these **examples** or generates dynamic data that still validates against the schema. This makes the demo look professional and readable.

---

## 9) Troubleshooting (Windows-friendly)

- **WSH popup when running npm scripts**: use `npx @stoplight/prism-cli ...` as above (no custom `.js` runner).
- **`Connection refused` in tests**: ensure Prism is running, ports open, and tests use `http://127.0.0.1`.
- **Port in use**: change `-p 4010/4020` in the `npx` command **and** update tests base URLs accordingly.
- **Java version error (`release 21 not supported`)**: switch project to Java 17 in `pom.xml` and IntelliJ.
- **OpenAPI validation complaints**: read the first error in the test output; it points to the mismatched field or path.

---

## 10) Extending the POC

- Add more endpoints/examples to the OpenAPI files
- Write additional contract tests for `POST`, `PUT`, `DELETE`
- Gate CI on `mvn verify` (GitHub Actions)
- Introduce consumer-driven contracts by pinning example payloads for specific use cases

---

## 11) Commands reference

**Start mocks**

```bash
# Accounts
npx -y @stoplight/prism-cli mock ../api/accounts.yaml -p 4010 --errors --cors --dynamic --log=info

# Petstore
npx -y @stoplight/prism-cli mock ../api/petstore.yaml -p 4020 --errors --cors --dynamic --log=info
```

**Run tests**

```bash
cd app
mvn -Dskip.mocks=true -U clean verify
```

**Curl sanity checks**

```bash
curl http://127.0.0.1:4010/accounts
curl "http://127.0.0.1:4020/pets?limit=3"
```

---

## 12) Why this approach is valuable

- **Fast feedback**: catches mismatches before provider/backend is ready
- **Stable demos**: no external dependencies; works offline
- **Cross-team alignment**: OpenAPI is the source of truth
- **CI ready**: can run headless on Linux agents

---

## 13) License

MIT (or choose your company’s standard license)
