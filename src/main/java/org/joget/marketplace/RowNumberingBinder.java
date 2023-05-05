package org.joget.marketplace;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.plugin.base.PluginManager;
import org.joget.apps.form.model.FormRow;

public class RowNumberingBinder extends DataListBinderDefault {
    protected DataListBinder binder;
    
    public String getName() {
        return "Row Numbering";
    }

    public String getVersion() {
        return "7.0.0";
    }

    public String getDescription() {
        return "Row Numbering Plugin";
    }

    public DataListColumn[] getColumns() {
        return getBinder().getColumns();
    }

    public String getPrimaryKeyColumnName() {
        return getBinder().getPrimaryKeyColumnName();
    }

    public DataListCollection getData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows) {
        generateColumns(dataList);
        DataListCollection columns = getBinder().getData(dataList, getBinder().getProperties(), filterQueryObjects, sort, desc, start, rows);
        if(!columns.isEmpty()){
            for (int i = 0; i < columns.size(); i++){
                int currentIndex = i + 1 + start;
                if(columns.get(i).getClass().getName().equals("org.joget.apps.form.model.FormRow")) {
                    ((FormRow) columns.get(i)).setProperty("index", Integer.toString(currentIndex));
                }
                if(columns.get(i).getClass().getName().equals("java.util.HashMap")) {
                    ((HashMap) columns.get(i)).put("index", currentIndex);
                }
            }
        }
        return columns;
    }

    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        return getBinder().getDataTotalRowCount(dataList, getBinder().getProperties(), filterQueryObjects);
    }

    public String getLabel() {
        return "Row Numbering Binder";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/dynamicColumnsDatalistBinder.json", null, true, "message/datalist/DynamicColumnsDatalistBinder");
    }
    
    protected Object getObject(ResultSet rs, String mapping){
        try {
            Integer index = null;
            try {
                index = Integer.parseInt(mapping);
            } catch (Exception e) {}
            if (index != null) {
                return rs.getObject(index);
            } else {
                return rs.getObject(mapping);
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    protected void generateColumns(DataList dataList) {
        Collection<DataListColumn> columns = new ArrayList<DataListColumn>();

        // add an index column before iterating through the query result
        DataListColumn n = new DataListColumn();
        n.setName("index");
        n.setLabel("Index");
        n.setHidden(false);
        n.setProperty("id", "number");
        n.setProperty("sort", "true");
        columns.add(n);
        DataListColumn[] designColumns = dataList.getColumns();
        for(DataListColumn d : designColumns){
            columns.add(d);
        }
        dataList.setColumns(columns.toArray(new DataListColumn[]{}));
    }
    
    protected DataListBinder getBinder() {
        if (binder == null) {
            //get the binder
            Object binderData = getProperty("actual_databinder");
            if (binderData != null && binderData instanceof Map) {
                Map bdMap = (Map) binderData;
                if (bdMap != null && bdMap.containsKey("className") && !bdMap.get("className").toString().isEmpty()) {
                    PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                    binder = (DataListBinder) pluginManager.getPlugin(bdMap.get("className").toString());
                    
                    if (binder != null) {
                        Map bdProps = (Map) bdMap.get("properties");
                        binder.setProperties(bdProps);
                    }
                }
            }
        }
        return binder;
    }
}
