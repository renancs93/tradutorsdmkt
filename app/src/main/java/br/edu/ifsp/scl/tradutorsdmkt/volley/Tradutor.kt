package br.edu.ifsp.scl.tradutorsdmkt.volley

import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_ID_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_ID_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_KEY_FIELD
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.APP_KEY_VALUE
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.END_POINT
import br.edu.ifsp.scl.tradutorsdmkt.Constantes.URL_BASE
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_TRADUCAO
import br.edu.ifsp.scl.tradutorsdmkt.model.Resposta
import br.edu.ifsp.scl.tradutorsdmkt.model.Translation
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.design.snackbar
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type

class Tradutor(val mainActivity: MainActivity) {

    fun traduzir(palavraOrigem: String, idiomaOrigem: String, idiomaDestino: String) {
        // Monta uma String com uma URL a partir das constantes e parâmetros do usuário
        val urlSb = StringBuilder(URL_BASE)
        with(urlSb) {
            append("${END_POINT}/")
            append("${idiomaOrigem}/")
            append("${palavraOrigem}/")
            append("translations=${idiomaDestino}")
        }
        val url = urlSb.toString()
        // Cria uma fila de requisições Volley para enviar a requisição
        val filaRequisicaoTraducao: RequestQueue = Volley.newRequestQueue(mainActivity)
        // Monta a requisição que será colocada na fila. Esse objeto é uma instância de uma classe anônima
        var traducaoJORequest: JsonObjectRequest =
            object : JsonObjectRequest(
                Request.Method.GET, // Método HTTP de requisição
                url, // URL
                null, // Objeto de requisição - somente em POST
                RespostaListener(), // Listener para tratar resposta
                ErroListener() // Listener para tratar erro
            ) {
                // Corpo do objeto
                // Sobreescrevendo a função para passar cabeçalho na requisição
                override fun getHeaders(): MutableMap<String, String> {
                    // Cabeçalho composto por Map com app_id, app_key e seus valores
                    var parametros: MutableMap<String, String> = mutableMapOf()
                    parametros.put(APP_ID_FIELD, APP_ID_VALUE)
                    parametros.put(APP_KEY_FIELD, APP_KEY_VALUE)
                    return parametros
                }
            }
        // Adiciona a requisição a fila
        filaRequisicaoTraducao.add(traducaoJORequest)
    }

    /* Trata a resposta de uma requisição quando o acesso ao WS foi realizado. Complexidade de O(N^5).
    Pode causar problemas de desempenho com respostas muito grandes */
//    inner class RespostaListener : Response.Listener<JSONObject> {
//        override fun onResponse(response: JSONObject?) {
//            try {
//                // Cria um objeto Gson que consegue fazer reflexão de um Json para Data Class
//                val gson: Gson = Gson()
//                // Reflete a resposta (que é um Json) num objeto da classe Resposta
//                val resposta: Resposta = gson.fromJson(response.toString(), Resposta::class.java)
//                // StringBuffer para armazenar o resultado das traduções
//                var traduzidoSb = StringBuffer()
//                // Parseando o objeto e adicionando as traduções ao StringBuffer, O(N^5)
//                resposta.results?.forEach {
//                    it?.lexicalEntries?.forEach {
//                        it?.entries?.forEach {
//                            it?.senses?.forEach {
//                                it?.translations?.forEach {
//                                    traduzidoSb.append("${it?.text}, ")
//                                }
//                            }
//                        }
//                    }
//                }
//                // Enviando as tradução ao Handler da thread de UI para serem mostrados na tela
//                mainActivity.tradutoHandler.obtainMessage(
//                    RESPOSTA_TRADUCAO,
//                    traduzidoSb.toString().substringBeforeLast(',')
//                ).sendToTarget()
//            } catch (jse: JSONException) {
//                mainActivity.mainLl.snackbar("Erro na conversão JSON")
//            }
//        }
//    }


    // Desserializador personalizado. Complexide O(N^2)
    class TranslationListDeserializer : JsonDeserializer<List<Translation>> {
        // Função que desserializa o Json e retorna uma lista de objetos Translation
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): List<Translation> {
            // Recuperando o JsonArray “results” do Objeto completo Json
            var results: JsonArray? = json?.asJsonObject?.getAsJsonArray("results")
            // Juntando os JsonArrays “lexicalEntries” de todos “results” num só JsonArray, O(N^2)
            val lexicalEntries: JsonArray = JsonArray()
            results?.forEach {
                it?.let {
                    lexicalEntries.addAll(it.asJsonObject?.getAsJsonArray("lexicalEntries"))
                }
            }
            // Juntando os JsonArrays “entries” de todos “lexicalEntries” num só JsonArray, O(N^2)
            val entries: JsonArray = JsonArray()
            lexicalEntries?.forEach {
                it?.let {
                    entries.addAll(it.asJsonObject?.getAsJsonArray("entries"))
                }
            }
            // Juntando todos os JsonArrays “senses” de todos “entries” num só JsonArray, O(N^2)
            val senses: JsonArray = JsonArray()
            entries.forEach {
                it?.let {
                    senses.addAll(it.asJsonObject?.getAsJsonArray("senses"))
                }
            }
            /* Juntando todos os JsonArrays “translations” de todos “senses” num só JsonArray,
            O(N^2) */
            val translations: JsonArray = JsonArray()
            senses.forEach {
                it?.let {
                    translations.addAll(it.asJsonObject?.getAsJsonArray("translations"))
                }
            }
            // Extraindo os campos, criando os objetos Translation e colocando na lista de retorno
            val listaTranslations: MutableList<Translation> = mutableListOf()
            translations.forEach {
                it?.let {
                    val translation: Translation = Translation()
                    translation.language = it.asJsonObject?.get("language").toString()
                    translation.text = it.asJsonObject?.get("text").toString()
                    listaTranslations.add(translation)
                }
            }

            return listaTranslations
        }
    }

    /* Trata a resposta de uma requisição quando o acesso ao WS foi realizado. Usa um Desserializador
    O(N^2) */
    inner class RespostaListener : Response.Listener<JSONObject> {
        override fun onResponse(response: JSONObject?) {
            try {
                // Usa um builder que usa o desserializador personalizado para criar um objeto Gson
                val gsonBuilder: GsonBuilder = GsonBuilder()
                // Usa reflexão para extrair o tipo da classe de um List<Translation>
                val listTranslationType = object : TypeToken<List<Translation>>() {}.type
                // Seta o desserializador personalizado no builder
                gsonBuilder.registerTypeAdapter(listTranslationType, TranslationListDeserializer())
                /* Usa o builder para criar um Gson e usa o Gson para converter o Json de resposta numa lista de
                Translation usando o desserializador personalizado. */
                val listTranslation: List<Translation> =
                    gsonBuilder.create().fromJson(response.toString(), listTranslationType)
                // Extrai somente o texto dos objetos Translation
                val listTranslationString: StringBuffer = StringBuffer()
                listTranslation.forEach { listTranslationString.append("${it.text}, ") }
                mainActivity.tradutoHandler.obtainMessage(RESPOSTA_TRADUCAO,
                    listTranslationString.toString().substringBeforeLast(',')).sendToTarget()
            } catch (je: JSONException) {
                mainActivity.mainLl.snackbar("Erro na conversão JSON")
            }
        }
    }

    // Trata erros na requisição ao WS
    inner class ErroListener : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            mainActivity.mainLl.snackbar("Erro na requisição: ${error.toString()}")
        }
    }


}
