import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lotus.domino.AgentBase;
import lotus.domino.AgentContext;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import org.jfree.ui.RefineryUtilities;

public class JavaAgent extends AgentBase {

	private static final String VACATION_STRING = "VACATION";

	public void NotesMain() {

		try {
		Session session = getSession(); 
		AgentContext agentContext = session.getAgentContext(); 
		Database db = agentContext.getCurrentDatabase();
		View view = db.getView("BarView/By Person"); //Should use view alias instead of view name
		DocumentCollection collectors = agentContext.getUnprocessedDocuments();
		
		List selectedDocuments = new ArrayList();
		Document proxy = collectors.getFirstDocument();
		addDocumentToCollectionByProxy(db, selectedDocuments, proxy);
		while (proxy != null) {
			proxy = collectors.getNextDocument(proxy);
			addDocumentToCollectionByProxy(db, selectedDocuments, proxy);
		}
		
		HashMap projectToDocumentsMap = new HashMap();
		List projects = new ArrayList();
		for (int i=0; i<selectedDocuments.size(); i++) {
			Document doc = (Document) selectedDocuments.get(i);
			
			String type = (String) doc.getItemValue("PersonArrangementForm_Type").get(0);
			boolean isVacation = updateMap(projectToDocumentsMap, projects, doc, type);
			if (!isVacation) {
				String projectName = (String) doc.getItemValue("PersonArrangementForm_Subject").get(0);
				updateMap(projectToDocumentsMap, projects, doc, projectName);
			}
		}
		

		/* Show chart */
	    final ScheduleChart chart = new ScheduleChart("Schedule Chart", projects, projectToDocumentsMap);
	    chart.pack();
	    RefineryUtilities.centerFrameOnScreen(chart);
	    chart.setVisible(true);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean updateMap(HashMap projectToDocumentsMap, List projects,
			Document doc, String projectName) {
		if (projectName.equalsIgnoreCase(VACATION_STRING)) {
			internalUpdateMap(projectToDocumentsMap, projects, doc, VACATION_STRING);
			return true;
		} else if (projectName.equalsIgnoreCase("WORK")) {
			//Do nothing
		} else {
			internalUpdateMap(projectToDocumentsMap, projects, doc, projectName);
		}
		return false;
	}

	private void internalUpdateMap(HashMap projectToDocumentsMap,
			List projects, Document doc, String projectName) {
		if (!projects.contains(projectName)) {
			projects.add(projectName);
			List documents = new ArrayList();
			documents.add(doc);
			projectToDocumentsMap.put(projectName, documents);
		} else {
			List documents = (List) projectToDocumentsMap.get(projectName);
			documents.add(doc);
		}
	}
	
	private void addDocumentToCollectionByProxy(Database db,
			List selectedDocuments, Document proxy) throws NotesException {
		if (proxy != null) {
			Document doc = db.getDocumentByUNID(proxy.getItemValueString("parentDocUID"));
			selectedDocuments.add(doc);
		}
	}
}