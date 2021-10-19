package felipedalperio.olx.com.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import felipedalperio.olx.com.R;
import felipedalperio.olx.com.helper.ConfiguracaoFirebase;

public class CadastroActivity extends AppCompatActivity {
    private Button botaoAcessar;
    private EditText campoEmail, campoSenha,campoNome;
    private Switch tipoAcesso;
    //FIREBASE:
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        campoNome.setVisibility(View.GONE);

        //FAZENDO COM QUE O CAMPO NOME APAREÇA , SWITCH ON:
        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(tipoAcesso.isChecked()) {//CADASTRO
                    campoNome.setVisibility(View.VISIBLE);
                }
                else{
                    campoNome.setVisibility(View.GONE);
                }
            }
        });


        //EVENTO DE CLICK NO BOTAO:
        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //RECUPERANDO OS CAMPOS:
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();
                String nome = campoNome.getText().toString();
                //VALIDANDO OS CAMPOS:
                if(!email.isEmpty()){
                    if(!senha.isEmpty()){
                        //VERIFICA SE O SWITCH ESTA MARCADO:
                        if(tipoAcesso.isChecked()){//CADASTRO
                            if(!nome.isEmpty()){
                                autenticacao.createUserWithEmailAndPassword(
                                        email,senha
                                ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(CadastroActivity.this, "Cadastro Realizado com Sucesso :D", Toast.LENGTH_SHORT).show();
                                        }else{
                                            String erroExecao = "";
                                            try {
                                                throw task.getException();
                                            }catch (FirebaseAuthWeakPasswordException e){
                                                erroExecao = "Digite uma senha mais Forte!";
                                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                                erroExecao = "PorfavorDigite um Email valido";
                                            }
                                            catch (FirebaseAuthUserCollisionException e) {
                                                erroExecao = "Está conta já foi Cadastrada!";
                                            }
                                            catch (Exception e) {
                                                erroExecao = "Ao cadastrar Usuario: " + e.getMessage();
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(CadastroActivity.this, "Erro: " + erroExecao, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else {
                                Toast.makeText(CadastroActivity.this, "Preencha o campo Nome", Toast.LENGTH_SHORT).show();
                            }
                        }else{//LOGIN
                            autenticacao.signInWithEmailAndPassword(
                                    email,senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(CadastroActivity.this,
                                                "Logado com Sucesso :D",
                                                Toast.LENGTH_SHORT).show();
                                        //REDIRENCIOANDO PARA A TELA PRINCIPAL>
                                        startActivity(new Intent(getApplicationContext(),AnunciosActivity.class));

                                    }else{
                                        Toast.makeText(CadastroActivity.this, "Erro ao fazer Login!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }


                    }else{
                        Toast.makeText(CadastroActivity.this, "Preencha o campo Senha", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CadastroActivity.this, "Preencha o campo E-mail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void inicializarComponentes(){
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        campoNome  = findViewById(R.id.editCadastroNome);
        botaoAcessar = findViewById(R.id.buttonAcesso);
        tipoAcesso = findViewById(R.id.switchAcesso);
    }
}
