package felipedalperio.olx.com.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import felipedalperio.olx.com.R;
import felipedalperio.olx.com.model.Anuncio;

public class DetalhesProdutoActivity extends AppCompatActivity {
    private CarouselView carouselView;
    private TextView titulo;
    private TextView descricao;
    private TextView estado;
    private TextView preco;
    private Anuncio anuncioSelecionado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_produto);
        //TOOLBAR:
        getSupportActionBar().setTitle("Detalhe Produto");
        inicializarComponentes();
        //recuperando a nuncio:
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");

        if(anuncioSelecionado != null){
            titulo.setText(anuncioSelecionado.getTitulo());
            descricao.setText(anuncioSelecionado.getDescricao());
            estado.setText(anuncioSelecionado.getEstado());
            preco.setText(anuncioSelecionado.getValor());

            //RECUPERANDO A IMAGEM:
            ImageListener imageListener = new ImageListener() {
                @Override
                public void setImageForPosition(int position, ImageView imageView) {
                    String urlString = anuncioSelecionado.getFotos().get(position);
                    Picasso.get().load(urlString).into(imageView);
                }
            };
            //quantos itens ele vai exebir:
            carouselView.setPageCount(anuncioSelecionado.getFotos().size());
            //O que ele vai exibir:
            carouselView.setImageListener(imageListener);
        }

    }

    public void visualizarTelefone(View view){
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", anuncioSelecionado.getTelefone(), null));
        startActivity(i);
    }

    private void inicializarComponentes(){
        carouselView    = findViewById(R.id.carouselView);
        titulo          = findViewById(R.id.textTituloDetalhe);
        descricao       = findViewById(R.id.textDescricaoDetalhe);
        estado          = findViewById(R.id.textEstadoDetalhe);
        preco          = findViewById(R.id.textPrecoDetalhe);
    }
}
