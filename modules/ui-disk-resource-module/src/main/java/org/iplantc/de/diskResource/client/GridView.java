package org.iplantc.de.diskResource.client;

import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.diskResource.client.events.DiskResourceSelectionChangedEvent;
import org.iplantc.de.diskResource.client.events.FolderPathSelectedEvent;
import org.iplantc.de.diskResource.client.events.FolderSelectionEvent;
import org.iplantc.de.diskResource.client.presenters.proxy.FolderContentsLoadConfig;
import org.iplantc.de.diskResource.client.events.DiskResourceNameSelectedEvent;
import org.iplantc.de.diskResource.client.events.ManageCommentsEvent;
import org.iplantc.de.diskResource.client.events.ManageMetadataEvent;
import org.iplantc.de.diskResource.client.events.ManageSharingEvent;
import org.iplantc.de.diskResource.client.events.RequestDiskResourceFavoriteEvent;
import org.iplantc.de.diskResource.client.events.ShareByDataLinkEvent;
import org.iplantc.de.diskResource.client.views.grid.DiskResourceColumnModel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

import com.sencha.gxt.data.shared.loader.BeforeLoadEvent;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.container.HasLayout;
import com.sencha.gxt.widget.core.client.grid.LiveGridCheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import java.util.List;

/**
 * Created by jstroot on 1/26/15.
 * @author jstroot
 */
public interface GridView extends IsWidget,
                                  HasLayout,
                                  FolderPathSelectedEvent.HasFolderPathSelectedEventHandlers,
                                  FolderSelectionEvent.FolderSelectionEventHandler,
                                  DiskResourceNameSelectedEvent.HasDiskResourceNameSelectedEventHandlers,
                                  DiskResourceSelectionChangedEvent.HasDiskResourceSelectionChangedEventHandlers {
    interface Appearance {

        String actionsColumnLabel();

        int actionsColumnWidth();

        String createdDateColumnLabel();

        int createdDateColumnWidth();

        String dataDragDropStatusText(int totalSelectionCount);

        String lastModifiedColumnLabel();

        int lastModifiedColumnWidth();

        int liveGridViewRowHeight();

        int liveToolItemWidth();

        String nameColumnLabel();

        int nameColumnWidth();

        String pathColumnLabel();

        int pathColumnWidth();

        String pathFieldLabel();

        int pathFieldLabelWidth();

        String pathFieldEmptyText();

        String permissionErrorMessage();

        int selectionStatusItemWidth();

        void setPagingToolBarStyle(ToolBar pagingToolBar);

        String sizeColumnLabel();

        int sizeColumnWidth();

        String gridViewEmptyText();
    }

    interface Presenter extends DiskResourceNameSelectedEvent.DiskResourceNameSelectedEventHandler,
                                ManageSharingEvent.ManageSharingEventHandler,
                                ManageMetadataEvent.ManageMetadataEventHandler,
                                ShareByDataLinkEvent.ShareByDataLinkEventHandler,
                                RequestDiskResourceFavoriteEvent.RequestDiskResourceFavoriteEventHandler,
                                ManageCommentsEvent.ManageCommentsEventHandler,
                                FolderSelectionEvent.FolderSelectionEventHandler {

        void deSelectDiskResources();

        void doMoveDiskResources(Folder targetFolder, List<DiskResource> resources);

        Element findGridRow(Element eventTargetElement);

        int findGridRowIndex(Element targetRow);

        List<DiskResource> getAllDiskResources();

        List<DiskResource> getSelectedDiskResources();

        Folder getSelectedUploadFolder();

        GridView getView();

        boolean isSelectAllChecked();

        void loadFolderContents(Folder folderToSelect);

        void setFilePreviewEnabled(boolean filePreviewEnabled);

        void setParentPresenter(DiskResourceView.Presenter parentPresenter);

        void setSelectedDiskResources(List<? extends HasId> diskResourcesToSelect);

        void setSelectedDiskResourcesById(List<? extends HasId> diskResourcesToSelect);

        void unRegisterHandler(EventHandler handler);

        DiskResource updateDiskResource(DiskResource diskResource);
    }

    HandlerRegistration addBeforeLoadHandler(BeforeLoadEvent.BeforeLoadHandler<FolderContentsLoadConfig> handler);

    Element findGridRow(Element eventTargetElement);

    int findGridRowIndex(Element targetRow);

    DiskResourceColumnModel getColumnModel();

    PagingLoader<FolderContentsLoadConfig,PagingLoadResult<DiskResource>> getGridLoader();

    LiveGridCheckBoxSelectionModel getSelectionModel();

    void setSingleSelect();
}
