package isaFoundry;


import isaFoundry.core.Core;

import org.slf4j.LoggerFactory;


/**
 * Hello world!
 * 
 */
public class App {

	public static void main(String[] args) {
		LoggerFactory.getLogger(App.class);
		Core core = new Core();
		core.run();
	}
}
