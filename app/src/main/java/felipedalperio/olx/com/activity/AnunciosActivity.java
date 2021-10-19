package felipedalperio.olx.com.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;
import felipedalperio.olx.com.R;
import felipedalperio.olx.com.adapter.AdapterAnuncios;
import felipedalperio.olx.com.helper.ConfiguracaoFirebase;
import felipedalperio.olx.com.helper.RecyclerItemClickListener;
import felipedalperio.olx.com.model.Anuncio;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnunciosPublicos;
    private Button buttonRegiao, buttonCategoria;
    private TextView textViewNaoEncontrado;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);
        //INICIALIZAR COMPONENTES:
        inicializarComponentes();
        textViewNaoEncontrado.setVisibility(View.GONE);
        //Configurações iniciais:
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase().child("anuncios");

        //Configurar RecyclerView:
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios,this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);

        recuperarAnunciosPublicos();

        //Aplicar Evento de click
        recyclerAnunciosPublicos.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                recyclerAnunciosPublicos,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Anuncio anuncioSelecionado = listaAnuncios.get(position);
                        Intent i = new Intent(AnunciosActivity.this, DetalhesProdutoActivity.class);
                        i.putExtra("anuncioSelecionado",anuncioSelecionado);
                        startActivity(i);

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

    }

    public void filtrarPorEstado(View view){
            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione o estado desejado");

            //CONFIGURANDO O SPINNER:
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner,null);
            //DADOS DO SPINNER ESTADO:
            final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] estados = getResources().getStringArray(R.array.estados);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,estados);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEstado.setAdapter(adapter);

            dialogEstado.setView(viewSpinner);

            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filtroEstado = spinnerEstado.getSelectedItem().toString();
                    recuperarAnunciosPorEstado();
                    filtrandoPorEstado = true;
                }
            });

            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();
    }
    public void filtrarPorCategoria(View view){
        if(filtrandoPorEstado == true){
            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a categoria desejado");

            //CONFIGURANDO O SPINNER:
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner,null);
            //DADOS DO SPINNER Categorias:
            final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
            String[] categorias = getResources().getStringArray(R.array.categoria);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,android.R.layout.simple_spinner_item,categorias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);

            dialogEstado.setView(viewSpinner);

            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                    recuperarAnunciosPorCategoria();
                }
            });

            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();
        }else{
            Toast.makeText(this, "Escolha antes uma Região", Toast.LENGTH_SHORT).show();
        }

    }

    public void recuperarAnunciosPorCategoria(){
        //Dialog:
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Aguarde!")
                .setCancelable(false)
                .build();
        dialog.show();
        //Configuracao nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaAnuncios.clear();
                for (DataSnapshot anuncios : dataSnapshot.getChildren()){
                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    listaAnuncios.add(anuncio);
                }
                if(listaAnuncios.isEmpty()){
                    textViewNaoEncontrado.setVisibility(View.VISIBLE);
                    textViewNaoEncontrado.setText("Nenhum produto encontrado na Região: "+filtroEstado + "\n na Categoria: " + filtroCategoria);
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void recuperarAnunciosPorEstado(){
        //Dialog:
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Aguarde!")
                .setCancelable(false)
                .build();
        dialog.show();
        //Configuracao nó por estado
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaAnuncios.clear();
                for (DataSnapshot categorias : dataSnapshot.getChildren()){
                    for (DataSnapshot anuncios : categorias.getChildren()){
                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);

                    }
                }
                if(listaAnuncios.isEmpty()){
                    textViewNaoEncontrado.setVisibility(View.VISIBLE);
                    textViewNaoEncontrado.setText("Nenhum produto encontrado na Região: "+filtroEstado);
                }
                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void recuperarAnunciosPublicos(){
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando Aguarde!")
                .setCancelable(false)
                .build();

        listaAnuncios.clear();
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot estados : dataSnapshot.getChildren()){
                    for (DataSnapshot categorias : estados.getChildren()){
                        for (DataSnapshot anuncios : categorias.getChildren()){
                            Anuncio anuncio = anuncios.getValue(Anuncio.class);
                            listaAnuncios.add(anuncio);
                        }
                    }
                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if( autenticacao.getCurrentUser() == null ){//USUARIO DESLOGADO
            menu.setGroupVisible(R.id.group_deslogado, true);
        }else{ // USUARIO LOGADO
            menu.setGroupVisible(R.id.group_logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(),CadastroActivity.class));
                break;
            case R.id.menu_sair:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
            case R.id.menu_anuncios:
                startActivity(new Intent(getApplicationContext(),MeusAnunciosActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){
        recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
        buttonCategoria = findViewById(R.id.buttonCategoria);
        buttonRegiao    = findViewById(R.id.buttonRegiao);
        textViewNaoEncontrado = findViewById(R.id.textViewNaoEncontrado);
    }
}
