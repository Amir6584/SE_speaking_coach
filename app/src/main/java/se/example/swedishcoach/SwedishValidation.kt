package se.example.swedishcoach

object SwedishValidation {
    private enum class Gender { EN, ETT }

    private val nounGender = mapOf(
        "hus" to Gender.ETT, "barn" to Gender.ETT, "jobb" to Gender.ETT, "år" to Gender.ETT,
        "språk" to Gender.ETT, "problem" to Gender.ETT, "möte" to Gender.ETT, "rum" to Gender.ETT,
        "namn" to Gender.ETT, "företag" to Gender.ETT, "bord" to Gender.ETT, "glas" to Gender.ETT,
        "brev" to Gender.ETT, "land" to Gender.ETT, "äpple" to Gender.ETT, "hotell" to Gender.ETT,
        "kontor" to Gender.ETT, "nummer" to Gender.ETT, "exempel" to Gender.ETT, "djur" to Gender.ETT,
        "foto" to Gender.ETT, "arbete" to Gender.ETT, "kök" to Gender.ETT, "badrum" to Gender.ETT,
        "universitet" to Gender.ETT, "sjukhus" to Gender.ETT, "körkort" to Gender.ETT, "kort" to Gender.ETT,
        "pass" to Gender.ETT, "tåg" to Gender.ETT, "yrke" to Gender.ETT, "ansikte" to Gender.ETT,
        "hjärta" to Gender.ETT, "ljus" to Gender.ETT, "syskon" to Gender.ETT,
        "bil" to Gender.EN, "bok" to Gender.EN, "dag" to Gender.EN, "person" to Gender.EN,
        "fråga" to Gender.EN, "lägenhet" to Gender.EN, "vän" to Gender.EN, "biljett" to Gender.EN,
        "stad" to Gender.EN, "vecka" to Gender.EN, "månad" to Gender.EN, "sak" to Gender.EN,
        "familj" to Gender.EN, "skola" to Gender.EN, "restaurang" to Gender.EN, "telefon" to Gender.EN,
        "dörr" to Gender.EN, "plats" to Gender.EN, "resa" to Gender.EN, "idé" to Gender.EN,
        "kvinna" to Gender.EN, "man" to Gender.EN, "tid" to Gender.EN, "gata" to Gender.EN,
        "butik" to Gender.EN, "station" to Gender.EN, "lärare" to Gender.EN, "student" to Gender.EN,
        "kurs" to Gender.EN, "film" to Gender.EN, "hund" to Gender.EN, "katt" to Gender.EN,
        "cykel" to Gender.EN, "regel" to Gender.EN, "möjlighet" to Gender.EN, "vana" to Gender.EN,
        "orsak" to Gender.EN, "effekt" to Gender.EN, "erfarenhet" to Gender.EN, "anledning" to Gender.EN
    )

    private val singularToPlural = mapOf(
        "bil" to "bilar", "bok" to "böcker", "dag" to "dagar", "person" to "personer",
        "fråga" to "frågor", "lägenhet" to "lägenheter", "vän" to "vänner", "biljett" to "biljetter",
        "vecka" to "veckor", "månad" to "månader", "skola" to "skolor", "hund" to "hundar",
        "katt" to "katter", "resa" to "resor", "plats" to "platser", "kurs" to "kurser",
        "film" to "filmer", "regel" to "regler", "möjlighet" to "möjligheter", "vana" to "vanor",
        "orsak" to "orsaker", "erfarenhet" to "erfarenheter", "anledning" to "anledningar",
        "hus" to "hus", "barn" to "barn", "jobb" to "jobb", "år" to "år", "språk" to "språk",
        "problem" to "problem", "rum" to "rum", "namn" to "namn", "företag" to "företag"
    )
    private val pluralForms = singularToPlural.filter { (singular, plural) -> singular != plural }.values.toSet()

    private val definitePluralToIndefinite = mapOf(
        "barnen" to "barn", "husen" to "hus", "jobben" to "jobb", "åren" to "år", "språken" to "språk",
        "problemen" to "problem", "rummen" to "rum", "namnen" to "namn", "företagen" to "företag",
        "bilarna" to "bilar", "böckerna" to "böcker", "dagarna" to "dagar", "personerna" to "personer",
        "frågorna" to "frågor", "lägenheterna" to "lägenheter", "vännerna" to "vänner",
        "biljetterna" to "biljetter", "veckorna" to "veckor", "månaderna" to "månader",
        "skolorna" to "skolor", "hundarna" to "hundar", "katterna" to "katter", "resorna" to "resor",
        "platserna" to "platser", "reglerna" to "regler", "möjligheterna" to "möjligheter"
    )

    private val presentToInfinitive = mapOf(
        "pratar" to "prata", "talar" to "tala", "jobbar" to "jobba", "arbetar" to "arbeta",
        "studerar" to "studera", "läser" to "läsa", "skriver" to "skriva", "köper" to "köpa",
        "säljer" to "sälja", "kommer" to "komma", "går" to "gå", "bor" to "bo", "lever" to "leva",
        "äter" to "äta", "dricker" to "dricka", "sover" to "sova", "säger" to "säga", "gör" to "göra",
        "ser" to "se", "tar" to "ta", "ger" to "ge", "vet" to "veta", "förstår" to "förstå",
        "behöver" to "behöva", "försöker" to "försöka", "börjar" to "börja", "slutar" to "sluta",
        "använder" to "använda", "spelar" to "spela", "lär" to "lära", "tränar" to "träna",
        "väntar" to "vänta", "kör" to "köra", "reser" to "resa", "ringer" to "ringa",
        "träffar" to "träffa", "hjälper" to "hjälpa", "frågar" to "fråga", "svarar" to "svara"
    )

    private val pastToInfinitive = mapOf(
        "åt" to "äta", "drack" to "dricka", "sov" to "sova", "jobbade" to "jobba", "arbetade" to "arbeta",
        "studerade" to "studera", "bodde" to "bo", "var" to "vara", "gick" to "gå", "kom" to "komma",
        "reste" to "resa", "körde" to "köra", "läste" to "läsa", "skrev" to "skriva", "gjorde" to "göra",
        "såg" to "se", "sa" to "säga", "tog" to "ta", "gav" to "ge", "köpte" to "köpa", "ringde" to "ringa",
        "träffade" to "träffa", "började" to "börja", "väntade" to "vänta"
    )

    private val pastToSupine = mapOf(
        "åt" to "ätit", "drack" to "druckit", "sov" to "sovit", "jobbade" to "jobbat", "arbetade" to "arbetat",
        "studerade" to "studerat", "bodde" to "bott", "var" to "varit", "gick" to "gått", "kom" to "kommit",
        "reste" to "rest", "körde" to "kört", "läste" to "läst", "skrev" to "skrivit", "gjorde" to "gjort",
        "såg" to "sett", "sa" to "sagt", "tog" to "tagit", "gav" to "gett", "köpte" to "köpt",
        "ringde" to "ringt", "träffade" to "träffat", "började" to "börjat", "väntade" to "väntat"
    )

    private val presentToSupine = mapOf(
        "bor" to "bott", "jobbar" to "jobbat", "arbetar" to "arbetat", "studerar" to "studerat", "lever" to "levt",
        "väntar" to "väntat", "känner" to "känt", "är" to "varit", "går" to "gått", "kör" to "kört",
        "använder" to "använt", "spelar" to "spelat", "tränar" to "tränat", "reser" to "rest", "läser" to "läst",
        "skriver" to "skrivit", "gör" to "gjort", "ser" to "sett", "säger" to "sagt", "äter" to "ätit",
        "dricker" to "druckit", "sover" to "sovit", "kommer" to "kommit", "tar" to "tagit", "ger" to "gett",
        "förstår" to "förstått", "ringer" to "ringt", "träffar" to "träffat", "börjar" to "börjat"
    )
    private val supineToInfinitive = (presentToSupine.entries.associate { it.value to presentToInfinitive[it.key].orEmpty() } + mapOf("haft" to "ha")).filterValues { it.isNotBlank() }

    private val adjectiveToNeuter = mapOf(
        "stor" to "stort", "liten" to "litet", "ny" to "nytt", "gammal" to "gammalt", "svensk" to "svenskt",
        "röd" to "rött", "grön" to "grönt", "lång" to "långt", "fin" to "fint", "dyr" to "dyrt",
        "billig" to "billigt", "varm" to "varmt", "kall" to "kallt", "snabb" to "snabbt",
        "långsam" to "långsamt", "viktig" to "viktigt", "svår" to "svårt", "enkel" to "enkelt",
        "god" to "gott", "dålig" to "dåligt", "rolig" to "roligt", "farlig" to "farligt", "tung" to "tungt"
    )
    private val neuterToAdjective = adjectiveToNeuter.entries.associate { (k, v) -> v to k }
    private val adjectiveToDefinite = mapOf(
        "stor" to "stora", "stort" to "stora", "liten" to "lilla", "litet" to "lilla", "ny" to "nya", "nytt" to "nya",
        "gammal" to "gamla", "gammalt" to "gamla", "svensk" to "svenska", "svenskt" to "svenska",
        "röd" to "röda", "rött" to "röda", "grön" to "gröna", "grönt" to "gröna", "lång" to "långa", "långt" to "långa",
        "fin" to "fina", "fint" to "fina", "dyr" to "dyra", "dyrt" to "dyra", "billig" to "billiga", "billigt" to "billiga",
        "viktig" to "viktiga", "viktigt" to "viktiga", "svår" to "svåra", "svårt" to "svåra", "rolig" to "roliga", "roligt" to "roliga"
    )

    private val modalWords = setOf("kan", "kunde", "vill", "ville", "ska", "skulle", "måste", "bör", "borde", "får", "fick")
    private val finiteVerbs: Set<String> = buildSet {
        addAll(presentToInfinitive.keys); addAll(pastToInfinitive.keys); addAll(modalWords); addAll(listOf("är", "har", "hade", "blir", "blev"))
    }
    private val subjunctions = listOf("därför att", "trots att", "även om", "eftersom", "fastän", "medan", "innan", "när", "om", "att", "så att", "tills", "förrän")
    private val qWords = "(?:var|vad|vem|varför|hur|när|vilken|vilket|vilka)"
    private val pronouns = "(?:jag|du|han|hon|vi|ni|de|den|det)"
    private val frontedAdverbs = setOf("idag", "imorgon", "igår", "nu", "ofta", "ibland", "vanligtvis", "först", "därefter", "här", "där", "sedan")

    fun looksSuspicious(text: String): Boolean = errorScore(text) > 0

    fun errorScore(text: String): Int {
        val l = normalize(text)
        var score = 0
        if (articleGenderError(l)) score += 4
        if (numberNounError(l)) score += 4
        if (possessiveAgreementError(l)) score += 3
        if (modalInfinitiveError(l)) score += 4
        if (perfectFormError(l)) score += 4
        if (mainClauseNegationError(l)) score += 3
        if (subordinateNegationError(l)) score += 4
        if (directQuestionOrderError(l)) score += 4
        if (indirectQuestionOrderError(l)) score += 4
        if (v2Error(l)) score += 4
        if (sedanError(l)) score += 4
        if (futureConflictError(l)) score += 5
        return score
    }

    fun highConfidenceRepair(input: String): String {
        var t = tidy(input)
        t = repairNumberNoun(t)
        t = repairArticleAndAdjective(t)
        t = repairPossessiveAgreement(t)
        t = repairPredicativeAdjective(t)
        t = repairModalInfinitive(t)
        t = repairPerfectForms(t)
        t = repairMainClauseNegation(t)
        t = repairSubordinateNegation(t)
        t = repairDirectQuestionOrder(t)
        t = repairIndirectQuestionOrder(t)
        t = repairFutureConflict(t)
        t = repairSedan(t)
        t = repairV2(t)
        return tidy(t)
    }

    fun isUsableCandidate(candidate: String, original: String): Boolean {
        val c = candidate.trim()
        if (c.isBlank() || c.contains('\n')) return false
        val l = c.lowercase()
        val meta = listOf("fix it", "fixa", "rätta", "korrigera", "förklaring", "meningen bör", "meningen ska", "the sentence", "corrected sentence", "grammar", "svaret är", "felet är", "du bör", "jag skulle")
        if (meta.any { l.startsWith(it) }) return false
        if (c.length < original.length * 0.35 || c.length > original.length * 2.3 + 40) return false
        val on = Regex("\\d+").findAll(original).map { it.value }.toList()
        val cn = Regex("\\d+").findAll(candidate).map { it.value }.toList()
        if (on != cn) return false
        return sharedWordRatio(original, candidate) >= 0.45
    }

    fun isConservativeEdit(original: String, candidate: String): Boolean {
        val a = tokens(original); val b = tokens(candidate)
        if (kotlin.math.abs(a.size - b.size) > 3) return false
        return sharedWordRatio(original, candidate) >= 0.55
    }

    private fun articleGenderError(t: String): Boolean = nounGender.any { (noun, g) ->
        val n = Regex.escape(noun)
        if (g == Gender.ETT) Regex("\\ben\\s+(?:\\p{L}+\\s+)?$n\\b").containsMatchIn(t)
        else Regex("\\bett\\s+(?:\\p{L}+\\s+)?$n\\b").containsMatchIn(t)
    }

    private fun numberNounError(t: String): Boolean {
        val number = "(?:\\d+|två|tre|fyra|fem|sex|sju|åtta|nio|tio|elva|tolv|många|flera)"
        for (definite in definitePluralToIndefinite.keys) {
            val m = Regex("\\b$number\\s+${Regex.escape(definite)}\\b").find(t) ?: continue
            val prefix = t.substring(0, m.range.first)
            if (!Regex("\\b(?:de|dessa)\\s+$").containsMatchIn(prefix)) return true
        }
        return singularToPlural.any { (singular, plural) -> singular != plural && Regex("\\b$number\\s+${Regex.escape(singular)}\\b").containsMatchIn(t) }
    }

    private fun possessiveAgreementError(t: String): Boolean = nounGender.any { (noun, g) ->
        val n=Regex.escape(noun)
        if (g==Gender.ETT) Regex("\\b(?:min|din|sin|ingen|någon)\\s+$n\\b").containsMatchIn(t)
        else Regex("\\b(?:mitt|ditt|sitt|inget|något)\\s+$n\\b").containsMatchIn(t)
    }

    private fun modalInfinitiveError(t: String): Boolean {
        val p = presentToInfinitive.keys.joinToString("|") { Regex.escape(it) }
        val m = modalWords.joinToString("|") { Regex.escape(it) }
        return Regex("\\b(?:$m|att)\\s+(?:$p)\\b").containsMatchIn(t)
    }

    private fun perfectFormError(t: String): Boolean {
        val past = pastToSupine.keys.joinToString("|") { Regex.escape(it) }
        val pres = presentToSupine.keys.joinToString("|") { Regex.escape(it) }
        return Regex("\\b(?:har|hade)\\s+(?:$past|$pres)\\b").containsMatchIn(t)
    }

    private fun mainClauseNegationError(t: String): Boolean {
        val finite = finiteVerbs.joinToString("|") { Regex.escape(it) }
        return Regex("^$pronouns\\s+inte\\s+(?:$finite)\\b").containsMatchIn(t)
    }

    private fun subordinateNegationError(t: String): Boolean {
        val finite = finiteVerbs.joinToString("|") { Regex.escape(it) }
        return subjunctions.any { Regex("\\b${Regex.escape(it)}\\s+$pronouns\\s+(?:$finite)\\s+inte\\b").containsMatchIn(t) }
    }

    private fun directQuestionOrderError(t: String): Boolean {
        val finite = finiteVerbs.joinToString("|") { Regex.escape(it) }
        return Regex("^$qWords\\s+$pronouns\\s+(?:$finite)\\b").containsMatchIn(t)
    }

    private fun indirectQuestionOrderError(t: String): Boolean {
        val finite = finiteVerbs.joinToString("|") { Regex.escape(it) }
        val bad = Regex("\\b$qWords\\s+(?:$finite)\\s+$pronouns\\b").find(t) ?: return false
        val prefix = t.substring(0, bad.range.first)
        return Regex("\\b(?:vet|visste|undrar|undrade|frågar|frågade|minns|förstår|säger|berättar)\\b").containsMatchIn(prefix)
    }

    private fun v2Error(t: String): Boolean = frontedAdverbs.any { Regex("^${Regex.escape(it)}\\s+$pronouns\\s+\\p{L}+", RegexOption.IGNORE_CASE).containsMatchIn(t) }
    private fun sedanError(t: String): Boolean = Regex("\\bsedan\\s+(?:19|20)\\d{2}\\b").containsMatchIn(t) && tokens(t).drop(1).any { it in presentToSupine }
    private fun futureConflictError(t: String): Boolean {
        if (!Regex("\\b(?:imorgon|i morgon|i övermorgon|nästa vecka|nästa månad|nästa år)\\b").containsMatchIn(t)) return false
        val sup = supineToInfinitive.keys.joinToString("|") { Regex.escape(it) }
        val past = pastToInfinitive.keys.joinToString("|") { Regex.escape(it) }
        return Regex("\\b(?:har|hade)\\s+(?:$sup)\\b").containsMatchIn(t) || Regex("\\b(?:$past)\\b").containsMatchIn(t)
    }

    private fun repairNumberNoun(input: String): String {
        var t=input
        val number="(?:\\d+|två|tre|fyra|fem|sex|sju|åtta|nio|tio|elva|tolv|många|flera)"
        for ((d, i) in definitePluralToIndefinite) {
            val r = Regex("(?i)\\b($number)\\s+${Regex.escape(d)}\\b")
            var start = 0
            while (true) {
                val m = r.find(t, start) ?: break
                val prefix = t.substring(0, m.range.first)
                if (Regex("(?i)\\b(?:de|dessa)\\s+$").containsMatchIn(prefix)) {
                    start = m.range.last + 1
                    continue
                }
                val rep = "${m.groupValues[1]} $i"
                t = t.replaceRange(m.range, rep)
                start = m.range.first + rep.length
            }
        }
        for ((s,p) in singularToPlural) if (s!=p) t=Regex("(?i)\\b($number)\\s+${Regex.escape(s)}\\b").replace(t,"$1 $p")
        val adj = adjectiveToDefinite.keys.joinToString("|") { Regex.escape(it) }
        for ((s,p) in singularToPlural) if (s!=p) {
            val r=Regex("(?i)\\b($number)\\s+($adj)\\s+${Regex.escape(p)}\\b")
            t=r.replace(t){m->"${m.groupValues[1]} ${adjectiveToDefinite[m.groupValues[2].lowercase()]?:m.groupValues[2]} $p"}
        }
        return t
    }

    private fun repairArticleAndAdjective(input:String):String{
        var t=input
        for((noun,g) in nounGender){
            val n=Regex.escape(noun)
            if(g==Gender.ETT){
                val base=adjectiveToNeuter.keys.joinToString("|"){Regex.escape(it)}
                val neu=neuterToAdjective.keys.joinToString("|"){Regex.escape(it)}
                t=Regex("(?i)\\ben\\s+($base)\\s+($n)\\b").replace(t){m->"ett ${adjectiveToNeuter[m.groupValues[1].lowercase()]?:m.groupValues[1]} ${m.groupValues[2]}"}
                t=Regex("(?i)\\ben\\s+($neu)\\s+($n)\\b").replace(t){m->"ett ${m.groupValues[1]} ${m.groupValues[2]}"}
                t=Regex("(?i)\\ben\\s+($n)\\b").replace(t,"ett $1")
            }else{
                val neu=neuterToAdjective.keys.joinToString("|"){Regex.escape(it)}
                t=Regex("(?i)\\bett\\s+($neu)\\s+($n)\\b").replace(t){m->"en ${neuterToAdjective[m.groupValues[1].lowercase()]?:m.groupValues[1]} ${m.groupValues[2]}"}
                t=Regex("(?i)\\bett\\s+($n)\\b").replace(t,"en $1")
            }
        }
        return t
    }

    private fun repairPossessiveAgreement(input:String):String{
        var t=input
        for((noun,g) in nounGender){ val n=Regex.escape(noun); if(g==Gender.ETT){
            t=Regex("(?i)\\bmin\\s+($n)\\b").replace(t,"mitt $1"); t=Regex("(?i)\\bdin\\s+($n)\\b").replace(t,"ditt $1"); t=Regex("(?i)\\bsin\\s+($n)\\b").replace(t,"sitt $1"); t=Regex("(?i)\\bingen\\s+($n)\\b").replace(t,"inget $1"); t=Regex("(?i)\\bnågon\\s+($n)\\b").replace(t,"något $1")
        } else {
            t=Regex("(?i)\\bmitt\\s+($n)\\b").replace(t,"min $1"); t=Regex("(?i)\\bditt\\s+($n)\\b").replace(t,"din $1"); t=Regex("(?i)\\bsitt\\s+($n)\\b").replace(t,"sin $1"); t=Regex("(?i)\\binget\\s+($n)\\b").replace(t,"ingen $1"); t=Regex("(?i)\\bnågot\\s+($n)\\b").replace(t,"någon $1")
        }}
        for(p in pluralForms){ val e=Regex.escape(p); t=Regex("(?i)\\b(?:min|mitt)\\s+($e)\\b").replace(t,"mina $1"); t=Regex("(?i)\\b(?:din|ditt)\\s+($e)\\b").replace(t,"dina $1"); t=Regex("(?i)\\b(?:sin|sitt)\\s+($e)\\b").replace(t,"sina $1"); t=Regex("(?i)\\b(?:ingen|inget)\\s+($e)\\b").replace(t,"inga $1"); t=Regex("(?i)\\b(?:någon|något)\\s+($e)\\b").replace(t,"några $1") }
        return t
    }

    private fun repairPredicativeAdjective(input:String):String{
        var t=input; val cop="(?:är|var|blir|blev)"
        for((noun,g) in nounGender){ val n=Regex.escape(noun); if(g==Gender.ETT){
            val base=adjectiveToNeuter.keys.joinToString("|"){Regex.escape(it)}
            t=Regex("(?i)\\b(?:ett|mitt|ditt|sitt|detta|något|inget)\\s+($n)\\s+($cop)\\s+($base)\\b").replace(t){m->"${m.value.substringBefore(m.groupValues[1])}${m.groupValues[1]} ${m.groupValues[2]} ${adjectiveToNeuter[m.groupValues[3].lowercase()]?:m.groupValues[3]}"}
        }}
        return t
    }

    private fun repairModalInfinitive(input:String):String{
        val w=input.split(Regex("\\s+")); val out=w.toMutableList(); for(i in 0 until out.lastIndex){ val a=core(out[i]).lowercase(); val b=core(out[i+1]).lowercase(); if(a in modalWords || a=="att"){ val inf=presentToInfinitive[b]?:continue; out[i+1]=punct(out[i+1],inf)}}; return out.joinToString(" ")
    }

    private fun repairPerfectForms(input:String):String{
        var t=input
        for((past,sup) in pastToSupine) t=Regex("(?i)\\b(har|hade)\\s+${Regex.escape(past)}\\b").replace(t,"$1 $sup")
        for((pres,sup) in presentToSupine) t=Regex("(?i)\\b(har|hade)\\s+${Regex.escape(pres)}\\b").replace(t,"$1 $sup")
        return t
    }

    private fun repairMainClauseNegation(input:String):String{
        val finite=finiteVerbs.joinToString("|"){Regex.escape(it)}
        return Regex("(?i)^($pronouns)\\s+inte\\s+($finite)\\b").replace(input){m->"${m.groupValues[1]} ${m.groupValues[2]} inte"}
    }

    private fun repairSubordinateNegation(input:String):String{
        var t=input; val finite=finiteVerbs.joinToString("|"){Regex.escape(it)}
        for(s in subjunctions.sortedByDescending{it.length}) t=Regex("(?i)\\b(${Regex.escape(s)})\\s+($pronouns)\\s+($finite)\\s+inte\\b").replace(t){m->"${m.groupValues[1]} ${m.groupValues[2]} inte ${m.groupValues[3]}"}
        return t
    }

    private fun repairDirectQuestionOrder(input:String):String{
        val finite=finiteVerbs.joinToString("|"){Regex.escape(it)}
        return Regex("(?i)^($qWords)\\s+($pronouns)\\s+($finite)\\b").replace(input){m->"${m.groupValues[1]} ${m.groupValues[3]} ${m.groupValues[2]}"}
    }

    private fun repairIndirectQuestionOrder(input:String):String{
        var t=input; val finite=finiteVerbs.joinToString("|"){Regex.escape(it)}; val r=Regex("(?i)\\b($qWords)\\s+($finite)\\s+($pronouns)\\b")
        var start=0; while(true){ val m=r.find(t,start)?:break; val prefix=t.substring(0,m.range.first); if(!Regex("(?i)\\b(?:vet|visste|undrar|undrade|frågar|frågade|minns|förstår|säger|berättar)\\b").containsMatchIn(prefix)){start=m.range.last+1;continue}; val rep="${m.groupValues[1]} ${m.groupValues[3]} ${m.groupValues[2]}"; t=t.replaceRange(m.range,rep); start=m.range.first+rep.length }
        return t
    }

    private fun repairFutureConflict(input:String):String{
        if(!futureConflictError(normalize(input))) return input
        val m=Regex("(?i)\\b(har|hade)\\s+([A-Za-zÅÄÖåäö]+)\\b").find(input); if(m!=null){ val inf=supineToInfinitive[m.groupValues[2].lowercase()]; if(inf!=null)return input.replaceRange(m.range,"ska $inf") }
        for((past,inf) in pastToInfinitive){ val x=Regex("(?i)\\b${Regex.escape(past)}\\b").find(input)?:continue; return input.replaceRange(x.range,"ska $inf") }; return input
    }

    private fun repairSedan(input:String):String{
        if(!Regex("(?i)\\bsedan\\s+(?:19|20)\\d{2}\\b").containsMatchIn(input)) return input
        val r=Regex("(?i)^(\\s*)($pronouns|[A-ZÅÄÖ][A-Za-zÅÄÖåäö-]+)\\s+([A-Za-zÅÄÖåäö]+)(\\b.*\\bsedan\\b.*)$"); val m=r.find(input)?:return input; val sup=presentToSupine[m.groupValues[3].lowercase()]?:return input; return "${m.groupValues[1]}${m.groupValues[2]} har $sup${m.groupValues[4]}"
    }

    private fun repairV2(input:String):String{
        for(a in frontedAdverbs){ val r=Regex("(?i)^(\\s*)(${Regex.escape(a)})\\s+($pronouns)\\s+([A-Za-zÅÄÖåäö]+)(\\b.*)$"); val m=r.find(input)?:continue; return "${m.groupValues[1]}${m.groupValues[2]} ${m.groupValues[4]} ${m.groupValues[3]}${m.groupValues[5]}"}; return input
    }

    private fun tokens(t:String)=Regex("[A-Za-zÅÄÖåäö]+|\\d+").findAll(normalize(t)).map{it.value}.toList()
    private fun normalize(t:String)=t.lowercase().replace(Regex("[.!?]+$"),"").replace(Regex("\\s+")," ").trim()
    private fun sharedWordRatio(a:String,b:String):Double{ val aa=tokens(a); val bb=tokens(b); if(aa.isEmpty())return 0.0; val used=BooleanArray(bb.size); var common=0; for(x in aa){ val i=bb.indices.firstOrNull{!used[it]&&bb[it]==x}; if(i!=null){used[i]=true;common++}}; return common.toDouble()/maxOf(aa.size,bb.size) }
    private fun core(t:String)=t.trim{!it.isLetter()}
    private fun punct(o:String,r:String)=o.takeWhile{!it.isLetter()}+r+o.takeLastWhile{!it.isLetter()}
    private fun tidy(t:String):String{ val c=t.replace(Regex("\\s+")," ").trim(); if(c.isBlank())return c; val cap=c.replaceFirstChar{if(it.isLowerCase())it.titlecase()else it.toString()}; return if(cap.last() in listOf('.','!','?'))cap else "$cap." }
}
