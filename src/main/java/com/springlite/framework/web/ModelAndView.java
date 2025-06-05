package com.springlite.framework.web;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    
    private String viewName;
    private Map<String, Object> model;
    
    public ModelAndView() {
        this.model = new HashMap<>();
    }
    
    public ModelAndView(String viewName) {
        this.viewName = viewName;
        this.model = new HashMap<>();
    }
    
    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model != null ? model : new HashMap<>();
    }
    
    public String getViewName() {
        return viewName;
    }
    
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
    
    public Map<String, Object> getModel() {
        return model;
    }
    
    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
    
    public ModelAndView addObject(String attributeName, Object attributeValue) {
        this.model.put(attributeName, attributeValue);
        return this;
    }
    
    public ModelAndView addAllObjects(Map<String, ?> modelMap) {
        if (modelMap != null) {
            this.model.putAll(modelMap);
        }
        return this;
    }
    
    public void clear() {
        this.viewName = null;
        this.model.clear();
    }
    
    public boolean isEmpty() {
        return this.viewName == null && this.model.isEmpty();
    }
    
    @Override
    public String toString() {
        return "ModelAndView{" +
                "viewName='" + viewName + '\'' +
                ", model=" + model +
                '}';
    }
} 