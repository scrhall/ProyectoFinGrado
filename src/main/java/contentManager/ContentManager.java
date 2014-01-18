package contentManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentManager {
	private final Properties properties = new Properties();

	private static Logger logger = LoggerFactory.getLogger(ContentManager.class);
	private Session session = null;
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
 
	public ContentManager(){		
		
		try {
			properties.load(new FileInputStream("contentManagement.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: "+e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: "+e);
			e.printStackTrace();
		}
		properties.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		
		// Set the alfresco object factory
		properties.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
		
		// Create a session
		SessionFactory factory = SessionFactoryImpl.newInstance();	
		
		// Create session
	    List<Repository> repositories = factory.getRepositories((Map)properties);	     
	    if (repositories != null && !repositories.isEmpty()) {
	        this.session = repositories.get(0).createSession();
	    } else {
	        // logger.error("Not found repository");	        
	    }		
	}
	
	public Folder newFolder(String path){
		// Create the folder
		// Folder properties (minimal set: fileName and object type id)
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, path);
		Folder parent = this.session.getRootFolder().createFolder(properties);
		return parent;
	}
	
	public void newDoc(String fileName, String filePath, ContentStream content) {
		Folder parent;
		Map<String, Object> properties = new HashMap<String, Object>();
		
		// Create new folder
		try{
			parent = newFolder(filePath);
		}
		catch (CmisConstraintException e){
			parent = (Folder) session.getObjectByPath("/"+filePath+"/");
		}
		
		// File properties (minimal set: fileName and object type id)
		try{
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			properties.put(PropertyIds.NAME, fileName);
			// Create a major version
			parent.createDocument(properties, content, VersioningState.MAJOR);
		}
		catch (CmisConstraintException e){
			
		}
		
	}
	
	public void newDoc(String fileName, String filePath) {
		newDoc(fileName, filePath, null);
		
	}
	
	public String uploadFile(String targetPath, String fileName, String filePath, String contentType) {
		Folder folder = (Folder) session.getObjectByPath(targetPath);
		String idDoc = null;
		ContentStream contentStream = null;
		
		// Create contentStream from file
		// File properties (minimal set: fileName and object type id)
		// Create a major version
		try{
			contentStream = new ContentStreamImpl(fileName, null, contentType, new FileInputStream(filePath+fileName));
			Map<String, Object> properties = new HashMap<String, Object>();
		    properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		    properties.put(PropertyIds.NAME, contentStream.getFileName());
		    folder.createDocument(properties, contentStream , VersioningState.MAJOR);
		} 
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch (CmisConstraintException e){
			
		}
	    return idDoc;
		
	}

	public void updateDocProperties(String fileName, Map<String, Object> newProperties) {
		try{
			Document doc = (Document)session.getObjectByPath(fileName);
			doc.updateProperties(newProperties);
		}
		catch (CmisObjectNotFoundException e){
			
		}
		
	}

	public void moveDoc(String fileName, String sourcePath, String targetPath) {
		try{
			Document doc = (Document)session.getObjectByPath(sourcePath+fileName);
			Folder sourceFolder = (Folder)session.getObjectByPath(sourcePath);
			Folder targetFolder = (Folder)session.getObjectByPath(targetPath);
			doc.move(sourceFolder, targetFolder);
		}
		catch (CmisObjectNotFoundException e){
			
		}
	}

	public void copyDoc(String fileName, String targetPath) {
		try{
			Document doc = (Document)session.getObjectByPath(fileName);
			Folder targetFolder = (Folder)session.getObjectByPath(targetPath);
			doc.addToFolder(targetFolder, true);
		}
		catch (CmisObjectNotFoundException e){
			
		}
	}

	public String urlDoc(String fileName, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeDoc(String fileName) {
		try{
			Document doc = (Document)session.getObjectByPath(fileName);
			doc.delete();
		}
		catch (CmisObjectNotFoundException e){
			
		}
	}

}
