/**
 * Aplicación para conectar a un servidor SFS
 * Provee un chat multiusuario
 * 
 * @author	Cristian Gómez Alvarez <cristianpark@gmail.com>
 * @fecha	Julio 30 de 2012
 * 
 * @package	prueba.smartfox.com
 * 
 * License:
 * This file is part of SFS_chat.
 *
 *  SFS_chat is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Foobar is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package prueba.smartfox.com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
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
import sfs2x.client.entities.variables.SFSUserVariable;
import sfs2x.client.entities.variables.UserVariable;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.PublicMessageRequest;
import sfs2x.client.requests.SetUserVariablesRequest;

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

public class ClienteSFS extends Activity implements IEventListener
{
	/*************** VARIABLES *******************/
	private SmartFox sfsClient;	//Cliente SmartFox
	private IEventListener evtListener;	//Listener
	public static String ESTADO="prueba.smartfox.com.ESTADO";
	
	private String ip;
	private String login;
	private String pass;
	
	private List<String> colores=Arrays.asList("#FF0000", "#0000FF", "#00FF00", "#000000", "#8B4513", "#008080", "#800000");
	
	private TextView TVchat;
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
        servidores[3]="190.251.90.206";
        
        //Obtener el TV del chat
        TVchat=(TextView) findViewById(R.id.TVchat);
        
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
				/******************** CONEXION ************************/
				
				//--- Se establece conexión
				if(event.getType().equalsIgnoreCase(SFSEvent.CONNECTION))
				{
					status = "Conectado";
			    	handler.sendEmptyMessage(0);
			    	
			    	if(event.getArguments().get("success").equals(true))
					{
			    		Log.d("CR-Conexión", "Se conectó, se hace login con usuario "+login+" pass: "+pass);
			    		
			    		// Logearse en una zona específica
				    	sfsClient.send(new LoginRequest(login, pass, "BatallaNaval"));
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
			    	
			    	Log.d("CR-Conexión", "Se perdió la conexión...destruyendo"+event.getArguments().toString());
				}
				
				//--- ATTEMPT
				else if(event.getType().equalsIgnoreCase(SFSEvent.CONNECTION_ATTEMPT_HTTP))
				{
					status = "Conexión intento";			    	
					Log.d("CR-Conexión", "Intento de conexión");
					
					sfsClient.disconnect();
				}
				
				//--- RETRY
				else if(event.getType().equalsIgnoreCase(SFSEvent.CONNECTION_RETRY))
				{
					status = "Conexión reiniciada";
			    	Log.d("CR-Conexión", "Se reinició la conexión");
			    	
			    	Toast.makeText(getApplicationContext(), "Reinicio", Toast.LENGTH_LONG).show();
				}
				
				
				/******************* LOGIN **********************/
				
				//--- Ingresa con credenciales inválidas
				if(event.getType().equalsIgnoreCase(SFSEvent.LOGIN_ERROR))
				{
					vibrar("Usuario/password no válidos", true);
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
				
				//--- Se logea en una zona
				else if(event.getType().equalsIgnoreCase(SFSEvent.LOGOUT))
				{
					status = "Salió de en la zona: "+sfsClient.getCurrentZone();
					handler.sendEmptyMessage(0);
					
			    	//Se devuelve a pantalla de login
			    	mostrarPantalla("login");
				}
				
				/******************** USUARIO *****************/
				
				//--- Se une a una sala
				else if(event.getType().equalsIgnoreCase(SFSEvent.ROOM_JOIN))
				{
					status = "Se unió a la sala: " + sfsClient.getLastJoinedRoom().getName() + "'.\n";
					handler.sendEmptyMessage(0);
					Log.d("CR-Room", "Se unió a la sala: " + sfsClient.getLastJoinedRoom().getName() + "'.\n");
					
					mostrarPantalla("chat");
					
					//Determinar el color del nick del usuario
					int random=(int) Math.round(Math.random()*(colores.size()-1));
					
					
					//Mostrar resultado
					TVchat.append(Html.fromHtml("<b><font color=\""+colores.get(random)+"\">["+	login+"]</font></b>"));
					TVchat.append(" se unió a la sala "+sfsClient.getLastJoinedRoom().getName());
					
					//Actualizar lista de variables (seleccionar color nick)
					// Create some User Variables 
					List<UserVariable> userVars = new ArrayList(); 
					userVars.add(new SFSUserVariable("colorNick", colores.get(random)));
					
					//Enviar variables del usuario
					sfsClient.send(new SetUserVariablesRequest(userVars));
				}
				
				//--- Un usuario entra a la sala
		        else if(event.getType().equals(SFSEvent.USER_ENTER_ROOM))
		        {
		        	User user=(User)event.getArguments().get("user");
		        	
		        	//Obtener el color del nick del usuario
		        	UserVariable VBcolor=user.getVariable("colorNick");
		        	String color=VBcolor!=null?VBcolor.getStringValue():"#000000";
		        	
		        	Log.d("CR-Chat", "Usuario ["+user.getName()+" entró a la sala");
		        	
		        	TVchat.append(Html.fromHtml("<br /><b><font color=\""+color+"\">["+user.getName()+"]</font></b>"));
		        	TVchat.append(" se unió a la sala");
		        }
				
				//--- Un usuario sale de la sala
		        else if(event.getType().equals(SFSEvent.USER_EXIT_ROOM))
		        {
		        	User user=(User)event.getArguments().get("user");
		        	
		        	//Obtener el color del nick del usuario
		        	UserVariable VBcolor=user.getVariable("colorNick");
		        	String color=VBcolor!=null?VBcolor.getStringValue():"#000000";
		        	
		        	Log.d("CR-Chat", "Usuario ["+user.getName()+" abandonó la sala");
		        	
		        	TVchat.append(Html.fromHtml("<br /><b><font color=\""+color+"\">["+user.getName()+"]</font></b>"));
		        	TVchat.append(" abandonó la sala");
		        }
				
				//--- Se recibe un mensaje público
	            else if(event.getType().equals(SFSEvent.PUBLIC_MESSAGE))
	            {
	                User usuarioEnvio=(User)event.getArguments().get("sender");
	                String mensaje=event.getArguments().get("message").toString();
	                
	                //Vibra cuando el mensaje lo envía otro usuario
	                if(usuarioEnvio.getName().compareTo(login)!=0){
	                	vibrar();
	                }
	                
	                //Obtener el color del nick del usuario
		        	UserVariable VBcolor=usuarioEnvio.getVariable("colorNick");
		        	String color=VBcolor!=null?VBcolor.getStringValue():"#000000";
		        	
		        	TVchat.append(Html.fromHtml("<br /><b><font color=\""+color+"\">["+usuarioEnvio.getName()+"]</font></b>"));
	                TVchat.append(": "+mensaje);
	                
	                //Bajar el scroll a la última posición
	                ScrollView SVscroll=(ScrollView)findViewById(R.id.scroll_chat);
	        		SVscroll.fullScroll(View.FOCUS_DOWN);
	        		
	        		Log.d("CR-Chat", "MENSAJE ["+usuarioEnvio.getName()+"]: "+mensaje);
	            }
				
				//--- Un usuario actualiza su lista de variables
	            else if(event.getType().equals(SFSEvent.USER_VARIABLES_UPDATE)){
	            	/*
	            	List changedVars = (List)event.getArguments().get("changedVars"); 
	            	User user = (User)event.getArguments().get("user");
	            	
	            	Check if the user changed his x and y user variables 
	            	if (changedVars.indexOf("x") != -1 || changedVars.indexOf("y") != -1) { 
	            		// Move the user avatar to a new position ... 
	            	};
	            	*/
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
		sfsClient.addEventListener(SFSEvent.CONNECTION_ATTEMPT_HTTP, this);
		sfsClient.addEventListener(SFSEvent.CONNECTION_RETRY, this);
		sfsClient.addEventListener(SFSEvent.LOGIN, this);
		sfsClient.addEventListener(SFSEvent.LOGIN_ERROR, this);
		sfsClient.addEventListener(SFSEvent.ROOM_JOIN, this);
		sfsClient.addEventListener(SFSEvent.HANDSHAKE, this);
		sfsClient.addEventListener(SFSEvent.USER_ENTER_ROOM, this);
		sfsClient.addEventListener(SFSEvent.USER_EXIT_ROOM, this);
		sfsClient.addEventListener(SFSEvent.PUBLIC_MESSAGE, this);
		sfsClient.addEventListener(SFSEvent.USER_VARIABLES_UPDATE, this);
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
			sfsClient.removeEventListener(SFSEvent.CONNECTION_ATTEMPT_HTTP, evtListener);
			sfsClient.removeEventListener(SFSEvent.CONNECTION_RETRY, evtListener);
			sfsClient.removeEventListener(SFSEvent.LOGIN, evtListener);
			sfsClient.removeEventListener(SFSEvent.LOGIN_ERROR, evtListener);
			sfsClient.removeEventListener(SFSEvent.ROOM_JOIN, evtListener);
	    	sfsClient.removeEventListener(SFSEvent.HANDSHAKE, evtListener);
	    	sfsClient.removeEventListener(SFSEvent.USER_ENTER_ROOM, evtListener);
			sfsClient.removeEventListener(SFSEvent.USER_EXIT_ROOM, evtListener);
			sfsClient.removeEventListener(SFSEvent.PUBLIC_MESSAGE, evtListener);
			sfsClient.removeEventListener(SFSEvent.USER_VARIABLES_UPDATE, evtListener);
	    	
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
	 * Funcion para validar que introduzcan usuario/password
	 */
	public Boolean validarDatos(){
		Boolean validos=true;
		
		String loginV=((EditText) findViewById(R.id.ETlogin)).getText().toString();
		String passV=((EditText) findViewById(R.id.ETpass)).getText().toString();
		
		//Se verifica si no ingresaron información
		if(loginV.length()==0 || passV.length()==0){
			validos=false;
		}
		
		return validos;
	}
	//--------------------------------------------
	
	/**
	 * Funcion para establecer conexión
	 */
	public void establecerConexion(View view){
		if(validarDatos()){
			Spinner SPserver=(Spinner) findViewById(R.id.SPserver);
			ip=SPserver.getSelectedItem().toString();
			login = ((EditText) findViewById(R.id.ETlogin)).getText().toString();
			pass = ((EditText) findViewById(R.id.ETpass)).getText().toString();
			pass=EncriptarMD5.md5(pass);
			
			//Se determina si ya se había establecido conexión con el servidor o no
			if(sfsClient.isConnected()){
				// Logearse en una zona específica
		    	sfsClient.send(new LoginRequest(login, pass, "BatallaNaval"));
			}
			else{
				//Conectar al servidor
				connectToServer(ip);
			}
		}
		else{
			vibrar("Debe ingresar usuario/password");
		}
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
	
	/**
	 * Función para mostrar la pantalla adecuada
	 */
	public void mostrarPantalla(String pantalla){
		//Se oculta layout de login y se muestra el de conexión
		LinearLayout LLlogin = (LinearLayout) findViewById(R.id.LLlogin);
		LinearLayout LLresultado = (LinearLayout) findViewById(R.id.LLchat);
		
		//Mostrar ocultar los layouts según corresponda
		LLlogin.setVisibility(pantalla.equals("login")?View.VISIBLE:View.GONE);
		LLresultado.setVisibility(pantalla.equals("chat")?View.VISIBLE:View.GONE);
	}
	//------------------------------------------
	
	/**
	 * Función para hacer vibrar el celular
	 */
	public void vibrar(){
		Vibrator vibrar=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	vibrar.vibrate(50);
	}
	//--------------------------------------
	
	/**
	 * Función para hacer vibrar el celular mostrando un mensaje antes
	 */
	public void vibrar(String mensaje){
		Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
		
		Vibrator vibrar=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	vibrar.vibrate(50);
	}
	//--------------------------------------
	
	/**
	 * Función para hacer vibrar el celular mostrando un mensaje antes y borrando password
	 */
	public void vibrar(String mensaje, Boolean borrarPass){
		Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
		
		if(borrarPass){
			((EditText) findViewById(R.id.ETpass)).setText(null);
		}
		
		Vibrator vibrar=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	vibrar.vibrate(50);
	}
	//--------------------------------------
}