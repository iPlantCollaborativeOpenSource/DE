package org.iplantc.de.apps.client.views.details;

import org.iplantc.de.apps.client.AppDetailsView;
import org.iplantc.de.apps.client.events.AppUpdatedEvent;
import org.iplantc.de.apps.client.events.selection.AppDetailsDocSelected;
import org.iplantc.de.apps.client.events.selection.AppFavoriteSelectedEvent;
import org.iplantc.de.apps.client.events.selection.AppRatingDeselected;
import org.iplantc.de.apps.client.events.selection.AppRatingSelected;
import org.iplantc.de.apps.client.events.selection.SaveMarkdownSelected;
import org.iplantc.de.apps.client.views.details.doc.AppDocMarkdownDialog;
import org.iplantc.de.apps.client.views.grid.cells.AppFavoriteCellWidget;
import org.iplantc.de.apps.client.views.grid.cells.AppRatingCellWidget;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.apps.AppDoc;
import org.iplantc.de.client.models.tool.Tool;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.adapters.EditorSource;
import com.google.gwt.editor.client.adapters.ListEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;

import java.util.List;

/**
 * @author jstroot
 */
public class AppDetailsViewImpl extends Composite implements AppDetailsView,
                                                             SaveMarkdownSelected.SaveMarkdownSelectedHandler {


    @UiTemplate("AppDetailsViewImpl.ui.xml")
    interface AppInfoViewUiBinder extends UiBinder<TabPanel, AppDetailsViewImpl> { }

    static class HighlightEditor implements LeafValueEditor<String> {

        private final AppDetailsAppearance appearance;
        private final DivElement integratorNameDiv;
        private final String searchRegexPattern;

        public HighlightEditor(final AppDetailsAppearance appearance,
                               final DivElement integratorNameDiv,
                               final String searchRegexPattern) {
            this.appearance = appearance;
            this.integratorNameDiv = integratorNameDiv;
            this.searchRegexPattern = searchRegexPattern;
        }

        @Override
        public void setValue(String value) {
            integratorNameDiv.setInnerSafeHtml(appearance.highlightText(value, searchRegexPattern));
        }

        @Override
        public String getValue() {
            return null;
        }
    }

    /**
     * Editor source class for binding to App.getTools()
     */
    private class ToolEditorSource extends EditorSource<ToolDetailsView> {
        private final AccordionLayoutContainer toolsContainer;

        public ToolEditorSource(final AccordionLayoutContainer toolsContainer) {
            this.toolsContainer = toolsContainer;
        }

        @Override
        public ToolDetailsView create(int index) {
            final ToolDetailsView toolDetailsView = new ToolDetailsView();
            toolsContainer.insert(toolDetailsView.asWidget(), index);
            if(index == 0){
                toolsContainer.setActiveWidget(toolDetailsView.asWidget());
            }
            return toolDetailsView;
        }

        @Override
        public void dispose(ToolDetailsView subEditor) {
            subEditor.asWidget().removeFromParent();
        }
    }

    interface AppDetailsEditorDriver extends SimpleBeanEditorDriver<App, AppDetailsViewImpl> {}

    private final AppInfoViewUiBinder BINDER = GWT.create(AppInfoViewUiBinder.class);
    private final AppDetailsEditorDriver editorDriver = GWT.create(AppDetailsEditorDriver.class);

    @UiField @Path("") AppFavoriteCellWidget favIcon; // Bind to app

    @UiField(provided = true) final AppDetailsAppearance appearance;
    /**
     * FIXME Ensure highlighting
     */
    @UiField @Ignore DivElement integratorNameDiv;
    final HighlightEditor integratorName;
    @UiField InlineLabel integratorEmail;
    /**
     * FIXME Not bound directly. Value given at init/construction time
     */
    @UiField @Ignore DivElement categories;
    @UiField @Path("") AppRatingCellWidget ratings; // Bind to app
    @UiField @Ignore InlineHyperlink helpLink;
    @UiField AccordionLayoutContainer toolsContainer;
    /**
     * FIXME Ensure highlighting
     */
    @UiField @Ignore DivElement descriptionElement;
    final HighlightEditor description;
    @UiField @Path("integrationDate") DateLabel publishedOn;

    final ListEditor<Tool, ToolDetailsView> tools;
    private final App app;

    @Inject UserInfo userInfo;

    @Inject
    AppDetailsViewImpl(final AppDetailsView.AppDetailsAppearance appearance,
                       @Assisted final App app,
                       @Assisted final String searchRegexPattern,
                       @Assisted final List<List<String>> appGroupHierarchies) {
        this.appearance = appearance;
        this.app = app;

        initWidget(BINDER.createAndBindUi(this));

        // Set up highlighting editors
        integratorName = new HighlightEditor(appearance, integratorNameDiv, searchRegexPattern);
        description = new HighlightEditor(appearance, descriptionElement, searchRegexPattern);
        categories.setInnerSafeHtml(appearance.getCategoriesHtml(appGroupHierarchies));
        this.tools = ListEditor.of(new ToolEditorSource(toolsContainer));

        // Add self so that rating cell events will fire
        ratings.setHasHandlers(this);
        editorDriver.initialize(this);
        editorDriver.edit(app);
    }

    @Override
    public HandlerRegistration addAppDetailsDocSelectedHandler(AppDetailsDocSelected.AppDetailsDocSelectedHandler handler) {
        return addHandler(handler, AppDetailsDocSelected.TYPE);
    }

    @Override
    public HandlerRegistration addAppFavoriteSelectedEventHandlers(AppFavoriteSelectedEvent.AppFavoriteSelectedEventHandler handler) {
        return favIcon.addAppFavoriteSelectedEventHandlers(handler);
    }

    @Override
    public HandlerRegistration addAppRatingDeselectedHandler(AppRatingDeselected.AppRatingDeselectedHandler handler) {
        return addHandler(handler, AppRatingDeselected.TYPE);
    }

    @Override
    public HandlerRegistration addAppRatingSelectedHandler(AppRatingSelected.AppRatingSelectedHandler handler) {
        return addHandler(handler, AppRatingSelected.TYPE);
    }

    @Override
    public HandlerRegistration addSaveMarkdownSelectedHandler(SaveMarkdownSelected.SaveMarkdownSelectedHandler handler) {
        return addHandler(handler, SaveMarkdownSelected.TYPE);
    }

    @Override
    public void onAppUpdated(final AppUpdatedEvent event) {
        editorDriver.edit(event.getApp());
    }

    @Override
    public void onSaveMarkdownSelected(SaveMarkdownSelected event) {
        // Forward event
        fireEvent(event);
    }

    @Override
    public void showDoc(AppDoc appDoc) {
        AppDocMarkdownDialog markdownDialog = new AppDocMarkdownDialog(app, appDoc, userInfo);
        markdownDialog.show();
        markdownDialog.addSaveMarkdownSelectedHandler(this);
    }

    @UiHandler("helpLink")
    void onHelpSelected(ClickEvent event) {
        fireEvent(new AppDetailsDocSelected(app));
    }

    @UiFactory @Ignore
    DateLabel createDateLabel() {
        return new DateLabel(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM));
    }

    public static native String render(String val) /*-{
		var markdown = $wnd.Markdown.getSanitizingConverter();
		return markdown.makeHtml(val);
    }-*/;

}
