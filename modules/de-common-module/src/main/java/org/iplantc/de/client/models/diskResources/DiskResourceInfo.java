/**
 *
 */
package org.iplantc.de.client.models.diskResources;


import org.iplantc.de.client.models.diskResources.DiskResource.PermissionValue;

import com.google.web.bindery.autobean.shared.AutoBean.PropertyName;

import java.util.Date;

/**
 * @author sriram
 *
 */
public interface DiskResourceInfo {

    @PropertyName("dir-count")
    int getDirCount();

    @PropertyName("dir-count")
    void setDirCount(int count);

    @PropertyName("file-count")
    void setFileCount(int count);

    @PropertyName("file-count")
    int getFileCount();

    @PropertyName("share-count")
    int getShareCount();

    @PropertyName("share-count")
    void setShareCount(int count);

    @PropertyName("permission")
    PermissionValue getPermission();

    @PropertyName("type")
    String getType();

    @PropertyName("type")
    void setType(String type);

    @PropertyName("date-created")
    Date getCreated();

    @PropertyName("date-created")
    void setCreated(Date created);

    @PropertyName("date-modified")
    Date getModified();

    @PropertyName("date-modified")
    void setModified(Date modified);

    @PropertyName("file-size")
    void setSize(long size);

    @PropertyName("file-size")
    long getSize();

    @PropertyName("mime-type")
    String getFileType();

    @PropertyName("mime-type")
    void setFileType(String mimeType);
    
    @PropertyName("info-type")
    String getInfoType();
    
    @PropertyName("info-type")
    void setInfoType(String infoType);
    
}
