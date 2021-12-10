package com.example.fivecontacts.main.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.fivecontacts.R;
import com.example.fivecontacts.main.model.Contato;
import com.example.fivecontacts.main.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

public class AlterarContatos_Activity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    EditText edtNome;
    Boolean firstTimeUser = true;
    BottomNavigationView bnv;
    ListView lv;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_contatos);
        edtNome = findViewById(R.id.edtBusca);
        bnv = findViewById(R.id.bnv);
        bnv.setOnNavigationItemSelectedListener(this);
        bnv.setSelectedItemId(R.id.anvMudar);
        lv = findViewById(R.id.listContatosDoCell);

        //Dados da Intent Anterior
        Intent quemChamou=this.getIntent();
        if (quemChamou!=null) {
            Bundle params = quemChamou.getExtras();
            if (params!=null) {
                //Recuperando o Usuario
                user = (User) params.getSerializable("usuario");
                setTitle("Alterar Contatos de Emergência");
                //TODO ENTENDER PQ NAO TÁ FUNCIONANDO
                if (user.isTema_escuro()){
                    ((ConstraintLayout) (lv.getParent())).setBackgroundColor(Color.BLACK);
                }
            }
        }
    }

    public void salvarContato (Contato w){
        SharedPreferences salvaContatos =
                getSharedPreferences("contatos",Activity.MODE_PRIVATE);

        int num = salvaContatos.getInt("numContatos", 0); //checando quantos contatos já tem
        SharedPreferences.Editor editor = salvaContatos.edit();
        try {
            ByteArrayOutputStream dt = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(dt);
            dt = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(dt);
            oos.writeObject(w);
            String contatoSerializado= dt.toString(StandardCharsets.ISO_8859_1.name());
            editor.putInt("numContatos",num+1);
            editor.putString("contato"+(num+1), contatoSerializado);
        }catch(Exception e){
            e.printStackTrace();
        }
        editor.commit();
        user.getContatos().add(w);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickBuscar(View v){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 3333);
            return;
        }

        ContentResolver cr = getContentResolver();
        String consulta = ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";
        String [] argumentosConsulta= {"%"+edtNome.getText()+"%"};
        //busca nos contatos
        Cursor cursor= cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                consulta,argumentosConsulta, null);

        final String[] nomesContatos = new String[cursor.getCount()];
        final String[] telefonesContatos = new String[cursor.getCount()];


        for (int i=0; cursor.moveToNext(); i++) {
            int indiceNome = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
            String contatoNome = cursor.getString(indiceNome);
            nomesContatos[i]= contatoNome;
            int indiceContatoID = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID);
            String contactID = cursor.getString(indiceContatoID);
            String consultaPhone = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID;
            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, consultaPhone, null, null);

            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                telefonesContatos[i]=number;
            }
        }

        if (nomesContatos !=null) {
            for(int j=0; j<=nomesContatos.length; j++) {
                ArrayAdapter<String> adaptador;
                adaptador = new ArrayAdapter<String>(this, R.layout.list_view_layout, nomesContatos);
                lv.setAdapter(adaptador);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //adiciona contato quando clicar
                        Contato c= new Contato();
                        c.setNome(nomesContatos[i]);
                        //TODO GARANTIR QUE O MAIS ESTÁ CORRETO - FUNÇÃO DE VERIFICAR
                        c.setNumero("tel:+"+telefonesContatos[i]);
                        salvarContato(c);
                        Intent intent = new Intent(getApplicationContext(), ListaDeContatos_Activity.class);
                        intent.putExtra("usuario", user);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.anvLigar) {
            Intent intent = new Intent(this, ListaDeContatos_Activity.class);
            intent.putExtra("usuario", user);
            startActivity(intent);

        }
        if (item.getItemId() == R.id.anvPerfil) {
            Intent intent = new Intent(this, PerfilUsuario_Activity.class);
            intent.putExtra("usuario", user);
            startActivity(intent);

        }
        return true;
    }
}