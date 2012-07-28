package prueba.smartfox.com;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

/*
 * SFS2X Connector example for Android
 * 
 * COPYRIGHT:	2011 gotoAndPlay() - http://www.smartfoxserver.com
 * DEVELOPER:	A51 Integrated - http://a51integrated.com
 */

import com.smartfoxserver.v2.exceptions.SFSException;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.User;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.PublicMessageRequest;

import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Connector example for SmartFoxServer 2X.
 * 
 * The activity tries to connect to "10.0.2.2"
 * (special alias to your host loopback interface
 * - i.e., 127.0.0.1 on your development machine)
 * and displays a message if the connection is successful.  
 */
public class ClienteSFS extends Activity implements IEventListener
{
	/*************** VARIABLES *******************/
	private SmartFox sfsClient;	//Cliente SmartFox
	private IEventListener evtListener;	//Listener
	public static String ESTADO="prueba.smartfox.com.ESTADO";
	
	public String ip;
	public String nick;
	public String pass;
	//---------------------------------------------

	String status = "";
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		System.setProperty("java.net.preferIPv6Addresses", "false");
		setContentView(R.layout.cliente_sfs);
		
		//Opciones del combo
        String[] servidores=new String[4];
        servidores[0]="10.0.0.3";
        servidores[1]="192.168.43.75";
        servidores[2]="10.0.0.110";
        servidores[3]="186.81.208.8";

        //Obtener el Spinner
        Spinner spinner = (Spinner) findViewById(R.id.SPserver);
		//Crear un adapter para mostrar los items
        ArrayAdapter adapter=new ArrayAdapter(this, android.R.layout.simple_spinner_item, servidores);
		//Determinar cómo se mostrará la lista
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		spinner.setAdapter(adapter);	//Asignar el adaptador al spinner
		
		//Inicializar las variables y listeners para SFS
		initSmartFox();
	}
	
	/**
	 * Handle events dispatched from the SFS2X server
	 * @param event - the event that has been dispatched from the server
	 * 
	 * @throws SFSException
	 */
	public void dispatch(final BaseEvent event) throws SFSException
    {
		Log.v("CR-EVENTO", event.getType());
		
		runOnUiThread(new Runnable() {
			//@Override
			public void run() {
				//Se establece conexión
				if(event.getType().equalsIgnoreCase(SFSEvent.CONNECTION))
				{
					status = "Conectado";
			    	handler.sendEmptyMessage(0);
			    	
			    	if(event.getArguments().get("success").equals(true))
					{
			    		Log.d("CR-Conexión", "Se conectó, se hace login");
			    		
			    		// Logearse en una zona específica
				    	sfsClient.send(new LoginRequest(nick, pass, "BatallaNaval"));
					}
		            //otherwise error message is shown
		            else
		            {
		            	//Error de conexión
		            	Toast.makeText(getApplicationContext(), "No se pudo establecer conexión", Toast.LENGTH_LONG).show();
		            }
				}
				
				//--- Se pierde la conexión
				else if(event.getType().equalsIgnoreCase(SFSEvent.CONNECTION_LOST))
				{
					status = "Conexión perdida";
			    	handler.sendEmptyMessage(0);
			    	
			    	// Destroy SFS instance
			    	onDestroy();
			    	
			    	Log.d("CR-Login", "Se perdió la conexión...destruyendo");
				}
				
				//--- Se logea en una zona
				else if(event.getType().equalsIgnoreCase(SFSEvent.LOGIN))
				{
					status = "Se logeo en la zona: "+sfsClient.getCurrentZone();
					handler.sendEmptyMessage(0);
					
					Log.d("CR-Login", "Se logeo en la zona: "+sfsClient.getCurrentZone());
					
			    	//Se envía petición para unirse a la sala
			    	sfsClient.send(new JoinRoomRequest("Juego"));
				}
				
				//--- Se une a una sala
				else if(event.getType().equalsIgnoreCase(SFSEvent.ROOM_JOIN))
				{
					status = "Se unió a la sala: " + sfsClient.getLastJoinedRoom().getName() + "'.\n";
					handler.sendEmptyMessage(0);
					Log.d("CR-Room", "Se unió a la sala: " + sfsClient.getLastJoinedRoom().getName() + "'.\n");
					
					//Se oculta layout de login y se muestra el de conexión
					LinearLayout LLlogin = (LinearLayout) findViewById(R.id.LLlogin);
					LLlogin.setVisibility(View.GONE);
					
					//Se oculta layout de login y se muestra el de conexión
					LinearLayout LLresultado = (LinearLayout) findViewById(R.id.LLchat);
					LLresultado.setVisibility(View.VISIBLE);
					
					//Mostrar resultado
					((TextView) findViewById(R.id.TVchat)).setText("["+	nick+"] se unió a la sala "+sfsClient.getLastJoinedRoom().getName());
				}
				
				//--- Un usuario entra a la sala
		        else if(event.getType().equals(SFSEvent.USER_ENTER_ROOM))
		        {
		        	User user = (User)event.getArguments().get("user");
		        	((TextView) findViewById(R.id.TVchat)).append("["+user.getName()+"] se unió a la sala");
		        }
				
				//--- Se recibe un mensaje público
	            else if(event.getType().equals(SFSEvent.PUBLIC_MESSAGE))
	            {
	                User usuarioEnvio=(User)event.getArguments().get("sender");
	                String mensaje=event.getArguments().get("message").toString();
	                
	                TextView TVchat=(TextView) findViewById(R.id.TVchat);
	                
	                //Vibra cuando el mensaje lo envía otro usuario
	                if(usuarioEnvio.getName().compareTo(nick)!=0){
	                	Vibrator vibrar=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	                	vibrar.vibrate(50);
	                }
	                
	                TVchat.append("\n["+usuarioEnvio.getName()+"]: "+mensaje);
	                //Bajar el scroll a la última posición
	                ScrollView SVscroll=(ScrollView)findViewById(R.id.scroll_chat);
	        		SVscroll.fullScroll(View.FOCUS_DOWN);
	        		
	        		Log.d("CR-Chat", "MENSAJE ["+usuarioEnvio.getName()+"]: "+mensaje);
	            }
			}
		});
    }
	//------------------------------------------------------------------
	
	private Handler handler = new Handler()
	{
         @Override
		public void handleMessage(Message msg)
         {
        	 ESTADO=status;
         }
	};
	
    /** 
     * Multiplayer part 
     */ 
    private void initSmartFox()
    {
        //Instantiate SmartFox client
		sfsClient = new SmartFox(true);
		
		//Agregar listeners según corresponda
		sfsClient.addEventListener(SFSEvent.CONNECTION, this);
		sfsClient.addEventListener(SFSEvent.CONNECTION_LOST, this);
		sfsClient.addEventListener(SFSEvent.LOGIN, this);
		sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
		sfsClient.addEventListener(SFSEvent.HANDSHAKE, this);
		sfsClient.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
		sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
		sfsClient.addEventListener(SFSEvent.PUBLIC_MESSAGE, this);
		//sfsClient.addEventListener(SFSEvent.USER_VARIABLES_UPDATE, this);
    } 
       
	/**
	 * Frees the resources.
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		if(sfsClient != null)
		{
			sfsClient.removeEventListener(SFSEvent.CONNECTION, evtListener);	
			sfsClient.removeEventListener(SFSEvent.CONNECTION_LOST, evtListener);		 
			sfsClient.removeEventListener(SFSEvent.LOGIN, evtListener);
			sfsClient.removeEventListener(SFSEvent.ROOM_JOIN, evtListener);
	    	sfsClient.removeEventListener(SFSEvent.HANDSHAKE, evtListener);
	    	sfsClient.removeEventListener(SFSEvent.USER_ENTER_ROOM, evtListener);
			sfsClient.removeEventListener(SFSEvent.USER_EXIT_ROOM, evtListener);
			sfsClient.removeEventListener(SFSEvent.PUBLIC_MESSAGE, evtListener);
	    	
			sfsClient.disconnect();
			
			status = "Destroyed";
	    	handler.sendEmptyMessage(0);
	    	
	    	Log.d("CR-SFS", "Se termina la ejecución");
		}
		// Exiting with System.exit(0) to avoid problem with persisting MINA threads
		System.exit(0);
	}
	
	
	/**
	 * Connects to SmartFoxServer instance.
	 * 
	 * @param ip the server IP. 
	 */
	private void connectToServer(final String ip)
	{
		//connect() method is called in separate thread
        //so it does not blocks the UI
		final SmartFox sfs = sfsClient;
		
		new Thread() {
			@Override
			public void run() {
				Log.d("CR-Conectar", "Se va a conectar al servidor "+ip);
				sfs.connect(ip, 9933);
			}
		}.start();
	}
	
	/**
	 * Funcion para establecer conexión
	 */
	public void establecerConexion(View view){
		Spinner SPserver=(Spinner) findViewById(R.id.SPserver);
		ip=SPserver.getSelectedItem().toString();
		nick = ((EditText) findViewById(R.id.ETnick)).getText().toString();
		pass = ((EditText) findViewById(R.id.ETpass)).getText().toString();
		
		//Conectar al servidor
		connectToServer(ip);
	}
	//--------------------------------------------
	
	/**
	 * Función para enviar mensaje
	 */
	public void enviarMensaje(View view){
		//Obtener el mensaje a enviar
		EditText ETmensaje=(EditText) findViewById(R.id.ETmensaje);
		
	    String mensaje=ETmensaje.getText().toString();
	    if(mensaje.length()>0) {
	        //Se envía el mensaje público
	        sfsClient.send(new PublicMessageRequest(mensaje));
	        ETmensaje.setText("");
	    }
	}
	//------------------------------------------
}