package se.example.swedishcoach

import android.content.Context
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwedishGrammarLlm(context: Context) : AutoCloseable {
    private val modelManager = BundledModelManager(context)
    private var model: LlamaModel? = null
    val description: String get() = "Grammar: ${BundledModelManager.MODEL_LABEL} • rigorous A1–B2"

    suspend fun prepare(onStatus:(String)->Unit={})=withContext(Dispatchers.IO){
        if(model!=null)return@withContext
        val file=modelManager.ensureModel(onStatus)
        onStatus("Loading ${BundledModelManager.MODEL_LABEL}…")
        model=Llama.loadModel(file.absolutePath, LlamaConfig(contextSize=2048,threads=4,temperature=0.0f,topP=1.0f,topK=1,seed=42))
        onStatus(description)
    }

    suspend fun correct(sentence:String):Result=withContext(Dispatchers.IO){
        val handle=model?:error("Swedish grammar model is not loaded")
        val input=tidy(sentence)
        val deterministic=SwedishValidation.highConfidenceRepair(input)
        val firstRaw=Llama.complete(handle,prompt(input,false),maxTokens=80)
        val first=extract(firstRaw.text,input)
        val secondRaw=Llama.complete(handle,prompt(input,true),maxTokens=80)
        val second=extract(secondRaw.text,input)

        val inputScore=SwedishValidation.errorScore(input)
        val detScore=SwedishValidation.errorScore(deterministic)
        val validModels=listOfNotNull(first,second).filter{SwedishValidation.errorScore(it)<=inputScore}
        val consensus=if(first!=null&&second!=null&&norm(first)==norm(second)) first else null

        val chosen=when{
            consensus!=null && SwedishValidation.isConservativeEdit(input,consensus) -> consensus
            deterministic!=input && detScore<inputScore -> {
                val better=validModels.filter{SwedishValidation.errorScore(it)<detScore}.minByOrNull{distance(input,it)}
                better?:deterministic
            }
            else -> validModels.filter{SwedishValidation.isConservativeEdit(input,it)}.minByOrNull{distance(input,it)}?:input
        }
        val final=SwedishValidation.highConfidenceRepair(chosen)
        Result(final, firstRaw.tokensPerSecond, second!=null, norm(final)!=norm(deterministic))
    }

    data class Result(val text:String,val tokensPerSecond:Float,val secondPass:Boolean,val modelEditUsed:Boolean)

    private fun prompt(sentence:String,targeted:Boolean):String{
        val extra=if(targeted) """
Fel: Jag såg en rött bil.
Rätt: Jag såg en röd bil.
Fel: Jag vill köper en biljett.
Rätt: Jag vill köpa en biljett.
Fel: Jag har gick hem.
Rätt: Jag har gått hem.
Fel: Var du bor?
Rätt: Var bor du?
Fel: Jag vet inte var bor han.
Rätt: Jag vet inte var han bor.
""".trimIndent() else ""
        return """
${RivstartGrammarProfile.compactChecklist}

${RivstartGrammarProfile.examples}
$extra
Fel: $sentence
Rätt:
""".trimIndent()
    }

    private fun extract(raw:String,original:String):String?{
        val lines=raw.replace("<|assistant|>","").replace("<|endoftext|>","").replace("<|im_end|>","").lineSequence().map{it.trim().replace(Regex("(?i)^(?:rätt|korrekt svenska|korrigerad mening|svar)\\s*:\\s*"),"").trim('"','\'',' ')}.filter{it.isNotBlank()}.map(::tidy).filter{SwedishValidation.isUsableCandidate(it,original)}.toList()
        return lines.minWithOrNull(compareBy<String>{SwedishValidation.errorScore(it)}.thenBy{distance(original,it)})
    }

    private fun distance(a:String,b:String):Int{ val aa=norm(a).split(' '); val bb=norm(b).split(' '); val common=aa.zip(bb).count{it.first==it.second}; return maxOf(aa.size,bb.size)-common+kotlin.math.abs(a.length-b.length)/12 }
    private fun norm(t:String)=t.lowercase().replace(Regex("[.!?]+$"),"").replace(Regex("\\s+")," ").trim()
    private fun tidy(t:String):String{ val c=t.replace(Regex("\\s+")," ").trim(); if(c.isBlank())return c; val cap=c.replaceFirstChar{if(it.isLowerCase())it.titlecase()else it.toString()}; return if(cap.last() in listOf('.','!','?'))cap else "$cap." }
    override fun close(){ model?.let{runCatching{Llama.releaseModel(it)}}; model=null }
}
