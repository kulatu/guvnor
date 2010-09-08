/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.qa.testscenarios;

import java.util.List;

import org.drools.guvnor.client.common.DirtyableComposite;
import org.drools.guvnor.client.common.DropDownValueChanged;
import org.drools.guvnor.client.common.FieldEditListener;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.InfoPopup;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.modeldriven.ui.EnumDropDown;
import org.drools.ide.common.client.modeldriven.DropDownData;
import org.drools.ide.common.client.modeldriven.FieldNature;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.brl.ActionInsertFact;
import org.drools.ide.common.client.modeldriven.brl.FactPattern;
import org.drools.ide.common.client.modeldriven.testing.CallFieldValue;
import org.drools.ide.common.client.modeldriven.testing.ExecutionTrace;
import org.drools.ide.common.client.modeldriven.testing.FactData;
import org.drools.ide.common.client.modeldriven.testing.Scenario;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * This provides for editing of fields in the RHS of a rule.
 * 
 * @author Nicolas Heron
 * 
 */
public class MethodParameterCallValueEditor extends DirtyableComposite {

    private CallFieldValue methodParameter;
    private DropDownData        enums;
    private SimplePanel         root;
    private Constants           constants     = GWT.create( Constants.class );
    private Scenario        model         = null;
    private String              parameterType = null;
    private Command             onValueChangeCommand = null;
    private ExecutionTrace    ex;

    public MethodParameterCallValueEditor(final CallFieldValue val,
                                      final DropDownData enums,
                                      ExecutionTrace ex,
                                      Scenario model,
                                      String parameterType, Command onValueChangeCommand) {
        if ( val.type.equals( SuggestionCompletionEngine.TYPE_BOOLEAN ) ) {
            this.enums = DropDownData.create( new String[]{"true", "false"} );
        } else {
            this.enums = enums;
        }
        this.root = new SimplePanel();
        this.ex=ex;
        this.methodParameter = val;
        this.model = model;
        this.parameterType = parameterType;
        this.onValueChangeCommand = onValueChangeCommand;
        refresh();
        initWidget( root );
    }

    private void refresh() {
        root.clear();
        if ( enums != null && (enums.fixedList != null || enums.queryExpression != null) ) {
            root.add( new EnumDropDown( methodParameter.value,
                                        new DropDownValueChanged() {
                                            public void valueChanged(String newText,
                                                                     String newValue) {
                                                methodParameter.value = newValue;
                                                if (onValueChangeCommand != null){
                                                    onValueChangeCommand.execute();
                                                }
                                                makeDirty();
                                            }
                                        },
                                        enums ) );
        } else {
            // FIX nheron il faut ajouter les autres choix pour appeller les
            // bons editeurs suivant le type
            // si la valeur vaut 0 il faut mettre un stylo (

            if ( methodParameter.nature == FieldNature.TYPE_UNDEFINED ) {
                // we have a blank slate..
                // have to give them a choice
                root.add( choice() );
            } else {
                if ( methodParameter.nature == FieldNature.TYPE_VARIABLE ) {
                    ListBox list = boundVariable( methodParameter );
                    root.add( list );
                } else {
                    TextBox box = boundTextBox( this.methodParameter );
                    root.add( box );
                }

            }

        }
    }

    private ListBox boundVariable(final FieldNature c) {
        /*
         * If there is a bound variable that is the same type of the current
         * variable type, then propose a list
         */
        final ListBox listVariable = new ListBox();
        List<String> vars = model.getFactNamesInScope(ex, true);
        for ( String v : vars ) {
        	FactData factData=(FactData)model.getFactTypes().get(v);
            if ( factData.type.equals( this.methodParameter.type ) ) {
                // First selection is empty
                if ( listVariable.getItemCount() == 0 ) {
                    listVariable.addItem( "..." );
                }

                listVariable.addItem( "="+v );
            }
        }
        if ( methodParameter.value.equals( "=" ) ) {
            listVariable.setSelectedIndex( 0 );
        } else {
            for ( int i = 0; i < listVariable.getItemCount(); i++ ) {
                if ( listVariable.getItemText( i ).equals( methodParameter.value ) ) {
                    listVariable.setSelectedIndex( i );
                }
            }
        }
        if ( listVariable.getItemCount() > 0 ) {

        	listVariable.addChangeHandler(new ChangeHandler() {
				
				public void onChange(ChangeEvent event) {
                    methodParameter.value = listVariable.getValue( listVariable.getSelectedIndex() );
                    if (onValueChangeCommand != null){
                        onValueChangeCommand.execute();
                    }
                    makeDirty();
                    refresh();
                }
			});
        	
//            listVariable.addChangeListener( new ChangeListener() {
//                public void onChange(Widget arg0) {
//                    ListBox w = (ListBox) arg0;
//                    methodParameter.value = w.getValue( w.getSelectedIndex() );
//                    if (onValueChangeCommand != null){
//                        onValueChangeCommand.execute();
//                    }
//                    makeDirty();
//                    refresh();
//                }
//
//            } );
        }
        return listVariable;
    }

    private TextBox boundTextBox(final CallFieldValue c) {
        final TextBox box = new TextBox();
        box.setStyleName( "constraint-value-Editor" );
        if ( c.value == null ) {
            box.setText( "" );
        } else {
            if ( c.value.trim().equals( "" ) ) {
                c.value = "";
            }
            box.setText( c.value );
        }

        if ( c.value == null || c.value.length() < 5 ) {
            box.setVisibleLength( 6 );
        } else {
            box.setVisibleLength( c.value.length() - 1 );
        }

        box.addChangeListener( new ChangeListener() {
            public void onChange(Widget w) {
                c.value = box.getText();
                if (onValueChangeCommand != null) {
                    onValueChangeCommand.execute();
                }
                makeDirty();
            }

        } );

        box.addKeyboardListener( new FieldEditListener( new Command() {
            public void execute() {
                box.setVisibleLength( box.getText().length() );
            }
        } ) );

        if ( methodParameter.type.equals( SuggestionCompletionEngine.TYPE_NUMERIC ) ) {
            box.addKeyboardListener( getNumericFilter( box ) );
        }

        return box;
    }

    /**
     * This will return a keyboard listener for field setters, which will obey
     * numeric conventions - it will also allow formulas (a formula is when the
     * first value is a "=" which means it is meant to be taken as the user
     * typed)
     */
    public static KeyboardListener getNumericFilter(final TextBox box) {
        return new KeyboardListener() {

            public void onKeyDown(Widget arg0,
                                  char arg1,
                                  int arg2) {

            }

            public void onKeyPress(Widget w,
                                   char c,
                                   int i) {
                if ( Character.isLetter( c ) && c != '=' && !(box.getText().startsWith( "=" )) ) {
                    ((TextBox) w).cancelKey();
                }
            }

            public void onKeyUp(Widget arg0,
                                char arg1,
                                int arg2) {
            }

        };
    }

    private Widget choice() {
        Image clickme = new Image( "images/edit.gif" );
        clickme.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                showTypeChoice( w );
            }
        } );
        return clickme;
    }

    protected void showTypeChoice(Widget w) {
        final FormStylePopup form = new FormStylePopup( "images/newex_wiz.gif",
                                                        constants.FieldValue() );
        Button lit = new Button( constants.LiteralValue() );
        lit.addClickListener( new ClickListener() {
            public void onClick(Widget w) {
                methodParameter.nature = FieldNature.TYPE_LITERAL;
                methodParameter.value = " ";
                makeDirty();
                refresh();
                form.hide();
            }

        } );
        form.addAttribute( constants.LiteralValue() + ":",
                           widgets( lit,
                                    new InfoPopup( constants.Literal(),
                                                   constants.LiteralValTip() ) ) );
        form.addRow( new HTML( "<hr/>" ) );
        form.addRow( new SmallLabel( constants.AdvancedSection() ) );


        /*
         * If there is a bound variable that is the same type of the current
         * variable type, then show abutton
         */
        
        
        List<String> vars = model.getFactNamesInScope(ex, true);
         for ( String v : vars ) {
            boolean createButton = false;
            Button variable = new Button( constants.BoundVariable() );
            FactData factData=(FactData)model.getFactTypes().get(v);
            if ( factData.type.equals( this.parameterType ) ) {
            	createButton = true;
            }
            if ( createButton == true ) {
                form.addAttribute( constants.BoundVariable() + ":",
                                   variable );
                variable.addClickListener( new ClickListener() {
                    public void onClick(Widget w) {
                        methodParameter.nature = FieldNature.TYPE_VARIABLE;
                        methodParameter.value = "=";
                        makeDirty();
                        refresh();
                        form.hide();
                    }

                } );
                break;
            }

        }
        form.show();
    }

    private Widget widgets(Button lit,
                           InfoPopup popup) {
        HorizontalPanel h = new HorizontalPanel();
        h.add( lit );
        h.add( popup );
        return h;
    }

}