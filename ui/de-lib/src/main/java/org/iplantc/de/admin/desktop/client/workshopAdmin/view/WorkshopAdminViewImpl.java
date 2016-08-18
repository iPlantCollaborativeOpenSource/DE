package org.iplantc.de.admin.desktop.client.workshopAdmin.view;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import org.iplantc.de.admin.desktop.client.workshopAdmin.WorkshopAdminView;
import org.iplantc.de.admin.desktop.client.workshopAdmin.events.MemberSelectedEvent;
import org.iplantc.de.admin.desktop.client.workshopAdmin.model.MemberProperties;
import org.iplantc.de.admin.desktop.client.workshopAdmin.view.cells.MemberNameCell;
import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.models.groups.Member;
import org.iplantc.de.collaborators.client.util.UserSearchField;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author dennis
 */
public class WorkshopAdminViewImpl extends Composite implements WorkshopAdminView {

    interface WorkshopAdminViewImplUiBinder extends UiBinder<Widget, WorkshopAdminViewImpl> {}

    @UiField(provided = true) UserSearchField userSearch;
    @UiField TextButton deleteButton;
    @UiField Grid<Member> grid;
    @UiField(provided = true) ListStore<Member> listStore;
    @UiField(provided = true) WorkshopAdminViewAppearance appearance;

    private MemberProperties memberProperties;
    private final ArrayList<HandlerRegistration> globalHandlerRegistrations = new ArrayList<>();

    private static WorkshopAdminViewImplUiBinder uiBinder = GWT.create(WorkshopAdminViewImplUiBinder.class);

    private final class MemberNameComparator implements Comparator<Member> {

        @Override
        public int compare(Member o1, Member o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    @Inject
    public WorkshopAdminViewImpl(final WorkshopAdminViewAppearance appearance,
                                 final MemberProperties memberProperties,
                                 @Assisted ListStore<Member> listStore) {
        this.userSearch = new UserSearchField(userSearchEventTag);
        this.appearance = appearance;
        this.memberProperties = memberProperties;
        this.listStore = listStore;
        initWidget(uiBinder.createAndBindUi(this));
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
    }

    @Override
    public <H extends EventHandler> void addGlobalEventHandler(GwtEvent.Type<H> type, H handler) {
        HandlerRegistration registration = EventBus.getInstance().addHandler(type, handler);
        globalHandlerRegistrations.add(registration);
    }

    @Override
    protected void onUnload() {
        super.onUnload();

        // FIXME: it would be nice not to have to use the global event bus.
        for (HandlerRegistration registration : globalHandlerRegistrations) {
            registration.removeHandler();
        }
        globalHandlerRegistrations.clear();
    }

    @UiFactory
    ColumnModel<Member> createColumnModel() {
        List<ColumnConfig<Member, ?>> list = Lists.newArrayList();
        ColumnConfig<Member, Member> nameCol = new ColumnConfig<>(
                new IdentityValueProvider<Member>("name"),
                appearance.nameColumnWidth(),
                appearance.nameColumnLabel());
        ColumnConfig<Member, String> emailCol = new ColumnConfig<>(
                memberProperties.email(),
                appearance.emailColumnWidth(),
                appearance.emailColumnLabel());
        ColumnConfig<Member, String> institutionCol = new ColumnConfig<>(
                memberProperties.institution(),
                appearance.institutionColumnWidth(),
                appearance.institutionColumnLabel());
        nameCol.setCell(new MemberNameCell(this));
        nameCol.setComparator(new MemberNameComparator());
        list.add(nameCol);
        list.add(emailCol);
        list.add(institutionCol);
        return new ColumnModel<>(list);
    }

    public void memberSelected(Member member) {
        fireEvent(new MemberSelectedEvent(member));
    }
}
