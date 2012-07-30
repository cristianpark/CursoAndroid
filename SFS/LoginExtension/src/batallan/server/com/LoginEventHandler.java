/**
 * Clase para manejar el evento de Login en la zona donde se use la extensión
 * 
 * @author	Cristian Gómez Alvarez <cristianpark@gmail.com>
 * @fecha	Julio 30 de 2012
 * 
 * @package	batallan.server.com
 * 
 * License:
 * This file is part of LoginExtension.
 *
 *  LoginExtension is free software: you can redistribute it and/or modify
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

package batallan.server.com;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import batallan.server.com.Conexion;

public class LoginEventHandler extends BaseServerEventHandler
{
   @Override
   public void handleServerEvent(ISFSEvent event) throws SFSException
   {
	   String login=(String) event.getParameter(SFSEventParam.LOGIN_NAME);
	   String passwordLG=(String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
	   String passwordBD="";	//Password del usuario obtenido desde la BD
	   
	   //Se obtiene el usuario del usuario en la BD
	   String consulta="SELECT btnusuario_password FROM btnusuario WHERE LOWER(btnusuario_login)='"+login.toLowerCase()+"'";
  
	   Conexion conexion = new Conexion(); //Inicializa la conexion con la BD
	   
	   trace("Conexión. Resultado: "+conexion.getResultado());
	   
	   if (conexion.getEstado()) 
	   {
		   ResultSet res = conexion.consulta(consulta);
		   try {
			   if(res!=null){
				   while(res.next()){                             
					   passwordBD=res.getString("btnusuario_password");
				   }
			   }
			   else{
				   trace("Ejecución consulta. Resultado: "+conexion.getResultado());
			   }
		   } catch (SQLException e) {
			   trace("Error SQL: " + e.getMessage());
		   }
	   }
	   
	   //Liberar la conexión
	   conexion.desconectar();
	   
	   //Obtener sesión para verificar login
	   ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);
	   
	   //Se verifica que los passwords sean correctos
	   if(!getApi().checkSecurePassword(session, passwordBD.trim(), passwordLG)){
		   //Crear el error a enviar
		   SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
	       errData.addParameter(login);
	         
	       //Enviar la excepción al cliente
	       throw new SFSLoginException("El usuario/password no son válidos. "+login+", pass login: "+passwordLG+" resultado: "+passwordBD); 
	   }
   }
   //-------------------------------------------------------------------------------
}