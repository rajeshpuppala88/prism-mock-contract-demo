const { spawn } = require("child_process");
const kill = require("tree-kill");
const fs = require("fs");

const pidFile = ".prism-pids.json";

function start(cmd, args) {
  const proc = spawn(cmd, args, { stdio: "inherit", shell: true });
  return proc.pid;
}
function savePids(pids) { fs.writeFileSync(pidFile, JSON.stringify(pids)); }
function loadPids() { return fs.existsSync(pidFile) ? JSON.parse(fs.readFileSync(pidFile)) : []; }

if (process.argv[2] === "stop") {
  for (const pid of loadPids()) { try { kill(pid); } catch (_) {} }
  if (fs.existsSync(pidFile)) fs.unlinkSync(pidFile);
  console.log("✅ Prism servers stopped.");
  process.exit(0);
}

const pids = [];
pids.push(start("npx", ["prism", "mock", "../api/accounts.yaml", "-p", "4010", "--errors", "--cors", "--dynamic"]));
pids.push(start("npx", ["prism", "mock", "../api/petstore.yaml", "-p", "4020", "--errors", "--cors", "--dynamic"]));
savePids(pids);

console.log("✅ Prism mock servers running:");
console.log("   Accounts API → http://localhost:4010");
console.log("   Petstore API → http://localhost:4020");
