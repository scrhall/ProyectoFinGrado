package test;

import isaFoundry.contentManager.ContentManager;

import org.junit.Test;

public class JJGTtest {

	@Test
	public void test() {
		ContentManager alfresco = new ContentManager();
		//Folder parent = alfresco.getSession().getRootFolder();
		//alfresco.newDoc("Prueba1.txt", "Directorio1");
		//alfresco.newDoc("Prueba1.1.txt", "Directorio1");
		//alfresco.newDoc("Prueba2.txt", "NuevoDirectorio2");
		//alfresco.newDoc("Prueba123.txt", "Directorio3", "antes que nada");
		//alfresco.appendDoc("Prueba3.txt", "Directorio3", "Probando de nuevo2");
		//alfresco.newPath("Directorio1");
		//alfresco.uploadFile("Directorio3", "horario_grado_TI_13-14.pdf", "C:","application/pdf");
		//Map<String, Object> properties = new HashMap<String, Object>();
	    //properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
	    //properties.put(PropertyIds.NAME, new Double(Math.random()).toString() );
		//alfresco.updateDocProperties("/Directorio1/Prueba4.txt", properties);
		//alfresco.moveDoc("Prueba.txt", "/Directorio1/", "/Directorio3/");
		//alfresco.removeDoc("/Directorio3/Prueba.txt");
		//AllowableActions actions = alfresco.getSession().getObjectByPath("/Directorio3/Prueba.txt").getAllowableActions();
		//System.out.print(actions);
		//alfresco.copyDoc("prueba2", "", "pruebas");
		//alfresco.toPDF("prueba_alfresco", "Directorio3", "japarejo", "pdf");
		//System.out.println(alfresco.getSession());
		//System.out.println(alfresco.getDocumentURL("prueba2", ""));
		System.out.println(alfresco.getOnlineEditURL("/Procesos/Proyecto1/prueba.docx"));
		//String url = alfresco.getDocumentUrl("Prueba3.txt", "Directorio3");
		//System.out.println(url);
		//System.out.println(alfresco.getSession().getRepositoryInfo().getCapabilities());
		//http://localhost:8080/share/page/context/mine/googledocsEditor?nodeRef=workspace%3A%2F%2FSpacesStore%2F900a768c-9148-4d78-aa93-dedd6c95fd89&return=context%2Fmine%2Fdocument-details%3FnodeRef%3Dworkspace%3A%2F%2FSpacesStore%2F900a768c-9148-4d78-aa93-dedd6c95fd89
	}

}

