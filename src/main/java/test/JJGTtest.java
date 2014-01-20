package test;

import isaFoundry.contentManager.ContentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.junit.Test;

public class JJGTtest {

	@Test
	public void test() {
		ContentManager alfresco = new ContentManager();
		Folder parent = alfresco.getSession().getRootFolder();
		alfresco.newDoc("Prueba1.txt", "Directorio1");
		//alfresco.newDoc("Prueba1.1.txt", "Directorio1");
		//alfresco.newDoc("Prueba2.txt", "NuevoDirectorio2");
		//alfresco.newDoc("Prueba2.2.txt", "NuevoDirectorio2");
		//ContentStream content = new ContentStreamImpl("Nuevo.txt", "plain/text", "Probando 1,2,3...");
		//alfresco.newDoc("Prueba3.txt", "Directorio3", content);
		//alfresco.newPath("Directorio1");
		//alfresco.uploadFile(parent, "horario_grado_TI_13-14.pdf", "C:/","application/pdf");
		//Map<String, Object> properties = new HashMap<String, Object>();
	    //properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
	    //properties.put(PropertyIds.NAME, new Double(Math.random()).toString() );
		//alfresco.updateDocProperties("/Directorio1/Prueba4.txt", properties);
		//alfresco.moveDoc("Prueba.txt", "/Directorio1/", "/Directorio3/");
		//alfresco.removeDoc("/Directorio3/Prueba.txt");
		//AllowableActions actions = alfresco.getSession().getObjectByPath("/Directorio3/Prueba.txt").getAllowableActions();
		//System.out.print(actions);
	}

}

