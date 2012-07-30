/**
 * Clase para establecer conexión con un servidor postgres
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

import java.sql.*;

public class Conexion {
    /********** ATRIBUTOS ***********************/
    private final String usuario   = "postgres"; 
    private final String pass   =  "postgres";
    private final String driver     = "org.postgresql.Driver";
    private final String URLdb      = "jdbc:postgresql://localhost:5432/";
    private final String db         = "batallan";
    
    private Connection conn; //Conexión a la base de datos
    private Statement stmt; // Ejecutar consultas
    private boolean estado; // Verifica el estado de la conexión
    private String resultado;	//Resultado de la conexión
    //------------------------------------------------
    
    /************ METODOS ******************************/
    
    /**
    * Función Conexion, Constructor de la clase
    */
    public Conexion() {
        setEstado(false);
        conn = null;
        stmt = null;
        conectar();
    }
    //----------------------------------------------------

    /**
    * Función conectar. Se llama cuando se invoca el constructor de la clase
    *                  Conecta con la BD
    *    
    */
    public void conectar() {        
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(URLdb+db, usuario, pass);
            stmt = conn.createStatement();
            setEstado(true); //pone en true la conexion
            resultado="Conexión exitosa";
        } catch (Exception er) {
            resultado="Conexión fallida: " + er.toString();
        }
    }
    //-------------------------------------
    
    /**
    * Función desconectar. Libera la conexion
    *    
    */
    public void desconectar() {
        setEstado(false);
        if (conn != null) {
            try {
                stmt.close();
                conn.close();
            } catch (SQLException er) { }
        }
    }
    //------------------------------------------

    /**
    * Función consulta. Función que ejecuta una consulta en la BD
    *   
    *  * @param consulta : Sentencia SQL con la consulta
    */
    public ResultSet consulta(String consulta) {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(consulta); //ejecuta la consulta
        } catch (SQLException er) {
            resultado="Consulta: " + consulta+" Error:" + er.toString();
        }
        return rs;
    }
    //--------------------------------------------------------
    
    /**
    * Función insertar. Función que permite insertar en la BD
    *    
    * @param consulta : Sentencia SQL con la insercion
    */   
    public boolean insertar(String consulta) {
        boolean insercion=true;
        try {
            insercion = stmt.execute(consulta); //ejecuta la inserción
            /*Retorna false si es una inserción o actualización
             * y true si es una consulta que devuelva ResultSet */
        } catch (SQLException er) {
        	resultado="Consulta: " + consulta+" Error:" + er.toString();
        }
        System.out.println("inserto:"+insercion);        
        //si insercion es falso, la inserción se hizo con éxito
       return !insercion;
    }
    //--------------------------------------------
    
    /**
    * Función actualizar. Función que permite actualizar datos en la BD
    *    
    * @param consulta : Sentencia SQL con la actualizacion
    */
    public int actualizar(String consulta) {
        int cant = 0;
        try {
            cant = stmt.executeUpdate(consulta);
        } catch (SQLException er) {
        	resultado="No se pudo ejecutar la consulta";
        }
        return cant;
    }
    //------------------------------------------------------

    /**
    * Función getEstado. Retorna el estado de la conexión con la BD
    *    
    */
    public boolean getEstado() {
        return estado;
    }
    //--------------------------------------------------------

    /**
    * Función setEstado. Establece el estado de la conexión con la BD
    *
    * @param estado : Nuevo estado de la conexión
    */ 
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    //-----------------------------------------------------------
    
    /**
     * Función getResultado. Retorna el resultado de la conexión con la BD
     *    
     */
     public String getResultado() {
         return resultado;
     }
     //--------------------------------------------------------
}
