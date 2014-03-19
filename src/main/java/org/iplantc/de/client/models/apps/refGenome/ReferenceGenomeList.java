package org.iplantc.de.client.models.apps.refGenome;

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

import java.util.List;

public interface ReferenceGenomeList {

    @PropertyName("genomes")
    List<ReferenceGenome> getReferenceGenomes();
}
