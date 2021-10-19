package felipedalperio.olx.com.activity;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;
import com.theartofdev.edmodo.cropper.CropImage;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import felipedalperio.olx.com.R;
import felipedalperio.olx.com.helper.ConfiguracaoFirebase;
import felipedalperio.olx.com.helper.Permissoes;
import felipedalperio.olx.com.model.Anuncio;

public class CadastrarAnuncioActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private ImageView imagem1,imagem2,imagem3;
    private Spinner campoEstado, campoCategoria;
    private Anuncio anuncio;
    private StorageReference storage;
    private int valor;
    private byte[] dadosImagem;
    private AlertDialog dialog;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    //LISTA DE FOTOS:
    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaUrlFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);
        //CONFIGURAÇÕES INICIAIS:
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        //VALIDAR PERMISSÕES:
        Permissoes.validarPermissoes(permissoes,this,1);
        //INICIALIZAR COMPONENTES:
        inicializarCamponentes();
        carregarDadosSpinner();
    }
    public void salvarAnuncio(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Salvando Anúncio")
                .setCancelable(false)
                .build();
        dialog.show();

        //SALVAR AS IMAGENS NO STORAGE:
        for(int i=0; i< listaFotosRecuperadas.size(); i++){
            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista = listaFotosRecuperadas.size();
            salvarFotoStorage(urlImagem, tamanhoLista, i);
        }
    }
    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){
        //CRIAR NÓ DO STORAGE
           final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador); //imagem0, imagem1, imagem2
        //Fazer um Upload de arquivo :
        UploadTask uploadTask = imagemAnuncio.putBytes( dadosImagem );
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String urlConvertida = uri.toString();      //Esta url funciona!!!
                        listaUrlFotos.add( urlConvertida );

                        if(totalFotos == listaUrlFotos.size()){
                            Toast.makeText(CadastrarAnuncioActivity.this, "Sucesso ao fazer Upload", Toast.LENGTH_SHORT).show();
                            anuncio.setFotos(listaUrlFotos);
                            anuncio.salvar();

                            dialog.dismiss();
                            finish();
                        }

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mensagemDeErro("Falha ao fazer Upload");
                Log.i("INFO","Falha ao fazer upload" + e.getMessage());
            }
        });
    }
    private Anuncio configurarAnuncio(){
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String fone = campoTelefone.getRawText().toString();
        String descricao = campoDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);
        return anuncio;

    }
    public void validarDadosAnuncio(View view){
        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());

        if(listaFotosRecuperadas.size() != 0){
            if(!anuncio.getEstado().isEmpty()){
                if(!anuncio.getCategoria().isEmpty()){
                    if(!anuncio.getTitulo().isEmpty()){
                        if(!valor.isEmpty() && !valor.equals("0")){
                            if(!anuncio.getTelefone().isEmpty() && anuncio.getTelefone().length() >= 11){
                                if(!anuncio.getDescricao().isEmpty()){
                                        salvarAnuncio();
                                }else{
                                    mensagemDeErro("Porfavor, Preencha o campo Descrição!");
                                }
                            }else{
                                mensagemDeErro("Porfavor, Preencha o campo Telefone!");
                            }
                        }else{
                            mensagemDeErro("Porfavor, Preencha o campo Valor!");
                        }
                    }else{
                        mensagemDeErro("Porfavor, Preencha o campo Título!");
                    }
                }else{
                    mensagemDeErro("Porfavor, Preencha o campo categoria!");
                }
            }else{
                mensagemDeErro("Porfavor, Preencha o campo estado!");
            }

        }else{
            mensagemDeErro("Porfavor, escolha pelo menos uma foto!");
        }
    }
    private void mensagemDeErro(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;
            case R.id.imageCadastro2:
                escolherImagem(2);
                break;
            case R.id.imageCadastro3:
                escolherImagem(3);
                break;
        }
    }

    public void escolherImagem(int requestCode){

        CropImage.activity()
                .start(CadastrarAnuncioActivity.this);
         valor = requestCode;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bitmap imagem = null;
            if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                try {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if( resultCode == RESULT_OK) {
                        Uri resultUri = result.getUri();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                        String caminhoFoto = imagem.toString();

                        if(imagem != null) {
                            if (valor == 1) {
                                imagem1.setImageBitmap(imagem);
                            } else if (valor == 2) {
                                imagem2.setImageBitmap(imagem);
                            } else if (valor == 3) {
                                imagem3.setImageBitmap(imagem);
                            }
                            listaFotosRecuperadas.add(caminhoFoto);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                            dadosImagem = baos.toByteArray();
                        }


                    }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                        Exception error = result.getError();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void carregarDadosSpinner(){
        String[] estados = getResources().getStringArray(R.array.estados);
        //CONFIGURANDO O ADAPTER DO ESTADO
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        campoEstado.setAdapter(adapter);

        //CONFIGURANDO O ADAPTER DA CATEGORIA:
        String[] categoria = getResources().getStringArray(R.array.categoria);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,categoria);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        campoCategoria.setAdapter(adapterCategoria);
    }
    private void inicializarCamponentes(){
        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        campoCategoria = findViewById(R.id.spinnerCategorias);
        campoEstado = findViewById(R.id.spinnerEstado);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        //LOCALIDADE:
        Locale locale = new Locale("pt","BR");
        campoValor.setLocale(locale);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertValidarPermissao();
            }
        }
    }

    private void alertValidarPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o App, é necessário aceitar as permissões! ");
        builder.setCancelable(false); //nao vai poder cancelar
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
