package isaFoundry.core;

import isaFoundry.contentManager.ContentManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Core {
	private Logger Log = LoggerFactory.getLogger(Core.class);
	private  ContentManager CManager;
	private  ProcessEngine processEngine;
	
	
	
	
	public Core() {
		ContentManager CManager=new ContentManager();
		processEngine=ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
	     
		LoadAllDefinitions();
	}

	private void LoadAllDefinitions() {
		//TODO: mirar todos los bpmn en diagrams y cargarlos todos
		final Collection<String> list = getResources(Pattern.compile(".bpmn"));
        for(final String name : list){
        	Log.info(name);
        }
		// Forma menos practica
		 RepositoryService repositoryService = processEngine.getRepositoryService();
	        repositoryService.createDeployment()
	          .addClasspathResource("FinalizacionProyecto.bpmn")
	          .deploy();
	        Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());            
	        repositoryService.createDeployment()
	        .addClasspathResource("CreacionProyecto.bpmn")
	        .deploy();
	      Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());     
	      repositoryService.createDeployment()
	      .addClasspathResource("Reuniones.bpmn")
	      .deploy();
	    Log.info("Number of process definitions: " + repositoryService.createProcessDefinitionQuery().count());            
	    
	}
        public static Collection<String> getResources(
                final Pattern pattern){
                final ArrayList<String> retval = new ArrayList<String>();
                final String classPath = System.getProperty("java.class.path", ".");
                final String[] classPathElements = classPath.split(":");
                for(final String element : classPathElements){
                    retval.addAll(getResources(element, pattern));
                }
                return retval;
            }
	private static Collection<String> getResources(
	        final String element,
	        final Pattern pattern){
	        final ArrayList<String> retval = new ArrayList<String>();
	        final File file = new File(element);
	        if(file.isDirectory()){
	            retval.addAll(getResourcesFromDirectory(file, pattern));
	        } else{
	            retval.addAll(getResourcesFromJarFile(file, pattern));
	        }
	        return retval;
	    }

	    private static Collection<String> getResourcesFromJarFile(
	        final File file,
	        final Pattern pattern){
	        final ArrayList<String> retval = new ArrayList<String>();
	        ZipFile zf;
	        try{
	            zf = new ZipFile(file);
	        } catch(final ZipException e){
	            throw new Error(e);
	        } catch(final IOException e){
	            throw new Error(e);
	        }
	        final Enumeration e = zf.entries();
	        while(e.hasMoreElements()){
	            final ZipEntry ze = (ZipEntry) e.nextElement();
	            final String fileName = ze.getName();
	            final boolean accept = pattern.matcher(fileName).matches();
	            if(accept){
	                retval.add(fileName);
	            }
	        }
	        try{
	            zf.close();
	        } catch(final IOException e1){
	            throw new Error(e1);
	        }
	        return retval;
	    }

	    private static Collection<String> getResourcesFromDirectory(
	        final File directory,
	        final Pattern pattern){
	        final ArrayList<String> retval = new ArrayList<String>();
	        final File[] fileList = directory.listFiles();
	        for(final File file : fileList){
	            if(file.isDirectory()){
	                retval.addAll(getResourcesFromDirectory(file, pattern));
	            } else{
	                try{
	                    final String fileName = file.getCanonicalPath();
	                    final boolean accept = pattern.matcher(fileName).matches();
	                    if(accept){
	                        retval.add(fileName);
	                    }
	                } catch(final IOException e){
	                    throw new Error(e);
	                }
	            }
	        }
	        return retval;
	    }
	/**
	 * Realiza el envio de un correo mediante la informacion proporcionada en xml.
	 * @param templatePath incluye toda la informacion del correo a enviar.
	 */
	public void sendEmail(String templatePath) {
		Document doc=readXmlFromUrl(templatePath);//Document doc=readXmlFromFile(templatePath)
		//TODO:	
		
	}
	
	/**
	 * Lee un xml desde una url.
	 * @param path url.
	 * @return
	 */
	public Document readXmlFromUrl(String path){
		try {
			URL url;
			String line;
			String xml="";
			url = new URL(path);
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = br.readLine()) != null){
				xml = xml + line;
			}			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			InputSource file = new InputSource();
			file.setCharacterStream(new StringReader(xml)); 
			Document doc;				
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Lee un xml desde un archivo.
	 * @param path
	 * @return
	 */
	public Document readXmlFromFile(String path){
		try {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();				
			Document doc;				
			doc = db.parse(new File(path));
			doc.getDocumentElement().normalize();
			return doc;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Copia un documento desde una ruta a otra.
	 * @param sourcePath 		origen.
	 * @param destinationPath 	destino.
	 */
	public void copyDoc(String sourcePath, String destinationPath) {
		CManager.copyDoc(sourcePath, destinationPath);
		
	}
	
	/**
	 * Recupera la url para la edicion del documento en linea.
	 * @param doc
	 * @return
	 */
	public String urlDocOnlineEdit(String doc) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Recupera la url para la obtencion del documento en formato pdf.
	 * @param doc
	 * @return
	 */
	public String urlDocPdf(String doc) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Recupera la url del documento.
	 * @param doc
	 * @return
	 */
	public String urlDoc(String doc) {

		// TODO Auto-generated method stub
		return null;
	}
}
