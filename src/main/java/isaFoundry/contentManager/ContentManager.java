package isaFoundry.contentManager;


import java.io.ByteArrayInputStream;
import java.io.File;
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

import javax.xml.transform.stream.StreamResult;


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
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContentManager {

	private final Properties	properties	= new Properties();
	private static Logger		Log			= LoggerFactory.getLogger(ContentManager.class);
	private Session				session		= null;

	public ContentManager() {
		try {
			System.out.println(System.getProperty("user.dir"));
			this.properties.load(ContentManager.class.getResourceAsStream("/isaFoundry/configs/contentManagement.properties"));
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
			System.out.println("Error: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public void newDoc(String fileName, String filePath, String text) {
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
			Folder parent = this.newFolder(filePath);
			// Create a major version
			parent.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (CmisConstraintException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public void newDoc(String fileName, String filePath) {
		this.newDoc(fileName , filePath , null);
	}
	
	public void copyDoc(String fileName, String filePath, String targetPath) {
		try {
			Document doc = (Document) this.session.getObjectByPath("/"+ filePath +"/"+ fileName);
			Folder targetFolder = (Folder) this.session.getObjectByPath("/"+ targetPath +"/");
			doc.addToFolder(targetFolder , true);
		} catch (CmisObjectNotFoundException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public void moveDoc(String fileName, String sourcePath, String targetPath) {
		try {
			Document doc = (Document) this.session.getObjectByPath("/"+ sourcePath +"/"+ fileName);
			Folder sourceFolder = (Folder) this.session.getObjectByPath("/"+ sourcePath +"/");
			Folder targetFolder = (Folder) this.session.getObjectByPath("/"+ targetPath +"/");
			doc.move(sourceFolder , targetFolder);
		} catch (CmisObjectNotFoundException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}

	public void overwriteDoc(String fileName, String filePath, String text) {
		//content
		byte[] content = text.getBytes();
		InputStream stream = new ByteArrayInputStream(content);
		ContentStream contentStream = new ContentStreamImpl(fileName, new BigInteger(content), "text/plain", stream);
		try {
		Document targetDocument = (Document) this.session.getObjectByPath("/"+ filePath +"/"+ fileName);
		targetDocument.setContentStream(contentStream, true);
		} catch (CmisObjectNotFoundException e){
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public void appendDoc(String fileName, String filePath, String text) {
		try {
			//content
			Document targetDocument = (Document) this.session.getObjectByPath("/"+ filePath +"/"+ fileName);
			String newText = this.getContentAsString(targetDocument.getContentStream()) + "\n" + text;
			byte[] content = newText.getBytes();
			InputStream stream = new ByteArrayInputStream(content);
			ContentStream contentStream = new ContentStreamImpl(fileName, new BigInteger(content), "text/plain", stream);
			targetDocument.setContentStream(contentStream, true);
		} catch (CmisObjectNotFoundException e){
			System.out.println("Error: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}

	public void removeDoc(String fileName, String filePath) {
		try {
			Document doc = (Document) this.session.getObjectByPath("/"+ filePath +"/"+ fileName);
			doc.delete();
		} catch (CmisObjectNotFoundException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public void updateDocProperties(String fileName, String filePath, Map<String, Object> newProperties) {
		try {
			Document doc = (Document) this.session.getObjectByPath("/"+ filePath +"/"+ fileName);
			doc.updateProperties(newProperties);
		} catch (CmisObjectNotFoundException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public String getDocumentUrl(String fileName, String filePath) {
		Document document = (Document) this.session.getObjectByPath("/"+ filePath +"/"+ fileName);
		return this.getDocumentURL(document);
	}
	
	public String getDocumentURL(Document document) {
	    String link = null;
	    try {
	        Method loadLink = AbstractAtomPubService.class.getDeclaredMethod("loadLink", 
	            new Class[] { String.class, String.class, String.class, String.class });
	        loadLink.setAccessible(true);
	        link = (String) loadLink.invoke(this.session.getBinding().getObjectService(), this.session.getRepositoryInfo().getId(),
	            document.getId(), AtomPubParser.LINK_REL_CONTENT, null);
	    } catch (Exception e) {
	    	System.out.println("Error: " + e);
			e.printStackTrace();
	    }
	    return link;
	}
	
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
				parent = (Folder) this.session.getObjectByPath("/"+ path +"/");
			}
		}
		return parent;
	}

	public void uploadFile(String targetPath, String fileName, String filePath, String contentType) {
		Folder folder = this.newFolder(targetPath);
		this.uploadFile(folder, fileName, filePath, contentType);
	}
	
	public void uploadFile(Folder folder, String fileName, String filePath, String contentType) {
		ContentStream contentStream = null;
		// Create contentStream from file
		// File properties (minimal set: fileName and object type id)
		// Create a major version
		try {
			contentStream = new ContentStreamImpl(fileName , null , contentType , new FileInputStream("/"+ filePath +"/"+ fileName));
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID , "cmis:document");
			properties.put(PropertyIds.NAME , contentStream.getFileName());
			folder.createDocument(properties , contentStream , VersioningState.MAJOR);
		} catch (FileNotFoundException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		} catch (CmisConstraintException e) {
			System.out.println("Error: " + e);
			e.printStackTrace();
		}
	}
	
	public void toPDF(String fileName, String filePath, String targetPath){
		File file = new File(filePath + fileName);
		try{
			COSDocument doc = new COSDocument(file);
			PDDocument pdf = new PDDocument(doc);
			pdf.save(targetPath + fileName);
			pdf.close();	
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
	}
	
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
