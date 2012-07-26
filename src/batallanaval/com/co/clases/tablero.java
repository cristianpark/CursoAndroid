package batallanaval.com.co.clases;


public class tablero {
	private Posicion posicion;
	private int x;
	private int y;
	
	public Posicion movimiento(int X, int Y){
		Posicion pos= new Posicion();
		pos.x=X;
		pos.y=Y;
		return pos;
	} 
	
	
public class Posicion {
	public int x;
	public int y;
}

}

