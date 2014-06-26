/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package org.iplantc.de.commons.client.tags.views;

import org.iplantc.de.client.models.tags.IplantTag;
import org.iplantc.de.commons.client.tags.presenter.TagListHandlers;
import org.iplantc.de.commons.client.tags.presenter.TagListHandlers.InsertionPoint;
import org.iplantc.de.commons.client.tags.resources.CustomIplantTagResources;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author cbopp
 * 
 */
public class TagView extends Composite {
    interface Binder extends UiBinder<Widget, TagView> {
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField
    HTMLPanel tagPanel;
    @UiField
    DivElement tag;
    @UiField
    Label value;
    private TagListHandlers uiHandlers;

    @UiField
    DivElement editOption;

    @UiField
    DivElement deleteOption;

    private HandlerRegistration handlerRegistration;

    private final List<HandlerRegistration> dndHandlers = new ArrayList<HandlerRegistration>();

    private boolean editable;

    private final CustomIplantTagResources resources;
    private static TagView draggedElement;
    Logger logger = Logger.getLogger("tags");

    public void setUiHandlers(TagListHandlers tagListHandlers) {
        this.uiHandlers = tagListHandlers;
    }

    public TagView(CustomIplantTagResources resources, IplantTag tag) {
        this.resources = resources;
        initWidget(uiBinder.createAndBindUi(this));

        this.tag.setAttribute("class", resources.style().tag());
        this.value.setStylePrimaryName(resources.style().tagCaption());

        this.value.setText(tag.getValue());
    }

    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            this.editable = editable;

            if (editable) {
                this.activateDeleteButton();
                this.activateEditButton();
                this.activateDnD();
                tagPanel.addDomHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        Element e = Element.as(event.getNativeEvent().getEventTarget());
                        logger.log(Level.SEVERE, e.getClassName());
                        if (e.getClassName().contains("tagEdit")) {
                            uiHandlers.onEditTag(TagView.this);
                        } else if (e.getClassName().contains("tagDelete")) {
                            uiHandlers.onRemoveTag(TagView.this);
                        }
                    }
                }, ClickEvent.getType());

                editOption.setTitle(I18N.DISPLAY.edit());
                deleteOption.setTitle(I18N.DISPLAY.remove());
            } else {
                deactivateDnD();
                // TODO: should also remove all handlers
            }
        }

    }

    private void activateEditButton() {

        tagPanel.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                editOption.addClassName(resources.style().tagEdit());
            }
        }, MouseOverEvent.getType());

        tagPanel.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                editOption.removeClassName(resources.style().tagEdit());
            }
        }, MouseOutEvent.getType());
    }

    private void activateDeleteButton() {

        tagPanel.addDomHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                deleteOption.addClassName(resources.style().tagDelete());

            }
        }, MouseOverEvent.getType());

        tagPanel.addDomHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                deleteOption.removeClassName(resources.style().tagDelete());

            }
        }, MouseOutEvent.getType());

    }

    private void activateDnD() {
        this.tagPanel.addStyleName(resources.style().tagEditable()); // Change cursor on hover

        // DnD
        this.getElement().setDraggable(Element.DRAGGABLE_TRUE);
        this.dndHandlers.add(this.addDomHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                event.setData("text", "");
                draggedElement = TagView.this;
                uiHandlers.onFocus();
            }
        }, DragStartEvent.getType()));

        this.dndHandlers.add(this.addDomHandler(new DragEnterHandler() {
            @Override
            public void onDragEnter(DragEnterEvent event) {
                if (draggedElement.equals(TagView.this))
                    return;

                // Calculate the mouse's percentage X position relative to the drag over element
                // 0 = left border; 100 = right border
                int percentagePositionX = (event.getNativeEvent().getClientX() - getAbsoluteLeft())
                        * 100 / getElement().getClientWidth();
                if (percentagePositionX < 50) {
                    tagPanel.addStyleName(resources.style().previewLeft());
                    tagPanel.removeStyleName(resources.style().previewRight());

                } else {
                    tagPanel.addStyleName(resources.style().previewRight());
                    tagPanel.removeStyleName(resources.style().previewLeft());
                }
            }
        }, DragEnterEvent.getType()));

        this.dndHandlers.add(this.addDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                if (draggedElement.equals(TagView.this))
                    return;

                // Calculate the mouse's percentage X position relative to the drag over element
                // 0 = left border; 100 = right border
                int percentagePositionX = (event.getNativeEvent().getClientX() - getAbsoluteLeft())
                        * 100 / getElement().getClientWidth();
                if (percentagePositionX < 50) {
                    tagPanel.addStyleName(resources.style().previewLeft());
                    tagPanel.removeStyleName(resources.style().previewRight());

                } else {
                    tagPanel.addStyleName(resources.style().previewRight());
                    tagPanel.removeStyleName(resources.style().previewLeft());
                }
            }
        }, DragOverEvent.getType()));

        this.dndHandlers.add(this.addDomHandler(new DragLeaveHandler() {
            @Override
            public void onDragLeave(DragLeaveEvent event) {
                if (draggedElement.equals(TagView.this))
                    return;

                tagPanel.removeStyleName(resources.style().previewLeft());
                tagPanel.removeStyleName(resources.style().previewRight());
            }
        }, DragLeaveEvent.getType()));

        this.dndHandlers.add(this.addDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                if (draggedElement.equals(TagView.this))
                    return;

                event.preventDefault();
                uiHandlers.onRelocateTag(draggedElement,
                                         TagView.this,
                                         tagPanel.getStyleName().contains(resources.style()
                                                                                   .previewLeft()) ? InsertionPoint.BEFORE
                                                                                                  : InsertionPoint.AFTER);
                tagPanel.removeStyleName(resources.style().previewLeft());
                tagPanel.removeStyleName(resources.style().previewRight());
            }
        },
                                                DropEvent.getType()));

        this.dndHandlers.add(this.addDomHandler(new DragEndHandler() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                uiHandlers.onBlur();
            }
        }, DragEndEvent.getType()));
    }

    private void deactivateDnD() {
        this.tagPanel.removeStyleName(resources.style().tagEditable());

        for (HandlerRegistration dndHandler : this.dndHandlers)
            dndHandler.removeHandler();

        this.dndHandlers.clear();
    }
}
