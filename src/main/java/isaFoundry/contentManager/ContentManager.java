package isaFoundry.contentManager;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.ConnectException;
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
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContentManager {

	private final Properties	properties	= new Properties();
	private static Logger		Log			= LoggerFactory.getLogger(ContentManager.class);
	private Session				session		= null;

	/**
	 * Constructor
	 */
	public ContentManager() {
		try {
			Log.info("Cargando configuracion del Gestor Documental.");
			this.properties.load(ContentManager.class.getResourceAsStream("/config/contentManagement.properties"));
		} catch (IOException e) {
			Log.error("Error: No se pudo cargar el archivo de configuracion del Gestor Documental.");
			e.printStackTrace();
		}
		this.connect();
	}

	/**
	 * Añade al final de un documento de nuestro repositorio una cadena de texto
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @param text
	 *            String con cadena de texto que va a ser añadida a el contenido
	 *            anterior del archivo
	 */
	public void appendDoc(String fileName, String sourcePath, String text) {
		try {
			Log.info("Añadiendo texto a un documento.");
			Document targetDocument = (Document) this.session.getObjectByPath(sourcePath + fileName);
			String newText = this.getContentAsString(targetDocument.getContentStream()) + "\n" + text;
			byte[] content = newText.getBytes();
			InputStream stream = new ByteArrayInputStream(content);
			ContentStream contentStream = new ContentStreamImpl(fileName , new BigInteger(content) , "text/plain" , stream);
			targetDocument.setContentStream(contentStream , true);
		} catch (Exception e) {
			Log.error("Error: No se pudo añadir el texto al documento.");
			e.printStackTrace();
		}
	}

	/**
	 * Copia un documento a un directorio de nuestro repositorio
	 * 
	 * @param doc
	 *            Documento que va a ser copiado
	 * @param targetFolder
	 *            Directorio de destino
	 * @param name
	 *            Nombre del documento
	 */
	public void copyDoc(Document doc, Folder targetFolder, String name) {
		try {
			Log.info("Copiando el documento '" + name + "' a la carpeta '" + targetFolder.getPath() + "'");
			String mimeType = doc.getContentStreamMimeType();
			ContentStream contentStream = new ContentStreamImpl(doc.getName() , null , mimeType , new DataInputStream(doc.getContentStream()
					.getStream()));
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
			properties.put(PropertyIds.NAME , name);
			targetFolder.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (CmisContentAlreadyExistsException e){
			
		
		} catch (Exception e) {
			Log.error("Error: No se pudo copiar el documento");
			e.printStackTrace();
		}
	}

	/**
	 * Copia un documento al directorio destino
	 * 
	 * @param filePath
	 *            Ruta completa del documento
	 * @param targetPath
	 *            Ruta del directorio destino
	 * @param name
	 *            Nombre del documento
	 */
	public void copyDoc(String filePath, String targetPath, String name) {
		Document doc = (Document) this.session.getObjectByPath(filePath);
		Folder targetFolder = (Folder) this.session.getObjectByPath(targetPath);
		this.copyDoc(doc , targetFolder , name);
	}

	/**
	 * Obtiene la url de un documento de nuestro repositorio a partir de un
	 * objeto Document
	 * 
	 * @param document
	 *            Objeto Document del cual queremos obtener la url
	 * @return Url del documento
	 */
	public String getDocumentURL(Document document) {
		Log.info("Recuperando la url del documento '" + document.getName() + "'");
		String link = null;
		try {
			Method loadLink = AbstractAtomPubService.class.getDeclaredMethod("loadLink" , new Class[] { String.class, String.class, String.class,
					String.class });
			loadLink.setAccessible(true);
			link = (String) loadLink.invoke(this.session.getBinding().getObjectService() , this.session.getRepositoryInfo().getId() ,
					document.getId() , AtomPubParser.LINK_REL_CONTENT , null);
		} catch (Exception e) {
			Log.error("Error: No se pudo recuperar la Url");
			e.printStackTrace();
		}
		return link;
	}

	/**
	 * Obtiene la url de un documento de nuestro repositorio a partir de su ruta
	 * y nombre
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @return url del documento
	 */
	public String getDocumentURL(String path) {
		Document doc = (Document) this.session.getObjectByPath(path);
		return this.getDocumentURL(doc);
	}

	/**
	 * Función que a partir de un objeto Document devuelve la url para su
	 * edición con google docs
	 * 
	 * @param doc
	 *            El objeto document
	 * @return Url para edición online mediante google docs
	 */
	public String getOnlineEditURL(Document doc) {
		String url = null;
		try {
			Log.info("Generando Url para la edicion del documento '" + doc.getName() + "'");
			String[] id = doc.getId().split(";");
			url = this.properties.getProperty("SERVER_URL") + "/share/page/document-details?nodeRef=" + id[0] ;
					//+ "&return=document-details%3FnodeRef%3D" + id[0];
			//url = this.properties.getProperty("SERVER_URL") + "share/page/googledocsEditor?nodeRef=" + id[0]
			//	  + "&return=repository%23filter%3Dpath%257C" + doc.getParents().get(0).getPath().toString() + "%26page%3D1";
			//url = this.properties.getProperty("SERVER_URL") + "share/page/googledocsEditor?nodeRef=" + id[0]
			//		  + "&return=repository%23filter%3Dpath%7C%2F" + doc.getParents().toString() + "%26page%3D1";
		} catch (Exception e) {
			Log.error("Error: No se pudo generar la Url");
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * Función que a partir de la ruta de un archivo en nuestro repositorio
	 * devuelve la url para su edición con google docs
	 * 
	 * @param path
	 *            ruta del archivo en nuestro repositorio
	 * @return url para edición online mediante google docs
	 */
	public String getOnlineEditURL(String path) {
		Document doc = null;
		try {
			doc = (Document) this.session.getObjectByPath(path);
		} catch (Exception e) {
			Log.error("Error: No se pudo generar la Url de edicion del documento.");
			e.printStackTrace();
		}
		return this.getOnlineEditURL(doc);
	}

	/**
	 * Mueve un documento desde el directorio origen al directorio destino
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @param targetPath
	 *            Ruta del directorio destino
	 */
	public void moveDoc(String fileName, String sourcePath, String targetPath) {
		try {
			Log.info("Moviendo el documento '" + fileName + "' de  '" + sourcePath + "' a '" + targetPath + "'.");
			Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
			Folder sourceFolder = (Folder) this.session.getObjectByPath(sourcePath);
			Folder targetFolder = (Folder) this.session.getObjectByPath(targetPath);
			doc.move(sourceFolder , targetFolder);
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: No se pudo mover el documento.");
			e.printStackTrace();
		}
	}

	/**
	 * Crea un documento de texto plano en nuestro repositorio sin contenido
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param targetPath
	 *            Ruta donde crearemos el documento
	 */
	public void newDoc(String fileName, String targetPath) {
		this.newDoc(fileName , targetPath , null);
	}

	/**
	 * crea un nuevo documento de texto plano en nuestro repositorio a partir de
	 * un String.
	 * 
	 * @param fileName
	 *            nombre del documento
	 * @param targetPath
	 *            ruta donde crearemos el documento
	 * @param text
	 *            String con cadena de texto que queremos incluir en el
	 *            documento
	 */
	public void newDoc(String fileName, String targetPath, String text) {
		Log.info("Creando el documento '" + fileName + "'.");
		byte[] content = text.getBytes();
		InputStream stream = new ByteArrayInputStream(content);
		ContentStream contentStream = new ContentStreamImpl(fileName , new BigInteger(content) , "text/plain" , stream);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
		properties.put(PropertyIds.NAME , fileName);
		try {
			// Crea la carpeta o la obtiene si ya esta creada
			Folder parent = this.newFolder(targetPath);
			parent.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (CmisConstraintException e) {
			Log.error("Error: No se pudo crear el documento.");
			e.printStackTrace();
		}
	}

	/**
	 * Crea un nuevo directorio en nuestro repositorio y lo devuelve como objeto
	 * Folder
	 * 
	 * @param path
	 *            Ruta de nuestro nuevo directorio
	 * @return Objeto Folder que representa nuestro nuevo directorio
	 */
	public Folder newFolder(String path) {
		Log.info("Creando carpeta con ruta '" + path + "'.");
		Folder parent;
		String[] aux = path.split("/");
		String name = aux[aux.length - 1];
		String folderPath = path.split(name)[0];
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:folder");
		properties.put(PropertyIds.NAME , name);
		if (path == "") {
			parent = this.session.getRootFolder();
		} else {
			try {
				parent = (Folder) this.session.getObjectByPath(folderPath);
				parent = parent.createFolder(properties);
			} catch (Exception e) {
				Log.info("La carpeta ya existe.");
				parent = (Folder) this.session.getObjectByPath(path);
			}
		}
		return parent;
	}

	/**
	 * Sobreescribe el contenido de un documento de nuestro repositorio con una
	 * cadena de texto
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @param text
	 *            String con cadena de texto que va a machacar el contenido
	 *            anterior del archivo
	 */
	public void overwriteDoc(String fileName, String sourcePath, String text) {
		Log.info("Sobreescribiendo el documento '" + fileName + "' en '" + sourcePath + "'.");
		byte[] content = text.getBytes();
		InputStream stream = new ByteArrayInputStream(content);
		ContentStream contentStream = new ContentStreamImpl(fileName , new BigInteger(content) , "text/plain" , stream);
		try {
			Document targetDocument = (Document) this.session.getObjectByPath(sourcePath + fileName);
			targetDocument.setContentStream(contentStream , true);
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: No se pudo sobreescribir el documento.");
			e.printStackTrace();
		}
	}

	/**
	 * Elimina un documento de nuestro repositorio
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Ruta del directorio origen
	 */
	public void removeDoc(String fileName, String sourcePath) {
		try {
			Log.info("Eliminando '" + fileName + "' de ' " + sourcePath + "'.");
			Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
			doc.delete();
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: No se pudo eliminar el documento.");
			e.printStackTrace();
		}
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * Transforma un documento de nuestro repositorio en pdf mediante el paso
	 * por una carpeta intermedia que
	 * tiene asignada una regla de transformación
	 * 
	 * @param fileName
	 *            Nombre del archivo
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @param targetPath
	 *            Ruta del directorio destino
	 * @param converterPath
	 *            Ruta del directorio de transformación
	 */
	public void toPDF(String fileName, String sourcePath, String targetPath, String converterPath) {
		Log.info("Generando PDF del documento '" + fileName + "'.");
		this.copyDoc(fileName , sourcePath , converterPath);
		int aux = fileName.indexOf('.');
		String newFile;
		if (aux != -1) {
			newFile = fileName.substring(0 , aux) + ".pdf";
		} else {
			newFile = fileName + ".pdf";
		}
		this.moveDoc(newFile , converterPath , targetPath);
		this.removeDoc(fileName , converterPath);
	}

	/**
	 * Actualiza las propiedades asociadas a un documento de nuestro repositorio
	 * 
	 * @param fileName
	 *            Nombre del documento
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @param newProperties
	 *            Mapa con las propiedades a ser actualizadas y sus valores
	 */
	public void updateDocProperties(String fileName, String sourcePath, Map<String, Object> newProperties) {
		try {
			Log.info("Añadiendo propiedades al documento '" + fileName + "'.");
			Document doc = (Document) this.session.getObjectByPath(sourcePath + fileName);
			doc.updateProperties(newProperties);
		} catch (CmisObjectNotFoundException e) {
			Log.error("Error: No se pudo añadir las propiedades al documento.");
			e.printStackTrace();
		}
	}

	/**
	 * Sube un archivo desde el sistema a nuestro repositorio
	 * 
	 * @param fileName
	 *            Nombre del archivo
	 * @param sourcePath
	 *            Ruta del directorio origen
	 * @param targetPath
	 *            Ruta del directorio destino
	 */
	public void uploadFile(String fileName, String sourcePath, String targetPath) {
		Log.info("Cargando documento '" + fileName + "' en el Gestor Documental");
		Folder folder = this.newFolder(targetPath);
		String contentType = new Tika().detect(sourcePath + fileName);
		try {
			ContentStream contentStream = new ContentStreamImpl(fileName , null , contentType , new FileInputStream(sourcePath + fileName));
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
			properties.put(PropertyIds.NAME , contentStream.getFileName());
			folder.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (Exception e) {
			Log.error("Error: No se pudo cargar el documento");
			e.printStackTrace();
		}
	}

	/**
	 * Inicia la conexion con alfresco mediante CMIS
	 * 
	 * @throws ConnectException
	 */
	private void connect() {
		Log.info("Conectando con el Gestor Documental...");
		Map<String, String> parameter = new HashMap<String, String>();
		// Se especifican los datos necesarios para la conexion
		parameter.put(SessionParameter.USER , this.properties.getProperty("USER"));
		parameter.put(SessionParameter.PASSWORD , this.properties.getProperty("PASSWORD"));
		parameter.put(SessionParameter.ATOMPUB_URL , this.properties.getProperty("ATOMPUB_URL"));
		parameter.put(SessionParameter.BINDING_TYPE , BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.OBJECT_FACTORY_CLASS , "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
		// Se crea una sesion para la conexion
		SessionFactory factory = SessionFactoryImpl.newInstance();
		List<Repository> repositories = factory.getRepositories(parameter);
		if ((repositories != null) && !repositories.isEmpty()) {
			// Se selecciona el primer repositorio y se crea abre una sesion
			// para la comunicacion
			this.session = repositories.get(0).createSession();
		} else {
			Log.error("Error al conectar al Gestor Documental, no se encontraron repositorios");
		}
	}

	/**
	 * devuelve un String con el contenido de un stream
	 * 
	 * @param stream
	 *            stream que queremos pasar a cadena de texto
	 * @return String con la cadena de texto que contiene el stream
	 * @throws IOException
	 */
	private String getContentAsString(ContentStream stream) throws IOException {
		InputStream in2 = stream.getStream();
		StringBuffer sbuf = null;
		sbuf = new StringBuffer(in2.available());
		int count;
		byte[] buf2 = new byte[100];
		while ((count = in2.read(buf2)) != -1) {
			for (int i = 0; i < count; i++) {
				sbuf.append((char) buf2[i]);
			}
		}
		in2.close();
		return sbuf.toString();
	}
}