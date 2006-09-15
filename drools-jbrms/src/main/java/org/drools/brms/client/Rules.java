package org.drools.brms.client;

import org.drools.brms.client.categorynav.CategorySelectHandler;
import org.drools.brms.client.categorynav.CategoryExplorerWidget;
import org.drools.brms.client.ruleeditor.RuleView;
import org.drools.brms.client.rulelist.EditItemEvent;
import org.drools.brms.client.rulelist.RuleListView;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TabPanel;


public class Rules extends JBRMSFeature {

    public static final int       EDITOR_TAB         = 1;
    
	public static ComponentInfo init() {
		return new ComponentInfo("Rules", "Find and edit rules.") {
			public JBRMSFeature createInstance() {
				return new Rules();
			}

			public Image getImage() {
				return new Image("images/rules.gif");
			}
		};
	}
	
	public Rules() {
		TabPanel tab = new TabPanel();
		tab.setWidth("100%");
		tab.setHeight("100%");
		initWidget(tab);
		
		
		HorizontalPanel explorePanel = doExplore(tab);
		
		RuleView ruleViewer = new RuleView();
		ruleViewer.setWidth("100%");
		ruleViewer.setHeight("100%");
		
		tab.add(explorePanel, "Explore");
		tab.add(ruleViewer, "Author");
		
		tab.selectTab(0);
		
		
	}

    /** This will setup the explorer tab */
	private HorizontalPanel doExplore(final TabPanel tab) {
		HorizontalPanel  panel = new HorizontalPanel();
        
        //setup the list
        final RuleListView list = new RuleListView(new EditItemEvent() {

            public void open(String[] rowData) {
                System.out.println("[Opening editor] " + rowData);
                tab.selectTab( EDITOR_TAB );                
            }
            
        });         
        
        //setup the nav, which will drive the list
		CategoryExplorerWidget nav = new CategoryExplorerWidget(new CategorySelectHandler() {

            public void selected(String selectedPath) {
                System.out.println("Selected path: " + selectedPath);  
                list.loadRulesForCategoryPath(selectedPath);
            }
            
        });			
		panel.add(nav);
        

		panel.add(list);
		return panel;
	}

}
