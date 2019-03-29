package br.edu.ifsp.scl.tradutorsdmkt

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import br.edu.ifsp.scl.tradutorsdmkt.MainActivity.codigosMensagen.RESPOSTA_TRADUCAO
import br.edu.ifsp.scl.tradutorsdmkt.volley.Tradutor
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.design.snackbar

class MainActivity : AppCompatActivity() {
    object codigosMensagen {
        // Constante usada para envio de mensagens ao Handler
        val RESPOSTA_TRADUCAO = 0
    }

    // Idiomas de origem e destino. Dependem da API do Oxford Dict.
    val idiomas = listOf("pt", "en")
    // Handler da thread de UI
    lateinit var tradutoHandler: TradutoHandler

    inner class TradutoHandler : Handler() {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == RESPOSTA_TRADUCAO) {
                // Alterar o conteúdo do TextView
                traduzidoTv.text = msg.obj.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Instancia o handler da thread de UI usado pelo tradutor
        tradutoHandler = TradutoHandler()
        // Cria e seta um Adapter com os idiomas de origem para um Spinner
        idiomaOrigemSp.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            idiomas
        )
        idiomaOrigemSp.setSelection(0) //pt
        // Cria e seta um Adapter com os idiomas de origem para um Spinner
        idiomaDestinoSp.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            idiomas
        )
        idiomaDestinoSp.setSelection(1) //en
        // Seta o Listener para o botão
        traduzirBt.setOnClickListener {
            // Testa se o usuário digitou alguma coisa para traduzir
            if (originalEt.text.isNotEmpty()) {
                // Instancia um tradutor para fazer a chamada ao WS
                val tradutor: Tradutor = Tradutor(this)
                // Solicita a tradução com base nos parâmetros selecionados pelo usuário
                tradutor.traduzir(originalEt.text.toString(),
                    idiomaOrigemSp.selectedItem.toString(),
                    idiomaDestinoSp.selectedItem.toString())
            }
            else {
                // Senão, mostra uma mensagem na parte debaixo do LinearLayout
                mainLl.snackbar("É preciso digitar uma palavra para ser traduzida")
            }
        }
    }





}