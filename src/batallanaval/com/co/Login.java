package batallanaval.com.co;

import batallanaval.com.co.clases.tablero;
import batallanaval.com.co.clases.tablero.Posicion;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Login extends Activity {
	tablero tab;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tab=new tablero();
        Posicion pos = tab.movimiento(1, 2);
        System.out.print(pos);    
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }
    
    
}
