const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
});

test("Authenticated user: Alice can create a note in her own collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      title: "Reflection on Surah Al-Fatihah",
      content: "The opening surah encapsulates Tawhid and guidance.",
      category: "tadabbur",
      surahNumber: 1,
      ayahStart: 1,
      ayahEnd: 7,
      tags: ["tawhid", "guidance"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: Bob cannot read Alice's note", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      title: "Secret reflection",
      content: "Private research",
      category: "theology",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").get());
});

test("Authenticated user: Alice can query her own notes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
});

test("Authenticated user: Alice can create a MindMap", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("mindmaps").doc("map_1").set({
      userId: ALICE_UID,
      title: "Themes of Surah Al-Baqarah",
      description: "Core theological narrative arcs",
      surahNumber: 2,
      nodesJson: "[]",
      edgesJson: "[]",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: Bob cannot update Alice's MindMap", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("mindmaps").doc("map_1").set({
      userId: ALICE_UID,
      title: "Themes of Surah Al-Baqarah",
      nodesJson: "[]",
      edgesJson: "[]",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("mindmaps").doc("map_1").update({
      title: "Hacked Title",
      updatedAt: new Date(),
    })
  );
});
