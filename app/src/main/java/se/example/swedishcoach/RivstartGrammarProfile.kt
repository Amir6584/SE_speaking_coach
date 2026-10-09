package se.example.swedishcoach

object RivstartGrammarProfile {
    val compactChecklist: String = """
Kontrollera noggrant: en/ett, singular/plural, obestämd/bestämd form, tal + plural,
adjektivkongruens, possessiva/reflexiva/indefinita pronomen, verbets infinitiv/presens/
preteritum/perfekt/futurum, hjälpverb och modalverb, V2 i huvudsats, BIFF i bisats,
frågeord och indirekta frågor, tidsuttryck, prepositioner, adverb/adjektiv, relativa
satser, passiv, particip, konditionalis, partikelverb och naturlig svensk ordföljd.
Bevara betydelse, namn, tal och tidsreferens. Svara endast med den korrigerade meningen.
""".trimIndent()

    val examples: String = """
Fel: Jag köpte en hus.
Rätt: Jag köpte ett hus.
Fel: Jag har 2 barnen.
Rätt: Jag har 2 barn.
Fel: Jag har två bil.
Rätt: Jag har två bilar.
Fel: Jag har två stor bilar.
Rätt: Jag har två stora bilar.
Fel: Min hus är stor.
Rätt: Mitt hus är stort.
Fel: Jag har ingen hus.
Rätt: Jag har inget hus.
Fel: Jag kan pratar svenska.
Rätt: Jag kan prata svenska.
Fel: Jag har gick hem.
Rätt: Jag har gått hem.
Fel: Idag jag jobbar hemma.
Rätt: Idag jobbar jag hemma.
Fel: Jag inte kommer idag.
Rätt: Jag kommer inte idag.
Fel: Eftersom jag kommer inte idag stannar hon hemma.
Rätt: Eftersom jag inte kommer idag stannar hon hemma.
Fel: Var du bor?
Rätt: Var bor du?
Fel: Jag vet inte var bor han.
Rätt: Jag vet inte var han bor.
Fel: Jag bor i Sverige sedan 1990.
Rätt: Jag har bott i Sverige sedan 1990.
Fel: Jag har ätit lunch imorgon.
Rätt: Jag ska äta lunch imorgon.
""".trimIndent()
}
