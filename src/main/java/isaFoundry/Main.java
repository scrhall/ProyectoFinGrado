package isaFoundry;


import isaFoundry.core.Core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

	/**
	 * Funcion Principal que se ejecutara al iniciar el programa
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Core core = new Core();
		core.start();
	}
}
