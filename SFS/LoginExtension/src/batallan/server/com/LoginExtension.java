/**
 * Extensión para manejar el login de un usuario en la zona
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

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class LoginExtension extends SFSExtension
{
	@Override
    public void init()
    {
        trace("Prueba extensión Login");
        
        //Registrar evento de login
        addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class); 
    }
	
	
}