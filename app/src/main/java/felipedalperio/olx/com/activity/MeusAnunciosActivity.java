package felipedalperio.olx.com.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import felipedalperio.olx.com.R;
import felipedalperio.olx.com.adapter.AdapterAnuncios;
import felipedalperio.olx.com.helper.ConfiguracaoFirebase;
import felipedalperio.olx.com.helper.RecyclerItemClickListener;
import felipedalperio.olx.com.model.Anuncio;

public class MeusAnunciosActivity extends AppCompatActivity {
    private RecyclerView recyclerAnuncios;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private ProgressBar progressMeusAnuncios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);
        anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child(ConfiguracaoFirebase.getIdUsuario());
        inicializarComponentes();
        progressMeusAnuncios.setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(new Intent(getApplicationContext(),CadastrarAnuncioActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurar RecyclerAnuncios:
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        adapterAnuncios =new AdapterAnuncios(anuncios,this);
        recyclerAnuncios.setAdapter(adapterAnuncios);

        //Recuperar anúncios para o usuário:
        recuperarAnuncios();
        //ADICIONANDO EVENTO DE CLICK:
        recyclerAnuncios.addOnItemTouchListener(
               new RecyclerItemClickListener(
                       this,
                       recyclerAnuncios,
                       new RecyclerItemClickListener.OnItemClickListener() {
                           @Override
                           public void onItemClick(View view, int position) {

                           }

                           @Override
                           public void onLongItemClick(View view, int position) {
                                Anuncio anuncioSelecionado = anuncios.get(position);
                                anuncioSelecionado.remover();

                                adapterAnuncios.notifyDataSetChanged();
                           }

                           @Override
                           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                           }
                       }
               )
        );
    }

    private void recuperarAnuncios(){
        progressMeusAnuncios.setVisibility(View.VISIBLE);
        //FAZ UM LISTENER PARA ESCUTAR AS MUDANÇAS:
        anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //LIMPA A LISTA:
                anuncios.clear();
                //PERCORRE OS FILHOS E ADICIONA O VALOR DENTRO DA LISTA
                for( DataSnapshot ds : dataSnapshot.getChildren()){
                    anuncios.add(ds.getValue(Anuncio.class));
                }
                progressMeusAnuncios.setVisibility(View.GONE);
                Collections.reverse(anuncios); //PARA FAZER UMA EXIBIÇÃO REVERSA
                //NOTIFICA O ADAPTER:
                adapterAnuncios.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inicializarComponentes(){
        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);
        progressMeusAnuncios = findViewById(R.id.progressMeusAnuncios);
    }

}
