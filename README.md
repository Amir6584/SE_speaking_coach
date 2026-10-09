# Swedish Speak Coach v28 — rigorous A1–B2 grammar

v28 keeps the working Swedish CPT 135M Q8 model and eight-part Android packaging, but makes correction stricter.

Key changes:
- always runs two independent completion passes;
- rejects meta/chat answers such as “fix it”;
- validates candidates against A1–B2 Swedish grammar checks;
- fixes number + noun form (`två bil` → `två bilar`, `2 barnen` → `2 barn`);
- fixes article/adjective agreement;
- fixes possessive/indefinite agreement;
- fixes modal/`att` + infinitive;
- fixes common `har/hade + wrong verb form` errors;
- distinguishes main-clause and subordinate-clause negation;
- fixes basic direct and indirect question word order;
- checks V2, `sedan + starting point`, and strong future/tense conflicts;
- accepts model-only corrections when they are conservative and valid instead of always preferring the unchanged sentence.

The grammar profile uses Rivstart A1/A2 + B1/B2 as a coverage target without reproducing textbook content.

## v29 Kotlin build fix

v29 keeps the v28 rigorous grammar/model pipeline unchanged and fixes two malformed Kotlin source files that prevented compilation:

- `CoachEngine.kt`: valid `suspend (WordSuggestion) -> Unit` callback signature and fully reformatted source.
- `MainActivity.kt`: UI helpers moved to normal class methods and the compressed one-line function body removed.

A standalone Kotlin parser pass reports no syntax/parser errors. Android dependency references are resolved by the normal Gradle build.
