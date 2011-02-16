/*******************************************************************************
 * Copyright (c) 2011 Bolton University, UK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 *******************************************************************************/
package uk.ac.bolton.archimate.editor.propertysections;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import uk.ac.bolton.archimate.editor.model.commands.EObjectFeatureCommand;
import uk.ac.bolton.archimate.model.IAccessRelationship;
import uk.ac.bolton.archimate.model.IArchimateElement;
import uk.ac.bolton.archimate.model.IArchimatePackage;


/**
 * Property Section for an Access Relationship
 * 
 * @author Phillip Beauvoir
 */
public class AccessRelationshipSection extends AbstractArchimatePropertySection {
    
    private static final String HELP_ID = "uk.ac.bolton.archimate.help.elementPropertySection";
    
    /**
     * Filter to show or reject this section depending on input value
     */
    public static class Filter implements IFilter {
        @Override
        public boolean select(Object object) {
            return object instanceof IAccessRelationship ||
                (object instanceof IAdaptable &&
                        ((IAdaptable)object).getAdapter(IArchimateElement.class) instanceof IAccessRelationship);
        }
    }

    /*
     * Adapter to listen to changes made elsewhere (including Undo/Redo commands)
     */
    private Adapter eAdapter = new AdapterImpl() {
        @Override
        public void notifyChanged(Notification msg) {
            Object feature = msg.getFeature();
            // Element Interface event (Undo/Redo and here!)
            if(feature == IArchimatePackage.Literals.ACCESS_RELATIONSHIP__ACCESS_TYPE) {
                refresh();
                fPage.labelProviderChanged(null); // Update Main label
            }
        }
    };
    
    private IAccessRelationship fAccessRelationship;

    private Combo fComboType;
    private boolean fIsUpdating;
    
    private static final String[] fComboTypeItems = {
        "Write",
        "Read",
        "Access"
    };
    
    @Override
    protected void createControls(Composite parent) {
        createCLabel(parent, "Access Type:", ITabbedLayoutConstants.STANDARD_LABEL_WIDTH, SWT.NONE);
        fComboType = new Combo(parent, SWT.READ_ONLY);
        fComboType.setItems(fComboTypeItems);
        fComboType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fComboType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(isAlive()) {
                    if(!fIsUpdating) {
                        getCommandStack().execute(new EObjectFeatureCommand("Access Type",
                                fAccessRelationship, IArchimatePackage.Literals.ACCESS_RELATIONSHIP__ACCESS_TYPE,
                                fComboType.getSelectionIndex()));
                    }
                }
            }
        });

        // Help ID
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELP_ID);
    }

    @Override
    protected void setElement(Object element) {
        // IAccessRelationship
        if(element instanceof IAccessRelationship) {
            fAccessRelationship = (IAccessRelationship)element;
        }
        // IAccessRelationship in a GEF Edit Part
        else if(element instanceof IAdaptable) {
            fAccessRelationship = (IAccessRelationship)((IAdaptable)element).getAdapter(IArchimateElement.class);
        }
        else {
            System.err.println("AccessRelationshipSection wants to display for " + element);
        }
    }
    
    @Override
    public void refresh() {
        // Populate fields...
        fIsUpdating = true;
        int type = fAccessRelationship.getAccessType();
        fComboType.select(type);
        fIsUpdating = false;
    }

    @Override
    protected Adapter getECoreAdapter() {
        return eAdapter;
    }
    
    @Override
    protected EObject getEObject() {
        return fAccessRelationship;
    }
}
