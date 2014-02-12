package isaFoundry.contentManager;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.AbstractAtomPubService;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.AtomPubParser;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContentManager {

	private final Properties	properties	= new Properties();
	private static Logger		Log			= LoggerFactory.getLogger(ContentManager.class);
	private Session				session		= null;

	public ContentManager() {
		try {
			System.out.println(System.getProperty("user.dir"));
			this.properties.load(ContentManager.class.getResourceAsStream("/configs/contentManagement.properties"));
			Map<String, String> parameter = new HashMap<String, String>();
			parameter.put(SessionParameter.USER , this.properties.getProperty("USER"));
			parameter.put(SessionParameter.PASSWORD , this.properties.getProperty("PASSWORD"));
			// Specify the connection settings
			parameter.put(SessionParameter.ATOMPUB_URL , this.properties.getProperty("ATOMPUB_URL"));
			parameter.put(SessionParameter.BINDING_TYPE , BindingType.ATOMPUB.value());
			// Set the alfresco object factory
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS , "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
			// Create a session
			SessionFactory factory = SessionFactoryImpl.newInstance();
			// Create session
			List<Repository> repositories = factory.getRepositories(parameter);
			if ((repositories != null) && !repositories.isEmpty()) {
				this.session = repositories.get(0).createSession();
			} else {
				Log.error("Not found repository");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.error("Error: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * función que a partir de la ruta de un archivo en nuestro repositorio devuelve la url para su edición con google docs
	 * 
	 * @param path ruta del archivo en nuestro repositorio
	 * @return url para edición online  mediante google docs
	 */
	public String getOnlineEditURL(String path){
		Document doc = null;
		try {
			doc = (Document) this.session.getObjectByPath(path);
		} catch (Exception e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
		return getOnlineEditURL(doc);
	}
	
	/**
	 * función que a partir de un objeto Document devuelve la url para su edición con google docs
	 * 
	 * @param doc el objeto document
	 * @return url para edición online mediante google docs
	 */
	public String getOnlineEditURL(Document doc){
		String[] id = doc.getId().split(";");
		String url =  this.properties.getProperty("SERVER_URL") 
				+"share/page/googledocsEditor?nodeRef="+ id[0]
				+"&return=context%2Fmine%2Fdocument-details%3FnodeRef%3D"+ id[0];
		return url;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * crea un nuevo documento de texto plano en nuestro repositorio a partir de un String.
	 * 
	 * @param fileName nombre del documento
	 * @param targetPath ruta donde crearemos el documento
	 * @param text String con cadena de texto que queremos incluir en el documento
	 */
	public void newDoc(String fileName, String targetPath, String text) {
		//content
		byte[] content = text.getBytes();
		InputStream stream = new ByteArrayInputStream(content);
		ContentStream contentStream = new ContentStreamImpl(fileName, new BigInteger(content), "text/plain", stream);
		Map<String, Object> properties = new HashMap<String, Object>();
		// File properties (minimal set: fileName and object type id)
		properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
		properties.put(PropertyIds.NAME , fileName);
		try {
			// Get parent folder or create it
			Folder parent = this.newFolder(targetPath);
			// Create a major version
			parent.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (CmisConstraintException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * crea un documento de texto plano en nuestro repositorio sin contenido
	 * 
	 * @param fileName nombre del documento
	 * @param targetPath ruta donde crearemos el documento
	 */
	public void newDoc(String fileName, String targetPath) {
		this.newDoc(fileName , targetPath , null);
	}
	
	/**
	 * copia un documento al directorio destino
	 * 
	 * @param filePath ruta completa del documento
	 * @param targetPath ruta del directorio destino
	 */
	public void copyDoc(String filePath, String targetPath) {
		Document doc = (Document) this.session.getObjectByPath(filePath);
		Folder targetFolder = (Folder) this.session.getObjectByPath(targetPath);
		this.copyDoc(doc, targetFolder);
	}
	
	/**
	 * copia un documento desde el directorio origen al directorio destino
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 * @param targetPath ruta del directorio destino
	 */
	public void copyDoc(String fileName, String sourcePath, String targetPath) {
		Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
		Folder targetFolder = (Folder) this.session.getObjectByPath(targetPath);
		this.copyDoc(doc, targetFolder);
	}
	
	/**
	 * copia un documento a un directorio de nuestro repositorio
	 * 
	 * @param doc documento que va a ser copiado
	 * @param targetFolder directorio de destino
	 */
	public void copyDoc(Document doc, Folder targetFolder) {
		try {
			String mimeType = doc.getContentStreamMimeType();
			ContentStream contentStream = new ContentStreamImpl(doc.getName(), null, mimeType, new DataInputStream(doc.getContentStream().getStream()));
			Map<String, Object> properties = new HashMap<String, Object>();
			// File properties (minimal set: fileName and object type id)
			properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
			properties.put(PropertyIds.NAME , doc.getName());
			targetFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		} catch (CmisConstraintException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * mueve un documento desde el directorio origen al directorio destino
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 * @param targetPath ruta del directorio destino
	 */
	public void moveDoc(String fileName, String sourcePath, String targetPath) {
		try {
			Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
			Folder sourceFolder = (Folder) this.session.getObjectByPath(sourcePath);
			Folder targetFolder = (Folder) this.session.getObjectByPath(targetPath);
			doc.move(sourceFolder , targetFolder);
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * sobreescribe el contenido de un documento de nuestro repositorio con una cadena de texto 
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 * @param text String con cadena de texto que va a machacar el contenido anterior del archivo
	 */
	public void overwriteDoc(String fileName, String sourcePath, String text) {
		//content
		byte[] content = text.getBytes();
		InputStream stream = new ByteArrayInputStream(content);
		ContentStream contentStream = new ContentStreamImpl(fileName, new BigInteger(content), "text/plain", stream);
		try {
		Document targetDocument = (Document) this.session.getObjectByPath(sourcePath + fileName);
		targetDocument.setContentStream(contentStream, true);
		} catch (CmisObjectNotFoundException e){
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * añade al final de un documento de nuestro repositorio una cadena de texto  
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 * @param text String con cadena de texto que va a ser añadida a el contenido anterior del archivo
	 */
	public void appendDoc(String fileName, String sourcePath, String text) {
		try {
			//content
			Document targetDocument = (Document) this.session.getObjectByPath(sourcePath + fileName);
			String newText = this.getContentAsString(targetDocument.getContentStream()) + "\n" + text;
			byte[] content = newText.getBytes();
			InputStream stream = new ByteArrayInputStream(content);
			ContentStream contentStream = new ContentStreamImpl(fileName, new BigInteger(content), "text/plain", stream);
			targetDocument.setContentStream(contentStream, true);
		} catch (CmisObjectNotFoundException e){
			Log.error("Error: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * elimina un documento de nuestro repositorio
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 */
	public void removeDoc(String fileName, String sourcePath) {
		try {
			Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
			doc.delete();
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * actualiza las propiedades asociadas a un documento de nuestro repositorio
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 * @param newProperties mapa con las propiedades a ser actualizadas y sus valores
	 */
	public void updateDocProperties(String fileName, String sourcePath, Map<String, Object> newProperties) {
		try {
			Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
			doc.updateProperties(newProperties);
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * obtiene la url de un documento de nuestro repositorio a partir de su ruta y nombre
	 * 
	 * @param fileName nombre del documento
	 * @param sourcePath ruta del directorio origen
	 * @return url del documento
	 */
	public String getDocumentURL(String path) {
		Document doc = (Document) this.session.getObjectByPath(path);
		return this.getDocumentURL(doc);
	}
	
	/**
	 * obtiene la url de un documento de nuestro repositorio a partir de un objeto Document
	 * 
	 * @param document objeto Document del cual queremos obtener la url
	 * @return url del documento
	 */
	public String getDocumentURL(Document document) {
	    String link = null;
	    try {
	        Method loadLink = AbstractAtomPubService.class.getDeclaredMethod("loadLink", 
	            new Class[] { String.class, String.class, String.class, String.class });
	        loadLink.setAccessible(true);
	        link = (String) loadLink.invoke(this.session.getBinding().getObjectService(), this.session.getRepositoryInfo().getId(),
	            document.getId(), AtomPubParser.LINK_REL_CONTENT, null);
	    } catch (Exception e) {
	    	Log.error("Error: " + e);
			e.printStackTrace();
	    }
	    return link;
	}
	
	/**
	 * crea un nuevo directorio en nuestro repositorio y lo devuelve como objeto Folder
	 * 
	 * @param path ruta de nuestro nuevo directorio
	 * @return objeto Folder que representa nuestro nuevo directorio
	 */
	public Folder newFolder(String path) {
		// Create the folder
		Folder parent;
		// Folder properties (minimal set: fileName and object type id)
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:folder");
		properties.put(PropertyIds.NAME , path);
		if (path == "")
			parent = this.session.getRootFolder();
		else {
			try {
				parent = this.session.getRootFolder().createFolder(properties);
			} catch (CmisConstraintException e){
				parent = (Folder) this.session.getObjectByPath(path);
			}
		}
		return parent;
	}

	/**
	 * sube un archivo desde el sistema a nuestro repositorio
	 * 
	 * @param fileName nombre del archivo
	 * @param sourcePath ruta del directorio origen
	 * @param targetPath ruta del directorio destino
	 */
	public void uploadFile(String fileName, String sourcePath,  String targetPath) {
		Folder folder = this.newFolder(targetPath);
		String contentType =  new Tika().detect(sourcePath + fileName);
		try {
			ContentStream contentStream = new ContentStreamImpl(fileName , null , contentType , new FileInputStream(sourcePath + fileName));
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
			properties.put(PropertyIds.NAME , contentStream.getFileName());
			folder.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (FileNotFoundException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		} catch (CmisConstraintException e) {
			Log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * transforma un documento de nuestro repositorio en pdf mediante el paso por una carpeta intermedia que
	 * tiene asignada una regla de transformación
	 * 
	 * @param fileName nombre del archivo
	 * @param sourcePath ruta del directorio origen
	 * @param targetPath ruta del directorio destino
	 * @param converterPath ruta del directorio de transformación
	 */
	public void toPDF(String fileName, String sourcePath, String targetPath, String converterPath){
		this.copyDoc(fileName, sourcePath, converterPath);
		int aux = fileName.indexOf('.');
		String newFile;
		if (aux!=-1)
			newFile = fileName.substring(0, aux)+".pdf";
		else 
			newFile = fileName+".pdf";
		this.moveDoc(newFile, converterPath, targetPath);
		this.removeDoc(fileName, converterPath);
	}
	
	/**
	 * devuelve un String con el contenido de un stream
	 * 
	 * @param stream stream que queremos pasar a cadena de texto
	 * @return String con la cadena de texto que contiene el stream
	 * @throws IOException
	 */
	private String getContentAsString(ContentStream stream) throws IOException {
		InputStream in2 = stream.getStream();
		StringBuffer sbuf = null;
		sbuf = new StringBuffer(in2.available());
		int count;
		byte[] buf2 = new byte[100];
		while ((count = in2.read(buf2)) != -1)
		{
			for (int i = 0; i < count; i++)
			{
				sbuf.append((char) buf2[i]);
			}
		}
		in2.close();
		return sbuf.toString();
	}
}